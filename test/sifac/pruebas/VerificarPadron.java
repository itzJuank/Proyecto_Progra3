package sifac.pruebas;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import sifac.comun.datos.*;
import sifac.comun.entidades.Cliente;
public class VerificarPadron {
    private static int cantidad;
    private static void exigir(boolean condicion, String nombre) {
        if (!condicion) throw new AssertionError(nombre);
        System.out.println("OK | " + nombre); cantidad++;
    }
    private static void caso(String respuesta, int espera, boolean error) throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            Thread hilo = new Thread(() -> {
                try (Socket s = servidor.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(
                             s.getInputStream(), StandardCharsets.UTF_8));
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                             s.getOutputStream(), StandardCharsets.UTF_8))) {
                    in.readLine();Thread.sleep(espera);
                    if (respuesta != null) { out.write(respuesta);out.newLine();out.flush(); }
                } catch (Exception ignored) { }
            });
            hilo.start();
            try {
                Cliente c = new RepositorioClientesPadron("localhost", servidor.getLocalPort(),
                        1000, 250).buscarPorCedula("104560789");
                exigir(!error && ((respuesta.equals("NOTFOUND") && c == null)
                        || (c != null && c.getNombre().equals("ANA LUCÍA"))), "respuesta " + respuesta);
            } catch (ServicioPadronException ex) { exigir(error, "fallo controlado " + respuesta); }
            finally { hilo.join(2000); }
        }
    }
    public static void main(String[] args) throws Exception {
        caso("OK|104560789|ANA LUCÍA|SAN JOSE|junta 1", 0, false);
        caso("NOTFOUND", 0, false);
        caso("OK|104560789", 0, true);
        caso("OK|112340567|OTRO|SAN JOSE|junta 1", 0, true);
        caso(null, 0, true);
        caso("NOTFOUND", 700, true);
        int cerrado;
        try (ServerSocket s = new ServerSocket(0)) { cerrado = s.getLocalPort(); }
        try {
            new RepositorioClientesPadron("localhost", cerrado).buscarPorCedula("104560789");
            throw new AssertionError("conexion rechazada");
        } catch (ServicioPadronException ex) { exigir(true, "conexion rechazada"); }
        try {
            new RepositorioClientesPadron("localhost", cerrado).buscarPorCedula("1\nSALIR");
            throw new AssertionError("validacion");
        } catch (IllegalArgumentException ex) { exigir(true, "entrada invalida"); }
        System.out.println("RESULTADO | " + cantidad + " verificaciones correctas");
    }
}
