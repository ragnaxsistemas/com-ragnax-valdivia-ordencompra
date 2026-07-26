package com.ragnax.valdivia.ordencompra.application.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service
public class GeneradorReporteService {

    @Autowired
    private TemplateEngine templateEngine; // Motor Thymeleaf configurado en Spring

    public byte[] generarPdfReporteGastos(
            Map<String, Map<String, List<PlantillaStatusDTO>>> agrupadoPorUnidadYEstado,
            String fechaInicio,
            String fechaFin) throws Exception {

// Formateadores
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

// Ejemplo de conversión
        String sfechaInicio = LocalDate.parse(fechaInicio, inputFormatter).format(outputFormatter);
        String sfechaFin = LocalDate.parse(fechaFin, inputFormatter).format(outputFormatter);
// Resultado: "31/07/2026"

        // 1. Asignar variables al contexto de Thymeleaf
        Context context = new Context();
        context.setVariable("agrupadoPorUnidadYEstado", agrupadoPorUnidadYEstado);
        context.setVariable("fechaInicio", sfechaInicio);
        context.setVariable("fechaFin", sfechaFin);

        // 2. Procesar el HTML (Renderizar variables y bucles th:each)
        String htmlProcesado = templateEngine.process("reporte-gastos", context);

        // 3. Convertir HTML procesado a PDF utilizando OpenHTMLtoPDF
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Soporte a HTML5 bien formado (XHTML)
            builder.withHtmlContent(htmlProcesado, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        }
    }
}
