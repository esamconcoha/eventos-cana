package com.canabackend.cana.services.impl;

import com.canabackend.cana.dtos.EstadoCuentaPedidoDto;
import com.canabackend.cana.dtos.PagoPedidoListDto;
import com.canabackend.cana.dtos.RegistrarPagoDto;
import com.canabackend.cana.exceptions.ErrorEnum;
import com.canabackend.cana.exceptions.MSCanaException;
import com.canabackend.cana.models.EstadosPagoPedido;
import com.canabackend.cana.models.PagosPedido;
import com.canabackend.cana.models.PedidosCana;
import com.canabackend.cana.projections.GetCatalogoByNombreProjection;
import com.canabackend.cana.repositories.CatalogosCanaRepository;
import com.canabackend.cana.repositories.EstadosPagoPedidoRepository;
import com.canabackend.cana.repositories.PagosPedidoRepository;
import com.canabackend.cana.repositories.PedidosCanaRepository;
import com.canabackend.cana.services.PagosPedidoSvc;
import com.canabackend.cana.utils.PagoConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PagosPedidoSvcImpl implements PagosPedidoSvc {

    @Autowired
    private PagosPedidoRepository pagosPedidoRepository;
    @Autowired
    private EstadosPagoPedidoRepository estadosPagoPedidoRepository;
    @Autowired
    private PedidosCanaRepository pedidosCanaRepository;
    @Autowired
    private CatalogosCanaRepository catalogosCanaRepository;

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void registrarPago(RegistrarPagoDto dto) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(dto.getCorrelativoPedido())
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        if (dto.getMontoPago() == null || dto.getMontoPago() <= 0) {
            throw new MSCanaException(ErrorEnum.MONTO_PAGO_INVALIDO);
        }
        validarTipoPago(dto.getTipoPago());
        validarMetodoPago(dto.getMetodoPago());

        double totalPedido = nvl(this.pagosPedidoRepository.getTotalPedido(dto.getCorrelativoPedido()));
        List<PagosPedido> pagosActivos = pagosActivos(dto.getCorrelativoPedido());
        double totalPagado = totalPagado(pagosActivos);

        if (PagoConstants.TIPO_PAGO_DEVOLUCION.equals(dto.getTipoPago())) {
            if (dto.getMontoPago() > totalPagado) {
                throw new MSCanaException(ErrorEnum.PAGO_EXCEDE_SALDO_PENDIENTE);
            }
        } else if (dto.getMontoPago() > (totalPedido - totalPagado)) {
            throw new MSCanaException(ErrorEnum.PAGO_EXCEDE_SALDO_PENDIENTE);
        }

        PagosPedido pago = new PagosPedido();
        pago.setCorrelativoPedido(dto.getCorrelativoPedido());
        pago.setMontoPago(dto.getMontoPago());
        pago.setFechaPago(LocalDateTime.now());
        pago.setTipoPago(dto.getTipoPago());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setReferenciaPago(dto.getReferenciaPago());
        pago.setUsuarioRegistro(dto.getUsuarioRegistro());
        pago.setEstadoRegistro(true);
        pago.setFechaCreo(LocalDateTime.now());
        this.pagosPedidoRepository.save(pago);

        recalcularEstadoPago(pedido, totalPedido);
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void anularPago(Long idPago) {
        PagosPedido pago = this.pagosPedidoRepository.findById(idPago)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PAGO_NOT_FOUND));
        pago.setEstadoRegistro(false);
        this.pagosPedidoRepository.save(pago);

        PedidosCana pedido = this.pedidosCanaRepository.findById(pago.getCorrelativoPedido())
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
        double totalPedido = nvl(this.pagosPedidoRepository.getTotalPedido(pago.getCorrelativoPedido()));
        recalcularEstadoPago(pedido, totalPedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoPedidoListDto> listarPagos(String correlativoPedido) {
        this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        List<PagoPedidoListDto> resultado = new ArrayList<>();
        for (PagosPedido p : this.pagosPedidoRepository.findByCorrelativoPedidoOrderByFechaPagoDesc(correlativoPedido)) {
            resultado.add(new PagoPedidoListDto(
                    p.getIdPago(), p.getMontoPago(), p.getFechaPago(),
                    p.getTipoPago(), this.catalogosCanaRepository.getNombreByCodigo(p.getTipoPago()),
                    p.getMetodoPago(), this.catalogosCanaRepository.getNombreByCodigo(p.getMetodoPago()),
                    p.getReferenciaPago(), p.getUsuarioRegistro(), p.getEstadoRegistro()));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoCuentaPedidoDto obtenerEstadoCuenta(String correlativoPedido) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));

        double totalPedido = nvl(this.pagosPedidoRepository.getTotalPedido(correlativoPedido));
        double totalPagado = totalPagado(pagosActivos(correlativoPedido));

        return new EstadoCuentaPedidoDto(
                correlativoPedido, totalPedido, totalPagado, totalPedido - totalPagado, pedido.getEstadoPago());
    }

    @Override
    @Transactional(rollbackFor = MSCanaException.class)
    public void recalcularEstadoPago(String correlativoPedido) {
        PedidosCana pedido = this.pedidosCanaRepository.findById(correlativoPedido)
                .orElseThrow(() -> new MSCanaException(ErrorEnum.PEDIDO_NOT_FOUND));
        double totalPedido = nvl(this.pagosPedidoRepository.getTotalPedido(correlativoPedido));
        recalcularEstadoPago(pedido, totalPedido);
    }

    /**
     * Recalcula el estado de pago del pedido tras registrar o anular un pago,
     * actualiza pedidos_cana (estado_pago + pagado) y, si el estado cambio,
     * cierra el registro vigente en estados_pago_pedido y abre uno nuevo.
     */
    private void recalcularEstadoPago(PedidosCana pedido, double totalPedido) {
        List<PagosPedido> pagosActivos = pagosActivos(pedido.getCorrelativoPedido());
        double totalPagado = totalPagado(pagosActivos);
        String nuevoEstado = calcularEstadoPago(totalPedido, totalPagado, pagosActivos);

        if (!nuevoEstado.equals(pedido.getEstadoPago())) {
            LocalDateTime ahora = LocalDateTime.now();
            this.estadosPagoPedidoRepository
                    .findFirstByCorrelativoPedidoAndFechaHoraFinIsNull(pedido.getCorrelativoPedido())
                    .ifPresent(historico -> {
                        historico.setFechaHoraFin(ahora);
                        this.estadosPagoPedidoRepository.save(historico);
                    });

            EstadosPagoPedido nuevoHistorico = new EstadosPagoPedido();
            nuevoHistorico.setCorrelativoPedido(pedido.getCorrelativoPedido());
            nuevoHistorico.setEstadoPago(nuevoEstado);
            nuevoHistorico.setFechaHoraInicio(ahora);
            nuevoHistorico.setFechaHoraFin(null);
            this.estadosPagoPedidoRepository.save(nuevoHistorico);

            pedido.setEstadoPago(nuevoEstado);
            pedido.setPagado(PagoConstants.ESTADO_PAGO_PAGADO.equals(nuevoEstado));
            this.pedidosCanaRepository.save(pedido);
        }
    }

    private String calcularEstadoPago(double totalPedido, double totalPagado, List<PagosPedido> pagosActivos) {
        if (totalPagado <= 0) {
            boolean huboDevolucion = pagosActivos.stream()
                    .anyMatch(p -> PagoConstants.TIPO_PAGO_DEVOLUCION.equals(p.getTipoPago()));
            return huboDevolucion ? PagoConstants.ESTADO_PAGO_DEVUELTO : PagoConstants.ESTADO_PAGO_PENDIENTE;
        }
        if (totalPedido > 0 && totalPagado >= totalPedido) {
            return PagoConstants.ESTADO_PAGO_PAGADO;
        }
        boolean soloUnAnticipo = pagosActivos.size() == 1
                && PagoConstants.TIPO_PAGO_ANTICIPO.equals(pagosActivos.get(0).getTipoPago());
        return soloUnAnticipo ? PagoConstants.ESTADO_PAGO_ANTICIPO : PagoConstants.ESTADO_PAGO_PARCIAL;
    }

    private List<PagosPedido> pagosActivos(String correlativoPedido) {
        List<PagosPedido> activos = new ArrayList<>();
        for (PagosPedido p : this.pagosPedidoRepository.findByCorrelativoPedidoOrderByFechaPagoDesc(correlativoPedido)) {
            if (Boolean.TRUE.equals(p.getEstadoRegistro())) {
                activos.add(p);
            }
        }
        return activos;
    }

    private double totalPagado(List<PagosPedido> pagosActivos) {
        double total = 0;
        for (PagosPedido p : pagosActivos) {
            double monto = nvl(p.getMontoPago());
            total += PagoConstants.TIPO_PAGO_DEVOLUCION.equals(p.getTipoPago()) ? -monto : monto;
        }
        return total;
    }

    private void validarTipoPago(String tipoPago) {
        List<GetCatalogoByNombreProjection> catalogo =
                this.catalogosCanaRepository.getCatalogoByNombre(PagoConstants.NOMBRE_CATALOGO_TIPO_PAGO);
        boolean valido = catalogo.stream().anyMatch(c -> c.getCodigo().equals(tipoPago));
        if (!valido) {
            throw new MSCanaException(ErrorEnum.TIPO_PAGO_INVALIDO);
        }
    }

    private void validarMetodoPago(String metodoPago) {
        List<GetCatalogoByNombreProjection> catalogo =
                this.catalogosCanaRepository.getCatalogoByIdTipoCatalogo(PagoConstants.ID_TIPO_CATALOGO_METODO_PAGO);
        boolean valido = catalogo.stream().anyMatch(c -> c.getCodigo().equals(metodoPago));
        if (!valido) {
            throw new MSCanaException(ErrorEnum.METODO_PAGO_INVALIDO);
        }
    }

    private double nvl(Double value) {
        return value != null ? value : 0d;
    }
}
