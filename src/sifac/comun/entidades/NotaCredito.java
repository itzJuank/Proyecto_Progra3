package sifac.comun.entidades;

import java.math.BigDecimal;

public class NotaCredito extends Documento {
    public NotaCredito(Cliente cliente, BigDecimal porcentajeImpuesto) {
        super(cliente, porcentajeImpuesto);
    }

    @Override
    public String getTipo() {
        return "Nota de crédito";
    }

    @Override
    public BigDecimal calcularTotal() {
        return calcularTotalConImpuesto().negate();
    }

    @Override
    public boolean descuentaInventario() {
        return false;
    }
}
