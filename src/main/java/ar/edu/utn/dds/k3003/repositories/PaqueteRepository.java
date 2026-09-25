package ar.edu.utn.dds.k3003.repositories;


import ar.edu.utn.dds.k3003.model.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, String> {
    List<Paquete> findByProductos(String productoID);
}
