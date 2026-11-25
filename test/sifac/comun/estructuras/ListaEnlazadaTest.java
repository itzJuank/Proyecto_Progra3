package sifac.comun.estructuras;

import org.junit.Test;
import static org.junit.Assert.*;

public class ListaEnlazadaTest {
    @Test
    public void permiteConsultarYEliminarEnUnaListaVacia() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();

        assertFalse(lista.eliminar(1));
        assertEquals(0, lista.cantidad());
        lista.copiarEn(new Integer[0]);
    }

    @Test
    public void permiteRetirarNodosYPreservarElOrdenDeInsercion() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        lista.agregar(1);
        lista.agregar(2);
        lista.agregar(3);

        assertTrue(lista.eliminar(2));
        assertTrue(lista.eliminar(3));
        assertFalse(lista.eliminar(3));
        lista.agregar(4);
        Integer[] valores = new Integer[lista.cantidad()];
        lista.copiarEn(valores);
        assertArrayEquals(new Integer[]{1, 4}, valores);

        assertTrue(lista.eliminar(1));
        assertTrue(lista.eliminar(4));
        assertEquals(0, lista.cantidad());
        lista.agregar(5);
        Integer[] valoresAlReinsertar = new Integer[lista.cantidad()];
        lista.copiarEn(valoresAlReinsertar);
        assertArrayEquals(new Integer[]{5}, valoresAlReinsertar);
    }
}
