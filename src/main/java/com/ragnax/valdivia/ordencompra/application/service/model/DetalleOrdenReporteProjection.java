package com.ragnax.valdivia.ordencompra.application.service.model;

import java.time.LocalDateTime;

public class DetalleOrdenReporteProjection {
    Integer idUnidad;
    Integer idEstadoOc;
    String nombreEstadoOc;
    Integer idOrdenCompra;
    String codigoOrdenCompra;
    String nombreOrdenCompra;
    Integer total;
    LocalDateTime fechaCreacion;
}
