package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonaciones;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaLogistica;
import ar.edu.utn.dds.k3003.model.Deposito;
import ar.edu.utn.dds.k3003.repositories.AsignacionRepository;
import ar.edu.utn.dds.k3003.repositories.DepositoRepository;
import ar.edu.utn.dds.k3003.repositories.PaqueteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.Double.compare;

@Service
public class Fachada implements FachadaLogistica {
  private FachadaDonadoresYEntidades fachadaDonadoresYEntidades;
  private FachadaDonaciones fachadaDonaciones;
  private final DepositoRepository depositoR;
  private final PaqueteRepository paqueteR;
  private final AsignacionRepository asignacionR;
  public Fachada(DepositoRepository depositoR, PaqueteRepository paqueteR, AsignacionRepository asignacionR) {

      this.depositoR = depositoR;
      this.paqueteR = paqueteR;
      this.asignacionR = asignacionR;
  }

  @Override
  public DepositoDTO agregarDeposito(DepositoDTO deposito) {
    int id = Math.toIntExact(depositoR.count() + 1);
    if (deposito.id() != null){
      throw new RuntimeException("Depósito inexistente");
    }
    else{
      DepositoDTO depDTO = new DepositoDTO(
              Integer.toString(id),
              null,
              deposito.nombre(),
              deposito.direccion(),
              deposito.capacidadMaxima(),
              null
      );
      Deposito dep = new Deposito(
              Integer.toString(id),
              deposito.nombre(),
              null,
              deposito.direccion(),
              deposito.capacidadMaxima(),
              null
      );
        depositoR.save(dep);
        return depDTO;
    }
  }

  @Override
  public DepositoDTO buscarDepositoPorID(String depositoID) throws NoSuchElementException {
    Deposito deposito = depositoR.findById(depositoID).orElseThrow(() -> new RuntimeException("No existe"));
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
    try{
      return asignacionR.findById(paqueteID);
    }
    catch(NullPointerException e){
      throw new RuntimeException("No existe la asignación");
    }
  }

  @Override
  public DepositoDTO gestionarDonacion(String depositoID, String donacionID, String productoID, Integer cantidad) throws NoSuchElementException {
    int id = Math.toIntExact(depositoR.count());
    DepositoDTO deposito = buscarDepositoPorID(depositoID);
    PaqueteDTO paquete = new PaqueteDTO(
            Integer.toString(id),
            donacionID,
            productoID,
            cantidad
    );
    List<NecesidadMaterialDTO> necesidadesMaterial = fachadaDonadoresYEntidades.obtenerNecesidadesInsatisfechasDe(productoID).stream()
            .filter(necesidad -> necesidad.tipo() == TipoNecesidadMaterialEnum.EXTRAORDINARIA|| paquete.cantidad() >= necesidad.cantidadObjetivo())
            .toList();

    if (paquete.cantidad() <= 0){
      throw new RuntimeException("No hay cantidad suficiente");
    }
    if(necesidadesMaterial.isEmpty()){
      throw new RuntimeException("No hay necesidades");
    }
    ejecutarMatchmaking(depositoID, paquete, necesidadesMaterial);

    return deposito;
  }

  @Override
  public void setAlgoritmoMM(String depositoID, TipoAlgoritmoEnum tipoAlgoritmo) {
    DepositoDTO deposito = buscarDepositoPorID(depositoID);
    DepositoDTO depositoNuevo = new DepositoDTO(
            deposito.id(),
            tipoAlgoritmo,
            deposito.nombre(),
            deposito.direccion(),
            deposito.capacidadMaxima(),
            deposito.stockActual()
    );
    depositoR.save(depositoNuevo);
  }
  @Override
  public AsignacionDTO ejecutarMatchmaking(String depositoID, PaqueteDTO paqueteDTO, List<NecesidadMaterialDTO> necesidades) {
    int id = Math.toIntExact(asignacionR.count() + 1);
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
    if (necesidadID == null) {
      necesidadID = "necesidad1";
    }
    AsignacionDTO asignacion = new AsignacionDTO(
            Integer.toString(id),
            paqueteDTO.id(),
            necesidadID,
            tiempo,
            estado
    );
   asignacionR.save(asignacion);
    return asignacion;
  }

  @Override
  public void reportarEntrega(PaqueteDTO paqueteDTO) {
    if (paqueteDTO == null) {
      throw new RuntimeException();
    }
    AsignacionDTO asignacion = buscarAsignacionPorPaqueteID(paqueteDTO.id());

    AsignacionDTO asignacionNueva = new AsignacionDTO(
            asignacion.id(),
            asignacion.paqueteID(),
            asignacion.necesidadID(),
            asignacion.fecha(),
            EstadoAsginacionEnum.COMPLETADA
    );

    asignacionR.save(asignacionNueva);

    fachadaDonadoresYEntidades.satisfacerNecesidad(asignacionNueva.necesidadID(), paqueteDTO.cantidad());
    fachadaDonaciones.cambiarEstadoDeDonacion(
            paqueteDTO.donacionID(),
            EstadoDonacionEnum.ACEPTADA
    );
  }

  @Override
  public void setFachadaDonadoresYEntidades(FachadaDonadoresYEntidades fachadaDonadoresYEntidades) {this.fachadaDonadoresYEntidades = fachadaDonadoresYEntidades;}

  @Override
  public void setFachadaDonaciones(FachadaDonaciones fachadaDonaciones) {
    this.fachadaDonaciones = fachadaDonaciones;
  }
}
