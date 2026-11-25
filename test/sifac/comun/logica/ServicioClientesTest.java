package sifac.comun.logica;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.entidades.Cliente;
import sifac.padron.RepositorioClientesArchivo;

public class ServicioClientesTest {
    @Test
    public void identificaAlClienteMedianteLaInterfaz() {
        Cliente esperado = new Cliente("112340567", "Cliente de prueba");
        RepositorioClientes repositorio = cedula -> {
            assertEquals(esperado.getCedula(), cedula);
            return esperado;
        };

        assertSame(esperado, new ServicioClientes(repositorio).identificar("112340567"));
    }

    @Test
    public void informaCuandoElClienteNoExiste() {
        ServicioClientes servicio = new ServicioClientes(cedula -> null);

        IllegalArgumentException aviso = assertThrows(IllegalArgumentException.class,
                () -> servicio.identificar("999999999"));
        assertEquals("Cliente no registrado", aviso.getMessage());
    }

    @Test
    public void consultaElPadronSinteticoIncluidoEnElProyecto() throws IOException {
        ServicioClientes servicio = servicioConPadronLocal();
        Cliente cliente = servicio.identificar("112340567");

        assertEquals("112340567", cliente.getCedula());
        assertEquals("MARIA JOSE JIMENEZ ROJAS", cliente.getNombre());
        assertThrows(IllegalArgumentException.class, () -> servicio.identificar("999999999"));
    }

    @Test
    public void rechazaCedulasQueNoTienenNueveDigitos() throws IOException {
        ServicioClientes servicio = servicioConPadronLocal();

        assertThrows(IllegalArgumentException.class, () -> servicio.identificar("123"));
        assertThrows(IllegalArgumentException.class, () -> servicio.identificar("11234A567"));
        assertThrows(IllegalArgumentException.class, () -> servicio.identificar(null));
    }

    private static ServicioClientes servicioConPadronLocal() throws IOException {
        return new ServicioClientes(new RepositorioClientesArchivo(
                "PADRON_SINTETICO.txt", "Distelec.txt"));
    }
}
