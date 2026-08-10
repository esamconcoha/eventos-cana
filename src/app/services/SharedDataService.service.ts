import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedDataService {
  private pantallasSource = new BehaviorSubject<string[]>([]);
  pantallas$ = this.pantallasSource.asObservable();

  setPantallas(pantallas: string[]) {
    this.pantallasSource.next(pantallas);
  }

  getPantallas(): string[] {
    return this.pantallasSource.value;
  }

  restorePantallas(): void {
    const pantallasGuardadas = localStorage.getItem('pantallas');

    if (pantallasGuardadas) {
      const pantallas = JSON.parse(pantallasGuardadas);
      this.setPantallas(pantallas);
    }
  }

  ensurePantallasLoaded(): string[] {
    let pantallas = this.getPantallas();
    if (!pantallas || pantallas.length === 0) {
      const stored = localStorage.getItem('pantallas');
      if (stored) pantallas = JSON.parse(stored);
      this.setPantallas(pantallas);
    }
    return pantallas;
  }
}
