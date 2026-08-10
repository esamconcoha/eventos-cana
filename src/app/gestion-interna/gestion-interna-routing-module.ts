import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: 'home',                          component: HomeComponent,          title: 'Inicio' },
      { path: 'administracion/usuarios',       component: UsuariosComponent,      title: 'Usuarios' },
      { path: 'administracion/inventario',     component: InventarioComponent,    title: 'Inventario' },
      { path: 'eventos/cotizaciones',          component: CotizacionesComponent,  title: 'Cotizaciones y Entregas' },
      { path: 'eventos/pedidos',               component: PedidosComponent,       title: 'Pedidos' },
      { path: 'eventos/entregas',              component: EntregasComponent,      title: 'Entregas' },
      { path: 'eventos/entregas/:idEntrega',   component: EntregaDetalleComponent, title: 'Detalle de entrega' },
      { path: 'eventos/recolecciones',         component: RecoleccionesComponent, title: 'Recolecciones' },
      { path: 'eventos/recolecciones/:idRecoleccion', component: RecoleccionDetalleComponent, title: 'Detalle de recoleccion' },
      { path: 'eventos/calendario',            component: CalendarioComponent,    title: 'Calendario de Eventos' },
      { path: 'administracion/servicios',        component: ServiciosComponent,     title: 'Servicios' },
      { path: 'mantenimiento/salones',         component: SalonesComponent,       title: 'Salones' },
      { path: 'reportes',                      component: ReportesComponent,      title: 'Reportes' },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GestionInternaRoutingModule {}
