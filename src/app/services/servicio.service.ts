import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ServicioDecoracion, CrearServicio, CategoriaServicio } from '../interfaces/servicio';

@Injectable({ providedIn: 'root' })
export class ServicioService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarCategorias(): Observable<CategoriaServicio[]> {
    return this.http.get<CategoriaServicio[]>(`${this.baseUrl}servicios/privado/listarCategorias`);
  }

  listarServicios(): Observable<ServicioDecoracion[]> {
    return this.http.get<ServicioDecoracion[]>(`${this.baseUrl}servicios/privado/listarServicios`);
  }

  guardarServicio(servicio: CrearServicio): Observable<any> {
    return this.http.post(`${this.baseUrl}servicios/privado/guardarServicio`, servicio);
  }

  editarServicio(id: number, servicio: CrearServicio): Observable<any> {
    return this.http.put(`${this.baseUrl}servicios/privado/editarServicio/${id}`, servicio);
  }

  inactivarServicio(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}servicios/privado/inactivarServicio/${id}`, null);
  }
}
