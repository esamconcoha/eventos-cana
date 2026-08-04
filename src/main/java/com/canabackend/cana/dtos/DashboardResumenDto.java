package com.canabackend.cana.dtos;

import lombok.Data;

import java.util.List;

/**
 * Resumen de la pantalla de inicio. Cada contador sale de un dato real; no hay
 * ninguno estimado ni de relleno.
 */
@Data
public class DashboardResumenDto {

    /** Cotizaciones creadas o pendientes de confirmar (estados 'C' y 'P'). */
    Long cotizacionesPendientes;

    /**
     * Items activos con unidades faltantes registradas. No es "stock bajo":
     * items_cana no tiene un minimo configurable contra el cual comparar, asi
     * que cualquier umbral seria inventado. cantidad_faltantes si es un dato real.
     */
    Long articulosConFaltantes;

    /** Eventos con fecha dentro de los proximos 7 dias. */
    Long eventosProximos7Dias;

    Long usuariosActivos;

    /** Entregas abiertas cuya fecha pactada es hoy: lo que toca despachar. */
    Long entregasHoy;

    /** Recolecciones abiertas sin fecha agendada: la cola de trabajo real. */
    Long recoleccionesSinAgendar;

    List<EventoProximoDto> proximosEventos;
}
