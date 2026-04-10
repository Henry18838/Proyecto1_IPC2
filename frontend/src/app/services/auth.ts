import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = 'http://localhost:8080/backend/api';

  constructor(private http: HttpClient) {}

  login(nombre: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { nombre, password }, { withCredentials: true });
  }

  logout(): Observable<any> {
    return this.http.delete(`${this.apiUrl}/login`, { withCredentials: true });
  }

  guardarSesion(usuario: any): void {
    localStorage.setItem('usuario', JSON.stringify(usuario));
  }

  obtenerUsuario(): any {
    const data = localStorage.getItem('usuario');
    return data ? JSON.parse(data) : null;
  }

  cerrarSesion(): void {
    localStorage.removeItem('usuario');
  }

  estaLogueado(): boolean {
    return this.obtenerUsuario() !== null;
  }

  getRol(): number {
    const usuario = this.obtenerUsuario();
    return usuario ? usuario.rol : 0;
  }

  esAdmin(): boolean { return this.getRol() === 3; }
  esOperaciones(): boolean { return this.getRol() === 2; }
  esAtencion(): boolean { return this.getRol() === 1; }
}