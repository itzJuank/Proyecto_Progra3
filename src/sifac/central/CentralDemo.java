package sifac.central;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
public class CentralDemo {
    private final Map<String, BigDecimal> registrados = new HashMap<>();
    private synchronized String registrar(String linea) {
        if (linea == null) return "ERROR|SOLICITUD";
        String[] p = linea.split("\\|", -1);
        if (p.length != 3 || !p[0].equals("REGISTRAR")
                || !p[1].matches("[A-Za-z0-9-]+")
                || !p[2].matches("[0-9]+[.][0-9]{2}")) return "ERROR|FORMATO";
        BigDecimal monto = new BigDecimal(p[2]);
        if (monto.signum() <= 0) return "ERROR|MONTO";
        BigDecimal previo = registrados.get(p[1]);
        if (previo != null && previo.compareTo(monto) != 0) return "ERROR|CONFLICTO";
        registrados.putIfAbsent(p[1], monto);
        return "ACK|" + p[1];
    }
    private void atender(Socket s) {
        try (s;
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     s.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                     s.getOutputStream(), StandardCharsets.UTF_8))) {
            s.setSoTimeout(5000);
            String respuesta = registrar(in.readLine());
            out.write(respuesta);out.newLine();out.flush();
            System.out.println(respuesta);
        } catch (IOException ex) { System.out.println("CLIENTE DESCONECTADO"); }
    }
    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5052;
        CentralDemo central = new CentralDemo();
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("CENTRAL DEMO | puerto " + puerto);
            while (true) {
                Socket s = servidor.accept();
                new Thread(() -> central.atender(s), "central-cliente").start();
            }
        }
    }
}
