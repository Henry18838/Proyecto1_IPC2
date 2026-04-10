import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PaqueteService {

  private apiUrl = 'http://localhost:8080/backend/api/paquetes';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
  }

  listarActivos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?activo=true`, { withCredentials: true });
  }

  listarPorDestino(destinoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}?destino=${destinoId}`, { withCredentials: true });
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  crear(paquete: any): Observable<any> {
    return this.http.post(this.apiUrl, paquete, { withCredentials: true });
  }

  actualizar(id: number, paquete: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, paquete, { withCredentials: true });
  }

  desactivar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  agregarServicio(paqueteId: number, servicio: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/${paqueteId}/servicios`, servicio, { withCredentials: true });
  }
}