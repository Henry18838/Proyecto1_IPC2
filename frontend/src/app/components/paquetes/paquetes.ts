import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PaqueteService } from '../../services/paquete';
import { DestinoService } from '../../services/destino';
import { ProveedorService } from '../../services/proveedor';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-paquetes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './paquetes.html',
  styleUrl: './paquetes.css'
})
export class Paquetes implements OnInit {

  paquetes: any[] = [];
  paquetesFiltrados: any[] = [];
  destinos: any[] = [];
  proveedores: any[] = [];
  paqueteForm: any = {};
  servicioForm: any = {};
  modoEdicion: boolean = false;
  mostrarModal: boolean = false;
  mostrarModalServicio: boolean = false;
  mostrarDetalle: boolean = false;
  paqueteSeleccionado: any = null;
  busqueda: string = '';
  cargando: boolean = false;

  constructor(
    private paqueteService: PaqueteService,
    private destinoService: DestinoService,
    private proveedorService: ProveedorService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarPaquetes();
    this.cargarDestinos();
    this.cargarProveedores();
  }

  cargarPaquetes(): void {
    this.cargando = true;
    this.paqueteService.listar().subscribe({
      next: (data) => {
        this.paquetes = data;
        this.paquetesFiltrados = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar los paquetes', 'error');
      }
    });
  }

  cargarDestinos(): void {
    this.destinoService.listar().subscribe({
      next: (data) => this.destinos = data,
      error: () => {}
    });
  }

  cargarProveedores(): void {
    this.proveedorService.listar().subscribe({
      next: (data) => this.proveedores = data,
      error: () => {}
    });
  }

  filtrar(): void {
    const texto = this.busqueda.toLowerCase();
    this.paquetesFiltrados = this.paquetes.filter(p =>
      p.nombre.toLowerCase().includes(texto) ||
      p.destinoNombre?.toLowerCase().includes(texto)
    );
  }

  abrirModal(paquete?: any): void {
    if (paquete) {
      this.modoEdicion = true;
      this.paqueteForm = { ...paquete };
    } else {
      this.modoEdicion = false;
      this.paqueteForm = { activo: true };
    }
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.paqueteForm = {};
  }

  verDetalle(paquete: any): void {
    this.paqueteService.buscarPorId(paquete.id).subscribe({
      next: (data) => {
        this.paqueteSeleccionado = data;
        this.mostrarDetalle = true;
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar el detalle', 'error');
      }
    });
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.paqueteSeleccionado = null;
  }

  abrirModalServicio(paquete: any): void {
    this.paqueteSeleccionado = paquete;
    this.servicioForm = {};
    this.mostrarModalServicio = true;
  }

  cerrarModalServicio(): void {
    this.mostrarModalServicio = false;
    this.servicioForm = {};
  }

  guardar(): void {
    if (!this.paqueteForm.nombre || !this.paqueteForm.destinoId ||
        !this.paqueteForm.precioVenta || !this.paqueteForm.capacidadMaxima) {
      Swal.fire('Error', 'Por favor completa todos los campos requeridos', 'error');
      return;
    }

    if (this.modoEdicion) {
      this.paqueteService.actualizar(this.paqueteForm.id, this.paqueteForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Paquete actualizado correctamente', 'success');
          this.cerrarModal();
          this.cargarPaquetes();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al actualizar', 'error');
        }
      });
    } else {
      this.paqueteService.crear(this.paqueteForm).subscribe({
        next: () => {
          Swal.fire('Éxito', 'Paquete creado correctamente', 'success');
          this.cerrarModal();
          this.cargarPaquetes();
        },
        error: (err) => {
          Swal.fire('Error', err.error?.error || 'Error al crear', 'error');
        }
      });
    }
  }

  agregarServicio(): void {
    if (!this.servicioForm.proveedorId || !this.servicioForm.costo) {
      Swal.fire('Error', 'Proveedor y costo son requeridos', 'error');
      return;
    }

    this.paqueteService.agregarServicio(this.paqueteSeleccionado.id, this.servicioForm).subscribe({
      next: () => {
        Swal.fire('Éxito', 'Servicio agregado correctamente', 'success');
        this.cerrarModalServicio();
        this.cargarPaquetes();
      },
      error: (err) => {
        Swal.fire('Error', err.error?.error || 'Error al agregar servicio', 'error');
      }
    });
  }

  desactivar(paquete: any): void {
    Swal.fire({
      title: '¿Desactivar paquete?',
      text: `¿Estás seguro de desactivar "${paquete.nombre}"?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, desactivar',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.paqueteService.desactivar(paquete.id).subscribe({
          next: () => {
            Swal.fire('Desactivado', 'Paquete desactivado correctamente', 'success');
            this.cargarPaquetes();
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