package sifac.comun.datos;

import java.util.List;
import sifac.comun.entidades.Documento;

public interface RepositorioDocumentos {
    long siguienteNumero();

    void guardar(Documento documento);

    List<Documento> listar();
}
