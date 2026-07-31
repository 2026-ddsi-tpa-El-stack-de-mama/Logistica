package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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
      Deposito dep = crearDeposito(depositoDTO);
      dep = depositoR.save(dep);
      return new DepositoDTO(dep.getId(), dep.getAlgoritmo(), dep.getNombre(), dep.getDireccion(), dep.getCapacidadMaxima(), null);
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe el depósito"));
    return new DepositoDTO(
            deposito.getId(),
            deposito.getAlgoritmo(),
            deposito.getNombre(),
            deposito.getDireccion(),
            deposito.getCapacidadMaxima(),
            null
    );
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
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException {
    DepositoDTO deposito = buscarDepositoPorID(depositoID);
    Deposito depositoPaquete = crearDeposito(deposito);
    Paquete paquete = new Paquete(
            null,
            donacionID,
            productoID,
            cantidad,
            depositoPaquete
    );
    paquete = paqueteR.save(paquete);
    PaqueteDTO paqueteDTO = new PaqueteDTO(
            paquete.getId(),
            donacionID,
            productoID,
            cantidad
    );
    metricas.counter("paquetes.creados").increment();
      System.out.println(
              paqueteR.findById(paquete.getId())
                      .isPresent()
      );
    List<NecesidadMaterialDTO> necesidadesMaterial = donadoresYEntidadesClient.obtenerNecesidadesInsatisfechasDe(paquete.getProductos());
    if (paqueteDTO.cantidad() <= 0){
      throw new RuntimeException("No hay cantidad suficiente");
    }
    if(necesidadesMaterial.isEmpty()){
      throw new RuntimeException("No hay necesidades");
      //depositoPaquete.setStockActual((List<Paquete>) paquete);
    }
    else{
      ejecutarMatchmaking(depositoID, paqueteDTO, necesidadesMaterial);
    }

    //depositoPaquete.setCapacidadMaxima(depositoPaquete.getCapacidadMaxima() - 1);
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
    LocalDateTime tiempo = LocalDateTime.now();
    EstadoAsginacionEnum estado = EstadoAsginacionEnum.ASIGNADA;
    DepositoDTO deposito = buscarDepositoPorID(depositoID);

    NecesidadMaterialDTO necesidad;
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
    }
    String necesidadID = necesidad.id();
    Asignacion asignacion = new Asignacion(
            null,
            paqueteDTO.id(),
            necesidadID,
            tiempo,
            EstadoAsignacionEnum.ASIGNADA
    );
    asignacion = asignacionR.save(asignacion);
    AsignacionDTO asignacionDTO = new AsignacionDTO(
            asignacion.getId(),
            paqueteDTO.id(),
            necesidadID,
            tiempo,
            estado
    );

    metricas.counter("asignaciones.creados").increment();
    return asignacionDTO;
  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    if (paqueteDTO == null) {
      throw new RuntimeException("El paquete no puede tener id nulo");
    }

    Asignacion asignacion = asignacionR.findByPaqueteID(paqueteDTO.id()).orElseThrow(() -> new RuntimeException("No existe la asignación"));

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

  //Métodos auxiliares para pasar de DTO a Clase
  public Deposito crearDeposito(DepositoDTO depositoDTO){
    return new Deposito(
            depositoDTO.id(),
            depositoDTO.nombre(),
            depositoDTO.algoritmo(),
            depositoDTO.direccion(),
            depositoDTO.capacidadMaxima(),
            null
    );
  }
}
