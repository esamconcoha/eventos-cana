import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PagoPedido, RegistrarPago, EstadoCuenta } from '../interfaces/pago';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  registrarPago(pago: RegistrarPago): Observable<PagoPedido> {
    return this.http.post<PagoPedido>(`${this.baseUrl}pagosPedido/privado/registrarPago`, pago);
  }

  anularPago(idPago: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}pagosPedido/privado/anularPago/${idPago}`, null);
  }

  listarPagos(correlativoPedido: string): Observable<PagoPedido[]> {
    return this.http.get<PagoPedido[]>(`${this.baseUrl}pagosPedido/privado/listar/${correlativoPedido}`);
  }

  obtenerEstadoCuenta(correlativoPedido: string): Observable<EstadoCuenta> {
    return this.http.get<EstadoCuenta>(`${this.baseUrl}pagosPedido/privado/estadoCuenta/${correlativoPedido}`);
  }
}
