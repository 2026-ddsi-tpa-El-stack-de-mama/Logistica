package ar.edu.utn.dds.k3003.controllers;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
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
    public String postDonacion(@PathVariable String id, @RequestBody Paquete paquete){
        return depositoService.postDonacion(id, paquete);
    }

    @PostMapping("/entregas")
    public String postEntrega(@RequestBody PaqueteDTO paquete){
        return depositoService.postEntrega(paquete);
    }


    @GetMapping("/ping-datadog")
    public String ping() {
        System.out.println("ENTRÓ AL ENDPOINT");

        depositoService.enviarLogADatadog("Ping Datadog desde Render 🚀");

        return "ok";
    }


}
