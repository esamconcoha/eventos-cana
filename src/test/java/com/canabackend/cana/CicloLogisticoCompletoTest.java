package com.canabackend.cana;

import com.canabackend.cana.dtos.DetallePedidoDto;
import com.canabackend.cana.dtos.EntregaDetalleDto;
import com.canabackend.cana.dtos.GuardarPedidoDto;
import com.canabackend.cana.dtos.PedidoDto;
import com.canabackend.cana.dtos.ProgramarRecoleccionDto;
import com.canabackend.cana.dtos.RecoleccionDetalleDto;
import com.canabackend.cana.dtos.RegistrarViajeDto;
import com.canabackend.cana.dtos.ViajeItemRequestDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.EntregasPedido;
import com.canabackend.cana.repositories.EntregasPedidoRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.services.EntregasPedidoSvc;
import com.canabackend.cana.services.PedidosCanaSvc;
import com.canabackend.cana.services.RecoleccionesSvc;
import com.canabackend.cana.utils.MovimientoConstants;
import com.canabackend.cana.utils.PedidoConstants;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Recorre el ciclo logistico completo contra la BD real: pedido -> entrega ->
 * recoleccion -> cierre con faltante, verificando los efectos que ninguna
 * consulta de lectura puede probar (contadores, transiciones de estado, la
 * apertura automatica de la recoleccion y el calculo del faltante).
 *
 * Va con @Transactional, asi que Spring revierte todo al terminar: no queda
 * nada escrito en la base mas alla de unas secuencias avanzadas.
 */
@SpringBootTest
@Transactional
class CicloLogisticoCompletoTest {

    private static final double CANTIDAD_PEDIDA = 10d;

    @Autowired private EntregasPedidoSvc entregasSvc;
    @Autowired private RecoleccionesSvc recoleccionesSvc;
    @Autowired private PedidosCanaSvc pedidosCanaSvc;
    @Autowired private PedidosCanaRepository pedidosCanaRepository;
    @Autowired private EntregasPedidoRepository entregasPedidoRepository;
    @Autowired private EntityManager em;

    @Test
    @DisplayName("pedido -> entrega -> recoleccion -> cierre con faltante")
    void cicloCompleto() {
        Long idItem = primerItemActivo();
        Assumptions.assumeTrue(idItem != null, "no hay items activos en items_cana");

        // ── 1. Crear el pedido: debe abrir la entrega sola ──────────────
        PedidoDto pedido = crearPedido(idItem);
        EntregasPedido entrega = entregasPedidoRepository
                .findByCorrelativoPedidoAndTipoMovimiento(
                        pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_ENTREGA)
                .orElse(null);
        assertNotNull(entrega, "guardarPedido debe crear la fila ENT");
        assertEquals(0, entrega.getCantidadViajesReales());
        assertEquals(PedidoConstants.ESTADO_EVENTO_CONFIRMADO,
                pedido.getEstadoActual().getCodigoEstado(), "todo pedido nace confirmado");

        // ── 2. Primer viaje de ida: parcial, 6 de 10 ───────────────────
        EntregaDetalleDto det = entregasSvc.registrarViaje(
                viaje(entrega.getIdEntrega(), idItem, 6d));
        assertEquals(1, det.getCantidadViajesReales(), "el contador se incrementa");
        assertNotNull(det.getFechaInicioEntrega(), "el primer viaje abre la entrega");
        assertEquals(PedidoConstants.ESTADO_EVENTO_EN_RUTA_ENTREGA,
                det.getEstadoActualPedido().getCodigoEstado());
        assertEquals(4d, pendienteDe(det.getItems(), idItem), 0.001, "quedan 4 por enviar");

        // No se puede enviar mas de lo que falta
        assertEquals(ErrorEnum.CANTIDAD_ITEM_EXCEDE_PENDIENTE,
                assertThrows(MSCanaException.class,
                        () -> entregasSvc.registrarViaje(viaje(entrega.getIdEntrega(), idItem, 5d))).getError());

        // ── 3. Segundo viaje: se completan los 10 ──────────────────────
        det = entregasSvc.registrarViaje(viaje(entrega.getIdEntrega(), idItem, 4d));
        assertEquals(2, det.getCantidadViajesReales());
        assertEquals(0d, pendienteDe(det.getItems(), idItem), 0.001);

        // ── 4. Cerrar la entrega: pasa a Entregado y abre la recoleccion ─
        det = entregasSvc.marcarFinalizada(entrega.getIdEntrega());
        assertTrue(det.getPedidoFinalizado());
        assertEquals(PedidoConstants.ESTADO_EVENTO_ENTREGADO,
                det.getEstadoActualPedido().getCodigoEstado());

        EntregasPedido rec = entregasPedidoRepository
                .findByCorrelativoPedidoAndTipoMovimiento(
                        pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_RECOLECCION)
                .orElse(null);
        assertNotNull(rec, "al cerrar la entrega se abre la recoleccion sola");
        assertEquals(2, rec.getCantidadViajesAproximados(),
                "la estimacion de vuelta se precarga con los viajes reales de ida");
        assertEquals(0, rec.getCantidadViajesReales());

        // ── 5. La recoleccion nace sin agendar y se puede programar ─────
        RecoleccionDetalleDto rdet = recoleccionesSvc.obtenerRecoleccion(rec.getIdEntrega());
        assertEquals(10d, rdet.getItems().get(0).getCantidadEntregada(), 0.001,
                "el tope de la vuelta es lo que salio, no lo que se pidio");
        assertEquals(10d, rdet.getTotalPendiente(), 0.001);

        ProgramarRecoleccionDto prog = new ProgramarRecoleccionDto();
        prog.setFechaRecoleccion(LocalDate.now().plusDays(2));
        prog.setCantidadViajesAproximados(1);
        rdet = recoleccionesSvc.programar(rec.getIdEntrega(), prog);
        assertEquals(LocalDate.now().plusDays(2), rdet.getFechaRecoleccion());

        // No se puede recolectar mas de lo que salio
        assertEquals(ErrorEnum.CANTIDAD_ITEM_EXCEDE_ENTREGADO,
                assertThrows(MSCanaException.class,
                        () -> recoleccionesSvc.registrarViaje(viaje(rec.getIdEntrega(), idItem, 11d))).getError());

        // ── 6. Viaje de vuelta: solo regresan 7 de 10 ──────────────────
        rdet = recoleccionesSvc.registrarViaje(viaje(rec.getIdEntrega(), idItem, 7d));
        assertEquals(1, rdet.getCantidadViajesReales());
        assertEquals(PedidoConstants.ESTADO_EVENTO_RECOLECTADO,
                rdet.getEstadoActualPedido().getCodigoEstado());
        assertEquals(3d, rdet.getTotalPendiente(), 0.001, "faltan 3 por volver");

        // ── 7. Cerrar con faltante: se permite y queda registrado ──────
        rdet = recoleccionesSvc.marcarFinalizada(rec.getIdEntrega());
        assertTrue(rdet.getRecoleccionFinalizada());
        assertEquals(PedidoConstants.ESTADO_EVENTO_FINALIZADO,
                rdet.getEstadoActualPedido().getCodigoEstado());
        assertEquals(3d, rdet.getTotalPendiente(), 0.001, "el faltante es 10 entregados - 7 recolectados");
        assertEquals(1, rdet.getItemsConPendiente());
        assertNotNull(rdet.getFechaRecoleccion(), "al cerrar se sella fecha_recogido");

        // Ya cerrada, no admite mas cambios
        assertEquals(ErrorEnum.RECOLECCION_YA_FINALIZADA,
                assertThrows(MSCanaException.class,
                        () -> recoleccionesSvc.registrarViaje(viaje(rec.getIdEntrega(), idItem, 1d))).getError());
    }

    @Test
    @DisplayName("un id de entrega no sirve como id de recoleccion")
    void noSeCruzanLosMovimientos() {
        Long idItem = primerItemActivo();
        Assumptions.assumeTrue(idItem != null, "no hay items activos en items_cana");

        PedidoDto pedido = crearPedido(idItem);
        EntregasPedido entrega = entregasPedidoRepository
                .findByCorrelativoPedidoAndTipoMovimiento(
                        pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_ENTREGA)
                .orElseThrow();

        assertEquals(ErrorEnum.RECOLECCION_NOT_FOUND,
                assertThrows(MSCanaException.class,
                        () -> recoleccionesSvc.obtenerRecoleccion(entrega.getIdEntrega())).getError());
    }

    @Test
    @DisplayName("no se puede finalizar una entrega sin viajes")
    void entregaSinViajesNoCierra() {
        Long idItem = primerItemActivo();
        Assumptions.assumeTrue(idItem != null, "no hay items activos en items_cana");

        PedidoDto pedido = crearPedido(idItem);
        EntregasPedido entrega = entregasPedidoRepository
                .findByCorrelativoPedidoAndTipoMovimiento(
                        pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_ENTREGA)
                .orElseThrow();

        assertEquals(ErrorEnum.ENTREGA_SIN_VIAJES,
                assertThrows(MSCanaException.class,
                        () -> entregasSvc.marcarFinalizada(entrega.getIdEntrega())).getError());
        assertFalse(entregasPedidoRepository.existsByCorrelativoPedidoAndTipoMovimiento(
                pedido.getCorrelativoPedido(), MovimientoConstants.TIPO_RECOLECCION),
                "si la entrega no cierra, no debe abrirse la recoleccion");
    }

    // ── Auxiliares ────────────────────────────────────────────────────

    private PedidoDto crearPedido(Long idItem) {
        GuardarPedidoDto dto = new GuardarPedidoDto();
        dto.setNombreClientePedido("Cliente de prueba");
        dto.setDireccionPedido("Direccion de prueba");
        dto.setFechaEvento(LocalDateTime.now().plusDays(7));
        dto.setFechaEntrega(LocalDate.now());
        dto.setCantidadViajesAproximados(2);

        DetallePedidoDto detalle = new DetallePedidoDto();
        detalle.setIdItem(idItem);
        detalle.setCantidadItemPedido(CANTIDAD_PEDIDA);
        dto.setDetalles(Collections.singletonList(detalle));
        dto.setDetallesServicios(Collections.emptyList());

        return this.pedidosCanaSvc.guardarPedido(dto);
    }

    private RegistrarViajeDto viaje(Long idMovimiento, Long idItem, double cantidad) {
        ViajeItemRequestDto item = new ViajeItemRequestDto();
        item.setIdItem(idItem);
        item.setCantidadItem(cantidad);

        RegistrarViajeDto dto = new RegistrarViajeDto();
        dto.setIdEntrega(idMovimiento);
        dto.setFechaInicioViaje(LocalDateTime.now());
        dto.setItems(Collections.singletonList(item));
        return dto;
    }

    private double pendienteDe(List<com.canabackend.cana.dtos.ItemEntregaDto> items, Long idItem) {
        return items.stream()
                .filter(i -> idItem.equals(i.getIdItem()))
                .findFirst()
                .map(com.canabackend.cana.dtos.ItemEntregaDto::getCantidadPendiente)
                .orElse(-1d);
    }

    private Long primerItemActivo() {
        List<?> ids = em.createNativeQuery(
                "select id_item from cana.items_cana where estado_item is true order by id_item limit 1")
                .getResultList();
        return ids.isEmpty() ? null : ((Number) ids.get(0)).longValue();
    }
}
