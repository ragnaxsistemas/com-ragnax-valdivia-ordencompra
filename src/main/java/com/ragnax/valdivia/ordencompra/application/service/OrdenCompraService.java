package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaStatusDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.*;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.GiroSii;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.*;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.GiroSiiRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UsuariosRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades.generarCodigo;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCompraService {

    private final OrdenCompraRepository ocRepo;
    private final EstadoOcRepository estadoOcRepository;
    private final DocumentoTributarioRepository documentoTributarioRepository;
    private final GiroSiiRepository giroSiiRepository;
    private final ProveedorRepository proveedorRepository;

    private final StatusOrdenCompraRepository statusOrdenCompraRepository;

    //private final RegionesRepository regionesRepository;
    private final UnidadRepository unidadRepository;
    private final UsuariosRepository usuariosRepository;

    private static final String COD_STATUS_BORRADOR ="borrador";
    /***private static final int STATUS_BORRADOR = 1;
    private static final int STATUS_PENDIENTE = 2;
    private static final int STATUS_AUTORIZADO = 3;
    private static final int STATUS_ANULADO = 4;
    private static final int STATUS_CONFIRMADO = 5;***/

    private EstadoOc registrarStatus(OrdenCompra oc, int idStatus, Integer idUsuario, String observacionStatus) {

        EstadoOc estadoOc = estadoOcRepository.findById(idStatus)
                .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + idStatus));

        StatusOrdenCompra s = new StatusOrdenCompra();
        s.setOrdenCompra(oc);
        s.setEstadoOc(estadoOc);
        s.setIdUsuario(idUsuario);
        s.setFechaEvento(LocalDateTime.now());
        s.setObservacion(observacionStatus);

        statusOrdenCompraRepository.save(s);

        return estadoOc;

    }

    private boolean validarNoSeBloqueada(OrdenCompra ordenCompra) {

        Optional < StatusOrdenCompra > optStatusOrdenCompra =
                statusOrdenCompraRepository.findByOrdenCompraAndEstadoOc(ordenCompra, EstadoOc.builder().idEstadoOc(4).build());

        if (optStatusOrdenCompra.isPresent()) {
            throw new IllegalStateException("La orden de compra está anulada y no puede ser modificada.");
        }
        optStatusOrdenCompra =
                statusOrdenCompraRepository.findByOrdenCompraAndEstadoOc(ordenCompra, EstadoOc.builder().idEstadoOc(5).build());

        if (optStatusOrdenCompra.isPresent()) {
            throw new IllegalStateException("La orden de compra está confirmada y no puede ser modificada.");
        }
        return true;
    }

    /***private boolean validarUnidadSupervisor(Integer idUsuario, Usuarios usuarioSupervisor) {

     if(usuario.getIdUnidad().getNombreUnidad().equals(usuarioSupervisor.getIdUnidad().getNombreUnidad())) {
     return true;
     }
     throw new IllegalStateException("La orden de compra no puede ser considerada por este supervisor."+ usuarioSupervisor.getUsername());
     }***/

    // ─── 1. Guardar (Borrador — estado 1) ─────────────────── //Mandar vacio a Plantilla
    public PlantillaDTO generarOC(PlantillaDTO plantillaDTO) {

        // ── 1. Resolver usuario (NOT NULL en BD) ─────────────────────────
        Usuarios usuario = usuariosRepository
                .findByUsername(plantillaDTO.getUsernameUsuario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado: " + plantillaDTO.getUsernameUsuario()));

        // ── 2. Resolver unidad (NOT NULL en BD) ──────────────────────────
        Unidad unidad = unidadRepository
                .findByCodigoUnidad(plantillaDTO.getCodUnidad())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unidad no encontrada: " + plantillaDTO.getCodUnidad()));

        // ── 3. Resolver FK opcionales (nullable en BD) ───────────────────

        // ── 4. Construir entidad SIN código aún ──────────────────────────
        OrdenCompra oc = OrdenCompra.builder()
                .idUsuario(usuario.getIdUsuario())
                .idUnidad(unidad.getIdUnidad())
                .documentoTributario(null) // puede ser null (nullable en BD)
                .proveedor(null) // puede ser null (nullable en BD)
                .nombreOrdenCompra(plantillaDTO.getNombreOrdenCompra())
                .observaciones(plantillaDTO.getObservaciones())
                .listProductosOrden(plantillaDTO.getListProductosOrden())
                .totalNeto(plantillaDTO.getTotalNeto())
                .impuesto(plantillaDTO.getImpuesto())
                .total(plantillaDTO.getTotal())
                .build();

        // ── 5. Primer save → BD asigna el ID real (sin condición de carrera) ─
        OrdenCompra saved = ocRepo.save(oc);
        String codigo = Utilidades.generarCodigo(saved.getIdOrdenCompra());
        saved.setCodigoOrdenCompra(codigo);
        saved = ocRepo.save(saved);
        // ── 6. Generar código con el ID real y actualizar ─────────────────
        saved.setCodigoOrdenCompra(generarCodigo(saved.getIdOrdenCompra()));
        OrdenCompra savedConCodigo = ocRepo.save(saved);

        String observacionStatus = "Status Generado por el usuario "+ usuario.getUsername() +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Borrador";
        // ── 7. Registrar estado borrador ──────────────────────────────────
        EstadoOc estadoOc = estadoOcRepository.findByCodigoEstadoOc(COD_STATUS_BORRADOR)
                .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + COD_STATUS_BORRADOR));

        registrarStatus(savedConCodigo, 1, savedConCodigo.getIdUsuario(), observacionStatus);

        plantillaDTO.setCodOrdenCompra(oc.getCodigoOrdenCompra());
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        plantillaDTO.setCodEstadoActualOc(estadoOc.getCodigoEstadoOc());

        return plantillaDTO;
    }

    // ─── 1. Guardar (Borrador — estado 1) ─────────────────── //Mandar vacio a Plantilla
    public PlantillaDTO guardar(PlantillaDTO plantillaDTO) {

        OrdenCompra oc = null;
        Usuarios usuario = null;

        if(!plantillaDTO.getCodOrdenCompra().equals("")){
            oc =
                    ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                            .orElseThrow(() -> new ValdiviaOCException("OC "+plantillaDTO.getCodOrdenCompra() +"no encontrada"));
        }
        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 1);

        if(!plantillaDTO.getUsernameUsuario().equals("")){
            Optional<Usuarios> optUsuario = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario());

            usuario = optUsuario.isPresent() ? optUsuario.get() : Usuarios.builder().build();
            oc.setIdUsuario(usuario.getIdUsuario());
        }
        //Enviar el Id del DTE
        if(!plantillaDTO.getCodDocumentoTributario().equals("")){
            Optional<DocumentoTributario> optDte = documentoTributarioRepository.findByCodigoDocumentoTributario
                    (plantillaDTO.getCodDocumentoTributario());
            oc.setDocumentoTributario(optDte.isPresent() ? optDte.get() : DocumentoTributario.builder().build());
        }

        if(!plantillaDTO.getCodUnidad().equals("")){
            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad
                    (plantillaDTO.getCodUnidad());

            oc.setIdUnidad(optUnidad.isPresent() ? optUnidad.get().getIdUnidad() : Unidad.builder().build().getIdUnidad());
        }

        if(!plantillaDTO.getRutProveedor().equals("")){
            Optional<Proveedor> optProveedor = proveedorRepository.findByRutProveedor
                    (Utilidades.formatearRut(plantillaDTO.getRutProveedor()));
            oc.setProveedor(optProveedor.isPresent() ? optProveedor.get() : Proveedor.builder().build());
        }

        if(!plantillaDTO.getCodGiroSeleccionado().equals("")){
            Optional<GiroSii> optGiroSii = giroSiiRepository .findByCodigoGiroSii(plantillaDTO.getCodGiroSeleccionado());

            oc.setCodigoGiroProveedor(optGiroSii.isPresent() ? optGiroSii.get().getCodigoGiroSii() : "");
        }
        // actualizar datos de plantilla

        oc.setNombreOrdenCompra(plantillaDTO.getNombreOrdenCompra() != null ?  plantillaDTO.getNombreOrdenCompra() : null);
        oc.setObservaciones(plantillaDTO.getObservaciones() != null ?  plantillaDTO.getObservaciones() : null);
        oc.setListProductosOrden(plantillaDTO.getListProductosOrden() != null ?  plantillaDTO.getListProductosOrden() : null);
        oc.setTotalNeto(plantillaDTO.getTotalNeto() != null ?  plantillaDTO.getTotalNeto() : null);
        oc.setImpuesto(plantillaDTO.getImpuesto() != null ?  plantillaDTO.getImpuesto() : null);
        oc.setTotal(plantillaDTO.getTotal() != null ?  plantillaDTO.getTotal() : null);

        OrdenCompra saved = ocRepo.save(oc);
        String observacionStatus = "Status Generado por el usuario "+ usuario.getUsername() +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Guardar/Borrador";
        EstadoOc estadoOc = registrarStatus(saved, 1, usuario.getIdUsuario(), observacionStatus);

        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }
    /******/
    // ─── 2. Solicitar autorización (Pendiente — estado 2) ───
    public PlantillaDTO solicitarAutorizacion(PlantillaDTO plantillaDTO) {

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+plantillaDTO.getCodOrdenCompra() +"no encontrada"));

        validarNoSeBloqueada(oc);
        //Buscar ultimo estado de la oc x
        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 2);

        Usuarios usuario = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + plantillaDTO.getUsernameUsuario()));
        oc.setIdUsuario(usuario.getIdUsuario());

        DocumentoTributario dte = documentoTributarioRepository.findByCodigoDocumentoTributario
                (plantillaDTO.getCodDocumentoTributario())
                .orElseThrow(() -> new ValdiviaOCException("DTE no encontrada"));
        oc.setDocumentoTributario(dte);

        Unidad unidad = unidadRepository.findByCodigoUnidad
                        (plantillaDTO.getCodUnidad())
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + plantillaDTO.getCodUnidad()));
        oc.setIdUnidad(unidad.getIdUnidad());

        Proveedor proveedor = proveedorRepository.findByRutProveedor
                        (Utilidades.formatearRut( plantillaDTO.getRutProveedor()))
                .orElseThrow(() -> new ValdiviaOCException("Proveedor no encontrada"));
        oc.setProveedor(proveedor);

        // actualizar datos de plantilla
        if(plantillaDTO.getNombreOrdenCompra()!=null && !plantillaDTO.getNombreOrdenCompra().equals("")){
            oc.setNombreOrdenCompra(plantillaDTO.getNombreOrdenCompra());
        }else{
            throw new ValdiviaOCException("Nombre de Orden no encontrada");
        }

        oc.setObservaciones(plantillaDTO.getObservaciones());

        if(plantillaDTO.getListProductosOrden()!=null && !plantillaDTO.getListProductosOrden().equals("")){
            oc.setListProductosOrden(plantillaDTO.getListProductosOrden());
        }else{
            throw new ValdiviaOCException("Lista de Productos no encontrada");
        }

        if(plantillaDTO.getTotalNeto()!=null && !plantillaDTO.getTotalNeto().equals("")){
            oc.setTotalNeto(plantillaDTO.getTotalNeto());
        }else{
            throw new ValdiviaOCException("Total Neto no encontrada");
        }

        if(plantillaDTO.getImpuesto()!=null && !plantillaDTO.getImpuesto().equals("")){
            oc.setImpuesto(plantillaDTO.getImpuesto());
        }else{
            throw new ValdiviaOCException("Impuesto no encontrada");
        }

        if(plantillaDTO.getTotal()!=null && !plantillaDTO.getTotal().equals("")){
            oc.setTotal(plantillaDTO.getTotal());
        }else{
            throw new ValdiviaOCException("Total no encontrada");
        }

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuario.getUsername() +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Pendiente de Autorizacion";

        EstadoOc estadoOc = registrarStatus(saved, 2, usuario.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    // ─── 3. Devolver (Borrador — estado 1, acción supervisor) ─
    public PlantillaDTO devolver(String codOCdevolver, PlantillaDTO plantillaDTO, String usuarioSupervisor) {
        if(!codOCdevolver.equalsIgnoreCase(plantillaDTO.getCodOrdenCompra())){
            new ValdiviaOCException("OC "+codOCdevolver +"no valida");
        }

        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCdevolver)
                .orElseThrow(() -> new ValdiviaOCException("OC "+codOCdevolver +"no encontrada"));

        validarNoSeBloqueada(oc);
        if(usuarioSup.getIdUnidad().getIdUnidad().equals(oc.getIdUnidad())){
            Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
            //Pendiente
            validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 1);

            //validarUnidadSupervisor(oc.getIdUsuario(), usuarioSup);
            // actualizar datos de plantilla
            //OrdenCompra saved = ocRepo.save(oc);

            String observacionStatus = "Status Generado por el usuario "+ usuarioSupervisor +" para la orden "+oc.getCodigoOrdenCompra() + " En estado Devolver/Borrador";
            EstadoOc estadoOc = registrarStatus(oc, 1, usuarioSup.getIdUsuario(), observacionStatus);


            plantillaDTO = convertToDTO(oc);
            plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
            return plantillaDTO;
        }
        throw new ValdiviaOCException("OC "+codOCdevolver +" unidad Invalida");
        //Con la OC obtenida, buscar estado actual
    }

    // ─── 4. Autorizar (estado 3) ─────────────────────────────
    public PlantillaDTO autorizar(String codOCautorizar, PlantillaDTO plantillaDTO, String usuarioSupervisor) {
        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCautorizar)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCautorizar +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 3);

        // actualizar datos de plantilla
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        //validarUnidadSupervisor(oc.getUsuario(), usuarioSup);

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSupervisor +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Autorizada";
        EstadoOc estadoOc = registrarStatus(saved, 3, usuarioSup.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }
    // ─── 5. Anular (estado 4 — bloquea la OC) ───────────────
    public PlantillaDTO anular(String codOCanular, PlantillaDTO plantillaDTO,  String usuarioSupervisor) {
        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCanular)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCanular +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 4);

        // actualizar datos de plantilla
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        //validarUnidadSupervisor(oc.getUsuario(), usuarioSup);

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSupervisor +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Autorizada";
        EstadoOc estadoOc = registrarStatus(saved, 4, usuarioSup.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    // ─── 6. Confirmar (estado 5 — bloquea la OC) ────────────
    public PlantillaDTO confirmar(String codOCautorizar, PlantillaDTO plantillaDTO,  String usuarioSupervisor) {
        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCautorizar +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 5);

        // actualizar datos de plantilla
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        validarNoSeBloqueada(oc);
        //validarUnidadSupervisor(oc.getUsuario(), usuarioSup);

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSup.getUsername() +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Confirmada";
        EstadoOc estadoOc = registrarStatus(saved, 5, usuarioSup.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    public void validarTransicionEstado(Integer estadoActual, Integer estadoNuevo) {
        boolean esValido = false;

        // Si el estado actual es nulo (orden nueva), permitimos entrar en Borrador (1)
        if (estadoActual == null) {
            if (estadoNuevo == 1) return;
            throw new RuntimeException("Una orden nueva debe comenzar en estado Borrador.");
        }

        switch (estadoActual) {
            case 1: // Borrador
                if (estadoNuevo == 1) esValido = true;
                if (estadoNuevo == 2) esValido = true;
                break;

            case 2: // Pendiente Autorización
                if (estadoNuevo == 1) esValido = true;
                if (estadoNuevo == 3) esValido = true;
                break;

            case 3: // Autorizado
                // Puede volver a Borrador(Devolver), ir a Pendiente Anulación, Anulado o Confirmada
                if (List.of(1, 4, 5, 6).contains(estadoNuevo)) esValido = true;
                break;

            case 4: // Anulado
            case 5: // Confirmada
                // Estados finales: no permiten más transiciones
                esValido = false;
                break;

            case 6: // Pendiente Anulación
                if (List.of(4, 5).contains(estadoNuevo)) esValido = true;
                break;

            default:
                throw new RuntimeException("Estado actual no reconocido: " + estadoActual);
        }

        if (!esValido) {
            throw new RuntimeException("Transición no permitida: de estado " + estadoActual + " a " + estadoNuevo);
        }
    }

    public Page < PlantillaStatusDTO > realizarBusquedaAvanzada(
            String codEstadoOc, String rut,
            String codUnidad, String codOrdenCompra, LocalDate fecha, Pageable pageable) {

        String rutParaQuery = rut;
        Integer unidadParaQuery = null;
        Integer idStatus = null;
        if (codEstadoOc != null && !codEstadoOc.isEmpty()) {
            // Si el RUT viene solo con números, lo "ensuciamos" para que se parezca a la BD
            // Ejemplo: 762345678 -> %76%234%567%8%
            idStatus = estadoOcRepository.findByCodigoEstadoOc(codEstadoOc)
                    .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + codEstadoOc)).getIdEstadoOc();
            // Esto crea un patrón que ignora los puntos y guiones intermedios
        }

        if (rut != null && !rut.isEmpty()) {
            // Si el RUT viene solo con números, lo "ensuciamos" para que se parezca a la BD
            // Ejemplo: 762345678 -> %76%234%567%8%
            rutParaQuery = rut.trim().replaceAll("", "%");
            // Esto crea un patrón que ignora los puntos y guiones intermedios
        }
        if (codUnidad != null && !codUnidad.isEmpty()) {
            Optional < Unidad > optUnidad = unidadRepository
                    .findByCodigoUnidad(codUnidad);

            if (optUnidad.isPresent()) {
                unidadParaQuery = optUnidad.get().getIdUnidad();
            }
        }
        // Estatus, rut Proveedor, Unidad, CodigoOrdenCompra, FechaCreacion, Pageable
        // 1. Ejecutar la búsqueda en el repositorio
        Page < OrdenCompra > ordenes = ocRepo.buscarAvanzado(
                idStatus, rutParaQuery, unidadParaQuery, codOrdenCompra, fecha, pageable
        );

        /***buscarYFiltrar(ordenes, idStatus, rutParaQuery, nombreProv,
         unidadId, codigo, fecha);***/
        // 2. Transformar la página de Entidades a página de DTOs
        return ordenes.map(this::convertToStatusDTO);
    }

    private PlantillaDTO convertToDTO(OrdenCompra oc) {
        PlantillaDTO dto = new PlantillaDTO();

        // --- Datos Heredados de PlantillaDTO (Base) ---
        dto.setCodOrdenCompra(oc.getCodigoOrdenCompra());
        dto.setFechaOrdenCompra(oc.getFechaCreacion().toString());
        dto.setNombreOrdenCompra(oc.getNombreOrdenCompra());
        dto.setObservaciones(oc.getObservaciones());
        dto.setTotalNeto(oc.getTotalNeto());
        dto.setImpuesto(oc.getImpuesto());
        dto.setTotal(oc.getTotal());
        // Asumiendo que guardas los items como JSON String en la BD
        dto.setListProductosOrden(oc.getListProductosOrden());

        // --- Datos del Usuario (JOIN FETCH oc.usuario) ---
        if (oc.getIdUsuario() != null) {
            Optional < Usuarios > optUsuarios = usuariosRepository.findById(oc.getIdUsuario());
            if (optUsuarios.isPresent()) {
                dto.setUsernameUsuario(optUsuarios.get().getUsername()); // Para compatibilidad con PlantillaDTO
            }

        }

        // --- Datos de la Unidad (JOIN FETCH oc.unidad) ---
        if (oc.getIdUnidad() != null) {
            Optional < Unidad > optUnidad = unidadRepository.findById(oc.getIdUnidad());
            if (optUnidad.isPresent()) {
                dto.setCodUnidad(optUnidad.get().getCodigoUnidad());
                //dto.setUnidad(optUnidad.get().getNombreUnidad());
            }
        }
        // --- Datos del Proveedor (JOIN FETCH oc.proveedor) ---
        if (oc.getProveedor() != null) {
            dto.setRutProveedor(oc.getProveedor().getRutProveedor());
        }

        if (oc.getCodigoGiroProveedor() != null) {
            dto.setCodGiroSeleccionado(oc.getCodigoGiroProveedor());
        }

        // --- Datos del Documento (JOIN FETCH oc.documentoTributario) ---
        if (oc.getDocumentoTributario() != null) {
            dto.setCodDocumentoTributario(oc.getDocumentoTributario().getCodigoDocumentoTributario()); // Campo padre
            dto.setNombreDocumentoTributario(oc.getDocumentoTributario().getNombreDocumentoTributario());
        }

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());

        if(optStatusOrdenCompraActual.isPresent()){
            StatusOrdenCompra ultimoEstado = optStatusOrdenCompraActual.get();
            dto.setCodEstadoActualOc(ultimoEstado.getEstadoOc().getCodigoEstadoOc()); // Campo padre
            dto.setEstadoActualOc(ultimoEstado.getEstadoOc().getNombreEstadoOc());
        }
        return dto;
    }

    private PlantillaStatusDTO convertToStatusDTO(OrdenCompra oc) {
        PlantillaStatusDTO dto = new PlantillaStatusDTO();

        // --- Datos Heredados de PlantillaDTO (Base) ---
        dto.setCodOrdenCompra(oc.getCodigoOrdenCompra());
        dto.setFechaOrdenCompra(oc.getFechaCreacion().toString());
        dto.setNombreOrdenCompra(oc.getNombreOrdenCompra());
        dto.setObservaciones(oc.getObservaciones());
        dto.setTotalNeto(oc.getTotalNeto());
        dto.setImpuesto(oc.getImpuesto());
        dto.setTotal(oc.getTotal());
        // Asumiendo que guardas los items como JSON String en la BD
        dto.setListProductosOrden(oc.getListProductosOrden());

        // --- Datos del Usuario (JOIN FETCH oc.usuario) ---
        if (oc.getIdUsuario() != null) {
            Optional < Usuarios > optUsuarios = usuariosRepository.findById(oc.getIdUsuario());
            if (optUsuarios.isPresent()) {
                dto.setUsernameUsuario(optUsuarios.get().getUsername());
                dto.setNombreUsuario(optUsuarios.get().getNombreMember());
                dto.setApellidoUsuario(optUsuarios.get().getApellidoPaternoMember());
                dto.setUsernameUsuario(optUsuarios.get().getUsername()); // Para compatibilidad con PlantillaDTO
            }

        }

        // --- Datos de la Unidad (JOIN FETCH oc.unidad) ---
        if (oc.getIdUnidad() != null) {
            Optional < Unidad > optUnidad = unidadRepository.findById(oc.getIdUnidad());
            if (optUnidad.isPresent()) {
                dto.setCodUnidad(optUnidad.get().getCodigoUnidad());
                dto.setNombreUnidad(optUnidad.get().getNombreUnidad());
                //dto.setUnidad(optUnidad.get().getNombreUnidad());
            }
        }
        // --- Datos del Proveedor (JOIN FETCH oc.proveedor) ---
        if (oc.getProveedor() != null) {
            dto.setRutProveedor(oc.getProveedor().getRutProveedor());
            dto.setNombreProveedor(oc.getProveedor().getNombreProveedor());
            dto.setRazonSocialProveedor(oc.getProveedor().getRazonSocialProveedor());
            dto.setDireccionProveedor(oc.getProveedor().getDireccion());
            //dto.setGiroProveedor(oc.getProveedor().getGiro());
            dto.setEmailProveedor(oc.getProveedor().getEmailProveedor());
            //dto.setComunaProveedor(oc.getProveedor().getIdComuna().getNombreComuna());
            //dto.setRegionProveedor(oc.getProveedor().getIdComuna().getRegion().getNombreRegion()) ;
            dto.setRutProveedor(oc.getProveedor().getRutProveedor());
        }

        if (oc.getCodigoGiroProveedor() != null) {
            Optional<GiroSii> optGiroSii = giroSiiRepository .findByCodigoGiroSii(oc.getCodigoGiroProveedor());

            dto.setCodGiroSeleccionado(oc.getCodigoGiroProveedor());
            dto.setGiroProveedor(optGiroSii.get().getNombreGiroSii());
        }

        // --- Datos del Documento (JOIN FETCH oc.documentoTributario) ---
        if (oc.getDocumentoTributario() != null) {
            dto.setCodDocumentoTributario(oc.getDocumentoTributario().getCodigoDocumentoTributario()); // Campo padre
            dto.setNombreDocumentoTributario(oc.getDocumentoTributario().getNombreDocumentoTributario());
            dto.setDescripcionDocumentoElectronico(oc.getDocumentoTributario().getDescripcionDocumentoTributario());
            // Si el impuesto viene como String en el DTO, lo convertimos
            dto.setImpuestoDocumentoElectronico(String.valueOf(oc.getDocumentoTributario().getImpuesto()));
        }

        // --- Estado Actual (Lógica de negocio) ---
        // Aquí puedes obtener el nombre del estado desde el último status o
        // mapear el ID de estado a un String legible para el frontend.

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());

        if(optStatusOrdenCompraActual.isPresent()){
            StatusOrdenCompra ultimoEstado = optStatusOrdenCompraActual.get();
            dto.setCodEstadoActualOc(ultimoEstado.getEstadoOc().getCodigoEstadoOc()); // Campo padre
            dto.setEstadoActualOc(ultimoEstado.getEstadoOc().getNombreEstadoOc());
        }
        return dto;
    }


    /***public Page<PlantillaStatusDTO> listarOrdenCompraUltimoStatus(String codigoEstadoOc, Pageable pageable) {

     EstadoOc estadoOc = estadoOcRepository.findByCodigoEstadoOc(codigoEstadoOc)
     .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + codigoEstadoOc));

     return ocRepo.findByStatusActual(estadoOc.getIdEstadoOc().longValue(), pageable).map(this::convertToStatusDTO);
     }***/

    /***public Page<PlantillaStatusDTO> obtenerBorradoresCodigoOrdenCompra(Pageable pageable, String codigoOrdenCompra) {
     Page<PlantillaStatusDTO> pgPlantillaStatusDTO = ocRepo.
     buscarAvanzado(STATUS_BORRADOR, null,null, null, codigoOrdenCompra, null, pageable).map(this::convertToStatusDTO);
     return pgPlantillaStatusDTO;
     }***/

    /***public Page<PlantillaStatusDTO> listarPendientes(Pageable pageable) {
     return ocRepo.findByStatusActual(STATUS_PENDIENTE, pageable).map(this::convertToStatusDTO);
     }

     public Page<PlantillaStatusDTO> listarAutorizadas(Pageable pageable) {

     return ocRepo.findByStatusActual(STATUS_AUTORIZADO, pageable).map(this::convertToStatusDTO);
     }

     public Page<PlantillaStatusDTO> listarConfirmadas(Pageable pageable) {
     return ocRepo.findByStatusActual(STATUS_CONFIRMADO, pageable).map(this::convertToStatusDTO);
     }

     public Page<PlantillaStatusDTO> listarAnuladas(Pageable pageable) {
     return ocRepo.findByStatusActual(STATUS_ANULADO, pageable).map(this::convertToStatusDTO);
     }***/



    /***protected PlantillaStatusDTO convertirAPlantilla(OrdenCompra oc) {
     // Instanciamos el DTO hijo que contiene todos los campos extendidos
     PlantillaStatusDTO dto = new PlantillaStatusDTO();

     // 1. Campos heredados de PlantillaDTO
     dto.setCodigoOrdenCompra(oc.getCodigoOrdenCompra());
     dto.setFechaOrdenCompra(oc.getFechaCreacion() != null ? oc.getFechaCreacion().toString() : "");
     dto.setNombreOrdenCompra(oc.getNombreOrdenCompra());
     dto.setObservaciones(oc.getObservaciones());
     dto.setListProductosOrden(oc.getListProductosOrden());
     dto.setTotalNeto(oc.getTotalNeto());
     dto.setImpuesto(oc.getImpuesto());
     dto.setTotal(oc.getTotal());

     // 2. Mapeo detallado de USUARIO
        if (oc.getIdUsuario() != null) {
            Optional < Usuarios > optUsuarios = usuariosRepository.findById(oc.getIdUsuario());
            if (optUsuarios.isPresent()) {
                dto.setUsernameUsuario(optUsuarios.get().getUsername());
                dto.setNombreUsuario(optUsuarios.get().getNombreMember());
                dto.setApellidoUsuario(optUsuarios.get().getApellidoPaternoMember());
                dto.setUsernameUsuario(optUsuarios.get().getUsername()); // Para compatibilidad con PlantillaDTO
            }

        }

     // 3. Mapeo detallado de UNIDAD
     //if (oc.getUnidad() != null) {
     //    dto.setUnidad(oc.getUnidad().getNombreUnidad()); // Campo padre
     //    dto.setCodigoUnidad(oc.getUnidad().getCodigoUnidad());
     //    dto.setNombreUnidad(oc.getUnidad().getNombreUnidad());
     //}

     // 4. Mapeo detallado de PROVEEDOR
     if (oc.getProveedor() != null) {
     dto.setProveedor(oc.getProveedor().getRutProveedor()); // Campo padre
     dto.setRutProveedor(oc.getProveedor().getRutProveedor());
     dto.setNombreProveedor(oc.getProveedor().getNombreProveedor());
     dto.setRazonSocialProveedor(oc.getProveedor().getRazonSocialProveedor());
     dto.setDireccionProveedor(oc.getProveedor().getDireccion());
     //dto.setGiroProveedor(oc.getProveedor().getGiro());
     dto.setTelefonoContactoProveedor(oc.getProveedor().getTelefonoContactoProveedor());
     dto.setEmailProveedor(oc.getProveedor().getEmailProveedor());

     //dto.setComunaProveedor(oc.getProveedor().getIdComuna().getNombreComuna());
     //dto.setRegionProveedor(oc.getProveedor().getIdComuna().getRegion().getNombreRegion()) ;


     }

     // 5. Mapeo detallado de DOCUMENTO ELECTRÓNICO
     if (oc.getDocumentoTributario() != null) {
     dto.setIdDocumentoElectronico(oc.getDocumentoTributario().getIdDocumentoTributario()); // Campo padre
     dto.setDocumentoElectronico(oc.getDocumentoTributario().getCodigoDocumentoTributario()); // Campo padre
     dto.setCodigoDocumentoElectronico(oc.getDocumentoTributario().getCodigoDocumentoTributario());
     dto.setNombreDocumentoElectronico(oc.getDocumentoTributario().getNombreDocumentoTributario());
     dto.setDescripcionDocumentoElectronico(oc.getDocumentoTributario().getDescripcionDocumentoTributario());
     // Si el impuesto viene como String en el DTO, lo convertimos
     dto.setImpuestoDocumentoElectronico(String.valueOf(oc.getDocumentoTributario().getImpuesto()));
     }

     // 6. Lógica de ESTADO (obteniendo el más reciente)
     StatusOrdenCompra ultimoEstado = oc.getStatusOrdenes().stream()
     .max(Comparator.comparing(StatusOrdenCompra::getFechaEvento))
     .orElse(null);

     if (ultimoEstado != null && ultimoEstado.getEstadoOc() != null) {
     String nombreEstado = ultimoEstado.getEstadoOc().getNombreEstadoOc();
     dto.setEstadoOc(nombreEstado); // Campo padre
     dto.setEstado(nombreEstado);
     dto.setNombreEstado(nombreEstado);
     dto.setDescripcionEstado(ultimoEstado.getEstadoOc().getDescripcion());
     }

     return dto;
     }***/

    /***public Page < PlantillaStatusDTO > buscarYFiltrar(Page < OrdenCompra > ordenes,
                                                      Integer idStatus, String rutProveedor, String nombreProv,
                                                      Integer unidadId, String codigo, LocalDate fecha) {

        // Convertimos la lista de la página a un Stream para filtrar
        List < PlantillaStatusDTO > filtrados = ordenes.getContent().stream()
                .map(this::convertToStatusDTO) // Primero convertimos a DTO para tener los campos limpios
                .filter(dto -> {
                    boolean match = true;

                    // 1. Filtro por Estado (Exacto)
                    if (idStatus != null) {
                        match &= dto.getEstadoActualOc() != null &&
                                dto.getEstadoActualOc().equals(idStatus);
                    }

                    // 2. Filtro por RUT (Normalizado: quita puntos y guion)
                    if (rutProveedor != null && !rutProveedor.isBlank()) {
                        String rutLimpioFiltro = rutProveedor.replaceAll("[.-]", "");
                        String rutLimpioDTO = dto.getRutProveedor().replaceAll("[.-]", "");
                        match &= rutLimpioDTO.contains(rutLimpioFiltro);
                    }

                    // 3. Filtro por Folio (Formato aaa-bbb-1235-xxx)
                    // Buscamos que el código contenga el número correlativo (ej: "1235")
                    // if (filtros.getFolio() != null && !filtros.getFolio().isBlank()) {
                    //     String correlativoBuscado = extraerCorrelativo(filtros.getFolio());
                    //    match &= dto.getCodigoOc().contains(correlativoBuscado);
                    //}

                    return match;
                })
                .collect(Collectors.toList());

        // Devolvemos una nueva instancia de Page con los resultados filtrados
        return new PageImpl < > (filtrados, ordenes.getPageable(), filtrados.size());
    }***/

    /**
     * Extrae el número central de un folio tipo aaa-bbb-1235-xxx
     */
    /***private String extraerCorrelativo(String folio) {
        if (folio.contains("-")) {
            String[] partes = folio.split("-");
            // Si tiene el formato completo, el correlativo suele ser la 3ra parte (índice 2)
            if (partes.length >= 3) return partes[2];
        }
        return folio; // Si no tiene guiones, asumimos que ya es el número
    }***/
}