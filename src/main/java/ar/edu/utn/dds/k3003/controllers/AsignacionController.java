package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.dtosPropios.AsignacionDirecta;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.AsignacionesHistorial;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.service.AsignacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/asignacionesHistorial")
    public List<AsignacionesHistorial> getAsignacionesHistorial(){
        return asignacionService.getAsignacionesHistorial();
    }

    @GetMapping("/paquetes/{id}")
    public Optional<Paquete> buscarPaquete(@PathVariable("id") String id){
        return asignacionService.getPaquete(id);
    }

    @PostMapping("/asignaciones")
    public Asignacion crearAsignacion(@RequestBody AsignacionDTO asignacion){
        return asignacionService.postAsignacion(asignacion);
    }

    @PostMapping("/asignacionesDirecta")
    public Asignacion postAsignacionesDirecta(@RequestBody AsignacionDirecta asignacionDirecta) {
        return asignacionService.postAsignacionesDirecta(asignacionDirecta);
    }

    @GetMapping("/asignaciones")
    public List<Asignacion> getAsignaciones() {
        return asignacionService.getAsignaciones();
    }
}
