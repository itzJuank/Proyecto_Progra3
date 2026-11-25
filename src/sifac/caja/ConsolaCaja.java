package sifac.caja;

import sifac.comun.datos.RepositorioClientes;
import sifac.comun.datos.RepositorioClientesPadron;
import sifac.comun.datos.ServicioPadronException;
import sifac.comun.logica.ServicioClientes;

public class ConsolaCaja {
    public static void main(String[] argumentos) {
        try {
            String servidor = argumentos.length > 0 ? argumentos[0] : "localhost";
            int puerto = argumentos.length > 1 ? Integer.parseInt(argumentos[1]) : 5051;
            String cedula = argumentos.length > 2 ? argumentos[2] : "104560789";
            RepositorioClientes repositorio = new RepositorioClientesPadron(servidor, puerto);
            ServicioClientes servicio = new ServicioClientes(repositorio);
            ConsolaCajaLocal.mostrarCliente(servicio.identificar(cedula));
        } catch (ServicioPadronException excepcion) {
            System.out.println("PADRON NO DISPONIBLE | Reintente mas tarde");
        } catch (NumberFormatException excepcion) {
            System.out.println("AVISO | Ingrese un puerto entero");
        } catch (IllegalArgumentException excepcion) {
            System.out.println("AVISO | " + excepcion.getMessage());
        }
    }
}
