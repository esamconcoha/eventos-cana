import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EntregaPedido, EstadisticasEntregas, RegistrarViaje, EstadoEntregaDerivado } from '../interfaces/entrega';
import { hoyISOLocal } from '../shared/fecha.util';

// Los endpoints de entregas devuelven { status, type, code, message, detail }.
// Se mapea por code (nunca por message) para mostrar textos consistentes en la UI.
const MENSAJES_ERROR_ENTREGA: Record<number, string> = {
  4010: 'El item no existe o está inactivo.',
  4012: 'La entrega no existe.',
  4013: 'Este pedido ya tiene una entrega abierta.',
  4014: 'Esta entrega ya fue finalizada.',
  4015: 'Los viajes aproximados deben ser al menos 1.',
  4016: 'El viaje debe incluir al menos un artículo.',
  4017: 'La cantidad debe ser mayor a 0.',
  4018: 'Ese artículo no pertenece a este pedido.',
  4019: 'La cantidad supera lo que aún falta por entregar de ese artículo.',
  4020: 'Las fechas del viaje no son válidas.',
  4021: 'No se puede finalizar una entrega sin viajes registrados.',
  4022: 'El catálogo de estados no está configurado correctamente. Contacta a soporte.',
  4036: 'Selecciona un archivo para subir.',
  4037: 'El archivo debe ser PDF, JPG, PNG o WEBP.',
  4038: 'El archivo no puede superar los 10MB.',
  4039: 'Esta entrega todavía no tiene una constancia firmada subida.'
};

export function mensajeErrorEntrega(err: any, fallback: string): string {
  const code = err?.error?.code;
  return (code && MENSAJES_ERROR_ENTREGA[code]) || err?.error?.message || fallback;
}

// Única fuente de verdad para clasificar una entrega por fecha pactada.
// fechaEntrega es pedidos_cana.fecha_entrega ("yyyy-mm-dd" pura), no
// fechaInicioEntrega/fechaFinEntrega (esas son de ejecución, no de agenda).
export function categoriaFechaEntrega(entrega: { fechaEntrega?: string | null; pedidoFinalizado: boolean }): EstadoEntregaDerivado {
  if (entrega.pedidoFinalizado) return 'FINALIZADA';
  if (!entrega.fechaEntrega) return 'SIN_FECHA';
  const fecha = entrega.fechaEntrega.slice(0, 10);
  const hoy = hoyISOLocal();
  if (fecha === hoy) return 'HOY';
  if (fecha > hoy) return 'PROGRAMADA';
  return 'ATRASADA';
}

@Injectable({ providedIn: 'root' })
export class EntregaService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarEntregas(): Observable<EntregaPedido[]> {
    return this.http.get<EntregaPedido[]>(`${this.baseUrl}entregas/privado/listarEntregas`);
  }

  obtenerEstadisticas(): Observable<EstadisticasEntregas> {
    return this.http.get<EstadisticasEntregas>(`${this.baseUrl}entregas/privado/estadisticas`);
  }

  obtenerEntrega(idEntrega: number): Observable<EntregaPedido> {
    return this.http.get<EntregaPedido>(`${this.baseUrl}entregas/privado/obtenerEntrega/${idEntrega}`);
  }

  registrarViaje(viaje: RegistrarViaje): Observable<EntregaPedido> {
    return this.http.post<EntregaPedido>(`${this.baseUrl}entregas/privado/registrarViaje`, viaje);
  }

  marcarFinalizada(idEntrega: number): Observable<EntregaPedido> {
    return this.http.put<EntregaPedido>(`${this.baseUrl}entregas/privado/marcarFinalizada/${idEntrega}`, null);
  }

  /** Constancia de entrega en PDF (lo despachado + servicios, con área de firma). */
  obtenerConstancia(idEntrega: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}entregas/privado/constancia/${idEntrega}`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  /**
   * Sube la constancia YA FIRMADA por el cliente (foto o escaneo de la de
   * arriba). Devuelve la entrega actualizada con constanciaFirmadaDisponible
   * en true, para no tener que recargarla aparte.
   */
  subirConstanciaFirmada(idEntrega: number, archivo: File, usuarioSubio: string): Observable<EntregaPedido> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    formData.append('usuarioSubio', usuarioSubio);
    return this.http.post<EntregaPedido>(
      `${this.baseUrl}entregas/privado/constanciaFirmada/${idEntrega}`, formData);
  }

  /** Descarga la constancia firmada vigente (PDF o imagen, según se subió). */
  obtenerConstanciaFirmada(idEntrega: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}entregas/privado/constanciaFirmada/${idEntrega}`, {
      observe: 'response',
      responseType: 'blob'
    });
  }
}

/** Espejo de ConstanciaFirmadaConstants en el backend: valida antes de subir
 *  para no gastar un viaje redondo en un archivo que el servidor va a rechazar. */
export const TIPOS_CONSTANCIA_FIRMADA_PERMITIDOS = ['application/pdf', 'image/jpeg', 'image/png', 'image/webp'];
export const TAMANIO_MAXIMO_CONSTANCIA_FIRMADA = 10 * 1024 * 1024; // 10MB
