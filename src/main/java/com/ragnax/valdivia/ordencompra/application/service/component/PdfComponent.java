package com.ragnax.valdivia.ordencompra.application.service.component;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class PdfComponent {




    /***Este es para Masiva*/
    public byte[] generarPdffromHtml(String html) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 2. Convertimos el HTML directamente al stream
        // No es necesario crear un PdfWriter manualmente para casos simples
        HtmlConverter.convertToPdf(html, out);

        // 3. Retornamos el arreglo de bytes
        return out.toByteArray();
    }


    public String guardarPdfIndividual(byte[] pdfBytes,
                                     String rutaCarpeta,
                                     String nombreArchivo) throws Exception {

        Path carpeta = Paths.get(rutaCarpeta);

        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }

        Path archivo = carpeta.resolve(nombreArchivo);

        try (OutputStream os = Files.newOutputStream(archivo)) {
            os.write(pdfBytes);
        }
        return String.valueOf(archivo.getParent()).concat("/").concat(String.valueOf(archivo.getFileName()));
    }
}
