package sifac.comun.logica;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.entidades.Cliente;
// Servicio minimo para ejecutar el ejemplo sin reemplazar ServicioFacturacion.
public class ServicioClientes {
    private final RepositorioClientes repositorio;
    public ServicioClientes(RepositorioClientes repositorio) {
        this.repositorio = repositorio;
    }
    public Cliente identificar(String cedula) {
        Cliente cliente = repositorio.buscarPorCedula(cedula);
        if (cliente == null) throw new IllegalArgumentException("Cliente no registrado");
        return cliente;
    }
}
