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
-- Data for Name: tipo_catalogos_cana; Type: TABLE DATA; Schema: cana; Owner: postgres
--

INSERT INTO cana.tipo_catalogos_cana (id_tipo_catalogo, nombre_catalogo, descripcion_catalogo, fecha_creo, usuario_creo, estado_registro) VALUES (1, 'ROLES', 'Listado de roles permitidos en el sistema', '2026-03-25 22:08:30.158374', 'ADMIN', true);
INSERT INTO cana.tipo_catalogos_cana (id_tipo_catalogo, nombre_catalogo, descripcion_catalogo, fecha_creo, usuario_creo, estado_registro) VALUES (2, 'TIPO_ITEM', 'Tipo de items de inventario', '2026-04-03 23:32:58.925452', 'ADMIN', true);
INSERT INTO cana.tipo_catalogos_cana (id_tipo_catalogo, nombre_catalogo, descripcion_catalogo, fecha_creo, usuario_creo, estado_registro) VALUES (3, 'TIPO_EVENTOS', 'tipos de eventos que se realizan', '2026-07-21 22:08:30.158', 'ADMIN', true);
INSERT INTO cana.tipo_catalogos_cana (id_tipo_catalogo, nombre_catalogo, descripcion_catalogo, fecha_creo, usuario_creo, estado_registro) VALUES (4, 'TIPO_PAGO', 'Tipo de pago a hacer', '2026-07-23 22:08:30.158', 'ADMIN', true);
INSERT INTO cana.tipo_catalogos_cana (id_tipo_catalogo, nombre_catalogo, descripcion_catalogo, fecha_creo, usuario_creo, estado_registro) VALUES (5, 'MODALIDADES_PAGO', 'modalidad en la que se realiza un pago', '2026-07-23 22:08:30.158', 'ADMIN', true);


--
-- Data for Name: catalogos_cana; Type: TABLE DATA; Schema: cana; Owner: postgres
--

INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (1, 1, 'A', 'Administrador/a General', 'Administrador/a general de eventos caná', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (2, 1, 'JO', 'Jefe Operativo', 'Encargado de la logística y entregas', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (3, 1, 'C', 'Contador', 'Encargado de pagos, cobros y emisión de facturas', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (4, 1, 'O', 'Operativo', 'Personal requerido para la operación de entregas y montajes', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (6, 2, 'C', 'Cristalería', 'Cristalería', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (7, 2, 'MA', 'Mantelería', 'Mantelería', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (5, 2, 'M', 'Mobiliario', 'Mobiliario', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (8, 2, 'OT', 'Otros', 'Otro tipo de item de inventario', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (9, 3, 'BO', 'Boda', 'Boda', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (10, 3, 'XV', 'XV Años', 'XV Años', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (11, 3, 'AV', 'Aniversario', 'Aniversario', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (12, 3, 'CP', 'Cumpleaños', 'Cumpleaños', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (13, 3, 'EC', 'Evento Corporativo', 'Evento Corporativo', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (14, 3, 'ER', 'Evento Religioso', 'Evento Religioso', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (15, 3, 'EF', 'Evento Familar', 'Evento Familiar', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (16, 3, 'GR', 'Graduación', 'Graduación', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (17, 3, 'ECA', 'Evento Caritativo', 'Evento Caritativo', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (18, 3, 'BS', 'Baby Shower', 'Baby Shower', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (19, 3, 'BP', 'Bautizo/Primera Comunión', 'Bautizo/Primera Comunión', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (20, 3, 'FH', 'Funeral/Homenaje', 'Funeral/Homenaje', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (21, 3, 'ED', 'Evento Deportivo', 'Evento Deportivo', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (22, 3, 'EGA', 'Evento Gastronómico', 'Evento Gastronómico', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (23, 3, 'CON', 'Convivio', 'Convivio', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (24, 3, 'CF', 'Concierto/Festival', 'Concierto/Festival', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (25, 3, 'DS', 'Despedida de Soltero/a', 'Despedida de Soltero/a', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (26, 3, 'OTR', 'Otros', 'Otros', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (27, 4, 'AN', 'Anticipo', 'Anticipo', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (28, 4, 'ABO', 'Abono', 'Abono', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (29, 4, 'PF', 'Pago final', 'Pago final', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (30, 4, 'DEV', 'Devolucion', 'Devolución', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (31, 5, 'EFE', 'Efectivo', 'Efectivo', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (32, 5, 'TRAN', 'Transferencia', 'Transferencia', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (33, 5, 'TAR', 'Tarjeta', 'Tarjeta', true);
INSERT INTO cana.catalogos_cana (id_catalogo, id_tipo_catalogo, codigo, nombre, descripcion, estado_registro) VALUES (34, 5, 'DEP', 'Depósito', 'Depósito', true);


--
-- Data for Name: estados; Type: TABLE DATA; Schema: cana; Owner: postgres
--

INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (4, 'COT', 'CAN', 'Cancelada', 'Cotización Cancelada', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (2, 'COT', 'P', 'Pendiente de Confirmar', 'Cotización Pendiente de Confirmar', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (3, 'COT', 'CONF', 'Confirmada', 'Cotización Confirmada', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (5, 'COT', 'E', 'Eliminada', 'Cotización eliminada', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (6, 'EVE', 'CNF', 'Confirmado', 'Evento confirmado', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (8, 'EVE', 'RE', 'En Ruta de entrega', 'Cuando se esta llevando el pedido al lugar', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (7, 'EVE', 'EP', 'En proceso', 'Cuando se esta cargando/alistando el evento', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (10, 'EVE', 'ETR', 'Entregado', 'Ya se cumplio con la entrega solicitada', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (12, 'EVE', 'REC', 'Recolectado / en ruta a bodega', 'En ruta a bodega', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (14, 'PAGO', 'PENDIENTE', 'Pendiente de pago', 'El pedido aun no tiene ningun pago registrado', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (15, 'PAGO', 'ANTICIPO', 'Anticipo recibido', 'Se recibio un anticipo pero no cubre el total', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (16, 'PAGO', 'PARCIAL', 'Pago parcial', 'Se han recibido abonos pero no se completa el total', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (17, 'PAGO', 'PAGADO', 'Pagado completo', 'El pedido esta completamente pagado', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (18, 'PAGO', 'DEVUELTO', 'Devuelto', 'Se devolvio el pago al cliente', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (13, 'EVE', 'FIN', 'Finalizado', 'recolectado y guardado', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (9, 'EVE', 'M', 'Montando / Decorando', 'Cuando se esta haciendo el montaje (cuando aplique)', false);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (11, 'EVE', 'PR', 'Pendiente de recolección', 'El evento finalizó y se necesita recojer', false);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (20, 'EVE', 'ECA', 'Evento cancelado', 'El evento se cancela por cliente o empresa', true);
INSERT INTO cana.estados (id_estado, tipo_estado, codigo_estado, nombre_estado, descripcion_estado, estado_registro) VALUES (1, 'COT', 'C', 'Creada', 'Cotización Creada', false);


--
-- PostgreSQL database dump complete
--

