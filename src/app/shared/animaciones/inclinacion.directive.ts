import { Directive, ElementRef, Input, OnDestroy, OnInit } from '@angular/core';
import { BucleCuadros, enReposo, movimientoReducido, resorte } from './bucle';

interface Resorte { valor: number; velocidad: number; }

/**
 * Inclina la tarjeta en 3D hacia donde está el mouse, con un reflejo de luz
 * que la recorre, como si fuera un panel de vidrio real que se gira en la mano.
 *
 *   <div class="surface-card" appInclinacion>...</div>
 *
 * La inclinación sigue al mouse con un resorte (rAF) y no con una transición
 * de CSS: al soltar, la tarjeta vuelve con un pequeño rebote en lugar de frenar
 * en seco, y si el mouse se mueve rápido el giro lo sigue sin trabarse.
 *
 * Solo con mouse: en pantallas táctiles no hay "hover" que seguir. El bucle
 * corre únicamente mientras la tarjeta se está moviendo; en reposo no gasta
 * nada.
 */
@Directive({
  selector: '[appInclinacion]',
  standalone: true
})
export class InclinacionDirective implements OnInit, OnDestroy {

  /** Grados máximos de giro en cada eje. */
  @Input() inclinacionMaxima = 7;

  private readonly rotX: Resorte = { valor: 0, velocidad: 0 };
  private readonly rotY: Resorte = { valor: 0, velocidad: 0 };
  private readonly elevar: Resorte = { valor: 0, velocidad: 0 };
  private objetivoX = 0;
  private objetivoY = 0;
  private objetivoElevar = 0;
  /** Posición del reflejo en % de la tarjeta; sigue al mouse sin resorte. */
  private brilloX = 50;
  private brilloY = 50;

  private reflejo: HTMLDivElement | null = null;
  private readonly bucle = new BucleCuadros(dt => this.paso(dt));
  private activo = false;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    if (movimientoReducido()) { return; }
    this.activo = true;
    const host = this.el.nativeElement;
    host.addEventListener('pointerenter', this.alEntrar);
    host.addEventListener('pointermove', this.alMover);
    host.addEventListener('pointerleave', this.alSalir);
  }

  ngOnDestroy(): void {
    const host = this.el.nativeElement;
    host.removeEventListener('pointerenter', this.alEntrar);
    host.removeEventListener('pointermove', this.alMover);
    host.removeEventListener('pointerleave', this.alSalir);
    this.bucle.detener();
  }

  private alEntrar = (e: PointerEvent): void => {
    if (!this.activo || e.pointerType !== 'mouse') { return; }
    this.prepararHost();
    this.objetivoElevar = 1;
    this.alMover(e);
  };

  private alMover = (e: PointerEvent): void => {
    if (!this.activo || e.pointerType !== 'mouse') { return; }
    const r = this.el.nativeElement.getBoundingClientRect();
    // -0.5 .. 0.5 desde el centro de la tarjeta.
    const px = (e.clientX - r.left) / r.width - 0.5;
    const py = (e.clientY - r.top) / r.height - 0.5;
    // El borde que está bajo el mouse se "hunde": mouse arriba -> rotateX positivo.
    this.objetivoX = -py * 2 * this.inclinacionMaxima;
    this.objetivoY = px * 2 * this.inclinacionMaxima;
    this.brilloX = (px + 0.5) * 100;
    this.brilloY = (py + 0.5) * 100;
    this.bucle.arrancar();
  };

  private alSalir = (): void => {
    if (!this.activo) { return; }
    this.objetivoX = 0;
    this.objetivoY = 0;
    this.objetivoElevar = 0;
    this.bucle.arrancar();
  };

  /**
   * Se hace la primera vez que entra el mouse y no en ngOnInit, para no pisar
   * la animación de entrada de .surface-card (glass-rise). Esa animación deja
   * fijado su transform final con fill-mode "both", y una animación CSS le gana
   * a cualquier transform en línea: por eso se apaga antes de inclinar.
   */
  private prepararHost(): void {
    if (this.reflejo) { return; }
    const host = this.el.nativeElement;
    host.style.animation = 'none';
    // El transform lo maneja el resorte: si quedara en la transición CSS del
    // host (transition-all), cada cuadro arrancaría una transición y el giro
    // se sentiría pegajoso.
    host.style.transition = 'box-shadow 0.3s ease, background-color 0.3s ease, opacity 0.3s ease';
    host.style.willChange = 'transform';
    if (getComputedStyle(host).position === 'static') {
      host.style.position = 'relative';
    }

    const reflejo = document.createElement('div');
    reflejo.setAttribute('aria-hidden', 'true');
    Object.assign(reflejo.style, {
      position: 'absolute',
      inset: '0',
      borderRadius: 'inherit',
      pointerEvents: 'none',
      opacity: '0',
      mixBlendMode: 'soft-light',
      zIndex: '1'
    } as Partial<CSSStyleDeclaration>);
    host.appendChild(reflejo);
    this.reflejo = reflejo;
  }

  private paso(dt: number): boolean {
    resorte(this.rotX, this.objetivoX, dt);
    resorte(this.rotY, this.objetivoY, dt);
    resorte(this.elevar, this.objetivoElevar, dt, 200, 22);

    const host = this.el.nativeElement;
    const subir = this.elevar.valor * 4;
    host.style.transform =
      `perspective(900px) rotateX(${this.rotX.valor.toFixed(2)}deg) ` +
      `rotateY(${this.rotY.valor.toFixed(2)}deg) translateY(${(-subir).toFixed(2)}px)`;

    if (this.reflejo) {
      const intensidad = Math.max(0, Math.min(this.elevar.valor, 1));
      this.reflejo.style.opacity = intensidad.toFixed(3);
      this.reflejo.style.background =
        `radial-gradient(circle at ${this.brilloX.toFixed(1)}% ${this.brilloY.toFixed(1)}%, ` +
        `rgba(255,255,255,0.85), rgba(255,255,255,0) 60%)`;
    }

    const quieto = enReposo(this.rotX, this.objetivoX)
      && enReposo(this.rotY, this.objetivoY)
      && enReposo(this.elevar, this.objetivoElevar, 0.002);
    if (quieto && this.objetivoElevar === 0) {
      // En reposo se limpia el transform: deja la tarjeta exactamente como
      // estaba y no mantiene una capa de composición por cada tarjeta.
      host.style.transform = '';
      if (this.reflejo) { this.reflejo.style.opacity = '0'; }
    }
    return !quieto;
  }
}
