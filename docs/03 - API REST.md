# 🔌 API REST — Referencia de Endpoints

> [!info] Base URL
> Producción: `https://eventos-cana.fly.dev/cana/api` · Local: `http://localhost:8082/cana/api`
> Todo lo que contiene `/privado/` exige header `Authorization: Bearer <jwt>`. Solo `/publico/**` es abierto.

## 🔓 Autenticación — `/publico`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/publico/authenticate` | Login con `{username: correo, password}`. Devuelve `jwt, nombre, dpi, rol, codigoRol, nombreRol`. Corta con error propio si el usuario está inactivo |
| `POST` | `/publico/guardarUsuario` | Alta de usuario (encripta contraseña con BCrypt) |

## 📋 Cotizaciones — `/cotizaciones`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/privado/listarCotizaciones` | Listado (DTO de lista con cliente, evento, estado) |
| `GET` | `/privado/historialEstados/{idCotizacion}` | Trazabilidad de estados de la cotización |
| `POST` | `/privado/guardarCotizacion` | Crea cotización con detalle de ítems y servicios |
| `PUT` | `/privado/actualizarCotizacion/{idCotizacion}` | Edita cabecera y detalles |
| `GET` | `/privado/documento/{idCotizacion}` | PDF Jasper de la cotización (se guarda en `documentos_cotizacion`) |
| `PUT` | `/privado/confirmarCotizacion/{id}` | Confirma y **crea el pedido automáticamente** (recibe fecha de entrega y viajes aproximados) |
| `PUT` | `/privado/cancelarCotizacion/{id}` | Estado → `CAN` |
| `PUT` | `/privado/eliminarCotizacion/{id}` | Borrado lógico, estado → `E` |

## 📦 Pedidos — `/pedidos`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/privado/listarPedidos` | Listado general |
| `GET` | `/privado/obtenerPedido/{correlativoPedido}` | Detalle completo (ítems + servicios) |
| `POST` | `/privado/guardarPedido` | Pedido directo sin cotización (genera correlativo `PED-…`, valida fechas no pasadas) |
| `PUT` | `/privado/actualizarPedido/{correlativoPedido}` | Edición |
| `PUT` | `/privado/cambiarEstado/{correlativoPedido}/{idEstado}` | Avanza la máquina de estados |
| `PUT` | `/privado/cancelarPedido/{correlativoPedido}` | Estado → `ECA` |
| `GET` | `/privado/historialEstados/{correlativoPedido}` | Trazabilidad de estados |

## 🚚 Entregas — `/entregas`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/privado/listarEntregas` | Entregas (`tipo_movimiento = ENT`) |
| `GET` | `/privado/estadisticas` | Contadores para las tarjetas de la pantalla |
| `GET` | `/privado/obtenerEntrega/{idEntrega}` | Detalle con viajes e ítems |
| `GET` | `/privado/pedidosDisponibles` | Pedidos confirmados sin entrega creada |
| `POST` | `/privado/crearEntrega` | Crea la fila `ENT` del pedido |
| `POST` | `/privado/registrarViaje` | Registra viaje con sus ítems (incrementa viajes reales) |
| `PUT` | `/privado/marcarFinalizada/{idEntrega}` | Cierra la entrega (pedido → `ETR`) |
| `GET` | `/privado/constancia/{idEntrega}` | PDF de constancia de entrega (Jasper) |
| `POST` | `/privado/constanciaFirmada/{idEntrega}` | Sube la constancia firmada (foto ≤ 10 MB, multipart) |
| `GET` | `/privado/constanciaFirmada/{idEntrega}` | Descarga la constancia firmada |

## ♻️ Recolecciones — `/recolecciones`

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/privado/listarRecolecciones` | Recolecciones (`tipo_movimiento = REC`) |
| `GET` | `/privado/estadisticas` | Contadores |
| `GET` | `/privado/obtenerRecoleccion/{idRecoleccion}` | Detalle con viajes |
| `PUT` | `/privado/programar/{idRecoleccion}` | Programa fecha y viajes aproximados |
| `POST` | `/privado/registrarViaje` | Viaje de vuelta a bodega con ítems |
| `PUT` | `/privado/marcarFinalizada/{idRecoleccion}` | Cierra recolección (pedido → `FIN`) |

## 💰 Pagos — `/pagosPedido`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/privado/registrarPago` | Registra pago (tipo AN/ABO/PF/DEV, método EFE/TRAN/TAR/DEP) y recalcula el estado de pago |
| `PUT` | `/privado/anularPago/{idPago}` | Anulación lógica (`estado_registro = false`) |
| `GET` | `/privado/listar/{correlativoPedido}` | Pagos del pedido |
| `GET` | `/privado/estadoCuenta/{correlativoPedido}` | Total, pagado, saldo y estado |

## 🗃️ Administración y catálogos

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/usuarios/privado/listarUsuarios` | Usuarios internos |
| `PUT` | `/usuarios/privado/modificarUsuario` | Edición |
| `PUT` | `/usuarios/privado/inactivarUsuario/{nitDpi}` | Borrado lógico |
| `GET` | `/items/privado/listarItems` | Inventario |
| `POST` | `/items/privado/guardarItem` | Alta de ítem |
| `PUT` | `/items/privado/editarItem/{id}` | Edición |
| `PUT` | `/items/privado/eliminarItem/{id}` | Borrado lógico |
| `GET` | `/servicios/privado/listarServicios` | Servicios de decoración |
| `GET` | `/servicios/privado/listarCategorias` | Categorías de servicio |
| `POST` | `/servicios/privado/guardarServicio` | Alta |
| `PUT` | `/servicios/privado/editarServicio/{id}` | Edición |
| `PUT` | `/servicios/privado/inactivarServicio/{id}` | Borrado lógico |
| `PUT` | `/detalleServicioPedido/privado/marcarRealizado/{id}` | Marca servicio ejecutado (`fecha_realizado`) |
| `GET` | `/salones/privado/listarSalones` | Salones |
| `POST` | `/salones/privado/guardarSalon` | Alta |
| `PUT` | `/salones/privado/editarSalon/{id}` | Edición |
| `PUT` | `/salones/privado/eliminarSalon/{id}` | Borrado lógico |
| `GET` | `/catalogosCana/getCatalogoByNombre/{nombre}` | Catálogo por nombre (ROLES, TIPO_ITEM, TIPO_EVENTOS, TIPO_PAGO, MODALIDADES_PAGO) |
| `GET` | `/direcciones/getDireccionesByDpiNit/{dpiNit}` | Direcciones de un usuario |

## 📊 Tableros y reportes

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/dashboard/privado/resumen` | KPIs del home (entregas de hoy, próximos eventos, etc.) |
| `GET` | `/calendario/privado/agenda` | Eventos + entregas + recolecciones para el calendario |
| `GET` | `/reportes/privado/general` | Reporte general (resumen, serie mensual, ranking, cartera, distribución) |
| `GET` | `/reportes/privado/general/pdf` | El mismo reporte como PDF Jasper (expone `Content-Disposition`) |

> [!note] Controladores sin endpoints propios
> `DetalleCotizacionController`, `DetallePedidoController`, `DetalleServicioCotizacionController`, `DetalleViajeController`, `DetalleViajeItemsController`, `EstadosController`, `EstadosPedido`, `RepresentantesEmpresasController` y `TipoCatalogosCanaController` existen con su `@RequestMapping` base pero hoy no publican rutas — los detalles viajan embebidos en los DTOs de cotización/pedido/entrega.

➡️ Siguiente: [[04 - Frontend Angular]]
