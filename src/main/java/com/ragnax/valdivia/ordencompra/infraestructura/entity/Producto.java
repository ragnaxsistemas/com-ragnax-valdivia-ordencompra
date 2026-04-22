package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre_producto", length = 50)
    private String nombreProducto;

    @Column(name = "codigo_producto", length = 60)
    private String codigoProducto;

    @Column(name = "descripcion_producto", length = 60)
    private String descripcionProducto;

    @Column(name = "valor_presupuesto")
    private Integer valorProducto;

    @Column(name = "active")
    private Boolean active;
}
