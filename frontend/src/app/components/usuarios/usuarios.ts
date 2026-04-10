import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.css'
})
export class Usuarios implements OnInit {

  usuarios: any[] = [];
  usuarioForm: any = {};
  modoEdicion: boolean = false;
  mostrarModal: boolean = false;
  cargando: boolean = false;

  roles: any[] = [
    { id: 1, nombre: 'Agente de Atención al Cliente' },
    { id: 2, nombre: 'Encargado de Operaciones' },
    { id: 3, nombre: 'Administrador' }
  ];

  private apiUrl = 'http://localhost:8080/backend/api/usuarios';

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private router: Router
  ) {
    if (!this.authService.estaLogueado() || !this.authService.esAdmin()) {
      this.router.navigate(['/dashboard']);
    }
  }

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.http.get<any[]>(this.apiUrl, { withCredentials: true }).subscribe({
      next: (data) => {
        this.usuarios = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar los usuarios', 'error');
      }
    });
  }

  getRolTexto(rol: number): string {
    return this.roles.find(r => r.id === rol)?.nombre || 'Desconocido';
  }

  abrirModal(usuario?: any): void {
    if (usuario) {
      this.modoEdicion = true;
      this.usuarioForm = { ...usuario };
    } else {
      this.modoEdicion = false;
      this.usuarioForm = {};
    }
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.usuarioForm = {};
  }

  guardar(): void {
    if (!this.usuarioForm.nombre || !this.usuarioForm.rol) {
      Swal.fire('Error', 'Nombre y rol son requeridos', 'error');
      return;
    }

    if (!this.modoEdicion && (!this.usuarioForm.password || this.usuarioForm.password.length < 6)) {
      Swal.fire('Error', 'La contraseña debe tener mínimo 6 caracteres', 'error');
      return;
    }

    if (this.modoEdicion) {
      this.http.put(`${this.apiUrl}/${this.usuarioForm.id}`,
        { rol: this.usuarioForm.rol },
        { withCredentials: true }
      ).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Rol actualizado correctamente', 'success');
          this.cerrarModal();
          this.cargarUsuarios();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al actualizar', 'error');
        }
      });
    } else {
      this.http.post(this.apiUrl, this.usuarioForm, { withCredentials: true }).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Usuario creado correctamente', 'success');
          this.cerrarModal();
          this.cargarUsuarios();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al crear', 'error');
        }
      });
    }
  }

  desactivar(usuario: any): void {
    Swal.fire({
      title: '¿Desactivar usuario?',
      text: `¿Estás seguro de desactivar a "${usuario.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.http.delete(`${this.apiUrl}/${usuario.id}`, { withCredentials: true }).subscribe({
          next: () => {
            Swal.fire('Desactivado', 'Usuario desactivado correctamente', 'success');
            this.cargarUsuarios();
          },
          error: (err) => {
            Swal.fire('Error', err.error?.error || 'Error al desactivar', 'error');
          }
        });
      }
    });
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}