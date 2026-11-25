package sifac.comun.entidades;

import java.math.BigDecimal;

public class Producto {
    private final String codigo;
    private final String nombre;
    private final BigDecimal precioUnitario;

    public Producto(String codigo, String nombre, BigDecimal precioUnitario) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Indique el código y el nombre del producto");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}
