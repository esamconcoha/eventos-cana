/**
 * Paleta de las gráficas de composición. Es la misma secuencia que usa
 * GraficoUtil en el backend, para que una porción tenga el mismo color en la
 * pantalla y en el PDF del mismo reporte.
 */
export const PALETA_GRAFICOS = [
  '#021930', '#F2B134', '#0F4A7A', '#D97706', '#1565A8',
  '#9A3E00', '#38BDF8', '#7C3AED', '#059669', '#94A3B8'
];

export function colorSerie(indice: number): string {
  return PALETA_GRAFICOS[indice % PALETA_GRAFICOS.length];
}

/**
 * Monto abreviado para los ejes: "Q 12.5k". Escrito a mano y no con el pipe
 * number porque el eje necesita ancho fijo y tres caracteres significativos.
 */
export function montoCorto(valor: number): string {
  const v = valor ?? 0;
  if (v >= 1_000_000) return recorta(v / 1_000_000) + 'M';
  if (v >= 1_000) return recorta(v / 1_000) + 'k';
  return String(Math.round(v));
}

function recorta(valor: number): string {
  const texto = valor.toFixed(1);
  return texto.endsWith('.0') ? texto.slice(0, -2) : texto;
}

/**
 * Redondea el tope del eje hacia arriba (1, 2, 4, 5 o 10 por magnitud) para que
 * las cuatro líneas de la grilla caigan en números legibles.
 */
export function escalaSuperior(maximo: number): number {
  if (!maximo || maximo <= 0) return 4;
  const magnitud = Math.pow(10, Math.floor(Math.log10(maximo)));
  const normalizado = maximo / magnitud;
  const redondeado =
    normalizado <= 1 ? 1 :
    normalizado <= 2 ? 2 :
    normalizado <= 4 ? 4 :
    normalizado <= 5 ? 5 : 10;
  return redondeado * magnitud;
}
