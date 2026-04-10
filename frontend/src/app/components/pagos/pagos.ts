import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PagoService } from '../../services/pago';
import { ReservacionService } from '../../services/reservacion';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagos.html',
  styleUrl: './pagos.css'
})
export class Pagos implements OnInit {

  numeroReservacion: string = '';
  reservacion: any = null;
  pagos: any[] = [];
  totalPagado: number = 0;
  pagoForm: any = {};
  mostrarModal: boolean = false;
  cargando: boolean = false;

  metodosPago: any[] = [
    { id: 1, nombre: 'Efectivo' },
    { id: 2, nombre: 'Tarjeta' },
    { id: 3, nombre: 'Transferencia' }
  ];

  constructor(
    private pagoService: PagoService,
    private reservacionService: ReservacionService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
  }

  ngOnInit(): void {}

  buscarReservacion(): void {
    if (!this.numeroReservacion) {
      Swal.fire('Error', 'Ingresa el número de reservación', 'error');
      return;
    }

    this.cargando = true;
    this.reservacionService.buscarPorNumero(this.numeroReservacion).subscribe({
      next: (data) => {
        this.reservacion = data;
        this.cargarPagos();
      },
      error: () => {
        this.cargando = false;
        this.reservacion = null;
        Swal.fire('No encontrada', 'No se encontró la reservación', 'warning');
      }
    });
  }

  cargarPagos(): void {
    this.pagoService.listarPorReservacion(this.reservacion.id).subscribe({
      next: (data) => {
        this.pagos = data.pagos;
        this.totalPagado = data.totalPagado;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
      }
    });
  }

  getMetodoTexto(metodo: number): string {
    return this.metodosPago.find(m => m.id === metodo)?.nombre || 'Desconocido';
  }

  getSaldoPendiente(): number {
    return this.reservacion ? this.reservacion.costoTotal - this.totalPagado : 0;
  }

  abrirModal(): void {
    if (!this.reservacion) {
      Swal.fire('Error', 'Primero busca una reservación', 'error');
      return;
    }
    if (this.reservacion.estado === 3 || this.reservacion.estado === 4) {
      Swal.fire('Error', 'No se puede pagar una reservación cancelada o completada', 'error');
      return;
    }
    this.pagoForm = { fechaPago: new Date().toISOString().split('T')[0] };
    this.mostrarModal = true;
  }

  cerrarModal(): void {
    this.mostrarModal = false;
    this.pagoForm = {};
  }

  registrarPago(): void {
    if (!this.pagoForm.monto || !this.pagoForm.metodo || !this.pagoForm.fechaPago) {
      Swal.fire('Error', 'Todos los campos son requeridos', 'error');
      return;
    }

    if (this.pagoForm.monto <= 0) {
      Swal.fire('Error', 'El monto debe ser mayor a 0', 'error');
      return;
    }

    const pago = {
      reservacionId: this.reservacion.id,
      monto: this.pagoForm.monto,
      metodo: this.pagoForm.metodo,
      fechaPago: this.pagoForm.fechaPago
    };

    this.pagoService.registrar(pago).subscribe({
      next: (resp) => {
        let mensaje = `Pago registrado. Total pagado: Q.${resp.totalPagado}`;
        if (resp.reservacionConfirmada) {
          mensaje += '\n✅ ¡Reservación CONFIRMADA!';
        } else {
          mensaje += `\nSaldo pendiente: Q.${resp.saldoPendiente}`;
        }
        Swal.fire('Éxito', mensaje, 'success');
        this.cerrarModal();
        this.cargarPagos();
        this.buscarReservacion();
      },
      error: (err) => {
        Swal.fire('Error', err.error?.error || 'Error al registrar pago', 'error');
      }
    });
  }

  generarComprobante(): void {
    if (!this.reservacion) return;
    this.pagoService.generarComprobante(this.reservacion.numeroReservacion);
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

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}