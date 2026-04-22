package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComunasRepository extends JpaRepository<Comunas, Integer> {

    Optional<Comunas> findByCodigoComuna(String codigoComuna);
    List<Comunas> findByRegion(Regiones region);
}
