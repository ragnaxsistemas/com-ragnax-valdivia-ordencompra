package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "documento_tributario")
public class DocumentoTributario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento_tributario")
    private Integer idDocumentoTributario;

    @Column(name = "nombre_documento_tributario", length = 50)
    private String nombreDocumentoTributario;

    @Column(name = "codigo_documento_tributario", length = 60)
    private String codigoDocumentoTributario;

    @Column(name = "descripcion_documento_tributario", length = 60)
    private String descripcionDocumentoTributario;

    @Column(name = "impuesto", precision = 5, scale = 2)
    private BigDecimal impuesto;

    @Column(name = "active")
    private Boolean active;

    //@JsonIgnore
    //@OneToMany(mappedBy = "documentoTributario", fetch = FetchType.LAZY)
    //private List<OrdenCompra> ordenesCompra;
}
