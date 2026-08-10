import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Pedido, CrearPedido, ActualizarPedido, EstadoPedidoHistorial } from '../interfaces/pedido';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.baseUrl}pedidos/privado/listarPedidos`);
  }

  obtenerPedido(correlativoPedido: string): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.baseUrl}pedidos/privado/obtenerPedido/${correlativoPedido}`);
  }

  guardarPedido(pedido: CrearPedido): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.baseUrl}pedidos/privado/guardarPedido`, pedido);
  }

  actualizarPedido(correlativoPedido: string, cambios: ActualizarPedido): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.baseUrl}pedidos/privado/actualizarPedido/${correlativoPedido}`, cambios);
  }

  /**
   * Confirma (o revierte) que un servicio del pedido ya se realizó.
   * Sustituye al estado "Montando / Decorando": el montaje corre en paralelo a
   * los viajes y solo aplica a quien contrató el servicio, así que se registra
   * por línea de servicio y no en el ciclo de vida del pedido.
   */
  marcarServicioRealizado(idDetalleServPedido: number, realizado: boolean): Observable<Pedido> {
    return this.http.put<Pedido>(
      `${this.baseUrl}detalleServicioPedido/privado/marcarRealizado/${idDetalleServPedido}`,
      null, { params: { realizado } });
  }

  cambiarEstadoLogistico(correlativoPedido: string, idEstado: number): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.baseUrl}pedidos/privado/cambiarEstado/${correlativoPedido}/${idEstado}`, null);
  }

  /** Cancela el pedido: lo mueve al estado terminal "Evento cancelado" (ECA). */
  cancelarPedido(correlativoPedido: string): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.baseUrl}pedidos/privado/cancelarPedido/${correlativoPedido}`, null);
  }

  historialEstados(correlativoPedido: string): Observable<EstadoPedidoHistorial[]> {
    return this.http.get<EstadoPedidoHistorial[]>(`${this.baseUrl}pedidos/privado/historialEstados/${correlativoPedido}`);
  }
}
