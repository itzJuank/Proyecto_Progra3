package sifac.comun.estructuras;

import java.util.Arrays;
import java.util.Objects;

public class Cola<T> {
    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int cantidad;
    private boolean cerrada;

    public synchronized void encolar(T valor) {
        if (cerrada) {
            throw new IllegalStateException("La cola está cerrada");
        }
        Nodo<T> nuevo = new Nodo<>(Objects.requireNonNull(valor));
        if (ultimo == null) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        cantidad++;
        notifyAll();
    }

    // El consumidor retira este valor cuando confirma que lo procesó.
    public synchronized T esperarPrimero() throws InterruptedException {
        while (primero == null && !cerrada) {
            wait();
        }
        return primero == null ? null : primero.valor;
    }

    public synchronized T desencolar() {
        if (primero == null) {
            return null;
        }
        T valor = primero.valor;
        primero = primero.siguiente;
        if (primero == null) {
            ultimo = null;
        }
        cantidad--;
        return valor;
    }

    public synchronized T[] copiar(T[] modelo) {
        T[] copia = Arrays.copyOf(modelo, cantidad);
        int indice = 0;
        for (Nodo<T> actual = primero; actual != null; actual = actual.siguiente) {
            copia[indice] = actual.valor;
            indice++;
        }
        return copia;
    }

    public synchronized boolean estaCerrada() {
        return cerrada;
    }

    public synchronized void cerrar() {
        cerrada = true;
        notifyAll();
    }
}
