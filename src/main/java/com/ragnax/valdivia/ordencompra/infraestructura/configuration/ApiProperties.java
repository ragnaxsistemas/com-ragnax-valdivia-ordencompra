package com.ragnax.valdivia.ordencompra.infraestructura.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
/****Properties que pueden cambiar el valor ****/
public class ApiProperties {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${archivo.carpeta.public}")
    private String archivoCarpetaPublic;

    @Value("${archivo.html.nombreCarpetaTemplate}")
    private String archivoHtmlNombreCarpetaTemplate;

    @Value("${archivo.html.nombreHtmlConfirmada}")
    private String archivoHtmlNombreHtmlConfirmada;

    @Value("${archivo.html.nombreHtmlAnulada}")
    private String archivoHtmlNombreHtmlAnulada;

    @Value("${archivo.html.logoEscudoBl}")
    private String archivoHtmlLogoEscudoBl;

    @Value("${archivo.html.logoEscudoColor}")
    private String archivoHtmlLogoEscudoColor;

    @Value("${archivo.html.logoEscudoNegro}")
    private String archivoHtmlLogoEscudoNegro;

    @Value("${archivo.html.logoTimbreContabilidad}")
    private String archivoHtmlLogoTimbreContabilidad;

    @Value("${archivo.html.logoTimbreGerencia}")
    private String archivoHtmlLogoTimbreGerencia;

}