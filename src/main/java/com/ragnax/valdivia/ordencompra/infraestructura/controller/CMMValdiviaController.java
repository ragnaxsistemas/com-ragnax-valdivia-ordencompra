package com.ragnax.valdivia.ordencompra.infraestructura.controller;

import com.ragnax.valdivia.ordencompra.application.service.usuarios.*;
import com.ragnax.valdivia.ordencompra.infraestructura.configuration.ApiProperties;
import com.ragnax.valdivia.ordencompra.infraestructura.configuration.JwtUtil;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class CMMValdiviaController {

    private final ApiProperties apiProperties;

    private final LoginService loginService;

    private final ComunaService comunaService;

    private final RegionService regionService;

    private final GiroService giroService;

    private final UnidadService unidadService;

    private final EmpresaClienteService empresaClienteService;

    // --- UPLOAD ---
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request){

        LoginResponse loginResponse =
                loginService.login(request.getUsername(), request.getPassword(), request.getCodEmpresa());

        TokenResponse tokenResponse = JwtUtil.generateToken(loginResponse);

        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<RegionDTO>> regioneslistarTodas() {
        return ResponseEntity.ok(regionService.listarTodas());
    }

    @GetMapping("/regiones/{codRegion}")
    public ResponseEntity<RegionDTO> oregionesbtenerPorId(@PathVariable String codRegion) {
        return ResponseEntity.ok(regionService.obtenerPorCodigo(codRegion));
    }

    @GetMapping("/comuna")
    public ResponseEntity<List<ComunaDTO>> comunaslistarTodas() {
        return ResponseEntity.ok(comunaService.listarTodas());
    }

    @GetMapping("/comuna/{codComuna}")
    public ResponseEntity<ComunaDTO> comunaPorCodComuna(@PathVariable String codComuna) {
        return ResponseEntity.ok(comunaService.obtenerPorCodComuna(codComuna));
    }

    @GetMapping("/comuna/region/{codRegion}")
    public ResponseEntity<List<ComunaDTO>> listarPorRegion(@PathVariable String codRegion) {
        return ResponseEntity.ok(comunaService.listarPorRegion(codRegion));
    }

    @GetMapping("giro/all")
    public ResponseEntity<List<GiroSiiDTO>> giroListarGiro(){
        return ResponseEntity.ok(giroService.listarTodos());
    }

    @GetMapping("/empresa/{codEmpresa}")
    public ResponseEntity<EmpresaClienteDTO> buscarEmpresa(@PathVariable String codEmpresa){
        return ResponseEntity.ok(empresaClienteService.buscarEmpresa(codEmpresa));
    }

    @GetMapping("/unidad/{codEmpresa}")
    public ResponseEntity<List<UnidadDTO>> unidadListarEmpresa(@PathVariable String codEmpresa){
        return ResponseEntity.ok(unidadService.listarUnidadEmpresa(codEmpresa));
    }

    @GetMapping("/unidad-compradora/{codEmpresa}")
    public ResponseEntity<List<UnidadDTO>> unidadCompradoraListarEmpresa(@PathVariable String codEmpresa){
        return ResponseEntity.ok(unidadService.listarUnidadCompradoraEmpresa(codEmpresa));
    }

    @GetMapping("/download/manual")
    public ResponseEntity<Resource> downloadManualUsuario() {
        log.info("********** downloadManualUsuario **********");

        // 1. Obtener la ruta base configurada (/var/www/sb_ope_001a/public_file/ o la de tu Mac)
        String rootPathStr = apiProperties.getArchivoCarpetaPublic();

        // 2. Construir la ruta exacta hacia el archivo manual_usuario.pdf
        Path filePath = Paths.get(rootPathStr, "documentacion", "manual_usuario.pdf");

        log.info("Buscando manual en la ruta: {}", filePath.toAbsolutePath().toString());

        // 3. Validar la existencia y lectura del archivo
        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            log.error("El archivo manual_usuario.pdf no existe o no se puede leer en la ruta especificada.");
            return ResponseEntity.notFound().build();
        }

        try {
            // 4. Cargar el archivo como recurso de Spring
            Resource resource = new UrlResource(filePath.toUri());

            // 5. Detectar el Content-Type de forma dinámica
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/pdf";
            }

            // 6. Configurar la cabecera para forzar la descarga del navegador
            String headerValue = "attachment; filename=\"manual_usuario.pdf\"";

            log.info("[MANUAL] Transmitiendo bytes directamente de forma exitosa.");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error al procesar el archivo del manual: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
