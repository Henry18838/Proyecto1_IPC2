import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  usuario: any;

  constructor(private authService: AuthService, private router: Router) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
    }
    this.usuario = this.authService.obtenerUsuario();
  }

  getRolTexto(): string {
    switch(this.usuario?.rol) {
      case 1: return 'Agente de Atención al Cliente';
      case 2: return 'Encargado de Operaciones';
      case 3: return 'Administrador';
      default: return 'Usuario';
    }
  }

  logout(): void {
    this.authService.cerrarSesion();
    this.router.navigate(['/login']);
  }
}