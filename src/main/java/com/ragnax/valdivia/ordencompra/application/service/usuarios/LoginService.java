package com.ragnax.valdivia.ordencompra.application.service.usuarios;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ItemValue;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.LoginResponse;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.*;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    @Autowired
    private final UsuariosRepository usuariosRepository;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final MenuRepository menuRepository;

    @Autowired
    private final MenuRolRepository menuRolRepository;

    @Transactional("usuariosTransactionManager")
    public LoginResponse login(String username, String password, String codEmpresa) {

        log.info("v3 username {} password {} codEmpresa {}", username, password, codEmpresa);

        List<ItemValue> items = Arrays.asList();

        Usuarios usuario = usuariosRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        Unidad unidadObj = unidadRepository.findById(usuario.getIdUnidad().getIdUnidad())
                .orElseThrow(() -> new RuntimeException("Unidad no encontrada"));

        EmpresaCliente empresaCliente = empresaClienteRepository.findById(unidadObj.getEmpresaCliente().getIdEmpresaCliente())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Validar si el Usuario esta habilitado para la Empresa x
        if (!empresaCliente.getCodigoEmpresaCliente().equals(codEmpresa)) {
            throw new ValdiviaOCException("No se pudo obtener usuario en empresa " + codEmpresa);
        }

        Set<String> codMenus = menuRolRepository.findByRole(usuario.getIdRole()).stream()
                .map(MenuRol::getMenu)
                .filter(menu -> menu != null && Boolean.TRUE.equals(menu.getEstadoMenu()))
                .map(Menu::getCodMenu)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if(empresaCliente.getCodigoEmpresaCliente().equalsIgnoreCase(codEmpresa)){
            final String finalUnidadFiltro = getUnidad(unidadObj.getShowNombreUnidad().toLowerCase());
            /****
             ('vld_ccm_1','search','Búsqueda y Gestión','/ccm/registros',1, 1),
             ('vld_ccm_2','file-earmark-plus','Creacion OC','/ccm/creacion-oc',1, 2),
             ('vld_ccm_3','check-circle','Autorizacion','/ccm/autorizacion',1, 3),
             ('vld_ccm_4','x-circle','Anulacion','/ccm/anulacion',1, 4),
             ('vld_ccm_5','person-badge','Gestion Proveedores','/ccm/proveedores',1, 6),
             ('vld_ccm_6','person-badge','Administracion de Items','/ccm/productos',1, 7),
             ('vld_ccm_7','file-earmark-bar-graph','Reportes','/ccm/reportes',1, 8),
             ('vld_ccm_8','person-gear','Administracion','/supervision/administracion',1, 9),
             ('vld_ccm_9','mortarboard','Capacitacion','/ccm/capacitacion',1, 10),
             ('vld_ccm_10','x-circle','Confirmacion','/ccm/confirmacion',1,5),
             ****/
            String urlEmpresa = empresaCliente.getUrlEmpresaCliente();

            items = menuRepository.findAllById(codMenus)
                    .stream()
                    // 2. Filtramos: Si la URL del menú CONTIENE la de la empresa, SE QUEDA
                    .filter(menu -> {
                        if (urlEmpresa.isEmpty() || menu.getUrl() == null) return false; // Si no hay patrón, no pasa nada
                        return menu.getUrl().contains(urlEmpresa);
                    })
                    .map(menu -> {
                        ItemValue item = new ItemValue();
                        item.setId(menu.getCodMenu());
                        item.setValue1(menu.getNombre());
                        item.setValue2(menu.getUrl());
                        item.setOrden(menu.getOrden());
                        return item;
                    })
                    .toList();
        }
        return LoginResponse.builder()
                .username(usuario.getUsername())
                .nombreMember(usuario.getNombreMember())
                .apellidoPaternoMember(usuario.getApellidoPaternoMember())
                .apellidoMaternoMember(usuario.getRut())
                .telefonoContactoMember(usuario.getTelefonoContactoMember())
                .emailPerfil(usuario.getEmailPerfil())
                .unidad(unidadObj)
                .empresa(empresaCliente)
                .role(usuario.getIdRole())
                .items(items)
                .build();
    }

    public String getUnidad(String nombreUnidadLower){
        // --- LÓGICA DE ASIGNACIÓN DE UNIDAD (MAPEO) ---
        String unidadFiltro = "";
        if (nombreUnidadLower.contains("finanzas")) {
            unidadFiltro = "finanzas";
        } else if (nombreUnidadLower.contains("operaciones")) {
            unidadFiltro = "operaciones";
        } else if (nombreUnidadLower.contains("administracion")) {
            unidadFiltro = "administracion"; // O el valor que corresponda a tus URLs de imprenta
        }

        return unidadFiltro;
    }

    @Autowired
    private final UnidadRepository unidadRepository;

    @Autowired
    private final EmpresaClienteRepository empresaClienteRepository;


}
