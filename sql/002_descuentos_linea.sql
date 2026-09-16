--
-- Descuentos por linea (articulos y servicios) en cotizaciones y pedidos.
--
-- El descuento vive en la LINEA y no en la cabecera porque el negocio lo aplica
-- por articulo/servicio ("el mantel va de cortesia", "20% en el mobiliario"), y
-- porque asi la trazabilidad sobrevive el paso cotizacion -> pedido: al
-- confirmar, cada linea se copia con su descuento tal cual quedo pactado.
--
-- Tipos (tipo_descuento):
--   NIN  sin descuento (default; es lo que queda en todas las filas historicas)
--   POR  porcentaje sobre el subtotal de la linea (valor_descuento = 0..100)
--   MON  monto fijo en Q sobre el subtotal de la linea
--   EXO  exoneracion total: la linea queda en Q 0.00 (valor_descuento se ignora)
--
-- Idempotente: se puede correr mas de una vez sin efecto.
-- Compatible con PostgreSQL 9.6 (ADD COLUMN IF NOT EXISTS existe desde 9.6).
--
-- IMPORTANTE: no altera ningun registro existente. Las columnas entran con
-- DEFAULT 'NIN' / 0, que es exactamente el comportamiento actual (sin descuento),
-- asi que los totales de cotizaciones y pedidos ya guardados no se mueven.
--

SET search_path = cana, public;


-- ─────────────────────────────────────────────────────────────
-- 1) Funciones de calculo
-- ─────────────────────────────────────────────────────────────
-- El calculo vive en la BD y no repetido en cada query porque el total del
-- pedido se arma en tres lugares distintos (estado de cuenta, reporteria y el
-- PDF de la cotizacion). Si la formula se copia, tarde o temprano se separan y
-- el saldo del pedido deja de cuadrar con el del reporte.

CREATE OR REPLACE FUNCTION cana.fn_descuento_linea(
    p_bruto numeric, p_tipo character varying, p_valor numeric)
RETURNS numeric AS $$
    SELECT CASE
        WHEN p_bruto IS NULL OR p_bruto <= 0 THEN 0::numeric
        WHEN p_tipo = 'EXO' THEN p_bruto
        WHEN p_tipo = 'POR' THEN
            round(p_bruto * LEAST(GREATEST(COALESCE(p_valor, 0), 0), 100) / 100, 2)
        WHEN p_tipo = 'MON' THEN
            LEAST(GREATEST(COALESCE(p_valor, 0), 0), p_bruto)
        ELSE 0::numeric
    END;
$$ LANGUAGE sql IMMUTABLE;

COMMENT ON FUNCTION cana.fn_descuento_linea(numeric, character varying, numeric) IS
    'Monto de descuento de una linea. Siempre entre 0 y el subtotal bruto: un porcentaje fuera de 0..100 o un monto mayor al bruto se recortan, nunca generan un neto negativo.';


CREATE OR REPLACE FUNCTION cana.fn_neto_linea(
    p_bruto numeric, p_tipo character varying, p_valor numeric)
RETURNS numeric AS $$
    SELECT COALESCE(p_bruto, 0) - cana.fn_descuento_linea(p_bruto, p_tipo, p_valor);
$$ LANGUAGE sql IMMUTABLE;

COMMENT ON FUNCTION cana.fn_neto_linea(numeric, character varying, numeric) IS
    'Subtotal de la linea ya con el descuento aplicado. Es la cifra que factura.';


CREATE OR REPLACE FUNCTION cana.fn_etiqueta_descuento(
    p_tipo character varying, p_valor numeric, p_motivo character varying)
RETURNS text AS $$
    SELECT CASE
        WHEN p_tipo = 'EXO' THEN 'Exonerado'
        WHEN p_tipo = 'POR' THEN '-' || CASE WHEN p_valor = trunc(p_valor)
                                             THEN to_char(p_valor, 'FM999999990')
                                             ELSE to_char(p_valor, 'FM999999990.00') END || '%'
        WHEN p_tipo = 'MON' THEN '-Q ' || to_char(COALESCE(p_valor, 0), 'FM999999990.00')
        ELSE NULL
    END
    || CASE WHEN p_tipo IN ('EXO', 'POR', 'MON')
                 AND p_motivo IS NOT NULL AND btrim(p_motivo) <> ''
            THEN ' (' || btrim(p_motivo) || ')'
            ELSE '' END;
$$ LANGUAGE sql IMMUTABLE;

COMMENT ON FUNCTION cana.fn_etiqueta_descuento(character varying, numeric, character varying) IS
    'Texto corto del descuento para documentos ("-20% (Cliente frecuente)", "Exonerado"). NULL cuando la linea no lleva descuento, para que el PDF no imprima una fila vacia.';


-- ─────────────────────────────────────────────────────────────
-- 2) Columnas de descuento en las cuatro tablas de detalle
-- ─────────────────────────────────────────────────────────────

ALTER TABLE cana.detalle_cotizacion
    ADD COLUMN IF NOT EXISTS tipo_descuento character varying(3) NOT NULL DEFAULT 'NIN',
    ADD COLUMN IF NOT EXISTS valor_descuento numeric(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS motivo_descuento character varying(200);

ALTER TABLE cana.detalle_servicio_cotizacion
    ADD COLUMN IF NOT EXISTS tipo_descuento character varying(3) NOT NULL DEFAULT 'NIN',
    ADD COLUMN IF NOT EXISTS valor_descuento numeric(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS motivo_descuento character varying(200);

ALTER TABLE cana.detalle_pedido
    ADD COLUMN IF NOT EXISTS tipo_descuento character varying(3) NOT NULL DEFAULT 'NIN',
    ADD COLUMN IF NOT EXISTS valor_descuento numeric(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS motivo_descuento character varying(200);

ALTER TABLE cana.detalle_servicio_pedido
    ADD COLUMN IF NOT EXISTS tipo_descuento character varying(3) NOT NULL DEFAULT 'NIN',
    ADD COLUMN IF NOT EXISTS valor_descuento numeric(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS motivo_descuento character varying(200);


-- ─────────────────────────────────────────────────────────────
-- 3) Reglas de integridad
-- ─────────────────────────────────────────────────────────────
-- Las filas historicas quedaron todas en ('NIN', 0), asi que los CHECK entran
-- sin validar nada en contra. Se agregan por DO porque ADD CONSTRAINT no
-- admite IF NOT EXISTS y este script tiene que poder re-correrse.

DO $$
DECLARE
    t text;
BEGIN
    FOREACH t IN ARRAY ARRAY['detalle_cotizacion', 'detalle_servicio_cotizacion',
                             'detalle_pedido', 'detalle_servicio_pedido']
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_constraint
                        WHERE conname = 'ck_' || t || '_tipo_descuento') THEN
            EXECUTE format(
                'ALTER TABLE cana.%I ADD CONSTRAINT %I CHECK (tipo_descuento IN (''NIN'',''POR'',''MON'',''EXO''))',
                t, 'ck_' || t || '_tipo_descuento');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_constraint
                        WHERE conname = 'ck_' || t || '_valor_descuento') THEN
            EXECUTE format(
                'ALTER TABLE cana.%I ADD CONSTRAINT %I CHECK (valor_descuento >= 0 AND (tipo_descuento <> ''POR'' OR valor_descuento <= 100))',
                t, 'ck_' || t || '_valor_descuento');
        END IF;
    END LOOP;
END
$$;


-- ─────────────────────────────────────────────────────────────
-- 4) Documentacion de columnas
-- ─────────────────────────────────────────────────────────────

COMMENT ON COLUMN cana.detalle_cotizacion.tipo_descuento IS 'NIN sin descuento | POR porcentaje | MON monto fijo | EXO exoneracion total de la linea.';
COMMENT ON COLUMN cana.detalle_cotizacion.valor_descuento IS 'Porcentaje (0..100) si tipo_descuento=POR, monto en Q si MON. 0 en NIN y EXO.';
COMMENT ON COLUMN cana.detalle_cotizacion.motivo_descuento IS 'Por que se otorgo el descuento. Se arrastra al pedido al confirmar la cotizacion.';

COMMENT ON COLUMN cana.detalle_servicio_cotizacion.tipo_descuento IS 'NIN sin descuento | POR porcentaje | MON monto fijo | EXO exoneracion total de la linea.';
COMMENT ON COLUMN cana.detalle_servicio_cotizacion.valor_descuento IS 'Porcentaje (0..100) si tipo_descuento=POR, monto en Q si MON. 0 en NIN y EXO.';
COMMENT ON COLUMN cana.detalle_servicio_cotizacion.motivo_descuento IS 'Por que se otorgo el descuento. Se arrastra al pedido al confirmar la cotizacion.';

COMMENT ON COLUMN cana.detalle_pedido.tipo_descuento IS 'NIN sin descuento | POR porcentaje | MON monto fijo | EXO exoneracion total de la linea.';
COMMENT ON COLUMN cana.detalle_pedido.valor_descuento IS 'Porcentaje (0..100) si tipo_descuento=POR, monto en Q si MON. 0 en NIN y EXO.';
COMMENT ON COLUMN cana.detalle_pedido.motivo_descuento IS 'Por que se otorgo el descuento. Viene copiado de la cotizacion cuando el pedido nacio de una.';

COMMENT ON COLUMN cana.detalle_servicio_pedido.tipo_descuento IS 'NIN sin descuento | POR porcentaje | MON monto fijo | EXO exoneracion total de la linea.';
COMMENT ON COLUMN cana.detalle_servicio_pedido.valor_descuento IS 'Porcentaje (0..100) si tipo_descuento=POR, monto en Q si MON. 0 en NIN y EXO.';
COMMENT ON COLUMN cana.detalle_servicio_pedido.motivo_descuento IS 'Por que se otorgo el descuento. Viene copiado de la cotizacion cuando el pedido nacio de una.';
