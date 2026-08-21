// ==============================================================
// PERMISOS POR ROL
// Fuente única de qué pantallas ve cada rol. La usan el menú lateral
// y el dashboard para mostrar solo lo que corresponde.
//
// Las claves son rutas relativas a `gestion-interna` (las mismas que
// se usan en el sidemenu y en los accesos del home). El rol se decide
// por su `codigo` de catalogos_cana (no por id ni nombre):
//   A  = Administrador/a General  -> todo
//   JO = Jefe Operativo
//   C  = Contador
//   O  = Operativo
// ==============================================================

/** Todas las pantallas del panel. Es lo que ve el Administrador general. */
export const TODAS_LAS_PANTALLAS: string[] = [
  'home',
  'administracion/usuarios',
  'administracion/inventario',
  'administracion/servicios',
  'eventos/cotizaciones',
  'eventos/pedidos',
  'eventos/entregas',
  'eventos/recolecciones',
  'eventos/calendario',
  'mantenimiento/salones',
  'reportes'
];

/**
 * Pantallas permitidas por rol. El Administrador ('A') no aparece aquí:
 * se resuelve con TODAS_LAS_PANTALLAS en pantallasPorRol(). Todos incluyen
 * 'home' (inicio) y 'eventos/calendario' porque son de acceso general.
 */
const PANTALLAS_POR_ROL: Record<string, string[]> = {
  // Jefe operativo: inventario, servicios, pedidos, entregas, cotizaciones y recolecciones.
  JO: [
    'home',
    'eventos/calendario',
    'administracion/inventario',
    'administracion/servicios',
    'eventos/cotizaciones',
    'eventos/pedidos',
    'eventos/entregas',
    'eventos/recolecciones'
  ],
  // Contador: inventario, servicios, pedidos, reportes.
  C: [
    'home',
    'eventos/calendario',
    'administracion/inventario',
    'administracion/servicios',
    'eventos/pedidos',
    'reportes'
  ],
  // Operativo: únicamente entregas, recolecciones y pedidos.
  O: [
    'home',
    'eventos/calendario',
    'eventos/pedidos',
    'eventos/entregas',
    'eventos/recolecciones'
  ]
};

/**
 * Lista de pantallas permitidas para un código de rol. Un código
 * desconocido solo ve el inicio, para no exponer de más.
 */
export function pantallasPorRol(codigoRol: string): string[] {
  if (codigoRol === 'A') { return [...TODAS_LAS_PANTALLAS]; }
  return PANTALLAS_POR_ROL[codigoRol] ?? ['home'];
}
