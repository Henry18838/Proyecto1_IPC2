import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ClienteService {

  private apiUrl = 'http://localhost:8080/backend/api/clientes';

  constructor(private http: HttpClient) {}

  listar(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { withCredentials: true });
  }

  buscarPorDpi(dpi: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${dpi}`, { withCredentials: true });
  }

  crear(cliente: any): Observable<any> {
    return this.http.post(this.apiUrl, cliente, { withCredentials: true });
  }

  actualizar(dpi: string, cliente: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${dpi}`, cliente, { withCredentials: true });
  }
}