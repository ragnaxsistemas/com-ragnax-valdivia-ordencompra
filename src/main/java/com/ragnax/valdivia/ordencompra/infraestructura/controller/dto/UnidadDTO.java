package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UnidadDTO {

    private String codigoUnidad;
    private String nombreUnidad;
    private String showNombreUnidad;
    private String codEmpresa;

}
