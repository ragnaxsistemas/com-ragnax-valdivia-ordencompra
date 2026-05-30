package com.ragnax.valdivia.ordencompra.infraestructura.controller;

import com.ragnax.valdivia.ordencompra.application.service.*;
import com.ragnax.valdivia.ordencompra.application.service.model.DocumentoOrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.*;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oc")
@CrossOrigin(origins = "*")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    private final ProveedorService proveedorService;

    private final EstadoOcService estadoOcService;

    private final DocumentoTributarioService documentoTributarioService;

    private final ProductoService productoService;

    @GetMapping("/dte/all")
    public ResponseEntity<List<DocumentoTributarioDTO>> dtelistarTodos() {
        return ResponseEntity.ok(documentoTributarioService.listarTodos());
    }

    @GetMapping("/dte/{codDte}")
    public ResponseEntity<DocumentoTributarioDTO> dtePorId(@PathVariable String codDte) {
        return ResponseEntity.ok(documentoTributarioService.obtenerPorCodigo(codDte));
    }

    @GetMapping("/status-oc/all")
    public ResponseEntity<List<EstadoOcDTO>> estadoOclistarTodos() {
        return ResponseEntity.ok(estadoOcService.listarTodos());
    }

    @GetMapping("/status-oc/{codEstadoOc}")
    public ResponseEntity<EstadoOcDTO> estadoOcPorcod(@PathVariable String codEstadoOc) {
        return ResponseEntity.ok(estadoOcService.obtenerPorCodigo(codEstadoOc));
    }

    @PostMapping("/producto")
    public ResponseEntity<ProductoDTO> crear(@RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.crear(dto));
    }

    @GetMapping("/producto/all")
    public ResponseEntity<List<ProductoDTO>> productolistarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/producto/activos")
    public ResponseEntity<List<ProductoDTO>> productolistarActivos() {
        return ResponseEntity.ok(productoService.listarActivos());
    }

    @GetMapping("/producto/{codProducto}")
    public ResponseEntity<ProductoDTO> productoobtenerPorId(@PathVariable String codProducto) {
        return ResponseEntity.ok(productoService.obtenerPorCodigo(codProducto));
    }


    @PutMapping("/producto/{codProducto}")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable String codProducto, @RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(codProducto, dto));
    }

    @GetMapping("/proveedor/all")
    public ResponseEntity<List<ProveedorDTO>> listarTodos() {
        return ResponseEntity.ok(proveedorService.listarTodos());
    }

    @GetMapping("/proveedor/activos")
    public ResponseEntity<List<ProveedorDTO>> listarActivos() {
        return ResponseEntity.ok(proveedorService.listarActivos());
    }

    @GetMapping("/proveedor/{rut_limpio}")
    public ResponseEntity<ProveedorDTO> obtenerPorId(@PathVariable String rut_limpio) {
        return ResponseEntity.ok(proveedorService.obtenerPorRut(rut_limpio));
    }

    @PostMapping("/proveedor")
    public ResponseEntity<ProveedorDTO> crear(@RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(proveedorService.crear(dto));
    }


    @PutMapping("/proveedor/{rut_limpio}")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable String rut_limpio, @RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(proveedorService.actualizar(rut_limpio, dto));
    }

    @PostMapping("/ordenes-compra/new")
    public ResponseEntity<PlantillaDTO> generarOC(
            @RequestBody OrdenCompraRequest req) {
        PlantillaDTO oc = ordenCompraService.generarOC(req.getPlantillaDTO());
        return ResponseEntity.status(HttpStatus.CREATED).body(oc);
    }

    /** POST /api/ordenes-compra
     *  Body: { ordenCompra, usuario } */
    @PostMapping("/ordenes-compra")
    public ResponseEntity<PlantillaDTO> guardar(
            @RequestBody OrdenCompraRequest req) {
        PlantillaDTO oc = ordenCompraService.guardar(req.getPlantillaDTO());
        return ResponseEntity.status(HttpStatus.OK).body(oc);
    }

    @PostMapping("/ordenes-compra/solicitar")
    public ResponseEntity<PlantillaDTO> solicitar(
            @RequestBody OrdenCompraRequest req) {
        return ResponseEntity.ok(
                ordenCompraService.solicitarAutorizacion(req.getPlantillaDTO()));
        //return   ResponseEntity.ok(null);
    }

    /** PATCH /api/ordenes-compra/{id}/devolver */
    @PostMapping("/ordenes-compra/devolver")
    public ResponseEntity<PlantillaDTO> devolver(
            @RequestBody OrdenCompraRequest req) {
        return ResponseEntity.ok(
                ordenCompraService.devolver(req.getCodOc(), req.getPlantillaDTO(), req.getUsuarioSup()));
        //return   ResponseEntity.ok(null);
    }

    /** PATCH /api/ordenes-compra/{id}/autorizar */
    @PostMapping("/ordenes-compra/autorizar")
    public ResponseEntity<PlantillaDTO> autorizar(
            @RequestBody OrdenCompraRequest req) {
        return ResponseEntity.ok(
                ordenCompraService.autorizar(req.getCodOc(), req.getPlantillaDTO(), req.getUsuarioSup()));
        //return   ResponseEntity.ok(null);
    }

    /** PATCH /api/ordenes-compra/{id}/anular */
    @PostMapping("/ordenes-compra/anular")
    public ResponseEntity<PlantillaDTO> anular(
            @RequestBody OrdenCompraRequest req) {
        return ResponseEntity.ok(
                ordenCompraService.anular(req.getCodOc(), req.getPlantillaDTO(), req.getUsuarioSup()));
        //return   ResponseEntity.ok(null);
    }

    /** PATCH /api/ordenes-compra/{id}/confirmar */
    @PostMapping("/ordenes-compra/confirmar")
    public ResponseEntity<PlantillaDTO> confirmar(
            @RequestBody OrdenCompraRequest req) {
        return ResponseEntity.ok(
                ordenCompraService.confirmar(req.getCodOc(), req.getPlantillaDTO(), req.getUsuarioSup()));
    }

    /** PATCH /api/ordenes-compra/{id}/confirmar */
    @PostMapping("/ordenes-compra/generar-documento-oc")
    public ResponseEntity<byte[]> generarDocumentoOc(
            @RequestBody OrdenCompraRequest req) throws Exception {

            DocumentoOrdenCompra documentoOrdenCompra =
                    ordenCompraService.generarDocumentoOc(req.getCodOc(), req.getPlantillaDTO());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "";
            if(documentoOrdenCompra.getCodEstadoOc().equals("anulado")){
                 filename = "OC_Anulada_" + req.getCodOc() + ".pdf";
            }else if(documentoOrdenCompra.getCodEstadoOc().equals("confirmada")){
                 filename = "OC_Confirmada_" + req.getCodOc() + ".pdf";
            }else {
                throw new Exception("Documento no encontrado");
            }
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(documentoOrdenCompra.getDocByte(), headers, HttpStatus.OK);

    }



        @GetMapping("/ordenes-compra/busqueda-avanzada")
        public ResponseEntity<Page<PlantillaStatusDTO>> buscar(
                @RequestParam(required = false) String codEstadoOc,
                @RequestParam(required = false) String rut,
                @RequestParam(required = false) String unidad,
                @RequestParam(required = false) String codOrdenCompra,
                @RequestParam(required = false) String fechaInicioStr,
                @RequestParam(required = false) String fechaFinStr,
                @PageableDefault(size = 10, sort = "idOrdenCompra") Pageable pageable
    ) {
            Page<PlantillaStatusDTO> resultados = ordenCompraService.realizarBusquedaAvanzada(
                    codEstadoOc, rut, unidad, codOrdenCompra, fechaInicioStr, fechaFinStr, pageable
            );
            //return   ResponseEntity.ok(null);
            return ResponseEntity.ok(resultados);
        }

        /****************** Archivos Adjuntos a OC ****************/
        /****************** Archivos Adjuntos a OC ****************/
        @PostMapping("/ordenes-compra/{codigoOrdenCompra}/adjuntos")
        public ResponseEntity<AdjuntoDTO> subirAdjunto(
                @PathVariable String codigoOrdenCompra,
                @RequestPart("req") OrdenCompraRequest req,
                @RequestParam("file") MultipartFile file) {

            if (file.isEmpty()) {
                throw new ValdiviaOCException("Por favor, selecciona un archivo válido.");
            }

            try {
                AdjuntoDTO adjDTO = ordenCompraService.guardarAdjunto(codigoOrdenCompra, req.getPlantillaDTO(), file);

                return ResponseEntity.ok(adjDTO);
            } catch (Exception e) {
                throw new ValdiviaOCException("Error al subir el archivo: " + e.getMessage());
            }
        }

        @GetMapping("/ordenes-compra/{codigoOrdenCompra}/archivos")
        public ResponseEntity<List<AdjuntoDTO>> obtenerRutasAdjuntos(@PathVariable String codigoOrdenCompra) {
            List<AdjuntoDTO> adjuntos = ordenCompraService.obtenerAdjuntosPorCodigoOrdenCompra(codigoOrdenCompra);

            if (adjuntos.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 No Content si no hay archivos
            }

            return ResponseEntity.ok(adjuntos); // 200 OK con la lista
        }

    @GetMapping("/ordenes-compra/download/**")
    public ResponseEntity<?> downloadUniversal(HttpServletRequest request) {
        try {
            // 1. Extraer el path dinámico desde el HttpServletRequest
            String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

            // 2. Obtener la cabecera del proxy
            String xForwardedFor = request.getHeader("X-Forwarded-For");

            // 3. Delegar toda la lógica al servicio
            InfoDescargaArchivoDTO infoDescarga = ordenCompraService.prepararDescargaUniversal(subPath, xForwardedFor);

            // 4. Preparar cabeceras comunes de respuesta
            String headerValue = String.format("inline; filename=\"%s\"", infoDescarga.getNombreArchivo());

            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(infoDescarga.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue);

            // 5. Responder según la estrategia determinada por el servicio
            if (infoDescarga.esParaNginx()) {
                System.out.println("[PROD - VALDIVIA] Redireccionando a Nginx X-Accel: {}" + infoDescarga.getNginxInternalUrl());
                return responseBuilder
                        .header("X-Accel-Redirect", infoDescarga.getNginxInternalUrl())
                        .build(); // Retorna vacío (Nginx inyectará los bytes)
            } else {
                System.out.println("[LOCAL - VALDIVIA] Transmitiendo bytes directamente desde Spring Boot");
                return responseBuilder
                        .body(infoDescarga.getRecurso()); // Retorna el recurso con los bytes reales
            }

        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado: {}" +  e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            System.out.println("Error al procesar la descarga del archivo" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el archivo");
        }
    }
}
