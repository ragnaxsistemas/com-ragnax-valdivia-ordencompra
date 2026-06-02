package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GiroDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;

    private String codigoGiroSii;
    private String nombreGiroSii;

}
