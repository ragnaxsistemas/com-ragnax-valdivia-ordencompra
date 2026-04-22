package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.EstadoOc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EstadoOcRepository extends JpaRepository<EstadoOc, Integer> {

    Optional<EstadoOc> findByCodigoEstadoOc(String nombreEstadoOc);
}
