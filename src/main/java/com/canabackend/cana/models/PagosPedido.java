package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_pedido", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagosPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    @Column(name = "monto_pago")
    private Double montoPago;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "tipo_pago")
    private String tipoPago;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "referencia_pago")
    private String referenciaPago;

    @Column(name = "usuario_registro")
    private String usuarioRegistro;

    @Column(name = "estado_registro")
    private Boolean estadoRegistro;

    @Column(name = "fecha_creo")
    private LocalDateTime fechaCreo;
}
