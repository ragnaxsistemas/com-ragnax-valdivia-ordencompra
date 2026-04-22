package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.EstadoOcDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.EstadoOc;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.EstadoOcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstadoOcService {

    @Autowired
    private EstadoOcRepository estadoOcRepository;

    public List<EstadoOcDTO> listarTodos() {
        return estadoOcRepository.findAll().stream().map(this::toDTO).toList();
    }

    public EstadoOcDTO obtenerPorCodigo(String codigoEstadoOc) {
        return estadoOcRepository.findByCodigoEstadoOc
                (codigoEstadoOc)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Estado Oc no encontrado: " + codigoEstadoOc));
    }

    private EstadoOcDTO toDTO(EstadoOc estadoOc) {
        return EstadoOcDTO.builder()
                //.idDocumentoTributario(d.getIdDocumentoTributario())
                .nombreEstadoOc(estadoOc.getNombreEstadoOc())
                .codigoEstadoOc(estadoOc.getCodigoEstadoOc())
                .descripcion(estadoOc.getDescripcion())
                
                .build();
    }

    /***private DocumentoTributario toEntity(DocumentoTributarioDTO dto) {
        return DocumentoTributario.builder()
                .nombreDocumentoTributario(dto.getNombreDocumentoTributario())
                .codigoDocumentoTributario(dto.getCodigoDocumentoTributario())
                .descripcionDocumentoTributario(dto.getDescripcionDocumentoTributario())
                .impuesto(dto.getImpuesto())
                .active(dto.getActive())
                .build();
    }***/
}
