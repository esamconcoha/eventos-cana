import { Component, ElementRef, Input, OnChanges, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { escalaSuperior, montoCorto } from './paleta';
import { BucleCuadros, enReposo, movimientoReducido, resorte } from '../animaciones/bucle';

/** Lo que el rastreador necesita saber de cada mes. */
interface Columna {
  centro: number;
  etiqueta: string;
  valorA: number;
  valorB: number;
  /** y de la barra más alta del grupo: la burbuja se apoya encima. */
  tope: number;
}

interface Resorte { valor: number; velocidad: number; }

interface BarraDibujada {
  x: number;
  y: number;
  ancho: number;
  alto: number;
  color: string;
  titulo: string;
  /** ms de espera antes de crecer: las columnas entran de izquierda a derecha. */
  retraso: number;
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
    <svg #lienzo [attr.viewBox]="'0 0 ' + W + ' ' + H" class="w-full h-auto" preserveAspectRatio="xMidYMid meet"
         (pointermove)="alMover($event)" (pointerleave)="alSalir()">
      <!-- Banda del rastreador: resalta la columna bajo el mouse. La mueve el
           bucle de rAF, no Angular (ver moverRastreador). -->
      <rect #banda [attr.y]="MT - 4" [attr.height]="areaAlto + 8" width="0" rx="8"
            fill="#021930" opacity="0" pointer-events="none" />

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

      <!-- Barras. El track incluye la versión de los datos: al cambiar el
           periodo los <rect> se recrean y la animación de entrada se repite. -->
      <!-- Sin <title> por barra: el rastreador ya muestra los valores, y el
           tooltip nativo aparecía encima del suyo con un segundo de retraso. -->
      @for (b of barras; track version + '-' + $index) {
        <rect [attr.x]="b.x" [attr.y]="b.y" [attr.width]="b.ancho" [attr.height]="b.alto"
              [attr.fill]="b.color" rx="2" class="grafico-barra"
              [style.animation-delay.ms]="b.retraso" [attr.aria-label]="b.titulo" />
      }

      <!-- Eje de categorías -->
      @for (e of etiquetasEje; track version + '-' + e.x) {
        <text [attr.x]="e.x" [attr.y]="H - 12" text-anchor="middle"
              class="fill-slate-400 text-[10px] anim-aparece"
              [style.animation-delay.ms]="$index * 40">{{ e.texto }}</text>
      }

      <!-- Burbuja del rastreador. Va al final para quedar encima de todo. -->
      <g #burbuja opacity="0" pointer-events="none">
        <rect [attr.width]="ANCHO_BURBUJA" [attr.height]="altoBurbuja" rx="8"
              fill="#021930" fill-opacity="0.92" />
        <text #textoTitulo x="12" y="17" class="fill-amber-300 text-[10px] font-bold tracking-wider"></text>
        <rect x="12" y="25" width="8" height="8" rx="2" [attr.fill]="colorA" stroke="white" stroke-opacity="0.4" />
        <text #textoA x="25" y="33" class="fill-white text-[11px]"></text>
        @if (serieB) {
          <rect x="12" y="41" width="8" height="8" rx="2" [attr.fill]="colorB" />
          <text #textoB x="25" y="49" class="fill-white text-[11px]"></text>
        }
      </g>
    </svg>
  `
})
export class GraficoBarrasComponent implements OnChanges, OnDestroy {

  @ViewChild('lienzo') private lienzo?: ElementRef<SVGSVGElement>;
  @ViewChild('banda') private banda?: ElementRef<SVGRectElement>;
  @ViewChild('burbuja') private burbuja?: ElementRef<SVGGElement>;
  @ViewChild('textoTitulo') private textoTitulo?: ElementRef<SVGTextElement>;
  @ViewChild('textoA') private textoA?: ElementRef<SVGTextElement>;
  @ViewChild('textoB') private textoB?: ElementRef<SVGTextElement>;

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
  readonly MT = 26;
  private readonly MB = 34;
  readonly ANCHO_BURBUJA = 172;

  // ─── Rastreador ───────────────────────────────────────────
  // Una banda y una burbuja que siguen al mouse de columna en columna. La
  // posición no salta: la arrastra un resorte cuadro a cuadro (rAF), así que
  // al barrer la gráfica se desliza con inercia y rebota apenas al llegar.
  private columnas: Columna[] = [];
  private paso = 0;
  private indice = -1;
  private readonly posX: Resorte = { valor: 0, velocidad: 0 };
  private readonly posY: Resorte = { valor: 0, velocidad: 0 };
  private readonly visible: Resorte = { valor: 0, velocidad: 0 };
  private objetivoX = 0;
  private objetivoY = 0;
  private objetivoVisible = 0;
  private readonly bucle = new BucleCuadros(dt => this.moverRastreador(dt));

  barras: BarraDibujada[] = [];
  grilla: LineaGrilla[] = [];
  etiquetasEje: EtiquetaEje[] = [];
  /** Ancho aproximado del texto de la primera leyenda, para colocar la segunda. */
  anchoTextoA = 0;
  /** Sube con cada cambio de datos; ver el track de las barras. */
  version = 0;

  /** Firma de los últimos datos dibujados; ver ngOnChanges. */
  private firma = '';

  ngOnChanges(): void {
    // Mismos datos en un arreglo nuevo: no se redibuja ni se esconde la
    // burbuja del rastreador (pasaba con series armadas en getters del padre).
    const firma = JSON.stringify([this.etiquetas, this.serieA, this.serieB, this.nombreA,
      this.nombreB, this.colorA, this.colorB, this.moneda]);
    if (firma === this.firma) { return; }
    this.firma = firma;
    this.version++;
    this.anchoTextoA = this.nombreA.length * 5.2;
    this.calcularGrilla();
    this.calcularBarras();
    // Con datos nuevos la columna marcada puede ya no existir: se oculta y el
    // siguiente movimiento del mouse la vuelve a elegir.
    this.indice = -1;
    this.objetivoVisible = 0;
    this.bucle.arrancar();
  }

  ngOnDestroy(): void {
    this.bucle.detener();
  }

  get altoBurbuja(): number {
    return this.serieB ? 58 : 42;
  }

  alMover(evento: PointerEvent): void {
    const svg = this.lienzo?.nativeElement;
    if (!svg || this.columnas.length === 0) { return; }
    const r = svg.getBoundingClientRect();
    // De píxeles de pantalla a unidades del viewBox.
    const x = ((evento.clientX - r.left) / r.width) * this.W;
    const y = ((evento.clientY - r.top) / r.height) * this.H;
    if (x < this.ML || x > this.W - this.MR || y < this.MT - 10 || y > this.H - this.MB + 10) {
      this.alSalir();
      return;
    }

    const i = Math.min(Math.max(Math.floor((x - this.ML) / this.paso), 0), this.columnas.length - 1);
    const primeraVez = this.visible.valor < 0.05;
    if (i !== this.indice) {
      this.indice = i;
      this.escribirTextos(this.columnas[i]);
    }
    const col = this.columnas[i];
    this.objetivoX = col.centro;
    this.objetivoY = Math.max(col.tope - this.altoBurbuja - 10, 2);
    this.objetivoVisible = 1;
    if (primeraVez || movimientoReducido()) {
      // Al aparecer se coloca directo en su columna: si arrancara desde la
      // última posición conocida cruzaría media gráfica para llegar.
      this.posX.valor = this.objetivoX; this.posX.velocidad = 0;
      this.posY.valor = this.objetivoY; this.posY.velocidad = 0;
    }
    this.bucle.arrancar();
  }

  alSalir(): void {
    this.objetivoVisible = 0;
    this.bucle.arrancar();
  }

  private escribirTextos(col: Columna): void {
    // Directo al DOM y no por binding: sin zone.js, un binding no se
    // repintaría hasta el próximo ciclo de detección de cambios.
    if (this.textoTitulo) { this.textoTitulo.nativeElement.textContent = col.etiqueta.toUpperCase(); }
    if (this.textoA) { this.textoA.nativeElement.textContent = `${this.nombreA}: ${this.formatear(col.valorA)}`; }
    if (this.textoB) { this.textoB.nativeElement.textContent = `${this.nombreB}: ${this.formatear(col.valorB)}`; }
  }

  private moverRastreador(dt: number): boolean {
    const banda = this.banda?.nativeElement;
    const burbuja = this.burbuja?.nativeElement;
    if (!banda || !burbuja) { return false; }

    resorte(this.posX, this.objetivoX, dt, 260, 24);
    resorte(this.posY, this.objetivoY, dt, 220, 22);
    resorte(this.visible, this.objetivoVisible, dt, 300, 30);

    const opacidad = Math.min(Math.max(this.visible.valor, 0), 1);
    const anchoBanda = this.paso * 0.9;
    banda.setAttribute('x', (this.posX.valor - anchoBanda / 2).toFixed(2));
    banda.setAttribute('width', anchoBanda.toFixed(2));
    banda.setAttribute('opacity', (opacidad * 0.07).toFixed(3));

    // La burbuja se centra en la columna pero no se sale de la gráfica.
    const bx = Math.min(Math.max(this.posX.valor - this.ANCHO_BURBUJA / 2, 2), this.W - this.ANCHO_BURBUJA - 2);
    // Al aparecer sube 6px mientras se vuelve opaca.
    const by = this.posY.valor + (1 - opacidad) * 6;
    burbuja.setAttribute('transform', `translate(${bx.toFixed(2)} ${by.toFixed(2)})`);
    burbuja.setAttribute('opacity', opacidad.toFixed(3));

    return !(enReposo(this.posX, this.objetivoX, 0.05)
      && enReposo(this.posY, this.objetivoY, 0.05)
      && enReposo(this.visible, this.objetivoVisible, 0.002));
  }

  get areaAlto(): number {
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
    this.columnas = [];

    const n = this.etiquetas.length;
    if (n === 0) { return; }

    const tope = this.tope;
    const paso = this.areaAncho / n;
    this.paso = paso;
    const anchoGrupo = paso * 0.62;
    const series = this.serieB ? 2 : 1;
    const anchoBarra = anchoGrupo / series;
    const base = this.MT + this.areaAlto;

    // El escalonado total se limita a ~600ms: con 24 meses, 50ms por columna
    // haría esperar más de un segundo a la última.
    const escalon = Math.min(50, 600 / n);

    for (let i = 0; i < n; i++) {
      const centro = this.ML + paso * i + paso / 2;
      const x = centro - anchoGrupo / 2;
      const retraso = Math.round(i * escalon);

      this.agregarBarra(x, base, anchoBarra, this.valor(this.serieA, i), tope,
        this.colorA, `${this.etiquetas[i]} · ${this.nombreA}: ${this.formatear(this.valor(this.serieA, i))}`,
        retraso);

      if (this.serieB) {
        // La segunda serie sale un pelo después que la primera de su grupo.
        this.agregarBarra(x + anchoBarra, base, anchoBarra, this.valor(this.serieB, i), tope,
          this.colorB, `${this.etiquetas[i]} · ${this.nombreB}: ${this.formatear(this.valor(this.serieB, i))}`,
          retraso + 80);
      }

      const valorA = this.valor(this.serieA, i);
      const valorB = this.valor(this.serieB, i);
      const mayor = Math.max(valorA, this.serieB ? valorB : 0);
      this.columnas.push({
        centro,
        etiqueta: this.etiquetas[i],
        valorA,
        valorB,
        tope: tope > 0 && mayor > 0 ? base - Math.max((this.areaAlto * mayor) / tope, 2) : base
      });

      // Con más de 13 meses las etiquetas se encimarían: se alternan.
      if (n <= 13 || i % 2 === 0) {
        this.etiquetasEje.push({ x: centro, texto: this.etiquetas[i] });
      }
    }
  }

  private agregarBarra(x: number, base: number, ancho: number, valor: number,
                       tope: number, color: string, titulo: string, retraso: number): void {
    if (valor <= 0 || tope <= 0) {
      // Aun así se registra el tooltip con una barra mínima invisible: sin esto
      // un mes en cero no tendría manera de consultarse.
      this.barras.push({ x, y: base - 1, ancho: Math.max(ancho - 1, 1), alto: 1, color: '#E2E8F0', titulo, retraso });
      return;
    }
    const alto = Math.max((this.areaAlto * valor) / tope, 2);
    this.barras.push({
      x, y: base - alto, ancho: Math.max(ancho - 1, 1), alto, color, titulo, retraso
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
