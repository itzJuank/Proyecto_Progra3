package sifac.comun.datos;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import sifac.comun.entidades.Cliente;
public class RepositorioClientesPadron implements RepositorioClientes {
    private final String host;
    private final int puerto;
    private final int conexionMs;
    private final int lecturaMs;
    public RepositorioClientesPadron(String host, int puerto) {
        this(host, puerto, 2500, 4500);
    }
    public RepositorioClientesPadron(String host, int puerto,
                                     int conexionMs, int lecturaMs) {
        this.host = host; this.puerto = puerto;
        this.conexionMs = conexionMs; this.lecturaMs = lecturaMs;
    }
    @Override public Cliente buscarPorCedula(String cedula) {
        if (cedula == null || !cedula.matches("[0-9]{9}")) {
            throw new IllegalArgumentException("Use nueve digitos");
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), conexionMs);
            socket.setSoTimeout(lecturaMs);
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(
                         socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(
                         socket.getOutputStream(), StandardCharsets.UTF_8))) {
                salida.write(cedula);
                salida.newLine();
                salida.flush();
                String respuesta = entrada.readLine();
                return ProtocoloPadron.interpretar(respuesta, cedula);
            }
        } catch (IOException ex) {
            throw new ServicioPadronException("No se pudo consultar el padron", ex);
        }
    }
}
