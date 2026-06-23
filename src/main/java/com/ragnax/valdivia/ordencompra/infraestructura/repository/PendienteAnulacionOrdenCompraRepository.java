package com.ragnax.valdivia.ordencompra.infraestructura.repository;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.EstadoOc;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.PendienteAnulacionOrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.StatusOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendienteAnulacionOrdenCompraRepository extends JpaRepository<PendienteAnulacionOrdenCompra, Integer> {

    List<PendienteAnulacionOrdenCompra> findByOrdenCompra(OrdenCompra oc);;

}
