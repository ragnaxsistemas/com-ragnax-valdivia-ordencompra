package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Integer> {

    Optional<OrdenCompra> findByCodigoOrdenCompra(String codigoOrdenCompra);

    @Query("SELECT oc FROM OrdenCompra oc " +
            "JOIN oc.proveedor p " +
            "WHERE (:idStatus IS NULL OR EXISTS (" +
            "    SELECT 1 FROM StatusOrdenCompra s " +
            "    WHERE s.ordenCompra = oc " +
            "    AND s.estadoOc.idEstadoOc = :idStatus " +
            "    AND s.fechaEvento = (SELECT MAX(s2.fechaEvento) FROM StatusOrdenCompra s2 WHERE s2.ordenCompra = oc)" +
            ")) " +
            "AND (:rut IS NULL OR REPLACE(REPLACE(p.rutProveedor, '.', ''), '-', '') LIKE CONCAT('%', REPLACE(REPLACE(:rut, '.', ''), '-', ''), '%')) " +
            //"AND (:nombreProv IS NULL OR UPPER(p.nombreProveedor) LIKE UPPER(CONCAT('%', :nombreProv, '%'))) " +
            "AND (:unidadId IS NULL OR oc.idUnidad = :unidadId) " +
            "AND (:codOrdenCompra IS NULL OR oc.codigoOrdenCompra LIKE CONCAT('%', :codOrdenCompra, '%')) " +
            "AND (:fecha IS NULL OR CAST(oc.fechaCreacion AS date) = :fecha)")
    Page<OrdenCompra> buscarAvanzado(
            @Param("idStatus") Integer idStatus,
            @Param("rut") String rut,
         //   @Param("nombreProv") String nombreProv,
            @Param("unidadId") Integer unidadId,
            @Param("codOrdenCompra") String codOrdenCompra,
            @Param("fecha") LocalDate fecha,
            Pageable pageable
    );


    /**
     * Trae todas las OC cuyo status MÁS RECIENTE tenga el idStatus indicado.
     * Usa EXISTS con subconsulta que filtra por fechaCambio máxima.
     ***/
    @Query(
            value = """
        SELECT oc FROM OrdenCompra oc
        LEFT JOIN FETCH oc.proveedor p
        LEFT JOIN FETCH oc.idUsuario u
        LEFT JOIN FETCH oc.idUnidad un
        LEFT JOIN FETCH oc.documentoTributario d
        WHERE (
            SELECT s.estadoOc.idEstadoOc 
            FROM StatusOrdenCompra s 
            WHERE s.ordenCompra = oc 
            ORDER BY s.idStatusOrdenCompra DESC 
            LIMIT 1
        ) = :idStatus
        ORDER BY oc.idOrdenCompra DESC
    """,
            countQuery = """
        SELECT COUNT(oc) FROM OrdenCompra oc
        WHERE (
            SELECT s.estadoOc.idEstadoOc 
            FROM StatusOrdenCompra s 
            WHERE s.ordenCompra = oc 
            ORDER BY s.idStatusOrdenCompra DESC 
            LIMIT 1
        ) = :idStatus
    """
    )
    Page<OrdenCompra> findByStatusActual(@Param("idStatus") Long idStatus, Pageable pageable);
}
    /***
    @Query("""
    SELECT COUNT(oc) FROM OrdenCompra oc
    WHERE EXISTS (
        SELECT 1 FROM StatusOrdenCompra s
        WHERE s.ordenCompra = oc
        AND s.estadoOc.idEstadoOc = :idStatus
        AND s.idStatusOrdenCompra = (
            SELECT MAX(s2.idStatusOrdenCompra)
            FROM StatusOrdenCompra s2
            WHERE s2.ordenCompra = oc
        )
    )
    """)
    Long contar(@Param("idStatus") Long idStatus);






}***/
