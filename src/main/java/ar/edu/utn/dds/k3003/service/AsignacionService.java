package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.EstadoAsignacionEnum;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {
    private final AsignacionRepository asignacionR;

    public AsignacionService(AsignacionRepository asignacionR){
        this.asignacionR = asignacionR;

        Asignacion asignacion1 = new Asignacion("1", "paq1", "nec1", LocalDateTime.of(2026, 5, 10, 14, 30), EstadoAsignacionEnum.ASIGNADA);
        Asignacion asignacion2 = new Asignacion("2", "paq2", "nec2", LocalDateTime.of(2026, 5, 11, 9, 15), EstadoAsignacionEnum.COMPLETADA);
        Asignacion asignacion3 = new Asignacion("3", "paq3", "nec3", LocalDateTime.of(2026, 5, 11, 18, 45), EstadoAsignacionEnum.ASIGNADA);
        Asignacion asignacion4 = new Asignacion("4", "paq4", "nec4", LocalDateTime.of(2026, 5, 12, 11, 0), EstadoAsignacionEnum.COMPLETADA);

        asignacionR.save(asignacion1);
        asignacionR.save(asignacion2);
        asignacionR.save(asignacion3);
        asignacionR.save(asignacion4);
    }
    public Optional<Asignacion> getAsignacion(String id) {
        return asignacionR.findById(id);
    }
}
