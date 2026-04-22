package com.ragnax.valdivia.ordencompra.application.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiRagnaxError {

    private String codigoImsbException;
    private String message;
}
