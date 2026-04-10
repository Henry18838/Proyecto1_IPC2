import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReservacionService } from '../../services/reservacion';
import { ClienteService } from '../../services/cliente';
import { PaqueteService } from '../../services/paquete';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reservaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reservaciones.html',
  styleUrl: './reservaciones.css'
})
export class Reservaciones implements OnInit {

  reservaciones: any[] = [];
  reservacionesFiltradas: any[] = [];
  paquetes: any[] = [];
  reservacionForm: any = { pasajeros: [] };
  mostrarModal: boolean = false;
  mostrarDetalle: boolean = false;
  reservacionDetalle: any = null;
  busqueda: string = '';
  cargando: boolean = false;
  soloDia: boolean = false;
  dpiBusqueda: string = '';

  constructor(
    private reservacionService: ReservacionService,
    private clienteService: ClienteService,
    private paqueteService: PaqueteService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarReservaciones();
    this.cargarPaquetes();
  }

  cargarReservaciones(): void {
    this.cargando = true;
    this.reservacionService.listar().subscribe({
      next: (data) => {
        this.reservaciones = data;
        this.reservacionesFiltradas = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudieron cargar las reservaciones', 'error');
      }
    });
  }

  cargarPaquetes(): void {
    this.paqueteService.listarActivos().subscribe({
      next: (data) => this.paquetes = data,
      error: () => {}
    });
  }

  cargarReservacionesHoy(): void {
    this.cargando = true;
    this.reservacionService.listarHoy().subscribe({
      next: (data) => {
        this.reservacionesFiltradas = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  filtrar(): void {
    const texto = this.busqueda.toLowerCase();
    this.reservacionesFiltradas = this.reservaciones.filter(r =>
      r.numeroReservacion.toLowerCase().includes(texto) ||
      r.paqueteNombre?.toLowerCase().includes(texto) ||
      r.agenteNombre?.toLowerCase().includes(texto)
    );
  }

  toggleSoloDia(): void {
    this.soloDia = !this.soloDia;
    if (this.soloDia) {
      this.cargarReservacionesHoy();
    } else {
      this.cargarReservaciones();
    }
  }

  getEstadoTexto(estado: number): string {
    switch(estado) {
      case 1: return 'Pendiente';
      case 2: return 'Confirmada';
      case 3: return 'Cancelada';
      case 4: return 'Completada';
      default: return 'Desconocido';
    }
  }

  getEstadoClass(estado: number): string {
    switch(estado) {
      case 1: return 'badge-pendiente';
      case 2: return 'badge-confirmada';
      case 3: return 'badge-cancelada';
      case 4: return 'badge-completada';
      default: return '';
    }
  }

  abrirModal(): void {
    this.reservacionForm = { pasajeros: [{ dpi: '' }] };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.reservacionForm = { pasajeros: [] };
  }

  agregarPasajero(): void {
    this.reservacionForm.pasajeros.push({ dpi: '' });
  }

  eliminarPasajero(index: number): void {
    this.reservacionForm.pasajeros.splice(index, 1);
  }

  verDetalle(numero: string): void {
    this.reservacionService.buscarPorNumero(numero).subscribe({
      next: (data) => {
        this.reservacionDetalle = data;
        this.mostrarDetalle = true;
      },
      error: () => {
        Swal.fire('Error', 'No se pudo cargar el detalle', 'error');
      }
    });
  }

  cerrarDetalle(): void {
    this.mostrarDetalle = false;
    this.reservacionDetalle = null;
  }

  guardar(): void {
    if (!this.reservacionForm.paqueteId || !this.reservacionForm.fechaViaje) {
      Swal.fire('Error', 'Paquete y fecha de viaje son requeridos', 'error');
      return;
    }

    const pasajeros = this.reservacionForm.pasajeros.filter((p: any) => p.dpi);
    if (pasajeros.length === 0) {
      Swal.fire('Error', 'Debe agregar al menos un pasajero', 'error');
      return;
    }

    const data = {
      paqueteId: this.reservacionForm.paqueteId,
      fechaViaje: this.reservacionForm.fechaViaje,
      cantidadPasajeros: pasajeros.length,
      pasajeros: pasajeros
    };

    this.reservacionService.crear(data).subscribe({
      next: (resp) => {
        Swal.fire('Éxito', `Reservación ${resp.numeroReservacion} creada. Total: Q.${resp.costoTotal}`, 'success');
        this.cerrarModal();
        this.cargarReservaciones();
      },
      error: (err) => {
        Swal.fire('Error', err.error?.error || 'Error al crear reservación', 'error');
      }
    });
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}