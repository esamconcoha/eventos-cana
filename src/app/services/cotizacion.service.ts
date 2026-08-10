import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Cotizacion, CrearCotizacion, ActualizarCotizacion, ConfirmarCotizacion } from '../interfaces/cotizacion';
import { TrazaEvento } from '../interfaces/trazabilidad';

@Injectable({ providedIn: 'root' })
export class CotizacionService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarCotizaciones(): Observable<Cotizacion[]> {
    return this.http.get<Cotizacion[]>(`${this.baseUrl}cotizaciones/privado/listarCotizaciones`);
  }

  guardarCotizacion(cotizacion: CrearCotizacion): Observable<HttpResponse<Blob>> {
    return this.http.post(`${this.baseUrl}cotizaciones/privado/guardarCotizacion`, cotizacion, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  /**
   * Devuelve el PDF regenerado. El backend lo guarda además como una versión
   * más en documentos_cotizacion, así que "Ver documento" ya toma los cambios.
   */
  actualizarCotizacion(id: number, cotizacion: ActualizarCotizacion): Observable<HttpResponse<Blob>> {
    return this.http.put(`${this.baseUrl}cotizaciones/privado/actualizarCotizacion/${id}`, cotizacion, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  obtenerDocumento(id: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}cotizaciones/privado/documento/${id}`, {
      observe: 'response',
      responseType: 'blob'
    });
  }

  confirmarCotizacion(id: number, datos: ConfirmarCotizacion): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}cotizaciones/privado/confirmarCotizacion/${id}`, datos);
  }

  cancelarCotizacion(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}cotizaciones/privado/cancelarCotizacion/${id}`, null);
  }

  eliminarCotizacion(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}cotizaciones/privado/eliminarCotizacion/${id}`, null);
  }

  /** Historial de estados para el módulo de trazabilidad (lo alimenta el trigger de sql/008). */
  historialEstados(id: number): Observable<TrazaEvento[]> {
    return this.http.get<TrazaEvento[]>(`${this.baseUrl}cotizaciones/privado/historialEstados/${id}`);
  }
}
