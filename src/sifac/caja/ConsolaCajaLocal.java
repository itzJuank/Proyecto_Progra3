package sifac.caja;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import sifac.central.ReceptorDocumentosLocal;
import sifac.comun.datos.RepositorioClientes;
import sifac.comun.datos.RepositorioDocumentosMemoria;
import sifac.comun.datos.RepositorioInventarioMemoria;
import sifac.comun.entidades.Cliente;
import sifac.comun.entidades.Documento;
import sifac.comun.entidades.FacturaContado;
import sifac.comun.entidades.FacturaCredito;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.NotaCredito;
import sifac.comun.entidades.Producto;
import sifac.comun.entidades.Proforma;
import sifac.comun.logica.ServicioClientes;
import sifac.comun.logica.ServicioFacturacion;
import sifac.padron.RepositorioClientesArchivo;

public class ConsolaCajaLocal {
    private final Scanner entrada;
    private final ServicioClientes clientes;
    private final ServicioFacturacion facturacion;

    private ConsolaCajaLocal(Scanner entrada, ServicioClientes clientes,
            ServicioFacturacion facturacion) {
        this.entrada = entrada;
        this.clientes = clientes;
        this.facturacion = facturacion;
    }

    public static void main(String[] argumentos) {
        try {
            RepositorioClientes repositorio = new RepositorioClientesArchivo(
                    "PADRON_SINTETICO.txt", "Distelec.txt");
            ServicioClientes servicio = new ServicioClientes(repositorio);
            if (argumentos.length > 0) {
                mostrarCliente(servicio.identificar(argumentos[0]));
                return;
            }
            String mensajeCierre = "Caja cerrada";
            try (Scanner entrada = new Scanner(System.in);
                    ServicioFacturacion facturacion = new ServicioFacturacion(
                            RepositorioInventarioMemoria.crearCatalogoDemostracion(),
                            new RepositorioDocumentosMemoria(), new ReceptorDocumentosLocal())) {
                try {
                    new ConsolaCajaLocal(entrada, servicio, facturacion).ejecutar();
                } catch (NoSuchElementException excepcion) {
                    mensajeCierre = "Caja cerrada al terminar la entrada";
                }
                if (facturacion.listarPendientes().length > 0) {
                    System.out.println("Enviando documentos pendientes antes de cerrar");
                }
            }
            System.out.println(mensajeCierre);
        } catch (IllegalArgumentException | IllegalStateException excepcion) {
            System.out.println("AVISO | " + excepcion.getMessage());
        } catch (IOException excepcion) {
            System.err.println("PADRON LOCAL NO DISPONIBLE | " + excepcion.getMessage());
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            System.out.println("AVISO | Se interrumpió la espera del cierre");
        }
    }

    private void ejecutar() {
        System.out.println("SIFAC | Caja local");
        BigDecimal porcentaje = leerPorcentaje();
        while (true) {
            String opcion = leer("""

                    1. Consultar cliente
                    2. Consultar inventario
                    3. Nuevo documento
                    4. Listar documentos de hoy por monto
                    5. Buscar documento e historial por número
                    6. Cierre de caja de hoy
                    7. Consultar documentos pendientes
                    0. Salir
                    Opción > """);
            try {
                switch (opcion) {
                    case "1" -> mostrarCliente(clientes.identificar(leer("Cédula > ")));
                    case "2" -> consultarProductos();
                    case "3" -> nuevoDocumento(porcentaje);
                    case "4" -> listarDocumentos();
                    case "5" -> buscarDocumento();
                    case "6" -> System.out.println("TOTAL DOCUMENTOS EMITIDOS | "
                            + facturacion.calcularCierre(LocalDate.now()).toPlainString());
                    case "7" -> listarPendientes();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("AVISO | Opción inválida");
                }
            } catch (NumberFormatException excepcion) {
                System.out.println("AVISO | Ingrese un número entero");
            } catch (IllegalArgumentException | IllegalStateException excepcion) {
                System.out.println("AVISO | " + excepcion.getMessage());
            }
        }
    }

    private void nuevoDocumento(BigDecimal porcentaje) {
        Cliente cliente = clientes.identificar(leer("Cédula del cliente > "));
        String opcion = leer("""
                1. Factura de contado
                2. Factura de crédito
                3. Nota de crédito
                4. Proforma
                0. Volver
                Tipo de documento > """);
        if (opcion.equals("0")) {
            return;
        }
        Documento documento = switch (opcion) {
            case "1" -> new FacturaContado(cliente, porcentaje);
            case "2" -> new FacturaCredito(cliente, porcentaje);
            case "3" -> new NotaCredito(cliente, porcentaje);
            case "4" -> new Proforma(cliente, porcentaje);
            default -> throw new IllegalArgumentException("Opción inválida");
        };
        editarDocumento(documento);
    }

    private void editarDocumento(Documento documento) {
        System.out.println(documento.getTipo());
        while (true) {
            String opcion = leer("""

                    1. Agregar línea
                    2. Deshacer última línea
                    3. Ver borrador
                    4. Emitir
                    0. Descartar borrador
                    Opción > """);
            try {
                switch (opcion) {
                    case "1" -> {
                        String codigo = leer("Código de producto > ");
                        int cantidad = Integer.parseInt(leer("Cantidad > "));
                        facturacion.agregarLinea(documento, codigo, cantidad);
                        System.out.println("TOTAL EN CURSO | " + documento.calcularTotal().toPlainString());
                    }
                    case "2" -> {
                        LineaDetalle linea = facturacion.deshacerLinea(documento);
                        System.out.println("LÍNEA RETIRADA | " + linea.getProducto().getCodigo());
                    }
                    case "3" -> mostrarDetalle(documento);
                    case "4" -> {
                        facturacion.emitir(documento);
                        mostrarResumen(documento);
                        return;
                    }
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("AVISO | Opción inválida");
                }
            } catch (NumberFormatException excepcion) {
                System.out.println("AVISO | Ingrese una cantidad entera");
            } catch (IllegalArgumentException | IllegalStateException excepcion) {
                System.out.println("AVISO | " + excepcion.getMessage());
            }
        }
    }

    private void consultarProductos() {
        String codigo = leer("Código de producto (Enter para listar) > ");
        if (codigo.isEmpty()) {
            for (Producto producto : facturacion.listarProductos()) {
                mostrarProducto(producto);
            }
        } else {
            mostrarProducto(facturacion.consultarProducto(codigo));
        }
    }

    private void mostrarProducto(Producto producto) {
        System.out.println(producto.getCodigo() + " | " + producto.getNombre()
                + " | Precio " + producto.getPrecioUnitario().toPlainString()
                + " | Existencias " + facturacion.consultarExistencias(producto.getCodigo()));
    }

    private void listarDocumentos() {
        List<Documento> documentos = facturacion.listarDelDia(LocalDate.now());
        if (documentos.isEmpty()) {
            System.out.println("AVISO | Sin documentos emitidos hoy");
        }
        for (Documento documento : documentos) {
            mostrarResumen(documento);
        }
    }

    private void listarPendientes() {
        Documento[] documentos = facturacion.listarPendientes();
        if (documentos.length == 0) {
            System.out.println("AVISO | No hay documentos pendientes de envío");
            return;
        }
        System.out.println("DOCUMENTOS PENDIENTES | " + documentos.length);
        for (Documento documento : documentos) {
            mostrarResumen(documento);
        }
    }

    private void buscarDocumento() {
        long numero = Long.parseLong(leer("Número de documento > "));
        Documento documento = facturacion.buscarPorNumero(numero);
        if (documento == null) {
            throw new IllegalArgumentException("Documento inexistente | " + numero);
        }
        mostrarDetalle(documento);
    }

    private void mostrarDetalle(Documento documento) {
        mostrarResumen(documento);
        mostrarCliente(documento.getCliente());
        for (LineaDetalle linea : documento.getLineas()) {
            System.out.println(linea.getProducto().getCodigo() + " | "
                    + linea.getProducto().getNombre() + " | Cantidad " + linea.getCantidad()
                    + " | Subtotal " + linea.calcularSubtotal().toPlainString());
        }
        System.out.println("HISTORIAL | " + Arrays.toString(documento.getHistorial()));
    }

    private static void mostrarResumen(Documento documento) {
        String numero = documento.getNumero() == 0 ? "BORRADOR" : "N.º " + documento.getNumero();
        System.out.println(numero + " | " + documento.getTipo() + " | TOTAL "
                + documento.calcularTotal().toPlainString());
    }

    static void mostrarCliente(Cliente cliente) {
        System.out.println("CLIENTE | " + cliente.getCedula() + " | " + cliente.getNombre());
    }

    private BigDecimal leerPorcentaje() {
        while (true) {
            String texto = leer("Porcentaje de impuesto de la sesión (ejemplo 10 o 0) > ");
            if (texto.matches("[0-9]+([.,][0-9]+)?")) {
                return new BigDecimal(texto.replace(',', '.'));
            }
            System.out.println("AVISO | Ingrese un porcentaje mayor o igual a cero");
        }
    }

    private String leer(String mensaje) {
        System.out.println(mensaje);
        return entrada.nextLine().strip();
    }
}
