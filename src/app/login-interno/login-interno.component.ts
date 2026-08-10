import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { usuarioSesion } from '../interfaces/usuario';
import { SesionServiceService } from '../services/sesionService.service';
import { TokenService } from '../services/token.service';
import { ToastService } from '../shared/toast/toast.service';

@Component({
  selector: 'app-login-interno',
  templateUrl: './login-interno.component.html',
  styleUrls: ['./login-interno.component.css'],
  imports: [ReactiveFormsModule]
})
export class LoginInternoComponent implements OnInit {
  formLogueo: FormGroup;

  constructor(
    private formBuilder: FormBuilder,
    private sesionService: SesionServiceService,
    private router: Router,
    private tokenService: TokenService,
    private toast: ToastService
  ) {
    this.formLogueo = this.formBuilder.group({
      usuario:  ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    localStorage.removeItem('rol');
    localStorage.removeItem('nombre');
    localStorage.removeItem('dpi');
  }

  validarForm(): void {
    if (this.formLogueo.invalid) {
      this.formLogueo.markAllAsTouched();
      this.toast.error('Campos requeridos', 'Por favor completa todos los campos.');
    } else {
      this.ingresar();
    }
  }

  ingresar(): void {
    const sesion: usuarioSesion = {
      username: this.formLogueo.get('usuario')?.value,
      password: this.formLogueo.get('password')?.value
    };

    this.sesionService.iniciarSesion(sesion).subscribe({
      next: (token) => {
        this.tokenService.setToken(token.jwt);
        this.tokenService.setUserName(token.nombre);
        this.tokenService.setRol(token.rol);
        this.tokenService.setCodigoRol(token.codigoRol);
        this.tokenService.setNombreRol(token.nombreRol);
        this.tokenService.setDpi(token.dpi);
        localStorage.clear();
        this.router.navigateByUrl('gestion-interna/home');
        this.toast.success('Bienvenido', token.nombre);
      },
      error: (err) => {
        // Solo dos mensajes: inactivo (código 1005) o usuario no válido.
        if (err?.error?.code === 1005) {
          this.toast.error('Usuario inactivo', 'El usuario está inactivo.');
        } else {
          this.toast.error('Usuario no válido', 'El usuario no es válido.');
        }
      }
    });
  }
}
