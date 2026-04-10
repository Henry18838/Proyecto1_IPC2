import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Clientes } from './components/clientes/clientes';
import { Reservaciones } from './components/reservaciones/reservaciones';
import { Pagos } from './components/pagos/pagos';
import { Cancelaciones } from './components/cancelaciones/cancelaciones';
import { Destinos } from './components/destinos/destinos';
import { Proveedores } from './components/proveedores/proveedores';
import { Paquetes } from './components/paquetes/paquetes';
import { Reportes } from './components/reportes/reportes';
import { Carga } from './components/carga/carga';
import { Usuarios } from './components/usuarios/usuarios';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard },
  { path: 'clientes', component: Clientes },
  { path: 'reservaciones', component: Reservaciones },
  { path: 'pagos', component: Pagos },
  { path: 'cancelaciones', component: Cancelaciones },
  { path: 'destinos', component: Destinos },
  { path: 'proveedores', component: Proveedores },
  { path: 'paquetes', component: Paquetes },
  { path: 'reportes', component: Reportes },
  { path: 'carga', component: Carga },
  { path: 'usuarios', component: Usuarios },
  { path: '**', redirectTo: 'login' }
];