package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ComunaDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.GiroSiiDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.GiroSii;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.ComunasRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.GiroSiiRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.RegionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GiroService {


    @Autowired
    private GiroSiiRepository giroSiiRepository;

    @Transactional("usuariosTransactionManager")
    public List<GiroSiiDTO> listarTodos() {
        return giroSiiRepository.findAll().stream().map(this::toDTO).toList();
    }


    private GiroSiiDTO toDTO(GiroSii c) {
        return GiroSiiDTO.builder().
        //idGiroSii(c.getIdGiroSii()).
        codigoGiroSii(c.getCodigoGiroSii()).
        nombreGiroSii(c.getNombreGiroSii()).
        afectoIvaSii(c.getAfectoIvaSii()).
        categoriaTributariaSii(c.getCategoriaTributariaSii()).
        disponibleInternetSii(c.getDisponibleInternetSii()).
        subRubroSii(c.getSubRubroSii()).
        rubroSii(c.getRubroSii()).build();

    }


}
