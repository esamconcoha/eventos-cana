import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RecoleccionService, mensajeErrorRecoleccion } from '../../../../services/recoleccion.service';
import { ToastService } from '../../../../shared/toast/toast.service';
import { RecoleccionDetalle, ItemRecoleccion } from '../../../../interfaces/recoleccion';
import { DetalleViaje } from '../../../../interfaces/entrega';
import { EstadoLogisticoDef, estadoLogisticoPorCodigo } from '../../../../interfaces/estado-logistico';

// El selector de "Registrar viaje de vuelta" solo ofrece lo que realmente
// salió y todavía no volvió, nunca el catálogo ni el detalle del pedido.
interface ItemViajeChecklist {
  item: ItemRecoleccion;
  seleccionado: boolean;
  cantidad: number;
}

@Component({
  selector: 'app-recoleccion-detalle',
  standalone: false,
  templateUrl: './recoleccion-detalle.component.html',
  styleUrl: './recoleccion-detalle.component.css'
})
export class RecoleccionDetalleComponent implements OnInit {

  idRecoleccion!: number;
  recoleccion: RecoleccionDetalle | null = null;
  cargando = false;
  viajeExpandidoId: number | null = null;

  mostrarRegistrarViaje = false;
  mostrarProgramar = false;
  itemsChecklist: ItemViajeChecklist[] = [];
  formViaje: FormGroup;
  formProgramar: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private recoleccionService: RecoleccionService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.formViaje = this.fb.group({
      fechaInicioViaje: ['', Validators.required],
      fechaFinViaje: ['']
    });
    this.formProgramar = this.fb.group({
      fechaRecoleccion: ['', Validators.required],
      cantidadViajesAproximados: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.idRecoleccion = Number(this.route.snapshot.paramMap.get('idRecoleccion'));
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.recoleccionService.obtenerRecoleccion(this.idRecoleccion).subscribe({
      next: (data) => {
        this.recoleccion = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  volver(): void {
    this.router.navigate(['/gestion-interna/eventos/recolecciones']);
  }

  estadoPedidoDef(): EstadoLogisticoDef | undefined {
    return estadoLogisticoPorCodigo(this.recoleccion?.estadoActualPedido?.codigoEstado);
  }

  estadoPedidoLabel(): string {
    return this.recoleccion?.estadoActualPedido?.nombreEstado ?? '—';
  }

  toggleViajeExpandido(idViaje: number): void {
    this.viajeExpandidoId = this.viajeExpandidoId === idViaje ? null : idViaje;
  }

  totalItemsViaje(viaje: DetalleViaje): number {
    return (viaje.items ?? []).reduce((acc, i) => acc + i.cantidadItem, 0);
  }

  // ─── Programar ─────────────────────────────────────────────
  abrirProgramar(): void {
    this.mostrarProgramar = true;
    this.formProgramar.reset({
      fechaRecoleccion: this.recoleccion?.fechaRecoleccion?.slice(0, 10) ?? '',
      cantidadViajesAproximados: this.recoleccion?.cantidadViajesAproximados ?? 1
    });
  }

  cerrarProgramar(): void { this.mostrarProgramar = false; }

  guardarProgramacion(): void {
    if (this.formProgramar.invalid) { this.formProgramar.markAllAsTouched(); return; }
    this.recoleccionService.programar(this.idRecoleccion, {
      fechaRecoleccion: this.formProgramar.value.fechaRecoleccion,
      cantidadViajesAproximados: Number(this.formProgramar.value.cantidadViajesAproximados)
    }).subscribe({
      next: (actualizada) => {
        this.recoleccion = actualizada;
        this.toast.success('Recolección agendada');
        this.cerrarProgramar();
      },
      error: (err) => this.toast.error('Error', mensajeErrorRecoleccion(err, 'No se pudo agendar'))
    });
  }

  // ─── Registrar viaje de vuelta ─────────────────────────────
  abrirRegistrarViaje(): void {
    this.mostrarRegistrarViaje = true;
    this.formViaje.reset({ fechaInicioViaje: this.isoLocalAhora() });
    this.itemsChecklist = (this.recoleccion?.items ?? [])
      .filter(item => item.cantidadPendiente > 0)
      .map(item => ({ item, seleccionado: false, cantidad: item.cantidadPendiente }));
  }

  cerrarRegistrarViaje(): void { this.mostrarRegistrarViaje = false; }

  /** El picker ya entrega "yyyy-MM-ddTHH:mm:ss" en hora local. */
  private isoLocalAhora(): string {
    const a = new Date();
    const p = (n: number) => String(n).padStart(2, '0');
    return `${a.getFullYear()}-${p(a.getMonth() + 1)}-${p(a.getDate())}`
         + `T${p(a.getHours())}:${p(a.getMinutes())}:00`;
  }

  get siguienteNumeroViaje(): number {
    return (this.recoleccion?.viajes?.length ?? this.recoleccion?.cantidadViajesReales ?? 0) + 1;
  }

  get itemsSeleccionadosCount(): number {
    return this.itemsChecklist.filter(i => i.seleccionado).length;
  }

  toggleItemChecklist(i: ItemViajeChecklist): void { i.seleccionado = !i.seleccionado; }

  campoInvalido(form: FormGroup, campo: string): boolean {
    const c = form.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  guardarViaje(): void {
    if (this.formViaje.invalid || !this.recoleccion) { this.formViaje.markAllAsTouched(); return; }

    const payload = {
      idEntrega: this.recoleccion.idRecoleccion,
      fechaInicioViaje: this.formViaje.value.fechaInicioViaje,
      fechaFinViaje: this.formViaje.value.fechaFinViaje || null,
      items: this.itemsChecklist
        .filter(i => i.seleccionado)
        .map(i => ({ idItem: i.item.idItem, cantidadItem: i.cantidad }))
    };

    this.recoleccionService.registrarViaje(payload).subscribe({
      next: (actualizada) => {
        this.recoleccion = actualizada;
        this.toast.success('Viaje de vuelta registrado');
        this.cerrarRegistrarViaje();
      },
      error: (err) => this.toast.error('Error', mensajeErrorRecoleccion(err, 'No se pudo registrar el viaje'))
    });
  }

  // ─── Finalizar ─────────────────────────────────────────────
  // Cerrar con pendientes es válido: lo que no volvió queda como faltante del
  // pedido. Se avisa con detalle porque es una decisión con consecuencia.
  marcarFinalizada(): void {
    if (!this.recoleccion) { return; }

    const faltantes = this.recoleccion.items.filter(i => i.cantidadPendiente > 0);

    if (faltantes.length > 0) {
      const detalle = faltantes
        .slice(0, 5)
        .map(i => `${i.nombreItem} (faltan ${i.cantidadPendiente})`)
        .join(', ');
      const extra = faltantes.length > 5 ? ` y ${faltantes.length - 5} más` : '';

      this.toast.confirm({
        title: `${faltantes.length} artículo(s) no volvieron`,
        message: `${detalle}${extra}. Al cerrar la recolección quedan registrados como faltantes del pedido.`,
        confirmText: 'Cerrar con faltantes',
        cancelText: 'Cancelar',
        onConfirm: () => this.finalizar()
      });
      return;
    }

    this.toast.confirm({
      title: '¿Finalizar la recolección?',
      message: `Volvió todo lo entregado del pedido ${this.recoleccion.correlativoPedido}.`,
      confirmText: 'Sí, finalizar',
      cancelText: 'Cancelar',
      onConfirm: () => this.finalizar()
    });
  }

  private finalizar(): void {
    this.recoleccionService.marcarFinalizada(this.idRecoleccion).subscribe({
      next: (actualizada) => {
        this.recoleccion = actualizada;
        this.toast.success('Recolección finalizada');
      },
      error: (err) => this.toast.error('Error', mensajeErrorRecoleccion(err, 'No se pudo finalizar'))
    });
  }
}
