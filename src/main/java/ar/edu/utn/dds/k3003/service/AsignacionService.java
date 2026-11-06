package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.EstadoAsginacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.dtosPropios.AsignacionDirecta;
import ar.edu.utn.dds.k3003.exceptions.AsignacionNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.PaqueteNoEncontradoException;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeoutException;

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
    public Asignacion getAsignacion(String id) {
        return asignacionR.findById(id).orElseThrow(() -> new AsignacionNoEncontradaException(id));
    }

    public List<AsignacionesHistorial> getAsignacionesHistorial() {
        return asignacionesHistorialR.findAll();
    }

    public Optional<PaqueteDTO> getPaquete(String id){    return paqueteR.findById(id)
            .map(paquete -> new PaqueteDTO(
                    paquete.getId(),
                    paquete.getDonacionID(),
                    paquete.getProductos(),
                    paquete.getCantidad()
            ));
    }

    public List<PaqueteDTO> getPaquetes(){
        return paqueteR.findAll().stream().map(paquete -> new PaqueteDTO(
                        paquete.getId(),
                        paquete.getDonacionID(),
                        paquete.getProductos(),
                        paquete.getCantidad()
                ))
                .toList();
    }

    public AsignacionDTO postAsignacion(AsignacionDirecta decision) {
        Paquete paquete = paqueteR.findById(decision.paqueteID()).orElseThrow(() -> new PaqueteNoEncontradoException(decision.paqueteID()));

        Asignacion asignacion = new Asignacion(
                null,
                paquete.getId(),
                decision.necesidadID(),
                LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")),
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
                LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")),
                EstadoAsignacionEnum.ASIGNADA,
                true
        );

        //EVENTO

        ConnectionFactory factory = new ConnectionFactory();
        Map<String, String> env = System.getenv();
        factory.setHost(env.get("E_QUEUE_HOST"));
        factory.setUsername(env.get("E_QUEUE_USERNAME"));
        factory.setPassword(env.get("E_QUEUE_PASSWORD"));
        factory.setVirtualHost(env.get("E_QUEUE_USERNAME"));
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String queueName = env.get("E_QUEUE_NAME");
            channel.queueDeclare(
                    queueName,
                    true,
                    false,
                    false,
                    null
            );
            AsignacionDTO descripcion = new AsignacionDTO(asignacion.getId(), asignacion.getPaqueteID(), asignacion.getNecesidadID(), asignacion.getFecha(), EstadoAsginacionEnum.ASIGNADA);
            EventoQueue evento = new EventoQueue(UUID.randomUUID().toString(), "AsignacionEvento", descripcion);
            ObjectMapper mapper = new ObjectMapper();
            byte[] body = mapper.writeValueAsBytes(evento);
            Map<String, Object> headers = new HashMap<>();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("", queueName, props, body);
            channel.close();
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        //FIN EVENTO
        return asignacionR.save(asignacion);
    }

    public List<Asignacion> getAsignaciones(){return asignacionR.findAll();}
}
