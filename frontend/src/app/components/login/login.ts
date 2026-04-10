import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  nombre: string = '';
  password: string = '';
  cargando: boolean = false;

  constructor(private authService: AuthService, private router: Router) {
    // Si ya está logueado, redirigir al dashboard
    if (this.authService.estaLogueado()) {
      this.router.navigate(['/dashboard']);
    }
  }

  login(): void {
    if (!this.nombre || !this.password) {
      Swal.fire('Error', 'Por favor ingresa usuario y password', 'error');
      return;
    }

    this.cargando = true;
    this.authService.login(this.nombre, this.password).subscribe({
      next: (resp) => {
        this.authService.guardarSesion(resp);
        this.cargando = false;
        Swal.fire('Bienvenido', `Hola ${resp.nombre}!`, 'success').then(() => {
          this.router.navigate(['/dashboard']);
        });
      },
      error: (err) => {
        this.cargando = false;
        Swal.fire('Error', err.error?.error || 'Credenciales incorrectas', 'error');
      }
    });
  }
}