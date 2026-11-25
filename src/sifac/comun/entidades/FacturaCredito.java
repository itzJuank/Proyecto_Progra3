package sifac.comun.entidades;

import java.math.BigDecimal;

public class FacturaCredito extends Documento {
    public FacturaCredito(Cliente cliente, BigDecimal porcentajeImpuesto) {
        super(cliente, porcentajeImpuesto);
    }

    @Override
    public String getTipo() {
        return "Factura de crédito";
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
