package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemAngular implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;
    //[{"codigoProducto":"","descripcionProducto":"eeeee","cantidad":555,"valorProducto":666}]
    private String descripcionProducto;
    private Integer cantidad;
    private Long valorProducto;

}
