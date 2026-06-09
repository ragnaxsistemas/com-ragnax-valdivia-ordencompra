package com.ragnax.valdivia.ordencompra.application.service.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReporteGastoUnidadDto implements Serializable {

    private Integer codigoUnidad;
    private String tipoDocumento;
    private Long totalOrdenesEmitidas;
    private Long inversionTotalNeta;
    private Long inversionTotalConImpuesto;
    
    
}
