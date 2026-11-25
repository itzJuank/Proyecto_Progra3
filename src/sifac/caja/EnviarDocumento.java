package sifac.caja;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class EnviarDocumento {
    public static void main(String[] args) {
        String id = args.length > 0 ? args[0] : "F-001";
        String monto = args.length > 1 ? args[1] : "1000.00";
        String host = args.length > 2 ? args[2] : "localhost";
        int puerto = args.length > 3 ? Integer.parseInt(args[3]) : 5052;
        if (!id.matches("[A-Za-z0-9-]+") || !monto.matches("[0-9]+[.][0-9]{2}")) {
            System.out.println("DATOS INVALIDOS"); return;
        }
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, puerto), 2500);
            s.setSoTimeout(4500);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                         s.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                         s.getOutputStream(), StandardCharsets.UTF_8))) {
                out.write("REGISTRAR|" + id + "|" + monto);out.newLine();out.flush();
                String respuesta = in.readLine();
                if (!("ACK|" + id).equals(respuesta)) {
                    throw new IOException("Sin confirmacion valida");
                }
                System.out.println("CONFIRMADO | " + id);
            }
        } catch (IOException ex) {
            System.out.println("PENDIENTE | " + id + " | No eliminar de la cola");
        }
    }
}
