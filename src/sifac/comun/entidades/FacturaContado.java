package sifac.comun.entidades;

import java.math.BigDecimal;

public class FacturaContado extends Documento {
    public FacturaContado(Cliente cliente, BigDecimal porcentajeImpuesto) {
        super(cliente, porcentajeImpuesto);
    }

    @Override
    public String getTipo() {
        return "Factura de contado";
    }

    @Override
    public BigDecimal calcularTotal() {
        return calcularTotalConImpuesto();
    }

    @Override
    public boolean descuentaInventario() {
        return true;
    }
}
