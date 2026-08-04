package com.canabackend.cana.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Contadores del tablero de entregas.
 *
 * enCurso, programadas, atrasadas y sinFecha parten en cuatro grupos disjuntos
 * a las entregas NO finalizadas, segun la fecha de entrega pactada del pedido:
 * la suma de los cuatro es el total de entregas abiertas. Que cierren permite
 * verificar el tablero de un vistazo.
 */
@Data
@AllArgsConstructor
public class EstadisticasEntregasDto {
    /** No finalizadas cuya fecha de entrega es hoy: lo que toca despachar. */
    Long enCurso;
    /** No finalizadas con fecha de entrega futura: ya tienen fecha y viajes estimados. */
    Long programadas;
    /** No finalizadas cuya fecha de entrega ya paso. Deberia ser 0. */
    Long atrasadas;
    /**
     * No finalizadas sin fecha de entrega. Solo puede haberlas entre los pedidos
     * anteriores a que fecha_entrega pasara a ser obligatoria.
     */
    Long sinFecha;
    /** Entregas con pedido_finalizado = true. */
    Long finalizadas;
    /** Viajes cuya fecha_inicio_viaje cae dentro del dia de hoy. */
    Long viajesHoy;
    /**
     * SUM(reales) - SUM(aproximados) considerando unicamente las entregas ya
     * finalizadas. Positivo = se necesitaron mas viajes de los estimados.
     * Las entregas en curso quedan fuera a proposito: sus viajes reales
     * todavia estan creciendo y el desvio siempre daria negativo.
     */
    Long desvioViajes;
}
