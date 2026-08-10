import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ItemService } from '../../../services/item.service';
import { CatalogoService } from '../../../services/catalogo.service';
import { ItemCana, CrearItem } from '../../../interfaces/item-cana';
import { Catalogo } from '../../../interfaces/catalogo';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-inventario',
  standalone: false,
  templateUrl: './inventario.component.html',
  styleUrl: './inventario.component.css'
})
export class InventarioComponent implements OnInit {

  items: ItemCana[] = [];
  itemsFiltrados: ItemCana[] = [];
  tiposItem: Catalogo[] = [];
  cargando = false;
  mostrarForm = false;
  modoEdicion = false;
  itemEditando: ItemCana | null = null;
  itemDetalle: ItemCana | null = null;

  paginaActual = 1;
  itemsPorPagina = 10;
  textoBusqueda = '';

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private itemService: ItemService,
    private catalogoService: CatalogoService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      idTipoItem:       [null, Validators.required],
      descripcionItem:  ['', [Validators.required, Validators.maxLength(200)]],
      costoItem:        [null, [Validators.required, Validators.min(0)]],
      cantidadItem:     [null, [Validators.required, Validators.min(0)]],
      cantidadFaltante: [0,   [Validators.required, Validators.min(0)]],
      observaciones:    ['']
    });
  }

  ngOnInit(): void {
    this.cargarTiposItem();
    this.cargarItems();
  }

  cargarTiposItem(): void {
    this.catalogoService.getCatalogoByNombre('TIPO_ITEM').subscribe({
      next: (data) => { this.tiposItem = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  cargarItems(): void {
    this.cargando = true;
    this.itemService.listarItems().subscribe({
      next: (data) => {
        this.items = data;
        this.aplicarFiltro();
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  aplicarFiltro(): void {
    const texto = this.textoBusqueda.toLowerCase().trim();
    this.itemsFiltrados = texto
      ? this.items.filter(i =>
          i.descripcionItem.toLowerCase().includes(texto) ||
          i.nombreItem.toLowerCase().includes(texto) ||
          i.idItem.toString().includes(texto)
        )
      : [...this.items];
    this.paginaActual = 1;
  }

  get totalPaginas(): number {
    return Math.ceil(this.itemsFiltrados.length / this.itemsPorPagina);
  }

  get itemsPaginados(): ItemCana[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    return this.itemsFiltrados.slice(inicio, inicio + this.itemsPorPagina);
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i + 1);
  }

  cambiarPagina(p: number): void {
    if (p >= 1 && p <= this.totalPaginas) this.paginaActual = p;
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.itemEditando = null;
    this.form.reset({ cantidadFaltante: 0 });
    this.mostrarForm = true;
  }

  abrirEditar(item: ItemCana): void {
    this.modoEdicion = true;
    this.itemEditando = item;
    this.form.patchValue({
      idTipoItem:       item.idTipoItem,
      descripcionItem:  item.descripcionItem,
      costoItem:        item.costoItem,
      cantidadItem:     item.cantidadItem,
      cantidadFaltante: item.cantidadFaltante,
      observaciones:    item.observaciones ?? ''
    });
    this.mostrarForm = true;
  }

  guardar(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload: CrearItem = this.form.value;

    const request$ = this.modoEdicion && this.itemEditando
      ? this.itemService.editarItem(this.itemEditando.idItem, payload)
      : this.itemService.guardarItem(payload);

    request$.subscribe({
      next: () => {
        this.toast.success(this.modoEdicion ? 'Item actualizado' : 'Item creado');
        this.cancelar();
        this.cargarItems();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo guardar')
    });
  }

  cancelar(): void {
    this.form.reset({ cantidadFaltante: 0 });
    this.mostrarForm = false;
    this.modoEdicion = false;
    this.itemEditando = null;
  }

  confirmarEliminar(item: ItemCana): void {
    this.toast.confirm({
      title: '¿Eliminar item?',
      message: `¿Estás seguro de eliminar "${item.descripcionItem}"?`,
      confirmText: 'Sí, eliminar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.itemService.eliminarItem(item.idItem).subscribe({
          next: () => {
            this.toast.success('Item eliminado');
            this.cargarItems();
          },
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
