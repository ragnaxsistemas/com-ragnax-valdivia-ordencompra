package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class PlantillaStatusDTO extends PlantillaDTO {

    //Usuarios
    private String apellidoUsuarioCreador;
    private String nombreUsuarioCreador;

    private String apellidoUsuarioSolicitante;
    private String nombreUsuarioSolicitante;

    private String apellidoUsuarioAutorizador;
    private String nombreUsuarioAutorizador;

    private String apellidoUsuarioAnulador;
    private String nombreUsuarioAnulador;

    private String apellidoUsuarioConfirmador;
    private String nombreUsuarioConfirmador;

    //Unidad
    private String nombreUnidad;

    //DTE
    private String descripcionDocumentoElectronico;
    private String impuestoDocumentoElectronico;

    //PROVEEDOR
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




}
