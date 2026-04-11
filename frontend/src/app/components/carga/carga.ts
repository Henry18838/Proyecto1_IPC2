import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CargaService } from '../../services/carga';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-carga',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carga.html',
  styleUrl: './carga.css'
})
export class Carga {

  archivoSeleccionado: File | null = null;
  nombreArchivo: string = '';
  cargando: boolean = false;
  resultado: any = null;

  constructor(
    private cargaService: CargaService,
    private authService: AuthService,
    private router: Router
  ) {
    if (!this.authService.estaLogueado() || !this.authService.esAdmin()) {
      this.router.navigate(['/dashboard']);
    }
  }

  seleccionarArchivo(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (!file.name.endsWith('.txt')) {
        Swal.fire('Error', 'Solo se permiten archivos .txt', 'error');
        return;
      }
      this.archivoSeleccionado = file;
      this.nombreArchivo = file.name;
      this.resultado = null;
    }
  }

  cargarArchivo(): void {
    if (!this.archivoSeleccionado) {
      Swal.fire('Error', 'Selecciona un archivo primero', 'error');
      return;
    }

    this.cargando = true;
    this.resultado = null;

    this.cargaService.cargarArchivo(this.archivoSeleccionado).subscribe({
      next: (data) => {
        this.resultado = data;
        this.cargando = false;

        const totalRegistros = Object.values(data.registrosProcesados)
          .reduce((a: any, b: any) => a + b, 0);

        if (data.totalErrores === 0) {
          Swal.fire('¡Éxito!', `Se procesaron ${totalRegistros} registros sin errores`, 'success');
        } else {
          Swal.fire('Completado con errores',
            `Se procesaron ${totalRegistros} registros con ${data.totalErrores} errores`,
            'warning');
        }
      },
      error: () => {
        this.cargando = false;
        Swal.fire('Error', 'No se pudo procesar el archivo', 'error');
      }
    });
  }

  getTotalRegistros(): number {
    if (!this.resultado) return 0;
    return Object.values(this.resultado.registrosProcesados)
      .reduce((a: any, b: any) => a + b, 0) as number;
  }

  getContadores(): any[] {
    if (!this.resultado) return [];
    return Object.entries(this.resultado.registrosProcesados)
      .map(([key, value]) => ({ tipo: key, cantidad: value }));
  }

  limpiar(): void {
    this.archivoSeleccionado = null;
    this.nombreArchivo = '';
    this.resultado = null;
  }

  volver(): void {
    this.router.navigate(['/dashboard']);
  }
}