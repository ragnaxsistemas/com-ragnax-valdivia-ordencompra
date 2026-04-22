package com.ragnax.valdivia.ordencompra.application.service.model.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        "usuario",
        "fechaCreacion",
        "nombreArchivo",
        "observacion",
})
public class JsonCartaUpload implements Serializable {

    private String usuario;
    private String fechaCreacion;
    private String nombreArchivo;
    private String observacion;

}
