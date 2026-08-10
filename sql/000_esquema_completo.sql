--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.24
-- Dumped by pg_dump version 9.6.24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: cana; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA cana;


--
-- Name: fn_trazabilidad_estado_cotizacion(); Type: FUNCTION; Schema: cana; Owner: -
--

CREATE FUNCTION cana.fn_trazabilidad_estado_cotizacion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado int8;
BEGIN
    IF (TG_OP = 'UPDATE' AND NEW.estado_cotizacion IS NOT DISTINCT FROM OLD.estado_cotizacion) THEN
        RETURN NEW;
    END IF;

    IF (NEW.estado_cotizacion IS NULL) THEN
        RETURN NEW;
    END IF;

    SELECT id_estado INTO v_id_estado
      FROM cana.estados
     WHERE tipo_estado = 'COT'
       AND codigo_estado = NEW.estado_cotizacion;

    IF (v_id_estado IS NULL) THEN
        RAISE EXCEPTION 'Estado de cotizacion "%" no existe en cana.estados (tipo_estado=COT)', NEW.estado_cotizacion;
    END IF;

    IF (TG_OP = 'UPDATE') THEN
        UPDATE cana.estados_cotizacion
           SET fecha_hora_fin = now()
         WHERE id_cotizacion = NEW.id_cotizacion
           AND fecha_hora_fin IS NULL;
    END IF;

    INSERT INTO cana.estados_cotizacion (
        id_cotizacion, id_estado, fecha_hora_inicio, fecha_hora_fin)
    VALUES (
        NEW.id_cotizacion, v_id_estado, now(), NULL);

    RETURN NEW;
END;
$$;


--
-- Name: fn_trazabilidad_estado_pedido(); Type: FUNCTION; Schema: cana; Owner: -
--

CREATE FUNCTION cana.fn_trazabilidad_estado_pedido() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado int8;
BEGIN
    -- En UPDATE, si el codigo no cambio no hay nada que historiar.
    IF (TG_OP = 'UPDATE' AND NEW.estado_pedido IS NOT DISTINCT FROM OLD.estado_pedido) THEN
        RETURN NEW;
    END IF;

    -- Defensivo: sin codigo no se historia (la columna deberia venir siempre).
    IF (NEW.estado_pedido IS NULL) THEN
        RETURN NEW;
    END IF;

    -- Resolver el id numerico del estado desde el codigo actual.
    -- Se filtra por tipo_estado para no colisionar con los codigos de cotizacion.
    SELECT id_estado INTO v_id_estado
      FROM cana.estados
     WHERE tipo_estado = 'EVE'
       AND codigo_estado = NEW.estado_pedido;

    -- Fallar rapido si el codigo no existe: id_estado es FK, insertar NULL
    -- reventaria con un error mucho menos claro que este.
    IF (v_id_estado IS NULL) THEN
        RAISE EXCEPTION 'Estado de pedido "%" no existe en cana.estados (tipo_estado=EVE)', NEW.estado_pedido;
    END IF;

    -- Cerrar el tramo abierto (solo aplica cuando ya habia uno, es decir UPDATE).
    IF (TG_OP = 'UPDATE') THEN
        UPDATE cana.estados_pedido
           SET fecha_hora_fin = now()
         WHERE correlativo_pedido = NEW.correlativo_pedido
           AND fecha_hora_fin IS NULL;
    END IF;

    -- Abrir el tramo nuevo (queda abierto hasta el proximo cambio).
    INSERT INTO cana.estados_pedido (
        correlativo_pedido, id_estado, fecha_hora_inicio, fecha_hora_fin)
    VALUES (
        NEW.correlativo_pedido, v_id_estado, now(), NULL);

    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: catalogos_cana; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.catalogos_cana (
    id_catalogo bigint NOT NULL,
    id_tipo_catalogo integer,
    codigo character varying(10),
    nombre character varying(50),
    descripcion character varying(75),
    estado_registro boolean DEFAULT true
);


--
-- Name: categorias_servicio; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.categorias_servicio (
    id_categoria integer NOT NULL,
    nombre_categoria character varying(100) NOT NULL,
    tipo_categoria character varying(50) NOT NULL,
    estado_registro boolean DEFAULT true
);


--
-- Name: categorias_servicio_id_categoria_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.categorias_servicio_id_categoria_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categorias_servicio_id_categoria_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.categorias_servicio_id_categoria_seq OWNED BY cana.categorias_servicio.id_categoria;


--
-- Name: cotizaciones; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.cotizaciones (
    id_cotizacion bigint NOT NULL,
    nombre_cliente_cotizacion character varying(50),
    direccion_cliente_cotizacion character varying(150),
    telefono_cliente_cotizacion bigint,
    fecha_cotizacion date,
    cotizacion_confirmada boolean DEFAULT false,
    dpi_nit_usuario_cotizacion character varying(16),
    estado_cotizacion character varying(10),
    fecha_hora_evento timestamp without time zone,
    cod_tipo_evento character varying(10)
);


--
-- Name: cotizaciones_id_cotizacion_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.cotizaciones_id_cotizacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cotizaciones_id_cotizacion_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.cotizaciones_id_cotizacion_seq OWNED BY cana.cotizaciones.id_cotizacion;


--
-- Name: detalle_cotizacion; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_cotizacion (
    id_detalle_cotizacion bigint NOT NULL,
    id_item integer,
    cantidad_item_cotizacion numeric(7,2),
    id_cotizacion bigint
);


--
-- Name: detalle_cotizacion_id_detalle_cotizacion_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_cotizacion_id_detalle_cotizacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_cotizacion_id_detalle_cotizacion_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_cotizacion_id_detalle_cotizacion_seq OWNED BY cana.detalle_cotizacion.id_detalle_cotizacion;


--
-- Name: detalle_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_pedido (
    id_detalle bigint NOT NULL,
    id_item integer,
    cantidad_item_pedido numeric(7,2),
    correlativo_pedido character varying(20)
);


--
-- Name: detalle_pedido_id_detalle_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_pedido_id_detalle_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_pedido_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_pedido_id_detalle_seq OWNED BY cana.detalle_pedido.id_detalle;


--
-- Name: detalle_servicio_cotizacion; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_servicio_cotizacion (
    id_detalle_serv_cotiz integer NOT NULL,
    id_cotizacion integer NOT NULL,
    id_servicio integer NOT NULL,
    cantidad numeric(10,2) DEFAULT 1 NOT NULL,
    precio_cotizado numeric(10,2) NOT NULL,
    especificaciones text
);


--
-- Name: detalle_servicio_cotizacion_id_detalle_serv_cotiz_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_servicio_cotizacion_id_detalle_serv_cotiz_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_servicio_cotizacion_id_detalle_serv_cotiz_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_servicio_cotizacion_id_detalle_serv_cotiz_seq OWNED BY cana.detalle_servicio_cotizacion.id_detalle_serv_cotiz;


--
-- Name: detalle_servicio_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_servicio_pedido (
    id_detalle_serv_pedido integer NOT NULL,
    correlativo_pedido character varying(50) NOT NULL,
    id_servicio integer NOT NULL,
    cantidad numeric(10,2) DEFAULT 1 NOT NULL,
    precio_acordado numeric(10,2) NOT NULL,
    especificaciones text,
    fecha_realizado timestamp without time zone
);


--
-- Name: COLUMN detalle_servicio_pedido.fecha_realizado; Type: COMMENT; Schema: cana; Owner: -
--

COMMENT ON COLUMN cana.detalle_servicio_pedido.fecha_realizado IS 'Momento en que se confirmo realizado el servicio (montaje, decoracion...). NULL = pendiente.';


--
-- Name: detalle_servicio_pedido_id_detalle_serv_pedido_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_servicio_pedido_id_detalle_serv_pedido_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_servicio_pedido_id_detalle_serv_pedido_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_servicio_pedido_id_detalle_serv_pedido_seq OWNED BY cana.detalle_servicio_pedido.id_detalle_serv_pedido;


--
-- Name: detalle_viaje; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_viaje (
    id_viaje bigint NOT NULL,
    id_entrega bigint NOT NULL,
    fecha_inicio_viaje timestamp without time zone,
    fecha_fin_viaje timestamp without time zone
);


--
-- Name: detalle_viaje_id_viaje_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_viaje_id_viaje_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_viaje_id_viaje_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_viaje_id_viaje_seq OWNED BY cana.detalle_viaje.id_viaje;


--
-- Name: detalle_viaje_items; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.detalle_viaje_items (
    id_detalle bigint NOT NULL,
    id_item bigint NOT NULL,
    id_detalle_viaje bigint NOT NULL,
    cantidad_item numeric(9,2)
);


--
-- Name: detalle_viaje_items_id_detalle_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.detalle_viaje_items_id_detalle_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detalle_viaje_items_id_detalle_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.detalle_viaje_items_id_detalle_seq OWNED BY cana.detalle_viaje_items.id_detalle;


--
-- Name: direcciones; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.direcciones (
    id_direccion bigint NOT NULL,
    direccion character varying(150),
    nit_dpi character varying(16)
);


--
-- Name: direcciones_id_direccion_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.direcciones_id_direccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: direcciones_id_direccion_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.direcciones_id_direccion_seq OWNED BY cana.direcciones.id_direccion;


--
-- Name: documentos_cotizacion; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.documentos_cotizacion (
    id_documento bigint NOT NULL,
    id_cotizacion bigint NOT NULL,
    nombre_documento character varying(150),
    tipo_documento character varying(30),
    content_type character varying(60),
    contenido bytea,
    fecha_generacion timestamp without time zone,
    usuario_genero character varying(50),
    estado_registro boolean DEFAULT true
);


--
-- Name: documentos_cotizacion_id_documento_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.documentos_cotizacion_id_documento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: documentos_cotizacion_id_documento_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.documentos_cotizacion_id_documento_seq OWNED BY cana.documentos_cotizacion.id_documento;


--
-- Name: documentos_entrega; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.documentos_entrega (
    id_documento bigint NOT NULL,
    id_entrega bigint NOT NULL,
    nombre_documento character varying(150),
    tipo_documento character varying(30),
    content_type character varying(60),
    contenido bytea,
    fecha_generacion timestamp without time zone,
    usuario_genero character varying(50),
    estado_registro boolean DEFAULT true
);


--
-- Name: documentos_entrega_id_documento_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.documentos_entrega_id_documento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: documentos_entrega_id_documento_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.documentos_entrega_id_documento_seq OWNED BY cana.documentos_entrega.id_documento;


--
-- Name: entregas_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.entregas_pedido (
    id_entrega bigint NOT NULL,
    correlativo_pedido character varying(20) NOT NULL,
    cantidad_viajes_aproximados integer,
    cantidad_viajes_reales integer DEFAULT 0,
    pedido_finalizado boolean DEFAULT false,
    fecha_inicio_entrega timestamp without time zone,
    fecha_fin_entrega timestamp without time zone,
    tipo_movimiento character varying(3) DEFAULT 'ENT'::character varying NOT NULL,
    CONSTRAINT entregas_pedido_tipo_movimiento_chk CHECK (((tipo_movimiento)::text = ANY ((ARRAY['ENT'::character varying, 'REC'::character varying])::text[])))
);


--
-- Name: COLUMN entregas_pedido.tipo_movimiento; Type: COMMENT; Schema: cana; Owner: -
--

COMMENT ON COLUMN cana.entregas_pedido.tipo_movimiento IS 'ENT = entrega (ida), REC = recoleccion (vuelta). Un pedido tiene a lo sumo una fila de cada tipo.';


--
-- Name: entregas_pedido_id_entrega_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.entregas_pedido_id_entrega_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entregas_pedido_id_entrega_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.entregas_pedido_id_entrega_seq OWNED BY cana.entregas_pedido.id_entrega;


--
-- Name: estados; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.estados (
    id_estado integer NOT NULL,
    tipo_estado character varying(20),
    codigo_estado character varying(10),
    nombre_estado character varying(50),
    descripcion_estado character varying(100),
    estado_registro boolean DEFAULT true
);


--
-- Name: estados_cotizacion; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.estados_cotizacion (
    correlativo_estadoc bigint NOT NULL,
    id_cotizacion integer,
    id_estado integer,
    fecha_hora_inicio timestamp without time zone,
    fecha_hora_fin timestamp without time zone
);


--
-- Name: estados_cotizacion_correlativo_estadoc_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.estados_cotizacion_correlativo_estadoc_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: estados_cotizacion_correlativo_estadoc_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.estados_cotizacion_correlativo_estadoc_seq OWNED BY cana.estados_cotizacion.correlativo_estadoc;


--
-- Name: estados_pago_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.estados_pago_pedido (
    correlativo_estado_pago bigint NOT NULL,
    correlativo_pedido character varying(50) NOT NULL,
    estado_pago character varying(10) NOT NULL,
    fecha_hora_inicio timestamp without time zone DEFAULT now() NOT NULL,
    fecha_hora_fin timestamp without time zone
);


--
-- Name: estados_pago_pedido_correlativo_estado_pago_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.estados_pago_pedido_correlativo_estado_pago_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: estados_pago_pedido_correlativo_estado_pago_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.estados_pago_pedido_correlativo_estado_pago_seq OWNED BY cana.estados_pago_pedido.correlativo_estado_pago;


--
-- Name: estados_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.estados_pedido (
    correlativo_estado bigint NOT NULL,
    correlativo_pedido character varying(20),
    id_estado integer,
    fecha_hora_inicio timestamp without time zone,
    fecha_hora_fin timestamp without time zone
);


--
-- Name: estados_pedido_correlativo_estado_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.estados_pedido_correlativo_estado_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: estados_pedido_correlativo_estado_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.estados_pedido_correlativo_estado_seq OWNED BY cana.estados_pedido.correlativo_estado;


--
-- Name: items_cana; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.items_cana (
    id_item bigint NOT NULL,
    id_tipo_item integer,
    costo_item numeric(9,2),
    descripcion_item character varying(150),
    cantidad_item bigint,
    observaciones character varying(200),
    cantidad_faltantes integer,
    estado_item boolean DEFAULT true
);


--
-- Name: items_cana_id_item_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.items_cana_id_item_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: items_cana_id_item_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.items_cana_id_item_seq OWNED BY cana.items_cana.id_item;


--
-- Name: mantenimiento_salones; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.mantenimiento_salones (
    id_salon integer NOT NULL,
    nombre_salon character varying(150),
    direccion_salon character varying(150),
    estado_salon boolean DEFAULT true
);


--
-- Name: mantenimiento_salones_id_salon_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.mantenimiento_salones_id_salon_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mantenimiento_salones_id_salon_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.mantenimiento_salones_id_salon_seq OWNED BY cana.mantenimiento_salones.id_salon;


--
-- Name: pagos_pedido; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.pagos_pedido (
    id_pago bigint NOT NULL,
    correlativo_pedido character varying(50) NOT NULL,
    monto_pago double precision NOT NULL,
    fecha_pago timestamp without time zone DEFAULT now() NOT NULL,
    tipo_pago character varying(10) NOT NULL,
    metodo_pago character varying(20) NOT NULL,
    referencia_pago character varying(100),
    usuario_registro character varying(50) NOT NULL,
    estado_registro boolean DEFAULT true NOT NULL,
    fecha_creo timestamp without time zone DEFAULT now() NOT NULL
);


--
-- Name: pagos_pedido_id_pago_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.pagos_pedido_id_pago_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pagos_pedido_id_pago_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.pagos_pedido_id_pago_seq OWNED BY cana.pagos_pedido.id_pago;


--
-- Name: pedidos_cana; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.pedidos_cana (
    correlativo_pedido character varying(20) NOT NULL,
    direccion_pedido character varying(150),
    estado_pedido character varying(10) DEFAULT true,
    dpi_usuario_pedido character varying(16),
    fecha_evento timestamp without time zone,
    fecha_entrega date,
    salon_entrega bigint,
    fecha_confirmacion_pedido date,
    fecha_recogido date,
    pagado boolean DEFAULT false,
    usuario_interno_pedido character varying(16),
    cod_tipo_evento character varying(10),
    estado_pago character varying(10),
    id_cotizacion integer,
    telefono_cliente_pedido bigint,
    nombre_cliente_pedidoprov character varying(150)
);


--
-- Name: COLUMN pedidos_cana.fecha_entrega; Type: COMMENT; Schema: cana; Owner: -
--

COMMENT ON COLUMN cana.pedidos_cana.fecha_entrega IS 'Fecha PACTADA de entrega (agenda). Lo realmente ejecutado vive en la fila ENT de entregas_pedido.';


--
-- Name: COLUMN pedidos_cana.fecha_recogido; Type: COMMENT; Schema: cana; Owner: -
--

COMMENT ON COLUMN cana.pedidos_cana.fecha_recogido IS 'Fecha PROGRAMADA de recoleccion (agenda). Lo realmente ejecutado vive en la fila REC de entregas_pedido.';


--
-- Name: representantes_empresas; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.representantes_empresas (
    id_representacion bigint NOT NULL,
    dpi_usuario character varying(16),
    nit_empresa character varying(12)
);


--
-- Name: representantes_empresas_id_representacion_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.representantes_empresas_id_representacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: representantes_empresas_id_representacion_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.representantes_empresas_id_representacion_seq OWNED BY cana.representantes_empresas.id_representacion;


--
-- Name: servicios_decoracion; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.servicios_decoracion (
    id_servicio integer NOT NULL,
    id_categoria integer NOT NULL,
    nombre_servicio character varying(150) NOT NULL,
    descripcion_servicio text,
    unidad_medida character varying(30) NOT NULL,
    requiere_detalle boolean DEFAULT false,
    estado_servicio boolean DEFAULT true
);


--
-- Name: servicios_decoracion_id_servicio_seq; Type: SEQUENCE; Schema: cana; Owner: -
--

CREATE SEQUENCE cana.servicios_decoracion_id_servicio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: servicios_decoracion_id_servicio_seq; Type: SEQUENCE OWNED BY; Schema: cana; Owner: -
--

ALTER SEQUENCE cana.servicios_decoracion_id_servicio_seq OWNED BY cana.servicios_decoracion.id_servicio;


--
-- Name: tipo_catalogos_cana; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.tipo_catalogos_cana (
    id_tipo_catalogo integer NOT NULL,
    nombre_catalogo character varying(75),
    descripcion_catalogo character varying(75),
    fecha_creo timestamp without time zone,
    usuario_creo character varying(20),
    estado_registro boolean DEFAULT true
);


--
-- Name: usuarios; Type: TABLE; Schema: cana; Owner: -
--

CREATE TABLE cana.usuarios (
    dpi_nit_usuario character varying(16) NOT NULL,
    nombres_usuario character varying(50),
    apellidos_usuario character varying(50),
    telefono_usuario bigint,
    correo character varying(50),
    es_representante boolean DEFAULT false,
    estado_usuario boolean DEFAULT true,
    rol integer,
    es_empresa boolean DEFAULT false,
    contrasenia character varying(255),
    usuario_creo character varying(16)
);


--
-- Name: categorias_servicio id_categoria; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.categorias_servicio ALTER COLUMN id_categoria SET DEFAULT nextval('cana.categorias_servicio_id_categoria_seq'::regclass);


--
-- Name: cotizaciones id_cotizacion; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.cotizaciones ALTER COLUMN id_cotizacion SET DEFAULT nextval('cana.cotizaciones_id_cotizacion_seq'::regclass);


--
-- Name: detalle_cotizacion id_detalle_cotizacion; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_cotizacion ALTER COLUMN id_detalle_cotizacion SET DEFAULT nextval('cana.detalle_cotizacion_id_detalle_cotizacion_seq'::regclass);


--
-- Name: detalle_pedido id_detalle; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_pedido ALTER COLUMN id_detalle SET DEFAULT nextval('cana.detalle_pedido_id_detalle_seq'::regclass);


--
-- Name: detalle_servicio_cotizacion id_detalle_serv_cotiz; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_cotizacion ALTER COLUMN id_detalle_serv_cotiz SET DEFAULT nextval('cana.detalle_servicio_cotizacion_id_detalle_serv_cotiz_seq'::regclass);


--
-- Name: detalle_servicio_pedido id_detalle_serv_pedido; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_pedido ALTER COLUMN id_detalle_serv_pedido SET DEFAULT nextval('cana.detalle_servicio_pedido_id_detalle_serv_pedido_seq'::regclass);


--
-- Name: detalle_viaje id_viaje; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje ALTER COLUMN id_viaje SET DEFAULT nextval('cana.detalle_viaje_id_viaje_seq'::regclass);


--
-- Name: detalle_viaje_items id_detalle; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje_items ALTER COLUMN id_detalle SET DEFAULT nextval('cana.detalle_viaje_items_id_detalle_seq'::regclass);


--
-- Name: direcciones id_direccion; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.direcciones ALTER COLUMN id_direccion SET DEFAULT nextval('cana.direcciones_id_direccion_seq'::regclass);


--
-- Name: documentos_cotizacion id_documento; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_cotizacion ALTER COLUMN id_documento SET DEFAULT nextval('cana.documentos_cotizacion_id_documento_seq'::regclass);


--
-- Name: documentos_entrega id_documento; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_entrega ALTER COLUMN id_documento SET DEFAULT nextval('cana.documentos_entrega_id_documento_seq'::regclass);


--
-- Name: entregas_pedido id_entrega; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.entregas_pedido ALTER COLUMN id_entrega SET DEFAULT nextval('cana.entregas_pedido_id_entrega_seq'::regclass);


--
-- Name: estados_cotizacion correlativo_estadoc; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_cotizacion ALTER COLUMN correlativo_estadoc SET DEFAULT nextval('cana.estados_cotizacion_correlativo_estadoc_seq'::regclass);


--
-- Name: estados_pago_pedido correlativo_estado_pago; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pago_pedido ALTER COLUMN correlativo_estado_pago SET DEFAULT nextval('cana.estados_pago_pedido_correlativo_estado_pago_seq'::regclass);


--
-- Name: estados_pedido correlativo_estado; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pedido ALTER COLUMN correlativo_estado SET DEFAULT nextval('cana.estados_pedido_correlativo_estado_seq'::regclass);


--
-- Name: items_cana id_item; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.items_cana ALTER COLUMN id_item SET DEFAULT nextval('cana.items_cana_id_item_seq'::regclass);


--
-- Name: mantenimiento_salones id_salon; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.mantenimiento_salones ALTER COLUMN id_salon SET DEFAULT nextval('cana.mantenimiento_salones_id_salon_seq'::regclass);


--
-- Name: pagos_pedido id_pago; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pagos_pedido ALTER COLUMN id_pago SET DEFAULT nextval('cana.pagos_pedido_id_pago_seq'::regclass);


--
-- Name: representantes_empresas id_representacion; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.representantes_empresas ALTER COLUMN id_representacion SET DEFAULT nextval('cana.representantes_empresas_id_representacion_seq'::regclass);


--
-- Name: servicios_decoracion id_servicio; Type: DEFAULT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.servicios_decoracion ALTER COLUMN id_servicio SET DEFAULT nextval('cana.servicios_decoracion_id_servicio_seq'::regclass);


--
-- Name: catalogos_cana catalogos_cana_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.catalogos_cana
    ADD CONSTRAINT catalogos_cana_pkey PRIMARY KEY (id_catalogo);


--
-- Name: categorias_servicio categorias_servicio_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.categorias_servicio
    ADD CONSTRAINT categorias_servicio_pkey PRIMARY KEY (id_categoria);


--
-- Name: cotizaciones cotizaciones_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.cotizaciones
    ADD CONSTRAINT cotizaciones_pkey PRIMARY KEY (id_cotizacion);


--
-- Name: detalle_cotizacion detalle_cotizacion_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_cotizacion
    ADD CONSTRAINT detalle_cotizacion_pkey PRIMARY KEY (id_detalle_cotizacion);


--
-- Name: detalle_pedido detalle_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_pedido
    ADD CONSTRAINT detalle_pedido_pkey PRIMARY KEY (id_detalle);


--
-- Name: detalle_servicio_cotizacion detalle_servicio_cotizacion_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_cotizacion
    ADD CONSTRAINT detalle_servicio_cotizacion_pkey PRIMARY KEY (id_detalle_serv_cotiz);


--
-- Name: detalle_servicio_pedido detalle_servicio_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_pedido
    ADD CONSTRAINT detalle_servicio_pedido_pkey PRIMARY KEY (id_detalle_serv_pedido);


--
-- Name: detalle_viaje_items detalle_viaje_items_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje_items
    ADD CONSTRAINT detalle_viaje_items_pkey PRIMARY KEY (id_detalle);


--
-- Name: detalle_viaje detalle_viaje_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje
    ADD CONSTRAINT detalle_viaje_pkey PRIMARY KEY (id_viaje);


--
-- Name: direcciones direcciones_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.direcciones
    ADD CONSTRAINT direcciones_pkey PRIMARY KEY (id_direccion);


--
-- Name: documentos_cotizacion documentos_cotizacion_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_cotizacion
    ADD CONSTRAINT documentos_cotizacion_pkey PRIMARY KEY (id_documento);


--
-- Name: documentos_entrega documentos_entrega_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_entrega
    ADD CONSTRAINT documentos_entrega_pkey PRIMARY KEY (id_documento);


--
-- Name: entregas_pedido entregas_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.entregas_pedido
    ADD CONSTRAINT entregas_pedido_pkey PRIMARY KEY (id_entrega);


--
-- Name: estados_cotizacion estados_cotizacion_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_cotizacion
    ADD CONSTRAINT estados_cotizacion_pkey PRIMARY KEY (correlativo_estadoc);


--
-- Name: estados_pago_pedido estados_pago_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pago_pedido
    ADD CONSTRAINT estados_pago_pedido_pkey PRIMARY KEY (correlativo_estado_pago);


--
-- Name: estados_pedido estados_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pedido
    ADD CONSTRAINT estados_pedido_pkey PRIMARY KEY (correlativo_estado);


--
-- Name: estados estados_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados
    ADD CONSTRAINT estados_pkey PRIMARY KEY (id_estado);


--
-- Name: items_cana items_cana_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.items_cana
    ADD CONSTRAINT items_cana_pkey PRIMARY KEY (id_item);


--
-- Name: mantenimiento_salones mantenimiento_salones_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.mantenimiento_salones
    ADD CONSTRAINT mantenimiento_salones_pkey PRIMARY KEY (id_salon);


--
-- Name: pagos_pedido pagos_pedido_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pagos_pedido
    ADD CONSTRAINT pagos_pedido_pkey PRIMARY KEY (id_pago);


--
-- Name: pedidos_cana pedidos_cana_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pedidos_cana
    ADD CONSTRAINT pedidos_cana_pkey PRIMARY KEY (correlativo_pedido);


--
-- Name: representantes_empresas representantes_empresas_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.representantes_empresas
    ADD CONSTRAINT representantes_empresas_pkey PRIMARY KEY (id_representacion);


--
-- Name: servicios_decoracion servicios_decoracion_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.servicios_decoracion
    ADD CONSTRAINT servicios_decoracion_pkey PRIMARY KEY (id_servicio);


--
-- Name: tipo_catalogos_cana tipo_catalogos_cana_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.tipo_catalogos_cana
    ADD CONSTRAINT tipo_catalogos_cana_pkey PRIMARY KEY (id_tipo_catalogo);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (dpi_nit_usuario);


--
-- Name: idx_detalle_viaje_fecha_inicio; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_detalle_viaje_fecha_inicio ON cana.detalle_viaje USING btree (fecha_inicio_viaje);


--
-- Name: idx_detalle_viaje_id_entrega; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_detalle_viaje_id_entrega ON cana.detalle_viaje USING btree (id_entrega);


--
-- Name: idx_detalle_viaje_items_id_detalle_viaje; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_detalle_viaje_items_id_detalle_viaje ON cana.detalle_viaje_items USING btree (id_detalle_viaje);


--
-- Name: idx_documentos_entrega_id_entrega; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_documentos_entrega_id_entrega ON cana.documentos_entrega USING btree (id_entrega);


--
-- Name: idx_entregas_pedido_correlativo_tipo; Type: INDEX; Schema: cana; Owner: -
--

CREATE UNIQUE INDEX idx_entregas_pedido_correlativo_tipo ON cana.entregas_pedido USING btree (correlativo_pedido, tipo_movimiento);


--
-- Name: idx_estados_pago_pedido_correlativo_pedido; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_estados_pago_pedido_correlativo_pedido ON cana.estados_pago_pedido USING btree (correlativo_pedido);


--
-- Name: idx_pagos_pedido_correlativo_pedido; Type: INDEX; Schema: cana; Owner: -
--

CREATE INDEX idx_pagos_pedido_correlativo_pedido ON cana.pagos_pedido USING btree (correlativo_pedido);


--
-- Name: cotizaciones trg_trazabilidad_estado_cotizacion; Type: TRIGGER; Schema: cana; Owner: -
--

CREATE TRIGGER trg_trazabilidad_estado_cotizacion AFTER INSERT OR UPDATE OF estado_cotizacion ON cana.cotizaciones FOR EACH ROW EXECUTE PROCEDURE cana.fn_trazabilidad_estado_cotizacion();


--
-- Name: pedidos_cana trg_trazabilidad_estado_pedido; Type: TRIGGER; Schema: cana; Owner: -
--

CREATE TRIGGER trg_trazabilidad_estado_pedido AFTER INSERT OR UPDATE OF estado_pedido ON cana.pedidos_cana FOR EACH ROW EXECUTE PROCEDURE cana.fn_trazabilidad_estado_pedido();


--
-- Name: estados_pedido correlativo_catalogoestado_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pedido
    ADD CONSTRAINT correlativo_catalogoestado_fk FOREIGN KEY (id_estado) REFERENCES cana.estados(id_estado);


--
-- Name: estados_pedido correlativo_estado_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pedido
    ADD CONSTRAINT correlativo_estado_fk FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: entregas_pedido correlativo_pedido_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.entregas_pedido
    ADD CONSTRAINT correlativo_pedido_fk FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: detalle_cotizacion detalle_cotizacion_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_cotizacion
    ADD CONSTRAINT detalle_cotizacion_fk FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones(id_cotizacion);


--
-- Name: detalle_servicio_cotizacion detalle_servicio_cotizacion_id_cotizacion_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_cotizacion
    ADD CONSTRAINT detalle_servicio_cotizacion_id_cotizacion_fkey FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones(id_cotizacion);


--
-- Name: detalle_servicio_cotizacion detalle_servicio_cotizacion_id_servicio_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_cotizacion
    ADD CONSTRAINT detalle_servicio_cotizacion_id_servicio_fkey FOREIGN KEY (id_servicio) REFERENCES cana.servicios_decoracion(id_servicio);


--
-- Name: detalle_servicio_pedido detalle_servicio_pedido_correlativo_pedido_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_pedido
    ADD CONSTRAINT detalle_servicio_pedido_correlativo_pedido_fkey FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: detalle_servicio_pedido detalle_servicio_pedido_id_servicio_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_servicio_pedido
    ADD CONSTRAINT detalle_servicio_pedido_id_servicio_fkey FOREIGN KEY (id_servicio) REFERENCES cana.servicios_decoracion(id_servicio);


--
-- Name: detalle_viaje_items detalle_viaje_items_item_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje_items
    ADD CONSTRAINT detalle_viaje_items_item_fk FOREIGN KEY (id_item) REFERENCES cana.items_cana(id_item);


--
-- Name: documentos_cotizacion documentos_cotizacion_id_cotizacion_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_cotizacion
    ADD CONSTRAINT documentos_cotizacion_id_cotizacion_fkey FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones(id_cotizacion);


--
-- Name: documentos_entrega documentos_entrega_id_entrega_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.documentos_entrega
    ADD CONSTRAINT documentos_entrega_id_entrega_fkey FOREIGN KEY (id_entrega) REFERENCES cana.entregas_pedido(id_entrega);


--
-- Name: representantes_empresas dpi_usuario_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.representantes_empresas
    ADD CONSTRAINT dpi_usuario_fk FOREIGN KEY (dpi_usuario) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: detalle_viaje entrega_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje
    ADD CONSTRAINT entrega_fk FOREIGN KEY (id_entrega) REFERENCES cana.entregas_pedido(id_entrega);


--
-- Name: usuarios fk_catalogo_roles; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.usuarios
    ADD CONSTRAINT fk_catalogo_roles FOREIGN KEY (rol) REFERENCES cana.catalogos_cana(id_catalogo);


--
-- Name: pedidos_cana fk_cotizacion_evento; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pedidos_cana
    ADD CONSTRAINT fk_cotizacion_evento FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones(id_cotizacion);


--
-- Name: estados_pago_pedido fk_estados_pago_pedido_pedido; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_pago_pedido
    ADD CONSTRAINT fk_estados_pago_pedido_pedido FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: pagos_pedido fk_pagos_pedido_pedido; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pagos_pedido
    ADD CONSTRAINT fk_pagos_pedido_pedido FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: pagos_pedido fk_pagos_pedido_usuario; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pagos_pedido
    ADD CONSTRAINT fk_pagos_pedido_usuario FOREIGN KEY (usuario_registro) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: pedidos_cana fk_salones_eventos; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pedidos_cana
    ADD CONSTRAINT fk_salones_eventos FOREIGN KEY (salon_entrega) REFERENCES cana.mantenimiento_salones(id_salon);


--
-- Name: estados_cotizacion id_cotizacion_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_cotizacion
    ADD CONSTRAINT id_cotizacion_fk FOREIGN KEY (id_cotizacion) REFERENCES cana.cotizaciones(id_cotizacion);


--
-- Name: estados_cotizacion id_estadoc_fl; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.estados_cotizacion
    ADD CONSTRAINT id_estadoc_fl FOREIGN KEY (id_estado) REFERENCES cana.estados(id_estado);


--
-- Name: detalle_pedido item_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_pedido
    ADD CONSTRAINT item_fk FOREIGN KEY (id_item) REFERENCES cana.items_cana(id_item);


--
-- Name: representantes_empresas nit_empresa_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.representantes_empresas
    ADD CONSTRAINT nit_empresa_fk FOREIGN KEY (nit_empresa) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: detalle_pedido pedidos_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_pedido
    ADD CONSTRAINT pedidos_fk FOREIGN KEY (correlativo_pedido) REFERENCES cana.pedidos_cana(correlativo_pedido);


--
-- Name: servicios_decoracion servicios_decoracion_id_categoria_fkey; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.servicios_decoracion
    ADD CONSTRAINT servicios_decoracion_id_categoria_fkey FOREIGN KEY (id_categoria) REFERENCES cana.categorias_servicio(id_categoria);


--
-- Name: catalogos_cana tipo_catalogo_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.catalogos_cana
    ADD CONSTRAINT tipo_catalogo_fk FOREIGN KEY (id_tipo_catalogo) REFERENCES cana.tipo_catalogos_cana(id_tipo_catalogo);


--
-- Name: cotizaciones usuario_cotizaciones_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.cotizaciones
    ADD CONSTRAINT usuario_cotizaciones_fk FOREIGN KEY (dpi_nit_usuario_cotizacion) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: direcciones usuario_direccion_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.direcciones
    ADD CONSTRAINT usuario_direccion_fk FOREIGN KEY (nit_dpi) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: pedidos_cana usuario_pedido_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.pedidos_cana
    ADD CONSTRAINT usuario_pedido_fk FOREIGN KEY (usuario_interno_pedido) REFERENCES cana.usuarios(dpi_nit_usuario);


--
-- Name: detalle_viaje_items viaje_fk; Type: FK CONSTRAINT; Schema: cana; Owner: -
--

ALTER TABLE ONLY cana.detalle_viaje_items
    ADD CONSTRAINT viaje_fk FOREIGN KEY (id_detalle_viaje) REFERENCES cana.detalle_viaje(id_viaje);


--
-- PostgreSQL database dump complete
--

