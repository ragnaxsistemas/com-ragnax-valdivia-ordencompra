package com.ragnax.valdivia.ordencompra.infraestructura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pendiente_anulacion_orden_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendienteAnulacionOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pendiente_anulacion_orden_compra")
    private Integer idPendienteAnulacionOrdenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_orden_compra", nullable = false,
                foreignKey = @ForeignKey(name = "fk_paoc_orden"))
    private OrdenCompra ordenCompra;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "active")
    private Boolean activa;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "observacion")
    private String observacion;
}
