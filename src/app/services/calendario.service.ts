import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CalendarioItem } from '../interfaces/calendario';

@Injectable({ providedIn: 'root' })
export class CalendarioService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  /**
   * @param desde primer día visible de la grilla, "yyyy-mm-dd"
   * @param hasta último día visible de la grilla, "yyyy-mm-dd"
   *
   * Ambas fechas se arman desde las partes LOCALES del reloj (ver
   * fechaISOLocal en el componente), nunca con toISOString(): eso convierte a
   * UTC y en UTC-6 corre el día, desplazando el mes entero que se pide.
   */
  obtenerAgenda(desde: string, hasta: string): Observable<CalendarioItem[]> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<CalendarioItem[]>(`${this.baseUrl}calendario/privado/agenda`, { params });
  }
}
