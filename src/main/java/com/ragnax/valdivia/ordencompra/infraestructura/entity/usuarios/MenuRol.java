package com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_rol")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRol {

    @Id
    @Column(name = "id_menu_rol")
    private Integer idMenuRol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cod_menu", nullable = false,
            foreignKey = @ForeignKey(name = "fk_menu_rol_menu"))
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_role", nullable = false,
                foreignKey = @ForeignKey(name = "fk_menu_rol_role"))
    private Role role;

    @Column(name = "estado_menu_rol")
    private Boolean estadoMenuRol;
}
