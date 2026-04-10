import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CancelacionService {

  private apiUrl = 'http://localhost:8080/backend/api/cancelaciones';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
  }

  listarPorIntervalo(fechaInicio: string, fechaFin: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`, { withCredentials: true });
  }

  procesar(numeroReservacion: string): Observable<any> {
    return this.http.post(this.apiUrl, { numeroReservacion }, { withCredentials: true });
  }
}