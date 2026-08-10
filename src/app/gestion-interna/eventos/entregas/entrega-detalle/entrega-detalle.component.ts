import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntregaService, mensajeErrorEntrega, categoriaFechaEntrega,
         TIPOS_CONSTANCIA_FIRMADA_PERMITIDOS, TAMANIO_MAXIMO_CONSTANCIA_FIRMADA } from '../../../../services/entrega.service';
import { ToastService } from '../../../../shared/toast/toast.service';
import { EntregaPedido, DetalleViaje, EntregaItemPendiente, EstadoEntregaDerivado } from '../../../../interfaces/entrega';
import { EstadoLogisticoDef, estadoLogisticoPorCodigo } from '../../../../interfaces/estado-logistico';
import { DetalleServicioPedido } from '../../../../interfaces/pedido';
import { PedidoService } from '../../../../services/pedido.service';
import { TokenService } from '../../../../services/token.service';
import { LoadingService } from '../../../../services/loading.service';

interface EstadoEntregaDef {
  codigo: EstadoEntregaDerivado;
  label: string;
  dot: string;
  badgeClass: string;
}

// El selector de "Registrar viaje" solo ofrece los items del PEDIDO (con su
// cantidad pendiente), nunca el catálogo completo de items_cana.
interface ItemViajeChecklist {
  item: EntregaItemPendiente;
  seleccionado: boolean;
  cantidad: number;
}

@Component({
  selector: 'app-entrega-detalle',
  standalone: false,
  templateUrl: './entrega-detalle.component.html',
  styleUrl: './entrega-detalle.component.css'
})
export class EntregaDetalleComponent implements OnInit {

  // Categoría por fecha PACTADA (pedidos_cana.fecha_entrega) vs. hoy, no por
  // si ya se registró un viaje — ver categoriaFechaEntrega() en el servicio.
  estados: EstadoEntregaDef[] = [
    { codigo: 'ATRASADA',   label: 'Atrasada',   dot: 'bg-red-500',     badgeClass: 'bg-red-50 text-red-600 ring-red-200' },
    { codigo: 'HOY',        label: 'Hoy',        dot: 'bg-amber-400',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
    { codigo: 'PROGRAMADA', label: 'Programada', dot: 'bg-sky-400',     badgeClass: 'bg-sky-50 text-sky-700 ring-sky-200' },
    { codigo: 'SIN_FECHA',  label: 'Sin fecha',  dot: 'bg-slate-300',   badgeClass: 'bg-slate-50 text-slate-500 ring-slate-200' },
    { codigo: 'FINALIZADA', label: 'Finalizada', dot: 'bg-emerald-500', badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200' }
  ];

  idEntrega!: number;
  entrega: EntregaPedido | null = null;
  cargando = false;
  viajeExpandidoId: number | null = null;

  // ─── Modal: Registrar viaje ───────────────────────────────
  mostrarRegistrarViaje = false;
  itemsChecklist: ItemViajeChecklist[] = [];
  form: FormGroup;

  // ─── Visor: constancia de entrega (PDF) ───────────────────
  documentoBlob: Blob | null = null;
  mostrarVisorDocumento = false;
  documentoNombre = 'constancia-entrega.pdf';
  documentoTitulo = 'Constancia de entrega';
  generandoConstancia = false;

  // ─── Constancia FIRMADA (foto/escaneo subido por el usuario) ──
  subiendoConstanciaFirmada = false;
  cargandoConstanciaFirmada = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private entregaService: EntregaService,
    private pedidoService: PedidoService,
    private tokenService: TokenService,
    private toast: ToastService,
    private loading: LoadingService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      fechaInicioViaje: ['', Validators.required],
      fechaFinViaje: ['']
    });
  }

  ngOnInit(): void {
    this.idEntrega = Number(this.route.snapshot.paramMap.get('idEntrega'));
    this.cargarEntrega();
  }

  cargarEntrega(): void {
    this.cargando = true;
    this.entregaService.obtenerEntrega(this.idEntrega).subscribe({
      next: (data) => {
        this.entrega = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  volver(): void {
    this.router.navigate(['/gestion-interna/eventos/entregas']);
  }

  // ─── Categoría por fecha (eje de agenda, no de estado) ────────
  estadoDerivado(entrega: EntregaPedido): EstadoEntregaDerivado {
    return categoriaFechaEntrega(entrega);
  }

  estadoDef(entrega: EntregaPedido): EstadoEntregaDef {
    return this.estados.find(e => e.codigo === this.estadoDerivado(entrega))!;
  }

  // ─── Estado logístico real del pedido ──────────────────────
  // Mismo catálogo compartido que usan Pedidos y el listado de Entregas.
  estadoPedidoDef(entrega: EntregaPedido): EstadoLogisticoDef | undefined {
    return estadoLogisticoPorCodigo(entrega.estadoActualPedido?.codigoEstado);
  }

  estadoPedidoLabel(entrega: EntregaPedido): string {
    return entrega.estadoActualPedido?.nombreEstado
        ?? this.estadoPedidoDef(entrega)?.label
        ?? '—';
  }

  // ─── Viajes registrados ────────────────────────────────────
  toggleViajeExpandido(idViaje: number): void {
    this.viajeExpandidoId = this.viajeExpandidoId === idViaje ? null : idViaje;
  }

  totalItemsViaje(viaje: DetalleViaje): number {
    return (viaje.items ?? []).reduce((acc, i) => acc + i.cantidadItem, 0);
  }

  // ─── Modal: Registrar viaje ────────────────────────────────
  // Solo items del pedido con saldo pendiente; el stepper arranca en
  // cantidadPendiente (tope real lo valida el backend con el código 4019).
  abrirRegistrarViaje(): void {
    this.mostrarRegistrarViaje = true;
    this.form.reset({ fechaInicioViaje: this.isoLocalAhora() });
    this.itemsChecklist = (this.entrega?.items ?? [])
      .filter(item => item.cantidadPendiente > 0)
      .map(item => ({ item, seleccionado: false, cantidad: item.cantidadPendiente }));
  }

  cerrarRegistrarViaje(): void {
    this.mostrarRegistrarViaje = false;
  }

  /** El picker ya entrega "yyyy-MM-ddTHH:mm:ss" en hora local. */
  private isoLocalAhora(): string {
    const a = new Date();
    const p = (n: number) => String(n).padStart(2, '0');
    return `${a.getFullYear()}-${p(a.getMonth() + 1)}-${p(a.getDate())}`
         + `T${p(a.getHours())}:${p(a.getMinutes())}:00`;
  }

  get siguienteNumeroViaje(): number {
    return (this.entrega?.viajes?.length ?? this.entrega?.cantidadViajesReales ?? 0) + 1;
  }

  get itemsSeleccionadosCount(): number {
    return this.itemsChecklist.filter(i => i.seleccionado).length;
  }

  toggleItemChecklist(i: ItemViajeChecklist): void {
    i.seleccionado = !i.seleccionado;
  }

  campoInvalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  guardarViaje(): void {
    if (this.form.invalid || !this.entrega) { this.form.markAllAsTouched(); return; }

    // El backend espera LocalDateTime plano ("2026-07-27T08:30:00"), sin
    // convertir a UTC/ISO — el valor del input datetime-local ya está en hora
    // local y solo le falta el segundo (viene como "YYYY-MM-DDTHH:mm").
    const payload = {
      idEntrega: this.entrega.idEntrega,
      fechaInicioViaje: this.form.value.fechaInicioViaje,
      fechaFinViaje: this.form.value.fechaFinViaje || null,
      items: this.itemsChecklist
        .filter(i => i.seleccionado)
        .map(i => ({ idItem: i.item.idItem, cantidadItem: i.cantidad }))
    };

    this.entregaService.registrarViaje(payload).subscribe({
      next: (actualizada) => {
        this.entrega = actualizada;
        this.toast.success('Viaje registrado');
        this.cerrarRegistrarViaje();
      },
      error: (err) => this.toast.error('Error', mensajeErrorEntrega(err, 'No se pudo registrar el viaje'))
    });
  }

  // ─── Servicios contratados ─────────────────────────────────
  // Mismo endpoint que en Pedidos: el servicio se registra por línea, no como
  // estado del pedido, porque ocurre en paralelo a los viajes.
  alternarServicioRealizado(servicio: DetalleServicioPedido): void {
    if (!servicio.idDetalleServPedido) { return; }
    const realizado = !servicio.fechaRealizado;
    const nombre = servicio.nombreServicio || `Servicio #${servicio.idServicio}`;

    this.toast.confirm({
      title: realizado ? '¿Confirmar que ya se realizó?' : '¿Marcar como pendiente?',
      message: realizado
        ? `Se registrará "${nombre}" como realizado con la fecha y hora de ahora.`
        : `Se borrará la fecha en que se confirmó "${nombre}".`,
      confirmText: realizado ? 'Sí, ya se realizó' : 'Sí, marcar pendiente',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.pedidoService.marcarServicioRealizado(servicio.idDetalleServPedido!, realizado).subscribe({
          // La respuesta es el pedido, no la entrega: se recarga el detalle.
          next: () => {
            this.cargarEntrega();
            this.toast.success(realizado ? 'Servicio confirmado' : 'Servicio marcado pendiente');
          },
          error: () => this.toast.error('Error', 'No se pudo actualizar el servicio')
        });
      }
    });
  }

  // ─── Marcar finalizada ─────────────────────────────────────
  // Advertencia previa (solo informativa, no bloquea): compara lo pedido vs.
  // lo enviado usando entrega.items (ya cargado por obtenerEntrega) y avisa
  // si quedan artículos pendientes antes de pedir la confirmación final.
  marcarFinalizada(): void {
    if (!this.entrega) return;

    const itemsFaltantes = (this.entrega.items ?? []).filter(i => i.cantidadPendiente > 0);

    if (itemsFaltantes.length > 0) {
      const detalle = itemsFaltantes
        .slice(0, 5)
        .map(i => `${i.nombreItem} (faltan ${i.cantidadPendiente})`)
        .join(', ');
      const extra = itemsFaltantes.length > 5 ? ` y ${itemsFaltantes.length - 5} más` : '';

      this.toast.confirm({
        title: `Faltan ${itemsFaltantes.length} artículo(s) por entregar`,
        message: `${detalle}${extra}. Es solo una advertencia: puedes finalizar la entrega de todas formas.`,
        confirmText: 'Finalizar de todas formas',
        cancelText: 'Cancelar',
        onConfirm: () => this.finalizarEntrega()
      });
      return;
    }

    this.toast.confirm({
      title: '¿Marcar entrega como finalizada?',
      message: `Pedido ${this.entrega.correlativoPedido}`,
      confirmText: 'Sí, finalizar',
      cancelText: 'Cancelar',
      onConfirm: () => this.finalizarEntrega()
    });
  }

  private finalizarEntrega(): void {
    this.entregaService.marcarFinalizada(this.entrega!.idEntrega).subscribe({
      next: (actualizada) => {
        this.entrega = actualizada;
        this.toast.success('Entrega finalizada');
      },
      error: (err) => this.toast.error('Error', mensajeErrorEntrega(err, 'No se pudo finalizar la entrega'))
    });
  }

  // ─── Constancia de entrega (PDF firmable) ──────────────────
  // Comprobante de lo entregado para que el cliente firme al recibir. Se puede
  // generar en cualquier momento (muestra lo despachado hasta ahora), no solo
  // al finalizar, útil para entregas parciales o para reimprimir.
  generarConstancia(): void {
    if (!this.entrega || this.generandoConstancia) { return; }
    this.generandoConstancia = true;
    this.loading.show();
    this.entregaService.obtenerConstancia(this.entrega.idEntrega).subscribe({
      next: (response) => {
        this.generandoConstancia = false;
        this.loading.hide();
        if (!response.body) {
          this.toast.error('Error', 'No se pudo generar la constancia');
          return;
        }
        this.documentoBlob = response.body;
        this.documentoNombre = `constancia-${this.entrega?.correlativoPedido || 'entrega'}.pdf`;
        this.documentoTitulo = `Constancia · ${this.entrega?.correlativoPedido}`;
        this.mostrarVisorDocumento = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.generandoConstancia = false;
        this.loading.hide();
        this.manejarErrorBlob(err, 'No se pudo generar la constancia');
      }
    });
  }

  // El backend responde con blob PDF; en error el body también es un blob JSON,
  // así que hay que leerlo como texto para sacar el message (mismo criterio que
  // en Cotizaciones).
  private manejarErrorBlob(err: any, mensajeDefault: string): void {
    const errorBlob: Blob | undefined = err?.error;
    if (errorBlob instanceof Blob && errorBlob.type.includes('json')) {
      errorBlob.text().then(texto => {
        let mensaje = mensajeDefault;
        try { mensaje = JSON.parse(texto)?.message ?? mensaje; } catch { /* ignore */ }
        this.toast.error('Error', mensaje);
      });
      return;
    }
    this.toast.error('Error', err?.error?.message ?? mensajeDefault);
  }

  cerrarVisorDocumento(): void {
    this.mostrarVisorDocumento = false;
    this.documentoBlob = null;
  }

  // ─── Constancia FIRMADA (foto/escaneo subido por el usuario) ──
  // Es un archivo distinto de la constancia de arriba: esa la genera el
  // sistema para imprimir y firmar; esta es la evidencia de que ya se firmó.

  /** Dispara el <input type="file"> oculto del template. */
  elegirArchivoConstanciaFirmada(input: HTMLInputElement): void {
    if (this.subiendoConstanciaFirmada) { return; }
    input.value = '';   // permite re-seleccionar el mismo archivo tras un error
    input.click();
  }

  onArchivoConstanciaFirmadaSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0];
    if (!archivo || !this.entrega) { return; }

    if (!TIPOS_CONSTANCIA_FIRMADA_PERMITIDOS.includes(archivo.type)) {
      this.toast.error('Archivo no permitido', 'Debe ser PDF, JPG, PNG o WEBP');
      return;
    }
    if (archivo.size > TAMANIO_MAXIMO_CONSTANCIA_FIRMADA) {
      this.toast.error('Archivo muy grande', 'El archivo no puede superar los 10MB');
      return;
    }

    this.subiendoConstanciaFirmada = true;
    this.loading.show();
    this.entregaService.subirConstanciaFirmada(this.entrega.idEntrega, archivo, this.tokenService.getDpi()).subscribe({
      next: (actualizada) => {
        this.entrega = actualizada;
        this.subiendoConstanciaFirmada = false;
        this.loading.hide();
        this.toast.success('Constancia firmada guardada');
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.subiendoConstanciaFirmada = false;
        this.loading.hide();
        this.toast.error('Error', mensajeErrorEntrega(err, 'No se pudo subir la constancia firmada'));
        this.cdr.detectChanges();
      }
    });
  }

  verConstanciaFirmada(): void {
    if (!this.entrega || this.cargandoConstanciaFirmada) { return; }
    this.cargandoConstanciaFirmada = true;
    this.loading.show();
    this.entregaService.obtenerConstanciaFirmada(this.entrega.idEntrega).subscribe({
      next: (response) => {
        this.cargandoConstanciaFirmada = false;
        this.loading.hide();
        if (!response.body) {
          this.toast.error('Error', 'No se pudo obtener la constancia firmada');
          return;
        }
        this.documentoBlob = response.body;
        this.documentoNombre = this.extraerNombreArchivo(response.headers.get('Content-Disposition'))
          ?? `constancia-firmada-${this.entrega?.correlativoPedido || 'entrega'}`;
        this.documentoTitulo = `Constancia firmada · ${this.entrega?.correlativoPedido}`;
        this.mostrarVisorDocumento = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargandoConstanciaFirmada = false;
        this.loading.hide();
        this.manejarErrorBlob(err, 'No se pudo obtener la constancia firmada');
      }
    });
  }

  private extraerNombreArchivo(contentDisposition: string | null): string | null {
    if (!contentDisposition) return null;
    const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
    return match ? decodeURIComponent(match[1]) : null;
  }
}
