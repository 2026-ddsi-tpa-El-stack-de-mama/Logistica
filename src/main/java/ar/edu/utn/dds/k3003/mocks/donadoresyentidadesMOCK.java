package ar.edu.utn.dds.k3003.mocks;

import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.*;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaDonadoresYEntidades;
import ar.edu.utn.dds.k3003.catedra.fachadas.FachadaIncentivos;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class donadoresyentidadesMOCK implements FachadaDonadoresYEntidades {

    @Override
    public DonadorDTO agregarDonador(DonadorDTO donadorDTO) {
        return null;
    }

    @Override
    public DonadorDTO buscarDonadorPorID(String donadorID) throws NoSuchElementException {
        return null;
    }

    @Override
    public EntidadBeneficaDTO agregarEntidad(EntidadBeneficaDTO entidadBeneficaDTO) {
        return null;
    }

    @Override
    public EntidadBeneficaDTO buscarEntidadPorID(String entidadID) throws NoSuchElementException {
        return null;
    }

    @Override
    public NecesidadMaterialDTO registrarNecesidad(NecesidadMaterialDTO necesidadMaterialDTO) {
        return null;
    }

    @Override
    public QuejaDTO agregarQueja(QuejaDTO quejaDTO) throws NoSuchElementException {
        return null;
    }

    @Override
    public Boolean puedeDonar(String donadorID) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<QuejaDTO> obtenerQuejasDe(String donadorID) throws NoSuchElementException {
        return List.of();
    }

    @Override
    public DonadorDTO modificarEstado(String donadorID, EstadoDonadorEnum estado) throws NoSuchElementException {
        return null;
    }

    @Override
    public DonadorDTO modifcarCategoria(String donadorID, String categoria) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<NecesidadMaterialDTO> obtenerNecesidadesInsatisfechasDe(String productoSolicitadoID) {
        NecesidadMaterialDTO necesidad = new NecesidadMaterialDTO(
                "1",
                "entidad1",
                10,
                "sin descripción",
                10,
                productoSolicitadoID,
                TipoNecesidadMaterialEnum.EXTRAORDINARIA
        );

        return List.of(necesidad);
    }

    @Override
    public void satisfacerNecesidad(String necesidadID, Integer cantidad) {
        System.out.println("Necesidad satisfecha");
    }

    @Override
    public DonadorStatsDTO estadisticasDonador(String donadorID) {
        return null;
    }

    @Override
    public void setFachadaIncentivos(FachadaIncentivos fachadaIncentivos) {

    }
}
