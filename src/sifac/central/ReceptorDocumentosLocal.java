package sifac.central;

import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.entidades.Documento;

public class ReceptorDocumentosLocal extends RepositorioDocumentosMemoria {
    private static final long DEMORA_MILISEGUNDOS = 2000;

    @Override
    public void guardar(Documento documento) {
        try {
            Thread.sleep(DEMORA_MILISEGUNDOS);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La recepción local fue interrumpida", excepcion);
        }
        super.guardar(documento);
    }
}
