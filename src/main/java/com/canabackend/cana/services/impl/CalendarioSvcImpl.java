package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.CalendarioItemDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.projections.EntregaCalendarioProjection;
import com.canabackend.cana.projections.EventoCalendarioProjection;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.services.CalendarioSvc;
import com.canabackend.cana.utils.CalendarioConstants;
import com.canabackend.cana.utils.MovimientoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CalendarioSvcImpl implements CalendarioSvc {

    /**
     * Tope de dias por consulta. Una vista de mes pide ~42 dias; el limite deja
     * lugar a una vista anual pero evita que un rango absurdo baje media base.
     */
    private static final long MAX_DIAS_RANGO = 400;

    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;
    @Autowired
    private EntregasPedidoRepository entregasPedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioItemDto> obtenerAgenda(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)
                || desde.plusDays(MAX_DIAS_RANGO).isBefore(hasta)) {
            throw new MSCanaException(ErrorEnum.RANGO_CALENDARIO_INVALIDO);
        }

        List<CalendarioItemDto> agenda = new ArrayList<>();
        agregarEventos(agenda, desde, hasta);
        agregarMovimientos(agenda, desde, hasta,
                MovimientoConstants.TIPO_ENTREGA, CalendarioConstants.TIPO_ENTREGA, "Entrega");
        agregarMovimientos(agenda, desde, hasta,
                MovimientoConstants.TIPO_RECOLECCION, CalendarioConstants.TIPO_RECOLECCION, "Recoleccion");

        // Un dia se lee de arriba hacia abajo: primero lo que tiene hora, en
        // orden; despues lo que solo tiene fecha (las entregas pactadas).
        agenda.sort(Comparator
                .comparing(CalendarioItemDto::getFecha)
                .thenComparing(CalendarioItemDto::getFechaHora,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CalendarioItemDto::getCorrelativoPedido,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return agenda;
    }

    private void agregarEventos(List<CalendarioItemDto> agenda, LocalDate desde, LocalDate hasta) {
        // fecha_evento es timestamp: el rango va del arranque del primer dia al
        // arranque del siguiente al ultimo, para incluir el ultimo dia completo.
        for (EventoCalendarioProjection p :
                this.pedidosCanaRepository.findEventosEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())) {

            CalendarioItemDto item = new CalendarioItemDto();
            item.setTipo(CalendarioConstants.TIPO_EVENTO);
            item.setFechaHora(p.getFechaHora());
            item.setFecha(p.getFechaHora() != null ? p.getFechaHora().toLocalDate() : null);
            item.setTitulo(p.getNombreTipoEvento() != null ? p.getNombreTipoEvento() : "Evento");
            item.setCorrelativoPedido(p.getCorrelativoPedido());
            item.setNombreCliente(p.getNombreCliente());
            item.setUbicacion(p.getUbicacion());
            item.setCodigoEstadoPedido(p.getCodigoEstadoPedido());
            item.setNombreEstadoPedido(p.getNombreEstadoPedido());
            agenda.add(item);
        }
    }

    /**
     * Entregas y recolecciones se leen con la misma consulta: solo cambia el
     * tipo de movimiento y, con el, la fecha de agenda que devuelve (la ida se
     * pacta con fecha_entrega y la vuelta con fecha_recogido).
     */
    private void agregarMovimientos(List<CalendarioItemDto> agenda, LocalDate desde, LocalDate hasta,
                                     String tipoMovimiento, String tipoCalendario, String titulo) {
        for (EntregaCalendarioProjection p :
                this.entregasPedidoRepository.findEntregasEntre(tipoMovimiento, desde, hasta)) {

            CalendarioItemDto item = new CalendarioItemDto();
            item.setTipo(tipoCalendario);
            item.setFecha(p.getFecha());
            item.setFechaHora(null);   // la fecha de agenda es DATE: no hay hora pactada
            item.setTitulo(titulo);
            item.setCorrelativoPedido(p.getCorrelativoPedido());
            item.setNombreCliente(p.getNombreCliente());
            item.setUbicacion(p.getUbicacion());
            item.setCodigoEstadoPedido(p.getCodigoEstadoPedido());
            item.setNombreEstadoPedido(p.getNombreEstadoPedido());
            item.setIdEntrega(p.getIdEntrega());
            item.setEntregaFinalizada(p.getEntregaFinalizada());
            item.setCantidadViajesReales(p.getCantidadViajesReales());
            item.setCantidadViajesAproximados(p.getCantidadViajesAproximados());
            agenda.add(item);
        }
    }
}
