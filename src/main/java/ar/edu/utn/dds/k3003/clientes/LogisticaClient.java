package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "logistica", url = "${FACHADA_L}")
public interface LogisticaClient {

    @GetMapping("/paquetes/{id}")
    PaqueteDTO buscarPaquete(
            @PathVariable("id") String id
    );

    @PostMapping("/asignaciones")
    AsignacionDTO crearAsignacion(
            @RequestBody AsignacionDTO asignacion
    );
}