package sifac.comun.logica;

import sifac.comun.datos.RepositorioDocumentos;
import sifac.comun.entidades.Documento;
import sifac.comun.estructuras.Cola;

class ProcesadorEnvios implements Runnable {
    private final Cola<Documento> pendientes;
    private final RepositorioDocumentos destino;
    private Exception fallo;

    ProcesadorEnvios(Cola<Documento> pendientes, RepositorioDocumentos destino) {
        this.pendientes = pendientes;
        this.destino = destino;
    }

    @Override
    public void run() {
        try {
            Documento documento;
            while ((documento = pendientes.esperarPrimero()) != null) {
                destino.guardar(documento);
                documento.marcarEnviado();
                pendientes.desencolar();
            }
        } catch (InterruptedException excepcion) {
            registrarFallo(excepcion);
            Thread.currentThread().interrupt();
        } catch (RuntimeException excepcion) {
            registrarFallo(excepcion);
        }
    }

    synchronized void verificarFuncionamiento() {
        if (fallo != null) {
            throw new IllegalStateException("El envío local se detuvo | " + fallo.getMessage(), fallo);
        }
    }

    private synchronized void registrarFallo(Exception excepcion) {
        fallo = excepcion;
    }
}
