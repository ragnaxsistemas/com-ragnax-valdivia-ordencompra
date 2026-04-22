package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.DocumentoTributarioDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.DocumentoTributario;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.DocumentoTributarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentoTributarioService {

    @Autowired
    private DocumentoTributarioRepository documentoTributarioRepository;

    public List<DocumentoTributarioDTO> listarTodos() {
        return documentoTributarioRepository.findAll().stream().map(this::toDTO).toList();
    }

    public DocumentoTributarioDTO obtenerPorId(Integer id) {
        return documentoTributarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Documento tributario no encontrado: " + id));
    }

    public DocumentoTributarioDTO obtenerPorCodigo(String codDocumentoElectronico) {
        return documentoTributarioRepository.findByCodigoDocumentoTributario
                (codDocumentoElectronico)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Documento tributario no encontrado: " + codDocumentoElectronico));
    }

    private DocumentoTributarioDTO toDTO(DocumentoTributario d) {
        return DocumentoTributarioDTO.builder()
                //.idDocumentoTributario(d.getIdDocumentoTributario())
                .nombreDocumentoTributario(d.getNombreDocumentoTributario())
                .codigoDocumentoTributario(d.getCodigoDocumentoTributario())
                .descripcionDocumentoTributario(d.getDescripcionDocumentoTributario())
                .impuesto(d.getImpuesto())
                .active(d.getActive())
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
