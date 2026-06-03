package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.service.AsignacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
public class AsignacionController {
    private final AsignacionService asignacionService;

    @Autowired
    public AsignacionController(AsignacionService asignacionService){
        this.asignacionService = asignacionService;
    }

    @GetMapping("/asignaciones/{id}")
    public Asignacion getAsignacion(@PathVariable String id){
        Optional<Asignacion> asignacion = asignacionService.getAsignacion(id);
        return asignacion.orElse(null);
    }
}
