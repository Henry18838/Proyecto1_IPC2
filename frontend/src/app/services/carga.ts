import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CargaService {

  private apiUrl = 'http://localhost:8080/backend/api/carga';

  constructor(private http: HttpClient) {}

  cargarArchivo(archivo: File): Observable<any> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post(this.apiUrl, formData, { withCredentials: true });
  }
}