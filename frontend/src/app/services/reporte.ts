import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ReporteService {

  private apiUrl = 'http://localhost:8080/backend/api/reportes';

  constructor(private http: HttpClient) {}

  obtener(tipo: string, fechaInicio?: string, fechaFin?: string): Observable<any> {
    let url = `${this.apiUrl}/${tipo}`;
    if (fechaInicio && fechaFin) {
      url += `?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
    }
    return this.http.get<any>(url, { withCredentials: true });
  }

  descargarCSV(tipo: string, fechaInicio?: string, fechaFin?: string): void {
    let url = `${this.apiUrl}/${tipo}?formato=csv`;
    if (fechaInicio && fechaFin) {
      url += `&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
    }
    window.open(url, '_blank');
  }
}