package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GiroSiiDTO {
   // private Integer idGiroSii;
    private String codigoGiroSii;
    private String nombreGiroSii;
    private String afectoIvaSii;
    private String categoriaTributariaSii;
    private String disponibleInternetSii;
    private String subRubroSii;
    private String rubroSii;
}