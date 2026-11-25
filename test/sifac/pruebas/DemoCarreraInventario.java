package sifac.pruebas;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import sifac.comun.datos.RepositorioInventarioMemoria;
import sifac.comun.entidades.LineaDetalle;
import sifac.comun.entidades.Producto;

public class DemoCarreraInventario {
    private static final int EXISTENCIAS_INICIALES = 10;
    private static final int CANTIDAD_POR_VENTA = 7;

    public static void main(String[] argumentos) throws InterruptedException {
        System.out.println("Inventario inicial | " + EXISTENCIAS_INICIALES);
        System.out.println("Dos cajas intentan vender " + CANTIDAD_POR_VENTA + " unidades cada una");
        sinSincronizar();
        sincronizado();
    }

    private static void sinSincronizar() throws InterruptedException {
        System.out.println("\nInventario sin sincronizar");
        int[] existencias = {EXISTENCIAS_INICIALES};
        AtomicInteger aceptadas = new AtomicInteger();
        CountDownLatch ambasLecturas = new CountDownLatch(2);
        Runnable venta = () -> {
            int disponible = existencias[0];
            ambasLecturas.countDown();
            try {
                // La barrera hace que ambas cajas lean el saldo inicial antes de descontar.
                ambasLecturas.await();
                if (disponible >= CANTIDAD_POR_VENTA) {
                    existencias[0] = disponible - CANTIDAD_POR_VENTA;
                    aceptadas.incrementAndGet();
                    System.out.println("[" + Thread.currentThread().getName()
                            + "] venta aceptada con el saldo leído de " + disponible);
                }
            } catch (InterruptedException excepcion) {
                Thread.currentThread().interrupt();
            }
        };
        ejecutarVentas(venta);
        mostrarResultado(aceptadas.get(), existencias[0]);
    }

    private static void sincronizado() throws InterruptedException {
        System.out.println("\nInventario sincronizado");
        RepositorioInventarioMemoria inventario = new RepositorioInventarioMemoria();
        Producto producto = new Producto("P001", "Martillo", BigDecimal.ONE);
        inventario.registrar(producto, EXISTENCIAS_INICIALES);
        LineaDetalle[] lineas = {new LineaDetalle(producto, CANTIDAD_POR_VENTA)};
        AtomicInteger aceptadas = new AtomicInteger();
        Runnable venta = () -> {
            try {
                inventario.descontar(lineas);
                aceptadas.incrementAndGet();
                System.out.println("[" + Thread.currentThread().getName() + "] venta aceptada");
            } catch (IllegalArgumentException excepcion) {
                System.out.println("[" + Thread.currentThread().getName()
                        + "] venta rechazada | " + excepcion.getMessage());
            }
        };
        ejecutarVentas(venta);
        mostrarResultado(aceptadas.get(), inventario.consultarExistencias(producto.getCodigo()));
    }

    private static void ejecutarVentas(Runnable venta) throws InterruptedException {
        CountDownLatch cajasListas = new CountDownLatch(2);
        Runnable intento = () -> {
            cajasListas.countDown();
            try {
                cajasListas.await();
                venta.run();
            } catch (InterruptedException excepcion) {
                Thread.currentThread().interrupt();
            }
        };
        Thread cajaUno = new Thread(intento, "caja-1");
        Thread cajaDos = new Thread(intento, "caja-2");
        cajaUno.start();
        cajaDos.start();
        cajaUno.join();
        cajaDos.join();
    }

    private static void mostrarResultado(int aceptadas, int existencias) {
        int saldoSegunVentas = EXISTENCIAS_INICIALES - aceptadas * CANTIDAD_POR_VENTA;
        System.out.println("Ventas aceptadas | " + aceptadas);
        System.out.println("Ventas rechazadas | " + (2 - aceptadas));
        System.out.println("Unidades vendidas | " + aceptadas * CANTIDAD_POR_VENTA);
        System.out.println("Existencias registradas | " + existencias);
        System.out.println("Existencias según las ventas aceptadas | " + saldoSegunVentas);
        System.out.println("Inventario consistente | " + (existencias == saldoSegunVentas ? "Sí" : "No"));
    }
}
