package sifac.comun.estructuras;

import java.util.Objects;

public class ListaEnlazada<T> {
    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int cantidad;

    public void agregar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        if (ultimo == null) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        cantidad++;
    }

    public boolean eliminar(T valor) {
        Nodo<T> anterior = null;
        Nodo<T> actual = primero;
        while (actual != null && !Objects.equals(actual.valor, valor)) {
            anterior = actual;
            actual = actual.siguiente;
        }
        if (actual == null) {
            return false;
        }
        if (anterior == null) {
            primero = actual.siguiente;
        } else {
            anterior.siguiente = actual.siguiente;
        }
        if (actual == ultimo) {
            ultimo = anterior;
        }
        cantidad--;
        return true;
    }

    public int cantidad() {
        return cantidad;
    }

    public void copiarEn(T[] destino) {
        if (destino.length < cantidad) {
            throw new IllegalArgumentException("El arreglo debe tener espacio para todos los elementos");
        }
        int indice = 0;
        for (Nodo<T> actual = primero; actual != null; actual = actual.siguiente) {
            destino[indice] = actual.valor;
            indice++;
        }
    }
}
