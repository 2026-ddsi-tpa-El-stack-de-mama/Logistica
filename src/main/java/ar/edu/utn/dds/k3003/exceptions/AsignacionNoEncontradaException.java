package ar.edu.utn.dds.k3003.exceptions;

public class AsignacionNoEncontradaException extends RuntimeException {
    public AsignacionNoEncontradaException(String id) {
        super("No existe la asignación con paquete ID: " + id);
    }
}
