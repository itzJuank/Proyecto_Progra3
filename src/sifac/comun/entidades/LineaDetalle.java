package sifac.comun.entidades;

import java.math.BigDecimal;

public class LineaDetalle {
    private final Producto producto;
    private final int cantidad;

    public LineaDetalle(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("Indique un producto");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal calcularSubtotal() {
        return producto.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidad));
    }
}
