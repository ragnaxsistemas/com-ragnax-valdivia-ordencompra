package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.application.service.model.ReporteGastoUnidadDto;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.AdjuntoOrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompraConfirmada;
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
public interface OrdenCompraConfirmadaRepository extends JpaRepository<OrdenCompraConfirmada, Integer> {

    Optional<OrdenCompraConfirmada> findByIdOrdenCompra(OrdenCompra idOrdenCompra);
}
