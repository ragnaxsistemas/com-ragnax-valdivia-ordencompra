package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PlantillaStatusDTO extends PlantillaDTO {

    private String apellidoUsuario;
    private String nombreUsuario;

    private String nombreUnidad;

    //private String rutProveedor;
    private String nombreProveedor;
    private String razonSocialProveedor;
    private String direccionProveedor;
    private String giroProveedor;
    private String telefonoContactoProveedor;
    private String emailProveedor;
    private String codRegionProveedor;
    private String codComunaProveedor;
    private String nombreRegionProveedor;
    private String nombreComunaProveedor;

    //private Integer idDocumentoElectronico;
    //private String nombreDocumentoElectronico;
    private String descripcionDocumentoElectronico;
    private String impuestoDocumentoElectronico;


}
