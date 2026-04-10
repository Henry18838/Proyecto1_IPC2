import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DestinoService } from '../../services/destino';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-destinos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './destinos.html',
  styleUrl: './destinos.css'
})
export class Destinos implements OnInit {

  destinos: any[] = [];
  destinosFiltrados: any[] = [];
  destinoForm: any = {};
  modoEdicion: boolean = false;
  mostrarModal: boolean = false;
  busqueda: string = '';
  cargando: boolean = false;

  constructor(
    private destinoService: DestinoService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarDestinos();
  }

  cargarDestinos(): void {
    this.cargando = true;
    this.destinoService.listar().subscribe({
      next: (data) => {
        this.destinos = data;
        this.destinosFiltrados = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar los destinos', 'error');
      }
    });
  }

  filtrar(): void {
    const texto = this.busqueda.toLowerCase();
    this.destinosFiltrados = this.destinos.filter(d =>
      d.nombre.toLowerCase().includes(texto) ||
      d.pais.toLowerCase().includes(texto)
    );
  }

  abrirModal(destino?: any): void {
    if (destino) {
      this.modoEdicion = true;
      this.destinoForm = { ...destino };
    } else {
      this.modoEdicion = false;
      this.destinoForm = {};
    }
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.destinoForm = {};
  }

  guardar(): void {
    if (!this.destinoForm.nombre || !this.destinoForm.pais) {
      Swal.fire('Error', 'Nombre y país son requeridos', 'error');
      return;
    }

    if (this.modoEdicion) {
      this.destinoService.actualizar(this.destinoForm.id, this.destinoForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Destino actualizado correctamente', 'success');
          this.cerrarModal();
          this.cargarDestinos();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al actualizar', 'error');
        }
      });
    } else {
      this.destinoService.crear(this.destinoForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Destino creado correctamente', 'success');
          this.cerrarModal();
          this.cargarDestinos();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al crear', 'error');
        }
      });
    }
  }

  eliminar(destino: any): void {
    Swal.fire({
      title: '¿Eliminar destino?',
      text: `¿Estás seguro de eliminar "${destino.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.destinoService.eliminar(destino.id).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'Destino eliminado correctamente', 'success');
            this.cargarDestinos();
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