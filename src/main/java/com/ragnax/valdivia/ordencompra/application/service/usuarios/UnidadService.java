package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ComunaDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.UnidadDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.ComunasRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.EmpresaClienteRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.RegionesRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UnidadService {

    @Autowired
    private UnidadRepository unidadRepository;

    @Autowired
    private EmpresaClienteRepository empresaClienteRepository;

    @Transactional("usuariosTransactionManager")
    public List<UnidadDTO> listarUnidadEmpresa(String codEmresa) {

        Optional<EmpresaCliente> optionalEmpresaCliente = empresaClienteRepository.findByCodigoEmpresaCliente(codEmresa);
        if(optionalEmpresaCliente.isPresent()) {
            return
                    unidadRepository.findByEmpresaCliente(EmpresaCliente.builder().
                            idEmpresaCliente(optionalEmpresaCliente.get().getIdEmpresaCliente()).build()).stream().map(this::toDTO).toList();
        }
        return Arrays.asList();
    }

    private UnidadDTO toDTO(Unidad c) {
        return UnidadDTO.builder().
                codigoUnidad(c.getCodigoUnidad()).
                nombreUnidad(c.getNombreUnidad()).
                showNombreUnidad(c.getShowNombreUnidad()).
                codEmpresa(c.getEmpresaCliente().getCodigoEmpresaCliente())
                .build();
    }
}
