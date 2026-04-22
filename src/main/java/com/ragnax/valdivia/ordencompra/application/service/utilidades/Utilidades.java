package com.ragnax.valdivia.ordencompra.application.service.utilidades;

import java.time.LocalDate;
import java.util.zip.CRC32;

public class Utilidades {

    public static String formatearRut(String rutLimpio) {
        if (rutLimpio == null || rutLimpio.isBlank()) {
            throw new IllegalArgumentException("RUT no puede ser vacío");
        }

        // limpiar por si llega con algo igual
        String rut = rutLimpio.trim().toUpperCase().replaceAll("[^0-9K]", "");

        if (rut.length() < 2) {
            throw new IllegalArgumentException("RUT inválido: " + rutLimpio);
        }

        String cuerpo = rut.substring(0, rut.length() - 1);
        String dv     = rut.substring(rut.length() - 1);

        // agregar puntos cada 3 dígitos desde la derecha
        StringBuilder cuerpoFormateado = new StringBuilder();
        int contador = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            if (contador > 0 && contador % 3 == 0) {
                cuerpoFormateado.insert(0, ".");
            }
            cuerpoFormateado.insert(0, cuerpo.charAt(i));
            contador++;
        }

        return cuerpoFormateado + "-" + dv;
    }

    public static String generarCodigo(Integer idOrdenCompra) {
        String año = String.valueOf(LocalDate.now().getYear());

        // ID con ceros a la izquierda (4 dígitos)
        String idFormateado = String.format("%04d", idOrdenCompra);

        // Hash corto: CRC32 del ID → 4 chars hex en mayúsculas
        CRC32 crc = new CRC32();
        crc.update(idOrdenCompra.toString().getBytes());
        String hash = String.format("%04X", crc.getValue() & 0xFFFF);

        return "OC-" + año + "-" + idFormateado + "-" + hash;
    }

    public static String generarCodigoProducto(Integer idProducto) {
        String año = String.valueOf(LocalDate.now().getYear());

        // ID con ceros a la izquierda (4 dígitos)
        String idFormateado = String.format("%04d", idProducto);

        // Hash corto: CRC32 del ID → 4 chars hex en mayúsculas
        CRC32 crc = new CRC32();
        crc.update(idProducto.toString().getBytes());
        String hash = String.format("%04X", crc.getValue() & 0xFFFF);

        return "PROD-" + año + "-" + idFormateado + "-" + hash;
    }
}
