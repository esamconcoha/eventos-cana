import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Catalogo } from '../interfaces/catalogo';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCatalogoByNombre(nombre: string): Observable<Catalogo[]> {
    return this.http.get<Catalogo[]>(`${this.baseUrl}catalogosCana/getCatalogoByNombre/${nombre}`);
  }
}
