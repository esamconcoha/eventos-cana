import { Direccion } from './direccion';

export interface EditarUsuario {
  dpiNit: string;
  nombresUsuario: string;
  apellidosUsuario: string;
  telefonoUsuario: number;
  correo: string;
  rol: number;
  direcciones: Direccion[];
}
