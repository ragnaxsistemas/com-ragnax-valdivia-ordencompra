package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.EmpresaClienteDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.UnidadDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.EmpresaCliente;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.EmpresaClienteRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaClienteService {

    @Autowired
    private EmpresaClienteRepository empresaClienteRepository;


    @Transactional("usuariosTransactionManager")
    public EmpresaClienteDTO buscarEmpresa(String codigoEmpresaCliente) {
        return
                toDTO(empresaClienteRepository.findByCodigoEmpresaCliente
                        (codigoEmpresaCliente).get());
    }


    private EmpresaClienteDTO toDTO(EmpresaCliente c) {
        if (c == null) {
            return null;
        }
        return EmpresaClienteDTO.builder().
                    codigoEmpresaCliente(c.getCodigoEmpresaCliente()).
                    rutEmpresaCliente(c.getRutEmpresaCliente()).
                    razonSocialEmpresaCliente(c.getNombreRazonSocialEmpresaCliente()).
                    nombreEmpresaCliente(c.getNombreEmpresaCliente())
                .build();
    }
}
