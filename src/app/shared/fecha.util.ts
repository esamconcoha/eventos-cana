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

/**
 * Fecha y hora "yyyy-mm-ddThh:mm:ss" armada desde las partes LOCALES del reloj.
 *
 * Es lo que hay que mandar en los campos que el backend recibe como
 * LocalDateTime (fechaEvento y compañía). Con toISOString() se mandaba la hora
 * en UTC y Jackson, al llenar un LocalDateTime, descarta la "Z" y se queda con
 * los dígitos literales: un evento a las 00:00 se guardaba como las 06:00, y
 * uno de las 20:00 caía en el día siguiente.
 */
export function fechaHoraISOLocal(fecha: Date): string {
  const hh = String(fecha.getHours()).padStart(2, '0');
  const mm = String(fecha.getMinutes()).padStart(2, '0');
  return `${fechaISOLocal(fecha)}T${hh}:${mm}:00`;
}
