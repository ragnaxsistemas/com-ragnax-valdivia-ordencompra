package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoOcDTO {

    private String codigoEstadoOc;
    private String nombreEstadoOc;
    private String descripcion;

}
