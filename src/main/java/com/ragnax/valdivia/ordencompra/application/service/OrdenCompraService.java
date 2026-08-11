package com.ragnax.valdivia.ordencompra.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragnax.valdivia.ordencompra.application.service.component.MailComponent;
import com.ragnax.valdivia.ordencompra.application.service.component.PdfComponent;
import com.ragnax.valdivia.ordencompra.application.service.model.DocumentoOrdenCompra;
import com.ragnax.valdivia.ordencompra.application.service.model.OrdenCompraHtml;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.PlantillaCargar;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.PlantillaOrdenCompra;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades;
import com.ragnax.valdivia.ordencompra.infraestructura.configuration.ApiProperties;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.*;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.*;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.*;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.*;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades.generarCodigo;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrdenCompraService {

    private final ApiProperties apiProperties;
    private final PdfComponent pdfComponent;
    private final MailComponent mailComponent;
    private final GeneradorReporteService generadorReporteService;

    private final OrdenCompraConfirmadaRepository ordenCompraConfirmadaRepository;
    private final OrdenCompraRepository ocRepo;
    private final EstadoOcRepository estadoOcRepository;
    private final DocumentoTributarioRepository documentoTributarioRepository;
    private final GiroSiiRepository giroSiiRepository;
    private final ProveedorRepository proveedorRepository;
    private final StatusOrdenCompraRepository statusOrdenCompraRepository;
    private final UnidadRepository unidadRepository;
    private final UsuariosRepository usuariosRepository;
    private final ComunasRepository comunasRepository;
    private final RegionesRepository regionesRepository;
    private final AdjuntoOrdenCompraRepository adjuntoOrdenCompraRepository;


    private static final String COD_STATUS_BORRADOR ="borrador";
    private static final int STATUS_BORRADOR = 1;
     private static final int STATUS_PENDIENTE = 2;
     private static final int STATUS_AUTORIZADO = 3;
     private static final int STATUS_ANULADO = 4;
     private static final int STATUS_CONFIRMADO = 5;

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
        /***optStatusOrdenCompra =
                statusOrdenCompraRepository.findByOrdenCompraAndEstadoOc(ordenCompra, EstadoOc.builder().idEstadoOc(5).build());

        if (optStatusOrdenCompra.isPresent()) {
            throw new IllegalStateException("La orden de compra está confirmada y no puede ser modificada.");
        }***/
        return true;
    }

    private boolean validarUnidadSupervisor(Usuarios usuarioPlantilla, Usuarios usuarioSupervisor) {

     if(usuarioPlantilla.getIdUnidad().getCodigoUnidad().equals(usuarioSupervisor.getIdUnidad().getCodigoUnidad())) {
        return true;
     }
     throw new IllegalStateException("La orden de compra no puede ser considerada por este supervisor."+ usuarioSupervisor.getUsername());
     }

    public PlantillaDTO findUltimoOc() {

        PlantillaDTO plantillaDTO = new PlantillaDTO();

        Integer ultimoIdOrdenCompra = ocRepo.findUltimoIdOrdenCompra();

        String codigo = Utilidades.generarCodigo(ultimoIdOrdenCompra+1);

        EstadoOc estadoOc = estadoOcRepository.findByCodigoEstadoOc(COD_STATUS_BORRADOR)
                .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + COD_STATUS_BORRADOR));

        plantillaDTO.setCodOrdenCompra(codigo);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        plantillaDTO.setCodEstadoActualOc(estadoOc.getCodigoEstadoOc());
        plantillaDTO.setCodEstadoActualOc(estadoOc.getCodigoEstadoOc());

        return plantillaDTO;

    }
    // ─── 1. Guardar (Borrador — estado 1) ─────────────────── //Mandar vacio a Plantilla
    public PlantillaDTO generarOC(PlantillaDTO plantillaDTO, String usernameCreador) {

        // ── 1. Resolver usuario (NOT NULL en BD) ─────────────────────────
        Usuarios usuario = usuariosRepository
                .findByUsername(usernameCreador)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado: " + usernameCreador));

        // ── 4. Construir entidad SIN código aún ──────────────────────────
        OrdenCompra oc = OrdenCompra.builder()

               // .idUnidad(unidad.getIdUnidad())
                .documentoTributario(null) // puede ser null (nullable en BD)
                .proveedor(null) // puede ser null (nullable en BD)
                .nombreOrdenCompra(plantillaDTO.getNombreOrdenCompra())
                .observaciones(plantillaDTO.getObservaciones())
                .listProductosOrden(plantillaDTO.getListProductosOrden())
                .totalNeto(plantillaDTO.getTotalNeto())
                .impuesto(plantillaDTO.getImpuesto())
                .total(plantillaDTO.getTotal())
                /**** **/
                .idUsuarioCreador(usuario.getIdUsuario())
                .build();

        // ── 5. Primer save → BD asigna el ID real (sin condición de carrera) ─
        OrdenCompra saved = ocRepo.save(oc);
        String codigo = Utilidades.generarCodigo(saved.getIdOrdenCompra());
        saved.setCodigoOrdenCompra(codigo);
        saved = ocRepo.save(saved);
        // ── 6. Generar código con el ID real y actualizar ─────────────────
        saved.setCodigoOrdenCompra(generarCodigo(saved.getIdOrdenCompra()));
        OrdenCompra savedConCodigo = ocRepo.save(saved);

        // ── 7. Registrar estado borrador ──────────────────────────────────
        EstadoOc estadoOc = estadoOcRepository.findByCodigoEstadoOc(COD_STATUS_BORRADOR)
                .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + COD_STATUS_BORRADOR));

        String observacionStatus = "Status "+ estadoOc.getNombreEstadoOc() +" para la orden "+saved.getCodigoOrdenCompra() +" para el usuario "+ usuario.getUsername();

        //Borrador
        registrarStatus(savedConCodigo, STATUS_BORRADOR, savedConCodigo.getIdUsuarioCreador(), observacionStatus);

        //Solo al crear se limpia la carpeta
        limpiarCarpetaOrdenCompra(oc.getCodigoOrdenCompra());

        plantillaDTO.setCodOrdenCompra(oc.getCodigoOrdenCompra());
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        plantillaDTO.setCodEstadoActualOc(estadoOc.getCodigoEstadoOc());
        plantillaDTO.setCodEstadoActualOc(estadoOc.getCodigoEstadoOc());

        return plantillaDTO;
    }

    // ─── 1. Guardar (Borrador — estado 1) ─────────────────── //Mandar vacio a Plantilla
    public PlantillaStatusDTO guardar(PlantillaDTO plantillaDTO, String usernameGuardar) {

        OrdenCompra oc = null;

        if(!plantillaDTO.getCodOrdenCompra().equals("")){
            Optional<OrdenCompra> optOrdenCompra =
                    ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra());
            if(optOrdenCompra.isEmpty()){
                PlantillaDTO genPlantillaDTO = generarOC(plantillaDTO, usernameGuardar);
                oc =  ocRepo.findByCodigoOrdenCompra(genPlantillaDTO.getCodOrdenCompra())
                        .orElseThrow(() -> new ValdiviaOCException("OC "+plantillaDTO.getCodOrdenCompra() +" no encontrada"));
            }
        }
        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 1);

        /***if(!plantillaDTO.getUsernameUsuarioCreador().equals("")){
            Optional<Usuarios> optUsuario = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuarioCreador());

            usuario = optUsuario.isPresent() ? optUsuario.get() : Usuarios.builder().build();
            oc.setIdUsuario(usuario.getIdUsuario());
        }***/
        //Enviar el Id del DTE
        if(!plantillaDTO.getCodDocumentoTributario().equals("")){
            Optional<DocumentoTributario> optDte = documentoTributarioRepository.findByCodigoDocumentoTributario
                    (plantillaDTO.getCodDocumentoTributario());
            oc.setDocumentoTributario(optDte.isPresent() ? optDte.get() : DocumentoTributario.builder().build());
        }

        if(plantillaDTO.getCodUnidad()!=null && !plantillaDTO.getCodUnidad().equals("")){
            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad
                    (plantillaDTO.getCodUnidad());

            oc.setIdUnidad(optUnidad.isPresent() ? optUnidad.get().getIdUnidad() : Unidad.builder().build().getIdUnidad());
        }

        if(plantillaDTO.getRutProveedor()!=null && !plantillaDTO.getRutProveedor().equals("")){
            Optional<Proveedor> optProveedor = proveedorRepository.findByRutProveedor
                    (Utilidades.formatearRut(Utilidades.limpiarRut(plantillaDTO.getRutProveedor())));
            oc.setProveedor(optProveedor.isPresent() ? optProveedor.get() : Proveedor.builder().build());
        }

        if(plantillaDTO.getCodGiroSeleccionado()!=null &&
                !plantillaDTO.getCodGiroSeleccionado().equals("")){
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

        String observacionStatus = "Status Guardar/Borrador para la orden "+saved.getCodigoOrdenCompra() +" para el usuario "+ usernameGuardar;
        EstadoOc estadoOc = registrarStatus(saved, STATUS_BORRADOR, oc.getIdUsuarioCreador(), observacionStatus);

        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc().toUpperCase());

        return realizarBusquedaAvanzada(
                null,
                null,
                null, oc.getCodigoOrdenCompra(), null, null
                , null, null,
                PageRequest.of(0, 1)).getContent().get(0);

 //       return plantillaDTO;
    }
    /******/
    // ─── 2. Solicitar autorización (Pendiente — estado 2) ───
    public PlantillaDTO solicitarAutorizacion(PlantillaDTO plantillaDTO, String usernameSolicitante, String codUnidadSupervisor) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(plantillaDTO);
        log.info("usernameSolicitante {} | codUnidadSupervisor {} | plantilla  {}", usernameSolicitante,codUnidadSupervisor, jsonString);

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+plantillaDTO.getCodOrdenCompra() +" no encontrada"));

        validarNoSeBloqueada(oc);
        //Buscar ultimo estado de la oc x
        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 2);

        Unidad unidadSolicitante = unidadRepository.findByCodigoUnidad
                        (codUnidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + codUnidadSupervisor));

        Usuarios usuarioSolicitante = usuariosRepository.findByUsernameAndIdUnidad(usernameSolicitante, unidadSolicitante)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usernameSolicitante));
        oc.setIdUsuarioSolicitante(usuarioSolicitante.getIdUsuario());

        DocumentoTributario dte = documentoTributarioRepository.findByCodigoDocumentoTributario
                        (plantillaDTO.getCodDocumentoTributario())
                .orElseThrow(() -> new ValdiviaOCException("DTE no encontrada"));
        oc.setDocumentoTributario(dte);

        Unidad unidad = unidadRepository.findByCodigoUnidad
                        (plantillaDTO.getCodUnidad())
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + plantillaDTO.getCodUnidad()));

        oc.setIdUnidad(unidad.getIdUnidad());

        Proveedor proveedor = proveedorRepository.findByRutProveedor
                        (Utilidades.formatearRut(Utilidades.limpiarRut(plantillaDTO.getRutProveedor())))
                .orElseThrow(() -> new ValdiviaOCException("Proveedor no encontrada"));
        oc.setProveedor(proveedor);
        oc.setCodigoGiroProveedor(plantillaDTO.getCodGiroSeleccionado());
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

        String observacionStatus = "Status Pendiente de Autorizacion para la orden "+saved.getCodigoOrdenCompra() +" para el usuarioSolicitante "+ usuarioSolicitante.getUsername();

        EstadoOc estadoOc = registrarStatus(saved, STATUS_PENDIENTE, usuarioSolicitante.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    // ─── 3. Devolver (Borrador — estado 1, acción supervisor) ─
    public PlantillaDTO devolver(String codOCdevolver, PlantillaDTO plantillaDTO, String usernameSupervisor, String codUnidadSupervisor) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(plantillaDTO);
        log.info("codOCdevolver {} | usernameSolicitante {} | codUnidadSupervisor {} | plantilla  {}", codOCdevolver, usernameSupervisor,codUnidadSupervisor, jsonString);

        if(!codOCdevolver.equalsIgnoreCase(plantillaDTO.getCodOrdenCompra())){
            new ValdiviaOCException("OC "+codOCdevolver +"no valida");
        }

        Unidad unidadSupervisor = unidadRepository.findByCodigoUnidad
                        (codUnidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + codUnidadSupervisor));

        Usuarios usuarioSup = usuariosRepository.findByUsernameAndIdUnidad(usernameSupervisor, unidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usernameSupervisor));

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCdevolver)
                .orElseThrow(() -> new ValdiviaOCException("OC "+codOCdevolver +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 1);

        //validarUnidadSupervisor(oc.getIdUsuario(), usuarioSup);
        // actualizar datos de plantilla
        //OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Devolver/Borrador para la orden "+oc.getCodigoOrdenCompra() +" para el usuario "+ usernameSupervisor;
        EstadoOc estadoOc = registrarStatus(oc, STATUS_BORRADOR, usuarioSup.getIdUsuario(), observacionStatus);

        plantillaDTO = convertToDTO(oc);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());

        //Enviar Correo hacia supervisor desde valdivia...
        return plantillaDTO;


        //Con la OC obtenida, buscar estado actual
    }

    // ─── 4. Autorizar (estado 3) ─────────────────────────────
    public PlantillaDTO autorizar(String codOCautorizar, PlantillaDTO plantillaDTO, String usernameSupervisor, String codUnidadSupervisor)  {

        log.info("codOCautorizar {} | usernameSolicitante {} | codUnidadSupervisor {}", codOCautorizar, usernameSupervisor,codUnidadSupervisor);

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCautorizar)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCautorizar +" no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 3);


        Unidad unidadSupervisor = unidadRepository.findByCodigoUnidad
                        (codUnidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + codUnidadSupervisor));
        //oc.setIdUnidad(unidad.getIdUnidad());

        Usuarios usuarioAutorizador = usuariosRepository.findByUsernameAndIdUnidad(usernameSupervisor, unidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usernameSupervisor));

        oc.setIdUsuarioAutorizador(usuarioAutorizador.getIdUsuario());

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Autorizada para la orden "+oc.getCodigoOrdenCompra() +" por el usuario "+ usernameSupervisor;
        EstadoOc estadoOc = registrarStatus(saved, STATUS_AUTORIZADO, usuarioAutorizador.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    // ─── 6. Confirmar (estado 5 — bloquea la OC) ────────────
    public PlantillaDTO confirmar(String codOCconfirmar, PlantillaDTO plantillaDTO,  String usernameSupervisor, String codUnidadSupervisor) throws Exception {

        log.info("codOCconfirmar {} | usernameSolicitante {} | codUnidadSupervisor {}", codOCconfirmar, usernameSupervisor,codUnidadSupervisor);

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCconfirmar +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 5);

        // actualizar datos de plantilla
        /***Usuarios usuarioPlantilla = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuarioConfirmador())
         .orElseThrow(() -> new ValdiviaOCException("Usuario plantilla no encontrado: " + plantillaDTO.getUsernameUsuarioConfirmador()));***/
        // obtener Usuarios Supervisor
        // obtener Usuarios Supervisor
        Unidad unidadSupervisor = unidadRepository.findByCodigoUnidad
                        (codUnidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + codUnidadSupervisor));

        Usuarios usuarioSolicitante = usuariosRepository.findById(oc.getIdUsuarioSolicitante())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getProveedor().getIdProveedor()));

        Usuarios usuarioAutorizador = usuariosRepository.findById(oc.getIdUsuarioAutorizador())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getIdUsuarioAutorizador()));

        /***Usuarios usuarioConfirmador = usuariosRepository.findById(oc.getIdUsuarioConfirmador())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getIdUsuarioConfirmador()));***/

        Usuarios usuarioConfirmador = usuariosRepository.findByUsernameAndIdUnidad(usernameSupervisor, unidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usernameSupervisor));

        oc.setIdUsuarioConfirmador(usuarioConfirmador.getIdUsuario());
        Proveedor proveedor = proveedorRepository.findById(oc.getProveedor().getIdProveedor())
                .orElseThrow(() -> new ValdiviaOCException("Proveedor no encontrado: " + oc.getProveedor().getIdProveedor()));
        //validarUnidadSupervisor(usuarioPlantilla, usuarioSup);
        validarNoSeBloqueada(oc);
        //validarUnidadSupervisor(oc.getUsuario(), usuarioSup);
        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Confirmada para la orden "+oc.getCodigoOrdenCompra() +" por el usuario "+ usernameSupervisor;
        EstadoOc estadoOc = registrarStatus(saved, STATUS_CONFIRMADO, usuarioConfirmador.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());

        DocumentoOrdenCompra documentoOrdenCompra = generarOrdenCompraConfirmada(plantillaDTO.getCodOrdenCompra());

        String filename = "CONFIRMADA_" + oc.getCodigoOrdenCompra() + ".pdf";
        //
        mailComponent.enviarCorreoResend("Confirmacion",
                proveedor, usuarioSolicitante, usuarioAutorizador, usuarioConfirmador, documentoOrdenCompra.getDocByte(), filename);

        return plantillaDTO;
    }

    // ─── 5. Anular (estado 4 — bloquea la OC) ───────────────
    public PlantillaDTO anular(String codOCanular, PlantillaDTO plantillaDTO,  String usernameSupervisor, String codUnidadSupervisor) throws Exception {

        log.info("codOCanular {} | usernameSolicitante {} | codUnidadSupervisor {}", codOCanular, usernameSupervisor,codUnidadSupervisor);

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOCanular)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOCanular +"no encontrada"));

        validarNoSeBloqueada(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Pendiente
        validarTransicionEstado(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(), 4);

        // actualizar datos de plantilla
        /***Usuarios usuarioPlantilla = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuarioAnulador())
                .orElseThrow(() -> new ValdiviaOCException("Usuario plantilla no encontrado: " + plantillaDTO.getUsernameUsuarioAnulador()));***/

        // obtener Usuarios Supervisor
        Unidad unidadSupervisor = unidadRepository.findByCodigoUnidad
                        (codUnidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + codUnidadSupervisor));

        Proveedor proveedor = proveedorRepository.findById(oc.getProveedor().getIdProveedor())
                .orElseThrow(() -> new ValdiviaOCException("Proveedor no encontrado: " + oc.getProveedor().getIdProveedor()));

        Usuarios usuarioSolicitante = usuariosRepository.findById(oc.getIdUsuarioSolicitante())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getProveedor().getIdProveedor()));

        Usuarios usuarioAutorizador = usuariosRepository.findById(oc.getIdUsuarioAutorizador())
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getProveedor().getIdProveedor()));

        //Usuarios usuarioAnulador = usuariosRepository.findById(oc.getIdUsuarioConfirmador())
        //        .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + oc.getProveedor().getIdProveedor()));

        Usuarios usuarioAnulador = usuariosRepository.findByUsernameAndIdUnidad(usernameSupervisor, unidadSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usernameSupervisor));

        oc.setIdUsuarioAnulador(usuarioAnulador.getIdUsuario());

        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Anular para la orden "+oc.getCodigoOrdenCompra() +" por el usuario "+ usernameSupervisor;
        EstadoOc estadoOc = registrarStatus(saved, STATUS_ANULADO, usuarioAnulador.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());

        DocumentoOrdenCompra documentoOrdenCompra =
                generarOrdenCompraAnulada(plantillaDTO.getCodOrdenCompra());

        String filename = "ANULADA_" + oc.getCodigoOrdenCompra() + ".pdf";

        mailComponent.enviarCorreoResend("Anulación", proveedor, usuarioSolicitante, usuarioAutorizador , usuarioAnulador,
                 documentoOrdenCompra.getDocByte(), filename);

        return plantillaDTO;
    }

//        DocumentoOrdenCompra documentoOrdenCompra =  generarDocumentoOc(plantillaDTO.getCodOrdenCompra(), plantillaDTO);

    //      String filename = "ANULADA_" + oc.getCodigoOrdenCompra() + ".pdf";

//        mailComponent.enviarCorreoResend("Anulación", optProveedor.getEmailProveedor(), documentoOrdenCompra.getDocByte(), filename);

//        return plantillaDTO;
//    }

    public DocumentoOrdenCompra generarDocumentoOc(String codOC, PlantillaDTO plantillaDTO) throws Exception {
        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOC +" no encontrada"));

        Optional<OrdenCompraConfirmada> optOcc = ordenCompraConfirmadaRepository.findByIdOrdenCompra(oc);

        if(optOcc.isPresent()) {
            Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());

            String html = generarOrdenCompraHtml(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(),
                    optOcc.get());

            return new DocumentoOrdenCompra(optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(), pdfComponent.generarPdffromHtml(html));
        }
        return null;
    }


    public DocumentoOrdenCompra generarOrdenCompraConfirmada(String codOrdenCompra) throws Exception {

        OrdenCompraConfirmada ordenCompraConfirmada = null;

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOrdenCompra)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOrdenCompra +" no encontrada"));

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());

        String html = "";
        if(optStatusOrdenCompraActual.isPresent() && (
                optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 4L ||
                        optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 5 )) {
            //solo hay una OC con ese codigo
            Page< PlantillaStatusDTO > pgPlantillaStatus = realizarBusquedaAvanzada(
                    optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(), oc.getProveedor().getRutProveedor(),
                    null, oc.getCodigoOrdenCompra(), null, null
                    , null, null,
                    PageRequest.of(0, 1));


            Usuarios usuarioCreador  = null;
            Usuarios usuarioSolicitante  = null;
            Usuarios usuarioAutorizador  = null;
            Usuarios usuarioAnulador  = null;
            Usuarios usuarioConfirmador  = null;

            PlantillaStatusImpresionDTO plantillaStatusImpresionDTO = new PlantillaStatusImpresionDTO(pgPlantillaStatus.getContent().get(0));

            NumberFormat formaterSoloPuntos = NumberFormat.getInstance(new Locale("es", "CL"));

            ordenCompraConfirmada =
                    OrdenCompraConfirmada.builder().
                            idOrdenCompra(oc).
                            codigoOrdenCompra(plantillaStatusImpresionDTO.getCodOrdenCompra()).
                            fechaEmision(plantillaStatusImpresionDTO.getFechaOrdenCompra()).
                            nombreDocumentoTributario(plantillaStatusImpresionDTO.getDescripcionDocumentoElectronico()).
                            codigoDocumentoTributario(plantillaStatusImpresionDTO.getDescripcionDocumentoElectronico()).
                            descripcionDocumentoTributario(plantillaStatusImpresionDTO.getDescripcionDocumentoElectronico()).
                            descripcionDocumentoTributario(plantillaStatusImpresionDTO.getImpuestoDocumentoElectronico()).
                            nombreUnidad(plantillaStatusImpresionDTO.getNombreUnidad().toUpperCase()).
                            rutProveedor(plantillaStatusImpresionDTO.getRutProveedor().toUpperCase()).
                            nombreProveedor(plantillaStatusImpresionDTO.getNombreProveedor().toUpperCase()).
                            razonSocialProveedor(plantillaStatusImpresionDTO.getRazonSocialProveedor().toUpperCase()).
                            direccionProveedor(plantillaStatusImpresionDTO.getDireccionProveedor().toUpperCase()).
                            emailProveedor(plantillaStatusImpresionDTO.getEmailProveedor().toUpperCase()).
                            codigoGiroProveedor(plantillaStatusImpresionDTO.getCodGiroSeleccionado()).
                            nombreGiroProveedor(plantillaStatusImpresionDTO.getGiroProveedor()).

                            nombreOrdenCompra(plantillaStatusImpresionDTO.getNombreOrdenCompra()).
                            observaciones(plantillaStatusImpresionDTO.getObservaciones()).
                            listProductosOrden(plantillaStatusImpresionDTO.getListProductosOrden()).


                            totalNeto(formaterSoloPuntos.format(plantillaStatusImpresionDTO.getTotalNeto())).
                            impuesto(formaterSoloPuntos.format(plantillaStatusImpresionDTO.getImpuesto())).
                            total(formaterSoloPuntos.format(plantillaStatusImpresionDTO.getTotal())).
                            anulada(false).
                            build();

            if(oc.getIdUsuarioCreador()!=null){
                usuarioCreador  = usuariosRepository.findById(oc.getIdUsuarioCreador()).get();
                ordenCompraConfirmada.setNombreUsuarioCreador(usuarioCreador.getNombreMember().concat(" ").concat(usuarioCreador.getApellidoPaternoMember()));
            }

            if(oc.getIdUsuarioSolicitante()!=null){
                usuarioSolicitante  = usuariosRepository.findById(oc.getIdUsuarioSolicitante()).get();
                ordenCompraConfirmada.setNombreUsuarioSolicitante(usuarioSolicitante.getNombreMember().concat(" ").concat(usuarioSolicitante.getApellidoPaternoMember()));
            }

            if(oc.getIdUsuarioAutorizador()!=null){
                usuarioAutorizador  = usuariosRepository.findById(oc.getIdUsuarioAutorizador()).get();
                ordenCompraConfirmada.setNombreUsuarioAutorizador(usuarioAutorizador.getNombreMember().concat(" ").concat(usuarioAutorizador.getApellidoPaternoMember()));
            }

            if(oc.getIdUsuarioConfirmador()!=null){
                usuarioConfirmador  = usuariosRepository.findById(oc.getIdUsuarioConfirmador()).get();
                ordenCompraConfirmada.setNombreUsuarioConfirmador(usuarioConfirmador.getNombreMember().concat(" ").concat(usuarioConfirmador.getApellidoPaternoMember()));
            }

            if(oc.getProveedor()!=null){

                ordenCompraConfirmada.setTelefonoContactoProveedor(oc.getProveedor().getTelefonoContactoProveedor());

                Optional<Comunas> optComunas = comunasRepository.findById(oc.getProveedor().getIdComuna());
                Optional<Regiones> optRegiones = regionesRepository.findById(optComunas.get().getRegion().getIdRegion());

                ordenCompraConfirmada.setNombreRegionProveedor(optRegiones.get().getNombreRegion().toUpperCase());
                ordenCompraConfirmada.setNombreComunaProveedor(optComunas.get().getNombreComuna().toUpperCase());
            }
            /**Aqui Guardar en el Orden de Compra Confirmada****/
            ordenCompraConfirmada = ordenCompraConfirmadaRepository.save(ordenCompraConfirmada);
            /**Aqui Guardar en el Orden de Compra Confirmada****/
            html = generarOrdenCompraHtml(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(),
                    ordenCompraConfirmada);

        }

        return new DocumentoOrdenCompra(optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(), pdfComponent.generarPdffromHtml(html));
    }

    public DocumentoOrdenCompra generarOrdenCompraAnulada(String codOrdenCompra) throws Exception {

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(codOrdenCompra)
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOrdenCompra +" no encontrada"));

        Optional<OrdenCompraConfirmada> optOcc = ordenCompraConfirmadaRepository.findByIdOrdenCompra(oc);

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());
        //Estado Anulado
        if(optOcc.isPresent() && optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc()== 4) {
            OrdenCompraConfirmada updOcc =  optOcc.get();
            updOcc.setAnulada(true);
            updOcc.setFechaAnulacion("");

            if(oc.getIdUsuarioAnulador()!=null){
                Usuarios usuarioAnulador  = usuariosRepository.findById(oc.getIdUsuarioAnulador()).get();
                updOcc.setNombreUsuarioAnulador(usuarioAnulador.getNombreMember().concat(" ").concat(usuarioAnulador.getApellidoPaternoMember()));
            }

            ordenCompraConfirmadaRepository.save(updOcc);

            String html = generarOrdenCompraHtml(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc(),
                    optOcc.get());

            return new DocumentoOrdenCompra(optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(),
                    pdfComponent.generarPdffromHtml(html));
        }
        return null;



    }







    private String  generarOrdenCompraHtml(Integer estadoOc, OrdenCompraConfirmada ordenCompraConfirmada) throws Exception {
        //Tres Logos Firmas
        String tipo = null;
        String html = null;
        String plantilla = null;
        if(estadoOc == 5 ){
            tipo = "1";
            plantilla = apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                    apiProperties.getArchivoHtmlNombreHtmlConfirmada());
        }
        else if(estadoOc == 4L){
            tipo = "2";
            plantilla = apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                    apiProperties.getArchivoHtmlNombreHtmlAnulada());

        }

        List<String> logos = Arrays.asList(
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoEscudoColor()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoTimbreContabilidad()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoTimbreGerencia())
        );

        ClassPathResource imgFileEsc = new ClassPathResource(logos.get(0)); //bci
        ClassPathResource imgFileContabilidad = new ClassPathResource(logos.get(1)); //contabilidad
        ClassPathResource imgFileGerencia = new ClassPathResource(logos.get(2)); //gerencia

        byte[] imageBytesEsc;
        try (InputStream is = imgFileEsc.getInputStream()) {
            imageBytesEsc = is.readAllBytes();
        }

        byte[] imageBytesContabilidad;
        try (InputStream is = imgFileContabilidad.getInputStream()) {
            imageBytesContabilidad = is.readAllBytes();
        }

        byte[] imageBytesGerencia;
        try (InputStream is = imgFileGerencia.getInputStream()) {
            imageBytesGerencia = is.readAllBytes();
        }

        String base64Esc = Base64.getEncoder().encodeToString(imageBytesEsc);
        String base64Contabilidad = Base64.getEncoder().encodeToString(imageBytesContabilidad);
        String base64Gerencia = Base64.getEncoder().encodeToString(imageBytesGerencia);

        /***Cargar una vez el String de html**/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(
                plantilla);

        OrdenCompraHtml ordenCompraHtml = new OrdenCompraHtml(tipo, htmlIndividual, Arrays.asList(base64Esc, base64Contabilidad, base64Gerencia));

        html = PlantillaOrdenCompra.generarPlantilla(ordenCompraHtml,
                ordenCompraConfirmada);

        return html;
    }

    private void validarTransicionEstado(Integer estadoActual, Integer estadoNuevo) {
        boolean esValido = false;

        // Si el estado actual es nulo (orden nueva), debe comenzar en Borrador (1)
        if (estadoActual == null) {
            if (estadoNuevo == 1) return;
            throw new RuntimeException("Una orden nueva debe comenzar en estado Borrador.");
        }

        switch (estadoActual) {
            case 1: // Borrador
                // Puede mantenerse en Borrador (1) o pasar a Pendiente Autorización (2)
                if (estadoNuevo == 1 || estadoNuevo == 2) esValido = true;
                break;

            case 2: // Pendiente Autorización
                // Puede volver a Borrador (1) o avanzar a Autorizado (3)
                if (estadoNuevo == 1 || estadoNuevo == 3) esValido = true;
                break;

            case 3: // Autorizado
                // Puede volver a Borrador (1) o avanzar a Confirmada (5)
                if (estadoNuevo == 1 || estadoNuevo == 5) esValido = true;
                break;

            case 4: // Anulado
                // Estado final absoluto: no permite más transiciones
                esValido = false;
                break;

            case 5: // Confirmada
                // Desde Confirmada ahora se puede pasar a Anulado (4)
                if (estadoNuevo == 4) esValido = true;
                break;

            default:
                throw new RuntimeException("Estado actual no reconocido: " + estadoActual);
        }

        if (!esValido) {
            throw new RuntimeException("Transición no permitida: de estado " + estadoActual + " a " + estadoNuevo);
        }
    }

    public Page<PlantillaStatusDTO> realizarBusquedaAvanzada(
            String codEstadoOc, String rut,
            String codUnidad, String codOrdenCompra, String fechaInicioStr, String fechaFinStr,
            String rangoInicio, String rangoFin, // Parámetros recibidos en el controlador
            Pageable pageable) {

        String rutParaQuery = rut;
        Integer unidadParaQuery = null;
        Integer idStatus = null;
        LocalDate fecInicio = (fechaInicioStr != null && !fechaInicioStr.isEmpty()) ? LocalDate.parse(fechaInicioStr) : null;
        LocalDate fecFin = (fechaFinStr != null && !fechaFinStr.isEmpty()) ? LocalDate.parse(fechaFinStr) : null;

        // 🌟 DECLARACIÓN DE VARIABLES PARA EL QUERY (Inicializadas en null)
        Long ocInicio = null;
        Long ocFin = null;

        // 🌟 VALIDACIÓN Y ASIGNACIÓN EN PAREJA OBLIGATORIA
        if (rangoInicio != null && !rangoInicio.isEmpty() && rangoFin != null && !rangoFin.isEmpty()) {
            try {
                ocInicio = Long.parseLong(rangoInicio.trim());
                ocFin = Long.parseLong(rangoFin.trim());
                System.out.println("🔍 [Filtro Rango] Aplicando rango de IDs desde: " + ocInicio + " hasta: " + ocFin);
            } catch (NumberFormatException e) {
                // Manejo preventivo si envían caracteres no numéricos en los inputs de rango
                System.err.println("❌ [Filtro Rango] Los valores de rango introducidos no son numéricos válidos: "
                        + rangoInicio + " - " + rangoFin);
                ocInicio = null;
                ocFin = null;
            }
        } else {
            System.out.println("ℹ️ [Filtro Rango] Rango incompleto u omitido. Se ignorará en la base de datos.");
        }

        if (codEstadoOc != null && !codEstadoOc.isEmpty()) {
            idStatus = estadoOcRepository.findByCodigoEstadoOc(codEstadoOc)
                    .orElseThrow(() -> new IllegalStateException("Status no encontrado: " + codEstadoOc)).getIdEstadoOc();
        }

        if (rut != null && !rut.isEmpty()) {
            rutParaQuery = rut.trim().replaceAll("", "%");
        }

        if (codUnidad != null && !codUnidad.isEmpty()) {
            Optional<Unidad> optUnidad = unidadRepository.findByCodigoUnidad(codUnidad);
            if (optUnidad.isPresent()) {
                unidadParaQuery = optUnidad.get().getIdUnidad();
            }
        }

        // 🌟 1. Ejecutar la búsqueda en el repositorio pasando ocInicio y ocFin
        Page<OrdenCompra> ordenes = ocRepo.buscarAvanzado(
                idStatus, rutParaQuery, unidadParaQuery, codOrdenCompra, fecInicio, fecFin,
                ocInicio, ocFin, // Se acoplan perfectamente con :idMin e :idMax de tu @Query
                pageable
        );
        /***buscarYFiltrar(ordenes, idStatus, rutParaQuery, nombreProv,
         unidadId, codigo, fecha);***/
        // 2. Transformar la página de Entidades a página de DTOs
        return ordenes.map(this::convertToStatusDTO);
    }

    /***Pasar las Respuesta de la busqueda de registros a Excel**/
    public void exportarOrdenesCompraExcel(
            String codEstadoOc, String rut, String codUnidad, String codOrdenCompra,
            String fechaInicioStr, String fechaFinStr, String rangoInicio, String rangoFin,
            HttpServletResponse response) throws IOException {

        // 1. Obtener TODOS los registros filtrados sin límite de paginación
        // Pasamos Pageable.unpaged() para ignorar el límite de tamaño y traernos todo el set
        Page<PlantillaStatusDTO> paginaResultados = realizarBusquedaAvanzada(
                codEstadoOc, rut, codUnidad, codOrdenCompra, fechaInicioStr, fechaFinStr,
                rangoInicio, rangoFin, // Parámetros recibidos en el controlador
                Pageable.unpaged()
        );

        List<PlantillaStatusDTO> listaOc = paginaResultados.getContent();

        // 2. Crear el libro de trabajo de Excel (Workbook)
        try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Órdenes de Compra CCM");

            // Estilos: Cabecera Corporativa (Fondo Cyan Suave, Texto Negrita)
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            // Color de fondo aproximado al Cyan CCM institucional (#4cb0c0)
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo para celdas de montos financieros
            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            moneyStyle.setDataFormat(format.getFormat("$#,##0"));

            // 3. Crear Fila de Títulos (Cabecera)
            String[] columnas = {"FOLIO", "FECHA EMISIÓN", "RUT PROVEEDOR", "RAZÓN SOCIAL", "UNIDAD REQUIRIENTE", "MONTO TOTAL", "ESTADO ACTUAL"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Llenar el Excel con los datos de las OCs extraídas
            int rowNum = 1;
            for (PlantillaStatusDTO oc : listaOc) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(oc.getCodOrdenCompra() != null ? oc.getCodOrdenCompra() : "—");
                row.createCell(1).setCellValue(oc.getFechaOrdenCompra() != null ? oc.getFechaOrdenCompra().toString() : "—");
                row.createCell(2).setCellValue(oc.getRutProveedor() != null ? oc.getRutProveedor() : "—");
                row.createCell(3).setCellValue(oc.getNombreProveedor() != null ? oc.getNombreProveedor().toUpperCase() : "—");
                row.createCell(4).setCellValue(oc.getNombreUnidad() != null ? oc.getNombreUnidad().toUpperCase() : "—");

                // Celda de Monto con formato numérico
                Cell montoCell = row.createCell(5);
                montoCell.setCellValue(oc.getTotal() != null ? oc.getTotal().doubleValue() : 0.0);
                montoCell.setCellStyle(moneyStyle);

                row.createCell(6).setCellValue(oc.getEstadoActualOc() != null ? oc.getEstadoActualOc().toUpperCase() : "—");
            }

            // Autoajustar el ancho de las columnas según su contenido para evitar textos truncados
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 5. Escribir el flujo binario directamente al cuerpo de la respuesta HTTP
            workbook.write(response.getOutputStream());
        }
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

        if (oc.getIdUsuarioCreador() != null) {
            Optional<Usuarios> optUsuarioCreador = usuariosRepository.findById(oc.getIdUsuarioCreador());
            if (optUsuarioCreador.isPresent()) {
                dto.setNombreUsuarioCreador(optUsuarioCreador.get().getNombreMember());
                dto.setApellidoUsuarioCreador(optUsuarioCreador.get().getApellidoPaternoMember());
            }
        }

        if (oc.getIdUsuarioSolicitante() != null) {
            Optional<Usuarios> optUsuarioSolicitante = usuariosRepository.findById(oc.getIdUsuarioSolicitante());
            if (optUsuarioSolicitante.isPresent()) {
                dto.setNombreUsuarioSolicitante(optUsuarioSolicitante.get().getNombreMember());
                dto.setApellidoUsuarioSolicitante(optUsuarioSolicitante.get().getApellidoPaternoMember());
            }
        }

        if (oc.getIdUsuarioAutorizador() != null) {
            Optional<Usuarios> optUsuarioAutorizador = usuariosRepository.findById(oc.getIdUsuarioAutorizador());
            if (optUsuarioAutorizador.isPresent()) {
                dto.setNombreUsuarioAutorizador(optUsuarioAutorizador.get().getNombreMember());
                dto.setApellidoUsuarioAutorizador(optUsuarioAutorizador.get().getApellidoPaternoMember());
            }
        }

        if (oc.getIdUsuarioAnulador() != null) {
            Optional<Usuarios> optUsuarioAnulador = usuariosRepository.findById(oc.getIdUsuarioAnulador());
            if (optUsuarioAnulador.isPresent()) {
                dto.setNombreUsuarioAutorizador(optUsuarioAnulador.get().getNombreMember());
                dto.setApellidoUsuarioAutorizador(optUsuarioAnulador.get().getApellidoPaternoMember());
            }
        }

        if (oc.getIdUsuarioConfirmador() != null) {
            Optional<Usuarios> optUsuarioConfirmador = usuariosRepository.findById(oc.getIdUsuarioConfirmador());
            if (optUsuarioConfirmador.isPresent()) {
                dto.setNombreUsuarioConfirmador(optUsuarioConfirmador.get().getNombreMember());
                dto.setApellidoUsuarioConfirmador(optUsuarioConfirmador.get().getApellidoPaternoMember());
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

            Optional<Comunas> optComunas = comunasRepository.findById(oc.getProveedor().getIdComuna());
            Optional<Regiones> optRegiones = regionesRepository.findById(optComunas.get().getRegion().getIdRegion());

            dto.setCodRegionProveedor(optRegiones.get().getCodigoRegion());
            dto.setCodComunaProveedor(optComunas.get().getCodigoComuna());
            dto.setNombreRegionProveedor(optRegiones.get().getNombreRegion());
            dto.setNombreComunaProveedor(optComunas.get().getNombreComuna());


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
    /******************************************************************************************************/
    /******************************************************************************************************/
    /******************************************************************************************************/
    public Map<String, String> calcularPeriodos(Integer periodos) {
        // 1. Obtener la fecha de hoy
        LocalDateTime ahora = LocalDateTime.now();

        // 2. Retroceder la cantidad de meses indicada
        // .withDayOfMonth(1) asegura que nos paramos en el PRIMER día de ese mes objetivo
        LocalDateTime fechaInicio = ahora.minusMonths(periodos).withDayOfMonth(1).with(LocalTime.MIN);

        // 3. Definir el fin del rango (usamos el final del mes actual con el último segundo del día)
        YearMonth mesActual = YearMonth.from(ahora);
        LocalDateTime fechaFin = mesActual.atEndOfMonth().atTime(LocalTime.MAX);

        // Formateador para guardarlo como String en el Map (Formato compatible con BD)
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, String> rangoFechas = new HashMap<>();
        rangoFechas.put("fechaInicio", fechaInicio.format(formateador)); //Fecha Inferior
        rangoFechas.put("fechaFin", fechaFin.format(formateador)); //Fecha Superior

        return rangoFechas;
    }

        public byte[] obtenerGastosPorPeriodo(String fechaInicio, String fechaFin) throws Exception {
            // Validación preventiva: Si envían valores negativos, lo forzamos a null
            // para que la query nativa de MySQL traiga el histórico completo.
            Page< PlantillaStatusDTO > pgPlantillaStatus = realizarBusquedaAvanzada(
                    null,
                    null,
                    null, null, fechaInicio, fechaFin
                    , null, null,
                    Pageable.unpaged()
            );

            List<PlantillaStatusDTO> listaPlana = pgPlantillaStatus.getContent();

    // 2. Agrupamos por codUnidad y ordenamos cronológicamente (de más antiguo a más nuevo)
            Map<String, Map<String, List<PlantillaStatusDTO>>> agrupadoPorUnidadYEstado = listaPlana.stream()
                    // Ordenamos primero por el ID de la Orden de Compra de forma ascendente
                    .sorted(Comparator.comparing(PlantillaStatusDTO::getCodOrdenCompra))
                    .collect(Collectors.groupingBy(
                            PlantillaStatusDTO::getCodUnidad,
                            LinkedHashMap::new, // Preserva el orden de aparición de las Unidades
                            Collectors.groupingBy(
                                    PlantillaStatusDTO::getEstadoActualOc, // O el getter correspondiente para el estado (ej: getNomEstado / getEstado)
                                    LinkedHashMap::new, // Preserva el orden de los Estados dentro de cada unidad
                                    Collectors.toList()  // Colecciona las OC respetando el orden por ID
                            )
                    ));

            List<EstadoOc> listaEstadoOc = estadoOcRepository.findAll();

            List<Unidad> listaUnidad = unidadRepository.findAll();

            byte[] pdfReporteGastos =
                    generadorReporteService.generarPdfReporteGastos(agrupadoPorUnidadYEstado, fechaInicio, fechaFin);
            //String html = PlantillaReporteUnidadEstado. generarPlantillaReporte(agrupadoPorUnidadYEstado, listaEstadoOc, listaUnidad);
            //Generar Html..
            // Ejecución de la consulta JPA mapeada al DTO Proyección
            return pdfReporteGastos;
        }


    public AdjuntoDTO guardarAdjunto(String codigoOrdenCompra, String username, MultipartFile file) throws IOException {

        OrdenCompra oc = null;
        Usuarios usuario = null;

        if (codigoOrdenCompra != null && !codigoOrdenCompra.equals("")) {
            oc = ocRepo.findByCodigoOrdenCompra(codigoOrdenCompra)
                    .orElseThrow(() -> new ValdiviaOCException("OC " + codigoOrdenCompra + " no encontrada"));
        }

        if (username != null && !username.equals("")) {
            Optional<Usuarios> optUsuario = usuariosRepository.findByUsername(username);
            usuario = optUsuario.isPresent() ? optUsuario.get() : Usuarios.builder().build();
        }

        // 1. Definir y crear la ruta de la carpeta: public_file/{codigo_orden_compra}/
        Path rutaCarpetaOC = Paths.get(apiProperties.getArchivoCarpetaPublic(), codigoOrdenCompra).toAbsolutePath().normalize();
        Files.createDirectories(rutaCarpetaOC); // Crea las carpetas si no existen

        // 2. Usar el nombre original SIN UUID para permitir que se pise
        String nombreOriginal = file.getOriginalFilename();

        // 3. Guardar el archivo físico en el servidor (pisará el archivo si existe gracias a REPLACE_EXISTING)
        Path rutaDestinoArchivo = rutaCarpetaOC.resolve(nombreOriginal);
        Files.copy(file.getInputStream(), rutaDestinoArchivo, StandardCopyOption.REPLACE_EXISTING);
        log.info("Archivo físico copiado/remplazado en: " + rutaDestinoArchivo.toString());

        // 4. Registrar o Actualizar en la Base de Datos
        // Buscamos si ya existía un adjunto con el mismo nombre para esa OC específica
        Optional<AdjuntoOrdenCompra> adjuntoExistenteOpt = adjuntoOrdenCompraRepository
                .findByIdOrdenCompraAndNombreArchivoAndActiveTrue(oc, nombreOriginal);

        AdjuntoOrdenCompra adjuntoOrdenCompra;

        if (adjuntoExistenteOpt.isPresent()) {
            // Si ya existe, reutilizamos el registro existente para actualizar sus metadatos (pisar lógicamente)
            adjuntoOrdenCompra = adjuntoExistenteOpt.get();
            log.info("Se encontró registro previo para el archivo '" + nombreOriginal + "'. Actualizando metadatos...");
        } else {
            // Si no existe, creamos una nueva instancia
            adjuntoOrdenCompra = new AdjuntoOrdenCompra();
            adjuntoOrdenCompra.setIdOrdenCompra(oc);
            adjuntoOrdenCompra.setNombreArchivo(nombreOriginal);
            adjuntoOrdenCompra.setRutaArchivo(rutaDestinoArchivo.toString());
        }

        // Actualizamos los campos que cambian con la nueva subida
        adjuntoOrdenCompra.setTipoContenido(file.getContentType());
        adjuntoOrdenCompra.setTamanoBytes((int) file.getSize());
        if (usuario != null) {
            adjuntoOrdenCompra.setIdUsuarioSube(usuario.getIdUsuario());
        }
        adjuntoOrdenCompra.setActive(true);

        // Guarda (si era existente hace un UPDATE, si era nuevo hace un INSERT)
        adjuntoOrdenCompraRepository.save(adjuntoOrdenCompra);

        return AdjuntoDTO.builder()
                .nombreArchivo(adjuntoOrdenCompra.getNombreArchivo())
                .urlDescarga(adjuntoOrdenCompra.getRutaArchivo())
                .build();
    }

    public List<AdjuntoDTO> obtenerAdjuntosPorCodigoOrdenCompra(String codigoOrdenCompra) {
        // 1. Buscar todos los registros de adjuntos asociados a esa OC
        OrdenCompra oc = null;Usuarios usuario = null;

            oc =
                    ocRepo.findByCodigoOrdenCompra(codigoOrdenCompra)
                            .orElseThrow(() -> new ValdiviaOCException("OC "+codigoOrdenCompra +" no encontrada"));

        List<AdjuntoOrdenCompra> listaAdjuntos = adjuntoOrdenCompraRepository.findByIdOrdenCompra(oc);

        // 2. Mapear la lista de entidades a una lista de DTOs para el Front-end
        return listaAdjuntos.stream().map(adjunto -> {
            // Armamos la URL apuntando al endpoint de descarga usando el ID del adjunto
            String urlDescarga = "/api/v1/oc/ordenes-compra/download/" + adjunto.getIdAdjuntoOc();

            return new AdjuntoDTO(
                    adjunto.getNombreArchivo(),
                    urlDescarga
            );
        }).toList(); // Si usas Java 16+, de lo contrario usa .collect(Collectors.toList())
    }

    public AdjuntoOrdenCompra buscarAdjuntoPorId(Integer idAdjunto) {
        return adjuntoOrdenCompraRepository.findById(idAdjunto)
                .orElseThrow(() -> new ValdiviaOCException("Registro de adjunto no encontrado en BD"));
    }

    @Transactional
    public boolean eliminarAdjuntoPorId(Integer idAdjunto) {
        log.info("[Adjuntos] Iniciando proceso de eliminación para el ID: {}", idAdjunto);

        // 1. Reutilizar tu método para buscar el adjunto en la Base de Datos
        AdjuntoOrdenCompra adjunto = this.buscarAdjuntoPorId(idAdjunto);

        // 2. Intentar borrar el archivo físico del disco rígido
        try {
            // Combinamos la ruta base configurada en apiProperties con la sub-ruta del registro
            Path rutaArchivoFisico = Paths.get(apiProperties.getArchivoCarpetaPublic(), adjunto.getRutaArchivo())
                    .toAbsolutePath()
                    .normalize();

            if (Files.exists(rutaArchivoFisico)) {
                Files.delete(rutaArchivoFisico);
                log.info("[Adjuntos] Archivo físico eliminado con éxito del servidor: {}", rutaArchivoFisico);
            } else {
                log.warn("[Adjuntos] El archivo físico no existía en la ruta esperada: {}", rutaArchivoFisico);
            }
        } catch (IOException e) {
            // Logeamos la falla como advertencia para que no interrumpa la limpieza de la Base de Datos
            // (Por si el archivo fue movido o borrado manualmente en el servidor)
            log.error("[Adjuntos] Error al intentar eliminar el archivo físico del disco: {}", e.getMessage());
        }

        // 3. Eliminar el registro lógicamente/físicamente de la Base de Datos
        adjuntoOrdenCompraRepository.delete(adjunto);

        log.info("[Adjuntos] Registro de adjunto ID {} eliminado exitosamente de la base de datos.", idAdjunto);

        return true;
    }

    public InfoDescargaArchivoDTO prepararDescargaUniversal(String subPath, String xForwardedFor) throws IOException {

        AdjuntoOrdenCompra adjuntoOrdenCompra = buscarAdjuntoPorId(Integer.parseInt(subPath));
        // 1. Resolver la ruta absoluta en el disco del servidor
        Path filePath = Paths.get(adjuntoOrdenCompra.getRutaArchivo()).toAbsolutePath().normalize();

        // 2. Validar la existencia del archivo físico (Lanzamos excepción si falla)
        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            throw new FileNotFoundException("El archivo solicitado no existe o no se puede leer: " + subPath);
        }

        // 3. Obtener metadatos del archivo
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        String fileName = filePath.getFileName().toString();
        String nginxInternalUrl = null;

        // 4. Evaluar si se delega a Nginx o se prepara el recurso local
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 🔥 PRODUCCIÓN (Construimos la URL interna de Nginx)
            String rootPathStr = apiProperties.getArchivoCarpetaPublic();
            String relativePath = filePath.toString().replace(rootPathStr, "");

            nginxInternalUrl = "/valdiviaoc/internal-files/" + relativePath.replace("\\", "/").replaceAll("^/+", "");

            return new InfoDescargaArchivoDTO(null, contentType, fileName, nginxInternalUrl);
        } else {
            // 💻 DESARROLLO LOCAL (Cargamos el recurso físico en memoria controlada por Spring)
            Resource resource = new UrlResource(filePath.toUri());
            return new InfoDescargaArchivoDTO(resource, contentType, fileName, null);
        }
    }

    public void limpiarCarpetaOrdenCompra(String codigoOrdenCompra){
        try{
            Path rutaCarpetaOC = Paths.get(apiProperties.getArchivoCarpetaPublic(), codigoOrdenCompra).toAbsolutePath().normalize();
            if (Files.exists(rutaCarpetaOC)) {
                log.info("[Adjuntos] Limpiando carpeta existente para la OC: {}", codigoOrdenCompra);

                // Caminamos por el directorio. El filtro evita que la carpeta raíz se borre a sí misma
                Files.walk(rutaCarpetaOC)
                        .filter(path -> !path.equals(rutaCarpetaOC))
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (file.delete()) {
                                log.debug("[Adjuntos] Archivo temporal eliminado: {}", file.getName());
                            } else {
                                log.warn("[Adjuntos] No se pudo eliminar el archivo: {}", file.getName());
                            }
                        });
            } else {
                // 3. Si no existe (es una OC nueva sin borradores previos), la creamos
                Files.createDirectories(rutaCarpetaOC);
            }
        } catch (IOException e) {
            log.info("Error al Crear Carpeta de OC: " + e.getMessage());
        }
    }

}