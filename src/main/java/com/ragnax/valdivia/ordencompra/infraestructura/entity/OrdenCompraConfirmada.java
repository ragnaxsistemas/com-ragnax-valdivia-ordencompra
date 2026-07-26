package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orden_compra_confirmada")
public class OrdenCompraConfirmada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_compra_confirmada")
    private Integer idOrdenCompraConfirmada;

    // ============================================================
    // RELACIONES (Foreign Keys)
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_orden_compra", nullable = false)
    private OrdenCompra idOrdenCompra;

    // ============================================================
    // CAMPOS DE LA ORDEN DE COMPRA
    // ============================================================

    @Column(name = "codigo_orden_compra")
    private String codigoOrdenCompra;

    @Column(name = "fecha_emision")
    private String fechaEmision;

    // ============================================================
    // HISTÓRICO DOCUMENTO TRIBUTARIO
    // ============================================================

    @Column(name = "nombre_documento_tributario", length = 50)
    private String nombreDocumentoTributario;

    @Column(name = "codigo_documento_tributario", length = 60)
    private String codigoDocumentoTributario;

    @Column(name = "descripcion_documento_tributario", length = 60)
    private String descripcionDocumentoTributario;

    @Column(name = "impuesto_tributario", precision = 5, scale = 2)
    private String impuestoTributario;

    @Column(name = "nombre_unidad")
    private String nombreUnidad;

    // ============================================================
    // HISTÓRICO PROVEEDOR Y DIRECCIÓN
    // ============================================================

    @Column(name = "rut_proveedor", length = 50)
    private String rutProveedor;

    @Column(name = "nombre_proveedor", length = 50)
    private String nombreProveedor;

    @Column(name = "razon_social_proveedor", length = 60)
    private String razonSocialProveedor;

    @Column(name = "direccion_proveedor", length = 255)
    private String direccionProveedor;

    @Column(name = "telefono_contacto_proveedor")
    private String telefonoContactoProveedor;

    @Column(name = "email_proveedor")
    private String emailProveedor;

    @Column(name = "nombre_region_proveedor")
    private String nombreRegionProveedor;

    @Column(name = "nombre_comuna_proveedor")
    private String nombreComunaProveedor;

    @Column(name = "codigo_giro_proveedor")
    private String codigoGiroProveedor;

    @Column(name = "nombre_giro_proveedor")
    private String nombreGiroProveedor;

    // ============================================================
    // DETALLES Y TOTALES
    // ============================================================

    @Column(name = "nombre_orden_compra", length = 450)
    private String nombreOrdenCompra;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Lob
    @Column(name = "list_productos_orden", columnDefinition = "LONGTEXT")
    private String listProductosOrden;

    @Column(name = "total_neto")
    private String totalNeto;

    @Column(name = "impuesto")
    private String impuesto;

    @Column(name = "total")
    private String total;

    // ============================================================
    // USUARIOS (Guardados como IDs según tu script)
    // ============================================================

    @Column(name = "nombre_usuario_creador", nullable = false)
    private String nombreUsuarioCreador;

    @Column(name = "nombre_usuario_solicitante")
    private String nombreUsuarioSolicitante;

    @Column(name = "nombre_usuario_autorizador")
    private String nombreUsuarioAutorizador;

    @Column(name = "nombre_usuario_anulador")
    private String nombreUsuarioAnulador;

    @Column(name = "nombre_usuario_confirmador")
    private String nombreUsuarioConfirmador;

    // ============================================================
    // ESTADOS Y FECHAS
    // ============================================================

    @Column(name = "fecha_confirmacion", nullable = false, insertable = false, updatable = false)
    private String fechaConfirmacion;

    @Column(name = "anulada", nullable = false)
    private Boolean anulada;

    @Column(name = "fecha_anulacion")
    private String fechaAnulacion;

}