package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.clientes.LogisticaClient;
import ar.edu.utn.dds.k3003.queue.AsignacionWorker;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.Map;

@SpringBootApplication
@EnableFeignClients
public class Application implements CommandLineRunner {
  private final LogisticaClient logisticaClient;
  private final DonadoresYEntidadesClient donadoresYEntidadesClient;

  public Application(
          LogisticaClient logisticaClient,
          DonadoresYEntidadesClient donadoresYEntidadesClient) {

    this.logisticaClient = logisticaClient;
    this.donadoresYEntidadesClient = donadoresYEntidadesClient;
  }

  @Override
  public void run(String... args) throws Exception {

    Map<String, String> env = System.getenv();

    ConnectionFactory factory = new ConnectionFactory();

    factory.setHost(env.get("QUEUE_HOST"));
    factory.setUsername(env.get("QUEUE_USERNAME"));
    factory.setPassword(env.get("QUEUE_PASSWORD"));
    factory.setVirtualHost(env.get("QUEUE_USERNAME"));

    String queueName = env.get("QUEUE_NAME");

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    AsignacionWorker worker = new AsignacionWorker(
            channel,
            queueName,
            logisticaClient,
            donadoresYEntidadesClient
    );

    worker.init();
  }
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
