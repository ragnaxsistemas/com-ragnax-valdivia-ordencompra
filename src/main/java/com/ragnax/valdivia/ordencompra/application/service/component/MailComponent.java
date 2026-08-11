package com.ragnax.valdivia.ordencompra.application.service.component;


import com.ragnax.valdivia.ordencompra.infraestructura.configuration.ApiProperties;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.Proveedor;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.valdivia.ordencompra.infraestructura.exception.ValdiviaOCException;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UsuariosRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
public class MailComponent {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private UnidadRepository unidadRepository;

    @Autowired
    private ApiProperties apiProperties;

    @Value("${spring.profiles.active}")
    private String profile;

    public  void enviarCorreoResend(String tipo, Proveedor proveedor, Usuarios usuarioSolicitante, Usuarios usuarioAutorizador , Usuarios usuarioConfirmador,
                                    byte[] archivoAdjunto, String nombreArchivo) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

           /*** Unidad setSupervision =  Unidad.builder().idUnidad(6).build();
            Unidad setAdministracion =  Unidad.builder().idUnidad(6).build();
            Unidad setOperadores =  Unidad.builder().idUnidad(7).build();

            List<Usuarios> listaSupervisionBCC = usuariosRepository.findByIdUnidad(setSupervision);
            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            List<Usuarios> listaOperadorCC = usuariosRepository.findByIdUnidad(setOperadores);***/

            List<String> listaCopias = Arrays.asList(usuarioSolicitante.getEmailPerfil(), usuarioAutorizador.getEmailPerfil(), usuarioConfirmador.getEmailPerfil(), apiProperties.getMailUsername());
// 2. Unificas los correos de las tres listas en un solo arreglo para BCC (sin duplicados)
            String[] destinatariosCC = Stream.of(listaCopias)
                    .flatMap(List::stream)           // Aplana las 3 listas en un solo flujo de objetos Usuarios
                    .distinct()                      // Opcional: Elimina correos duplicados si un usuario se repite
                    .toArray(String[]::new);

            //apiProperties.getMailUsername()
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            String to = proveedor.getEmailProveedor();
            String[] cc = destinatariosCC;
            String bcc = apiProperties.getMailUsername();

            if(profile.equals("app")){
                to = apiProperties.getMailUsername();
                cc = Stream.of(Arrays.asList("julio.i.cornejo.g@gmail.com", "julio.i.cornejo.gonzalez@gmail.com")).flatMap(List::stream)           // Aplana las 3 listas en un solo flujo de objetos Usuarios
                        .distinct()                      // Opcional: Elimina correos duplicados si un usuario se repite
                        .toArray(String[]::new);
            }

            helper.setFrom(apiProperties.getMailUsername(), "Corporación Cultural Municipal De Valdivia");
            helper.setTo(to); //mailReceptoProveedor
            helper.setCc(cc); //solicitante, autoriza /confirmar
            helper.setBcc(bcc);
            String subject = String.format("%s Orden de Compra Emitida - Corporación Cultural Municipal De Valdivia", tipo);
            helper.setSubject(subject);

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimado proveedor:\n\n" +
                            "Junto con saludar, se adjunta Orden de Compra de la Corporación Cultural de la Ilustre Municipalidad de Valdivia.\n" +
                            "Favor se solicita comunicarse directamente con la Unidad Requiriente a cargo.\n\n" +
                            "Saluda atentamente,\n" +
                            "Corporación Cultural Ilustre Municipalidad de Valdivia\n\n"+
                            "Esta notificación fue emitida automáticamente por Sistema Órdenes de Compra de la CCM Valdivia"
                    ,
                    tipo
            );

            helper.setText(cuerpo);

            // Adjuntar el archivo
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)
            log.info("subject {} - cuerpo {} - FROM {} - TO {} - CC {} - BCC {} ***", subject, cuerpo,apiProperties.getMailUsername(),
                    to, destinatariosCC, bcc);

            emailSender.send(message);
            log.info("Correo enviado con éxito vía Resend");
        } catch (MessagingException e) {
            e.printStackTrace();

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

