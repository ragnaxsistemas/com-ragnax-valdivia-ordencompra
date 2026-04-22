package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.GiroDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ProveedorDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.Proveedor;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.ProveedorGiro;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.GiroSii;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.ProveedorGiroRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.ProveedorRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.ComunasRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.GiroSiiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private GiroSiiRepository giroSiiRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProveedorGiroRepository proveedorGiroRepository;

    @Autowired
    private ComunasRepository comunasRepository;

    public List<ProveedorDTO> listarTodos() {
        // 1. Cargamos los datos necesarios de la BD
        List<Proveedor> proveedores = proveedorRepository.findAll();
        List<ProveedorGiro> todosLosGirosRelacion = proveedorGiroRepository.findAll();

        // 2. Cargamos TODOS los Giros SII en un Map para búsqueda rápida (ID -> Objeto)
        // Esto es lo que pediste: llamar a GiroSiiRepository y mantenerlos listos
        Map<Integer, GiroSii> mapaGirosSii = giroSiiRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GiroSii::getIdGiroSii, g -> g));

        // 3. Agrupamos la relación Proveedor-Giro por el ID del proveedor
        Map<Integer, List<ProveedorGiro>> girosPorProveedor = todosLosGirosRelacion.stream()
                .collect(Collectors.groupingBy(pg -> pg.getIdProveedor().getIdProveedor()));

        // 4. Mapeamos a DTO
        return proveedores.stream()
                .map(p -> {
                    List<ProveedorGiro> relaciones = girosPorProveedor.getOrDefault(p.getIdProveedor(), List.of());
                    return toDTO(p, relaciones, mapaGirosSii);
                })
                .toList();
    }

    public List<ProveedorDTO> listarActivos() {
        List<Proveedor> proveedores = proveedorRepository.findByActive(true);

        List<ProveedorGiro> todosLosGirosRelacion = proveedorGiroRepository.findAll();

        // 2. Cargamos TODOS los Giros SII en un Map para búsqueda rápida (ID -> Objeto)
        // Esto es lo que pediste: llamar a GiroSiiRepository y mantenerlos listos
        Map<Integer, GiroSii> mapaGirosSii = giroSiiRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GiroSii::getIdGiroSii, g -> g));

        // 3. Agrupamos la relación Proveedor-Giro por el ID del proveedor
        Map<Integer, List<ProveedorGiro>> girosPorProveedor = todosLosGirosRelacion.stream()
                .collect(Collectors.groupingBy(pg -> pg.getIdProveedor().getIdProveedor()));

        // 4. Mapeamos a DTO
        return proveedores.stream()
                .map(p -> {
                    List<ProveedorGiro> relaciones = girosPorProveedor.getOrDefault(p.getIdProveedor(), List.of());
                    return toDTO(p, relaciones, mapaGirosSii);
                })
                .toList();
    }

    public ProveedorDTO obtenerPorRut(String rut_limpio) {
        //El rut se va a limpiar si o si
        Optional<Proveedor> optProv  = proveedorRepository.findByRutProveedor(Utilidades.formatearRut(rut_limpio));

        List<ProveedorGiro> todosLosGirosRelacion = proveedorGiroRepository.findByIdProveedor(optProv.get());

        // 2. Cargamos TODOS los Giros SII en un Map para búsqueda rápida (ID -> Objeto)
        // Esto es lo que pediste: llamar a GiroSiiRepository y mantenerlos listos
        Map<Integer, GiroSii> mapaGirosSii = giroSiiRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GiroSii::getIdGiroSii, g -> g));

        // 3. Agrupamos la relación Proveedor-Giro por el ID del proveedor
        Map<Integer, List<ProveedorGiro>> girosPorProveedor = todosLosGirosRelacion.stream()
                .collect(Collectors.groupingBy(pg -> pg.getIdProveedor().getIdProveedor()));

        // 4. Mapeamos a DTO
        return Arrays.asList(optProv.get()).stream()
                .map(p -> {
                    List<ProveedorGiro> relaciones = girosPorProveedor.getOrDefault(p.getIdProveedor(), List.of());
                    return toDTO(p, relaciones, mapaGirosSii);
                })
                .toList().get(0);
    }

    public ProveedorDTO crear(ProveedorDTO dto) {

        Optional<Proveedor> optProv  = proveedorRepository.findByRutProveedor(Utilidades.formatearRut(dto.getRutProveedor()));

        if(optProv.isPresent()) {
            throw new RuntimeException("ya existe Proveedor con rut: " + dto.getRutProveedor());
        }
        Proveedor proveedorGuardado = proveedorRepository.save(toEntity(dto));

        // 2. Gestionar los Giros en la tabla intermedia
        if (dto.getListaGiros() != null && !dto.getListaGiros().isEmpty()) {

            List<ProveedorGiro> nuevosGiros = dto.getListaGiros().stream()
                    .map(item -> {
                        // Buscamos el giro por código
                        GiroSii giroSii = giroSiiRepository.findByCodigoGiroSii(item.getCodigoGiroSii())
                                .orElseThrow(() -> new RuntimeException("Giro con código " + item.getCodigoGiroSii() + " no encontrado"));

                        // Construimos la relación
                        return ProveedorGiro.builder()
                                .idProveedor(proveedorGuardado)
                                .idGiro(giroSii.getIdGiroSii())
                                .build();
                    })
                    .toList();

            proveedorGiroRepository.saveAll(nuevosGiros);
        }

        // 3. Recuperar los datos frescos para el mapa de Giros SII (como en el listar)
        Map<Integer, GiroSii> mapaGirosSii = giroSiiRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GiroSii::getIdGiroSii, g -> g));

        // 4. Retornar el DTO completo
        List<ProveedorGiro> girosRelacion = proveedorGiroRepository.findByIdProveedor(proveedorGuardado);
        return toDTO(proveedorGuardado, girosRelacion, mapaGirosSii);
    }

    @Transactional // Importante: toda la actualización debe ser una transacción
    public ProveedorDTO actualizar(String rut_limpio, ProveedorDTO dto) {
        // 1. Buscar el proveedor existente
        Proveedor entity = proveedorRepository.findByRutProveedor(Utilidades.formatearRut(rut_limpio))
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + rut_limpio));

        // 2. Actualizar campos básicos
        entity.setNombreProveedor(dto.getNombreProveedor());
        entity.setRazonSocialProveedor(dto.getRazonSocialProveedor());
        entity.setDireccion(dto.getDireccion());
        entity.setTelefonoContactoProveedor(dto.getTelefonoContactoProveedor());
        entity.setEmailProveedor(dto.getEmailProveedor());
        entity.setActive(dto.getActivo());

        if (dto.getCodComuna() != null) {
            Comunas comuna = comunasRepository.findByCodigoComuna(dto.getCodComuna())
                    .orElseThrow(() -> new RuntimeException("Comuna no encontrado: " + dto.getCodComuna()));

            entity.setIdComuna(comuna.getIdComuna());
        }

        // 3. Guardar cambios en el Proveedor
        Proveedor proveedorGuardado = proveedorRepository.save(entity);

        // 4. LIMPIEZA Y CARGA DE GIROS
        // Borramos los registros previos en la tabla intermedia
        proveedorGiroRepository.deleteByIdProveedor(proveedorGuardado);

        if (dto.getListaGiros() != null && !dto.getListaGiros().isEmpty()) {
            List<ProveedorGiro> nuevosGiros = dto.getListaGiros().stream()
                    .map(item -> {
                        // Buscamos la entidad GiroSii para obtener su ID real
                        GiroSii giroSii = giroSiiRepository.findByCodigoGiroSii(item.getCodigoGiroSii())
                                .orElseThrow(() -> new RuntimeException("Código " + item.getCodigoGiroSii() + " no existe"));

                        return ProveedorGiro.builder()
                                .idProveedor(proveedorGuardado)
                                .idGiro(giroSii.getIdGiroSii())
                                .build();
                    })
                    .collect(Collectors.toList());

            proveedorGiroRepository.saveAll(nuevosGiros);
        }

        // 5. Preparar datos para el retorno
        // Sugerencia: Solo buscar los giros necesarios, no todos los de la BD para el mapa
        Map<Integer, GiroSii> mapaGirosSii = giroSiiRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GiroSii::getIdGiroSii, g -> g));

        List<ProveedorGiro> girosRelacion = proveedorGiroRepository.findByIdProveedor(proveedorGuardado);

        return toDTO(proveedorGuardado, girosRelacion, mapaGirosSii);
    }

    private ProveedorDTO toDTO(Proveedor p, List<ProveedorGiro> relaciones, Map<Integer, GiroSii> mapaGirosSii) {

        // 1. Mapeo de giros con Stream de Java 17
        List<GiroDTO> listaGirosValues = relaciones.stream()
                .map(rel -> {
                    // Obtenemos el ID del giro desde la relación (ajustar si el método se llama distinto)
                    Integer idGiroBuscado = rel.getIdGiro();
                    GiroSii detalleGiro = mapaGirosSii.get(idGiroBuscado);

                    GiroDTO item = new GiroDTO();
                    if (detalleGiro != null) {
                        //item.setIdGiro(detalleGiro.getIdGiroSii().intValue());
                        item.setNombreGiroSii(detalleGiro.getNombreGiroSii());
                        item.setCodigoGiroSii(detalleGiro.getCodigoGiroSii());
                    } else {
                        //item.setIdGiro(idGiroBuscado != null ? idGiroBuscado.intValue() : null);
                        item.setNombreGiroSii("Giro no encontrado");
                        item.setCodigoGiroSii("NO-GIRO");
                    }
                    return item;
                })
                .toList(); // Java 17 ya permite .toList() directamente sin Collectors

        Comunas comuna = comunasRepository.findById(p.getIdComuna())
                .orElseThrow(() -> new RuntimeException("Comuna no encontrado: " + p.getIdComuna()));

        // 2. Construcción del DTO con Builder
        return ProveedorDTO.builder()
               // .idProveedor(p.getIdProveedor())
                .rutProveedor(p.getRutProveedor())
                .nombreProveedor(p.getNombreProveedor())
                .razonSocialProveedor(p.getRazonSocialProveedor())
                .direccion(p.getDireccion())
                .telefonoContactoProveedor(p.getTelefonoContactoProveedor())
                .emailProveedor(p.getEmailProveedor())
                // Verificación de nulos para la relación Comuna
                .codComuna(comuna.getCodigoComuna())
                .activo(p.getActive())
                .listaGiros(listaGirosValues)
                .build();
    }

    private Proveedor toEntity(ProveedorDTO dto) {

        Comunas comuna = comunasRepository.findByCodigoComuna(dto.getCodComuna())
                .orElseThrow(() -> new RuntimeException("Comuna no encontrada: " + dto.getCodComuna()));

        return Proveedor.builder()
                //.idProveedor(dto.getIdProveedor())
                .rutProveedor(dto.getRutProveedor())
                .nombreProveedor(dto.getNombreProveedor())
                .razonSocialProveedor(dto.getRazonSocialProveedor())
                .direccion(dto.getDireccion())
                .telefonoContactoProveedor(dto.getTelefonoContactoProveedor())
                .emailProveedor(dto.getEmailProveedor())
                .active(dto.getActivo())
                // Seteamos la comuna solo con el ID para la relación
                .idComuna(comuna.getIdComuna())
                .build();
    }


}


/******/





