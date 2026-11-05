package ar.edu.utn.dds.k3003.service;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.PaqueteDTO;
import ar.edu.utn.dds.k3003.eventos.DepositoEvento;
import ar.edu.utn.dds.k3003.exceptions.AsignacionNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.DepositoNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.PaqueteNoEncontradoException;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.model.Paquete;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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

    public Deposito getDeposito(String id) {
        return depositoR.findById(id).orElseThrow(() -> new DepositoNoEncontradoException(id));
    }

    public List<Deposito> getDepositos() {
        return depositoR.findAll();
    }

    public Deposito postDeposito(Deposito deposito) {
        depositoR.save(deposito);

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

            DepositoEvento descripcion = new DepositoEvento(deposito.getId());
            EventoQueue evento = new EventoQueue(UUID.randomUUID().toString(), "DepositoEvento", descripcion);
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

        return deposito;
    }

    public String deleteDeposito(String id) {
        depositoR.deleteById(id);
        return "Deposito con id " + id + " eliminado.";
    }

    public String postDonacion(String depositoID, PaqueteDTO paquete){
        try{
            fachada.gestionarDonacion(depositoID, paquete.donacionID(), paquete.producto(), paquete.cantidad());
        } catch (NoSuchElementException | IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return paquete.id();
    }

    public String postEntrega(PaqueteDTO paquete){
        Paquete paqueter = paqueteR.findById(paquete.id()).orElseThrow(() -> new PaqueteNoEncontradoException(paquete.id()));
        asignacionR.findByPaqueteID(paquete.id()).orElseThrow(() -> new AsignacionNoEncontradaException(paquete.id()));
        fachada.reportarEntrega(paquete);
        return "Llegó el paquete " + paqueter.getId();
    }

    public String getStock(String productoID){
        List<Paquete> paquete = paqueteR.findByProductos(productoID);
        String stocks = "";
        for (int i = 0; i < paquete.size(); i++){
            stocks += "Cantidad: " + paquete.get(i).getCantidad() + ". Paquete: " + paquete.get(i).getId() + "\n";
        }
        return stocks;
    }

    public Integer postStock(String productoID, Integer cantidad){
        Paquete paquete = (Paquete) paqueteR.findByProductos(productoID);
        int stock = Math.max(paquete.getCantidad() - cantidad, 0);
        paquete.setCantidad(stock);
        paqueteR.save(paquete);
        return paquete.getCantidad();
    }

}

