import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

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

  urlSegura: SafeResourceUrl | null = null;
  esPdf = false;
  /** La constancia firmada suele subirse como foto de celular, no como PDF. */
  esImagen = false;
  private objectUrl: string | null = null;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['blob'] || changes['visible']) {
      if (this.visible && this.blob) {
        this.generarUrl();
      } else {
        this.limpiarUrl();
      }
    }
  }

  private generarUrl(): void {
    this.limpiarUrl();
    if (!this.blob) return;
    this.esPdf = this.blob.type === 'application/pdf';
    this.esImagen = this.blob.type.startsWith('image/');
    this.objectUrl = URL.createObjectURL(this.blob);
    this.urlSegura = this.sanitizer.bypassSecurityTrustResourceUrl(this.objectUrl);
  }

  private limpiarUrl(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
    this.urlSegura = null;
  }

  descargar(): void {
    if (!this.blob) return;
    const url = this.objectUrl ?? URL.createObjectURL(this.blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = this.nombreArchivo;
    a.click();
  }

  cerrar(): void {
    this.cerrarModal.emit();
  }

  ngOnDestroy(): void {
    this.limpiarUrl();
  }
}
