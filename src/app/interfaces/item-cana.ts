export interface ItemCana {
  idItem: number;
  idTipoItem: number;
  costoItem: number;
  descripcionItem: string;
  cantidadItem: number;
  observaciones?: string;
  cantidadFaltante: number;
  nombreItem: string;
  estadoItem: boolean;
}

export interface CrearItem {
  idTipoItem: number;
  costoItem: number;
  descripcionItem: string;
  cantidadItem: number;
  observaciones?: string;
  cantidadFaltante: number;
}
