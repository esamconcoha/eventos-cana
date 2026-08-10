import { Direccion } from './direccion';

export interface UsuarioInterno {
  dpiNitUsuario: string;
  nombresUsuario: string;
  apellidosUsuario: string;
  telefonoUsuario: string;
  correoUsuario: string;
  rol: string;
  idRol:number;
  estadoUsuario: Boolean;
  direcciones?: Direccion[];
}
