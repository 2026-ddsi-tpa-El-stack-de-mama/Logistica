package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.eventos.AsignacionEvento;
import ar.edu.utn.dds.k3003.eventos.DepositoEvento;
import ar.edu.utn.dds.k3003.eventos.EntidadDepositoEvento;
import ar.edu.utn.dds.k3003.exceptions.*;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.observabilidad.TraceContext;
import ar.edu.utn.dds.k3003.queue.AsignacionQueue;
import ar.edu.utn.dds.k3003.queue.EventoQueue;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class Fachada implements FachadaLogistica {
  private final DonacionesClient donacionesClient;
  private final DonadoresYEntidadesClient donadoresYEntidadesClient;
  private final DepositoRepository depositoR;
  private final PaqueteRepository paqueteR;
  private final AsignacionRepository asignacionR;
  private final AsignacionesHistorialRepository asignacionesHistorialR;
  private final MeterRegistry metricas;
  private static final Logger log = LoggerFactory.getLogger(Fachada.class);

  @Autowired
  public Fachada(DepositoRepository depositoR, PaqueteRepository paqueteR, AsignacionRepository asignacionR, DonacionesClient donacionesClient, DonadoresYEntidadesClient donadoresYEntidadesClient, AsignacionesHistorialRepository asignacionesHistorialR, MeterRegistry metricas) {
      this.depositoR = depositoR;
      this.paqueteR = paqueteR;
      this.asignacionR = asignacionR;
      this.donacionesClient = donacionesClient;
      this.donadoresYEntidadesClient = donadoresYEntidadesClient;
      this.asignacionesHistorialR = asignacionesHistorialR;
      this.metricas = metricas;
  }

  @Override
  public DepositoDTO agregarDeposito(DepositoDTO deposito) {
      Deposito dep = new Deposito(
              deposito.id(),
              deposito.nombre(),
              deposito.algoritmo(),
              deposito.direccion(),
              deposito.capacidadMaxima(),
              null
      );
      dep = depositoR.save(dep);
      return new DepositoDTO(dep.getId(), dep.getAlgoritmo(), dep.getNombre(), dep.getDireccion(), dep.getCapacidadMaxima(), null);
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new DepositoNoEncontradoException(depositoID));
    List<PaqueteDTO> stock = deposito.getStockActual().stream().map(p -> new PaqueteDTO(
                      p.getId(),
                      p.getDonacionID(),
                      p.getProductos(),
                      p.getCantidad())).toList();
      return new DepositoDTO(deposito.getId(), deposito.getAlgoritmo(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), stock);
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {
      Asignacion asignacion = asignacionR.findByPaqueteID(paqueteID).orElseThrow(() -> new AsignacionNoEncontradaException(paqueteID));
      EstadoAsginacionEnum estado;
      if (asignacion.getEstado() == EstadoAsignacionEnum.ASIGNADA){estado = EstadoAsginacionEnum.ASIGNADA;}
      else{estado = EstadoAsginacionEnum.COMPLETADA;}
      return new AsignacionDTO(
              asignacion.getId(),
              paqueteID,
              asignacion.getNecesidadID(),
              asignacion.getFecha(),
              estado
      );
  }

  @Override
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException, IOException, TimeoutException {
    DepositoDTO deposito = buscarDepositoPorID(depositoID);
    Deposito depositoPaquete = depositoR.findById(depositoID).orElseThrow(() -> new DepositoNoEncontradoException(depositoID));

    ConnectionFactory factory = new ConnectionFactory();
    Map<String, String> env = System.getenv();
    factory.setHost(env.get("A_QUEUE_HOST"));
    factory.setUsername(env.get("A_QUEUE_USERNAME"));
    factory.setPassword(env.get("A_QUEUE_PASSWORD"));
    factory.setVirtualHost(env.get("A_QUEUE_USERNAME"));

    if(depositoPaquete.getCapacidadMaxima() < cantidad){
        throw new DepositoSinEspacioException(depositoID);
    }

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    String queueName = env.get("A_QUEUE_NAME");

    Paquete paquete = new Paquete(
            null,
            donacionID,
            productoID,
            cantidad,
            depositoPaquete
    );
    Paquete paqueteGuardado = paqueteR.save(paquete);
      log.info(
              "Paquete guardado. id={}, productoId={}, donacionId={}, cantidad={}",
              paqueteGuardado.getId(),
              paqueteGuardado.getProductos(),
              paqueteGuardado.getDonacionID(),
              paqueteGuardado.getCantidad()
      );
    metricas.counter("paquetes.creados").increment();

    List<NecesidadMaterialDTO> necesidadesMaterial = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(paqueteGuardado.getProductos());

    if(necesidadesMaterial.isEmpty()){
      depositoPaquete.getStockActual().add(paqueteGuardado);
      depositoR.save(depositoPaquete);
      channel.close();
      connection.close();
      return deposito;
    }
    depositoPaquete.setCapacidadMaxima(depositoPaquete.getCapacidadMaxima() - paquete.getCantidad());

    AsignacionQueue mensaje = new AsignacionQueue(
            paqueteGuardado.getId(),
            depositoPaquete.getAlgoritmo()
    );

      log.info(
              "Enviando paquete a la pila. paqueteID={}, algoritmo={}",
              mensaje.paqueteID(),
              mensaje.algoritmo()
      );

    ObjectMapper mapper = new ObjectMapper();

    byte[] body = mapper.writeValueAsBytes(mensaje);

    String traceId = TraceContext.traceId();
    Map<String, Object> headers = new HashMap<>();
    if (traceId != null && !traceId.isBlank()) {
        headers.put("traceId", traceId);
    }
    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers(headers).build();

    channel.basicPublish("", queueName, props, body);

    metricas.counter("entrada.pila").increment();
    channel.close();
    connection.close();

    depositoR.save(depositoPaquete);
    return deposito;
  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new DepositoNoEncontradoException(depositoID));
    deposito.setAlgoritmo(tipoAlgoritmo);
    depositoR.save(deposito);
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
    EstadoAsginacionEnum estado = EstadoAsginacionEnum.ASIGNADA;
    Paquete paquete = paqueteR.findById(paqueteDTO.id()).orElseThrow(() -> new PaqueteNoEncontradoException(paqueteDTO.id()));
    Asignacion asignacion = asignacionR.findByPaqueteID(paquete.getId()).orElseThrow(() -> new AsignacionNoEncontradaException(paquete.getId()));
    NecesidadMaterialDTO necesidad = donadoresYEntidadesClient.obtenerNecesidad(asignacion.getNecesidadID()).getBody();
    String necesidadID = necesidad.id();
      if(Objects.equals(paqueteDTO.cantidad(), necesidad.cantidadObjetivo())) {
          log.info("Paquete asignado por completo");
      }
      else if (paqueteDTO.cantidad() > necesidad.cantidadObjetivo()){
          paquete.setCantidad(paquete.getCantidad() - necesidad.cantidadObjetivo());
          paqueteR.save(paquete);
          Deposito depositoSobrante = depositoR.findById(depositoID).orElseThrow(() -> new DepositoNoEncontradoException(depositoID));
          depositoR.save(depositoSobrante);
      }
      else {
          if (necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) {
              log.info("Paquete asignado por completo");
          } else {
              necesidad = necesidades.stream().filter(n -> !n.id().equals(necesidadID) && (n.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA || n.cantidadObjetivo() <= paquete.getCantidad()))
                      .findFirst().orElseThrow(NecesidadNoCompatible::new);
              asignacion.setNecesidadID(necesidad.id());
          }
      }
      asignacionR.save(asignacion);
      metricas.counter("asignaciones.creados").increment();

      //EVENTO

      ConnectionFactory factory = new ConnectionFactory();
      Map<String, String> env = System.getenv();
      factory.setHost(env.get("E_QUEUE_HOST"));
      factory.setUsername(env.get("E_QUEUE_USERNAME"));
      factory.setPassword(env.get("E_QUEUE_PASSWORD"));
      factory.setVirtualHost(env.get("E_QUEUE_USERNAME"));
      AsignacionDTO asignacionDTO = new AsignacionDTO(asignacion.getId(), asignacion.getPaqueteID(), asignacion.getNecesidadID(), asignacion.getFecha(), estado);
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
          AsignacionEvento descripcion = new AsignacionEvento(asignacionDTO, depositoID) ;
          NecesidadMaterialDTO necesidadAuxiliar = donadoresYEntidadesClient.obtenerNecesidad(necesidadID).getBody();
          EntidadBeneficaDTO entidad = donadoresYEntidadesClient.buscarEntidad(necesidadAuxiliar.entidadID()).getBody();
          EntidadDepositoEvento descripcionAuxiliar = new EntidadDepositoEvento(entidad, depositoID);
          EventoQueue evento = new EventoQueue(UUID.randomUUID().toString(), "AsignacionEvento", descripcion);
          EventoQueue eventoAuxiliar = new EventoQueue(UUID.randomUUID().toString(), "EntidadDepositoEvento", descripcionAuxiliar);
          ObjectMapper mapper = new ObjectMapper();
          byte[] body = mapper.writeValueAsBytes(evento);
          byte[] bodyAuxiliar = mapper.writeValueAsBytes(eventoAuxiliar);
          Map<String, Object> headers = new HashMap<>();
          AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers(headers).build();
          channel.basicPublish("", queueName, props, body);
          channel.basicPublish("", queueName, props, bodyAuxiliar);
          channel.close();
          connection.close();
      } catch (IOException e) {
          throw new RuntimeException(e);
      } catch (TimeoutException e) {
          throw new RuntimeException(e);
      }

      return new AsignacionDTO(asignacion.getId(), asignacion.getPaqueteID(), asignacion.getNecesidadID(), asignacion.getFecha(), estado);
  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    Paquete paquete = paqueteR.findById(paqueteDTO.id()).orElseThrow(() -> new PaqueteNoEncontradoException(paqueteDTO.id()));
    Asignacion asignacion = asignacionR.findByPaqueteID(paquete.getId()).orElseThrow(() -> new AsignacionNoEncontradaException(paqueteDTO.id()));

    AsignacionesHistorial asignacionesH = new AsignacionesHistorial(
            null,
            asignacion.getId(),
            asignacion.getEstado(),
            LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"))
    );

    if (asignacion.getEstado() == EstadoAsignacionEnum.COMPLETADA) {
      throw new AsignacionEntregadaException();
    }

    asignacionesHistorialR.save(asignacionesH);
    asignacion.setEstado(EstadoAsignacionEnum.COMPLETADA);
    asignacionR.save(asignacion);

    donadoresYEntidadesClient.satisfacerNecesidad(asignacion.getNecesidadID(), paqueteDTO.cantidad());
    donacionesClient.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);

    metricas.counter("entrega").increment();

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
          AsignacionDTO asignacionDTO = new AsignacionDTO(asignacion.getId(), asignacion.getPaqueteID(), asignacion.getNecesidadID(), asignacion.getFecha(), EstadoAsginacionEnum.COMPLETADA);
          AsignacionEvento descripcion = new AsignacionEvento(asignacionDTO, paquete.getDeposito().getId()) ;
          EventoQueue evento = new EventoQueue(UUID.randomUUID().toString(), "ModificarAsignacionEvento", descripcion);
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

  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) { }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) { }

}
