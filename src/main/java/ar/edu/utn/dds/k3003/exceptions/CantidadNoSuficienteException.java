package ar.edu.utn.dds.k3003.exceptions;

public class CantidadNoSuficienteException extends RuntimeException {
    public CantidadNoSuficienteException() {
        super("La cantidad no es suficiente");
    }
}
