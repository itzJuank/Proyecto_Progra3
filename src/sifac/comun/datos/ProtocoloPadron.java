package sifac.comun.datos;
import java.io.IOException;
import sifac.comun.entidades.Cliente;
public final class ProtocoloPadron {
    private ProtocoloPadron() {}
    public static Cliente interpretar(String respuesta, String solicitada)
            throws IOException {
        if (respuesta == null) throw new IOException("Conexion cerrada sin respuesta");
        if (respuesta.equals("NOTFOUND")) return null;
        String[] campos = respuesta.split("\\|", -1);
        if (campos.length != 5 || !campos[0].equals("OK")
                || !campos[1].equals(solicitada) || campos[2].isBlank()) {
            throw new IOException("Respuesta de padron invalida");
        }
        return new Cliente(campos[1], campos[2]);
    }
}
