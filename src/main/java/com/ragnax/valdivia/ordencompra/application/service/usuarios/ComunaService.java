package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ComunaDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Regiones;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.ComunasRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.RegionesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ComunaService {

    @Autowired
    private ComunasRepository comunasRepository;

    @Autowired
    private RegionesRepository regionesRepository;

    @Transactional("usuariosTransactionManager")
    public List<ComunaDTO> listarTodas() {
        return comunasRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional("usuariosTransactionManager")
    public List<ComunaDTO> listarPorRegion(String codRegion) {

        Optional<Regiones> optRegion = regionesRepository.findByCodigoRegion(codRegion);

        return comunasRepository.findByRegion(optRegion.get())
                .stream().map(this::toDTO).toList();
    }

    @Transactional("usuariosTransactionManager")
    public ComunaDTO obtenerPorCodComuna(String codComuna) {
        return comunasRepository.findByCodigoComuna(codComuna)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada: " + codComuna));
    }

    public ComunaDTO crear(ComunaDTO dto) {
        return toDTO(comunasRepository.save(toEntity(dto)));
    }

    public ComunaDTO actualizar(Integer id, ComunaDTO dto) {
        Comunas entity = comunasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada: " + id));
        entity.setNombreComuna(dto.getNombreComuna());
        if (dto.getCodRegion() != null) {
            Regiones region = regionesRepository.findByCodigoRegion(dto.getCodRegion())
                    .orElseThrow(() -> new RuntimeException("Región no encontrada: " + dto.getCodRegion()));
            entity.setRegion(region);
        }
        return toDTO(comunasRepository.save(entity));
    }

    public void eliminar(Integer id) {
        comunasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada: " + id));
        comunasRepository.deleteById(id);
    }

    private ComunaDTO toDTO(Comunas c) {
        return ComunaDTO.builder()
                //.idComuna(c.getIdComuna())
                .codComuna(c.getCodigoComuna())
                .nombreComuna(c.getNombreComuna())
                .codRegion(c.getRegion() != null ? c.getRegion().getCodigoRegion() : null)
                .nombreRegion(c.getRegion() != null ? c.getRegion().getNombreRegion() : null)
                .build();
    }

    private Comunas toEntity(ComunaDTO dto) {
        Regiones region = regionesRepository.findByCodigoRegion(dto.getCodRegion())
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + dto.getCodRegion()));
        return Comunas.builder()
                .nombreComuna(dto.getNombreComuna())
                .region(region)
                .build();
    }
}
