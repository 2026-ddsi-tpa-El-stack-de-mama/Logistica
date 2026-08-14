package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="asignaciones")
public class Asignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String paqueteID;
    private String necesidadID;
    private LocalDateTime fecha;
    @Enumerated(EnumType.STRING)
    private EstadoAsignacionEnum estado;
    private Boolean asignacionDirecta;

    public Asignacion(String id, String paqueteID, String necesidadID, LocalDateTime fecha, EstadoAsignacionEnum estado, Boolean asignacionDirecta) {
        this.id = id;
        this.paqueteID = paqueteID;
        this.necesidadID = necesidadID;
        this.fecha = fecha;
        this.estado = estado;
        this.asignacionDirecta = asignacionDirecta;
    }

    protected Asignacion() {
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
    //Asignacion directa
    public Boolean getAsignacionDirecta() {
        return asignacionDirecta;
    }
    public void setAsignacionDirecta(Boolean asignacionDirecta) {
        this.asignacionDirecta = asignacionDirecta;
    }

}
