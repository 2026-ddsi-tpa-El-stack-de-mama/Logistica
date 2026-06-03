package ar.edu.utn.dds.k3003.model;

import java.time.LocalDateTime;

public class Asignacion {
    private String id;
    private String paqueteID;
    private String necesidadID;
    private LocalDateTime fecha;
    private EstadoAsignacionEnum estado;

    public Asignacion(String id, String paqueteID, String necesidadID, LocalDateTime fecha, EstadoAsignacionEnum estado) {
        this.id = id;
        this.paqueteID = paqueteID;
        this.necesidadID = necesidadID;
        this.fecha = fecha;
        this.estado = estado;
    }

    //GETTER Y SETTERS
    //ID
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    //Paquete ID
    public String getPaqueteID() {
        return paqueteID;
    }
    public void setPaqueteID(String paqueteID) {
        this.paqueteID = paqueteID;
    }
    //Necesidad ID
    public String getNecesidadID() {
        return necesidadID;
    }
    public void setNecesidadID(String necesidadID) {
        this.necesidadID = necesidadID;
    }
    //Fecha
    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    //Estado
    public EstadoAsignacionEnum getEstado() {
        return estado;
    }
    public void setEstado(EstadoAsignacionEnum estado) {
        this.estado = estado;
    }
}
