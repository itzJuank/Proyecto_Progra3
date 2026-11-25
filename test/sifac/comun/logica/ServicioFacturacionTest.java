package sifac.comun.logica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioInventarioMemoria;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.EstadoDocumento;
import sifac.comun.entidades.FacturaContado;
import sifac.comun.entidades.FacturaCredito;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.NotaCredito;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.Proforma;

public class ServicioFacturacionTest {
    private final Cliente cliente = new Cliente("104560789", "Cliente de prueba");
    private final BigDecimal impuesto = new BigDecimal("10");
    private final RepositorioInventarioMemoria inventario = new RepositorioInventarioMemoria();
    private final RepositorioDocumentosMemoria emitidos = new RepositorioDocumentosMemoria();
    private final RepositorioDocumentosMemoria recibidos = new RepositorioDocumentosMemoria();
    private ServicioFacturacion servicio;

    @Before
    public void preparar() {
        inventario.registrar(new Producto("P003", "Arandela", new BigDecimal("0.05")), 10);
        inventario.registrar(new Producto("P001", "Martillo", new BigDecimal("10.00")), 5);
        inventario.registrar(new Producto("P002", "Brocha", new BigDecimal("4.00")), 8);
        servicio = new ServicioFacturacion(inventario, emitidos, recibidos);
    }

    @After
    public void cerrar() throws InterruptedException {
        servicio.close();
    }

    @Test
    public void calculaElTotalDeLosCuatroTipos() {
        Documento contado = new FacturaContado(cliente, impuesto);
        Documento credito = new FacturaCredito(cliente, impuesto);
        Documento nota = new NotaCredito(cliente, impuesto);
        Documento proforma = new Proforma(cliente, impuesto);
        for (Documento documento : List.of(contado, credito, nota, proforma)) {
            servicio.agregarLinea(documento, "P001", 2);
        }
        assertEquals(new BigDecimal("22.00"), contado.calcularTotal());
        assertEquals(new BigDecimal("22.00"), credito.calcularTotal());
        assertEquals(new BigDecimal("-22.00"), nota.calcularTotal());
        assertEquals(new BigDecimal("22.00"), proforma.calcularTotal());

        Documento redondeo = new FacturaContado(cliente, impuesto);
        servicio.agregarLinea(redondeo, "P003", 1);
        assertEquals(new BigDecimal("0.06"), redondeo.calcularTotal());
    }

    @Test
    public void rechazaUnDocumentoSinClienteOConImpuestoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new FacturaContado(null, impuesto));
        assertThrows(IllegalArgumentException.class,
                () -> new FacturaCredito(cliente, new BigDecimal("-1")));
    }

    @Test
    public void rechazaCantidadesQueNoSeanPositivas() {
        Documento documento = new FacturaContado(cliente, impuesto);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarLinea(documento, "P001", 0));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarLinea(documento, "P001", -1));
        assertEquals(0, documento.getLineas().length);
    }

    @Test
    public void consultaProductosYRechazaLosInexistentes() {
        assertEquals("Martillo", servicio.consultarProducto("P001").getNombre());
        assertEquals(5, servicio.consultarExistencias("P001"));
        assertEquals("P001", servicio.listarProductos().get(0).getCodigo());
        Documento documento = new FacturaContado(cliente, impuesto);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregarLinea(documento, "P999", 1));
        assertEquals(0, documento.getLineas().length);
    }

    @Test
    public void rechazaEmitirUnDocumentoVacio() {
        Documento documento = new FacturaContado(cliente, impuesto);
        assertThrows(IllegalStateException.class, () -> servicio.emitir(documento));
        assertEquals(EstadoDocumento.BORRADOR, documento.getEstado());
        assertTrue(emitidos.listar().isEmpty());
    }

    @Test
    public void rechazaDeshacerSiElBorradorEstaVacio() {
        Documento documento = new FacturaContado(cliente, impuesto);

        IllegalStateException aviso = assertThrows(IllegalStateException.class,
                () -> servicio.deshacerLinea(documento));
        assertEquals("El documento no tiene líneas para deshacer", aviso.getMessage());
        assertEquals(0, documento.getLineas().length);
    }

    @Test
    public void deshaceSoloLaUltimaLineaAgregada() {
        Documento documento = new FacturaContado(cliente, impuesto);
        servicio.agregarLinea(documento, "P001", 2);
        servicio.agregarLinea(documento, "P002", 1);

        assertEquals("P002", servicio.deshacerLinea(documento).getProducto().getCodigo());
        LineaDetalle[] copia = documento.getLineas();
        assertEquals(1, copia.length);
        copia[0] = null;
        assertEquals(new BigDecimal("22.00"), documento.calcularTotal());
        assertEquals(5, servicio.consultarExistencias("P001"));
    }

    @Test
    public void rechazaLosCambiosEnUnDocumentoEmitido() {
        Documento documento = new FacturaContado(cliente, impuesto);
        servicio.agregarLinea(documento, "P001", 1);
        servicio.emitir(documento);

        assertThrows(IllegalStateException.class,
                () -> servicio.agregarLinea(documento, "P002", 1));
        assertThrows(IllegalStateException.class, () -> servicio.deshacerLinea(documento));
        assertEquals(1, documento.getLineas().length);
        assertEquals(new BigDecimal("11.00"), documento.calcularTotal());
        assertEquals(4, servicio.consultarExistencias("P001"));
        assertEquals(8, servicio.consultarExistencias("P002"));
    }

    @Test
    public void rechazoDeStockConservaTodoElInventarioYElConsecutivo() {
        Documento documento = new FacturaContado(cliente, impuesto);
        servicio.agregarLinea(documento, "P001", 2);
        servicio.agregarLinea(documento, "P002", 3);
        servicio.agregarLinea(documento, "P001", 4);

        assertThrows(IllegalArgumentException.class, () -> servicio.emitir(documento));
        assertEquals(5, servicio.consultarExistencias("P001"));
        assertEquals(8, servicio.consultarExistencias("P002"));
        assertEquals(3, documento.getLineas().length);
        assertEquals(EstadoDocumento.BORRADOR, documento.getEstado());
        assertTrue(emitidos.listar().isEmpty());

        servicio.deshacerLinea(documento);
        servicio.emitir(documento);
        assertEquals(1L, documento.getNumero());
        assertEquals(3, servicio.consultarExistencias("P001"));
        assertEquals(5, servicio.consultarExistencias("P002"));
        assertThrows(IllegalStateException.class, () -> servicio.emitir(documento));
        assertEquals(3, servicio.consultarExistencias("P001"));
    }

    @Test
    public void soloLasFacturasDescuentanExistencias() {
        Documento contado = new FacturaContado(cliente, impuesto);
        Documento credito = new FacturaCredito(cliente, impuesto);
        Documento nota = new NotaCredito(cliente, impuesto);
        Documento proforma = new Proforma(cliente, impuesto);
        for (Documento documento : List.of(contado, credito, nota, proforma)) {
            servicio.agregarLinea(documento, "P001", 1);
            servicio.emitir(documento);
        }
        assertEquals(3, servicio.consultarExistencias("P001"));
        assertEquals(1L, contado.getNumero());
        assertEquals(2L, credito.getNumero());
        assertEquals(3L, nota.getNumero());
        assertEquals(4L, proforma.getNumero());
    }

    @Test
    public void conservaElHistorialYEntregaLosDocumentosAlCerrar() throws InterruptedException {
        Documento primero = new FacturaContado(cliente, impuesto);
        Documento segundo = new Proforma(cliente, impuesto);
        for (Documento documento : List.of(primero, segundo)) {
            servicio.agregarLinea(documento, "P001", 1);
            servicio.emitir(documento);
        }
        servicio.close();

        EstadoDocumento[] historial = {EstadoDocumento.BORRADOR,
            EstadoDocumento.EMITIDO, EstadoDocumento.ENVIADO};
        assertArrayEquals(historial, primero.getHistorial());
        assertArrayEquals(historial, segundo.getHistorial());
        assertEquals(2, recibidos.listar().size());
        assertTrue(recibidos.listar().containsAll(List.of(primero, segundo)));
    }

    @Test
    public void ordenaPorMontoBuscaPorNumeroYLimitaElCierreAlDia() {
        LocalDate fecha = LocalDate.of(2026, 11, 24);
        Documento factura = registrarParaFecha(new FacturaContado(cliente, impuesto), fecha, 2);
        Documento nota = registrarParaFecha(new NotaCredito(cliente, impuesto), fecha, 1);
        Documento proforma = registrarParaFecha(new Proforma(cliente, impuesto), fecha, 1);
        Documento otroDia = registrarParaFecha(new FacturaCredito(cliente, impuesto), fecha.plusDays(1), 3);

        assertEquals(List.of(nota, proforma, factura), servicio.listarDelDia(fecha));
        assertEquals(new BigDecimal("22.00"), servicio.calcularCierre(fecha));
        assertSame(otroDia, servicio.buscarPorNumero(4));
        assertSame(nota, servicio.buscarPorNumero(2));
        assertNull(servicio.buscarPorNumero(999));
    }

    @Test(timeout = 10000)
    public void dosCajasNoPuedenVenderLaMismaExistencia() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try (ServicioFacturacion otraCaja = new ServicioFacturacion(inventario, emitidos, recibidos)) {
            Documento primero = new FacturaContado(cliente, impuesto);
            Documento segundo = new FacturaContado(cliente, impuesto);
            servicio.agregarLinea(primero, "P003", 7);
            otraCaja.agregarLinea(segundo, "P003", 7);
            FutureTask<Boolean> primeraVenta = new FutureTask<>(() -> intentarVenta(servicio, primero, inicio));
            FutureTask<Boolean> segundaVenta = new FutureTask<>(() -> intentarVenta(otraCaja, segundo, inicio));
            Thread cajaUno = new Thread(primeraVenta, "caja-1");
            Thread cajaDos = new Thread(segundaVenta, "caja-2");
            cajaUno.start();
            cajaDos.start();
            inicio.countDown();

            boolean resultadoUno = primeraVenta.get(5, TimeUnit.SECONDS);
            boolean resultadoDos = segundaVenta.get(5, TimeUnit.SECONDS);
            cajaUno.join();
            cajaDos.join();
            int saldo = servicio.consultarExistencias("P003");
            int aceptadas = emitidos.listar().size();
            assertNotEquals(resultadoUno, resultadoDos);
            assertEquals(3, saldo);
            assertEquals(1, aceptadas);
            System.out.println("Facturación con 10 unidades y dos solicitudes de 7 | aceptadas "
                    + aceptadas + " | rechazadas " + (2 - aceptadas) + " | saldo " + saldo);
        }
    }

    private Documento registrarParaFecha(Documento documento, LocalDate fecha, int cantidad) {
        servicio.agregarLinea(documento, "P001", cantidad);
        documento.registrarEmision(emitidos.siguienteNumero(), fecha);
        emitidos.guardar(documento);
        return documento;
    }

    private static boolean intentarVenta(ServicioFacturacion caja, Documento documento,
            CountDownLatch inicio) throws InterruptedException {
        inicio.await();
        try {
            caja.emitir(documento);
            return true;
        } catch (IllegalArgumentException excepcion) {
            assertTrue(excepcion.getMessage().startsWith("Existencias insuficientes"));
            return false;
        }
    }
}
