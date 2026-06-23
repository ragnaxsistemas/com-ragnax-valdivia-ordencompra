package com.ragnax.valdivia.ordencompra.application.service;

import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PendienteAnulacionDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PendienteAnulacionOrdenCompraRequest;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.PendienteAnulacionOrdenCompra;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.OrdenCompraRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.PendienteAnulacionOrdenCompraRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PendienteAnulacionOrdenCompraService {


    @Autowired
    private PendienteAnulacionOrdenCompraRepository pendienteAnulacionOrdenCompraRepository;

    @Autowired
    private OrdenCompraRepository ocRepo;

    @Autowired
    private UnidadRepository unidadRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    public PendienteAnulacionDTO obtenerPendienteAnulacionOrdenCompra(String codOrdenCompra) {
        // 1. Cargamos los datos necesarios de la BD
        OrdenCompra oc = null;
        //Usuarios usuario = null;

        if(!codOrdenCompra.equals("")){
            oc =
                    ocRepo.findByCodigoOrdenCompra(codOrdenCompra)
                            .orElseThrow(() -> new ValdiviaOCException("OC "+codOrdenCompra +"no encontrada"));
        }

        List<PendienteAnulacionOrdenCompra> listPendienteAnulacionOrdenCompra= pendienteAnulacionOrdenCompraRepository. findByOrdenCompra(oc);

        Optional<PendienteAnulacionOrdenCompra> optPendienteAnulacionOrdenCompra = listPendienteAnulacionOrdenCompra.stream()
                .findFirst();

        if(optPendienteAnulacionOrdenCompra.isPresent()){

            Usuarios usuarioSolicitante = usuariosRepository.findById(optPendienteAnulacionOrdenCompra.get().getIdUsuario())
                    .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + optPendienteAnulacionOrdenCompra.get().getIdUsuario()));

            // 2. Definir el nuevo formato (Nota: MM en mayúsculas para meses)
            DateTimeFormatter salidaFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // 3. Formatear
            String fechaHora = optPendienteAnulacionOrdenCompra.get().getFechaEvento().format(salidaFormat);

            return PendienteAnulacionDTO.builder().
                    codOrdenCompra(oc.getCodigoOrdenCompra()).
                    active(optPendienteAnulacionOrdenCompra.get().getActiva()).
                    username(usuarioSolicitante.getUsername()).
                    nombreUsuario(usuarioSolicitante.getNombreMember()).
                    apellidoUsuario(usuarioSolicitante.getApellidoPaternoMember()).
                    observacionAnulacion("Anulacion solicitada por el usuario ".toUpperCase() + usuarioSolicitante.getUsername() +" realizada el ".toUpperCase() +fechaHora.replace(" ", " a las ").concat(" Horas")).
                    fechaSolicitudAnulacion(optPendienteAnulacionOrdenCompra.get().getFechaEvento().format(DateTimeFormatter.ISO_DATE_TIME)).build();
        }
        return new PendienteAnulacionDTO();
    }

    public PendienteAnulacionDTO guardarPendienteAnulacionOrdenCompra(PendienteAnulacionOrdenCompraRequest pendienteAnulacionOrdenCompraRequest) {
        // 1. Cargamos los datos necesarios de la BD
        OrdenCompra oc = null;
        //Usuarios usuario = null;

        if(!pendienteAnulacionOrdenCompraRequest.getCodOc().equals("")){
            oc =
                    ocRepo.findByCodigoOrdenCompra(pendienteAnulacionOrdenCompraRequest.getCodOc())
                            .orElseThrow(() -> new ValdiviaOCException("OC "+pendienteAnulacionOrdenCompraRequest.getCodOc() +"no encontrada"));
        }

        Unidad unidadSolicitanteAnulacion = unidadRepository.findByCodigoUnidad
                        (pendienteAnulacionOrdenCompraRequest.getUnidadPendienteAnulacion())
                .orElseThrow(() -> new ValdiviaOCException("Unidad no encontrada: " + pendienteAnulacionOrdenCompraRequest.getUnidadPendienteAnulacion()));

        Usuarios usuarioSolicitante = usuariosRepository.findByUsernameAndIdUnidad(pendienteAnulacionOrdenCompraRequest.getUsuarioPendienteAnulacion(), unidadSolicitanteAnulacion)
                .orElseThrow(() -> new ValdiviaOCException("Usuario no encontrado: " + pendienteAnulacionOrdenCompraRequest.getUsuarioPendienteAnulacion()));

        String observacionPendiente = "Status Pendiente de Anulacion para la orden "+oc.getCodigoOrdenCompra() +" para el usuario "+ pendienteAnulacionOrdenCompraRequest.getUsuarioPendienteAnulacion();

        PendienteAnulacionOrdenCompra pendienteAnulacionOrdenCompra =
            pendienteAnulacionOrdenCompraRepository.save(PendienteAnulacionOrdenCompra.builder().
                    ordenCompra(oc).
                    idUsuario(usuarioSolicitante.getIdUsuario()).
                    activa(true).
                    fechaEvento(LocalDateTime.now()).
                    observacion(observacionPendiente).
                    build());

        return PendienteAnulacionDTO.builder().
             codOrdenCompra(oc.getCodigoOrdenCompra()).
             active(pendienteAnulacionOrdenCompra.getActiva()).
             username(usuarioSolicitante.getUsername()).
             nombreUsuario(usuarioSolicitante.getNombreMember()).
             apellidoUsuario(usuarioSolicitante.getApellidoPaternoMember()).
             fechaSolicitudAnulacion(pendienteAnulacionOrdenCompra.getFechaEvento().format(DateTimeFormatter.ISO_DATE_TIME)).build();
    }

}


/******/





