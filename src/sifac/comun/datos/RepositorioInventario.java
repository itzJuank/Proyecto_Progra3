package sifac.comun.datos;

import java.util.List;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;

public interface RepositorioInventario {
    Producto buscarPorCodigo(String codigo);

    // Devuelve los productos del catálogo en orden de código.
    List<Producto> listarProductos();

    int consultarExistencias(String codigo);

    void descontar(LineaDetalle[] lineas);
}
