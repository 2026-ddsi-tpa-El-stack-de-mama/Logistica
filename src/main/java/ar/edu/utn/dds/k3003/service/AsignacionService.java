package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.dtosPropios.AsignacionDirecta;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {
    private final AsignacionRepository asignacionR;
    private final AsignacionesHistorialRepository asignacionesHistorialR;
    private final PaqueteRepository paqueteR;

    public AsignacionService(AsignacionRepository asignacionR, AsignacionesHistorialRepository asignacionesHistorialR, PaqueteRepository paqueteR){
        this.asignacionR = asignacionR;
        this.asignacionesHistorialR = asignacionesHistorialR;
        this.paqueteR = paqueteR;
    }
    public Optional<Asignacion> getAsignacion(String id) {
        return asignacionR.findById(id);
    }

    public List<AsignacionesHistorial> getAsignacionesHistorial() {
        return asignacionesHistorialR.findAll();
    }

    public Optional<Paquete> getPaquete(String id){
        return paqueteR.findById(id);
    }

    public Asignacion postAsignacion(AsignacionDTO asignacionDTO){
        EstadoAsignacionEnum estado;
        if (asignacionDTO.estado() == EstadoAsginacionEnum.ASIGNADA){
            estado = EstadoAsignacionEnum.ASIGNADA;
        }
        else{
            estado = EstadoAsignacionEnum.COMPLETADA;
        }
        Asignacion asignacion = new Asignacion(
            asignacionDTO.id(),
            asignacionDTO.paqueteID(),
            asignacionDTO.necesidadID(),
            asignacionDTO.fecha(),
            estado,
            false
        );
        return asignacion;
    }

    public Asignacion postAsignacionesDirecta(AsignacionDirecta infoAsignacion){
        Asignacion asignacion = new Asignacion(
                null,
                infoAsignacion.paqueteID(),
                infoAsignacion.necesidadID(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                true
        );
        Asignacion asignacionMuestra = asignacionR.save(asignacion);
        return asignacionMuestra;
    }

    public List<Asignacion> getAsignaciones(){return asignacionR.findAll();}
}
