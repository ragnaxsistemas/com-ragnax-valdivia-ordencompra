package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import org.springframework.core.io.Resource;

public class InfoDescargaArchivoDTO {
    private final Resource recurso;
    private final String contentType;
    private final String nombreArchivo;
    private final String nginxInternalUrl; // Será null si estamos en local

    public InfoDescargaArchivoDTO(Resource recurso, String contentType, String nombreArchivo, String nginxInternalUrl) {
        this.recurso = recurso;
        this.contentType = contentType;
        this.nombreArchivo = nombreArchivo;
        this.nginxInternalUrl = nginxInternalUrl;
    }

    // Getters
    public Resource getRecurso() { return recurso; }
    public String getContentType() { return contentType; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getNginxInternalUrl() { return nginxInternalUrl; }

    public boolean esParaNginx() {
        return nginxInternalUrl != null && !nginxInternalUrl.isEmpty();
    }
}
