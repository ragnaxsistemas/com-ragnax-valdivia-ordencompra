package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoOcDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;

    private String codigoEstadoOc;
    private String nombreEstadoOc;
    private String descripcion;

}
