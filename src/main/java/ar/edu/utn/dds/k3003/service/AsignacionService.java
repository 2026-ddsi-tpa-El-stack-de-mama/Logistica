package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.dtosPropios.AsignacionDirecta;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsignacionService {
    private final Fachada fachada;
    private final DonadoresYEntidadesClient donadoresYEntidadesClient;
    private final AsignacionRepository asignacionR;
    private final AsignacionesHistorialRepository asignacionesHistorialR;
    private final PaqueteRepository paqueteR;

    public AsignacionService(Fachada fachada, DonadoresYEntidadesClient donadoresYEntidadesClient, AsignacionRepository asignacionR, AsignacionesHistorialRepository asignacionesHistorialR, PaqueteRepository paqueteR){
        this.fachada = fachada;
        this.donadoresYEntidadesClient = donadoresYEntidadesClient;
        this.asignacionR = asignacionR;
        this.asignacionesHistorialR = asignacionesHistorialR;
        this.paqueteR = paqueteR;
    }
    public Optional<Asignacion> getAsignacion(String id) {
        return asignacionR.findById(id);
    }

    public List<AsignacionesHistorial> getAsignacionesHistorial() {
        return asignacionesHistorialR.findAll();
    }

    public Optional<Paquete> getPaquete(String id){
        return paqueteR.findById(id);
    }

    public AsignacionDTO postAsignacion(AsignacionDirecta decision) {
        Paquete paquete = paqueteR.findById(decision.paqueteID()).orElseThrow(() -> new RuntimeException("No existe el paquete"));

        Asignacion asignacion = new Asignacion(
                null,
                paquete.getId(),
                decision.necesidadID(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                false
        );

        asignacionR.save(asignacion);

        PaqueteDTO paqueteDTO = new PaqueteDTO(
                paquete.getId(),
                paquete.getDonacionID(),
                paquete.getProductos(),
                paquete.getCantidad()
        );

        String depositoID = paquete.getDeposito().getId();

        List<NecesidadMaterialDTO> necesidades = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(paquete.getProductos());

        return fachada.ejecutarMatchmaking(depositoID, paqueteDTO, necesidades);
    }

    public Asignacion postAsignacionesDirecta(AsignacionDirecta infoAsignacion){
        Asignacion asignacion = new Asignacion(
                null,
                infoAsignacion.paqueteID(),
                infoAsignacion.necesidadID(),
                LocalDateTime.now(),
                EstadoAsignacionEnum.ASIGNADA,
                true
        );
        Asignacion asignacionMuestra = asignacionR.save(asignacion);
        return asignacionMuestra;
    }

    public List<Asignacion> getAsignaciones(){return asignacionR.findAll();}
}
