import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { GestionInternaRoutingModule } from './gestion-interna-routing-module';
import { SidemenuComponent } from '../shared/sidemenu/sidemenu.component';
import { ToastComponent } from '../shared/toast/toast.component';
import { DocumentViewerComponent } from '../shared/document-viewer/document-viewer.component';
import { DatePickerComponent } from '../shared/date-picker/date-picker.component';
import { TrazabilidadModalComponent } from '../shared/trazabilidad/trazabilidad-modal.component';

import { GestionInterna } from './gestion-interna';
import { LayoutComponent } from './layout/layout.component';
import { HomeComponent } from './home/home.component';
import { UsuariosComponent } from './administracion/usuarios/usuarios.component';
import { InventarioComponent } from './administracion/inventario/inventario.component';
import { CotizacionesComponent } from './eventos/cotizaciones/cotizaciones.component';
import { PedidosComponent } from './eventos/pedidos/pedidos.component';
import { EntregasComponent } from './eventos/entregas/entregas.component';
import { EntregaDetalleComponent } from './eventos/entregas/entrega-detalle/entrega-detalle.component';
import { RecoleccionesComponent } from './eventos/recolecciones/recolecciones.component';
import { RecoleccionDetalleComponent } from './eventos/recolecciones/recoleccion-detalle/recoleccion-detalle.component';
import { CalendarioComponent } from './eventos/calendario/calendario.component';
import { ServiciosComponent } from './administracion/servicios/servicios.component';
import { SalonesComponent } from './mantenimiento/salones/salones.component';
import { ReportesComponent } from './reportes/reportes.component';
import { GraficoBarrasComponent } from '../shared/graficos/grafico-barras.component';
import { GraficoDonaComponent } from '../shared/graficos/grafico-dona.component';
import { GraficoRankingComponent } from '../shared/graficos/grafico-ranking.component';

@NgModule({
  declarations: [
    GestionInterna,
    LayoutComponent,
    HomeComponent,
    UsuariosComponent,
    InventarioComponent,
    CotizacionesComponent,
    PedidosComponent,
    EntregasComponent,
    EntregaDetalleComponent,
    RecoleccionesComponent,
    RecoleccionDetalleComponent,
    CalendarioComponent,
    ServiciosComponent,
    SalonesComponent,
    ReportesComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    GestionInternaRoutingModule,
    SidemenuComponent,
    ToastComponent,
    DocumentViewerComponent,
    DatePickerComponent,
    TrazabilidadModalComponent,
    GraficoBarrasComponent,
    GraficoDonaComponent,
    GraficoRankingComponent
  ]
})
export class GestionInternaModule {}
