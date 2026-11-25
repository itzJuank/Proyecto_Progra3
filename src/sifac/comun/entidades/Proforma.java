package sifac.comun.entidades;

import java.math.BigDecimal;

public class Proforma extends Documento {
    public Proforma(Cliente cliente, BigDecimal porcentajeImpuesto) {
        super(cliente, porcentajeImpuesto);
    }

    @Override
    public String getTipo() {
        return "Proforma";
    }

    @Override
    public BigDecimal calcularTotal() {
        return calcularTotalConImpuesto();
    }

    @Override
    public boolean descuentaInventario() {
        return false;
    }
}
