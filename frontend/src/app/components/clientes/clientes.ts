import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ClienteService } from '../../services/cliente';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.html',
  styleUrl: './clientes.css'
})
export class Clientes implements OnInit {

  clientes: any[] = [];
  clientesFiltrados: any[] = [];
  clienteForm: any = {};
  modoEdicion: boolean = false;
  mostrarModal: boolean = false;
  busqueda: string = '';
  cargando: boolean = false;

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarClientes();
  }

  cargarClientes(): void {
    this.cargando = true;
    this.clienteService.listar().subscribe({
      next: (data) => {
        this.clientes = data;
        this.clientesFiltrados = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar los clientes', 'error');
      }
    });
  }

  filtrar(): void {
    const texto = this.busqueda.toLowerCase();
    this.clientesFiltrados = this.clientes.filter(c =>
      c.nombre.toLowerCase().includes(texto) ||
      c.dpi.includes(texto) ||
      c.email?.toLowerCase().includes(texto)
    );
  }

  abrirModal(cliente?: any): void {
    if (cliente) {
      this.modoEdicion = true;
      this.clienteForm = { ...cliente };
    } else {
      this.modoEdicion = false;
      this.clienteForm = {};
    }
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.clienteForm = {};
  }

  guardar(): void {
    if (!this.clienteForm.dpi || !this.clienteForm.nombre ||
        !this.clienteForm.email || !this.clienteForm.telefono) {
      Swal.fire('Error', 'Por favor completa todos los campos requeridos', 'error');
      return;
    }

    if (this.modoEdicion) {
      this.clienteService.actualizar(this.clienteForm.dpi, this.clienteForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Cliente actualizado correctamente', 'success');
          this.cerrarModal();
          this.cargarClientes();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al actualizar', 'error');
        }
      });
    } else {
      this.clienteService.crear(this.clienteForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Cliente creado correctamente', 'success');
          this.cerrarModal();
          this.cargarClientes();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al crear', 'error');
        }
      });
    }
  }

  buscarPorDpi(): void {
    if (!this.busqueda) return;
    this.clienteService.buscarPorDpi(this.busqueda).subscribe({
      next: (cliente) => {
        this.clientesFiltrados = [cliente];
      },
      error: () => {
        Swal.fire('No encontrado', 'No se encontró cliente con ese DPI', 'warning');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}