package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComunaDTO {
    //private Integer idComuna;
    private String codComuna;
    private String nombreComuna;
    private String codRegion;
    private String nombreRegion;
}
