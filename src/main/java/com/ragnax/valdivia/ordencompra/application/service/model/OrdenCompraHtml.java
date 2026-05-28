package com.ragnax.valdivia.ordencompra.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraHtml implements Serializable {

    private String html;

    private List<String> listImagesBase64;

}
