package sifac.comun.estructuras;

import org.junit.Test;
import static org.junit.Assert.*;

public class PilaTest {
    @Test
    public void permiteConsultarYDesapilarEnUnaPilaVacia() {
        Pila<String> pila = new Pila<>();

        assertNull(pila.desapilar());
        assertTrue(pila.estaVacia());
        assertEquals(0, pila.cantidad());
        pila.copiarEnOrdenDeInsercion(new String[0]);
    }

    @Test
    public void copiaEnOrdenDeInsercionYRetiraLaUltimaEntrada() {
        Pila<String> pila = new Pila<>();
        pila.apilar("primera");
        pila.apilar("segunda");
        pila.apilar("tercera");

        String[] valores = new String[pila.cantidad()];
        pila.copiarEnOrdenDeInsercion(valores);
        assertArrayEquals(new String[]{"primera", "segunda", "tercera"}, valores);
        assertEquals("tercera", pila.desapilar());
        assertEquals(2, pila.cantidad());
        assertEquals("segunda", pila.desapilar());
        assertEquals("primera", pila.desapilar());
        assertTrue(pila.estaVacia());
        assertEquals(0, pila.cantidad());
        assertNull(pila.desapilar());
    }
}
