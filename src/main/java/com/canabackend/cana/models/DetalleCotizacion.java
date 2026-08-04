package com.canabackend.cana.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_cotizacion", schema = "cana")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_cotizacion")
    private Long idDetalleCotizacion;

    @Column(name = "id_item")
    private Long idItem;

    @Column(name = "cantidad_item_cotizacion")
    private Double cantidadItemCotizacion;

    @Column(name = "id_cotizacion")
    private Long idCotizacion;


}
