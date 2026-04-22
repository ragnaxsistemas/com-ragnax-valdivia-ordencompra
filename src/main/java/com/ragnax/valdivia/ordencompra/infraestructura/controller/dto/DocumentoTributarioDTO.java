package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoTributarioDTO {
    //private Integer idDocumentoTributario;
    private String nombreDocumentoTributario;
    private String codigoDocumentoTributario;
    private String descripcionDocumentoTributario;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal impuesto;
    private Boolean active;
}
