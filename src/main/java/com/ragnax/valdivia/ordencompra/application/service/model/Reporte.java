package com.ragnax.valdivia.ordencompra.application.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class Reporte implements Serializable {

    private String fechaGeneracion;
    private String folioProceso;
    private String foliosCreados;
    private String rutAnalizados;
    private String patentesAnalizadas;
    private String multasTotales;

    public Reporte(String fechaGeneracion, String folioProceso, String foliosCreados, String rutAnalizados, String patentesAnalizadas, String multasTotales) {
        this.fechaGeneracion = fechaGeneracion;
        this.folioProceso = folioProceso;
        this.foliosCreados = foliosCreados;
        this.rutAnalizados = rutAnalizados;
        this.patentesAnalizadas = patentesAnalizadas;
        this.multasTotales = multasTotales;
    }
}
