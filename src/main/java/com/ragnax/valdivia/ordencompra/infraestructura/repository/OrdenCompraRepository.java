package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.application.service.model.ReporteGastoUnidadDto;
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
            "AND (:fechaInicio IS NULL OR CAST(oc.fechaCreacion AS date) >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR CAST(oc.fechaCreacion AS date) <= :fechaFin) " +
            // 🌟 NUEVO FILTRO: Rango de ID de Orden de Compra (Ambos deben ser obligatorios para aplicar)
            "AND ((:idMin IS NULL OR :idMax IS NULL) OR (oc.idOrdenCompra BETWEEN :idMin AND :idMax))")
    Page<OrdenCompra> buscarAvanzado(
            @Param("idStatus") Integer idStatus,
            @Param("rut") String rut,
            // @Param("nombreProv") String nombreProv,
            @Param("unidadId") Integer unidadId,
            @Param("codOrdenCompra") String codOrdenCompra,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idMin") Long idMin, // 🌟 Parámetro Rango Inferior
            @Param("idMax") Long idMax, // 🌟 Parámetro Rango Superior
            Pageable pageable
    );

    @Query(value = "SELECT " +
            "    oc.id_unidad AS codigoUnidad, " +
            "    dt.nombre_documento_tributario AS tipoDocumento, " +
            "    COUNT(oc.id_orden_compra) AS totalOrdenesEmitidas, " +
            "    SUM(oc.total_neto) AS inversionTotalNeta, " +
            "    SUM(oc.total) AS inversionTotalConImpuesto " +
            "FROM orden_compra oc " +
            "INNER JOIN documento_tributario dt ON oc.fk_id_documento_tributario = dt.id_documento_tributario " +
            "WHERE (:mesesAtras IS NULL OR TIMESTAMPDIFF(MONTH, oc.fecha_creacion, NOW()) = :mesesAtras) " +
            "GROUP BY oc.id_unidad, dt.nombre_documento_tributario " +
            "ORDER BY inversionTotalConImpuesto DESC",
            nativeQuery = true)
    List<ReporteGastoUnidadDto> obtenerReporteGastosPorUnidad(@Param("mesesAtras") Integer mesesAtras);
}
