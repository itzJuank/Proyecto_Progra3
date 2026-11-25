package sifac.comun.logica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioInventarioMemoria;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.Proforma;
import sifac.comun.estructuras.Cola;

public class ServicioFacturacionConcurrenciaTest {
    private final Cliente cliente = new Cliente("104560789", "Cliente de prueba");
    private final Producto producto = new Producto("P001", "Martillo", BigDecimal.TEN);

    @Test(timeout = 10000)
    public void sigueEmitiendoMientrasElConsumidorProcesaYEntregaTodoEnOrden() throws Exception {
        int cantidad = 20;
        RepositorioInventarioMemoria inventario = new RepositorioInventarioMemoria();
        inventario.registrar(producto, 10);
        RepositorioDocumentosMemoria emitidos = new RepositorioDocumentosMemoria();
        DestinoConDemora destino = new DestinoConDemora(cantidad);

        try (ServicioFacturacion caja = new ServicioFacturacion(inventario, emitidos, destino)) {
            FutureTask<Void> cierre = new FutureTask<>(() -> {
                caja.close();
                return null;
            });
            try {
                caja.emitir(crearProforma());
                assertTrue(destino.envioIniciado.await(2, TimeUnit.SECONDS));
                assertEquals("envio-documentos", destino.nombreHilo);
                for (int numero = 2; numero <= cantidad; numero++) {
                    caja.emitir(crearProforma());
                }
                assertEquals(cantidad, emitidos.listar().size());
                assertTrue(destino.listar().isEmpty());
                Documento[] pendientes = caja.listarPendientes();
                assertEquals(cantidad, pendientes.length);
                for (int indice = 0; indice < cantidad; indice++) {
                    Documento documento = pendientes[indice];
                    assertEquals(indice + 1L, documento.getNumero());
                    assertEquals(EstadoDocumento.EMITIDO, documento.getEstado());
                }
                System.out.println("Receptor detenido en " + destino.nombreHilo + " | emitidos "
                        + emitidos.listar().size() + " | recibidos " + destino.listar().size());

                Thread hiloCierre = new Thread(cierre, "cierre-prueba");
                hiloCierre.start();
                assertThrows(TimeoutException.class, () -> cierre.get(100, TimeUnit.MILLISECONDS));
                Thread.State estadoCierre = hiloCierre.getState();
                assertEquals(Thread.State.WAITING, estadoCierre);
                assertThrows(IllegalStateException.class, () -> caja.emitir(crearProforma()));
                System.out.println("Cierre con la primera entrega detenida | " + estadoCierre);
            } finally {
                destino.permitirEnvio.countDown();
            }
            cierre.get(5, TimeUnit.SECONDS);
            assertEquals(0, caja.listarPendientes().length);
        }

        assertEquals(cantidad, destino.listar().size());
        assertEquals(cantidad, destino.cantidadRecibida);
        for (int indice = 0; indice < cantidad; indice++) {
            assertEquals(indice + 1L, destino.orden[indice]);
        }
        for (Documento documento : emitidos.listar()) {
            assertEquals(EstadoDocumento.ENVIADO, documento.getEstado());
        }
        System.out.println("Después del cierre | recibidos " + destino.cantidadRecibida
                + " | orden " + Arrays.toString(destino.orden));
    }

    @Test(timeout = 5000)
    public void conservaLosPendientesEnLaColaCuandoFallaElReceptor() throws InterruptedException {
        Cola<Documento> pendientes = new Cola<>();
        Documento primero = crearProforma();
        Documento segundo = crearProforma();
        primero.registrarEmision(1, LocalDate.now());
        segundo.registrarEmision(2, LocalDate.now());
        pendientes.encolar(primero);
        pendientes.encolar(segundo);

        IllegalStateException causa = new IllegalStateException("Fallo local simulado");
        ProcesadorEnvios procesador = new ProcesadorEnvios(pendientes, destinoConFallo(causa));
        Thread consumidor = new Thread(procesador, "envio-prueba");
        consumidor.start();
        consumidor.join();

        IllegalStateException aviso = assertThrows(IllegalStateException.class, procesador::verificarFuncionamiento);
        assertSame(causa, aviso.getCause());
        assertSame(primero, pendientes.desencolar());
        assertSame(segundo, pendientes.desencolar());
        assertEquals(EstadoDocumento.EMITIDO, primero.getEstado());
        assertEquals(EstadoDocumento.EMITIDO, segundo.getEstado());
        System.out.println("Fallo de recepción simulado | pendientes con estado EMITIDO "
                + primero.getNumero() + ", " + segundo.getNumero());
    }

    @Test(timeout = 5000)
    public void informaAlCerrarSiUnaEntregaQuedaPendiente() {
        RepositorioDocumentosMemoria emitidos = new RepositorioDocumentosMemoria();
        IllegalStateException causa = new IllegalStateException("Fallo local simulado");
        Documento documento = crearProforma();

        IllegalStateException aviso = assertThrows(IllegalStateException.class, () -> {
            try (ServicioFacturacion caja = new ServicioFacturacion(new RepositorioInventarioMemoria(),
                    emitidos, destinoConFallo(causa))) {
                caja.emitir(documento);
            }
        });

        assertSame(causa, aviso.getCause());
        assertEquals(EstadoDocumento.EMITIDO, documento.getEstado());
        assertSame(documento, emitidos.listar().get(0));
        System.out.println("Cierre con entrega pendiente | " + aviso.getMessage());
    }

    private Documento crearProforma() {
        Documento documento = new Proforma(cliente, BigDecimal.ZERO);
        documento.agregarLinea(new LineaDetalle(producto, 1));
        return documento;
    }

    private static RepositorioDocumentosMemoria destinoConFallo(IllegalStateException causa) {
        return new RepositorioDocumentosMemoria() {
            @Override
            public void guardar(Documento documento) {
                throw causa;
            }
        };
    }

    private static class DestinoConDemora extends RepositorioDocumentosMemoria {
        private final CountDownLatch envioIniciado = new CountDownLatch(1);
        private final CountDownLatch permitirEnvio = new CountDownLatch(1);
        private final long[] orden;
        private int cantidadRecibida;
        private String nombreHilo;

        DestinoConDemora(int cantidad) {
            orden = new long[cantidad];
        }

        @Override
        public void guardar(Documento documento) {
            if (cantidadRecibida == 0) {
                nombreHilo = Thread.currentThread().getName();
                envioIniciado.countDown();
                try {
                    if (!permitirEnvio.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Tiempo de espera de la prueba agotado");
                    }
                } catch (InterruptedException excepcion) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Prueba interrumpida", excepcion);
                }
            }
            super.guardar(documento);
            orden[cantidadRecibida] = documento.getNumero();
            cantidadRecibida++;
        }
    }
}
