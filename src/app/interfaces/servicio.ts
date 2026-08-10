export interface CategoriaServicio {
  idCategoria: number;
  nombreCategoria: string;
  tipoCategoria: string;
  estadoRegistro: boolean;
}

export interface ServicioDecoracion {
  idServicio: number;
  idCategoria: number;
  nombreCategoria?: string;   // solo para mostrar en tabla
  nombreServicio: string;
  descripcionServicio: string;
  unidadMedida: string;
  requiereDetalle: boolean;
  estadoServicio: boolean;
}

export interface CrearServicio {
  idCategoria: number;
  nombreServicio: string;
  descripcionServicio: string;
  unidadMedida: string;
  requiereDetalle: boolean;
}
