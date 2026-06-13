package ar.edu.utn.dds.k3003.clientes;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "donaciones", url="${FACHADA_D}")
public interface DonacionesClient {
    @PatchMapping("/donaciones/{id}/estado")
    public ResponseEntity<DonacionDTO> cambiarEstadoDeDonacion(@PathVariable("id") String donacionID, @RequestParam EstadoDonacionEnum estado);
}
