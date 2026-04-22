package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorGiroId implements Serializable {
    private Integer idProveedor; // Coincide con el nombre del atributo en la Entidad
    private Integer idGiro;      // Coincide con el nombre del atributo en la Entidad
}