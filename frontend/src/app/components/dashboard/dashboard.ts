import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard {
  usuario: any;
  menuItems: any[] = [];

  constructor(private authService: AuthService, private router: Router) {
    if (!this.authService.estaLogueado()) {
      this.router.navigate(['/login']);
      return;
    }
    this.usuario = this.authService.obtenerUsuario();
    this.configurarMenu();
  }

  configurarMenu(): void {
    const rol = this.usuario?.rol;

    if (rol === 1 || rol === 3) {
      this.menuItems.push(
        { label: '👥 Clientes', ruta: '/clientes' },
        { label: '📋 Reservaciones', ruta: '/reservaciones' },
        { label: '💳 Pagos', ruta: '/pagos' },
        { label: '❌ Cancelaciones', ruta: '/cancelaciones' }
      );
    }

    if (rol === 2 || rol === 3) {
      this.menuItems.push(
        { label: '🌍 Destinos', ruta: '/destinos' },
        { label: '🏨 Proveedores', ruta: '/proveedores' },
        { label: '✈️ Paquetes', ruta: '/paquetes' }
      );
    }

    if (rol === 3) {
      this.menuItems.push(
        { label: '📊 Reportes', ruta: '/reportes' },
        { label: '👤 Usuarios', ruta: '/usuarios' },
        { label: '📂 Carga de Datos', ruta: '/carga' }
      );
    }
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
    this.authService.logout().subscribe();
    this.authService.cerrarSesion();
    this.router.navigate(['/login']);
  }
}