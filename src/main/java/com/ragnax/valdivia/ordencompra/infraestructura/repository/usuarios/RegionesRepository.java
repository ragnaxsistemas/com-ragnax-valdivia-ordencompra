package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionesRepository extends JpaRepository<Regiones, Integer> {

    Optional<Regiones> findByCodigoRegion(String codigoRegion);
}
