package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendienteAnulacionOrdenCompraRequest {

    private String codOc;
    private String usuarioPendienteAnulacion;
    private String unidadPendienteAnulacion;

}
