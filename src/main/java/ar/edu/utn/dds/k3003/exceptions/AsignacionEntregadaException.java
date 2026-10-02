package ar.edu.utn.dds.k3003.exceptions;

public class AsignacionEntregadaException extends RuntimeException {
    public AsignacionEntregadaException() {
        super("La asignación ya fue entregada");
    }
}
