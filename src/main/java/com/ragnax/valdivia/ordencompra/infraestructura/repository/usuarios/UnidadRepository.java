package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {

    Optional<Unidad> findByCodigoUnidad(String codigoUnidad);
    List<Unidad> findByEmpresaCliente(EmpresaCliente empresaCliente);

}
