package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import com.ragnax.valdivia.ordencompra.infraestructura.entity.usuarios.Usuarios;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_orden_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_orden_compra")
    private Integer idStatusOrdenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_orden_compra", nullable = false,
                foreignKey = @ForeignKey(name = "fk_soc_orden"))
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_estado_oc", nullable = false,
                foreignKey = @ForeignKey(name = "fk_soc_status"))
    private EstadoOc estadoOc;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "observacion")
    private String observacion;
}
