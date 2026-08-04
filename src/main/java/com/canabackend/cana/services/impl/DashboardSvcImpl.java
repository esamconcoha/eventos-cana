package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.DashboardResumenDto;
import com.canabackend.cana.dtos.EventoProximoDto;
import com.canabackend.cana.projections.DashboardResumenProjection;
import com.canabackend.cana.projections.EventoCalendarioProjection;
import com.canabackend.cana.repositories.DashboardRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.services.DashboardSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardSvcImpl implements DashboardSvc {

    /** Cuantos eventos se listan en "Proximos eventos". */
    private static final int MAX_PROXIMOS_EVENTOS = 5;

    /**
     * Ventana hacia adelante para buscar los proximos eventos. Acota la consulta
     * sin arriesgar que la lista quede corta: si en 6 meses no hay 5 eventos, es
     * que efectivamente no los hay.
     */
    private static final int MESES_VENTANA_PROXIMOS = 6;

    @Autowired
    private DashboardRepository dashboardRepository;
    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenDto obtenerResumen() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        DashboardResumenDto dto = new DashboardResumenDto();

        DashboardResumenProjection p =
                this.dashboardRepository.obtenerResumen(hoy, ahora, ahora.plusDays(7));
        dto.setCotizacionesPendientes(valorOCero(p != null ? p.getCotizacionesPendientes() : null));
        dto.setArticulosConFaltantes(valorOCero(p != null ? p.getArticulosConFaltantes() : null));
        dto.setEventosProximos7Dias(valorOCero(p != null ? p.getEventosProximos7Dias() : null));
        dto.setUsuariosActivos(valorOCero(p != null ? p.getUsuariosActivos() : null));
        dto.setEntregasHoy(valorOCero(p != null ? p.getEntregasHoy() : null));
        dto.setRecoleccionesSinAgendar(valorOCero(p != null ? p.getRecoleccionesSinAgendar() : null));

        dto.setProximosEventos(buscarProximosEventos(ahora));
        return dto;
    }

    /**
     * Reutiliza la consulta del calendario: ya devuelve cliente, ubicacion,
     * tipo de evento y estado, y viene ordenada por fecha. Solo se recorta.
     */
    private List<EventoProximoDto> buscarProximosEventos(LocalDateTime ahora) {
        List<EventoProximoDto> proximos = new ArrayList<>();
        for (EventoCalendarioProjection p :
                this.pedidosCanaRepository.findEventosEntre(ahora, ahora.plusMonths(MESES_VENTANA_PROXIMOS))) {
            if (proximos.size() >= MAX_PROXIMOS_EVENTOS) {
                break;
            }
            EventoProximoDto evento = new EventoProximoDto();
            evento.setCorrelativoPedido(p.getCorrelativoPedido());
            evento.setFechaEvento(p.getFechaHora());
            evento.setNombreCliente(p.getNombreCliente());
            evento.setNombreTipoEvento(p.getNombreTipoEvento());
            evento.setUbicacion(p.getUbicacion());
            evento.setCodigoEstadoPedido(p.getCodigoEstadoPedido());
            evento.setNombreEstadoPedido(p.getNombreEstadoPedido());
            proximos.add(evento);
        }
        return proximos;
    }

    private long valorOCero(Long valor) {
        return valor != null ? valor : 0L;
    }
}
