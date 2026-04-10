import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReservacionService {

  private apiUrl = 'http://localhost:8080/backend/api/reservaciones';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
  }

  listarHoy(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?hoy=true`, { withCredentials: true });
  }

  listarPorCliente(clienteId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?clienteId=${clienteId}`, { withCredentials: true });
  }

  buscarPorNumero(numero: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${numero}`, { withCredentials: true });
  }

  crear(reservacion: any): Observable<any> {
    return this.http.post(this.apiUrl, reservacion, { withCredentials: true });
  }

  actualizarEstado(numero: string, estado: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${numero}`, { estado }, { withCredentials: true });
  }
}