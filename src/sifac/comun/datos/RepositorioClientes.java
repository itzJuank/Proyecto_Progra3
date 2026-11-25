package sifac.comun.datos;
import sifac.comun.entidades.Cliente;
public interface RepositorioClientes {
    Cliente buscarPorCedula(String cedula);
}
