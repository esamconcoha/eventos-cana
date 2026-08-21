import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { SharedDataService } from '../../services/SharedDataService.service';
import { TokenService } from '../../services/token.service';
import { pantallasPorRol } from '../../security/permisos';

export interface MenuItem {
  path?: string;
  title: string;
  icon: string;
  children?: { path: string; title: string; icon: string }[];
  expanded?: boolean;
}

@Component({
  selector: 'app-sidemenu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidemenu.component.html',
  styleUrl: './sidemenu.component.css'
})
export class SidemenuComponent implements OnInit {

  @Input() isOpen = false;
  @Input() isCollapsed = false;
  @Input() isMobile = false;
  @Input() isSidebarOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() toggle = new EventEmitter<void>();

  menuItems: MenuItem[] = [];

  // ==============================================================
  // CONFIGURACIÓN DEL MENÚ
  // Agrega aquí nuevas secciones o subitems según el proyecto crezca
  // ==============================================================
  private configuracionMenu: MenuItem[] = [
    { path: 'home', title: 'Inicio', icon: 'home' },
    {
      title: 'Administración',
      icon: 'admin_panel_settings',
      expanded: false,
      children: [
        { path: 'administracion/usuarios',   title: 'Usuarios',   icon: 'people' },
        { path: 'administracion/inventario', title: 'Inventario', icon: 'inventory_2' },
        { path: 'administracion/servicios',  title: 'Servicios',  icon: 'miscellaneous_services' }
      ]
    },
    {
      title: 'Eventos',
      icon: 'celebration',
      expanded: false,
      children: [
        { path: 'eventos/cotizaciones', title: 'Cotizaciones', icon: 'request_quote' },
        { path: 'eventos/pedidos',      title: 'Pedidos',      icon: 'local_mall' },
        { path: 'eventos/entregas',     title: 'Entregas',     icon: 'local_shipping' },
        { path: 'eventos/recolecciones', title: 'Recolecciones', icon: 'assignment_return' },
        { path: 'eventos/calendario',   title: 'Calendario de Eventos',   icon: 'calendar_month' }
      ]
    },
    {
      title: 'Mantenimiento',
      icon: 'construction',
      expanded: false,
      children: [
        { path: 'mantenimiento/salones', title: 'Salones', icon: 'meeting_room' }
      ]
    },
    { path: 'reportes',      title: 'Reportes',      icon: 'bar_chart' }
  ];
  // ==============================================================

  constructor(
    private sharedData: SharedDataService,
    private router: Router,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    // El menú se arma según el rol del usuario (su código en el catálogo).
    // Se lee del token (sessionStorage), así que sobrevive a un refresco.
    const permitidas = pantallasPorRol(this.tokenService.getCodigoRol());
    this.menuItems = this.filtrarMenu(this.configuracionMenu, permitidas);
  }

  /**
   * Deja solo lo que el rol puede ver. Filtra tanto los items simples
   * (por su path) como los hijos de cada grupo; un grupo que se queda sin
   * hijos no se muestra.
   */
  private filtrarMenu(config: MenuItem[], permitidas: string[]): MenuItem[] {
    return config.reduce<MenuItem[]>((acc, item) => {
      if (item.children) {
        const children = item.children.filter(c => permitidas.includes(c.path));
        if (children.length > 0) { acc.push({ ...item, children }); }
      } else if (item.path && permitidas.includes(item.path)) {
        acc.push(item);
      }
      return acc;
    }, []);
  }

  toggleItem(item: MenuItem): void {
    if (item.children) {
      item.expanded = !item.expanded;
    }
  }

  onToggle() {
    this.toggle.emit();
  }

  logout(): void {
    localStorage.clear();
    this.sharedData.setPantallas([]);
    this.router.navigate(['/']);
  }
}
