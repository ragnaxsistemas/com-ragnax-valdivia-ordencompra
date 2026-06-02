package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoTributarioDTO implements Serializable {
    private static final long serialVersionUID = -1098427707835311622L;
    //private Integer idDocumentoTributario;
    private String nombreDocumentoTributario;
    private String codigoDocumentoTributario;
    private String descripcionDocumentoTributario;
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal impuesto;
    private Boolean active;
}
