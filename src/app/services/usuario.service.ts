import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UsuarioInterno } from '../interfaces/usuario-interno';
import { CrearUsuario } from '../interfaces/crear-usuario';
import { EditarUsuario } from '../interfaces/editar-usuario';
import { Direccion } from '../interfaces/direccion';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  guardarUsuario(usuario: CrearUsuario): Observable<any> {
    return this.http.post(`${this.baseUrl}publico/guardarUsuario`, usuario);
  }

  listarUsuarios(): Observable<UsuarioInterno[]> {
    return this.http.get<UsuarioInterno[]>(`${this.baseUrl}usuarios/privado/listarUsuarios`);
  }

  editarUsuario(usuario: EditarUsuario): Observable<any> {
    return this.http.put(`${this.baseUrl}usuarios/privado/modificarUsuario`, usuario);
  }

  inactivarUsuario(dpi: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}usuarios/privado/inactivarUsuario/${dpi}`, null);
  }

  getDireccionesUsuario(dpi: string): Observable<Direccion[]> {
    return this.http.get<Direccion[]>(`${this.baseUrl}direcciones/getDireccionesByDpiNit/${dpi}`);
  }
}
