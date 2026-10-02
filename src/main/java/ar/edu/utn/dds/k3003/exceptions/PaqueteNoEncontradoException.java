package ar.edu.utn.dds.k3003.exceptions;

public class PaqueteNoEncontradoException extends RuntimeException {
    public PaqueteNoEncontradoException(String id) {
        super("No existe el paquete con ID " + id);
    }
}
