import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ServicioService } from '../../../services/servicio.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { ServicioDecoracion, CategoriaServicio, CrearServicio } from '../../../interfaces/servicio';

@Component({
  selector: 'app-servicios',
  standalone: false,
  templateUrl: './servicios.component.html',
  styleUrl: './servicios.component.css'
})
export class ServiciosComponent implements OnInit {

  servicios: ServicioDecoracion[] = [];
  serviciosFiltrados: ServicioDecoracion[] = [];
  categorias: CategoriaServicio[] = [];
  cargando = false;
  mostrarForm = false;
  modoEdicion = false;
  servicioEditando: ServicioDecoracion | null = null;

  textoBusqueda = '';
  paginaActual = 1;
  porPagina = 10;

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private servicioService: ServicioService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      idCategoria:         [null, Validators.required],
      nombreServicio:      ['',   [Validators.required, Validators.maxLength(100)]],
      descripcionServicio: ['',   [Validators.required, Validators.maxLength(300)]],
      unidadMedida:        ['',   Validators.required],
      requiereDetalle:     [false]
    });
  }

  ngOnInit(): void {
    this.cargarCategorias();
    this.cargarServicios();
  }

  cargarCategorias(): void {
    this.servicioService.listarCategorias().subscribe({
      next: (data) => { this.categorias = data.filter(c => c.estadoRegistro); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarServicios(): void {
    this.cargando = true;
    this.servicioService.listarServicios().subscribe({
      next: (data) => {
        this.servicios = data;
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
    this.serviciosFiltrados = txt
      ? this.servicios.filter(s =>
          s.nombreServicio.toLowerCase().includes(txt) ||
          (s.nombreCategoria ?? '').toLowerCase().includes(txt) ||
          s.descripcionServicio.toLowerCase().includes(txt)
        )
      : [...this.servicios];
    this.paginaActual = 1;
  }

  get totalPaginas(): number {
    return Math.ceil(this.serviciosFiltrados.length / this.porPagina);
  }

  get serviciosPaginados(): ServicioDecoracion[] {
    const ini = (this.paginaActual - 1) * this.porPagina;
    return this.serviciosFiltrados.slice(ini, ini + this.porPagina);
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
    this.servicioEditando = null;
    this.form.reset({ requiereDetalle: false });
    this.mostrarForm = true;
  }

  abrirEditar(s: ServicioDecoracion): void {
    this.modoEdicion = true;
    this.servicioEditando = s;
    this.form.patchValue({
      idCategoria:         s.idCategoria,
      nombreServicio:      s.nombreServicio,
      descripcionServicio: s.descripcionServicio,
      unidadMedida:        s.unidadMedida,
      requiereDetalle:     s.requiereDetalle
    });
    this.mostrarForm = true;
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload: CrearServicio = this.form.value;

    const req$ = this.modoEdicion && this.servicioEditando
      ? this.servicioService.editarServicio(this.servicioEditando.idServicio, payload)
      : this.servicioService.guardarServicio(payload);

    req$.subscribe({
      next: () => {
        this.toast.success(this.modoEdicion ? 'Servicio actualizado' : 'Servicio creado');
        this.cancelar();
        this.cargarServicios();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo guardar')
    });
  }

  cancelar(): void {
    this.form.reset({ requiereDetalle: false });
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.servicioEditando = null;
  }

  // ─── Inactivar ───────────────────────────────────────────
  confirmarInactivar(s: ServicioDecoracion): void {
    this.toast.confirm({
      title: '¿Inactivar servicio?',
      message: `¿Estás seguro de inactivar "${s.nombreServicio}"?`,
      confirmText: 'Sí, inactivar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.servicioService.inactivarServicio(s.idServicio).subscribe({
          next: () => { this.toast.success('Servicio inactivado'); this.cargarServicios(); },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo inactivar')
        });
      }
    });
  }

  campoInvalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c?.invalid && c?.touched);
  }
}
