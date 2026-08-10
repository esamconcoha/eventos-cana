import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { UsuarioService } from '../../../services/usuario.service';
import { CatalogoService } from '../../../services/catalogo.service';
import { TokenService } from '../../../services/token.service';
import { UsuarioInterno } from '../../../interfaces/usuario-interno';
import { CrearUsuario } from '../../../interfaces/crear-usuario';
import { EditarUsuario } from '../../../interfaces/editar-usuario';
import { Catalogo } from '../../../interfaces/catalogo';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-usuarios',
  standalone: false,
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {

  // Formularios
  formCrear: FormGroup;
  formEditar: FormGroup;

  // Estado
  usuarios: UsuarioInterno[] = [];
  roles: Catalogo[] = [];
  cargando = false;
  mostrarFormCrear = false;
  mostrarFormEditar = false;
  usuarioEditando: UsuarioInterno | null = null;

  constructor(
    private fb: FormBuilder,
    private usuarioService: UsuarioService,
    private catalogoService: CatalogoService,
    private tokenService: TokenService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.formCrear = this.fb.group({
      dpiNitUsuario:   ['', [Validators.required, Validators.maxLength(16)]],
      nombresUsuario:  ['', [Validators.required, Validators.maxLength(50)]],
      apellidosUsuario:['', [Validators.required, Validators.maxLength(50)]],
      telefonoUsuario: ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      correo:          ['', [Validators.required, Validators.email, Validators.maxLength(50)]],
      contrasenia:     ['', [Validators.required, Validators.minLength(6)]],
      rol:             [null, Validators.required],
      direcciones:     this.fb.array([this.nuevaDireccion()])
    });

    this.formEditar = this.fb.group({
      nombresUsuario:   ['', [Validators.required, Validators.maxLength(50)]],
      apellidosUsuario: ['', [Validators.required, Validators.maxLength(50)]],
      telefonoUsuario:  ['', [Validators.required, Validators.pattern('^[0-9]{8}$')]],
      correo:           ['', [Validators.required, Validators.email, Validators.maxLength(50)]],
      rol:              [null, Validators.required],
      direcciones:      this.fb.array([])
    });
  }

  ngOnInit(): void {
    this.cargarRoles();
    this.cargarUsuarios();
  }

  // ─── Roles ───────────────────────────────────────────────
  cargarRoles(): void {
    this.catalogoService.getCatalogoByNombre('ROLES').subscribe({
      next: (data) => { this.roles = data; this.cdr.detectChanges(); },
      error: () => {}
    });
  }

  // ─── Usuarios ────────────────────────────────────────────
  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.listarUsuarios().subscribe({
      next: (data) => { 
        this.usuarios = data; this.cargando = false; this.cdr.detectChanges(); },
      error: () => { this.cargando = false; this.cdr.detectChanges(); }
    });
  }

  // ─── Crear ───────────────────────────────────────────────
  get direccionesCrear(): FormArray {
    return this.formCrear.get('direcciones') as FormArray;
  }

  nuevaDireccion(): FormGroup {
    return this.fb.group({ direccion: ['', Validators.required] });
  }

  agregarDireccionCrear(): void {
    this.direccionesCrear.push(this.nuevaDireccion());
  }

  eliminarDireccionCrear(i: number): void {
    if (this.direccionesCrear.length > 1) this.direccionesCrear.removeAt(i);
    else this.toast.info('Aviso', 'Debe haber al menos una dirección');
  }

  guardar(): void {
    if (this.formCrear.invalid) { this.formCrear.markAllAsTouched(); return; }
    const payload: CrearUsuario = {
      ...this.formCrear.value,
      telefonoUsuario: Number(this.formCrear.value.telefonoUsuario),
      usuarioCreo: this.tokenService.getDpi(),
      esRepresentante: false,
      esEmpresa: false,
      estadoUsuario: true
    };
    this.usuarioService.guardarUsuario(payload).subscribe({
      next: () => {
        this.toast.success('Usuario creado');
        this.cancelarCrear();
        this.cargarUsuarios();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo guardar')
    });
  }

  cancelarCrear(): void {
    this.formCrear.reset({ rol: null });
    this.direccionesCrear.clear();
    this.direccionesCrear.push(this.nuevaDireccion());
    this.mostrarFormCrear = false;
  }

  // ─── Editar ──────────────────────────────────────────────
  get direccionesEditar(): FormArray {
    return this.formEditar.get('direcciones') as FormArray;
  }

  agregarDireccionEditar(): void {
    this.direccionesEditar.push(this.nuevaDireccion());
  }

  eliminarDireccionEditar(i: number): void {
    if (this.direccionesEditar.length > 1) this.direccionesEditar.removeAt(i);
  }

  abrirEditar(usuario: UsuarioInterno): void {
    this.usuarioEditando = usuario;
    this.mostrarFormEditar = true;
    this.mostrarFormCrear = false;

    // Pre-llenar campos básicos
    this.formEditar.patchValue({
      nombresUsuario:   usuario.nombresUsuario,
      apellidosUsuario: usuario.apellidosUsuario,
      telefonoUsuario:  usuario.telefonoUsuario,
      correo:           usuario.correoUsuario,
      rol:              usuario.idRol
    });

    // Cargar direcciones del usuario
    this.direccionesEditar.clear();
    this.usuarioService.getDireccionesUsuario(usuario.dpiNitUsuario).subscribe({
      next: (dirs) => {
        if (dirs.length === 0) {
          this.direccionesEditar.push(this.nuevaDireccion());
        } else {
          dirs.forEach(d => {
            this.direccionesEditar.push(this.fb.group({
              idDireccion: [d.idDireccion],
              direccion: [d.direccion, Validators.required]
            }));
          });
        }
        this.cdr.detectChanges();
      },
      error: () => this.direccionesEditar.push(this.nuevaDireccion())
    });
  }

  guardarEdicion(): void {
    if (this.formEditar.invalid || !this.usuarioEditando) { this.formEditar.markAllAsTouched(); return; }
    const payload: EditarUsuario = {
      ...this.formEditar.value,
      dpiNit: this.usuarioEditando.dpiNitUsuario,
      telefonoUsuario: Number(this.formEditar.value.telefonoUsuario)
    };
    console.log(payload);
    this.usuarioService.editarUsuario(payload).subscribe({
      next: () => {
        this.toast.success('Usuario actualizado');
        this.cancelarEditar();
        this.cargarUsuarios();
      },
      error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo actualizar')
    });
  }

  cancelarEditar(): void {
    this.formEditar.reset();
    this.direccionesEditar.clear();
    this.mostrarFormEditar = false;
    this.usuarioEditando = null;
  }

  confirmarInactivar(usuario: UsuarioInterno): void {
    this.toast.confirm({
      title: '¿Inactivar usuario?',
      message: `¿Estás seguro de inactivar a ${usuario.nombresUsuario} ${usuario.apellidosUsuario}?`,
      confirmText: 'Sí, inactivar',
      cancelText: 'Cancelar',
      onConfirm: () => {
        this.usuarioService.inactivarUsuario(usuario.dpiNitUsuario).subscribe({
          next: () => {
            this.toast.success('Usuario inactivado');
            this.cargarUsuarios();
          },
          error: (err) => this.toast.error('Error', err?.error?.message ?? 'No se pudo inactivar el usuario')
        });
      }
    });
  }
  campoInvalidoCrear(campo: string): boolean {
    const c = this.formCrear.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  campoInvalidoEditar(campo: string): boolean {
    const c = this.formEditar.get(campo);
    return !!(c?.invalid && c?.touched);
  }

  dirInvalidaCrear(i: number): boolean {
    const d = this.direccionesCrear.at(i);
    return !!(d?.invalid && d?.touched);
  }

  dirInvalidaEditar(i: number): boolean {
    const d = this.direccionesEditar.at(i);
    return !!(d?.invalid && d?.touched);
  }
}
