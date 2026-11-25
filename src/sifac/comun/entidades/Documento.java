package sifac.comun.entidades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import sifac.comun.estructuras.ListaEnlazada;
import sifac.comun.estructuras.Pila;

public abstract class Documento {
    private final Cliente cliente;
    private final BigDecimal porcentajeImpuesto;
    private final Pila<LineaDetalle> lineas = new Pila<>();
    private final ListaEnlazada<EstadoDocumento> historial = new ListaEnlazada<>();
    private EstadoDocumento estado;
    private long numero;
    private LocalDate fechaEmision;

    protected Documento(Cliente cliente, BigDecimal porcentajeImpuesto) {
        if (cliente == null) {
            throw new IllegalArgumentException("Indique el cliente del documento");
        }
        if (porcentajeImpuesto == null || porcentajeImpuesto.signum() < 0) {
            throw new IllegalArgumentException("El porcentaje de impuesto debe ser mayor o igual a cero");
        }
        this.cliente = cliente;
        this.porcentajeImpuesto = porcentajeImpuesto;
        cambiarEstado(EstadoDocumento.BORRADOR);
    }

    public Cliente getCliente() {
        return cliente;
    }

    public synchronized long getNumero() {
        return numero;
    }

    public synchronized LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public synchronized EstadoDocumento getEstado() {
        return estado;
    }

    public synchronized EstadoDocumento[] getHistorial() {
        EstadoDocumento[] copia = new EstadoDocumento[historial.cantidad()];
        historial.copiarEn(copia);
        return copia;
    }

    public synchronized LineaDetalle[] getLineas() {
        LineaDetalle[] copia = new LineaDetalle[lineas.cantidad()];
        lineas.copiarEnOrdenDeInsercion(copia);
        return copia;
    }

    public abstract String getTipo();

    public abstract BigDecimal calcularTotal();

    public abstract boolean descuentaInventario();

    public synchronized void agregarLinea(LineaDetalle linea) {
        exigirBorrador();
        if (linea == null) {
            throw new IllegalArgumentException("Indique la línea de detalle");
        }
        lineas.apilar(linea);
    }

    public synchronized LineaDetalle deshacerLinea() {
        exigirBorrador();
        if (lineas.estaVacia()) {
            throw new IllegalStateException("El documento no tiene líneas para deshacer");
        }
        return lineas.desapilar();
    }

    public synchronized void validarEmision() {
        exigirBorrador();
        if (lineas.estaVacia()) {
            throw new IllegalStateException("Agregue al menos una línea antes de emitir");
        }
    }

    public synchronized void registrarEmision(long numero, LocalDate fechaEmision) {
        validarEmision();
        if (numero <= 0 || fechaEmision == null) {
            throw new IllegalArgumentException("Indique el consecutivo y la fecha de emisión");
        }
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        cambiarEstado(EstadoDocumento.EMITIDO);
    }

    public synchronized void marcarEnviado() {
        if (estado == EstadoDocumento.BORRADOR) {
            throw new IllegalStateException("Emita el documento antes de enviarlo");
        }
        if (estado == EstadoDocumento.EMITIDO) {
            cambiarEstado(EstadoDocumento.ENVIADO);
        }
    }

    protected synchronized BigDecimal calcularTotalConImpuesto() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaDetalle linea : getLineas()) {
            subtotal = subtotal.add(linea.calcularSubtotal());
        }
        BigDecimal impuesto = subtotal.multiply(porcentajeImpuesto.movePointLeft(2));
        return subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);
    }

    private void exigirBorrador() {
        if (estado != EstadoDocumento.BORRADOR) {
            throw new IllegalStateException("El documento ya fue emitido");
        }
    }

    private void cambiarEstado(EstadoDocumento nuevoEstado) {
        estado = nuevoEstado;
        historial.agregar(nuevoEstado);
    }
}
