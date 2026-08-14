package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.model.Asignacion;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@FeignClient(name = "logistica", url = "${FACHADA_L}")
public interface LogisticaClient {

    @GetMapping("/paquetes/{id}")
    Optional<Paquete> buscarPaquete(
            @PathVariable("id") String id
    );

    @PostMapping("/asignaciones")
    Asignacion crearAsignacion(
            @RequestBody AsignacionDTO asignacion
    );
}