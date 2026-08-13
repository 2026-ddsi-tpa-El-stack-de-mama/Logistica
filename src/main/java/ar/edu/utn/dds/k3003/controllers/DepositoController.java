package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.service.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
public class DepositoController {
    private final DepositoService depositoService;

    @Autowired
    public DepositoController(DepositoService depositoService){
        this.depositoService = depositoService;
    }

    @GetMapping("/depositos/{id}")
    public Deposito getDeposito(@PathVariable String id){
        Optional<Deposito> deposito = depositoService.getDeposito(id);
        return deposito.orElse(null);
    }

    @DeleteMapping("/depositos/{id}")
    public ResponseEntity<String> deleteDeposito(@PathVariable String id) {
        return ResponseEntity.ok(depositoService.deleteDeposito(id));
    }

    @GetMapping("/depositos")
    public List<Deposito> getDeposito(){
        return depositoService.getDepositos();
    }

    @PostMapping("/depositos")
    public Deposito postDeposito(@RequestBody Deposito deposito) {
        return depositoService.postDeposito(deposito);
    }

    @PostMapping("/depositos/{id}/donacion")
    public String postDonacion(@PathVariable String id, @RequestBody PaqueteDTO paquete){
        return depositoService.postDonacion(id, paquete);
    }

    @PostMapping("/entregas")
    public String postEntrega(@RequestBody PaqueteDTO paquete){
        return depositoService.postEntrega(paquete);
    }

    @GetMapping("/stock/{productoID}")
    public Integer getStock(@PathVariable String productoID){return depositoService.getStock(productoID);}

    @PostMapping("/stock/{productoID}")
    public Integer postStock(@PathVariable String productoID, @RequestBody Integer cantidad){
        return depositoService.postStock(productoID, cantidad);
    }

}
