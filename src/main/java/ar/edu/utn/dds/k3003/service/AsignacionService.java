package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AsignacionService {
    private final AsignacionRepository asignacionR;

    public AsignacionService(AsignacionRepository asignacionR){
        this.asignacionR = asignacionR;
    }
    public Optional<Asignacion> getAsignacion(String id) {
        return asignacionR.findById(id);
    }
}
