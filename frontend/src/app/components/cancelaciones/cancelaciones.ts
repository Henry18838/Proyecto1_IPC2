import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CancelacionService } from '../../services/cancelacion';
import { ReservacionService } from '../../services/reservacion';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-cancelaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cancelaciones.html',
  styleUrl: './cancelaciones.css'
})
export class Cancelaciones implements OnInit {

  cancelaciones: any[] = [];
  numeroReservacion: string = '';
  reservacion: any = null;
  cargando: boolean = false;

  constructor(
    private cancelacionService: CancelacionService,
    private reservacionService: ReservacionService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {
    this.cargarCancelaciones();
  }

  cargarCancelaciones(): void {
    this.cargando = true;
    this.cancelacionService.listar().subscribe({
      next: (data) => {
        this.cancelaciones = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  buscarReservacion(): void {
    if (!this.numeroReservacion) {
      Swal.fire('Error', 'Ingresa el número de reservación', 'error');
      return;
    }

    this.reservacionService.buscarPorNumero(this.numeroReservacion).subscribe({
      next: (data) => {
        this.reservacion = data;
      },
      error: () => {
        this.reservacion = null;
        Swal.fire('No encontrada', 'No se encontró la reservación', 'warning');
      }
    });
  }

  procesarCancelacion(): void {
    if (!this.reservacion) {
      Swal.fire('Error', 'Primero busca una reservación', 'error');
      return;
    }

    Swal.fire({
      title: '¿Cancelar reservación?',
      text: `¿Estás seguro de cancelar la reservación ${this.reservacion.numeroReservacion}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, cancelar',
      cancelButtonText: 'No',
      confirmButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        this.cancelacionService.procesar(this.reservacion.numeroReservacion).subscribe({
          next: (resp) => {
            Swal.fire(
              '¡Cancelada!',
              `Porcentaje de reembolso: ${resp.porcentajeReembolso}%\n` +
              `Monto reembolsado: Q.${resp.montoReembolsado}\n` +
              `Pérdida agencia: Q.${resp.perdidaAgencia}`,
              'success'
            );
            this.reservacion = null;
            this.numeroReservacion = '';
            this.cargarCancelaciones();
          },
          error: (err) => {
            Swal.fire('Error', err.error?.error || 'Error al cancelar', 'error');
          }
        });
      }
    });
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

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}