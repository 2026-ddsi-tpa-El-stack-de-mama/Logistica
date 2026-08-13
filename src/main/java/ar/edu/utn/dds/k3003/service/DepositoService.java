package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

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

    public String deleteDeposito(String id) {
        depositoR.deleteById(id);
        return "Deposito con id " + id + " eliminado.";
    }

    public String postDonacion(String depositoID, PaqueteDTO paquete){
        try{
            depositoR.findById(depositoID);
            fachada.gestionarDonacion(depositoID, paquete.donacionID(), paquete.producto(), paquete.cantidad());
        } catch (NoSuchElementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return paquete.id();
    }

    public String postEntrega(PaqueteDTO paquete){
        Paquete paqueter = paqueteR.findById(paquete.id()).orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
        asignacionR.findByPaqueteID(paquete.id()).orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
        fachada.reportarEntrega(paquete);
        return "Llegó el paquete " + paqueter.getId();
    }

    public Integer getStock(String productoID){
        Paquete paquete = paqueteR.findByProductos(productoID);
        return paquete.getCantidad();
    }

    public Integer postStock(String productoID, Integer cantidad){
        Paquete paquete = paqueteR.findByProductos(productoID);
        int stock;
        if (paquete.getCantidad() - cantidad > 0){
            stock = paquete.getCantidad() - cantidad;
        }
        else{
            stock = 0;
        }
        paquete.setCantidad(stock);
        paqueteR.save(paquete);
        return paquete.getCantidad();
    }

}

