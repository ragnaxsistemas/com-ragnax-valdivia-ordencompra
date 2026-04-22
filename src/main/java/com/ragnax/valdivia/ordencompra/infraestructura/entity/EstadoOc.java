package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "estado_oc")
public class EstadoOc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_oc")
    private Integer idEstadoOc;

    @Column(name = "codigo_estado_oc", nullable = false, unique = true, length = 30)
    private String codigoEstadoOc;

    @Column(name = "nombre_estado_oc", nullable = false, unique = true, length = 50)
    private String nombreEstadoOc;

    @Column(name = "descripcion")
    private String descripcion;

    //@OneToMany(mappedBy = "estadoOc", fetch = FetchType.LAZY)
    //private List<StatusOrdenCompra> statusOrdenes;
}
