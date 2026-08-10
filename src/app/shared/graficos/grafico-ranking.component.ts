import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface FilaRanking {
  etiqueta: string;
  /** Línea secundaria: categoría, cliente, lo que aporte contexto. */
  detalle?: string | null;
  /** Lo que mide la barra. */
  valor: number;
  /** Texto que se muestra a la derecha. Si falta se imprime el valor. */
  valorTexto?: string;
}

/**
 * Ranking de barras horizontales. Es HTML y no SVG a propósito: las etiquetas
 * son nombres de artículos o clientes, y en HTML se truncan y muestran tooltip
 * sin tener que medir texto a mano.
 */
@Component({
  selector: 'app-grafico-ranking',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (filas.length === 0) {
      <div class="flex items-center justify-center h-32 text-sm text-grayMedium">
        Sin datos en el periodo
      </div>
    } @else {
      <ul class="space-y-3">
        @for (f of filas; track f.etiqueta) {
          <li>
            <div class="flex items-baseline gap-2 mb-1">
              <span class="text-sm text-gray-800 truncate" [title]="f.etiqueta">{{ f.etiqueta }}</span>
              @if (f.detalle) {
                <span class="text-[11px] text-grayMedium truncate shrink-0">{{ f.detalle }}</span>
              }
              <span class="ml-auto text-sm font-montserrat font-bold text-gray-900 shrink-0">
                {{ f.valorTexto ?? f.valor }}
              </span>
            </div>
            <div class="h-2 rounded-full bg-slate-100 overflow-hidden">
              <div class="h-full rounded-full transition-all duration-500"
                   [style.width.%]="ancho(f)" [style.background-color]="color"></div>
            </div>
          </li>
        }
      </ul>
    }
  `
})
export class GraficoRankingComponent {

  @Input() filas: FilaRanking[] = [];
  @Input() color = '#021930';

  ancho(fila: FilaRanking): number {
    const maximo = Math.max(...this.filas.map(f => f.valor ?? 0), 0);
    if (maximo <= 0) { return 0; }
    // Mínimo visible: una barra de 0.2% no se distingue de "sin dato".
    return Math.max((fila.valor * 100) / maximo, 2);
  }
}
