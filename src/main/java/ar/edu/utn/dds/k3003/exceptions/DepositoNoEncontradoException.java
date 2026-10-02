package ar.edu.utn.dds.k3003.exceptions;

public class DepositoNoEncontradoException extends RuntimeException {
    public DepositoNoEncontradoException(String id) {
        super("No existe el depósito con ID: " + id);
    }
}
