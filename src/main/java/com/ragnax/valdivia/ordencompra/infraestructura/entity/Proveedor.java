package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(name = "rut_proveedor", length = 50)
    private String rutProveedor;

    @Column(name = "nombre_proveedor", length = 50)
    private String nombreProveedor;

    @Column(name = "razon_social_proveedor", length = 60)
    private String razonSocialProveedor;

    @Column(name = "direccion", length = 60)
    private String direccion;

    /***@Column(name = "giro", length = 60)
    private String giro;***/

    @Column(name = "telefono_contacto_proveedor")
    private String telefonoContactoProveedor;

    @Column(name = "email_proveedor")
    private String emailProveedor;

    @Column(name = "id_comuna")
    private Integer idComuna;

    @Column(name = "active")
    private Boolean active;

}
