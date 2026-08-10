import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { CotizacionService } from '../../../services/cotizacion.service';
import { ItemService } from '../../../services/item.service';
import { ServicioService } from '../../../services/servicio.service';
import { CatalogoService } from '../../../services/catalogo.service';
import { TokenService } from '../../../services/token.service';
import { LoadingService } from '../../../services/loading.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Cotizacion, CrearCotizacion, ActualizarCotizacion, ConfirmarCotizacion,
         EstadoCotizacion, cotizacionEsEditable } from '../../../interfaces/cotizacion';
import { ItemCana } from '../../../interfaces/item-cana';
import { ServicioDecoracion } from '../../../interfaces/servicio';
import { Catalogo } from '../../../interfaces/catalogo';
import { TrazaEvento } from '../../../interfaces/trazabilidad';

@Component({
  selector: 'app-cotizaciones',
  standalone: false,
  templateUrl: './cotizaciones.component.html',
  styleUrl: './cotizaciones.component.css'
})
export class CotizacionesComponent implements OnInit {

  cotizaciones: Cotizacion[] = [];
  cotizacionesFiltradas: Cotizacion[] = [];
  items: ItemCana[] = [];
  servicios: ServicioDecoracion[] = [];
  tiposEvento: Catalogo[] = [];
  cargando = false;
  mostrarForm = false;
  /** Si tiene valor, el formulario esta editando esa cotizacion en vez de crear. */
  cotizacionEditando: Cotizacion | null = null;

  // ─── Visor de documento generado ───────────────────────────
  mostrarVisorDocumento = false;
  documentoBlob: Blob | null = null;
  documentoNombre = '';
  documentoTitulo = '';

  // ─── Trazabilidad (ventana emergente) ────────────────────
  mostrarTrazabilidad = false;
  cargandoTraza = false;
  trazaHistorial: TrazaEvento[] = [];
  trazaTitulo = '';
  trazaSubtitulo = '';

  // ─── Estados y filtros ───────────────────────────────────
  // El estado "Creada" se retira: las cotizaciones nuevas nacen en "Pendiente de
  // confirmar". No se ofrece como filtro.
  estadosDisponibles: { codigo: EstadoCotizacion; label: string }[] = [
    { codigo: 'P',    label: 'Pendiente de Confirmar' },
    { codigo: 'CONF', label: 'Confirmada' },
    { codigo: 'CAN',  label: 'Cancelada' }
  ];
  estadosSeleccionados: EstadoCotizacion[] = ['P', 'CONF', 'CAN'];
  textoClienteFiltro = '';

  // ─── Filtro por fecha del evento ─────────────────────────
  // Mismos presets que Pedidos, pero el default acá es "Todos": Cotizaciones es
  // un pipeline de propuestas a futuro, no una agenda de la semana; arrancar
  // filtrado a esta semana escondería la mayoría de las cotizaciones.
  rangoPreset: 'semana' | 'mes' | 'todos' | 'rango' = 'todos';
  fechaDesde: string | null = null;   // 'yyyy-mm-dd' (fecha local, inclusive)
  fechaHasta: string | null = null;   // 'yyyy-mm-dd' (fecha local, inclusive)
  readonly presetsFecha: { id: 'semana' | 'mes' | 'todos'; label: string }[] = [
    { id: 'semana', label: 'Esta semana' },
    { id: 'mes',    label: 'Este mes' },
    { id: 'todos',  label: 'Todos' }
  ];

  // ─── Selector de fecha/hora del evento ────────────────────
  calendarioAbierto = false;
  mesVisible = new Date();
  fechaEventoSeleccionada: Date | null = null;
  horaEvento = '';
  horaPickerAbierto = false;
  horasDisponibles = Array.from({ length: 24 }, (_, i) => i);
  minutosDisponibles = Array.from({ length: 12 }, (_, i) => i * 5);

  form: FormGroup;

  // ─── Confirmar cotización (pide fecha de entrega + viajes aprox.) ──
  // El backend usa estos datos para crear de una vez el registro en
  // entregas_pedido; ya no existe un paso manual de "Nueva entrega".
  mostrarConfirmar = false;
  cotizacionAConfirmar: Cotizacion | null = null;
  formConfirmar: FormGroup;

  constructor(
    private fb: FormBuilder,
    private cotizacionService: CotizacionService,
    private itemService: ItemService,
    private servicioService: ServicioService,
    private catalogoService: CatalogoService,
    private tokenService: TokenService,
    private toast: ToastService,
    private loading: LoadingService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      nombreClienteCotizacion:    ['', [Validators.required, Validators.maxLength(100)]],
      direccionClienteCotizacion: ['', [Validators.required, Validators.maxLength(200)]],
      telefonoClienteCotizacion:  ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      fechaHoraEvento:            ['', Validators.required],
      codTipoEvento:              [null, Validators.required],
      detalles: this.fb.array([this.nuevoDetalle()]),
      detallesServicios: this.fb.array([])
    });

    this.formConfirmar = this.fb.group({
      fechaEntregaEstimada:      ['', Validators.required],
      cantidadViajesAproximados: [2, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.cargarItems();
    this.cargarServicios();
    this.cargarTiposEvento();
    this.cargarCotizaciones();
  }

  // ─── Etiqueta legible de un estado ────────────────────────
  estadoLabel(codigo: string): string {
    return this.estadosDisponibles.find(e => e.codigo === codigo)?.label ?? codigo;
  }

  // ─── Filtros (estado + cliente) ───────────────────────────
  toggleEstadoFiltro(codigo: EstadoCotizacion): void {
    this.estadosSeleccionados = this.estadosSeleccionados.includes(codigo)
      ? this.estadosSeleccionados.filter(e => e !== codigo)
      : [...this.estadosSeleccionados, codigo];
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    const texto = this.textoClienteFiltro.toLowerCase().trim();
    this.cotizacionesFiltradas = this.cotizaciones.filter(c =>
      this.estadosSeleccionados.includes(c.estadoCotizacion) &&
      this.enRangoFecha(c) &&
      (!texto || c.nombreClienteCotizacion.toLowerCase().includes(texto))
    );
  }

  // ─── Rango de fechas (por fecha del evento) ──────────────
  private enRangoFecha(c: Cotizacion): boolean {
    if (!this.fechaDesde && !this.fechaHasta) { return true; }
    const fecha = (c.fechaHoraEvento ?? '').slice(0, 10);   // 'yyyy-mm-dd'
    if (!fecha) { return false; }
    if (this.fechaDesde && fecha < this.fechaDesde) { return false; }
    if (this.fechaHasta && fecha > this.fechaHasta) { return false; }
    return true;
  }

  private fmtLocal(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
  }

  seleccionarPreset(preset: 'semana' | 'mes' | 'todos'): void {
    const hoy = new Date();
    if (preset === 'semana') {
      const inicio = new Date(hoy);
      inicio.setDate(hoy.getDate() - hoy.getDay());     // domingo
      const fin = new Date(inicio);
      fin.setDate(inicio.getDate() + 6);                // sábado
      this.fechaDesde = this.fmtLocal(inicio);
      this.fechaHasta = this.fmtLocal(fin);
    } else if (preset === 'mes') {
      this.fechaDesde = this.fmtLocal(new Date(hoy.getFullYear(), hoy.getMonth(), 1));
      this.fechaHasta = this.fmtLocal(new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0));
    } else {
      this.fechaDesde = null;
      this.fechaHasta = null;
    }
    this.rangoPreset = preset;
    this.aplicarFiltros();
  }

  /** Al editar las fechas a mano, el preset pasa a "rango" personalizado. */
  onRangoManualChange(): void {
    this.rangoPreset = 'rango';
    this.aplicarFiltros();
  }

  // ─── Monto total de una cotización ya guardada ────────────
  montoTotal(cotizacion: Cotizacion): number {
    const totalItems = (cotizacion.detalles ?? []).reduce((acc, d) => {
      const costo = d.costoItem ?? this.getItem(d.idItem)?.costoItem ?? 0;
      return acc + costo * d.cantidadItemCotizacion;
    }, 0);
    const totalServicios = (cotizacion.detallesServicios ?? []).reduce(
      (acc, d) => acc + d.precioCotizado * d.cantidad, 0
    );
    return totalItems + totalServicios;
  }

  // ─── Items ───────────────────────────────────────────────
  cargarItems(): void {
    this.itemService.listarItems().subscribe({
      next: (data) => { this.items = data.filter(i => i.estadoItem); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Servicios ───────────────────────────────────────────
  cargarServicios(): void {
    this.servicioService.listarServicios().subscribe({
      next: (data) => { this.servicios = data.filter(s => s.estadoServicio); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Tipos de evento (catálogo) ───────────────────────────
  cargarTiposEvento(): void {
    this.catalogoService.getCatalogoByNombre('TIPO_EVENTOS').subscribe({
      next: (data) => { this.tiposEvento = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Cotizaciones ────────────────────────────────────────
  cargarCotizaciones(): void {
    this.cargando = true;
    this.cotizacionService.listarCotizaciones().subscribe({
      next: (data) => {
        this.cotizaciones = data;
        this.aplicarFiltros();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  // ─── Detalles de items (FormArray) ────────────────────────
  get detalles(): FormArray {
    return this.form.get('detalles') as FormArray;
  }

  nuevoDetalle(): FormGroup {
    return this.fb.group({
      idItem:                 [null, Validators.required],
      cantidadItemCotizacion: [1,   [Validators.required, Validators.min(1)]]
    });
  }

  agregarDetalle(): void {
    this.detalles.push(this.nuevoDetalle());
  }

  eliminarDetalle(i: number): void {
    if (this.detalles.length > 1) {
      this.detalles.removeAt(i);
    } else {
      this.toast.info('Aviso', 'Debe haber al menos un item en la cotización');
    }
  }

  // Obtiene el item seleccionado para mostrar nombre y costo
  getItem(idItem: number): ItemCana | undefined {
    return this.items.find(i => i.idItem === Number(idItem));
  }

  // Calcula el subtotal de una fila de item
  subtotalDetalle(index: number): number {
    const row = this.detalles.at(index).value;
    const item = this.getItem(row.idItem);
    return item ? (item.costoItem * (row.cantidadItemCotizacion || 0)) : 0;
  }

  // ─── Detalles de servicios (FormArray) ────────────────────
  get detallesServicios(): FormArray {
    return this.form.get('detallesServicios') as FormArray;
  }

  nuevoDetalleServicio(): FormGroup {
    return this.fb.group({
      idServicio:       [null, Validators.required],
      cantidad:         [1,   [Validators.required, Validators.min(1)]],
      precioCotizado:   [null, [Validators.required, Validators.min(0)]],
      especificaciones: ['']
    });
  }

  agregarDetalleServicio(): void {
    this.detallesServicios.push(this.nuevoDetalleServicio());
  }

  eliminarDetalleServicio(i: number): void {
    this.detallesServicios.removeAt(i);
  }

  // Obtiene el servicio seleccionado para mostrar nombre y si requiere detalle
  getServicio(idServicio: number): ServicioDecoracion | undefined {
    return this.servicios.find(s => s.idServicio === Number(idServicio));
  }

  // Calcula el subtotal de una fila de servicio
  subtotalDetalleServicio(index: number): number {
    const row = this.detallesServicios.at(index).value;
    return (row.precioCotizado || 0) * (row.cantidad || 0);
  }

  // Total general (items + servicios)
  get totalCotizacion(): number {
    const totalItems = this.detalles.controls.reduce((acc, _, i) => acc + this.subtotalDetalle(i), 0);
    const totalServicios = this.detallesServicios.controls.reduce((acc, _, i) => acc + this.subtotalDetalleServicio(i), 0);
    return totalItems + totalServicios;
  }

  // ─── Selector de fecha/hora del evento ────────────────────
  abrirCalendario(): void {
    this.calendarioAbierto = true;
  }

  cerrarCalendario(): void {
    this.calendarioAbierto = false;
    this.horaPickerAbierto = false;
  }

  get diasCalendario(): (Date | null)[] {
    const year = this.mesVisible.getFullYear();
    const month = this.mesVisible.getMonth();
    const primerDia = new Date(year, month, 1);
    const ultimoDia = new Date(year, month + 1, 0);
    const dias: (Date | null)[] = [];
    for (let i = 0; i < primerDia.getDay(); i++) dias.push(null);
    for (let d = 1; d <= ultimoDia.getDate(); d++) dias.push(new Date(year, month, d));
    return dias;
  }

  get nombreMesVisible(): string {
    const texto = this.mesVisible.toLocaleDateString('es-GT', { month: 'long', year: 'numeric' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  mesAnterior(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() - 1, 1);
  }

  mesSiguiente(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() + 1, 1);
  }

  seleccionarDia(dia: Date): void {
    this.fechaEventoSeleccionada = dia;
    this.actualizarFechaHoraEvento();
  }

  toggleHoraPicker(event: Event): void {
    event.stopPropagation();
    this.horaPickerAbierto = !this.horaPickerAbierto;
  }

  get horaSeleccionadaNum(): number | null {
    return this.horaEvento ? Number(this.horaEvento.split(':')[0]) : null;
  }

  get minutoSeleccionadoNum(): number | null {
    return this.horaEvento ? Number(this.horaEvento.split(':')[1]) : null;
  }

  seleccionarHora(h: number): void {
    const m = this.minutoSeleccionadoNum ?? 0;
    this.horaEvento = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
    this.actualizarFechaHoraEvento();
  }

  seleccionarMinuto(m: number): void {
    const h = this.horaSeleccionadaNum ?? 0;
    this.horaEvento = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
    this.actualizarFechaHoraEvento();
  }

  private actualizarFechaHoraEvento(): void {
    if (!this.fechaEventoSeleccionada) return;
    const [h, m] = (this.horaEvento || '00:00').split(':').map(Number);
    const dt = new Date(this.fechaEventoSeleccionada);
    dt.setHours(h || 0, m || 0, 0, 0);
    this.form.patchValue({ fechaHoraEvento: dt.toISOString() });
  }

  esHoy(dia: Date): boolean {
    return dia.toDateString() === new Date().toDateString();
  }

  esSeleccionado(dia: Date): boolean {
    return !!this.fechaEventoSeleccionada && dia.toDateString() === this.fechaEventoSeleccionada.toDateString();
  }

  get fechaEventoTexto(): string {
    if (!this.fechaEventoSeleccionada) return '';
    const fecha = this.fechaEventoSeleccionada.toLocaleDateString('es-GT', {
      day: 'numeric', month: 'long', year: 'numeric'
    });
    return this.horaEvento ? `${fecha}, ${this.horaEvento}` : fecha;
  }

  private resetCalendario(): void {
    this.calendarioAbierto = false;
    this.horaPickerAbierto = false;
    this.mesVisible = new Date();
    this.fechaEventoSeleccionada = null;
    this.horaEvento = '';
  }

  // ─── Editar ──────────────────────────────────────────────
  // Reutiliza el formulario de creación: los campos son los mismos y mantener
  // dos formularios en paralelo terminaría en que se despeguen.
  editar(cotizacion: Cotizacion): void {
    if (!cotizacionEsEditable(cotizacion.estadoCotizacion)) { return; }

    this.cotizacionEditando = cotizacion;
    this.mostrarForm = true;

    this.form.patchValue({
      nombreClienteCotizacion: cotizacion.nombreClienteCotizacion,
      direccionClienteCotizacion: cotizacion.direccionClienteCotizacion,
      telefonoClienteCotizacion: cotizacion.telefonoClienteCotizacion,
      codTipoEvento: cotizacion.codTipoEvento,
      fechaHoraEvento: cotizacion.fechaHoraEvento
    });

    // El calendario propio del formulario necesita su estado aparte; se arma
    // con partes locales para no correr el día en UTC-6.
    if (cotizacion.fechaHoraEvento) {
      const [f, h] = cotizacion.fechaHoraEvento.split('T');
      const [y, m, d] = f.split('-').map(Number);
      this.fechaEventoSeleccionada = new Date(y, m - 1, d);
      this.mesVisible = new Date(y, m - 1, 1);
      this.horaEvento = h ? h.slice(0, 5) : '';
    }

    this.detalles.clear();
    for (const d of cotizacion.detalles ?? []) {
      this.detalles.push(this.fb.group({
        idItem: [d.idItem, Validators.required],
        cantidadItemCotizacion: [d.cantidadItemCotizacion, [Validators.required, Validators.min(1)]]
      }));
    }
    if (this.detalles.length === 0) { this.detalles.push(this.nuevoDetalle()); }

    this.detallesServicios.clear();
    for (const s of cotizacion.detallesServicios ?? []) {
      this.detallesServicios.push(this.fb.group({
        idServicio: [s.idServicio, Validators.required],
        cantidad: [s.cantidad, [Validators.required, Validators.min(1)]],
        precioCotizado: [s.precioCotizado, [Validators.required, Validators.min(0)]],
        especificaciones: [s.especificaciones ?? '']
      }));
    }
  }

  // ─── Guardar ─────────────────────────────────────────────
  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.cotizacionEditando) { this.guardarEdicion(); return; }

    const payload: CrearCotizacion = {
      nombreClienteCotizacion: this.form.value.nombreClienteCotizacion,
      direccionClienteCotizacion: this.form.value.direccionClienteCotizacion,
      telefonoClienteCotizacion: Number(this.form.value.telefonoClienteCotizacion),
      fechaHoraEvento: this.form.value.fechaHoraEvento,
      cotizacionConfirmada: false,
      dpiNitUsuarioCotizacion: this.tokenService.getDpi(),
      codTipoEvento: this.form.value.codTipoEvento,
      detalleCotizacion: this.detalles.value.map((d: any) => ({
        idItem: Number(d.idItem),
        cantidadItemCotizacion: d.cantidadItemCotizacion
      })),
      detalleServicioCotizacion: this.detallesServicios.value.map((d: any) => ({
        idServicio: Number(d.idServicio),
        cantidad: d.cantidad,
        precioCotizado: d.precioCotizado,
        especificaciones: d.especificaciones || undefined
      }))
    };

    this.loading.show();
    this.cotizacionService.guardarCotizacion(payload).subscribe({
      next: (response) => {
        this.loading.hide();
        this.toast.success('Cotización creada');
        this.cancelar();
        this.cargarCotizaciones();

        if (response.body) {
          this.documentoBlob = response.body;
          this.documentoNombre = this.extraerNombreArchivo(response.headers.get('Content-Disposition'))
            ?? `cotizacion-${new Date().toISOString().slice(0, 10)}.pdf`;
          this.documentoTitulo = 'Cotización generada';
          this.mostrarVisorDocumento = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.loading.hide();
        this.manejarErrorBlob(err, 'No se pudo guardar la cotización');
      }
    });
  }

  /**
   * El backend regenera el PDF y lo guarda como una versión más, así que el
   * documento que se muestra acá ya es el actualizado y "Ver documento"
   * también devolverá este.
   */
  private guardarEdicion(): void {
    const id = this.cotizacionEditando!.idCotizacion;

    const payload: ActualizarCotizacion = {
      nombreClienteCotizacion: this.form.value.nombreClienteCotizacion,
      direccionClienteCotizacion: this.form.value.direccionClienteCotizacion,
      telefonoClienteCotizacion: Number(this.form.value.telefonoClienteCotizacion),
      fechaHoraEvento: this.form.value.fechaHoraEvento,
      codTipoEvento: this.form.value.codTipoEvento,
      detalleCotizacion: this.detalles.value.map((d: any) => ({
        idItem: Number(d.idItem),
        cantidadItemCotizacion: d.cantidadItemCotizacion
      })),
      detalleServicioCotizacion: this.detallesServicios.value.map((d: any) => ({
        idServicio: Number(d.idServicio),
        cantidad: d.cantidad,
        precioCotizado: d.precioCotizado,
        especificaciones: d.especificaciones || undefined
      }))
    };

    this.loading.show();
    this.cotizacionService.actualizarCotizacion(id, payload).subscribe({
      next: (response) => {
        this.loading.hide();
        this.toast.success('Cotización actualizada', 'El documento se regeneró con los cambios');
        this.cancelar();
        this.cargarCotizaciones();

        if (response.body) {
          this.documentoBlob = response.body;
          this.documentoNombre = this.extraerNombreArchivo(response.headers.get('Content-Disposition'))
            ?? `cotizacion-${new Date().toISOString().slice(0, 10)}.pdf`;
          this.documentoTitulo = 'Cotización actualizada';
          this.mostrarVisorDocumento = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        this.loading.hide();
        this.manejarErrorBlob(err, 'No se pudo actualizar la cotización');
      }
    });
  }

  /** Expuesto al template para mostrar el botón solo cuando corresponde. */
  esEditable(cotizacion: Cotizacion): boolean {
    return cotizacionEsEditable(cotizacion.estadoCotizacion);
  }

  /**
   * El documento solo se puede ver mientras la cotización siga vigente:
   * pendiente de confirmar o confirmada (más el legado "creada"). En cancelada
   * no se muestra.
   */
  puedeVerDocumento(cotizacion: Cotizacion): boolean {
    const e = cotizacion.estadoCotizacion;
    return e === 'C' || e === 'P' || e === 'CONF';
  }

  // ─── Ver documento de una cotización existente ────────────
  verDocumento(cotizacion: Cotizacion): void {
    this.loading.show();
    this.cotizacionService.obtenerDocumento(cotizacion.idCotizacion).subscribe({
      next: (response) => {
        this.loading.hide();
        if (!response.body) {
          this.toast.error('Error', 'El documento no está disponible');
          return;
        }
        this.documentoBlob = response.body;
        this.documentoNombre = this.extraerNombreArchivo(response.headers.get('Content-Disposition'))
          ?? `${cotizacion.codigoCotizacion || 'cotizacion'}.pdf`;
        this.documentoTitulo = `Cotización ${cotizacion.codigoCotizacion}`;
        this.mostrarVisorDocumento = true;
        // Sin esto el modal no aparecia hasta el siguiente click: esta era la
        // unica accion de la pantalla que no disparaba un segundo ciclo de
        // deteccion de cambios despues de la respuesta (a diferencia de
        // guardar/editar, que de rebote lo hacen via cargarCotizaciones()).
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading.hide();
        this.manejarErrorBlob(err, 'No se pudo obtener el documento');
      }
    });
  }

  private extraerNombreArchivo(contentDisposition: string | null): string | null {
    if (!contentDisposition) return null;
    const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
    return match ? decodeURIComponent(match[1]) : null;
  }

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

  // ─── Trazabilidad ────────────────────────────────────────
  verTrazabilidad(cotizacion: Cotizacion): void {
    this.mostrarTrazabilidad = true;
    this.cargandoTraza = true;
    this.trazaHistorial = [];
    this.trazaTitulo = cotizacion.codigoCotizacion || `Cotización #${cotizacion.idCotizacion}`;
    this.trazaSubtitulo = cotizacion.nombreClienteCotizacion ?? '';

    this.cotizacionService.historialEstados(cotizacion.idCotizacion).subscribe({
      next: (data) => {
        this.trazaHistorial = data;
        this.cargandoTraza = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargandoTraza = false;
        this.mostrarTrazabilidad = false;
        this.toast.error('Error', err?.error?.message ?? 'No se pudo cargar la trazabilidad');
        this.cdr.detectChanges();
      }
    });
  }

  cerrarTrazabilidad(): void {
    this.mostrarTrazabilidad = false;
    this.trazaHistorial = [];
  }

  cancelar(): void {
    this.form.reset();
    this.detalles.clear();
    this.detalles.push(this.nuevoDetalle());
    this.detallesServicios.clear();
    this.resetCalendario();
    this.mostrarForm = false;
    this.cotizacionEditando = null;
  }

  // ─── Confirmar cotización ────────────────────────────────
  abrirConfirmar(cotizacion: Cotizacion): void {
    this.cotizacionAConfirmar = cotizacion;
    this.formConfirmar.reset({ cantidadViajesAproximados: 2 });
    this.mostrarConfirmar = true;
  }

  cerrarConfirmar(): void {
    this.mostrarConfirmar = false;
    this.cotizacionAConfirmar = null;
  }

  campoInvalidoConfirmar(campo: string): boolean {
    const c = this.formConfirmar.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  guardarConfirmacion(): void {
    if (this.formConfirmar.invalid || !this.cotizacionAConfirmar) { this.formConfirmar.markAllAsTouched(); return; }

    const payload: ConfirmarCotizacion = {
      fechaEntregaEstimada: this.formConfirmar.value.fechaEntregaEstimada,
      cantidadViajesAproximados: Number(this.formConfirmar.value.cantidadViajesAproximados)
    };

    this.cotizacionService.confirmarCotizacion(this.cotizacionAConfirmar.idCotizacion, payload).subscribe({
      next: () => {
        this.toast.success('Cotización confirmada');
        this.cerrarConfirmar();
        this.cargarCotizaciones();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo confirmar')
    });
  }

  // ─── Cancelar cotización ─────────────────────────────────
  cancelarCotizacion(cotizacion: Cotizacion): void {
    this.toast.confirm({
      title: '¿Cancelar cotización?',
      message: `Cotización ${cotizacion.codigoCotizacion} — ${cotizacion.nombreClienteCotizacion}`,
      confirmText: 'Sí, cancelar',
      cancelText: 'Volver',
      onConfirm: () => {
        this.cotizacionService.cancelarCotizacion(cotizacion.idCotizacion).subscribe({
          next: () => { this.toast.success('Cotización cancelada'); this.cargarCotizaciones(); },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo cancelar')
        });
      }
    });
  }

  // ─── Eliminar ────────────────────────────────────────────
  eliminar(cotizacion: Cotizacion): void {
    this.toast.confirm({
      title: '¿Eliminar cotización?',
      message: `¿Estás seguro de eliminar la cotización ${cotizacion.codigoCotizacion}?`,
      confirmText: 'Sí, eliminar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.cotizacionService.eliminarCotizacion(cotizacion.idCotizacion).subscribe({
          next: () => { this.toast.success('Cotización eliminada'); this.cargarCotizaciones(); },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo eliminar')
        });
      }
    });
  }

  campoInvalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  detalleInvalido(index: number, campo: string): boolean {
    const c = this.detalles.at(index).get(campo);
    return !!(c?.invalid && c?.touched);
  }

  detalleServicioInvalido(index: number, campo: string): boolean {
    const c = this.detallesServicios.at(index).get(campo);
    return !!(c?.invalid && c?.touched);
  }
}
