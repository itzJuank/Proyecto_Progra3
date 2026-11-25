package sifac.comun.datos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sifac.comun.entidades.Documento;

public class RepositorioDocumentosMemoria implements RepositorioDocumentos {
    private final Map<Long, Documento> documentos = new HashMap<>();
    private long ultimoNumero;

    @Override
    public synchronized long siguienteNumero() {
        ultimoNumero++;
        return ultimoNumero;
    }

    @Override
    public void guardar(Documento documento) {
        long numero = documento.getNumero();
        if (numero <= 0) {
            throw new IllegalArgumentException("Emita el documento antes de guardarlo");
        }
        synchronized (this) {
            Documento anterior = documentos.putIfAbsent(numero, documento);
            if (anterior != null && anterior != documento) {
                throw new IllegalStateException("Consecutivo duplicado | " + numero);
            }
            ultimoNumero = Math.max(ultimoNumero, numero);
        }
    }

    @Override
    public synchronized List<Documento> listar() {
        return new ArrayList<>(documentos.values());
    }
}
