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

    @Value("${archivo.html.logoEscudo}")
    private String archivoHtmlLogoEscudo;

}