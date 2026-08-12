package ar.edu.utn.dds.k3003.queue;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.clientes.LogisticaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.Double.compare;

public class AsignacionWorker extends DefaultConsumer {
    private String queueName;
    private LogisticaClient logisticaClient;
    private DonadoresYEntidadesClient donadoresYEntidadesClient;

    public AsignacionWorker(Channel channel, String queueName, LogisticaClient logisticaClient, DonadoresYEntidadesClient donadoresYEntidadesClient) {
        super(channel);
        this.queueName = queueName;
        this.logisticaClient = logisticaClient;
        this.donadoresYEntidadesClient = donadoresYEntidadesClient;
    }

    public void init() throws IOException {
// Declarar la cola desde la cual consumir mensajes
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);
// Consumir mensajes de la cola
        this.getChannel().basicConsume(this.queueName, false, this);
    };

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        try {
            //Leer el mensaje
            String json = new String(body, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            AsignacionQueue mensaje = mapper.readValue(json, AsignacionQueue.class);
            String paqueteId = mensaje.paqueteID();
            TipoAlgoritmoEnum algoritmo = mensaje.algoritmo();

            System.out.println("Paquete recibido: " + paqueteId);
            System.out.println("Algoritmo: " + algoritmo);

            //Sigue algoritmo

            PaqueteDTO paquete = logisticaClient.buscarPaquete(paqueteId);
            List<NecesidadMaterialDTO> necesidades = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(paquete.producto());
            ejecutarMatchmaking(paquete, necesidades, algoritmo);
            if (paquete.cantidad() <= 0){
                throw new RuntimeException("No hay cantidad suficiente");
            }


            getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch (Exception e) {
            e.printStackTrace();

            getChannel().basicNack(
                    envelope.getDeliveryTag(),
                    false,
                    true
            );
        }
    }

    private AsignacionDTO ejecutarMatchmaking(PaqueteDTO paquete, List<NecesidadMaterialDTO> necesidades, TipoAlgoritmoEnum algoritmo) {
        LocalDateTime tiempo = LocalDateTime.now();
        EstadoAsginacionEnum estado = EstadoAsginacionEnum.ASIGNADA;

        NecesidadMaterialDTO necesidad;
        if (algoritmo == TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE) {
            necesidad = necesidades.stream().max((n1, n2) -> {
                double score1 = n1.nivelDeUrgencia() / ((double) paquete.cantidad() / n1.cantidadObjetivo());
                double score2 = n2.nivelDeUrgencia() / ((double) paquete.cantidad() / n2.cantidadObjetivo());
                return compare(score1, score2);
            }).orElseThrow(RuntimeException::new);
        } else {
            necesidad = necesidades.stream().max((n1, n2) -> {
                int d1 = n1.cantidadObjetivo() - paquete.cantidad();
                int d2 = n2.cantidadObjetivo() - paquete.cantidad();
                return compare(d1, d2);
            }).orElseThrow(RuntimeException::new);
        }
        AsignacionDTO asignacion = new AsignacionDTO(
                null,
                paquete.id(),
                necesidad.id(),
                LocalDateTime.now(),
                EstadoAsginacionEnum.ASIGNADA
        );

        logisticaClient.crearAsignacion(asignacion);
        return asignacion;
    }

}

