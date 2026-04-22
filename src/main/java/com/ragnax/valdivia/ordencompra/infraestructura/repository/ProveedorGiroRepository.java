package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.Proveedor;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.ProveedorGiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorGiroRepository extends JpaRepository<ProveedorGiro, Integer> {

    List<ProveedorGiro> findByIdProveedor(Proveedor proveedor);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProveedorGiro pg WHERE pg.idProveedor = :proveedor")
    void deleteByIdProveedor(@Param("proveedor") Proveedor proveedor);


}
