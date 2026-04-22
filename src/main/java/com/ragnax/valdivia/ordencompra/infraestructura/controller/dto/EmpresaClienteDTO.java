package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmpresaClienteDTO {

    private String codigoEmpresaCliente;
    private String rutEmpresaCliente;
    private String razonSocialEmpresaCliente;
    private String nombreEmpresaCliente;

}
