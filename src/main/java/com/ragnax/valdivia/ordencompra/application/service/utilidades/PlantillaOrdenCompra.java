package com.ragnax.valdivia.ordencompra.application.service.utilidades;

import com.ragnax.valdivia.ordencompra.application.service.model.OrdenCompraHtml;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaStatusImpresionDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlantillaOrdenCompra {

    public static String generarPlantillaConfirmada(OrdenCompraHtml ordenCompraHtml,
                                                            PlantillaStatusImpresionDTO plantillaStatusImpresionDTO) {
        //Escudo
        //String imgTag1 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(0) + "' style='width: 60px; height: auto;'/>";

        //Firma
        //String imgTag2 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(1) + "' style='width: 180px; height: auto; display: block; margin: 0 auto;'/>";

        String htmlReemplazado = ordenCompraHtml.getHtml().replace("{{FOLIO_OC}}", plantillaStatusImpresionDTO.getCodOrdenCompra());

        //htmlReemplazado = htmlReemplazado.replace("{{LOGO_1}}", imgTag1);
        
        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CREACION_OC}}", plantillaStatusImpresionDTO.getFechaOrdenCompra());

        htmlReemplazado = htmlReemplazado.replace("{{UNIDAD_COMPRADORA_OC}}", plantillaStatusImpresionDTO.getNombreUnidad());

        htmlReemplazado = htmlReemplazado.replace("{{AUTORIZADOR_OC}}", plantillaStatusImpresionDTO.getUsuarioAutorizador());

        htmlReemplazado = htmlReemplazado.replace("{{CONFIRMADOR_OC}}", plantillaStatusImpresionDTO.getUsuarioConfirmador());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRE_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{RUT_PROVEEDOR_OC}}", formatearRut(plantillaStatusImpresionDTO.getRutProveedor()));

        htmlReemplazado = htmlReemplazado.replace("{{DIRECCION_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getDireccionProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{COMUNA_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreComunaProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{REGION_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreRegionProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{GIRO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getGiroProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{FONO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getTelefonoContactoProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{FONO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getEmailProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{EMAIL_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getEmailProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRE_OC}}", plantillaStatusImpresionDTO.getNombreOrdenCompra());

        htmlReemplazado = htmlReemplazado.replace("{{PLAZO}}", "CREDITO A 30 DIAS");

        htmlReemplazado = htmlReemplazado.replace("{{TABLA_PRODUCTOS_OC}}", "<tr> <td>1</td><td>[Descripción del producto o servicio]</td><td>0</td><td>$ 0</td><td>$ 0</td></tr>");

        if(plantillaStatusImpresionDTO.getCodDocumentoTributario() .equalsIgnoreCase("38")){
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_NETO_OC}}", "Neto A");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "Impuesto A");
            htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", "Total A");
        }else{
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_NETO_OC}}", "Neto B");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "Impuesto B");
            htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", "Total B");
        }

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_NETO_OC}}", String.valueOf(plantillaStatusImpresionDTO.getTotalNeto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_IMPUESTO_OC}}", String.valueOf(plantillaStatusImpresionDTO.getImpuesto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", String.valueOf(plantillaStatusImpresionDTO.getTotal()));

        return htmlReemplazado;
    }

    public static String generarPlantillaAnulado(OrdenCompraHtml ordenCompraHtml,
                                                    PlantillaStatusImpresionDTO plantillaStatusImpresionDTO) {
        //Escudo
        //String imgTag1 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(0) + "' style='width: 60px; height: auto;'/>";

        //Firma
        //String imgTag2 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(1) + "' style='width: 180px; height: auto; display: block; margin: 0 auto;'/>";

        String htmlReemplazado = ordenCompraHtml.getHtml().replace("{{FOLIO_OC}}", plantillaStatusImpresionDTO.getCodOrdenCompra());

        //htmlReemplazado = htmlReemplazado.replace("{{LOGO_1}}", imgTag1);

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CREACION_OC}}", formatearFecha(plantillaStatusImpresionDTO.getFechaOrdenCompra()));

        htmlReemplazado = htmlReemplazado.replace("{{UNIDAD_COMPRADORA_OC}}", plantillaStatusImpresionDTO.getNombreUnidad());

        htmlReemplazado = htmlReemplazado.replace("{{AUTORIZADOR_OC}}", plantillaStatusImpresionDTO.getUsuarioAutorizador());

        htmlReemplazado = htmlReemplazado.replace("{{ANULADOR_OC}}", plantillaStatusImpresionDTO.getUsuarioAnulador());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRE_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{RUT_PROVEEDOR_OC}}", formatearRut(plantillaStatusImpresionDTO.getRutProveedor()));

        htmlReemplazado = htmlReemplazado.replace("{{DIRECCION_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getDireccionProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{COMUNA_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreComunaProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{REGION_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getNombreRegionProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{GIRO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getGiroProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{FONO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getTelefonoContactoProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{FONO_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getEmailProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{EMAIL_PROVEEDOR_OC}}", plantillaStatusImpresionDTO.getEmailProveedor());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRE_OC}}", plantillaStatusImpresionDTO.getNombreOrdenCompra());

        htmlReemplazado = htmlReemplazado.replace("{{PLAZO}}", "CREDITO A 30 DIAS");

        htmlReemplazado = htmlReemplazado.replace("{{TABLA_PRODUCTOS_OC}}", "<tr> <td>1</td><td>[Descripción del producto o servicio]</td><td>0</td><td>$ 0</td><td>$ 0</td></tr>");

        if(plantillaStatusImpresionDTO.getCodDocumentoTributario() .equalsIgnoreCase("38")){
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_NETO_OC}}", "Neto A");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "Impuesto A");
            htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", "Total A");
        }else{
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_NETO_OC}}", "Neto B");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "Impuesto B");
            htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", "Total B");
        }

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_NETO_OC}}", String.valueOf(plantillaStatusImpresionDTO.getTotalNeto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_IMPUESTO_OC}}", String.valueOf(plantillaStatusImpresionDTO.getImpuesto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", String.valueOf(plantillaStatusImpresionDTO.getTotal()));

        return htmlReemplazado;
    }


    public static String formatearRut(String rut) {
        rut = rut.replaceAll("^0+", ""); // quitar ceros izquierda

        String cuerpo = rut.substring(0, rut.length() - 1);
        String dv = rut.substring(rut.length() - 1);

        cuerpo = cuerpo.replaceAll("(\\d)(?=(\\d{3})+(?!\\d))", "$1.");

        return cuerpo + "-" + dv;
    }

    public static String formatearFecha(String fecha) {
        LocalDateTime fechaHora = LocalDateTime.parse(fecha);

        // 2. Definir el nuevo formato (Nota: MM en mayúsculas para meses)
        DateTimeFormatter salidaFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 3. Formatear
        String resultado = fechaHora.format(salidaFormat);

        return resultado;
    }

}
