import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { PedidoService } from '../../../services/pedido.service';
import { PagoService } from '../../../services/pago.service';
import { ItemService } from '../../../services/item.service';
import { ServicioService } from '../../../services/servicio.service';
import { CatalogoService } from '../../../services/catalogo.service';
import { TokenService } from '../../../services/token.service';
import { SalonService } from '../../../services/salon.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Pedido, EstadoPagoPedido, DetalleServicioPedido } from '../../../interfaces/pedido';
import { PagoPedido, EstadoCuenta } from '../../../interfaces/pago';
import { ItemCana } from '../../../interfaces/item-cana';
import { ServicioDecoracion } from '../../../interfaces/servicio';
import { Catalogo } from '../../../interfaces/catalogo';
import { Salon } from '../../../interfaces/salon';
import { EstadoLogisticoDef, ESTADOS_LOGISTICOS, ESTADO_CANCELADO_DEF, esCodigoCancelado } from '../../../interfaces/estado-logistico';
import { TrazaEvento } from '../../../interfaces/trazabilidad';


@Component({
  selector: 'app-pedidos',
  standalone: false,
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.css'
})
export class PedidosComponent implements OnInit {

  // ─── Flujo logístico (cana.estados, tipo_estado = 'EVE'; orden por id_estado) ──
  // Catálogo compartido con el módulo de Entregas: una sola definición de
  // etiquetas y colores, para que el mismo pedido no se muestre distinto según
  // la pantalla. Ver interfaces/estado-logistico.ts.
  estadosLogisticos: EstadoLogisticoDef[] = ESTADOS_LOGISTICOS;

  estadosPago: { codigo: EstadoPagoPedido; label: string; badgeClass: string }[] = [
    { codigo: 'PENDIENTE', label: 'Pendiente', badgeClass: 'bg-red-50 text-red-600 ring-red-200' },
    { codigo: 'ANTICIPO',  label: 'Anticipo',  badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
    { codigo: 'PARCIAL',   label: 'Parcial',   badgeClass: 'bg-amber-50 text-amber-700 ring-amber-200' },
    { codigo: 'PAGADO',    label: 'Pagado',    badgeClass: 'bg-emerald-50 text-emerald-700 ring-emerald-200' },
    { codigo: 'DEVUELTO',  label: 'Devuelto',  badgeClass: 'bg-purple-50 text-purple-700 ring-purple-200' }
  ];

  pedidos: Pedido[] = [];
  pedidosFiltrados: Pedido[] = [];
  items: ItemCana[] = [];
  servicios: ServicioDecoracion[] = [];
  tiposEvento: Catalogo[] = [];
  tiposPago: Catalogo[] = [];
  modalidadesPago: Catalogo[] = [];
  salones: Salon[] = [];
  cargando = false;
  mostrarForm = false;

  // ─── Tabs + filtros ─────────────────────────────────────
  tabActiva: 'todos' | 'no-pagados' = 'todos';
  estadosLogisticosSeleccionados: number[] = this.estadosLogisticos.map(e => e.idEstado);
  textoClienteFiltro = '';

  // ─── Filtro por fecha del evento ─────────────────────────
  // Vista por defecto: los eventos de ESTA semana (se fija en ngOnInit). Botones
  // rápidos "Esta semana" / "Este mes" / "Todos", más un rango manual desde/hasta.
  rangoPreset: 'semana' | 'mes' | 'todos' | 'rango' = 'semana';
  fechaDesde: string | null = null;   // 'yyyy-mm-dd' (fecha local, inclusive)
  fechaHasta: string | null = null;   // 'yyyy-mm-dd' (fecha local, inclusive)
  readonly presetsFecha: { id: 'semana' | 'mes' | 'todos'; label: string }[] = [
    { id: 'semana', label: 'Esta semana' },
    { id: 'mes',    label: 'Este mes' },
    { id: 'todos',  label: 'Todos' }
  ];

  get pedidosNoPagados(): Pedido[] {
    return this.pedidos.filter(p => p.estadoPago !== 'PAGADO');
  }

  form: FormGroup;

  // ─── Selector de fecha/hora del evento ──────────────────
  calendarioAbierto = false;
  mesVisible = new Date();
  fechaEventoSeleccionada: Date | null = null;
  horaEvento = '';
  horaPickerAbierto = false;
  horasDisponibles = Array.from({ length: 24 }, (_, i) => i);
  minutosDisponibles = Array.from({ length: 12 }, (_, i) => i * 5);

  // ─── Drawer de detalle ───────────────────────────────────
  mostrarDetalle = false;
  pedidoSeleccionado: Pedido | null = null;
  cargandoDetalle = false;
  estadoCuenta: EstadoCuenta | null = null;
  pagos: PagoPedido[] = [];
  cargandoPagos = false;
  mostrarFormPago = false;
  formPago: FormGroup;

  // ─── Trazabilidad (ventana emergente) ────────────────────
  mostrarTrazabilidad = false;
  cargandoTraza = false;
  trazaHistorial: TrazaEvento[] = [];
  trazaTitulo = '';
  trazaSubtitulo = '';

  // ─── Drawer de edición rápida ────────────────────────────
  mostrarEditar = false;
  cargandoEditar = false;
  pedidoEditando: Pedido | null = null;
  formEditar: FormGroup;

  // Selector de fecha/hora del evento, copia independiente para no chocar
  // con el calendario del formulario de "Nuevo pedido".
  calendarioEditarAbierto = false;
  mesVisibleEditar = new Date();
  fechaEventoSeleccionadaEditar: Date | null = null;
  horaEventoEditar = '';
  horaPickerEditarAbierto = false;

  constructor(
    private fb: FormBuilder,
    private pedidoService: PedidoService,
    private pagoService: PagoService,
    private itemService: ItemService,
    private servicioService: ServicioService,
    private catalogoService: CatalogoService,
    private tokenService: TokenService,
    private salonService: SalonService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      nombreClientePedido:   ['', [Validators.required, Validators.maxLength(100)]],
      telefonoClientePedido: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      dpiUsuarioPedido:      ['', [Validators.required, Validators.maxLength(16)]],
      direccionPedido:  ['', [Validators.required, Validators.maxLength(200)]],
      salonEntrega:     [null],
      fechaEventoHora:  ['', Validators.required],
      fechaEntrega:     ['', Validators.required],
      cantidadViajesAproximados: [2, [Validators.required, Validators.min(1)]],
      fechaRecoleccion: [''],
      codTipoEvento:    [null, Validators.required],
      detalles: this.fb.array([this.nuevoDetalle()]),
      detallesServicios: this.fb.array([])
    });

    this.formPago = this.fb.group({
      montoPago:        [null, [Validators.required, Validators.min(0.01)]],
      tipoPago:         [null, Validators.required],
      metodoPago:       [null, Validators.required],
      referenciaPago:   ['']
    });

    this.formEditar = this.fb.group({
      direccionPedido: ['', [Validators.required, Validators.maxLength(200)]],
      salonEntrega:    [null],
      fechaEntrega:    [''],
      fechaRecoleccion: [''],
      fechaEventoHora: ['', Validators.required],
      detalles: this.fb.array([]),
      detallesServicios: this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.fijarRango('semana');   // vista por defecto: eventos de esta semana
    this.cargarItems();
    this.cargarServicios();
    this.cargarTiposEvento();
    this.cargarTiposPago();
    this.cargarModalidadesPago();
    this.cargarSalones();
    this.cargarPedidos();
  }

  // ─── Catálogos y datos auxiliares ───────────────────────
  cargarItems(): void {
    this.itemService.listarItems().subscribe({
      next: (data) => { this.items = data.filter(i => i.estadoItem); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarServicios(): void {
    this.servicioService.listarServicios().subscribe({
      next: (data) => { this.servicios = data.filter(s => s.estadoServicio); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarTiposEvento(): void {
    this.catalogoService.getCatalogoByNombre('TIPO_EVENTOS').subscribe({
      next: (data) => { this.tiposEvento = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarTiposPago(): void {
    this.catalogoService.getCatalogoByNombre('TIPO_PAGO').subscribe({
      next: (data) => { this.tiposPago = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarModalidadesPago(): void {
    this.catalogoService.getCatalogoByNombre('MODALIDADES_PAGO').subscribe({
      next: (data) => { this.modalidadesPago = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarSalones(): void {
    this.salonService.listarSalones().subscribe({
      next: (data) => { this.salones = data.filter(s => s.estadoSalon); this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  nombreSalon(idSalon: number | null | undefined): string {
    if (!idSalon) return '—';
    return this.salones.find(s => s.idSalon === idSalon)?.nombreSalon ?? `Salón #${idSalon}`;
  }

  /**
   * Confirma o revierte que un servicio del pedido ya se realizó.
   * El backend devuelve el pedido completo, así que el drawer se refresca solo.
   */
  alternarServicioRealizado(servicio: DetalleServicioPedido): void {
    if (!servicio.idDetalleServPedido) { return; }
    const realizado = !servicio.fechaRealizado;
    const nombre = servicio.nombreServicio || `Servicio #${servicio.idServicio}`;

    // Se pregunta en ambos sentidos: confirmar deja constancia con fecha, y
    // deshacer la borra. Ninguna de las dos deberia pasar por un clic al pasar.
    this.toast.confirm({
      title: realizado ? '¿Confirmar que ya se realizó?' : '¿Marcar como pendiente?',
      message: realizado
        ? `Se registrará "${nombre}" como realizado con la fecha y hora de ahora.`
        : `Se borrará la fecha en que se confirmó "${nombre}".`,
      confirmText: realizado ? 'Sí, ya se realizó' : 'Sí, marcar pendiente',
      cancelText: 'Cancelar',
      onConfirm: () => this.guardarServicioRealizado(servicio.idDetalleServPedido!, realizado)
    });
  }

  private guardarServicioRealizado(idDetalleServPedido: number, realizado: boolean): void {
    this.pedidoService.marcarServicioRealizado(idDetalleServPedido, realizado).subscribe({
      next: (actualizado) => {
        this.pedidoSeleccionado = actualizado;
        const i = this.pedidos.findIndex(p => p.correlativoPedido === actualizado.correlativoPedido);
        if (i !== -1) { this.pedidos[i] = actualizado; this.aplicarFiltros(); }
        this.toast.success(realizado ? 'Servicio confirmado' : 'Servicio marcado pendiente');
        this.cdr.detectChanges();
      },
      error: () => this.toast.error('Error', 'No se pudo actualizar el servicio')
    });
  }

  // ─── Etiquetas / helpers de estado ──────────────────────
  estadoLogisticoDef(idEstado: number | undefined): EstadoLogisticoDef | undefined {
    return this.estadosLogisticos.find(e => e.idEstado === idEstado);
  }

  estadoLogisticoLabel(idEstado: number | undefined): string {
    return this.estadoLogisticoDef(idEstado)?.label ?? (idEstado != null ? String(idEstado) : '—');
  }

  // ─── Estado cancelado (ECA) ──────────────────────────────
  // Terminal y fuera del catálogo lineal: se detecta por código y se muestra con
  // su propio color/etiqueta, no por el id (que no está en ESTADOS_LOGISTICOS).
  esPedidoCancelado(pedido: Pedido | null | undefined): boolean {
    return !!pedido && esCodigoCancelado(pedido.estadoActual?.codigoEstado);
  }

  dotEstadoPedido(pedido: Pedido): string {
    return this.esPedidoCancelado(pedido)
      ? ESTADO_CANCELADO_DEF.dot
      : (this.estadoLogisticoDef(pedido.estadoActual?.idEstado)?.dot ?? 'bg-gray-300');
  }

  /**
   * Parte-fecha "yyyy-mm-dd" de un valor de fecha/hora, para pasarla como límite
   * a un app-date-picker. Devuelve null si no hay valor, así el picker queda sin
   * tope. Encadena las fechas del formulario: la entrega no puede pasar del
   * evento y la recolección no puede ser antes de la entrega.
   */
  fechaSolo(valor: string | null | undefined): string | null {
    return valor ? valor.slice(0, 10) : null;
  }

  labelEstadoPedido(pedido: Pedido): string {
    if (this.esPedidoCancelado(pedido)) { return ESTADO_CANCELADO_DEF.label; }
    return this.estadoLogisticoDef(pedido.estadoActual?.idEstado)?.label
      ?? pedido.estadoActual?.nombreEstado
      ?? '—';
  }

  estadoPagoDef(codigo: string | undefined) {
    return this.estadosPago.find(e => e.codigo === codigo);
  }

  siguienteEstadoLogistico(idEstadoActual: number | undefined): EstadoLogisticoDef | null {
    // El "avanzar" sigue solo el flujo lineal; el estado cancelado (ECA) es
    // terminal y no cuenta como paso siguiente aunque esté en el catálogo.
    const flujo = this.estadosLogisticos.filter(e => !esCodigoCancelado(e.codigoEstado));
    const idx = flujo.findIndex(e => e.idEstado === idEstadoActual);
    if (idx === -1 || idx === flujo.length - 1) return null;
    return flujo[idx + 1];
  }

  // ─── Tabs + filtros de listado ───────────────────────────
  cambiarTab(tab: 'todos' | 'no-pagados'): void {
    this.tabActiva = tab;
    this.aplicarFiltros();
  }

  toggleEstadoLogisticoFiltro(idEstado: number): void {
    this.estadosLogisticosSeleccionados = this.estadosLogisticosSeleccionados.includes(idEstado)
      ? this.estadosLogisticosSeleccionados.filter(e => e !== idEstado)
      : [...this.estadosLogisticosSeleccionados, idEstado];
    this.aplicarFiltros();
  }

  aplicarFiltros(): void {
    const texto = this.textoClienteFiltro.toLowerCase().trim();
    const base = this.tabActiva === 'no-pagados' ? this.pedidosNoPagados : this.pedidos;
    this.pedidosFiltrados = base.filter(p =>
      // ECA ya está en el catálogo (id 20), así que se filtra por su checkbox
      // como cualquier otro estado.
      this.estadosLogisticosSeleccionados.includes(p.estadoActual?.idEstado as number) &&
      this.enRangoFecha(p) &&
      (!texto || (p.nombreClientePedido ?? '').toLowerCase().includes(texto))
    );
  }

  // ─── Rango de fechas (por fecha del evento) ──────────────
  /** true si la fecha del evento del pedido cae dentro del rango seleccionado. */
  private enRangoFecha(pedido: Pedido): boolean {
    if (!this.fechaDesde && !this.fechaHasta) { return true; }
    const fecha = (pedido.fechaEvento ?? '').slice(0, 10);   // 'yyyy-mm-dd'
    if (!fecha) { return false; }
    if (this.fechaDesde && fecha < this.fechaDesde) { return false; }
    if (this.fechaHasta && fecha > this.fechaHasta) { return false; }
    return true;
  }

  private fmtLocal(d: Date): string {
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
  }

  /** Fija las fechas del preset SIN re-filtrar (sirve para el arranque). */
  private fijarRango(preset: 'semana' | 'mes' | 'todos'): void {
    const hoy = new Date();
    if (preset === 'semana') {
      const inicio = new Date(hoy);
      inicio.setDate(hoy.getDate() - hoy.getDay());          // domingo (igual que el calendario)
      const fin = new Date(inicio);
      fin.setDate(inicio.getDate() + 6);                     // sábado
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
  }

  /** Botón de preset: fija el rango y re-filtra. */
  seleccionarPreset(preset: 'semana' | 'mes' | 'todos'): void {
    this.fijarRango(preset);
    this.aplicarFiltros();
  }

  /** Al editar las fechas a mano, el preset pasa a "rango" personalizado. */
  onRangoManualChange(): void {
    this.rangoPreset = 'rango';
    this.aplicarFiltros();
  }

  // ─── Listado principal ────────────────────────────────────
  cargarPedidos(): void {
    this.cargando = true;
    this.pedidoService.listarPedidos().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.aplicarFiltros();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  // Refresca en la fila el saldo/estado de pago sin abrir el detalle
  refrescarEstadoPago(pedido: Pedido): void {
    this.pagoService.obtenerEstadoCuenta(pedido.correlativoPedido).subscribe({
      next: (ec) => {
        pedido.estadoPago = ec.estadoPago;
        pedido.saldoPendiente = ec.saldoPendiente;
        pedido.montoTotalPedido = ec.montoTotalPedido;
        pedido.pagado = ec.pagado;
        this.aplicarFiltros();
        this.toast.success('Actualizado', `${pedido.correlativoPedido}: saldo Q ${ec.saldoPendiente.toFixed(2)}`);
        this.cdr.detectChanges();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo actualizar el estado de pago')
    });
  }

  // ─── Detalle de items (FormArray) ────────────────────────
  get detalles(): FormArray {
    return this.form.get('detalles') as FormArray;
  }

  nuevoDetalle(): FormGroup {
    return this.fb.group({
      idItem: [null, Validators.required],
      cantidadItemPedido: [1, [Validators.required, Validators.min(1)]]
    });
  }

  agregarDetalle(): void {
    this.detalles.push(this.nuevoDetalle());
  }

  eliminarDetalle(i: number): void {
    if (this.detalles.length > 1) {
      this.detalles.removeAt(i);
    } else {
      this.toast.info('Aviso', 'Debe haber al menos un item en el pedido');
    }
  }

  getItem(idItem: number): ItemCana | undefined {
    return this.items.find(i => i.idItem === Number(idItem));
  }

  subtotalDetalle(index: number): number {
    const row = this.detalles.at(index).value;
    const item = this.getItem(row.idItem);
    return item ? (item.costoItem * (row.cantidadItemPedido || 0)) : 0;
  }

  // ─── Detalle de servicios (FormArray) ─────────────────────
  get detallesServicios(): FormArray {
    return this.form.get('detallesServicios') as FormArray;
  }

  nuevoDetalleServicio(): FormGroup {
    return this.fb.group({
      idServicio:       [null, Validators.required],
      cantidad:         [1,   [Validators.required, Validators.min(1)]],
      precioAcordado:   [null, [Validators.required, Validators.min(0)]],
      especificaciones: ['']
    });
  }

  agregarDetalleServicio(): void {
    this.detallesServicios.push(this.nuevoDetalleServicio());
  }

  eliminarDetalleServicio(i: number): void {
    this.detallesServicios.removeAt(i);
  }

  getServicio(idServicio: number): ServicioDecoracion | undefined {
    return this.servicios.find(s => s.idServicio === Number(idServicio));
  }

  subtotalDetalleServicio(index: number): number {
    const row = this.detallesServicios.at(index).value;
    return (row.precioAcordado || 0) * (row.cantidad || 0);
  }

  get totalPedido(): number {
    const totalItems = this.detalles.controls.reduce((acc, _, i) => acc + this.subtotalDetalle(i), 0);
    const totalServicios = this.detallesServicios.controls.reduce((acc, _, i) => acc + this.subtotalDetalleServicio(i), 0);
    return totalItems + totalServicios;
  }

  montoTotal(pedido: Pedido): number {
    if (pedido.montoTotalPedido != null) return pedido.montoTotalPedido;
    const totalItems = (pedido.detalles ?? []).reduce((acc, d) => acc + (d.costoItem ?? 0) * d.cantidadItemPedido, 0);
    const totalServicios = (pedido.detallesServicios ?? []).reduce((acc, d) => acc + d.precioAcordado * d.cantidad, 0);
    return totalItems + totalServicios;
  }

  // ─── Detalle de items/servicios del drawer de edición (FormArray) ──
  get detallesEditar(): FormArray {
    return this.formEditar.get('detalles') as FormArray;
  }

  nuevoDetalleEditar(idItem: number | null = null, cantidad = 1): FormGroup {
    return this.fb.group({
      idItem: [idItem, Validators.required],
      cantidadItemPedido: [cantidad, [Validators.required, Validators.min(1)]]
    });
  }

  agregarDetalleEditar(): void {
    this.detallesEditar.push(this.nuevoDetalleEditar());
  }

  eliminarDetalleEditar(i: number): void {
    if (this.detallesEditar.length > 1) {
      this.detallesEditar.removeAt(i);
    } else {
      this.toast.info('Aviso', 'Debe haber al menos un item en el pedido');
    }
  }

  subtotalDetalleEditar(index: number): number {
    const row = this.detallesEditar.at(index).value;
    const item = this.getItem(row.idItem);
    return item ? (item.costoItem * (row.cantidadItemPedido || 0)) : 0;
  }

  get detallesServiciosEditar(): FormArray {
    return this.formEditar.get('detallesServicios') as FormArray;
  }

  nuevoDetalleServicioEditar(idServicio: number | null = null, cantidad = 1, precioAcordado: number | null = null, especificaciones = ''): FormGroup {
    return this.fb.group({
      idServicio:       [idServicio, Validators.required],
      cantidad:         [cantidad,   [Validators.required, Validators.min(1)]],
      precioAcordado:   [precioAcordado, [Validators.required, Validators.min(0)]],
      especificaciones: [especificaciones]
    });
  }

  agregarDetalleServicioEditar(): void {
    this.detallesServiciosEditar.push(this.nuevoDetalleServicioEditar());
  }

  eliminarDetalleServicioEditar(i: number): void {
    this.detallesServiciosEditar.removeAt(i);
  }

  subtotalDetalleServicioEditar(index: number): number {
    const row = this.detallesServiciosEditar.at(index).value;
    return (row.precioAcordado || 0) * (row.cantidad || 0);
  }

  get totalPedidoEditar(): number {
    const totalItems = this.detallesEditar.controls.reduce((acc, _, i) => acc + this.subtotalDetalleEditar(i), 0);
    const totalServicios = this.detallesServiciosEditar.controls.reduce((acc, _, i) => acc + this.subtotalDetalleServicioEditar(i), 0);
    return totalItems + totalServicios;
  }

  detalleInvalidoEditar(index: number, campo: string): boolean {
    const c = this.detallesEditar.at(index).get(campo);
    return !!(c?.invalid && c?.touched);
  }

  detalleServicioInvalidoEditar(index: number, campo: string): boolean {
    const c = this.detallesServiciosEditar.at(index).get(campo);
    return !!(c?.invalid && c?.touched);
  }

  // ─── Selector de fecha/hora del evento (form "Nuevo pedido") ──
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
    this.form.patchValue({ fechaEventoHora: dt.toISOString() });
  }

  esHoy(dia: Date): boolean {
    return dia.toDateString() === new Date().toDateString();
  }

  esSeleccionado(dia: Date): boolean {
    return !!this.fechaEventoSeleccionada && dia.toDateString() === this.fechaEventoSeleccionada.toDateString();
  }

  get fechaEventoTexto(): string {
    if (!this.fechaEventoSeleccionada) return '';
    const fecha = this.fechaEventoSeleccionada.toLocaleDateString('es-GT', { day: 'numeric', month: 'long', year: 'numeric' });
    return this.horaEvento ? `${fecha}, ${this.horaEvento}` : fecha;
  }

  private resetCalendario(): void {
    this.calendarioAbierto = false;
    this.horaPickerAbierto = false;
    this.mesVisible = new Date();
    this.fechaEventoSeleccionada = null;
    this.horaEvento = '';
  }

  // ─── Selector de fecha/hora del evento (drawer de edición) ──
  abrirCalendarioEditar(): void {
    this.calendarioEditarAbierto = true;
  }

  cerrarCalendarioEditar(): void {
    this.calendarioEditarAbierto = false;
    this.horaPickerEditarAbierto = false;
  }

  get diasCalendarioEditar(): (Date | null)[] {
    const year = this.mesVisibleEditar.getFullYear();
    const month = this.mesVisibleEditar.getMonth();
    const primerDia = new Date(year, month, 1);
    const ultimoDia = new Date(year, month + 1, 0);
    const dias: (Date | null)[] = [];
    for (let i = 0; i < primerDia.getDay(); i++) dias.push(null);
    for (let d = 1; d <= ultimoDia.getDate(); d++) dias.push(new Date(year, month, d));
    return dias;
  }

  get nombreMesVisibleEditar(): string {
    const texto = this.mesVisibleEditar.toLocaleDateString('es-GT', { month: 'long', year: 'numeric' });
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  mesAnteriorEditar(): void {
    this.mesVisibleEditar = new Date(this.mesVisibleEditar.getFullYear(), this.mesVisibleEditar.getMonth() - 1, 1);
  }

  mesSiguienteEditar(): void {
    this.mesVisibleEditar = new Date(this.mesVisibleEditar.getFullYear(), this.mesVisibleEditar.getMonth() + 1, 1);
  }

  seleccionarDiaEditar(dia: Date): void {
    this.fechaEventoSeleccionadaEditar = dia;
    this.actualizarFechaHoraEventoEditar();
  }

  toggleHoraPickerEditar(event: Event): void {
    event.stopPropagation();
    this.horaPickerEditarAbierto = !this.horaPickerEditarAbierto;
  }

  get horaSeleccionadaNumEditar(): number | null {
    return this.horaEventoEditar ? Number(this.horaEventoEditar.split(':')[0]) : null;
  }

  get minutoSeleccionadoNumEditar(): number | null {
    return this.horaEventoEditar ? Number(this.horaEventoEditar.split(':')[1]) : null;
  }

  seleccionarHoraEditar(h: number): void {
    const m = this.minutoSeleccionadoNumEditar ?? 0;
    this.horaEventoEditar = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
    this.actualizarFechaHoraEventoEditar();
  }

  seleccionarMinutoEditar(m: number): void {
    const h = this.horaSeleccionadaNumEditar ?? 0;
    this.horaEventoEditar = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
    this.actualizarFechaHoraEventoEditar();
  }

  private actualizarFechaHoraEventoEditar(): void {
    if (!this.fechaEventoSeleccionadaEditar) return;
    const [h, m] = (this.horaEventoEditar || '00:00').split(':').map(Number);
    const dt = new Date(this.fechaEventoSeleccionadaEditar);
    dt.setHours(h || 0, m || 0, 0, 0);
    this.formEditar.patchValue({ fechaEventoHora: dt.toISOString() });
  }

  esSeleccionadoEditar(dia: Date): boolean {
    return !!this.fechaEventoSeleccionadaEditar && dia.toDateString() === this.fechaEventoSeleccionadaEditar.toDateString();
  }

  get fechaEventoTextoEditar(): string {
    if (!this.fechaEventoSeleccionadaEditar) return '';
    const fecha = this.fechaEventoSeleccionadaEditar.toLocaleDateString('es-GT', { day: 'numeric', month: 'long', year: 'numeric' });
    return this.horaEventoEditar ? `${fecha}, ${this.horaEventoEditar}` : fecha;
  }

  private resetCalendarioEditar(): void {
    this.calendarioEditarAbierto = false;
    this.horaPickerEditarAbierto = false;
    this.mesVisibleEditar = new Date();
    this.fechaEventoSeleccionadaEditar = null;
    this.horaEventoEditar = '';
  }

  // ─── Guardar nuevo pedido (directo) ───────────────────────
  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    const payload = {
      nombreClientePedido: this.form.value.nombreClientePedido,
      telefonoClientePedido: Number(this.form.value.telefonoClientePedido),
      dpiUsuarioPedido: this.form.value.dpiUsuarioPedido,
      usuarioInternoPedido: this.tokenService.getDpi(),
      direccionPedido: this.form.value.direccionPedido,
      fechaEvento: this.form.value.fechaEventoHora,
      salonEntrega: this.form.value.salonEntrega ? Number(this.form.value.salonEntrega) : undefined,
      fechaEntrega: this.form.value.fechaEntrega,
      cantidadViajesAproximados: Number(this.form.value.cantidadViajesAproximados),
      fechaRecoleccion: this.form.value.fechaRecoleccion || undefined,
      codTipoEvento: this.form.value.codTipoEvento,
      detalles: this.detalles.value.map((d: any) => ({
        idItem: Number(d.idItem),
        cantidadItemPedido: d.cantidadItemPedido
      })),
      detallesServicios: this.detallesServicios.value.map((d: any) => ({
        idServicio: Number(d.idServicio),
        cantidad: d.cantidad,
        precioAcordado: d.precioAcordado,
        especificaciones: d.especificaciones || undefined
      }))
    };

    this.pedidoService.guardarPedido(payload).subscribe({
      next: () => {
        this.toast.success('Pedido creado');
        this.cancelar();
        this.cargarPedidos();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo guardar el pedido')
    });
  }

  cancelar(): void {
    this.form.reset();
    this.detalles.clear();
    this.detalles.push(this.nuevoDetalle());
    this.detallesServicios.clear();
    this.resetCalendario();
    this.mostrarForm = false;
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

  // ══════════════════════════════════════════════════════════
  // DETALLE DEL PEDIDO (drawer: info + estado logístico + pagos)
  // ══════════════════════════════════════════════════════════
  verDetalle(pedido: Pedido): void {
    this.mostrarDetalle = true;
    this.pedidoSeleccionado = pedido;
    this.cargandoDetalle = true;
    this.pedidoService.obtenerPedido(pedido.correlativoPedido).subscribe({
      next: (data) => {
        this.pedidoSeleccionado = data;
        this.cargandoDetalle = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargandoDetalle = false; this.cdr.detectChanges(); }
    });
    this.cargarPagos(pedido.correlativoPedido);
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.pedidoSeleccionado = null;
    this.estadoCuenta = null;
    this.pagos = [];
    this.mostrarFormPago = false;
    this.formPago.reset();
  }

  cargarPagos(correlativoPedido: string): void {
    this.cargandoPagos = true;
    this.pagoService.obtenerEstadoCuenta(correlativoPedido).subscribe({
      next: (ec) => { this.estadoCuenta = ec; this.cdr.detectChanges(); },
      error: () => {}
    });
    this.pagoService.listarPagos(correlativoPedido).subscribe({
      next: (data) => { this.pagos = data; this.cargandoPagos = false; this.cdr.detectChanges(); },
      error: () => { this.cargandoPagos = false; this.cdr.detectChanges(); }
    });
  }

  avanzarEstado(pedido: Pedido): void {
    const siguiente = this.siguienteEstadoLogistico(pedido.estadoActual?.idEstado);
    if (!siguiente) return;
    this.toast.confirm({
      title: `¿Avanzar a "${siguiente.label}"?`,
      message: `Pedido ${pedido.correlativoPedido}`,
      confirmText: 'Sí, avanzar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.pedidoService.cambiarEstadoLogistico(pedido.correlativoPedido, siguiente.idEstado).subscribe({
          next: (actualizado) => {
            this.toast.success('Estado actualizado', siguiente.label);
            if (this.pedidoSeleccionado?.correlativoPedido === pedido.correlativoPedido) {
              this.pedidoSeleccionado = actualizado;
            }
            this.cargarPedidos();
          },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo actualizar el estado')
        });
      }
    });
  }

  // ─── Trazabilidad ────────────────────────────────────────
  // El historial se pide cada vez que se abre y no se cachea: el estado del
  // pedido lo pueden mover otros módulos (entregas, recolecciones), así que un
  // historial guardado se quedaría viejo sin avisar.
  verTrazabilidad(pedido: Pedido): void {
    this.mostrarTrazabilidad = true;
    this.cargandoTraza = true;
    this.trazaHistorial = [];
    this.trazaTitulo = pedido.correlativoPedido;
    this.trazaSubtitulo = pedido.nombreClientePedido ?? '';

    this.pedidoService.historialEstados(pedido.correlativoPedido).subscribe({
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

  // ─── Cancelar pedido (estado terminal ECA) ───────────────
  // Única vía para dar de baja un evento ya confirmado. En Cotizaciones aparece
  // luego la nota "Pedido cancelado" sobre la cotización de origen.
  cancelarPedido(pedido: Pedido): void {
    if (this.esPedidoCancelado(pedido)) { return; }
    this.toast.confirm({
      title: '¿Cancelar este pedido?',
      message: `El pedido ${pedido.correlativoPedido} pasará a "Evento cancelado".`,
      confirmText: 'Sí, cancelar pedido',
      cancelText: 'Volver',
      onConfirm: () => {
        this.pedidoService.cancelarPedido(pedido.correlativoPedido).subscribe({
          next: (actualizado) => {
            if (this.pedidoSeleccionado?.correlativoPedido === pedido.correlativoPedido) {
              this.pedidoSeleccionado = actualizado;
            }
            this.cargarPedidos();
            this.toast.success('Pedido cancelado', pedido.correlativoPedido);
          },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo cancelar el pedido')
        });
      }
    });
  }

  // ─── Dropdown de estado en la tabla (no solo el siguiente en la
  // secuencia, cualquier estado del catálogo). Se posiciona con
  // position:fixed y se renderiza fuera del contenedor con scroll de la
  // tabla, porque un dropdown absoluto ahí se recorta verticalmente.
  estadoDropdownPedido: Pedido | null = null;
  estadoDropdownPos: { top: number; left: number } | null = null;

  toggleEstadoDropdown(pedido: Pedido, event: MouseEvent): void {
    event.stopPropagation();
    if (this.estadoDropdownPedido?.correlativoPedido === pedido.correlativoPedido) {
      this.cerrarEstadoDropdown();
      return;
    }
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    this.estadoDropdownPos = this.posicionarDropdown(rect);
    this.estadoDropdownPedido = pedido;
  }

  /**
   * El menú es position:fixed para que no lo recorte el contenedor con scroll
   * de la tabla, pero eso implica que tampoco puede "scrollearse" hacia la
   * vista: si no cabe abajo hay que abrirlo hacia arriba. Sin esto, la última
   * fila abría el menú fuera de la pantalla y quedaba inalcanzable.
   */
  private posicionarDropdown(rect: DOMRect): { top: number; left: number } {
    const ALTO_MAXIMO = 288;   // max-h-72 del panel
    const MARGEN = 8;

    const espacioAbajo = window.innerHeight - rect.bottom;
    const espacioArriba = rect.top;
    const alto = Math.min(ALTO_MAXIMO, this.estadosLogisticos.length * 40 + 12);

    // Abre hacia arriba solo si no cabe abajo y arriba hay más lugar.
    const haciaArriba = espacioAbajo < alto + MARGEN && espacioArriba > espacioAbajo;
    const top = haciaArriba
        ? Math.max(MARGEN, rect.top - alto - 6)
        : Math.min(rect.bottom + 6, window.innerHeight - alto - MARGEN);

    // Que tampoco se salga por la derecha en pantallas angostas.
    const ANCHO = 224;   // w-56
    const left = Math.max(MARGEN, Math.min(rect.left, window.innerWidth - ANCHO - MARGEN));

    return { top, left };
  }

  cerrarEstadoDropdown(): void {
    this.estadoDropdownPedido = null;
    this.estadoDropdownPos = null;
  }

  seleccionarEstadoDropdown(nuevoEstado: EstadoLogisticoDef): void {
    const pedido = this.estadoDropdownPedido;
    this.cerrarEstadoDropdown();
    if (!pedido) return;
    // Elegir "Evento cancelado" en el combo equivale a cancelar el pedido: pasa
    // por la misma confirmación que el botón, no se aplica en un solo clic.
    if (esCodigoCancelado(nuevoEstado.codigoEstado)) {
      this.cancelarPedido(pedido);
      return;
    }
    this.cambiarEstadoDesdeTabla(pedido, nuevoEstado.idEstado);
  }

  private cambiarEstadoDesdeTabla(pedido: Pedido, nuevoIdEstado: number): void {
    if (pedido.estadoActual?.idEstado === nuevoIdEstado) return;

    this.pedidoService.cambiarEstadoLogistico(pedido.correlativoPedido, nuevoIdEstado).subscribe({
      next: (actualizado) => {
        this.toast.success('Estado actualizado', this.estadoLogisticoLabel(nuevoIdEstado));
        if (this.pedidoSeleccionado?.correlativoPedido === pedido.correlativoPedido) {
          this.pedidoSeleccionado = actualizado;
        }
        this.cargarPedidos();
      },
      error: (err) => {
        this.toast.error('Error', err?.error?.message ?? 'No se pudo actualizar el estado');
        this.cdr.detectChanges();
      }
    });
  }

  // ─── Registrar pago ───────────────────────────────────────
  campoInvalidoPago(campo: string): boolean {
    const c = this.formPago.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  registrarPago(): void {
    if (this.formPago.invalid || !this.pedidoSeleccionado) { this.formPago.markAllAsTouched(); return; }

    // Red de seguridad: el botón ya está oculto en cancelados, pero el estado
    // pudo cambiar con el drawer abierto.
    if (this.esPedidoCancelado(this.pedidoSeleccionado)) {
      this.toast.error('Pedido cancelado', 'No se pueden registrar pagos en un pedido cancelado');
      return;
    }

    const payload = {
      correlativoPedido: this.pedidoSeleccionado.correlativoPedido,
      montoPago: Number(this.formPago.value.montoPago),
      tipoPago: this.formPago.value.tipoPago,
      metodoPago: this.formPago.value.metodoPago,
      referenciaPago: this.formPago.value.referenciaPago || undefined,
      usuarioRegistro: this.tokenService.getDpi()
    };

    this.pagoService.registrarPago(payload).subscribe({
      next: () => {
        this.toast.success('Pago registrado');
        this.formPago.reset();
        this.mostrarFormPago = false;
        if (this.pedidoSeleccionado) this.cargarPagos(this.pedidoSeleccionado.correlativoPedido);
        this.cargarPedidos();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo registrar el pago')
    });
  }

  anularPago(pago: PagoPedido): void {
    this.toast.confirm({
      title: '¿Anular pago?',
      message: `Q ${pago.montoPago.toFixed(2)} — ${pago.referenciaPago ?? 'sin referencia'}`,
      confirmText: 'Sí, anular',
      cancelText: 'Volver',
      onConfirm: () => {
        this.pagoService.anularPago(pago.idPago).subscribe({
          next: () => {
            this.toast.success('Pago anulado');
            if (this.pedidoSeleccionado) this.cargarPagos(this.pedidoSeleccionado.correlativoPedido);
            this.cargarPedidos();
          },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo anular el pago')
        });
      }
    });
  }

  // ══════════════════════════════════════════════════════════
  // EDICIÓN (dirección / salón / fecha entrega / fecha evento / items / servicios)
  // ══════════════════════════════════════════════════════════
  abrirEditar(pedido: Pedido): void {
    this.pedidoEditando = pedido;
    this.mostrarEditar = true;
    this.cargandoEditar = true;
    this.formEditar.reset();
    this.detallesEditar.clear();
    this.detallesServiciosEditar.clear();
    this.resetCalendarioEditar();

    // Se pide el pedido completo porque el listado no trae detalles/servicios.
    this.pedidoService.obtenerPedido(pedido.correlativoPedido).subscribe({
      next: (data) => {
        this.pedidoEditando = data;

        const fe = new Date(data.fechaEvento);
        this.fechaEventoSeleccionadaEditar = fe;
        this.horaEventoEditar = `${String(fe.getHours()).padStart(2, '0')}:${String(fe.getMinutes()).padStart(2, '0')}`;

        this.formEditar.patchValue({
          direccionPedido: data.direccionPedido,
          salonEntrega: data.salonEntrega ?? null,
          fechaEntrega: data.fechaEntrega ? data.fechaEntrega.substring(0, 10) : '',
          fechaRecoleccion: data.fechaRecoleccion ? data.fechaRecoleccion.substring(0, 10) : '',
          fechaEventoHora: fe.toISOString()
        });

        const detalles = data.detalles ?? [];
        if (detalles.length > 0) {
          detalles.forEach(d => this.detallesEditar.push(this.nuevoDetalleEditar(d.idItem, d.cantidadItemPedido)));
        } else {
          this.detallesEditar.push(this.nuevoDetalleEditar());
        }

        (data.detallesServicios ?? []).forEach(d =>
          this.detallesServiciosEditar.push(
            this.nuevoDetalleServicioEditar(d.idServicio, d.cantidad, d.precioAcordado, d.especificaciones ?? '')
          )
        );

        this.cargandoEditar = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.cargandoEditar = false;
        this.toast.error('Error', err?.error?.message ?? 'No se pudo cargar el pedido');
        this.cdr.detectChanges();
      }
    });
  }

  cerrarEditar(): void {
    this.mostrarEditar = false;
    this.pedidoEditando = null;
    this.detallesEditar.clear();
    this.detallesServiciosEditar.clear();
    this.resetCalendarioEditar();
  }

  campoInvalidoEditar(campo: string): boolean {
    const c = this.formEditar.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  guardarEdicion(): void {
    if (this.formEditar.invalid || !this.pedidoEditando) { this.formEditar.markAllAsTouched(); return; }

    const payload = {
      direccionPedido: this.formEditar.value.direccionPedido,
      salonEntrega: this.formEditar.value.salonEntrega ? Number(this.formEditar.value.salonEntrega) : undefined,
      fechaEntrega: this.formEditar.value.fechaEntrega || undefined,
      fechaRecoleccion: this.formEditar.value.fechaRecoleccion || undefined,
      fechaEvento: this.formEditar.value.fechaEventoHora,
      detalles: this.detallesEditar.value.map((d: any) => ({
        idItem: Number(d.idItem),
        cantidadItemPedido: d.cantidadItemPedido
      })),
      detallesServicios: this.detallesServiciosEditar.value.map((d: any) => ({
        idServicio: Number(d.idServicio),
        cantidad: d.cantidad,
        precioAcordado: d.precioAcordado,
        especificaciones: d.especificaciones || undefined
      }))
    };

    this.pedidoService.actualizarPedido(this.pedidoEditando.correlativoPedido, payload).subscribe({
      next: () => {
        this.toast.success('Pedido actualizado');
        this.cerrarEditar();
        this.cargarPedidos();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo actualizar el pedido')
    });
  }
}
