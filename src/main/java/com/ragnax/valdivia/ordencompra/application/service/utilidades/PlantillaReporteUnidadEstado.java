/***package com.ragnax.valdivia.ordencompra.application.service.utilidades;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.;
import com.ragnax.valdivia.ordencompra.application.service.model.OrdenCompraHtml;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.ItemAngular;
import com.ragnax.valdivia.ordencompra.infraestructura.controller.dto.PlantillaStatusDTO;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.EstadoOc;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.OrdenCompraConfirmada;
import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Unidad;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlantillaReporteUnidadEstado {
    //Lista de unidades, Lista de Estados
    public static String generarPlantillaReporte(Map<String, Map<String, List<PlantillaStatusDTO>>> agrupadoPorUnidadYEstado,
                                List<EstadoOc> listaEstadoOc,
                                List<Unidad> listaUnidad ) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Formateador de moneda chilena (o estándar sin decimales)
        NumberFormat formatoMonto = NumberFormat.getInstance(new Locale("es", "CL"));

        // 1. Recorremos cada Unidad Requiriente
        for (Map.Entry<String, Map<String, List<PlantillaStatusDTO>>> entryUnidad : agrupadoPorUnidadYEstado.entrySet()) {

            String unidadRequiriente = entryUnidad.getKey();

            Map<String, List<PlantillaStatusDTO>> mapaEstados = entryUnidad.getValue();

            htmlBuilder.append("<div class=\"border border-slate-300 rounded-lg overflow-hidden shadow-sm bg-white mb-8\">\n");

            // --- BLOQUE UNIDAD REQUIRENTE ---
            htmlBuilder.append("    <div class=\"bg-slate-50 px-5 py-3 border-b border-slate-200 flex justify-between items-center\">\n")
                    .append("        <div class=\"flex items-center gap-2\">\n")
                    .append("            <span class=\"text-xs font-bold text-black uppercase\">UNIDAD REQUIRENTE:</span>\n")
                    .append("            <span class=\"border border-ccmCyan font-bold text-xs uppercase px-3 py-1 rounded bg-white tracking-wide text-black\">\n")
                    .append("                ").append(unidadRequiriente != null ? unidadRequiriente : "SIN UNIDAD").append("\n")
                    .append("            </span>\n")
                    .append("        </div>\n")
                    .append("    </div>\n");

            // --- CONTENEDOR DE ESTADOS DENTRO DE ESTA UNIDAD ---
            htmlBuilder.append("    <div class=\"p-5 space-y-8\">\n");

            // 2. Recorremos cada Estado de la Unidad actual
            for (Map.Entry<String, List<PlantillaStatusDTO>> entryEstado : mapaEstados.entrySet()) {
                String estadoOc = entryEstado.getKey();
                List<PlantillaStatusDTO> listaOcs = entryEstado.getValue();

                // Calculamos el acumulado total para este Estado
                long totalEstado = 0;

                htmlBuilder.append("        <div>\n")
                        .append("            <div class=\"flex items-center gap-2 mb-2\">\n")
                        .append("                <span class=\"w-2 h-2 rounded-full bg-slate-800\"></span>\n")
                        .append("                <h3 class=\"text-xs font-bold uppercase tracking-wider text-black\">Estado: ")
                        .append(estadoOc != null ? estadoOc : "DESCONOCIDO").append("</h3>\n")
                        .append("            </div>\n")
                        .append("            <div class=\"border border-ccmCyan/60 rounded overflow-hidden\">\n")
                        .append("                <table class=\"min-w-full text-left text-xs font-bold\">\n")
                        .append("                    <thead class=\"border-b border-ccmCyan/60 text-black uppercase bg-white\">\n")
                        .append("                        <tr>\n")
                        .append("                            <th class=\"px-4 py-3 font-bold\">FOLIO</th>\n")
                        .append("                            <th class=\"px-4 py-3 font-bold\">FECHA</th>\n")
                        .append("                            <th class=\"px-4 py-3 font-bold\">GLOSA / DESCRIPCIÓN</th>\n")
                        .append("                            <th class=\"px-4 py-3 font-bold text-right\">MONTO TOTAL</th>\n")
                        .append("                        </tr>\n")
                        .append("                    </thead>\n")
                        .append("                    <tbody class=\"divide-y divide-slate-100 bg-white text-black\">\n");

                // 3. Recorremos la lista de OCs pertenecientes a este Estado
                for (PlantillaStatusDTO oc : listaOcs) {
                    long montoOc = oc.getTotal() != null ? oc.getTotal().longValue() : 0;
                    totalEstado += montoOc;

                    htmlBuilder.append("                        <tr class=\"hover:bg-slate-50/80 transition-colors\">\n")
                            .append("                            <td class=\"px-4 py-3.5\">").append(oc.getCodOrdenCompra() != null ? oc.getCodOrdenCompra() : "").append("</td>\n")
                            .append("                            <td class=\"px-4 py-3.5 font-normal\">").append(oc.getFechaOrdenCompra() != null ? oc.getFechaOrdenCompra() : "").append("</td>\n")
                            .append("                            <td class=\"px-4 py-3.5 font-normal\">").append(oc.getNombreOrdenCompra() != null ? oc.getNombreOrdenCompra() : "").append("</td>\n")
                            .append("                            <td class=\"px-4 py-3.5 text-right font-bold text-black\">$ ").append(formatoMonto.format(montoOc)).append("</td>\n")
                            .append("                        </tr>\n");
                }

                // --- FOOTER CON EL TOTAL ACUMULADO DEL ESTADO ---
                htmlBuilder.append("                    </tbody>\n")
                        .append("                    <tfoot class=\"bg-slate-50 border-t border-slate-200 text-black\">\n")
                        .append("                        <tr>\n")
                        .append("                            <td colspan=\"3\" class=\"px-4 py-2.5 text-right font-bold text-xs uppercase tracking-wider\">Total ").append(estadoOc).append(":</td>\n")
                        .append("                            <td class=\"px-4 py-2.5 text-right font-black text-sm border-l border-slate-200 bg-slate-100/50 text-black\">$ ").append(formatoMonto.format(totalEstado)).append("</td>\n")
                        .append("                        </tr>\n")
                        .append("                    </tfoot>\n")
                        .append("                </table>\n")
                        .append("            </div>\n")
                        .append("        </div>\n");
            }

            htmlBuilder.append("    </div>\n") // Cierre de p-5 space-y-8
                    .append("</div>\n\n"); // Cierre del contenedor principal de la Unidad
        }

        return htmlBuilder.toString();
    }

}***/
