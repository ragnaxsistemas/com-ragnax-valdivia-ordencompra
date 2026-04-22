package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.RegionDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.RegionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegionService {

    @Autowired
    private RegionesRepository regionesRepository;

    @Transactional("usuariosTransactionManager")
    public List<RegionDTO> listarTodas() {
        return regionesRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional("usuariosTransactionManager")
    public RegionDTO obtenerPorCodigo(String codRegion) {
        return regionesRepository.findByCodigoRegion(codRegion)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + codRegion));
    }

    private RegionDTO toDTO(Regiones r) {
        return RegionDTO.builder()
                .codigoRegion(r.getCodigoRegion())
                .nombreRegion(r.getNombreRegion())
                .build();
    }

    private Regiones toEntity(RegionDTO dto) {
        return Regiones.builder()
                .nombreRegion(dto.getNombreRegion())
                .build();
    }
}
