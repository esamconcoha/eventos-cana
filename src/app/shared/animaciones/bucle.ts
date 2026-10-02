/**
 * Utilidades para los efectos que se calculan cuadro a cuadro con
 * requestAnimationFrame (inclinación de tarjetas, confeti, rastreador de la
 * gráfica).
 *
 * La app corre sin zone.js: nada de esto dispara detección de cambios, y está
 * bien, porque todos estos efectos escriben directo en el DOM o en un canvas.
 * Ojo: el navegador integrado de desarrollo no dispara rAF (ver la nota de
 * memoria del proyecto), así que ahí estos efectos se quedan quietos.
 */

export function movimientoReducido(): boolean {
  return typeof window !== 'undefined'
    && !!window.matchMedia
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/**
 * Bucle de rAF que se apaga solo. `paso` recibe el tiempo transcurrido desde el
 * cuadro anterior en segundos (acotado, para que volver a una pestaña dormida
 * no haga "saltar" la física) y devuelve true mientras quiera seguir.
 *
 * Llamar a `arrancar()` con el bucle ya corriendo no hace nada, así que se
 * puede invocar en cada evento de mouse sin acumular bucles.
 */
export class BucleCuadros {
  private id: number | null = null;
  private anterior = 0;

  constructor(private readonly paso: (dt: number) => boolean) {}

  get corriendo(): boolean {
    return this.id !== null;
  }

  arrancar(): void {
    if (this.id !== null) { return; }
    this.anterior = performance.now();
    this.id = requestAnimationFrame(this.tick);
  }

  detener(): void {
    if (this.id !== null) {
      cancelAnimationFrame(this.id);
      this.id = null;
    }
  }

  private tick = (ahora: number): void => {
    const dt = Math.min((ahora - this.anterior) / 1000, 1 / 20);
    this.anterior = ahora;
    if (this.paso(dt)) {
      this.id = requestAnimationFrame(this.tick);
    } else {
      this.id = null;
    }
  };
}

/**
 * Un paso de resorte amortiguado (integración semi-implícita de Euler). Es lo
 * que da el "rebote" natural: el valor pasa un poco del objetivo y vuelve, en
 * lugar de frenar en seco como una curva de CSS.
 */
export function resorte(
  estado: { valor: number; velocidad: number },
  objetivo: number,
  dt: number,
  rigidez = 170,
  friccion = 18
): void {
  const fuerza = rigidez * (objetivo - estado.valor) - friccion * estado.velocidad;
  estado.velocidad += fuerza * dt;
  estado.valor += estado.velocidad * dt;
}

/** true cuando el resorte ya está prácticamente quieto en su objetivo. */
export function enReposo(estado: { valor: number; velocidad: number }, objetivo: number, tolerancia = 0.01): boolean {
  return Math.abs(objetivo - estado.valor) < tolerancia && Math.abs(estado.velocidad) < tolerancia;
}
