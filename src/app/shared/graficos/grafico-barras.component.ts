import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { escalaSuperior, montoCorto } from './paleta';

interface BarraDibujada {
  x: number;
  y: number;
  ancho: number;
  alto: number;
  color: string;
  titulo: string;
}

interface LineaGrilla {
  y: number;
  etiqueta: string;
}

interface EtiquetaEje {
  x: number;
  texto: string;
}

/**
 * Barras verticales de una o dos series (facturado vs cobrado por mes).
 *
 * SVG dibujado a mano en vez de una librería de gráficas: el tablero necesita
 * tres tipos de gráfica y ninguna interacción más allá del tooltip, así que una
 * dependencia nueva costaría más de lo que ahorra. El mismo dibujo lo replica
 * GraficoUtil en el backend para el PDF.
 */
@Component({
  selector: 'app-grafico-barras',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg [attr.viewBox]="'0 0 ' + W + ' ' + H" class="w-full h-auto" preserveAspectRatio="xMidYMid meet">
      <!-- Grilla y eje de valores -->
      @for (linea of grilla; track linea.y) {
        <line [attr.x1]="ML" [attr.y1]="linea.y" [attr.x2]="W - MR" [attr.y2]="linea.y"
              stroke="#E2E8F0" stroke-width="1" />
        <text [attr.x]="ML - 8" [attr.y]="linea.y + 3" text-anchor="end"
              class="fill-slate-400 text-[10px]">{{ linea.etiqueta }}</text>
      }

      <!-- Leyenda -->
      @if (serieB) {
        <rect [attr.x]="ML" y="6" width="9" height="9" rx="2" [attr.fill]="colorA" />
        <text [attr.x]="ML + 14" y="14" class="fill-slate-500 text-[10px]">{{ nombreA }}</text>
        <rect [attr.x]="ML + 20 + anchoTextoA" y="6" width="9" height="9" rx="2" [attr.fill]="colorB" />
        <text [attr.x]="ML + 34 + anchoTextoA" y="14" class="fill-slate-500 text-[10px]">{{ nombreB }}</text>
      }

      <!-- Barras -->
      @for (b of barras; track $index) {
        <rect [attr.x]="b.x" [attr.y]="b.y" [attr.width]="b.ancho" [attr.height]="b.alto"
              [attr.fill]="b.color" rx="2" class="transition-opacity hover:opacity-75">
          <title>{{ b.titulo }}</title>
        </rect>
      }

      <!-- Eje de categorías -->
      @for (e of etiquetasEje; track e.x) {
        <text [attr.x]="e.x" [attr.y]="H - 12" text-anchor="middle"
              class="fill-slate-400 text-[10px]">{{ e.texto }}</text>
      }
    </svg>
  `
})
export class GraficoBarrasComponent implements OnChanges {

  @Input() etiquetas: string[] = [];
  @Input() serieA: number[] = [];
  @Input() nombreA = '';
  @Input() colorA = '#021930';
  /** Null deja una sola serie y oculta la leyenda. */
  @Input() serieB: number[] | null = null;
  @Input() nombreB = '';
  @Input() colorB = '#F2B134';
  /** Formatea tooltips y eje como quetzales. */
  @Input() moneda = true;

  readonly W = 720;
  readonly H = 250;
  readonly ML = 58;
  readonly MR = 12;
  private readonly MT = 26;
  private readonly MB = 34;

  barras: BarraDibujada[] = [];
  grilla: LineaGrilla[] = [];
  etiquetasEje: EtiquetaEje[] = [];
  /** Ancho aproximado del texto de la primera leyenda, para colocar la segunda. */
  anchoTextoA = 0;

  ngOnChanges(): void {
    this.anchoTextoA = this.nombreA.length * 5.2;
    this.calcularGrilla();
    this.calcularBarras();
  }

  private get areaAlto(): number {
    return this.H - this.MT - this.MB;
  }

  private get areaAncho(): number {
    return this.W - this.ML - this.MR;
  }

  private get tope(): number {
    const maximo = Math.max(
      ...this.serieA.map(v => v ?? 0),
      ...(this.serieB ?? []).map(v => v ?? 0),
      0
    );
    return escalaSuperior(maximo);
  }

  private calcularGrilla(): void {
    const tope = this.tope;
    this.grilla = [0, 1, 2, 3, 4].map(i => ({
      y: this.MT + this.areaAlto - (this.areaAlto * i) / 4,
      etiqueta: montoCorto((tope * i) / 4)
    }));
  }

  private calcularBarras(): void {
    this.barras = [];
    this.etiquetasEje = [];

    const n = this.etiquetas.length;
    if (n === 0) { return; }

    const tope = this.tope;
    const paso = this.areaAncho / n;
    const anchoGrupo = paso * 0.62;
    const series = this.serieB ? 2 : 1;
    const anchoBarra = anchoGrupo / series;
    const base = this.MT + this.areaAlto;

    for (let i = 0; i < n; i++) {
      const centro = this.ML + paso * i + paso / 2;
      const x = centro - anchoGrupo / 2;

      this.agregarBarra(x, base, anchoBarra, this.valor(this.serieA, i), tope,
        this.colorA, `${this.etiquetas[i]} · ${this.nombreA}: ${this.formatear(this.valor(this.serieA, i))}`);

      if (this.serieB) {
        this.agregarBarra(x + anchoBarra, base, anchoBarra, this.valor(this.serieB, i), tope,
          this.colorB, `${this.etiquetas[i]} · ${this.nombreB}: ${this.formatear(this.valor(this.serieB, i))}`);
      }

      // Con más de 13 meses las etiquetas se encimarían: se alternan.
      if (n <= 13 || i % 2 === 0) {
        this.etiquetasEje.push({ x: centro, texto: this.etiquetas[i] });
      }
    }
  }

  private agregarBarra(x: number, base: number, ancho: number, valor: number,
                       tope: number, color: string, titulo: string): void {
    if (valor <= 0 || tope <= 0) {
      // Aun así se registra el tooltip con una barra mínima invisible: sin esto
      // un mes en cero no tendría manera de consultarse.
      this.barras.push({ x, y: base - 1, ancho: Math.max(ancho - 1, 1), alto: 1, color: '#E2E8F0', titulo });
      return;
    }
    const alto = Math.max((this.areaAlto * valor) / tope, 2);
    this.barras.push({
      x, y: base - alto, ancho: Math.max(ancho - 1, 1), alto, color, titulo
    });
  }

  private valor(serie: number[] | null, indice: number): number {
    return serie?.[indice] ?? 0;
  }

  private formatear(valor: number): string {
    return this.moneda
      ? 'Q ' + valor.toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      : valor.toLocaleString('es-GT');
  }
}
