package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.EstadoOc;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.StatusOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusOrdenCompraRepository extends JpaRepository<StatusOrdenCompra, Integer> {

    // Devuelve el registro de status más reciente de una OC
    @Query("""
        SELECT s FROM StatusOrdenCompra s
        WHERE s.ordenCompra.idOrdenCompra = :idOC
        AND s.fechaEvento = (
            SELECT MAX(s2.fechaEvento)
            FROM StatusOrdenCompra s2
            WHERE s2.ordenCompra.idOrdenCompra = :idOC
        )
    """)
    Optional<StatusOrdenCompra> findStatusActual(@Param("idOC") Long idOC);

    Optional<StatusOrdenCompra> findByOrdenCompraAndEstadoOc(OrdenCompra ordenCompra,  EstadoOc estadoOc);
}
