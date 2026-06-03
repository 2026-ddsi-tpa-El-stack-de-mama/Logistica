package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.service.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public Optional<Deposito> deleteDeposito(@PathVariable String id) {
        return depositoService.deleteDeposito(id);
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
    public Deposito postDonacion(@PathVariable String id, @RequestParam String donacionID, @RequestParam String productoID, @RequestParam Integer cantidad){
        return depositoService.postDonacion(id, donacionID, productoID, cantidad);
    }

    @PostMapping("/entregas")
    public void postEntrega(@RequestParam PaqueteDTO paquete){
        depositoService.postEntrega(paquete);
    }

}
