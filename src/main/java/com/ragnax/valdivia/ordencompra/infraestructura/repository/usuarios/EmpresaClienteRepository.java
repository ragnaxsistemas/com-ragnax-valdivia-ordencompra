package com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaClienteRepository extends JpaRepository<EmpresaCliente, Integer> {

    Optional<EmpresaCliente> findByCodigoEmpresaCliente(String codigoEmpresaCliente);

}
