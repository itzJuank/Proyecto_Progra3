package sifac.comun.datos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;

public class RepositorioInventarioMemoria implements RepositorioInventario {
    private final Map<String, Producto> productos = new TreeMap<>();
    private final Map<String, Integer> existencias = new HashMap<>();

    public static RepositorioInventarioMemoria crearCatalogoDemostracion() {
        RepositorioInventarioMemoria inventario = new RepositorioInventarioMemoria();
        inventario.registrar(new Producto("P001", "Martillo", new BigDecimal("3500.00")), 5);
        inventario.registrar(new Producto("P002", "Paquete de tornillos", new BigDecimal("1200.00")), 20);
        inventario.registrar(new Producto("P003", "Brocha", new BigDecimal("800.00")), 10);
        return inventario;
    }

    public synchronized void registrar(Producto producto, int cantidad) {
        if (producto == null || cantidad < 0) {
            throw new IllegalArgumentException("Indique un producto y existencias no negativas");
        }
        String codigo = producto.getCodigo();
        if (productos.containsKey(codigo)) {
            throw new IllegalArgumentException("Código de producto repetido | " + codigo);
        }
        productos.put(codigo, producto);
        existencias.put(codigo, cantidad);
    }

    @Override
    public synchronized Producto buscarPorCodigo(String codigo) {
        return codigo == null ? null : productos.get(codigo);
    }

    @Override
    public synchronized List<Producto> listarProductos() {
        return new ArrayList<>(productos.values());
    }

    @Override
    public synchronized int consultarExistencias(String codigo) {
        Integer cantidad = existencias.get(codigo);
        if (cantidad == null) {
            throw new IllegalArgumentException("Producto inexistente | " + codigo);
        }
        return cantidad;
    }

    @Override
    public synchronized void descontar(LineaDetalle[] lineas) {
        Map<String, Integer> saldos = new HashMap<>();
        // Cada línea usa el saldo provisional del producto. Se aplican juntos al final.
        for (LineaDetalle linea : lineas) {
            String codigo = linea.getProducto().getCodigo();
            Integer disponible = saldos.getOrDefault(codigo, existencias.get(codigo));
            if (disponible == null) {
                throw new IllegalArgumentException("Producto inexistente | " + codigo);
            }
            if (disponible < linea.getCantidad()) {
                throw new IllegalArgumentException("Existencias insuficientes | " + codigo);
            }
            saldos.put(codigo, disponible - linea.getCantidad());
        }
        existencias.putAll(saldos);
    }
}
