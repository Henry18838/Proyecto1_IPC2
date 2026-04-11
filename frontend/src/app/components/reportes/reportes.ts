import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReporteService } from '../../services/reporte';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.css'
})
export class Reportes {

  tipoReporte: string = '';
  fechaInicio: string = '';
  fechaFin: string = '';
  resultado: any = null;
  cargando: boolean = false;

  tiposReporte: any[] = [
    { id: 'ventas', nombre: '📊 Reporte de Ventas' },
    { id: 'cancelaciones', nombre: '❌ Reporte de Cancelaciones' },
    { id: 'ganancias', nombre: '💰 Reporte de Ganancias' },
    { id: 'agente-mas-ventas', nombre: '🏆 Agente con Más Ventas' },
    { id: 'agente-mas-ganancias', nombre: '💵 Agente con Más Ganancias' },
    { id: 'paquete-mas-vendido', nombre: '✈️ Paquete Más Vendido' },
    { id: 'paquete-menos-vendido', nombre: '📉 Paquete Menos Vendido' },
    { id: 'ocupacion-destino', nombre: '🌍 Ocupación por Destino' }
  ];

  constructor(
    private reporteService: ReporteService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado() || !this.authService.esAdmin()) {
      this.router.navigate(['/dashboard']);
    }
  }

  generarReporte(): void {
    if (!this.tipoReporte) {
      Swal.fire('Error', 'Selecciona un tipo de reporte', 'error');
      return;
    }

    this.cargando = true;
    this.resultado = null;

    this.reporteService.obtener(
      this.tipoReporte,
      this.fechaInicio || undefined,
      this.fechaFin || undefined
    ).subscribe({
      next: (data) => {
        this.resultado = data;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudo generar el reporte', 'error');
      }
    });
  }

  descargarCSV(): void {
    if (!this.tipoReporte) return;
    this.reporteService.descargarCSV(
      this.tipoReporte,
      this.fechaInicio || undefined,
      this.fechaFin || undefined
    );
  }

  esLista(): boolean {
    return Array.isArray(this.resultado);
  }

  esObjeto(): boolean {
    return this.resultado && !Array.isArray(this.resultado);
  }

  getKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  getTipoNombre(): string {
    return this.tiposReporte.find(t => t.id === this.tipoReporte)?.nombre || '';
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}