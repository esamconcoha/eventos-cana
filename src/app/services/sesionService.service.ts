import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { usuarioSesion } from '../interfaces/usuario';
import { Observable } from 'rxjs';
import { token } from '../interfaces/token';

@Injectable({
  providedIn: 'root'
})
export class SesionServiceService {
private baseUrl = environment.apiUrl;
constructor(private httpClient: HttpClient) { }

iniciarSesion(sesion:usuarioSesion): Observable<token> {
  return this.httpClient.post<token>(`${this.baseUrl}publico/authenticate`, sesion);
}

}
