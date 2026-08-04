package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_cana", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidosCana {
    @Id
    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    @Column(name = "direccion_pedido")
    private String direccionPedido;

    /** Codigo del estado ACTUAL del pedido (tipo_estado='EVE' en cana.estados).
     *  El historial vive en estados_pedido, mantenido por trigger (sql/008). */
    @Column(name = "estado_pedido")
    private String estadoPedido;

    @Column(name = "dpi_usuario_pedido")
    private String dpiUsuarioPedido;

    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(name = "salon_entrega")
    private Long salonEntrega;

    @Column(name = "fecha_confirmacion_pedido")
    private LocalDate fechaConfirmacionPedido;

    @Column(name = "fecha_recogido")
    private LocalDate fechaRecogido;

    @Column(name = "pagado")
    private Boolean pagado;

    @Column(name = "estado_pago")
    private String estadoPago;

    @Column(name = "usuario_interno_pedido")
    private String usuarioInternoPedido;

    @Column (name="cod_tipo_evento")
    private String codTipoEvento;

    @Column (name="id_cotizacion")
    private Integer idCotizacion;

    @Column (name ="telefono_cliente_pedido")
    private Integer telefonoClientePedido;

    @Column (name="nombre_cliente_pedidoprov")
    private String nombreClientePedido;

}
