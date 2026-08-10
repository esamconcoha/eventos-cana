import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Salon, GuardarSalon } from '../interfaces/salon';

@Injectable({ providedIn: 'root' })
export class SalonService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarSalones(): Observable<Salon[]> {
    return this.http.get<Salon[]>(`${this.baseUrl}salones/privado/listarSalones`);
  }

  guardarSalon(salon: GuardarSalon): Observable<any> {
    return this.http.post(`${this.baseUrl}salones/privado/guardarSalon`, salon);
  }

  editarSalon(id: number, salon: GuardarSalon): Observable<any> {
    return this.http.put(`${this.baseUrl}salones/privado/editarSalon/${id}`, salon);
  }

  eliminarSalon(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}salones/privado/eliminarSalon/${id}`, null);
  }
}
