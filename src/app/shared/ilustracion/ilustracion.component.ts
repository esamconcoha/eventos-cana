import { Component, Input } from '@angular/core';

/**
 * documento  confirmar, crear, editar, finalizar (portapapeles con casillas)
 * basurero   eliminar, inactivar, anular
 * estado     cambios de estado de un pedido o servicio (línea de avance)
 */
export type EscenaIlustracion = 'documento' | 'basurero' | 'estado';

/** pregunta: antes de actuar (confirmación). listo: la acción ya se hizo (éxito). */
export type ModoIlustracion = 'pregunta' | 'listo';

let siguienteId = 0;

/**
 * Ilustraciones animadas de las confirmaciones y los avisos de éxito.
 *
 * SVG en línea con animaciones CSS, sin imágenes ni librerías. Las tres escenas
 * comparten el mismo lenguaje (papel crema, madera ámbar, halo cálido,
 * destellos) para que se sientan de la misma familia, y cada una tiene dos
 * modos: en "pregunta" el objeto espera con un globo "?"; en "listo" la acción
 * se completa en pantalla (se marca la casilla, cae el papel, avanza el estado)
 * y aparece una palomita.
 *
 * El tamaño lo decide quien la usa con CSS (width); el alto sale del viewBox.
 */
@Component({
  selector: 'app-ilustracion',
  standalone: true,
  templateUrl: './ilustracion.component.html',
  styleUrl: './ilustracion.component.css',
  host: { 'aria-hidden': 'true', class: 'block' }
})
export class IlustracionComponent {
  @Input() escena: EscenaIlustracion = 'documento';
  @Input() modo: ModoIlustracion = 'pregunta';

  /** Id propio para el degradado: con dos toasts abiertos no deben pisarse. */
  readonly idHalo = `ilus-halo-${siguienteId++}`;
}
