import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PagoService {

  private apiUrl = 'http://localhost:8080/backend/api/pagos';

  constructor(private http: HttpClient) {}

  listarPorReservacion(reservacionId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}?reservacionId=${reservacionId}`, { withCredentials: true });
  }

  registrar(pago: any): Observable<any> {
    return this.http.post(this.apiUrl, pago, { withCredentials: true });
  }

  generarComprobante(numeroReservacion: string): void {
    window.open(`${this.apiUrl}/comprobante/${numeroReservacion}`, '_blank');
  }
}