import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReporteGeneral } from '../interfaces/reporte';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  obtenerGeneral(desde: string, hasta: string): Observable<ReporteGeneral> {
    return this.http.get<ReporteGeneral>(`${this.baseUrl}reportes/privado/general`, {
      params: this.rango(desde, hasta)
    });
  }

  /**
   * El PDF lo arma el backend con las mismas consultas que la pantalla: no se
   * captura el HTML, así que el documento sale idéntico en cualquier navegador
   * y no depende de lo que esté renderizado en ese momento.
   */
  descargarPdf(desde: string, hasta: string): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}reportes/privado/general/pdf`, {
      params: this.rango(desde, hasta),
      observe: 'response',
      responseType: 'blob'
    });
  }

  private rango(desde: string, hasta: string): HttpParams {
    return new HttpParams().set('desde', desde).set('hasta', hasta);
  }
}
