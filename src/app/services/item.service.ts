import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ItemCana, CrearItem } from '../interfaces/item-cana';

@Injectable({ providedIn: 'root' })
export class ItemService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarItems(): Observable<ItemCana[]> {
    return this.http.get<ItemCana[]>(`${this.baseUrl}items/privado/listarItems`);
  }

  guardarItem(item: CrearItem): Observable<any> {
    return this.http.post(`${this.baseUrl}items/privado/guardarItem`, item);
  }

  editarItem(id: number, item: CrearItem): Observable<any> {
    return this.http.put(`${this.baseUrl}items/privado/editarItem/${id}`, item);
  }

  eliminarItem(id: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}items/privado/eliminarItem/${id}`, null);
  }
}
