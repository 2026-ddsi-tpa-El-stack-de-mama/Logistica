package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

import java.util.List;

public record DepositoEvento(String id, Integer ocupacion, TipoAlgoritmoEnum algoritmo, List<AsignacionDTO> asignaciones) {
}
