package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.AsignacionDTO;

public record AsignacionEvento(AsignacionDTO asignacionDTO, String depositoID) {
}
