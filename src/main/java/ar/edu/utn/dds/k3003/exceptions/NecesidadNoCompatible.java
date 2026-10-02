package ar.edu.utn.dds.k3003.exceptions;

public class NecesidadNoCompatible extends RuntimeException {
    public NecesidadNoCompatible() {
        super("No hay otra necesidad compatible");
    }
}
