package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ProductoDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.Producto;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<ProductoDTO> listarActivos() {
        return productoRepository.findByActive(true).stream().map(this::toDTO).toList();
    }

    public ProductoDTO obtenerPorId(Integer id) {
        return productoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    public ProductoDTO obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigoProducto(codigo)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigo));
    }

    public String generarCodigoAutomatico(Integer ultimoId) {
        if (ultimoId == null) {
            return "PROD-001";
        }

        // Incrementamos el contador
        int nuevoCorrelativo = ultimoId + 1;

        // %03d significa: Un entero (d) con 3 dígitos de ancho, rellenando con ceros (0)
        return String.format("PROD-%03d", nuevoCorrelativo);
    }

    public ProductoDTO crear(ProductoDTO dto) {

        Pageable pageByidDesc = PageRequest.of(0, 1, Sort.by("idProducto").descending());

        Page<Producto> pageIdProducto = productoRepository.findAll(pageByidDesc);

        Integer idPais = (!pageIdProducto.isEmpty()) ? (Integer)  pageIdProducto.getContent().get(0).getIdProducto() + 1 : 1;

        dto.setCodigoProducto(generarCodigoAutomatico(idPais));
        dto.setActivo(true);
        return toDTO(productoRepository.save(toEntity(dto)));
    }

    public ProductoDTO actualizar(String codigo, ProductoDTO dto) {
        Producto entity = productoRepository.findByCodigoProducto(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + codigo));
        entity.setNombreProducto(dto.getNombreProducto());
        entity.setCodigoProducto(dto.getCodigoProducto());
        entity.setDescripcionProducto(dto.getDescripcionProducto());
        entity.setValorProducto(dto.getValorProducto());
        entity.setActive( dto.getActivo());
        return toDTO(productoRepository.save(entity));
    }

    private ProductoDTO toDTO(Producto p) {
        return ProductoDTO.builder()
                //.idProducto(p.getIdProducto())
                .nombreProducto(p.getNombreProducto())
                .codigoProducto(p.getCodigoProducto())
                .descripcionProducto(p.getDescripcionProducto())
                .valorProducto(p.getValorProducto())
                .activo(p.getActive() )
                .build();
    }

    private Producto toEntity(ProductoDTO dto) {
        return Producto.builder()
                .nombreProducto(dto.getNombreProducto())
                .codigoProducto(dto.getCodigoProducto())
                .descripcionProducto(dto.getDescripcionProducto())
                .valorProducto(dto.getValorProducto())
                .active(dto.getActivo())
                .build();
    }
}