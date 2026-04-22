package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoDTO {
    //private Integer idProducto;
    private String nombreProducto;
    private String codigoProducto;
    private String descripcionProducto;
    private Integer valorProducto;
    private Boolean activo;
}
