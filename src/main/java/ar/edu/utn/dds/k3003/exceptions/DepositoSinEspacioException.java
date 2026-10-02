package ar.edu.utn.dds.k3003.exceptions;

public class DepositoSinEspacioException extends RuntimeException {
    public DepositoSinEspacioException(String id) {
        super("No hay espacio en el deposito con ID " + id);
    }
}
