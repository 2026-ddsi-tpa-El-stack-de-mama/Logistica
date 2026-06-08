package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepositoService {
    private Fachada fachada;
    private final DepositoRepository depositoR;
    private final PaqueteRepository paqueteR;

    public DepositoService(DepositoRepository depositoR, PaqueteRepository paqueteR){
        this.depositoR = depositoR;
        this.paqueteR = paqueteR;

        Deposito deposito1 = new Deposito("1", "Depósito Central", TipoAlgoritmoEnum.SUB_ATENDIDOS, "Av. Rivadavia 1234", 500,new ArrayList<>());
        Deposito deposito2 = new Deposito("2", "Depósito Norte", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, "Calle Belgrano 456", 300, new ArrayList<>());
        Deposito deposito3 = new Deposito("3", "Depósito Solidario", TipoAlgoritmoEnum.SUB_ATENDIDOS, "San Martín 789", 1000, new ArrayList<>());
        Deposito deposito4 = new Deposito("4", "Depósito Oeste", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, "Mitre 321", 750, new ArrayList<>());

        depositoR.save(deposito1);
        depositoR.save(deposito2);
        depositoR.save(deposito3);
        depositoR.save(deposito4);
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

    public Deposito postDonacion(String depositoID, String donacionID, String productoID, Integer cantidad){
        DepositoDTO depositoDTO = fachada.gestionarDonacion(depositoID, donacionID, productoID, cantidad);
        return new Deposito(
                depositoID,
                depositoDTO.nombre(),
                depositoDTO.algoritmo(),
                depositoDTO.direccion(),
                depositoDTO.capacidadMaxima(),
                null
        );
    }
    /*Se que esto no es correcto pero como sé que no puedo probarlo
    (al necesitar otra fachada), lo dejo así y en la entrega 3 lo
    arreglaré con mi grupo
     */
    public void postEntrega(PaqueteDTO paquete){
        fachada.reportarEntrega(paquete);
    }
}

