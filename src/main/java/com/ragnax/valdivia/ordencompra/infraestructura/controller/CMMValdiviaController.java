package com.ragnax.valdivia.ordencompra.infraestructura.controller;

import com.ragnax.valdivia.ordencompra.application.service.usuarios.*;
import com.ragnax.valdivia.ordencompra.infraestructura.configuration.JwtUtil;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class CMMValdiviaController {

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
                loginService.login(request.getUsername(), request.getPassword());

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

}
