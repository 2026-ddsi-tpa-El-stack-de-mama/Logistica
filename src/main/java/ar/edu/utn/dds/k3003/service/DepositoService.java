package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class DepositoService {
    private final Fachada fachada;
    private final DepositoRepository depositoR;
    private final PaqueteRepository paqueteR;
    private final AsignacionRepository asignacionR;

    public DepositoService(Fachada fachada, DepositoRepository depositoR, PaqueteRepository paqueteR, AsignacionRepository asignacionR){
        this.fachada = fachada;
        this.depositoR = depositoR;
        this.paqueteR = paqueteR;
        this.asignacionR = asignacionR;
    }

    public Optional<Deposito> getDeposito(String id) {
        return depositoR.findById(id);
    }

    public List<Deposito> getDepositos() {
        return depositoR.findAll();
    }

    public Deposito postDeposito(Deposito deposito) {
        depositoR.save(deposito);
        return deposito;
    }

    public Optional<Deposito> deleteDeposito(String id) {
        Optional<Deposito> deposito = depositoR.findById(id);
        depositoR.deleteById(id);
        return deposito;
    }

    public String postDonacion(String depositoID, Paquete paquete){
        try{
            depositoR.findById(depositoID);
            fachada.gestionarDonacion(depositoID, paquete.getDonacionID(), paquete.getProductos(), paquete.getCantidad());
        } catch (NoSuchElementException e) {
            throw new RuntimeException(e);
        }

        return paquete.getId();
    }
    public String postEntrega(PaqueteDTO paquete){
        paqueteR.findById(paquete.id()).orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
        asignacionR.findByPaqueteID(paquete.id()).orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
        fachada.reportarEntrega(paquete);
        return "Llegó el paquete " + paquete.id();
    }

}

