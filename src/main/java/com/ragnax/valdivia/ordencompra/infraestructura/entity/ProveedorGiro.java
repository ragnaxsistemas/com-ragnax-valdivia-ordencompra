package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Comunas;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(ProveedorGiroId.class) // Referencia a la clase ID creada arriba
@Table(name = "proveedor_giro")
public class ProveedorGiro {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_proveedor",
            foreignKey = @ForeignKey(name = "fk_proveedor_giro_proveedor"))
    private Proveedor idProveedor;

    @Id
    @Column(name = "id_giro")
    private Integer idGiro;

}
