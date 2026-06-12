package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "donadoresYEntidades", url = "${FACHADA_DYE}")
public interface DonadoresYEntidadesClient {

    @GetMapping("/necesidades/insatisfechas")
    List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(@RequestParam String productoId);

    @PostMapping("/{necesidadID}/satisfaccion")
    NecesidadMaterialDTO satisfacerNecesidad(@PathVariable String necesidadID, @RequestParam Integer cantidad);
}
