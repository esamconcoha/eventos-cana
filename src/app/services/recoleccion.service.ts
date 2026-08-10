import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EntregaPedido, EstadisticasEntregas, RegistrarViaje } from '../interfaces/entrega';
import { RecoleccionDetalle, ProgramarRecoleccion } from '../interfaces/recoleccion';

// Códigos propios de la vuelta. Los compartidos con entregas (4016/4017/4020)
// se repiten acá para que la pantalla no dependa del otro módulo.
const MENSAJES_ERROR_RECOLECCION: Record<number, string> = {
  4016: 'El viaje debe incluir al menos un artículo.',
  4017: 'La cantidad debe ser mayor a 0.',
  4020: 'Las fechas del viaje no son válidas.',
  4022: 'El catálogo de estados no está configurado correctamente. Contacta a soporte.',
  4025: 'La recolección no existe.',
  4026: 'Esta recolección ya fue finalizada.',
  4027: 'No se puede finalizar una recolección sin viajes registrados.',
  4028: 'No se puede recolectar más de lo que se entregó de ese artículo.',
  4029: 'Ese artículo nunca salió en un viaje de entrega de este pedido.',
  4030: 'La fecha de recolección es obligatoria.'
};

export function mensajeErrorRecoleccion(err: any, fallback: string): string {
  const code = err?.error?.code;
  return (code && MENSAJES_ERROR_RECOLECCION[code]) || err?.error?.message || fallback;
}

@Injectable({ providedIn: 'root' })
export class RecoleccionService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // El listado reutiliza EntregaPedido: una recolección es el mismo movimiento
  // logístico, y fechaEntrega trae acá la fecha programada de recolección.
  listarRecolecciones(): Observable<EntregaPedido[]> {
    return this.http.get<EntregaPedido[]>(`${this.baseUrl}recolecciones/privado/listarRecolecciones`);
  }

  obtenerEstadisticas(): Observable<EstadisticasEntregas> {
    return this.http.get<EstadisticasEntregas>(`${this.baseUrl}recolecciones/privado/estadisticas`);
  }

  obtenerRecoleccion(id: number): Observable<RecoleccionDetalle> {
    return this.http.get<RecoleccionDetalle>(`${this.baseUrl}recolecciones/privado/obtenerRecoleccion/${id}`);
  }

  programar(id: number, datos: ProgramarRecoleccion): Observable<RecoleccionDetalle> {
    return this.http.put<RecoleccionDetalle>(`${this.baseUrl}recolecciones/privado/programar/${id}`, datos);
  }

  registrarViaje(viaje: RegistrarViaje): Observable<RecoleccionDetalle> {
    return this.http.post<RecoleccionDetalle>(`${this.baseUrl}recolecciones/privado/registrarViaje`, viaje);
  }

  marcarFinalizada(id: number): Observable<RecoleccionDetalle> {
    return this.http.put<RecoleccionDetalle>(`${this.baseUrl}recolecciones/privado/marcarFinalizada/${id}`, null);
  }
}
