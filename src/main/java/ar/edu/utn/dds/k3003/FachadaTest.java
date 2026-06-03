package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.NecesidadMaterialDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.TipoNecesidadMaterialEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.logistica.*;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

public class FachadaTest {
    Fachada fachada = new Fachada();

    @Test
    public void testPropioAgregarDeposito() {
        PaqueteDTO paquete = new PaqueteDTO("paq1", "don1", "prod1", 5);
        DepositoDTO deposito = new DepositoDTO(
                null,
                TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE,
                "Depósito Sur",
                "Av Corrientes 888",
                500,
                List.of(paquete)
        );

        DepositoDTO resultado = fachada.agregarDeposito(deposito);

        Assertions.assertNotNull(resultado.id());
        Assertions.assertEquals(TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE, resultado.nombre());
    }

    @Test
    public void testPropioBuscarDepositoPorID() {
        PaqueteDTO paquete = new PaqueteDTO("paq2", "don2", "prod2", 9);
        DepositoDTO deposito = new DepositoDTO(
                null,
                TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE,
                "Depósito Norte",
                "Boulogne Sur 293",
                200,
                List.of(paquete)
        );

        DepositoDTO creado = fachada.agregarDeposito(deposito);
        DepositoDTO encontrado = fachada.buscarDepositoPorID(creado.id());

        Assertions.assertEquals(creado.id(), encontrado.id());
    }

    @Test
    public void testPropioBuscarAsignacionPorPaqueteID() {
        PaqueteDTO paquete = new PaqueteDTO("paq1", "don1", "prod1", 5);

        AsignacionDTO asignacion = new AsignacionDTO("1", "ent1", "nec1", LocalDateTime.of(2023, 10, 10, 12, 30), EstadoAsginacionEnum.ASIGNADA);
        DepositoDTO deposito = new DepositoDTO(
                null,
                TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE,
                "Depósito Sur",
                "Av Corrientes 888",
                500,
                List.of(paquete)
        );

        NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
                "nec1",
                "ent1",
                1,
                "necesidad 1",
                3,
                "prod1",
                TipoNecesidadMaterialEnum.EXTRAORDINARIA

        );

        fachada.ejecutarMatchmaking(deposito.id(), paquete, List.of(necesidad));

        AsignacionDTO asignacionBuscada = fachada.buscarAsignacionPorPaqueteID("paq1");

        Assertions.assertNotNull(asignacionBuscada);
        Assertions.assertEquals("paq1", asignacionBuscada.paqueteID());
    }

    @Test
    public void testPropioGestionarDonacion() {
        DepositoDTO deposito = new DepositoDTO(
                null,
                TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE,
                "Depósito Oeste",
                "Varela 788",
                100,
                null
        );

        DepositoDTO depo = fachada.agregarDeposito(deposito);

        Assertions.assertThrows(RuntimeException.class, () -> fachada.gestionarDonacion(
                depo.id(),
                "don1",
                "prod1",
                0
        ));
    }

    @Test
    public void testPropioEjecutarMatchmaking() {
        PaqueteDTO paquete = new PaqueteDTO("paq2", "don2", "prod2", 8);
        DepositoDTO deposito = new DepositoDTO(
                null,
                TipoAlgoritmoEnum.PRIORIDAD_POR_SCORE,
                "Depósito Oeste",
                "Varela 788",
                100,
                null
        );
        NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
                "nec2",
                "ent2",
                6,
                "necesidad 2",
                5,
                "prod2",
                TipoNecesidadMaterialEnum.EXTRAORDINARIA

        );
        AsignacionDTO asignacion = fachada.ejecutarMatchmaking(
                deposito.id(),
                paquete,
                List.of(necesidad)
        );

        Assertions.assertNotNull(asignacion.id());
        Assertions.assertEquals("paq2", asignacion.paqueteID());
    }

    @Test
    public void testPropioReportarEntrega() {
        Fachada fachada = new Fachada();

        PaqueteDTO paquete = new PaqueteDTO(
                "paqueteNuevo",
                "don1",
                "prod1",
                5
        );

        Assertions.assertThrows(RuntimeException.class, () -> fachada.reportarEntrega(paquete));
    }
}

