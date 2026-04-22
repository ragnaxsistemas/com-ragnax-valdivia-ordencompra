package com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 45)
    private String nombre;

    //@OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    //private List<MenuRol> menuRoles;

    //@OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    //private List<Usuarios> usuarios;
}
