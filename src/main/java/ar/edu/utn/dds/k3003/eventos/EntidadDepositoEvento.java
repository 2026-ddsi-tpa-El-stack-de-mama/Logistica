package ar.edu.utn.dds.k3003.eventos;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.EntidadBeneficaDTO;

public record EntidadDepositoEvento(EntidadBeneficaDTO entidad, String depositoID) {
}
