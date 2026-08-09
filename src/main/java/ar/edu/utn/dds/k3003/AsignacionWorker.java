package ar.edu.utn.dds.k3003;

import com.rabbitmq.client.*;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.util.Map;

public class AsignacionWorker extends DefaultConsumer {
    private String queueName;
    private EntityManagerFactory entityManagerFactory;

    protected AsignacionWorker(Channel channel, String queueName) {
        super(channel);
        this.queueName = queueName;
    }

    private void init() throws IOException {
// Declarar la cola desde la cual consumir mensajes
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);
// Consumir mensajes de la cola
        this.getChannel().basicConsume(this.queueName, false, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
// Confirmar la recepción del mensaje a la mensajeria
        this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        String analisisId = new String(body, "UTF-8");
        System.out.println("Se recibio el siguiente payload:");
        System.out.println(analisisId);
    }

    public static void main(String[] args) throws Exception {
// Establecer la conexión con CloudAMQP
        Map<String, String> env = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(env.get("QUEUE_HOST"));
        factory.setUsername(env.get("QUEUE_USERNAME"));
        factory.setPassword(env.get("QUEUE_PASSWORD"));
// En el plan más barato, el VHOST == USER
        factory.setVirtualHost(env.get("QUEUE_USERNAME"));
        String queueName = env.get("QUEUE_NAME");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        AsignacionWorker worker = new AsignacionWorker(channel,queueName);
        worker.init();
    }

}

