package com.ragnax.valdivia.ordencompra.infraestructura.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data @NoArgsConstructor @AllArgsConstructor
public class PlantillaStatusImpresionDTO extends PlantillaStatusDTO {


    private String usuarioAutorizador;
    private String usuarioAnulador;
    private String usuarioConfirmador;

    public PlantillaStatusImpresionDTO(PlantillaStatusDTO base) {
        BeanUtils.copyProperties(base, this);
    }
}
