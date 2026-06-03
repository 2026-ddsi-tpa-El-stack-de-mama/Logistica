package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.DepositoDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DepositoService {
    private Fachada fachada;
    private final List<Deposito> depositos;
    List<Paquete> paquetes = new ArrayList<>();

    public DepositoService(){

        depositos = new ArrayList<>();

        Deposito deposito1 = new Deposito("1", "Depósito Central", TipoAlgoritmoEnum.SUB_ATENDIDOS, "Av. Rivadavia 1234", 500, paquetes);
        Deposito deposito2 = new Deposito("2", "Depósito Norte", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, "Calle Belgrano 456", 300, paquetes);
        Deposito deposito3 = new Deposito("3", "Depósito Solidario", TipoAlgoritmoEnum.SUB_ATENDIDOS, "San Martín 789", 1000, paquetes);
        Deposito deposito4 = new Deposito("4", "Depósito Oeste", TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, "Mitre 321", 750, paquetes);

        depositos.addAll(Arrays.asList(deposito1, deposito2, deposito3, deposito4));
    }
    public Optional<Deposito> getDeposito(String id) {
        Optional<Deposito> optional = Optional.empty();
        for(Deposito d : depositos){
            if(id.equals(d.getId())){
                optional = Optional.of(d);
                return optional;
            }
        } return optional;
    }

    public List<Deposito> getDepositos() {
        return depositos;
    }

    public Deposito postDeposito(Deposito deposito) {
        depositos.add(deposito);
        return deposito;
    }

    public Optional<Deposito> deleteDeposito(String id) {
        Optional<Deposito> optional = Optional.empty();
        for(Deposito d : depositos){
            if(Objects.equals(id, d.getId())){
                optional = Optional.of(d);
                depositos.remove(d);
                return optional;
            }
        } return optional;
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

