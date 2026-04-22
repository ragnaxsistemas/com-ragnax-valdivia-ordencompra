package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlantillaDTO {

    private String usernameUsuario;

    private String codUnidad;

    private String rutProveedor;

    private String codGiroSeleccionado;

    private String codDocumentoTributario;
    private String nombreDocumentoTributario;
    //OC
    private String codOrdenCompra;

    private String fechaOrdenCompra;

    private String codEstadoActualOc;
    private String estadoActualOc; //Nombre

    private String nombreOrdenCompra;

    private String observaciones;

    private String listProductosOrden;

    private Integer totalNeto;

    private Integer impuesto;

    private Integer total;

}
