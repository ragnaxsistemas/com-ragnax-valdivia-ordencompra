package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComunaDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;
    //private Integer idComuna;
    private String codComuna;
    private String nombreComuna;
    private String codRegion;
    private String nombreRegion;
}
