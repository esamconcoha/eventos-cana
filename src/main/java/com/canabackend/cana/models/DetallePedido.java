package com.canabackend.cana.models;

import com.canabackend.cana.utils.DescuentoConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_pedido", schema = "cana")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedido implements LineaConDescuento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "cantidad_item_pedido")
    private Double cantidadItemPedido;

    @Column(name = "correlativo_pedido")
    private String correlativoPedido;

    /**
     * Descuento de la linea. Ver {@link com.canabackend.cana.utils.DescuentoConstants}:
     * NIN sin descuento, POR porcentaje, MON monto fijo, EXO exoneracion total.
     */
    @Column(name = "tipo_descuento")
    private String tipoDescuento = DescuentoConstants.TIPO_NINGUNO;

    /** Porcentaje (0..100) si el tipo es POR, monto en Q si es MON; 0 en NIN y EXO. */
    @Column(name = "valor_descuento")
    private double valorDescuento;

    /** Por que se otorgo. Se arrastra tal cual de la cotizacion al pedido. */
    @Column(name = "motivo_descuento")
    private String motivoDescuento;


}
