package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "adjunto_orden_compra")
public class AdjuntoOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adjunto_oc")
    private Integer idAdjuntoOc;

    // Relación ManyToOne con la Orden de Compra
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_orden_compra", nullable = false)
    private OrdenCompra idOrdenCompra;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 512)
    private String rutaArchivo;

    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido;

    @Column(name = "tamano_bytes")
    private Integer tamanoBytes;

    @Column(name = "fecha_subida", nullable = false, insertable = false, updatable = false)
    private LocalDateTime fechaSubida;

    @Column(name = "id_usuario_sube", nullable = false)
    private Integer idUsuarioSube;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

}
