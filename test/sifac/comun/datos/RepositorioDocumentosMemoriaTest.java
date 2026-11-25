package sifac.comun.datos;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.Proforma;

public class RepositorioDocumentosMemoriaTest {
    private final RepositorioDocumentos repositorio = new RepositorioDocumentosMemoria();

    @Test
    public void permiteGuardarDeNuevoElMismoDocumento() {
        long numero = repositorio.siguienteNumero();
        Documento documento = documentoEmitido(numero);
        repositorio.guardar(documento);
        repositorio.guardar(documento);

        assertEquals(1, repositorio.listar().size());
        assertSame(documento, repositorio.listar().get(0));
        assertEquals(numero + 1, repositorio.siguienteNumero());
    }

    @Test
    public void conservaElOriginalAnteUnConsecutivoDuplicado() {
        long numero = repositorio.siguienteNumero();
        Documento original = documentoEmitido(numero);
        Documento otro = documentoEmitido(numero);
        repositorio.guardar(original);

        assertThrows(IllegalStateException.class, () -> repositorio.guardar(otro));
        assertEquals(1, repositorio.listar().size());
        assertSame(original, repositorio.listar().get(0));
    }

    private static Documento documentoEmitido(long numero) {
        Cliente cliente = new Cliente("112340567", "Cliente de prueba");
        Producto producto = new Producto("P001", "Martillo", BigDecimal.TEN);
        Documento documento = new Proforma(cliente, BigDecimal.ZERO);
        documento.agregarLinea(new LineaDetalle(producto, 1));
        documento.registrarEmision(numero, LocalDate.of(2026, 11, 24));
        return documento;
    }
}
