package ar.edu.utn.dds.k3003.queue;

import ar.edu.utn.dds.k3003.catedra.dtos.logistica.TipoAlgoritmoEnum;

public record AsignacionQueue(
        String paqueteID,
        TipoAlgoritmoEnum algoritmo
){}