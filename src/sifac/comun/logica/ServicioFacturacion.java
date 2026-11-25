package sifac.comun.logica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import sifac.comun.datos.RepositorioDocumentos;
import sifac.comun.datos.RepositorioInventario;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;
import sifac.comun.estructuras.Cola;

public class ServicioFacturacion implements AutoCloseable {
    private final RepositorioInventario inventario;
    private final RepositorioDocumentos emitidos;
    private final Cola<Documento> pendientes = new Cola<>();
    private final ProcesadorEnvios procesadorEnvios;
    private final Thread hiloEnvios;

    public ServicioFacturacion(RepositorioInventario inventario,
            RepositorioDocumentos emitidos, RepositorioDocumentos destino) {
        this.inventario = Objects.requireNonNull(inventario, "Indique el inventario");
        this.emitidos = Objects.requireNonNull(emitidos, "Indique el repositorio de documentos");
        procesadorEnvios = new ProcesadorEnvios(pendientes,
                Objects.requireNonNull(destino, "Indique el destino"));
        hiloEnvios = new Thread(procesadorEnvios, "envio-documentos");
        hiloEnvios.start();
    }

    public Producto consultarProducto(String codigo) {
        Producto producto = inventario.buscarPorCodigo(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("Producto inexistente | " + codigo);
        }
        return producto;
    }

    public List<Producto> listarProductos() {
        return inventario.listarProductos();
    }

    public int consultarExistencias(String codigo) {
        return inventario.consultarExistencias(codigo);
    }

    public void agregarLinea(Documento documento, String codigo, int cantidad) {
        Producto producto = consultarProducto(codigo);
        documento.agregarLinea(new LineaDetalle(producto, cantidad));
    }

    public LineaDetalle deshacerLinea(Documento documento) {
        return documento.deshacerLinea();
    }

    public synchronized void emitir(Documento documento) {
        if (pendientes.estaCerrada()) {
            throw new IllegalStateException("La caja está cerrada");
        }
        procesadorEnvios.verificarFuncionamiento();
        synchronized (documento) {
            documento.validarEmision();
            if (documento.descuentaInventario()) {
                inventario.descontar(documento.getLineas());
            }
            documento.registrarEmision(emitidos.siguienteNumero(), LocalDate.now());
            emitidos.guardar(documento);
            pendientes.encolar(documento);
        }
    }

    public Documento[] listarPendientes() {
        return pendientes.copiar(new Documento[0]);
    }

    public List<Documento> listarDelDia(LocalDate fecha) {
        List<Documento> documentos = new ArrayList<>();
        for (Documento documento : emitidos.listar()) {
            if (fecha.equals(documento.getFechaEmision())) {
                documentos.add(documento);
            }
        }
        documentos.sort(Comparator.comparing(Documento::calcularTotal)
                .thenComparingLong(Documento::getNumero));
        return documentos;
    }

    public Documento buscarPorNumero(long numero) {
        List<Documento> documentos = emitidos.listar();
        documentos.sort(Comparator.comparingLong(Documento::getNumero));
        List<Long> numeros = new ArrayList<>();
        for (Documento documento : documentos) {
            numeros.add(documento.getNumero());
        }
        int indice = Collections.binarySearch(numeros, numero);
        return indice < 0 ? null : documentos.get(indice);
    }

    public BigDecimal calcularCierre(LocalDate fecha) {
        BigDecimal total = BigDecimal.ZERO;
        for (Documento documento : listarDelDia(fecha)) {
            total = total.add(documento.calcularTotal());
        }
        return total;
    }

    @Override
    public void close() throws InterruptedException {
        synchronized (this) {
            pendientes.cerrar();
        }
        hiloEnvios.join();
        procesadorEnvios.verificarFuncionamiento();
    }
}
