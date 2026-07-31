package ar.edu.utn.dds.k3003.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="asignacioneshistorial")
public class AsignacionesHistorial {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String IDasignacion;
    @Enumerated(EnumType.STRING)
    private EstadoAsignacionEnum estado;
    private LocalDateTime fecha;

    public AsignacionesHistorial(String id, String IDasignacion, EstadoAsignacionEnum estado, LocalDateTime fecha) {
        this.id = id;
        this.IDasignacion = IDasignacion;
        this.fecha = fecha;
        this.estado = estado;
    }

    protected AsignacionesHistorial(){}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getIDasignacion() {
        return IDasignacion;
    }
    public void setIDasignacion(String IDasignacion) {
        this.IDasignacion = IDasignacion;
    }

    public EstadoAsignacionEnum getEstado() {
        return estado;
    }
    public void setEstado(EstadoAsignacionEnum estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
