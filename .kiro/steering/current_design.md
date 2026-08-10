> Debe usarse como referencia base para cualquier decisión de desarrollo, estructura o estilo.

---

## 1. Stack Tecnológico

- Angular 21 (standalone components + NgModules mixtos)
- Tailwind CSS v3 como sistema de estilos principal
- RxJS para manejo de estado reactivo (BehaviorSubject)
- SweetAlert2 para alertas y notificaciones
- Reactive Forms para formularios
- Google Material Icons (via CDN, usado en sidemenu)
- Fuentes: Montserrat (títulos/botones) y Open Sans (cuerpo), cargadas vía Google Fonts

---

## 2. Estructura de Carpetas

```
src/app/
├── app.ts                        # Componente raíz (standalone), solo contiene <router-outlet>
├── app.config.ts                 # Configuración de la app (providers globales, incluye provideHttpClient)
├── app.routes.ts                 # Rutas raíz
│
├── login-interno/                # Standalone component
│   ├── login-interno.component.ts
│   ├── login-interno.component.html
│   └── login-interno.component.css   # CSS clásico (caso heredado, no replicar)
│
├── gestion-interna/              # Módulo lazy-loaded (NgModule clásico)
│   ├── gestion-interna-module.ts
│   ├── gestion-interna-routing-module.ts
│   ├── layout/                   # Shell del área interna (standalone)
│   │   ├── layout.component.ts   # Contiene sidemenu + <router-outlet>
│   │   ├── layout.component.html
│   │   └── layout.component.css
│   ├── home/                     # Vista de inicio (standalone)
│   │   ├── home.component.ts
│   │   ├── home.component.html
│   │   └── home.component.css
│   └── administracion/
│       └── usuarios/             # Gestión de usuarios (standalone)
│           ├── usuarios.component.ts
│           ├── usuarios.component.html
│           └── usuarios.component.css
│
├── shared/                       # Componentes reutilizables (todos standalone)
│   ├── sidemenu/
│   │   ├── sidemenu.component.ts   # Soporta items simples y grupos con hijos
│   │   ├── sidemenu.component.html # 100% Tailwind CSS
│   │   └── sidemenu.component.css  # Vacío
│   └── loading/
│       ├── loading.component.ts
│       ├── loading.component.html
│       └── loading.component.css
│
├── services/
│   ├── loading.service.ts
│   ├── sesionService.service.ts
│   ├── SharedDataService.service.ts
│   ├── token.service.ts
│   └── usuario.service.ts        # CRUD de usuarios internos
│
├── interfaces/
│   ├── usuario.ts                # usuarioSesion (login)
│   ├── usuario-interno.ts        # UsuarioInterno (respuesta del listado)
│   ├── crear-usuario.ts          # CrearUsuario (payload para crear/editar)
│   ├── direccion.ts              # Direccion (tabla direcciones BD)
│   └── token.ts
│
└── security/
│   └── auth.interceptor.ts       # JWT interceptor funcional (agrega Bearer token a todas las requests a apiUrl)
```

---

## 3. Routing

```
/                              → redirect a /login
/login                         → LoginInternoComponent (standalone, carga directa)
/gestion-interna               → GestionInternaModule (lazy-loaded)
  /gestion-interna             → LayoutComponent (shell con sidemenu)
    /gestion-interna/home                      → HomeComponent
    /gestion-interna/administracion/usuarios   → UsuariosComponent
```

- `LayoutComponent` es el shell: contiene el sidemenu y un `<router-outlet>` para las vistas hijas.
- Todas las vistas internas son rutas hijas de `LayoutComponent`.
- Las rutas hijas se definen en `gestion-interna-routing-module.ts` usando `loadComponent` (lazy standalone).
- Para agregar una nueva sección: crear el componente en `gestion-interna/<seccion>/` y registrar la ruta en el routing module.

---

## 4. Servicios

### TokenService (`token.service.ts`)
- Maneja la sesión del usuario usando `sessionStorage`.
- Claves: `auth-token`, `auth-user`, `auth-rol`, `auth-dpi`.
- Métodos: `setToken`, `getToken`, `setUserName`, `getUserName`, `setRol`, `getRol`, `setDpi`, `getDpi`, `logOut`.
- `logOut()` limpia todo el sessionStorage.

### SesionServiceService (`sesionService.service.ts`)
- Llama al endpoint `publico/authenticate` con `POST`.
- Usa `environment.apiUrl` como base URL.
- Retorna un observable de tipo `token` (jwt, nombre, rol, dpi).

### LoadingService (`loading.service.ts`)
- BehaviorSubject `loading$` de tipo boolean.
- Métodos: `show()` y `hide()`.
- Se consume en `LoadingComponent` con el async pipe.

### UsuarioService (`usuario.service.ts`)
- Maneja el CRUD de usuarios internos del sistema.
- Usa `environment.apiUrl` como base URL.
- El JWT lo agrega automáticamente el interceptor `jwtInterceptorInterceptor`.
- Métodos: `guardarUsuario`, `listarUsuarios`, `editarUsuario`, `eliminarUsuario`.
- Endpoint activo: `POST publico/guardarUsuario`.
- Endpoints preparados (pendientes de backend): `PUT privado/editarUsuario/:dpi`, `DELETE privado/eliminarUsuario/:dpi`, `GET privado/listarUsuarios`.

### SharedDataService (`SharedDataService.service.ts`)
- Controla qué ítems del sidemenu son visibles según permisos del backend.
- Persiste en `localStorage` bajo la clave `'pantallas'`.
- Métodos: `setPantallas`, `getPantallas`, `restorePantallas`, `ensurePantallasLoaded`.

---

## 5. Autenticación y Sesión

- El login llama a `SesionServiceService.iniciarSesion()`.
- Al éxito, guarda `jwt`, `nombre`, `rol`, `dpi` en `sessionStorage` via `TokenService`.
- Redirige a `gestion-interna/home`.
- Al cerrar sesión (`SidemenuComponent.logout()`): limpia `localStorage`, resetea `pantallas$` y navega a `/`.
- La carpeta `security/` está reservada para guards de ruta (aún no implementados).

---

## 6. Sidemenu y Control de Acceso por Pantallas

- `SidemenuComponent` es standalone, importado directamente por `LayoutComponent`.
- Soporta dos tipos de ítems: simples (`path` directo) y grupos con hijos (`children[]`).
- Los grupos tienen toggle de expansión con animación de flecha.
- Los ítems visibles se filtran según `pantallas$` del `SharedDataService`.
- Si `pantallas$` está vacío, se muestran todos (modo desarrollo).
- Soporta modo colapsado (desktop) y overlay (mobile, breakpoint `lg` = 1024px).
- Usa Material Icons para los íconos.

Configuración actual del menú:
| title          | path / tipo              | icon                   |
|----------------|--------------------------|------------------------|
| Inicio         | home                     | home                   |
| Administración | grupo (expandible)       | admin_panel_settings   |
| → Usuarios     | administracion/usuarios  | people                 |
| Ventas         | ventas                   | attach_money           |
| Inventario     | inventario               | inventory_2            |
| Reportes       | reportes                 | bar_chart              |
| Configuración  | configuracion            | settings               |

---

## 7. Estilos — Reglas y Convenciones

### Sistema principal: Tailwind CSS
- Todos los componentes nuevos deben usar **Tailwind CSS** exclusivamente.
- No crear archivos `.css` con estilos propios salvo excepciones justificadas.
- El archivo `sidemenu.component.css` y `loading.component.css` están vacíos intencionalmente.

### Excepción: Login
- `login-interno.component.css` usa CSS clásico (no Tailwind). Es un caso heredado.
- No replicar este patrón en componentes nuevos.

### Tipografía (definida en `styles.css` con `@layer base`)
- `body`: `font-opensans`, 15px, `text-gray-800`, `bg-slate-50`
- `h1`: `font-montserrat`, 24-28px, bold, `text-gray-900`
- `h2`: `font-montserrat`, 20-22px, semibold, `text-gray-800`
- `button`: `font-montserrat`, semibold, 14px
- `small / .note`: `font-opensans`, 12px, `text-grayMedium`

### Colores personalizados (Tailwind extend)
- `grayMedium`: `#6B7280`

### Fuentes personalizadas (Tailwind extend)
- `font-montserrat`: Montserrat, sans-serif
- `font-opensans`: Open Sans, sans-serif

---

## 8. Patrones de Componentes

- Componentes nuevos dentro de `gestion-interna` deben ser **standalone: false** y declararse en `GestionInternaModule`, o bien ser **standalone: true** e importarse directamente.
- Componentes en `shared/` deben ser siempre **standalone: true**.
- Usar `@if` y `@for` (nueva sintaxis de control flow de Angular 17+), no `*ngIf` ni `*ngFor`.
- Excepción: `loading.component.html` usa `*ngIf` con async pipe (pendiente de migrar).

---

## 9. Interfaces

```typescript
// usuario.ts
export interface usuarioSesion {
  username: string;
  password: string;
}

// usuario-interno.ts — para mostrar la tabla (campos del backend)
export interface UsuarioInterno {
  dpiNitUsuario: string;
  nombreUsuario: string;
  apellidosUsuario: string;
  telefonoUsuario: string;
  correoUsuario: string;
  rol: string;
  idRol: number;
  estadoUsuario: boolean;
  direcciones?: Direccion[];
}

// crear-usuario.ts — para el formulario de creación/edición
export interface CrearUsuario {
  dpi_nit_usuario: string;
  nombres_usuario: string;
  apellidos_usuario: string;
  telefono_usuario: string;
  correo: string;
  es_representante: boolean;
  estado_usuario: boolean;
  rol: number;
  es_empresa: boolean;
  contrasenia: string;
  usuario_creo: string;
  direcciones?: Direccion[];
}

// direccion.ts
export interface Direccion {
  id_direccion?: number;
  direccion: string;
  nit_dpi?: string;
}

// token.ts
export class token {
  jwt: string = "";
  nombre: string = "";
  rol: string = "";
  dpi: string = "";
}
```

---

## 10. Pendientes / Áreas a Desarrollar

- `security/`: Contiene `auth.interceptor.ts` (JWT funcional). Guards pendientes (AuthGuard, RoleGuard).
- Endpoints de editar/eliminar usuario pendientes de implementación en el backend.
- `listarUsuarios` pendiente de endpoint en el backend.
- Funcionalidad de editar/eliminar usuarios en la tabla (botones listos, lógica pendiente).
- `loading.component.html` usa sintaxis antigua (`*ngIf`), pendiente migrar a `@if`.
- El tipo `token` es una clase, no una interfaz — considerar migrar a `interface`.
