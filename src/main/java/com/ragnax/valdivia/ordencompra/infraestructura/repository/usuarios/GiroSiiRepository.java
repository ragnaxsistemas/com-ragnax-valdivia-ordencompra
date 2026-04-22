package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.GiroSii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiroSiiRepository extends JpaRepository<GiroSii, Integer> {

    Optional<GiroSii> findByCodigoGiroSii(String codigoGiroSii);
}
