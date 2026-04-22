package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.DocumentoTributario;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    Optional<Proveedor> findByRutProveedor(String rutProveedor);
    List<Proveedor> findByActive(Boolean active);

}
