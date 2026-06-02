package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GiroSiiDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;
   // private Integer idGiroSii;
    private String codigoGiroSii;
    private String nombreGiroSii;
    private String afectoIvaSii;
    private String categoriaTributariaSii;
    private String disponibleInternetSii;
    private String subRubroSii;
    private String rubroSii;
}