import { Directive, ElementRef, Input, OnChanges, OnDestroy } from '@angular/core';

/**
 * Hace "contar" un número desde el valor anterior (o desde 0 la primera vez)
 * hasta el nuevo, en lugar de que aparezca de golpe.
 *
 *   <p [appContador]="resumen.entregasHoy"></p>
 *   <p [appContador]="'Q 12,500.00'"></p>
 *
 * Acepta números o textos ya formateados ("Q 12,500.00", "45.2%", "30 u"): se
 * anima solo el primer número del texto y se respetan su prefijo, sufijo,
 * decimales y separador de miles. Null o undefined muestran "—".
 *
 * Escribe directo en textContent: no depende de la detección de cambios (la
 * app corre sin zone.js) y no dispara un ciclo por cuadro. Usa setTimeout y no
 * requestAnimationFrame porque rAF no corre en pestañas en segundo plano ni en
 * el navegador integrado de desarrollo, y el número se quedaría en 0.
 */
@Directive({
  selector: '[appContador]',
  standalone: true
})
export class ContadorDirective implements OnChanges, OnDestroy {

  @Input('appContador') valor: number | string | null | undefined;
  /** Duración de la cuenta en ms. */
  @Input() contadorDuracion = 900;

  private actual = 0;
  private timer: ReturnType<typeof setTimeout> | null = null;

  private static readonly NUMERO = /-?\d[\d,]*(\.\d+)?/;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnChanges(): void {
    this.detener();

    if (this.valor === null || this.valor === undefined || this.valor === '') {
      this.escribir('—');
      return;
    }

    const texto = String(this.valor);
    const match = texto.match(ContadorDirective.NUMERO);
    if (!match) {
      this.escribir(texto);
      return;
    }

    const destino = Number(match[0].replace(/,/g, ''));
    const decimales = match[1] ? match[1].length - 1 : 0;
    // En textos se respeta lo que traía (un "2026" no gana coma); en números
    // se agrupa siempre.
    const miles = typeof this.valor === 'number' || match[0].includes(',');
    const antes = texto.slice(0, match.index);
    const despues = texto.slice((match.index ?? 0) + match[0].length);
    const formatear = (n: number) => antes + this.formatear(n, decimales, miles) + despues;

    if (this.sinMovimiento() || destino === this.actual) {
      this.actual = destino;
      this.escribir(formatear(destino));
      return;
    }

    const origen = this.actual;
    const inicio = Date.now();
    const paso = () => {
      const t = Math.min((Date.now() - inicio) / this.contadorDuracion, 1);
      // easeOutCubic: arranca rápido y frena al llegar, como un odómetro.
      const avance = 1 - Math.pow(1 - t, 3);
      this.actual = origen + (destino - origen) * avance;
      this.escribir(formatear(t < 1 ? this.actual : destino));
      if (t < 1) {
        this.timer = setTimeout(paso, 16);
      } else {
        this.actual = destino;
        this.timer = null;
      }
    };
    paso();
  }

  ngOnDestroy(): void {
    this.detener();
  }

  private formatear(n: number, decimales: number, miles: boolean): string {
    return n.toLocaleString('en-US', {
      minimumFractionDigits: decimales,
      maximumFractionDigits: decimales,
      useGrouping: miles
    });
  }

  private sinMovimiento(): boolean {
    return typeof window !== 'undefined'
      && !!window.matchMedia
      && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  private escribir(texto: string): void {
    this.el.nativeElement.textContent = texto;
  }

  private detener(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
