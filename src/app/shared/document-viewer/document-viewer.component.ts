import {
  ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Input, OnChanges,
  OnDestroy, Output, SimpleChanges, ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import type { PDFDocumentProxy } from 'pdfjs-dist';

/**
 * Visor de documentos (PDF e imagenes) con zoom propio.
 *
 * El PDF NO se muestra en un <iframe>: el visor nativo del navegador se recorta
 * en pantallas chicas, iOS Safari directamente no dibuja un blob: dentro de un
 * iframe, y su zoom no se puede controlar desde la aplicacion. Se dibuja con
 * pdf.js sobre <canvas>, una hoja por pagina, dentro de una zona con scroll:
 * asi el zoom es nuestro (botones + pinza) y se puede leer sin descargar nada.
 *
 * pdf.js se carga con import() dinamico para que quede en su propio chunk: es
 * ~1 MB que solo hace falta cuando se abre un PDF.
 */

/** La escala 1 = pagina ajustada al ancho de la zona de lectura. */
const ESCALA_MIN = 0.5;
const ESCALA_MAX = 5;
const PASO_ESCALA = 0.25;

@Component({
  selector: 'app-document-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './document-viewer.component.html'
})
export class DocumentViewerComponent implements OnChanges, OnDestroy {
  @Input() visible = false;
  @Input() blob: Blob | null = null;
  @Input() nombreArchivo = 'documento.pdf';
  @Input() titulo = 'Documento generado';
  @Output() cerrarModal = new EventEmitter<void>();

  /** Solo para imagenes; el PDF ya no necesita URL porque se dibuja a mano. */
  urlSegura: SafeResourceUrl | null = null;
  esPdf = false;
  /** La constancia firmada suele subirse como foto de celular, no como PDF. */
  esImagen = false;

  cargando = false;
  errorPdf = false;
  paginas = 0;
  escala = 1;

  private objectUrl: string | null = null;
  private pdf: PDFDocumentProxy | null = null;
  private hojas: HTMLElement | null = null;
  /** Invalida los repintados en curso cuando cambia el documento o la escala. */
  private tokenPintado = 0;
  private temporizadorPintado: ReturnType<typeof setTimeout> | null = null;
  private distanciaPinza = 0;
  private escalaPinza = 1;

  constructor(
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef
  ) {}

  /**
   * El contenedor de las hojas vive dentro del @if del template, asi que no
   * existe todavia cuando llega el blob. Se pinta en cuanto aparece.
   */
  @ViewChild('hojas') set refHojas(ref: ElementRef<HTMLElement> | undefined) {
    this.hojas = ref?.nativeElement ?? null;
    if (this.hojas && this.pdf) { void this.pintarPaginas(); }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['blob'] || changes['visible']) {
      if (this.visible && this.blob) {
        this.abrirDocumento();
      } else {
        this.limpiar();
      }
    }
  }

  // ─── Carga ─────────────────────────────────────────────────
  private abrirDocumento(): void {
    this.limpiar();
    if (!this.blob) { return; }

    this.esPdf = this.blob.type === 'application/pdf';
    this.esImagen = this.blob.type.startsWith('image/');
    this.objectUrl = URL.createObjectURL(this.blob);

    if (this.esImagen) {
      this.urlSegura = this.sanitizer.bypassSecurityTrustResourceUrl(this.objectUrl);
    } else if (this.esPdf) {
      void this.cargarPdf(this.blob);
    }
  }

  private async cargarPdf(blob: Blob): Promise<void> {
    this.cargando = true;
    this.errorPdf = false;
    try {
      const pdfjs = await import('pdfjs-dist');
      // El worker se copia a assets desde node_modules (ver angular.json). Ruta
      // absoluta: con una relativa se buscaria dentro de la ruta del router.
      pdfjs.GlobalWorkerOptions.workerSrc = '/assets/pdfjs/pdf.worker.min.mjs';

      // Copia del ArrayBuffer: pdf.js lo transfiere al worker y lo deja vacio,
      // y el mismo blob se vuelve a usar al descargar.
      const datos = new Uint8Array(await blob.arrayBuffer());
      const pdf = await pdfjs.getDocument({ data: datos }).promise;

      if (this.blob !== blob) { void pdf.destroy(); return; }   // se cerro mientras cargaba
      this.pdf = pdf;
      this.paginas = pdf.numPages;
      await this.pintarPaginas();
    } catch {
      this.errorPdf = true;
    } finally {
      this.cargando = false;
      // La app corre sin zone.js: despues de un await hay que avisarle a
      // Angular a mano, igual que en el resto de los componentes.
      this.cdr.detectChanges();
    }
  }

  // ─── Dibujo de las paginas ─────────────────────────────────
  /**
   * Redibuja todas las hojas a la escala actual. Se arma primero un fragmento
   * completo y se cambia de golpe: reemplazar hoja por hoja dejaba el visor en
   * blanco mientras se redibujaba al hacer zoom.
   */
  private async pintarPaginas(): Promise<void> {
    const contenedor = this.hojas;
    const pdf = this.pdf;
    if (!contenedor || !pdf) { return; }

    const token = ++this.tokenPintado;
    // El ancho de la zona de lectura menos el padding lateral de las hojas.
    const anchoDisponible = Math.max(contenedor.clientWidth - 24, 240);
    // Tope al dibujar en pantallas de mucha densidad: a dpr 3 y zoom 5 el
    // canvas se pasaba del maximo de textura del navegador y salia en negro.
    const dpr = Math.min(window.devicePixelRatio || 1, 2);

    const fragmento = document.createDocumentFragment();

    for (let n = 1; n <= pdf.numPages; n++) {
      const pagina = await pdf.getPage(n);
      if (token !== this.tokenPintado) { return; }

      const natural = pagina.getViewport({ scale: 1 });
      const ajuste = anchoDisponible / natural.width;
      const vista = pagina.getViewport({ scale: ajuste * this.escala * dpr });

      const anchoCss = Math.floor(vista.width / dpr);
      const altoCss = Math.floor(vista.height / dpr);

      const lienzo = document.createElement('canvas');
      lienzo.width = Math.floor(vista.width);
      lienzo.height = Math.floor(vista.height);
      lienzo.style.width = `${anchoCss}px`;
      lienzo.style.height = `${altoCss}px`;
      lienzo.className = 'block mx-auto bg-white rounded-lg shadow-lg';
      // Medidas a escala 1: sirven para reescalar por CSS al instante mientras
      // llega el redibujado (ver aplicarEscalaCss).
      lienzo.dataset['ancho'] = String(anchoCss / this.escala);
      lienzo.dataset['alto'] = String(altoCss / this.escala);

      const contexto = lienzo.getContext('2d');
      if (contexto) {
        await pagina.render({ canvas: lienzo, canvasContext: contexto, viewport: vista }).promise;
      }
      if (token !== this.tokenPintado) { return; }

      fragmento.appendChild(lienzo);
    }

    contenedor.replaceChildren(fragmento);
  }

  /**
   * Reescala por CSS lo que ya esta dibujado. El redibujado real cuesta unos
   * cientos de ms; sin esto el zoom parecia no responder.
   */
  private aplicarEscalaCss(): void {
    if (!this.hojas) { return; }
    for (const hijo of Array.from(this.hojas.children)) {
      const lienzo = hijo as HTMLElement;
      const ancho = Number(lienzo.dataset['ancho']);
      const alto = Number(lienzo.dataset['alto']);
      if (!ancho || !alto) { continue; }
      lienzo.style.width = `${Math.floor(ancho * this.escala)}px`;
      lienzo.style.height = `${Math.floor(alto * this.escala)}px`;
    }
  }

  private programarRepintado(): void {
    if (!this.esPdf) { return; }
    this.aplicarEscalaCss();
    if (this.temporizadorPintado) { clearTimeout(this.temporizadorPintado); }
    this.temporizadorPintado = setTimeout(() => void this.pintarPaginas(), 180);
  }

  // ─── Zoom ──────────────────────────────────────────────────
  get zoomPorcentaje(): number {
    return Math.round(this.escala * 100);
  }

  get puedeZoom(): boolean {
    return (this.esPdf && !this.errorPdf) || this.esImagen;
  }

  acercar(): void { this.fijarEscala(this.escala + PASO_ESCALA); }
  alejar(): void { this.fijarEscala(this.escala - PASO_ESCALA); }
  ajustar(): void { this.fijarEscala(1); }

  private fijarEscala(valor: number): void {
    const nueva = Math.min(ESCALA_MAX, Math.max(ESCALA_MIN, Math.round(valor * 100) / 100));
    if (nueva === this.escala) { return; }
    this.escala = nueva;
    this.programarRepintado();
  }

  // ─── Pinza y trackpad ──────────────────────────────────────
  alIniciarToque(evento: TouchEvent): void {
    if (evento.touches.length !== 2) { return; }
    this.distanciaPinza = this.separacion(evento.touches);
    this.escalaPinza = this.escala;
  }

  alMoverToque(evento: TouchEvent): void {
    if (evento.touches.length !== 2 || !this.distanciaPinza) { return; }
    // Sin preventDefault el navegador hace su propio pinch-zoom de toda la
    // pagina y el modal, que es fixed, se sale de la pantalla.
    evento.preventDefault();
    this.fijarEscala(this.escalaPinza * (this.separacion(evento.touches) / this.distanciaPinza));
  }

  alTerminarToque(): void {
    this.distanciaPinza = 0;
  }

  /** ctrl+rueda es el pinch de los trackpads y el zoom con rueda del raton. */
  alRodar(evento: WheelEvent): void {
    if (!evento.ctrlKey) { return; }
    evento.preventDefault();
    this.fijarEscala(this.escala * (evento.deltaY < 0 ? 1.1 : 1 / 1.1));
  }

  private separacion(toques: TouchList): number {
    const dx = toques[0].clientX - toques[1].clientX;
    const dy = toques[0].clientY - toques[1].clientY;
    return Math.hypot(dx, dy);
  }

  /** Al girar el celular cambia el ancho disponible y el ajuste ya no sirve. */
  @HostListener('window:resize')
  alRedimensionar(): void {
    if (this.visible && this.pdf) { this.programarRepintado(); }
  }

  @HostListener('document:keydown.escape')
  alEscape(): void {
    if (this.visible) { this.cerrar(); }
  }

  // ─── Acciones ──────────────────────────────────────────────
  descargar(): void {
    if (!this.blob) { return; }
    const url = this.objectUrl ?? URL.createObjectURL(this.blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.nombreArchivo;
    a.click();
  }

  cerrar(): void {
    this.cerrarModal.emit();
  }

  // ─── Limpieza ──────────────────────────────────────────────
  private limpiar(): void {
    this.tokenPintado++;
    if (this.temporizadorPintado) {
      clearTimeout(this.temporizadorPintado);
      this.temporizadorPintado = null;
    }
    if (this.pdf) {
      void this.pdf.destroy();
      this.pdf = null;
    }
    this.hojas?.replaceChildren();
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
    this.urlSegura = null;
    this.esPdf = false;
    this.esImagen = false;
    this.paginas = 0;
    this.errorPdf = false;
    this.cargando = false;
    this.escala = 1;
  }

  ngOnDestroy(): void {
    this.limpiar();
  }
}
