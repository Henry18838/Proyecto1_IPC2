import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProveedorService } from '../../services/proveedor';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './proveedores.html',
  styleUrl: './proveedores.css'
})
export class Proveedores implements OnInit {

  proveedores: any[] = [];
  proveedoresFiltrados: any[] = [];
  proveedorForm: any = {};
  modoEdicion: boolean = false;
  mostrarModal: boolean = false;
  busqueda: string = '';
  cargando: boolean = false;

  tiposServicio: any[] = [
    { id: 1, nombre: 'Aerolínea' },
    { id: 2, nombre: 'Hotel' },
    { id: 3, nombre: 'Tour' },
    { id: 4, nombre: 'Traslado' },
    { id: 5, nombre: 'Otro' }
  ];

  constructor(
    private proveedorService: ProveedorService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarProveedores();
  }

  cargarProveedores(): void {
    this.cargando = true;
    this.proveedorService.listar().subscribe({
      next: (data) => {
        this.proveedores = data;
        this.proveedoresFiltrados = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar los proveedores', 'error');
      }
    });
  }

  filtrar(): void {
    const texto = this.busqueda.toLowerCase();
    this.proveedoresFiltrados = this.proveedores.filter(p =>
      p.nombre.toLowerCase().includes(texto) ||
      p.pais?.toLowerCase().includes(texto)
    );
  }

  getTipoTexto(tipo: number): string {
    return this.tiposServicio.find(t => t.id === tipo)?.nombre || 'Desconocido';
  }

  abrirModal(proveedor?: any): void {
    if (proveedor) {
      this.modoEdicion = true;
      this.proveedorForm = { ...proveedor };
    } else {
      this.modoEdicion = false;
      this.proveedorForm = {};
    }
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.proveedorForm = {};
  }

  guardar(): void {
    if (!this.proveedorForm.nombre || !this.proveedorForm.tipo) {
      Swal.fire('Error', 'Nombre y tipo son requeridos', 'error');
      return;
    }

    if (this.modoEdicion) {
      this.proveedorService.actualizar(this.proveedorForm.id, this.proveedorForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Proveedor actualizado correctamente', 'success');
          this.cerrarModal();
          this.cargarProveedores();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al actualizar', 'error');
        }
      });
    } else {
      this.proveedorService.crear(this.proveedorForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Proveedor creado correctamente', 'success');
          this.cerrarModal();
          this.cargarProveedores();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al crear', 'error');
        }
      });
    }
  }

  eliminar(proveedor: any): void {
    Swal.fire({
      title: '¿Eliminar proveedor?',
      text: `¿Estás seguro de eliminar "${proveedor.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.proveedorService.eliminar(proveedor.id).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'Proveedor eliminado correctamente', 'success');
            this.cargarProveedores();
          },
          error: (err) => {
            Swal.fire('Error', err.error?.error || 'Error al eliminar', 'error');
          }
        });
      }
    });
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}