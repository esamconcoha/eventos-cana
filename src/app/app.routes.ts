import { Routes } from '@angular/router';
import { LoginInternoComponent } from './login-interno/login-interno.component';

export const routes: Routes = [
  {path:'login',component:LoginInternoComponent,pathMatch:'full'},
  {path:'',redirectTo:'login',pathMatch:'full'},
   {path: 'gestion-interna', loadChildren: () => import('./gestion-interna/gestion-interna-module').then(m => m.GestionInternaModule)} 
];
