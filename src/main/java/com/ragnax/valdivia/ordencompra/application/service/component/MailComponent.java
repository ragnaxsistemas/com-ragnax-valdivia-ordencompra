package com.ragnax.valdivia.ordencompra.application.service.component;


import com.ragnax.valdivia.ordencompra.infraestructura.configuration.ApiProperties;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UnidadRepository;
import com.ragnax.valdivia.ordencompra.infraestructura.repository.usuarios.UsuariosRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
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

    public  void enviarCorreoResend(String tipo, String mailReceptoCCM, byte[] archivoAdjunto, String nombreArchivo) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            Unidad setSupervision =  Unidad.builder().idUnidad(6).build();
            Unidad setAdministracion =  Unidad.builder().idUnidad(6).build();
            Unidad setOperadores =  Unidad.builder().idUnidad(7).build();

            List<Usuarios> listaSupervisionBCC = usuariosRepository.findByIdUnidad(setSupervision);
            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            List<Usuarios> listaOperadorCC = usuariosRepository.findByIdUnidad(setOperadores);


// 2. Unificas los correos de las tres listas en un solo arreglo para BCC (sin duplicados)
            String[] destinatariosCC = Stream.of(listaAdministracionCC, listaOperadorCC)
                    .flatMap(List::stream)           // Aplana las 3 listas en un solo flujo de objetos Usuarios
                    .map(Usuarios::getEmailPerfil)   // Extrae el correo de cada usuario
                    .distinct()                      // Opcional: Elimina correos duplicados si un usuario se repite
                    .toArray(String[]::new);

            String[] destinatariosBCC = listaSupervisionBCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);
           // destinatariosBCC[destinatariosBCC.length-1] =  "julio.ignacio.cornejo.sb@gmail.com";   //apiProperties.getMailUsername();
            // Si aún no has validado tu dominio, usa el de prueba de Resend:
            helper.setFrom("onboarding@resend.dev", "Corporación Cultural Municipal De Valdivia");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo("julio.ignacio.cornejo.sb@gmail.com"); //mailReceptoCCM
           // helper.setCc("julio.ignacio.cornejo.sb@gmail.com"); //destinatariosCC
           // helper.setBcc("julio.ignacio.cornejo.sb@gmail.com"); //destinatariosBCC
            helper.setSubject(String.format("%s Orden de Compra - Corporación Cultural Municipal De Valdivia", tipo));

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Buen día:" +
                            "Junto con saludar, por medio de la presente se informa que se ha realizado la %s de Órdenes de Compra correspondientes a la Corporación Cultural de la I. Municipalidad de Valdivia, según consta en el archivo adjunto.\n\n" +
                            "Agradeciendo de antemano su gestión y quedando atento a sus comentarios, le saluda atentamente,\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    tipo
            );

            helper.setText(cuerpo);

            // Adjuntar el archivo
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)

            emailSender.send(message);
            log.info("Correo enviado con éxito vía Resend");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public  void enviarCorreoResend(String observacion, String tipo,  String unidad, int largoCsv, byte[] archivoAdjunto, String nombreArchivo) {

        try {
            Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

            MimeMessage message = emailSender.createMimeMessage();

            Optional<Unidad> optUnidad = unidadRepository.findByShowNombreUnidad(unidad);
            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Si aún no has validado tu dominio, usa el de prueba de Resend:
            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (mailUnidad);
            helper.setCc(mailAdm);
            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(String.format("Solicitud normalizacion %s Correos de Chile - I. Municipalidad de San Bernardo", observacion));

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimada Fernanda,\n\n" +
                            "Buen día, favor solicito normalizar archivo adjunto correspondiente a %,d registros del %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "La presente solicitud corresponde a una %s realizada por la unidad %s.\n\n" +
                            "Quedamos atentos a sus comentarios, saludos cordiales y gracias de antemano.\n\n" +
                            "Julio Cornejo\n" +
                            "Cel. 993003452",
                    largoCsv,
                    observacion,
                    tipo,
                    unidad
            );

            helper.setText(cuerpo);

            // Adjuntar el archivo
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)

            emailSender.send(message);
            log.info("Correo enviado con éxito");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public  void enviarCorreoResendCargaMerge(String observacion, String tipo,  String unidad, int largoCsv, String nombreArchivo) {
        Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();


        try {
            MimeMessage message = emailSender.createMimeMessage();

            Optional<Unidad> optUnidad = unidadRepository.findByShowNombreUnidad(unidad);
            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Si aún no has validado tu dominio, usa el de prueba de Resend:
            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (mailUnidad);
            helper.setCc(mailAdm);
            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(String.format("Solicitud proceso %s IMSB - I. Municipalidad de San Bernardo", observacion));

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimada Unidad de %s,\n\n" +
                            "Buen día,\n" +
                            "se ha procesado la %s del proceso correspondiente al %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "Archivo procesado llamado: %s.\n\n",
                    unidad,
                    tipo,
                    observacion,
                    nombreArchivo);

            helper.setText(cuerpo);

            // Adjuntar el archivo
            //helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));// 'true' para HTML (como tus plantillas)

            emailSender.send(message);
            log.info("Correo enviado con éxito vía Resend");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public  void enviarCorreoHabilitarImprenta(String observacion, String tipo,  String unidad, String nombreArchivo) {
        Unidad setAdministracion =  Unidad.builder().idUnidad(1).build();

        MimeMessage message = emailSender.createMimeMessage();
        try {
            Optional<Unidad> optUnidad = unidadRepository.findByShowNombreUnidad(unidad);
            List<Usuarios> lista = usuariosRepository.findByIdUnidad(optUnidad.get());
            String[] mailUnidad = lista.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);

            List<Usuarios> listaAdministracionCC = usuariosRepository.findByIdUnidad(setAdministracion);
            String[] mailAdm = listaAdministracionCC.stream()
                    .map(Usuarios::getEmailPerfil)
                    .toArray(String[]::new);
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Si aún no has validado tu dominio, usa el de prueba de Resend:
            helper.setFrom(apiProperties.getMailUsername(), "Generacion Cartas Ilustre Municipalidad San bernardo");
            // helper.setCc(new String[] {"julio.i.cornejo.g@gmail.com"} );
            helper.setTo (mailUnidad);
            helper.setCc(mailAdm);
            helper.setBcc(apiProperties.getMailUsername());
            helper.setSubject(String.format("Solicitud proceso %s IMSB - I. Municipalidad de San Bernardo", observacion));

            // Construcción del Cuerpo
            String cuerpo = String.format(
                    "Estimada Unidad de Imprenta,\n\n" +
                            "Buen día,\n" +
                            "se ha procesado la %s un archivo de proceso correspondiente al %s de la I. Municipalidad de San Bernardo.\n\n" +
                            "el archivo esta disponible para su descarga: %s.\n\n",
                    tipo,
                    observacion,
                    nombreArchivo);

            helper.setText(cuerpo);

            emailSender.send(message);
            log.info("Correo enviado con éxito vía Resend");
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

