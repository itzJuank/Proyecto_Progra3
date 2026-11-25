package sifac.comun.estructuras;

public class Pila<T> {
    private Nodo<T> cima;
    private int cantidad;

    public void apilar(T valor) {
        Nodo<T> nuevo = new Nodo<>(valor);
        nuevo.siguiente = cima;
        cima = nuevo;
        cantidad++;
    }

    public T desapilar() {
        if (cima == null) {
            return null;
        }
        T valor = cima.valor;
        cima = cima.siguiente;
        cantidad--;
        return valor;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int cantidad() {
        return cantidad;
    }

    public void copiarEnOrdenDeInsercion(T[] destino) {
        if (destino.length < cantidad) {
            throw new IllegalArgumentException("El arreglo debe tener espacio para todos los elementos");
        }
        int indice = cantidad - 1;
        for (Nodo<T> actual = cima; actual != null; actual = actual.siguiente) {
            destino[indice] = actual.valor;
            indice--;
        }
    }
}
