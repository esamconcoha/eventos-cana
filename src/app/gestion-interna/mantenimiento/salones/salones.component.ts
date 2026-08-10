import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SalonService } from '../../../services/salon.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Salon, GuardarSalon } from '../../../interfaces/salon';

@Component({
  selector: 'app-salones',
  standalone: false,
  templateUrl: './salones.component.html',
  styleUrl: './salones.component.css'
})
export class SalonesComponent implements OnInit {

  salones: Salon[] = [];
  salonesFiltrados: Salon[] = [];
  cargando = false;
  mostrarForm = false;
  modoEdicion = false;
  salonEditando: Salon | null = null;

  textoBusqueda = '';
  paginaActual = 1;
  porPagina = 10;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private salonService: SalonService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      nombreSalon:    ['', [Validators.required, Validators.maxLength(100)]],
      direccionSalon: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  ngOnInit(): void {
    this.cargarSalones();
  }

  cargarSalones(): void {
    this.cargando = true;
    this.salonService.listarSalones().subscribe({
      next: (data) => {
        this.salones = data;
        this.aplicarFiltro();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  // ─── Filtro y paginación ─────────────────────────────────
  aplicarFiltro(): void {
    const txt = this.textoBusqueda.toLowerCase().trim();
    this.salonesFiltrados = txt
      ? this.salones.filter(s =>
          s.nombreSalon.toLowerCase().includes(txt) ||
          s.direccionSalon.toLowerCase().includes(txt)
        )
      : [...this.salones];
    this.paginaActual = 1;
  }

  get totalPaginas(): number {
    return Math.ceil(this.salonesFiltrados.length / this.porPagina);
  }

  get salonesPaginados(): Salon[] {
    const ini = (this.paginaActual - 1) * this.porPagina;
    return this.salonesFiltrados.slice(ini, ini + this.porPagina);
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  cambiarPagina(p: number): void {
    if (p >= 1 && p <= this.totalPaginas) this.paginaActual = p;
  }

  // ─── Crear / Editar ──────────────────────────────────────
  abrirCrear(): void {
    this.modoEdicion = false;
    this.salonEditando = null;
    this.form.reset();
    this.mostrarForm = true;
  }

  abrirEditar(s: Salon): void {
    this.modoEdicion = true;
    this.salonEditando = s;
    this.form.patchValue({
      nombreSalon: s.nombreSalon,
      direccionSalon: s.direccionSalon
    });
    this.mostrarForm = true;
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload: GuardarSalon = this.form.value;

    const req$ = this.modoEdicion && this.salonEditando
      ? this.salonService.editarSalon(this.salonEditando.idSalon, payload)
      : this.salonService.guardarSalon(payload);

    req$.subscribe({
      next: () => {
        this.toast.success(this.modoEdicion ? 'Salón actualizado' : 'Salón creado');
        this.cancelar();
        this.cargarSalones();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo guardar')
    });
  }

  cancelar(): void {
    this.form.reset();
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.salonEditando = null;
  }

  // ─── Eliminar (lógico) ────────────────────────────────────
  confirmarEliminar(s: Salon): void {
    this.toast.confirm({
      title: '¿Eliminar salón?',
      message: `¿Estás seguro de eliminar "${s.nombreSalon}"?`,
      confirmText: 'Sí, eliminar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.salonService.eliminarSalon(s.idSalon).subscribe({
          next: () => { this.toast.success('Salón eliminado'); this.cargarSalones(); },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo eliminar')
        });
      }
    });
  }

  campoInvalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c?.invalid && c?.touched);
  }
}
