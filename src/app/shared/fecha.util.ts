/**
 * Fecha "yyyy-mm-dd" armada desde las partes LOCALES del reloj.
 *
 * Nunca usar toISOString().slice(0,10) para esto: eso da la fecha en UTC y,
 * estando en UTC-6, después de las 18:00 locales ya devuelve el día siguiente.
 * El backend compara contra LocalDate.now() (zona del servidor), así que las
 * dos puntas tienen que hablar del mismo día.
 */
export function fechaISOLocal(fecha: Date): string {
  const y = fecha.getFullYear();
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function hoyISOLocal(): string {
  return fechaISOLocal(new Date());
}
