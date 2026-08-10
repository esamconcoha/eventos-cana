import { Direccion } from './direccion';

export interface CrearUsuario {
  dpiNitUsuario: string;
  nombresUsuario: string;
  apellidosUsuario: string;
  telefonoUsuario: number;
  correo: string;
  esRepresentante: boolean;
  estadoUsuario: boolean;
  rol: number;
  esEmpresa: boolean;
  contrasenia: string;
  usuarioCreo: string;
  direcciones?: Direccion[];
}
