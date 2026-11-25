package sifac.comun.estructuras;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ColaTest {
    @Test(timeout = 5000)
    public void permiteRetirarEnVacioYTerminaAlCerrar() throws InterruptedException {
        Cola<Integer> cola = new Cola<>();

        assertNull(cola.desencolar());
        assertArrayEquals(new Integer[0], cola.copiar(new Integer[0]));
        cola.cerrar();
        assertNull(cola.esperarPrimero());
        assertThrows(IllegalStateException.class, () -> cola.encolar(1));
    }

    @Test(timeout = 5000)
    public void entregaEnOrdenDeLlegadaYProcesaPendientesAlCerrar() throws InterruptedException {
        Cola<String> cola = new Cola<>();
        cola.encolar("primero");
        cola.encolar("segundo");

        String[] copia = cola.copiar(new String[0]);
        assertArrayEquals(new String[]{"primero", "segundo"}, copia);
        copia[0] = "otro";
        assertEquals("primero", cola.esperarPrimero());
        assertEquals("primero", cola.desencolar());
        assertArrayEquals(new String[]{"segundo"}, cola.copiar(new String[0]));
        assertEquals("segundo", cola.desencolar());
        assertNull(cola.desencolar());

        cola.encolar("tercero");
        cola.encolar("cuarto");
        cola.cerrar();
        assertEquals("tercero", cola.esperarPrimero());
        assertEquals("tercero", cola.desencolar());
        assertEquals("cuarto", cola.esperarPrimero());
        assertEquals("cuarto", cola.desencolar());
        assertNull(cola.esperarPrimero());
    }

    @Test(timeout = 5000)
    public void esperaUnElementoMientrasLaColaEstaAbierta() throws Exception {
        Cola<String> cola = new Cola<>();
        CountDownLatch consumidorListo = new CountDownLatch(1);
        FutureTask<String> lectura = new FutureTask<>(() -> {
            consumidorListo.countDown();
            return cola.esperarPrimero();
        });
        Thread consumidor = new Thread(lectura, "consumidor-prueba");
        consumidor.setDaemon(true);
        consumidor.start();
        try {
            assertTrue(consumidorListo.await(2, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> lectura.get(100, TimeUnit.MILLISECONDS));
            Thread.State estadoEnVacio = consumidor.getState();
            assertEquals(Thread.State.WAITING, estadoEnVacio);
            System.out.println("Consumidor con cola vacía | " + estadoEnVacio);
            cola.encolar("pendiente");
            assertEquals("pendiente", lectura.get(2, TimeUnit.SECONDS));
            assertEquals("pendiente", cola.desencolar());
        } finally {
            lectura.cancel(true);
            consumidor.join(1000);
        }
        assertEquals(Thread.State.TERMINATED, consumidor.getState());
        System.out.println("Consumidor después de recibir | " + consumidor.getState());
    }
}
