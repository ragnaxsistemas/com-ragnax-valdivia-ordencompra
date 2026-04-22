package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProveedorDTO {
    //private Integer idProveedor;
    private String rutProveedor;
    private String nombreProveedor;
    private String razonSocialProveedor;
    private String direccion;
    private List<GiroDTO> listaGiros;
    private String telefonoContactoProveedor;
    private String emailProveedor;
    private String codComuna;
    private Boolean activo;
}
