package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.AdjuntoOrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdjuntoOrdenCompraRepository extends JpaRepository<AdjuntoOrdenCompra, Integer> {
    List<AdjuntoOrdenCompra> findByIdOrdenCompra(OrdenCompra idOrdenCompra);
    Optional<AdjuntoOrdenCompra> findByIdOrdenCompraAndNombreArchivoAndActiveTrue(OrdenCompra idOrdenCompra, String nombreArchivo);

}
