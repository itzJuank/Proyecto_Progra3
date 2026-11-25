package sifac.padron;

import java.io.IOException;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.entidades.Cliente;

public class RepositorioClientesArchivo implements RepositorioClientes {

    private final RepositorioPadron padron;

    public RepositorioClientesArchivo(String rutaPadron, String rutaDistelec)
            throws IOException {
        padron = new RepositorioPadron();
        padron.cargar(rutaPadron, rutaDistelec);
    }

    @Override
    public Cliente buscarPorCedula(String cedula) {
        if (cedula == null || !cedula.matches("[0-9]{9}")) {
            throw new IllegalArgumentException("Use nueve digitos");
        }
        Elector elector = padron.buscarBinaria(cedula);
        if (elector == null) {
            return null;
        }
        return new Cliente(elector.getCedula(), elector.getNombreCompleto());
    }
}
