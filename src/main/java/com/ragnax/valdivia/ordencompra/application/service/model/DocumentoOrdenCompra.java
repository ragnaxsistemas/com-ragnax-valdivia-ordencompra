package com.ragnax.valdivia.ordencompra.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoOrdenCompra implements Serializable {

    private String codEstadoOc;

    private byte[] docByte;

}
