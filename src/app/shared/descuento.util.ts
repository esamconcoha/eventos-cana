import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { TipoDescuento } from '../interfaces/descuento';

/**
 * Cálculo y validación de los descuentos por línea, compartido por Cotizaciones
 * y Pedidos.
 *
 * Es el gemelo de `cana.fn_descuento_linea` en la BD (y de DescuentoConstants en
 * el backend): acá solo se usa para previsualizar el total mientras se llena el
 * formulario. La cifra que manda es siempre la que devuelve el backend, así que
 * si las fórmulas se separan lo que se ve al guardar deja de coincidir con lo
 * que se cobra.
 */

export const TIPOS_DESCUENTO: { codigo: TipoDescuento; label: string }[] = [
  { codigo: 'NIN', label: 'Sin descuento' },
  { codigo: 'POR', label: 'Porcentaje (%)' },
  { codigo: 'MON', label: 'Monto fijo (Q)' },
  { codigo: 'EXO', label: 'Exonerar línea' }
];

/** Los únicos tipos que piden un número; NIN y EXO no llevan valor. */
export function requiereValor(tipo: TipoDescuento | null | undefined): boolean {
  return tipo === 'POR' || tipo === 'MON';
}

/**
 * Monto descontado, recortado a [0, bruto]: un porcentaje o monto excesivo deja
 * la línea en Q 0.00, nunca en negativo (igual que la función de la BD).
 */
export function montoDescuento(bruto: number, tipo: TipoDescuento | null | undefined, valor: number | null | undefined): number {
  if (!bruto || bruto <= 0) { return 0; }
  const v = Number(valor) || 0;
  switch (tipo) {
    case 'EXO': return bruto;
    case 'POR': return Math.round(bruto * Math.min(Math.max(v, 0), 100) / 100 * 100) / 100;
    case 'MON': return Math.min(Math.max(v, 0), bruto);
    default:    return 0;
  }
}

export function netoLinea(bruto: number, tipo: TipoDescuento | null | undefined, valor: number | null | undefined): number {
  return bruto - montoDescuento(bruto, tipo, valor);
}

/** Texto corto para mostrar el descuento en listados y drawers ("-20%", "Exonerado"). */
export function etiquetaDescuento(tipo: TipoDescuento | null | undefined, valor: number | null | undefined): string {
  const v = Number(valor) || 0;
  switch (tipo) {
    case 'EXO': return 'Exonerado';
    case 'POR': return `-${v}%`;
    case 'MON': return `-Q ${v.toFixed(2)}`;
    default:    return '';
  }
}

/**
 * Validador de la fila completa (no de un control suelto) porque la regla cruza
 * dos campos: el valor solo es obligatorio, y solo tiene tope, según el tipo.
 */
export const validadorDescuentoLinea: ValidatorFn = (grupo: AbstractControl): ValidationErrors | null => {
  const tipo = grupo.get('tipoDescuento')?.value as TipoDescuento | null;
  if (!requiereValor(tipo)) { return null; }

  const valor = grupo.get('valorDescuento')?.value;
  if (valor === null || valor === undefined || valor === '' || Number(valor) <= 0) {
    return { descuentoSinValor: true };
  }
  if (tipo === 'POR' && Number(valor) > 100) {
    return { descuentoPorcentajeInvalido: true };
  }
  return null;
};

/** Normaliza una fila del formulario al shape que espera el backend. */
export function payloadDescuento(fila: any): {
  tipoDescuento: TipoDescuento;
  valorDescuento: number;
  motivoDescuento?: string;
} {
  const tipo: TipoDescuento = fila?.tipoDescuento || 'NIN';
  const motivo = (fila?.motivoDescuento ?? '').toString().trim();
  return {
    tipoDescuento: tipo,
    valorDescuento: requiereValor(tipo) ? Number(fila.valorDescuento) : 0,
    motivoDescuento: tipo === 'NIN' || !motivo ? undefined : motivo
  };
}
