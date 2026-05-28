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
@Table(name = "orden_compra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_compra")
    private Integer idOrdenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_documento_tributario",
                foreignKey = @ForeignKey(name = "fk_oc_documento"))
    private DocumentoTributario documentoTributario;

    @Column(name = "id_unidad", nullable = false)
    private Integer idUnidad;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_proveedor",
            foreignKey = @ForeignKey(name = "fk_oc_proveedor"))
    private Proveedor proveedor;

    @Column(name = "codigo_giro_proveedor")
    private String codigoGiroProveedor;

    @Column(name = "codigo_orden_compra")
    private String codigoOrdenCompra;

    @Column(name = "nombre_orden_compra")
    private String nombreOrdenCompra;

    @Column(name = "observaciones")
    private String observaciones;

    @Lob
    @Column(name = "list_productos_orden", columnDefinition = "LONGTEXT")
    private String listProductosOrden;

    @Column(name = "total_neto")
    private Integer totalNeto;

    @Column(name = "impuesto")
    private Integer impuesto;

    @Column(name = "total")
    private Integer total;

    @Column(name = "username_autorizador")
    private String usernameAutorizador;

    @Column(name = "username_anulador")
    private String usernameAnulador;

    @Column(name = "username_confirmador")
    private String usernameConfirmador;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

}
