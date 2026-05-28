package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.application.service.component.PdfComponent;
import com.ragnax.valdivia.ordencompra.application.service.model.DocumentoOrdenCompra;
import com.ragnax.valdivia.ordencompra.application.service.model.OrdenCompraHtml;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.PlantillaCargar;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.PlantillaOrdenCompra;
import com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades;
import com.ragnax.valdivia.ordencompra.infraestructura.configuration.ApiProperties;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.*;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.*;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.GiroSii;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.*;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.AdjuntoOrdenCompraRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.GiroSiiRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UsuariosRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.ragnax.valdivia.ordencompra.application.service.utilidades.Utilidades.generarCodigo;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrdenCompraService {

    private final ApiProperties apiProperties;
    private final PdfComponent pdfComponent;

    private final OrdenCompraRepository ocRepo;
    private final EstadoOcRepository estadoOcRepository;
    private final DocumentoTributarioRepository documentoTributarioRepository;
    private final GiroSiiRepository giroSiiRepository;
    private final ProveedorRepository proveedorRepository;
    private final StatusOrdenCompraRepository statusOrdenCompraRepository;
    private final UnidadRepository unidadRepository;
    private final UsuariosRepository usuariosRepository;
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
        optStatusOrdenCompra =
                statusOrdenCompraRepository.findByOrdenCompraAndEstadoOc(ordenCompra, EstadoOc.builder().idEstadoOc(5).build());

        if (optStatusOrdenCompra.isPresent()) {
            throw new IllegalStateException("La orden de compra está confirmada y no puede ser modificada.");
        }
        return true;
    }

    private boolean validarUnidadSupervisor(Usuarios usuarioPlantilla, Usuarios usuarioSupervisor) {

     if(usuarioPlantilla.getIdUnidad().getCodigoUnidad().equals(usuarioSupervisor.getIdUnidad().getCodigoUnidad())) {
        return true;
     }
     throw new IllegalStateException("La orden de compra no puede ser considerada por este supervisor."+ usuarioSupervisor.getUsername());
     }

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
        //Borrador
        registrarStatus(savedConCodigo, STATUS_BORRADOR, savedConCodigo.getIdUsuario(), observacionStatus);

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
        EstadoOc estadoOc = registrarStatus(saved, STATUS_BORRADOR, usuario.getIdUsuario(), observacionStatus);

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

        EstadoOc estadoOc = registrarStatus(saved, STATUS_PENDIENTE, usuario.getIdUsuario(), observacionStatus);
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
            EstadoOc estadoOc = registrarStatus(oc, STATUS_BORRADOR, usuarioSup.getIdUsuario(), observacionStatus);


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

        // obtener Usuarios OC
        Usuarios usuarioPlantilla = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario())
                .orElseThrow(() -> new ValdiviaOCException("Usuario plantilla no encontrado: " + plantillaDTO.getUsernameUsuario()));
        // obtener Usuarios Supervisor
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        validarUnidadSupervisor(usuarioPlantilla, usuarioSup);
        oc.setUsernameAutorizador(usuarioSup.getUsername());
        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSupervisor +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Autorizada";
        EstadoOc estadoOc = registrarStatus(saved, STATUS_AUTORIZADO, usuarioSup.getIdUsuario(), observacionStatus);
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
        Usuarios usuarioPlantilla = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario())
                .orElseThrow(() -> new ValdiviaOCException("Usuario plantilla no encontrado: " + plantillaDTO.getUsernameUsuario()));
        // obtener Usuarios Supervisor
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        validarUnidadSupervisor(usuarioPlantilla, usuarioSup);
        oc.setUsernameAnulador(usuarioSup.getUsername());
        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSupervisor +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Autorizada";
        EstadoOc estadoOc = registrarStatus(saved, STATUS_ANULADO, usuarioSup.getIdUsuario(), observacionStatus);
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
        Usuarios usuarioPlantilla = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario())
                .orElseThrow(() -> new ValdiviaOCException("Usuario plantilla no encontrado: " + plantillaDTO.getUsernameUsuario()));
        // obtener Usuarios Supervisor
        Usuarios usuarioSup = usuariosRepository.findByUsername(usuarioSupervisor)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + usuarioSupervisor));

        validarUnidadSupervisor(usuarioPlantilla, usuarioSup);

        validarNoSeBloqueada(oc);
        //validarUnidadSupervisor(oc.getUsuario(), usuarioSup);
        oc.setUsernameConfirmador(usuarioSup.getUsername());
        OrdenCompra saved = ocRepo.save(oc);

        String observacionStatus = "Status Generado por el usuario "+ usuarioSup.getUsername() +" para la orden "+saved.getCodigoOrdenCompra() + " En estado Confirmada";
        EstadoOc estadoOc = registrarStatus(saved, STATUS_CONFIRMADO, usuarioSup.getIdUsuario(), observacionStatus);
        plantillaDTO.setEstadoActualOc(estadoOc.getNombreEstadoOc());
        return plantillaDTO;
    }

    private OrdenCompraHtml generarOrdenCompraHtml() throws Exception {
        //Tres Logos Escudo, Firma y BCIx2
            List<String> logos = Arrays.asList(
                    apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                           apiProperties.getArchivoHtmlLogoEscudo())
//                    ,apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
//                            apiProperties.getArchivoHtmlLogoFirmaTesoreria()),
//                    apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
//                            apiProperties.getArchivoHtmlLogoBciCupon())
//
            );

            ClassPathResource imgFileEsc = new ClassPathResource(logos.get(0)); //bci
//            ClassPathResource imgFileFirma = new ClassPathResource(logos.get(1)); //firma
//            ClassPathResource imgFileBci = new ClassPathResource(logos.get(2)); //escudo

            byte[] imageBytesEsc;
            try (InputStream is = imgFileEsc.getInputStream()) {
                imageBytesEsc = is.readAllBytes();
            }

//            byte[] imageBytesFirma;
//            try (InputStream is = imgFileFirma.getInputStream()) {
//                imageBytesFirma = is.readAllBytes();
//            }

//            byte[] imageBytesBci;
//            try (InputStream is = imgFileBci.getInputStream()) {
//                imageBytesBci = is.readAllBytes();
//            }

              String base64Esc = Base64.getEncoder().encodeToString(imageBytesEsc);

        /***Cargar una vez el String de html**/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreHtmlConfirmada())
        );

        return new OrdenCompraHtml(htmlIndividual, Arrays.asList(base64Esc));

    }

    // ─── 6. Confirmar (estado 5 — bloquea la OC) ────────────
    public DocumentoOrdenCompra generarDocumentoOc(String codOC, PlantillaDTO plantillaDTO) throws Exception {

        OrdenCompra oc = ocRepo.findByCodigoOrdenCompra(plantillaDTO.getCodOrdenCompra())
                .orElseThrow(() -> new ValdiviaOCException("OC "+ codOC +"no encontrada"));

        Optional<StatusOrdenCompra> optStatusOrdenCompraActual = statusOrdenCompraRepository.findStatusActual(oc.getIdOrdenCompra().longValue());

        if(optStatusOrdenCompraActual.isPresent() && (
                optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 4L ||
                        optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 5 )) {
            //solo hay una OC con ese codigo
            Page< PlantillaStatusDTO > pgPlantillaStatus = realizarBusquedaAvanzada(
                    optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(), oc.getProveedor().getRutProveedor(),
                    null, oc.getCodigoOrdenCompra(), null, null, PageRequest.of(0, 1));

            Usuarios usuarioAutorizador  = usuariosRepository.findByUsername(oc.getUsernameAutorizador()).get();
            Usuarios usuarioAnulador  = usuariosRepository.findByUsername(oc.getUsernameAnulador()).get();

            PlantillaStatusImpresionDTO plantillaStatusImpresionDTO = new PlantillaStatusImpresionDTO(pgPlantillaStatus.getContent().get(0));
            plantillaStatusImpresionDTO.setUsuarioAutorizador(usuarioAutorizador.getNombreMember().concat(" ").concat(usuarioAutorizador.getApellidoPaternoMember()));
            plantillaStatusImpresionDTO.setUsuarioAnulador(usuarioAnulador.getNombreMember().concat(" ").concat(usuarioAnulador.getApellidoPaternoMember()));
            OrdenCompraHtml ordenCompraHtml = generarOrdenCompraHtml();

            String html = "";
            if(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 4L){
                html = PlantillaOrdenCompra.generarPlantillaConfirmada(ordenCompraHtml,
                        plantillaStatusImpresionDTO);
            }
            if(optStatusOrdenCompraActual.get().getEstadoOc().getIdEstadoOc() == 5 ){
                html = PlantillaOrdenCompra.generarPlantillaAnulado(ordenCompraHtml,
                        plantillaStatusImpresionDTO);
            }

            return new DocumentoOrdenCompra(optStatusOrdenCompraActual.get().getEstadoOc().getCodigoEstadoOc(), pdfComponent.generarPdffromHtml(html));
        }



        return null;
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
            String codUnidad, String codOrdenCompra, String fechaInicioStr, String fechaFinStr, Pageable pageable) {

        String rutParaQuery = rut;
        Integer unidadParaQuery = null;
        Integer idStatus = null;
        LocalDate inicio = (fechaInicioStr != null) ? LocalDate.parse(fechaInicioStr) : null;
        LocalDate fin = (fechaFinStr != null) ? LocalDate.parse(fechaFinStr) : null;
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
                idStatus, rutParaQuery, unidadParaQuery, codOrdenCompra, inicio, fin, pageable
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
    /******************************************************************************************************/
    /******************************************************************************************************/
    /******************************************************************************************************/
    public AdjuntoDTO guardarAdjunto(String codigoOrdenCompra, PlantillaDTO plantillaDTO, MultipartFile file) throws IOException {

        OrdenCompra oc = null;
        Usuarios usuario = null;
        if(!plantillaDTO.getCodOrdenCompra().equals("")){
            oc =
                    ocRepo.findByCodigoOrdenCompra(codigoOrdenCompra)
                            .orElseThrow(() -> new ValdiviaOCException("OC "+codigoOrdenCompra +"no encontrada"));
        }

        if(!plantillaDTO.getUsernameUsuario().equals("")){
            Optional<Usuarios> optUsuario = usuariosRepository.findByUsername(plantillaDTO.getUsernameUsuario());

            usuario = optUsuario.isPresent() ? optUsuario.get() : Usuarios.builder().build();

        }

        // 1. Definir y crear la ruta de la carpeta: public_file/{codigo_orden_compra}/
        Path rutaCarpetaOC = Paths.get(apiProperties.getArchivoCarpetaPublic(), codigoOrdenCompra).toAbsolutePath().normalize();
        Files.createDirectories(rutaCarpetaOC); // Crea las carpetas si no existen

        // 2. Evitar colisiones de nombres (ej: si suben dos veces "cotizacion.pdf")
        String nombreOriginal = file.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;

        // 3. Guardar el archivo físico en el servidor
        Path rutaDestinoArchivo = rutaCarpetaOC.resolve(nombreUnico);
        Files.copy(file.getInputStream(), rutaDestinoArchivo, StandardCopyOption.REPLACE_EXISTING);

        // 4. Registrar en la Base de Datos
        // Aquí mapeas a tu Entidad JPA que representa la tabla `adjunto_orden_compra`

        AdjuntoOrdenCompra adjuntoOrdenCompra = new AdjuntoOrdenCompra();
        adjuntoOrdenCompra.setIdOrdenCompra(oc);
        adjuntoOrdenCompra.setNombreArchivo(nombreOriginal);
        adjuntoOrdenCompra.setRutaArchivo(rutaDestinoArchivo.toString()); // Guarda la ruta absoluta o relativa
        adjuntoOrdenCompra.setTipoContenido(file.getContentType());
        adjuntoOrdenCompra.setTamanoBytes((int) file.getSize());
        adjuntoOrdenCompra.setIdUsuarioSube(usuario.getIdUsuario());
        adjuntoOrdenCompra.setActive(true);

        adjuntoOrdenCompraRepository.save(adjuntoOrdenCompra);

        guardar(plantillaDTO);

        log.info("Archivo guardado en: " + rutaDestinoArchivo.toString());
        return AdjuntoDTO.builder().nombreArchivo(adjuntoOrdenCompra.getNombreArchivo()).urlDescarga(adjuntoOrdenCompra.getRutaArchivo()).build();

    }

    public List<AdjuntoDTO> obtenerAdjuntosPorCodigoOrdenCompra(String codigoOrdenCompra) {
        // 1. Buscar todos los registros de adjuntos asociados a esa OC
        OrdenCompra oc = null;Usuarios usuario = null;

            oc =
                    ocRepo.findByCodigoOrdenCompra(codigoOrdenCompra)
                            .orElseThrow(() -> new ValdiviaOCException("OC "+codigoOrdenCompra +"no encontrada"));

        List<AdjuntoOrdenCompra> listaAdjuntos = adjuntoOrdenCompraRepository.findByIdOrdenCompra(oc);

        // 2. Mapear la lista de entidades a una lista de DTOs para el Front-end
        return listaAdjuntos.stream().map(adjunto -> {
            // Armamos la URL apuntando al endpoint de descarga usando el ID del adjunto
            String urlDescarga = "/api/ordenes-compra/adjuntos/descargar/" + adjunto.getIdAdjuntoOc();

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

    public InfoDescargaArchivoDTO prepararDescargaUniversal(String subPath, String xForwardedFor) throws IOException {

        // 1. Resolver la ruta absoluta en el disco del servidor
        Path filePath = Paths.get(apiProperties.getArchivoCarpetaPublic(), subPath).toAbsolutePath().normalize();

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

}