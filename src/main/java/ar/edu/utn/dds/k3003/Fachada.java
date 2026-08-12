package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.clientes.DonacionesClient;
import ar.edu.utn.dds.k3003.clientes.DonadoresYEntidadesClient;
import ar.edu.utn.dds.k3003.model.*;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.AsignacionesHistorialRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static java.lang.Double.compare;

@Service
public class Fachada implements FachadaLogistica {
  private final DonacionesClient donacionesClient;
  private final DonadoresYEntidadesClient donadoresYEntidadesClient;
  private final DepositoRepository depositoR;
  private final PaqueteRepository paqueteR;
  private final AsignacionRepository asignacionR;
  private final AsignacionesHistorialRepository asignacionesHistorialR;
  private final MeterRegistry metricas;

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
      DepositoDTO depositoDTO = new DepositoDTO(null, null, deposito.nombre(), deposito.direccion(), deposito.capacidadMaxima(), null);
      Deposito dep = new Deposito(
              depositoDTO.id(),
              depositoDTO.nombre(),
              depositoDTO.algoritmo(),
              depositoDTO.direccion(),
              depositoDTO.capacidadMaxima(),
              null
      );
      dep = depositoR.save(dep);
      return new DepositoDTO(dep.getId(), dep.getAlgoritmo(), dep.getNombre(), dep.getDireccion(), dep.getCapacidadMaxima(), null);
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe el depósito"));
    List<PaqueteDTO> stock = deposito.getStockActual().stream().map(p -> new PaqueteDTO(
                      p.getId(),
                      p.getDonacionID(),
                      p.getProductos(),
                      p.getCantidad())).toList();
      return new DepositoDTO(deposito.getId(), deposito.getAlgoritmo(), deposito.getNombre(), deposito.getDireccion(), deposito.getCapacidadMaxima(), stock);
  }

  @Override
  public AsignacionDTO buscarAsignacionPorPaqueteID(String paqueteID) throws NoSuchElementException {
      Asignacion asignacion = asignacionR.findByPaqueteID(paqueteID).orElseThrow(() -> new RuntimeException("No existe la asignación"));
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
    Deposito depositoPaquete = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe el depósito"));

    ConnectionFactory factory = new ConnectionFactory();
    Map<String, String> env = System.getenv();
    factory.setHost(env.get("QUEUE_HOST"));
    factory.setUsername(env.get("QUEUE_USERNAME"));
    factory.setPassword(env.get("QUEUE_PASSWORD"));
    factory.setVirtualHost(env.get("QUEUE_USERNAME"));

    Connection connection = factory.newConnection();

    Channel channel = connection.createChannel();
    String queueName = env.get("QUEUE_NAME");

    if(depositoPaquete.getCapacidadMaxima() < cantidad){
        throw new RuntimeException("No hay espacio en el depósito");
    }

    Paquete paquete = new Paquete(
            null,
            donacionID,
            productoID,
            cantidad,
            depositoPaquete
    );
    Paquete paqueteGuardado = paqueteR.save(paquete);
    metricas.counter("paquetes.creados").increment();

    List<NecesidadMaterialDTO> necesidadesMaterial = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(paqueteGuardado.getProductos());

    if(necesidadesMaterial.isEmpty()){
      depositoPaquete.getStockActual().add(paqueteGuardado);
      depositoR.save(depositoPaquete);
      return deposito;
    }

    AsignacionQueue mensaje = new AsignacionQueue(
            paqueteGuardado.getId(),
            depositoPaquete.getAlgoritmo()
    );

    ObjectMapper mapper = new ObjectMapper();

    byte[] body = mapper.writeValueAsBytes(mensaje);

    channel.basicPublish("", queueName, null, body);
    ejecutarMatchmaking(depositoID, new PaqueteDTO(paqueteGuardado.getId(), donacionID, productoID, cantidad), necesidadesMaterial);
    depositoPaquete.setCapacidadMaxima(depositoPaquete.getCapacidadMaxima() - paquete.getCantidad());

    channel.close();
    connection.close();


      return deposito;
  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe el depósito"));

    deposito.setAlgoritmo(tipoAlgoritmo);

    depositoR.save(deposito);
  }

  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
    //LocalDateTime tiempo = LocalDateTime.now();
    EstadoAsginacionEnum estado = EstadoAsginacionEnum.ASIGNADA;
    DepositoDTO deposito = buscarDepositoPorID(depositoID);
    Paquete paquete = paqueteR.getReferenceById(paqueteDTO.id());
    Asignacion asignacion = asignacionR.findByPaqueteID(paqueteDTO.id()).orElseThrow(() -> new RuntimeException("No existe la asignación"));
    NecesidadMaterialDTO necesidad = donadoresYEntidadesClient.obtenerNecesidad(asignacion.getNecesidadID()).getBody();
    /*
    if (deposito.algoritmo() == TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE) {
      necesidad = necesidades.stream().max((n1, n2) -> {
                double score1 = n1.nivelDeUrgencia() / ((double) paqueteDTO.cantidad() / n1.cantidadObjetivo());
                double score2 = n2.nivelDeUrgencia() / ((double) paqueteDTO.cantidad() / n2.cantidadObjetivo());
                return compare(score1, score2);
              }).orElseThrow(RuntimeException::new);
    } else {
      necesidad = necesidades.stream().max((n1, n2) -> {
                int d1 = n1.cantidadObjetivo() - paqueteDTO.cantidad();
                int d2 = n2.cantidadObjetivo() - paqueteDTO.cantidad();
                return compare(d1, d2);
              }).orElseThrow(RuntimeException::new);
    }*/

    String necesidadID = necesidad.id();
      if(Objects.equals(paqueteDTO.cantidad(), necesidad.cantidadObjetivo())) {
          System.out.println("Se asignó por completo el paquete");
      }
      else if (paqueteDTO.cantidad() > necesidad.cantidadObjetivo()){
          paquete.setCantidad(paqueteDTO.cantidad() - necesidad.cantidadObjetivo());
          paqueteR.save(paquete);
          Deposito depositoSobrante = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe el depósito"));
          depositoR.save(depositoSobrante);
      }
      else {
          if (necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA) {
              System.out.println("Se asignó por completo el paquete");
          } else {
              necesidad = necesidades.stream().filter(n -> !n.id().equals(necesidadID) && (n.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA || n.cantidadObjetivo() <= paqueteDTO.cantidad()))
                      .findFirst().orElseThrow(() -> new RuntimeException("No hay otra necesidad compatible"));
          }
      }
      metricas.counter("asignaciones.creados").increment();
      return new AsignacionDTO(asignacion.getId(), asignacion.getPaqueteID(), asignacion.getNecesidadID(), asignacion.getFecha(), estado);
  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    Paquete paquete = paqueteR.findById(paqueteDTO.id()).orElseThrow(() -> new RuntimeException("No existe el paquete"));
    Asignacion asignacion = asignacionR.findByPaqueteID(paquete.getId()).orElseThrow(() -> new RuntimeException("No existe la asignación"));

    AsignacionesHistorial asignacionesH = new AsignacionesHistorial(
            null,
            asignacion.getId(),
            asignacion.getEstado(),
            LocalDateTime.now()
    );
    asignacionesHistorialR.save(asignacionesH);

    if (asignacion.getEstado() == EstadoAsignacionEnum.COMPLETADA) {
      throw new RuntimeException("La asignación ya fue entregada");
    }
    else{
      asignacion.setEstado(EstadoAsignacionEnum.COMPLETADA);
    }
    asignacionR.save(asignacion);


    donadoresYEntidadesClient.satisfacerNecesidad(asignacion.getNecesidadID(), paqueteDTO.cantidad());
    donacionesClient.cambiarEstadoDeDonacion(paqueteDTO.donacionID(), EstadoDonacionEnum.ACEPTADA);
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) { }

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) { }

}
