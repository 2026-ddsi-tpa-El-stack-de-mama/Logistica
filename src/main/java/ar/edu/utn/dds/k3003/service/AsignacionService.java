package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.AsignacionesHistorial;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {
    private final AsignacionRepository asignacionR;
    private final AsignacionesHistorialRepository asignacionesHistorialR;

    public AsignacionService(AsignacionRepository asignacionR, AsignacionesHistorialRepository asignacionesHistorialR){
        this.asignacionR = asignacionR;
        this.asignacionesHistorialR = asignacionesHistorialR;
    }
    public Optional<Asignacion> getAsignacion(String id) {
        return asignacionR.findById(id);
    }

    public List<AsignacionesHistorial> getAsignacionesHistorial() {
        return asignacionesHistorialR.findAll();
    }
}
