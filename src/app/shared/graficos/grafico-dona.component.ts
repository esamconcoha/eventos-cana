import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { colorSerie } from './paleta';
import { ContadorDirective } from '../animaciones/contador.directive';

/** Para que cada dona de la página tenga su propia máscara SVG. */
let siguienteId = 0;

export interface PorcionDona {
  etiqueta: string;
  valor: number;
  /** Texto del tooltip y de la leyenda. Si falta se usa el valor crudo. */
  detalle?: string;
}

interface ArcoDibujado {
  path: string;
  color: string;
  titulo: string;
}

/**
 * Dona de composición con leyenda al lado. La leyenda es HTML y no SVG para que
 * los nombres largos hagan salto de línea en vez de recortarse.
 */
@Component({
  selector: 'app-grafico-dona',
  standalone: true,
  imports: [CommonModule, ContadorDirective],
  template: `
    @if (arcos.length === 0) {
      <div class="flex items-center justify-center h-40 text-sm text-grayMedium">
        Sin datos en el periodo
      </div>
    } @else {
      <div class="flex items-center gap-5 flex-wrap sm:flex-nowrap">
        <svg viewBox="0 0 200 200" class="w-40 h-40 shrink-0 overflow-visible">
          <!-- Un @for de un solo elemento, con la versión como track: cuando
               cambian los datos se recrea la máscara y la dona se vuelve a
               dibujar desde arriba en sentido horario. -->
          @for (v of [version]; track v) {
            <defs>
              <mask [attr.id]="idMascara + '-' + v">
                <!-- Anillo blanco que se va trazando; lo que cubre se ve. Radio
                     65 y grosor 50 tapan de 40 a 90: más que la dona (46 a 82)
                     para no recortarla cuando una porción crece con el hover. -->
                <circle cx="100" cy="100" r="65" fill="none" stroke="white" stroke-width="50"
                        [attr.stroke-dasharray]="LARGO_TRAZO" transform="rotate(-90 100 100)"
                        class="grafico-mascara-trazo" />
              </mask>
            </defs>
            <g [attr.mask]="'url(#' + idMascara + '-' + v + ')'">
              @for (a of arcos; track $index) {
                <path [attr.d]="a.path" [attr.fill]="a.color" class="grafico-porcion">
                  <title>{{ a.titulo }}</title>
                </path>
              }
            </g>
          }
          <text x="100" y="97" text-anchor="middle"
                class="fill-[#021930] text-[22px] font-bold font-montserrat"
                [appContador]="totalTexto"></text>
          <text x="100" y="114" text-anchor="middle"
                class="fill-slate-400 text-[10px] tracking-wider anim-aparece"
                style="animation-delay: 400ms">{{ leyendaTotal }}</text>
        </svg>

        <ul class="flex-1 min-w-0 space-y-2">
          @for (p of porcionesVisibles; track version + p.etiqueta) {
            <li class="flex items-center gap-2 text-sm min-w-0 anim-entra"
                [style.animation-delay.ms]="200 + $index * 70">
              <span class="w-2.5 h-2.5 rounded-sm shrink-0"
                    [style.background-color]="color($index)"></span>
              <span class="truncate text-gray-700" [title]="p.etiqueta">{{ p.etiqueta }}</span>
              <span class="ml-auto shrink-0 text-xs text-grayMedium">{{ porcentaje(p) }}</span>
            </li>
          }
          @if (ocultas > 0) {
            <li class="text-xs text-grayMedium pl-5">+{{ ocultas }} más</li>
          }
        </ul>
      </div>
    }
  `
})
export class GraficoDonaComponent implements OnChanges {

  @Input() porciones: PorcionDona[] = [];
  /** Texto bajo el número del centro. */
  @Input() leyendaTotal = 'TOTAL';
  /** Formatea el centro y los tooltips como quetzales. */
  @Input() moneda = false;
  /** Cuántas entradas de leyenda se listan antes de agrupar en "+N más". */
  @Input() maximoLeyenda = 6;

  arcos: ArcoDibujado[] = [];
  porcionesVisibles: PorcionDona[] = [];
  ocultas = 0;
  totalTexto = '';
  private total = 0;

  /** Circunferencia del anillo de la máscara (2π·65 ≈ 408.4), redondeada hacia
   *  arriba. Tiene que coincidir con el valor inicial de dibuja-trazo en styles.css. */
  readonly LARGO_TRAZO = 409;
  readonly idMascara = `dona-mascara-${siguienteId++}`;
  /** Sube con cada cambio de datos; ver el @for de la máscara. */
  version = 0;

  /** Firma de los últimos datos dibujados; ver ngOnChanges. */
  private firma = '';

  ngOnChanges(): void {
    // Si el padre pasa los mismos datos en un arreglo nuevo (p. ej. desde un
    // getter), no se redibuja: sin esto la dona se volvía a trazar en cada
    // ciclo de detección de cambios.
    const firma = JSON.stringify([this.porciones, this.moneda, this.maximoLeyenda, this.leyendaTotal]);
    if (firma === this.firma) { return; }
    this.firma = firma;
    this.version++;
    // Un valor en cero no dibuja arco y solo ensuciaría la leyenda.
    const validas = (this.porciones ?? []).filter(p => (p.valor ?? 0) > 0);
    this.total = validas.reduce((suma, p) => suma + p.valor, 0);
    this.totalTexto = this.formatearCentro(this.total);
    this.porcionesVisibles = validas.slice(0, this.maximoLeyenda);
    this.ocultas = Math.max(validas.length - this.maximoLeyenda, 0);
    this.arcos = this.calcularArcos(validas);
  }

  color(indice: number): string {
    return colorSerie(indice);
  }

  porcentaje(porcion: PorcionDona): string {
    if (this.total <= 0) { return '0%'; }
    return Math.round((porcion.valor * 100) / this.total) + '%';
  }

  private calcularArcos(porciones: PorcionDona[]): ArcoDibujado[] {
    if (this.total <= 0) { return []; }

    const arcos: ArcoDibujado[] = [];
    let angulo = -90;
    for (let i = 0; i < porciones.length; i++) {
      const p = porciones[i];
      const barrido = (p.valor / this.total) * 360;
      arcos.push({
        path: this.arcoAnular(100, 100, 82, 46, angulo, angulo + barrido),
        color: colorSerie(i),
        titulo: `${p.etiqueta}: ${p.detalle ?? this.formatearCentro(p.valor)} (${this.porcentaje(p)})`
      });
      angulo += barrido;
    }
    return arcos;
  }

  /**
   * Segmento de anillo entre dos ángulos. Una sola porción de 360° no se puede
   * dibujar con un arco (inicio y fin coinciden y el path queda vacío), así que
   * se recorta a 359.99°.
   */
  private arcoAnular(cx: number, cy: number, rExterno: number, rInterno: number,
                     desde: number, hasta: number): string {
    const barrido = Math.min(hasta - desde, 359.99);
    const fin = desde + barrido;
    const grande = barrido > 180 ? 1 : 0;

    const e1 = this.punto(cx, cy, rExterno, desde);
    const e2 = this.punto(cx, cy, rExterno, fin);
    const i2 = this.punto(cx, cy, rInterno, fin);
    const i1 = this.punto(cx, cy, rInterno, desde);

    return [
      `M ${e1.x} ${e1.y}`,
      `A ${rExterno} ${rExterno} 0 ${grande} 1 ${e2.x} ${e2.y}`,
      `L ${i2.x} ${i2.y}`,
      `A ${rInterno} ${rInterno} 0 ${grande} 0 ${i1.x} ${i1.y}`,
      'Z'
    ].join(' ');
  }

  private punto(cx: number, cy: number, radio: number, grados: number): { x: number; y: number } {
    const rad = (grados * Math.PI) / 180;
    return {
      x: +(cx + radio * Math.cos(rad)).toFixed(2),
      y: +(cy + radio * Math.sin(rad)).toFixed(2)
    };
  }

  private formatearCentro(valor: number): string {
    if (!this.moneda) {
      return valor.toLocaleString('es-GT', { maximumFractionDigits: 0 });
    }
    if (valor >= 1000) {
      return 'Q ' + (valor / 1000).toFixed(1).replace('.0', '') + 'k';
    }
    return 'Q ' + valor.toFixed(0);
  }
}
