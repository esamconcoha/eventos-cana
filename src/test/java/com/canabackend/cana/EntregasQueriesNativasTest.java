package com.canabackend.cana;

import com.canabackend.cana.projections.DashboardResumenProjection;
import com.canabackend.cana.projections.EntregaCalendarioProjection;
import com.canabackend.cana.projections.EntregaListProjection;
import com.canabackend.cana.projections.EstadisticasProjection;
import com.canabackend.cana.projections.EventoCalendarioProjection;
import com.canabackend.cana.projections.ItemEntregaProjection;
import com.canabackend.cana.projections.ItemRecoleccionProjection;
import com.canabackend.cana.projections.PedidoDisponibleProjection;
import com.canabackend.cana.projections.ViajeItemProjection;
import com.canabackend.cana.repositories.DashboardRepository;
import com.canabackend.cana.repositories.DetalleViajeItemsRepository;
import com.canabackend.cana.repositories.DetalleViajeRepository;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.utils.MovimientoConstants;
import com.canabackend.cana.utils.PedidoConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Ejecuta contra la BD real todas las consultas nativas del modulo de entregas.
 *
 * El SQL de una @Query nativa no lo valida el compilador: un nombre de columna
 * mal escrito o un cast invalido solo aparece cuando la consulta corre. Este
 * test las ejecuta todas en modo lectura para que ese error salga aca y no en
 * la primera pantalla que las use.
 *
 * No valida datos ni cantidades (dependen de lo que haya cargado en la base),
 * solo que cada consulta sea SQL valido y que sus alias mapeen a la proyeccion.
 */
@SpringBootTest
@Transactional(readOnly = true)
class EntregasQueriesNativasTest {

    @Autowired
    private EntregasPedidoRepository entregasPedidoRepository;
    @Autowired
    private DetalleViajeRepository detalleViajeRepository;
    @Autowired
    private DetalleViajeItemsRepository detalleViajeItemsRepository;
    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;
    @Autowired
    private DashboardRepository dashboardRepository;

    @Test
    @DisplayName("listarEntregas: join a pedidos_cana/usuarios/estados y alias de la proyeccion")
    void findEntregasConCliente() {
        assertDoesNotThrow(() -> {
            for (EntregaListProjection p : entregasPedidoRepository.findEntregasConCliente(MovimientoConstants.TIPO_ENTREGA)) {
                // Tocar los getters fuerza la lectura de cada alias
                p.getIdEntrega();
                p.getCorrelativoPedido();
                p.getNombreClientePedido();
                p.getDireccionPedido();
                p.getCantidadViajesAproximados();
                p.getCantidadViajesReales();
                p.getPedidoFinalizado();
                p.getFechaInicioEntrega();
                p.getFechaFinEntrega();
                p.getFechaEvento();
                p.getFechaEntrega();
                p.getIdEstadoPedido();
                p.getCodigoEstadoPedido();
                p.getNombreEstadoPedido();
            }
        });
    }

    @Test
    @DisplayName("estadisticas: count(*) filter (where ...) y la particion por fecha_entrega")
    void obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();

        EstadisticasProjection p =
                entregasPedidoRepository.obtenerEstadisticas(
                        MovimientoConstants.TIPO_ENTREGA, hoy, inicioDelDia, inicioDelDia.plusDays(1));

        assertNotNull(p, "la consulta de estadisticas siempre debe devolver una fila");
        assertNotNull(p.getEnCurso());
        assertNotNull(p.getProgramadas());
        assertNotNull(p.getAtrasadas());
        assertNotNull(p.getSinFecha());
        assertNotNull(p.getFinalizadas());
        assertNotNull(p.getViajesHoy());
        assertNotNull(p.getDesvioViajes());
    }

    @Test
    @DisplayName("pedidosDisponibles: expansion del IN con lista de codigos de estado")
    void findPedidosDisponibles() {
        assertDoesNotThrow(() -> {
            for (PedidoDisponibleProjection p :
                    entregasPedidoRepository.findPedidosDisponibles(
                            PedidoConstants.ESTADOS_EVENTO_ADMITEN_ENTREGA, MovimientoConstants.TIPO_ENTREGA)) {
                p.getCorrelativoPedido();
                p.getNombreClientePedido();
                p.getDireccionPedido();
                p.getFechaEvento();
                p.getFechaEntrega();
                p.getSalonEntrega();
                p.getCodigoEstadoPedido();
                p.getNombreEstadoPedido();
                p.getTotalItems();
            }
        });
    }

    @Test
    @DisplayName("itemsEntrega: subconsulta agrupada y casts a double precision")
    void findItemsEntrega() {
        assertDoesNotThrow(() -> {
            // Se ejecuta aunque no exista la entrega 1: interesa validar el SQL,
            // no que devuelva filas.
            for (ItemEntregaProjection p : entregasPedidoRepository.findItemsEntrega(1L)) {
                p.getIdItem();
                p.getNombreItem();
                p.getCantidadPedida();
                p.getCantidadEnviada();
            }
        });
    }

    @Test
    @DisplayName("itemsPorEntrega: join viaje-item y cast de numeric a double")
    void findItemsPorEntrega() {
        assertDoesNotThrow(() -> {
            for (ViajeItemProjection p :
                    detalleViajeItemsRepository.findItemsPorEntrega(1L)) {
                p.getIdDetalle();
                p.getIdViaje();
                p.getIdItem();
                p.getNombreItem();
                p.getCantidadItem();
            }
        });
    }

    @Test
    @DisplayName("calendario: eventos por rango de timestamp y nombre de tipo por subconsulta")
    void findEventosEntre() {
        LocalDateTime desde = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        assertDoesNotThrow(() -> {
            for (EventoCalendarioProjection p :
                    pedidosCanaRepository.findEventosEntre(desde, desde.plusMonths(1))) {
                p.getCorrelativoPedido();
                p.getFechaHora();
                p.getNombreCliente();
                p.getUbicacion();
                p.getNombreTipoEvento();
                p.getCodigoEstadoPedido();
                p.getNombreEstadoPedido();
            }
        });
    }

    @Test
    @DisplayName("calendario: entregas por rango de fecha pactada")
    void findEntregasEntre() {
        LocalDate desde = LocalDate.now().withDayOfMonth(1);
        assertDoesNotThrow(() -> {
            for (EntregaCalendarioProjection p :
                    entregasPedidoRepository.findEntregasEntre(
                            MovimientoConstants.TIPO_ENTREGA, desde, desde.plusMonths(1))) {
                p.getIdEntrega();
                p.getCorrelativoPedido();
                p.getFecha();
                p.getNombreCliente();
                p.getUbicacion();
                p.getCantidadViajesReales();
                p.getCantidadViajesAproximados();
                p.getEntregaFinalizada();
                p.getCodigoEstadoPedido();
                p.getNombreEstadoPedido();
            }
        });
    }

    @Test
    @DisplayName("recoleccion: CTE que compara lo entregado contra lo recolectado")
    void findItemsRecoleccion() {
        assertDoesNotThrow(() -> {
            for (ItemRecoleccionProjection p :
                    entregasPedidoRepository.findItemsRecoleccion(1L)) {
                p.getIdItem();
                p.getNombreItem();
                p.getCantidadEntregada();
                p.getCantidadRecolectada();
            }
        });
    }

    @Test
    @DisplayName("recoleccion: listado, estadisticas y calendario con tipo REC")
    void consultasConTipoRecoleccion() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        assertDoesNotThrow(() -> {
            entregasPedidoRepository.findEntregasConCliente(MovimientoConstants.TIPO_RECOLECCION);
            entregasPedidoRepository.findEntregasEntre(
                    MovimientoConstants.TIPO_RECOLECCION, hoy.withDayOfMonth(1), hoy.plusMonths(1));
        });
        // La particion por fecha usa fecha_recogido cuando el tipo es REC
        assertNotNull(entregasPedidoRepository.obtenerEstadisticas(
                MovimientoConstants.TIPO_RECOLECCION, hoy, inicioDelDia, inicioDelDia.plusDays(1)));
    }

    @Test
    @DisplayName("dashboard: los seis contadores de la pantalla de inicio")
    void obtenerResumenDashboard() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        DashboardResumenProjection p =
                dashboardRepository.obtenerResumen(hoy, ahora, ahora.plusDays(7));

        assertNotNull(p, "el resumen siempre debe devolver una fila");
        assertNotNull(p.getCotizacionesPendientes());
        assertNotNull(p.getArticulosConFaltantes());
        assertNotNull(p.getEventosProximos7Dias());
        assertNotNull(p.getUsuariosActivos());
        assertNotNull(p.getEntregasHoy());
        assertNotNull(p.getRecoleccionesSinAgendar());
    }

    @Test
    @DisplayName("consultas derivadas del nombre del metodo")
    void consultasDerivadas() {
        assertDoesNotThrow(() -> {
            detalleViajeRepository.findByIdEntregaOrderByFechaInicioViajeAscIdViajeAsc(1L);
            detalleViajeRepository.countByIdEntrega(1L);
            entregasPedidoRepository.existsByCorrelativoPedidoAndTipoMovimiento(
                    "PED-0000-00000", MovimientoConstants.TIPO_ENTREGA);
            entregasPedidoRepository.findByCorrelativoPedidoAndTipoMovimiento(
                    "PED-0000-00000", MovimientoConstants.TIPO_ENTREGA);
        });
    }
}
