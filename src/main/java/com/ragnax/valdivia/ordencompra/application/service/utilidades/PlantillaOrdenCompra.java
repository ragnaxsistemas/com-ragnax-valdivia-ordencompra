package com.ragnax.valdivia.ordencompra.application.service.utilidades;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragnax.valdivia.ordencompra.application.service.model.OrdenCompraHtml;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ItemAngular;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaStatusImpresionDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ProductoDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlantillaOrdenCompra {

    public static String generarPlantillaConfirmada(OrdenCompraHtml ordenCompraHtml,
                                                            PlantillaStatusImpresionDTO plantillaStatusImpresionDTO) throws JsonProcessingException {

        //Escudo
        String imgTag1 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(0) + "' style='width: 120px; height: auto;'/>";

        String imgTag2 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(1) + "' style='width: 60px; height: auto;'/>";

        String imgTag3 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(2) + "' style='width: 60px; height: auto;'/>";

        String htmlReemplazado = ordenCompraHtml.getHtml().replace("{{FOLIO_OC}}", plantillaStatusImpresionDTO.getCodOrdenCompra());

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_OC_CCM}}", imgTag1);

        htmlReemplazado = htmlReemplazado.replace("{{TIMBRE_CONTABILIDAD}}", imgTag2);

        htmlReemplazado = htmlReemplazado.replace("{{TIMBRE_GERENCIA}}", imgTag3);
        
        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CREACION_OC}}", formatearFecha(plantillaStatusImpresionDTO.getFechaOrdenCompra()));

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

        htmlReemplazado = htmlReemplazado.replace("{{OBSERVACIONES_OC}}", plantillaStatusImpresionDTO.getObservaciones());

        htmlReemplazado = htmlReemplazado.replace("{{PLAZO}}", "CREDITO A 30 DIAS");

        htmlReemplazado = htmlReemplazado.replace("{{TABLA_PRODUCTOS_OC}}", stringAppendTable(plantillaStatusImpresionDTO.getListProductosOrden()));

        java.text.NumberFormat formaterClp = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CL"));
        formaterClp.setMaximumFractionDigits(0);

        if(plantillaStatusImpresionDTO.getCodDocumentoTributario() .equalsIgnoreCase("38") ||
                plantillaStatusImpresionDTO.getCodDocumentoTributario() .equalsIgnoreCase("38-c")){
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_NETO_OC}}", "MONTO TOTAL");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "RETENCIÓN");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_OC}}", "VALOR LÍQUIDO");
        }else{
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_NETO_OC}}", "TOTAL NETO");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "IMPUESTO");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_OC}}", "TOTAL");
        }

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_NETO_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getTotalNeto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_IMPUESTO_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getImpuesto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getTotal()));

        return htmlReemplazado;
    }

    public static String stringAppendTable(String jsonProductos) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<ItemAngular> items = null;

        // 1. Intentar parsear el JSON si contiene datos válidos
        if (jsonProductos != null && !jsonProductos.trim().isEmpty() && !jsonProductos.equals("[]")) {
            items = mapper.readValue(
                    jsonProductos,
                    new TypeReference<List<ItemAngular>>() {
                    }
            );
        }

        StringBuilder filasHtml = new StringBuilder();
        int totalFilasPintadas = 0;

        // 2. Pintar los productos reales si existen
        if (items != null && !items.isEmpty()) {
            java.text.NumberFormat formaterClp = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CL"));
            formaterClp.setMaximumFractionDigits(0);

            for (int i = 0; i < items.size(); i++) {
                ItemAngular item = items.get(i);
                totalFilasPintadas++;

                int cantidad = (item.getCantidad() != null) ? item.getCantidad() : 0;
                long valorUnitario = (item.getValorProducto() != null) ? item.getValorProducto() : 0L;
                long valorTotal = (cantidad > 0 && valorUnitario > 0) ? (long) cantidad * valorUnitario : 0L;

                String strUnitario = formaterClp.format(valorUnitario);
                String strTotal = formaterClp.format(valorTotal);
                String descripcion = (item.getDescripcionProducto() != null) ? item.getDescripcionProducto() : "";

                filasHtml.append("<tr>")
                        .append("  <td style=\"text-align: center;\">").append(totalFilasPintadas).append("</td>")
                        .append("  <td>").append(descripcion).append("</td>")
                        .append("  <td style=\"text-align: center;\">").append(cantidad).append("</td>")
                        .append("  <td style=\"text-align: center;\">").append(strUnitario).append("</td>")
                        .append("  <td style=\"text-align: center;\">").append(strTotal).append("</td>")
                        .append("</tr>");
            }
        }

        // 3. Rellenar con filas vacías si no alcanzamos el mínimo de 5
        // Nota: Usamos "&nbsp;" (non-breaking space) para asegurar que el motor del PDF dibuje los bordes de la celda vacía
        /***while (totalFilasPintadas < 10) {
            totalFilasPintadas++;
            filasHtml.append("<tr>")
                    .append("  <td style=\"text-align: center; color: #999;\">").append(totalFilasPintadas).append("</td>")
                    .append("  <td>&nbsp;</td>")
                    .append("  <td>&nbsp;</td>")
                    .append("  <td>&nbsp;</td>")
                    .append("  <td>&nbsp;</td>")
                    .append("</tr>");
        }***/

        return filasHtml.toString();
    }

    public static String generarPlantillaAnulado(OrdenCompraHtml ordenCompraHtml,
                                                    PlantillaStatusImpresionDTO plantillaStatusImpresionDTO) throws JsonProcessingException {
        //Escudo
        String imgTag1 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(0) + "' style='width: 120px; height: auto;'/>";

        String imgTag2 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(1) + "' style='width: 60px; height: auto;'/>";

        String imgTag3 = "<img src='data:image/png;base64," + ordenCompraHtml.getListImagesBase64().get(2) + "' style='width: 60px; height: auto;'/>";

        String htmlReemplazado = ordenCompraHtml.getHtml().replace("{{FOLIO_OC}}", plantillaStatusImpresionDTO.getCodOrdenCompra());

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_OC_CCM}}", imgTag1);

        htmlReemplazado = htmlReemplazado.replace("{{TIMBRE_CONTABILIDAD}}", imgTag2);

        htmlReemplazado = htmlReemplazado.replace("{{TIMBRE_GERENCIA}}", imgTag3);

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

        htmlReemplazado = htmlReemplazado.replace("{{OBSERVACIONES_OC}}", plantillaStatusImpresionDTO.getObservaciones());

        htmlReemplazado = htmlReemplazado.replace("{{PLAZO}}", "CREDITO A 30 DIAS");

        htmlReemplazado = htmlReemplazado.replace("{{TABLA_PRODUCTOS_OC}}", stringAppendTable(plantillaStatusImpresionDTO.getListProductosOrden()));

        java.text.NumberFormat formaterClp = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "CL"));
        formaterClp.setMaximumFractionDigits(0);

        if (plantillaStatusImpresionDTO.getCodDocumentoTributario().equalsIgnoreCase("38") ||
                plantillaStatusImpresionDTO.getCodDocumentoTributario().equalsIgnoreCase("38-c")) {
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_NETO_OC}}", "MONTO TOTAL");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "RETENCIÓN");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_OC}}", "VALOR LÍQUIDO");
        } else {
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_NETO_OC}}", "TOTAL NETO");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_IMPUESTO_OC}}", "IMPUESTO");
            htmlReemplazado = htmlReemplazado.replace("{{TITULO_TOTAL_OC}}", "TOTAL");
        }

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_NETO_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getTotalNeto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_IMPUESTO_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getImpuesto()));
        htmlReemplazado = htmlReemplazado.replace("{{VALOR_TOTAL_OC}}", formaterClp.format(plantillaStatusImpresionDTO.getTotal()));

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
        DateTimeFormatter salidaFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // 3. Formatear
        String resultado = fechaHora.format(salidaFormat);

        return resultado;
    }
}
