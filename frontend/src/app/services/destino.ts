import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DestinoService {

  private apiUrl = 'http://localhost:8080/backend/api/destinos';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  crear(destino: any): Observable<any> {
    return this.http.post(this.apiUrl, destino, { withCredentials: true });
  }

  actualizar(id: number, destino: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, destino, { withCredentials: true });
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { withCredentials: true });
  }
}