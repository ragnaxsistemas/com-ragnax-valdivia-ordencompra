package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraRequest {

    private String codOc;
    private String usuarioExec;
    private String unidadExec;
    private PlantillaDTO plantillaDTO;


}
