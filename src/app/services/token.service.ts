import { Injectable } from '@angular/core';
const USER_TOKEN = 'auth-token';
const USER_NAME = 'auth-user';
const USER_ROL = 'auth-rol';
const USER_ROL_CODIGO = 'auth-rol-codigo';
const USER_ROL_NOMBRE = 'auth-rol-nombre';
const USER_DPI= 'auth-dpi';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

constructor() { }
public setToken(token: string): void {
  window.sessionStorage.removeItem(USER_TOKEN);
  window.sessionStorage.setItem(USER_TOKEN, token);
}

public getToken(): string {
  return sessionStorage.getItem(USER_TOKEN)!;
}


public setUserName(username: string): void {
  window.sessionStorage.removeItem(USER_NAME);
  window.sessionStorage.setItem(USER_NAME, username);
}



public setRol(rol: string): void {
  window.sessionStorage.removeItem(USER_ROL);
  window.sessionStorage.setItem(USER_ROL, rol);
}

public getRol(): string {
  return sessionStorage.getItem(USER_ROL)!;
}

/** Código del rol. Es el valor a usar para decidir qué se muestra. */
public setCodigoRol(codigo: string): void {
  window.sessionStorage.setItem(USER_ROL_CODIGO, codigo ?? '');
}

public getCodigoRol(): string {
  return sessionStorage.getItem(USER_ROL_CODIGO) ?? '';
}

/** Nombre del rol, solo para mostrar en pantalla. */
public setNombreRol(nombre: string): void {
  window.sessionStorage.setItem(USER_ROL_NOMBRE, nombre ?? '');
}

public getNombreRol(): string {
  return sessionStorage.getItem(USER_ROL_NOMBRE) ?? '';
}

/** true si el usuario tiene alguno de los códigos de rol indicados. */
public tieneRol(...codigos: string[]): boolean {
  const actual = this.getCodigoRol();
  return !!actual && codigos.includes(actual);
}
public getUserName(): string {
  return sessionStorage.getItem(USER_NAME)!;
}

public setDpi(dpi: string): void {
  window.sessionStorage.removeItem(USER_DPI);
  window.sessionStorage.setItem(USER_DPI, dpi);
}
public getDpi(): string {
  return sessionStorage.getItem(USER_DPI)!;}

public logOut(): void {
  window.sessionStorage.clear();
}

}
