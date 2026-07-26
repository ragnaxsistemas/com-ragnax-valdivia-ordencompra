package com.ragnax.valdivia.ordencompra.application.service.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReporteGastoUnidadDto implements Serializable {

    private Integer idUnidad;
    private String qAutorizadas;
    private Long qConfirmadas;
    private Long qAnuladas;
    private Long gastoTotalUnidad;
    
    
}
