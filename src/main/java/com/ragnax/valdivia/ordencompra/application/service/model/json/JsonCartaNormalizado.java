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

        "uploadJson",
        "uploadXlsx",

        "usuarioDesnormalizar",
        "adjuntoXlsx",
        "fechaCreacionDesnormalizado",
        "desnormalizadoJson",
        "desnormalizadoXlsx",
        "desnormalizadoCsv",

        "usuarioNormalizado",
        "fechaCreacionNormalizado",
        "normalizadoJson",
        "normalizadoXlsx"


})
public class JsonCartaNormalizado implements Serializable {

    private String usuario;
    private String fechaCreacion;
    private String nombreArchivo;
    private String observacion;

    private String uploadJson;
    private String uploadXlsx;
    private String usuarioDesnormalizar;
    private String adjuntoXlsx;
    private String fechaCreacionDesnormalizado;
    private String desnormalizadoJson;
    private String desnormalizadoXlsx;
    private String desnormalizadoCsv;

    private String usuarioNormalizado;
    private String fechaCreacionNormalizado;
    private String normalizadoJson;
    private String normalizadoXlsx;
}
