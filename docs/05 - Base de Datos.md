# 🐘 Base de Datos — PostgreSQL 9.6, esquema `cana`

> [!info] Ficha técnica
> **PostgreSQL 9.6.24** corriendo en **Docker Compose** (servicio `db`) en el droplet `167.172.155.223`. Todo vive en el esquema **`cana`** (25 tablas, 2 funciones trigger, extensión `unaccent` instalada **dentro del esquema**). Fuente de verdad: `sql/000_esquema_completo.sql` + `sql/001_datos_catalogos.sql`.

> [!warning] Reglas de oro al tocar la BD
> - Es **PG 9.6**: para columnas autoincrementales usar `BIGSERIAL`, **no** `GENERATED ALWAYS AS IDENTITY` (sintaxis de PG 10+).
> - La extensión `unaccent` debe vivir **en el esquema `cana`**: las queries la invocan calificada (`cana.unaccent(...)`). En `public` fallan las búsquedas de `ItemsCanaRepository` y `ServiciosDecoracionRepository`.
> - `ddl-auto=none`: Hibernate **no** crea ni altera tablas; los cambios de esquema se hacen con scripts SQL.
> - El dump completo (`dump/dump.sql`) trae además un esquema `neon_auth` — residuo de cuando la BD estuvo en Neon; el aplicativo solo usa `cana`.

## 🧭 Diagrama Entidad-Relación

```mermaid
erDiagram
    usuarios ||--o{ cotizaciones : "cotiza"
    usuarios ||--o{ pedidos_cana : "usuario interno"
    usuarios ||--o{ direcciones : "tiene"
    usuarios ||--o{ pagos_pedido : "registra"
    usuarios ||--o{ representantes_empresas : "representa / es empresa"
    catalogos_cana ||--o{ usuarios : "rol"
    tipo_catalogos_cana ||--o{ catalogos_cana : "agrupa"

    cotizaciones ||--o{ detalle_cotizacion : "ítems"
    cotizaciones ||--o{ detalle_servicio_cotizacion : "servicios"
    cotizaciones ||--o{ documentos_cotizacion : "PDFs"
    cotizaciones ||--o{ estados_cotizacion : "historial"
    cotizaciones ||--o| pedidos_cana : "genera al confirmar"
    estados ||--o{ estados_cotizacion : ""
    estados ||--o{ estados_pedido : ""

    pedidos_cana ||--o{ detalle_pedido : "ítems"
    pedidos_cana ||--o{ detalle_servicio_pedido : "servicios"
    pedidos_cana ||--o{ entregas_pedido : "ENT + REC"
    pedidos_cana ||--o{ pagos_pedido : "pagos"
    pedidos_cana ||--o{ estados_pedido : "historial"
    pedidos_cana ||--o{ estados_pago_pedido : "historial pago"
    mantenimiento_salones ||--o{ pedidos_cana : "salón entrega"

    entregas_pedido ||--o{ detalle_viaje : "viajes"
    entregas_pedido ||--o{ documentos_entrega : "constancias"
    detalle_viaje ||--o{ detalle_viaje_items : "carga"
    items_cana ||--o{ detalle_viaje_items : ""
    items_cana ||--o{ detalle_pedido : ""
    items_cana ||--o{ detalle_cotizacion : ""

    categorias_servicio ||--o{ servicios_decoracion : ""
    servicios_decoracion ||--o{ detalle_servicio_cotizacion : ""
    servicios_decoracion ||--o{ detalle_servicio_pedido : ""

    usuarios {
        varchar dpi_nit_usuario PK
        varchar nombres_usuario
        varchar correo
        varchar contrasenia "BCrypt"
        int rol FK
        boolean estado_usuario
    }
    cotizaciones {
        bigint id_cotizacion PK
        varchar nombre_cliente_cotizacion
        varchar estado_cotizacion "P/CONF/CAN/E"
        timestamp fecha_hora_evento
        varchar cod_tipo_evento
        boolean cotizacion_confirmada
    }
    pedidos_cana {
        varchar correlativo_pedido PK "PED-…"
        varchar estado_pedido "CNF…FIN/ECA"
        varchar estado_pago
        timestamp fecha_evento
        date fecha_entrega "pactada"
        date fecha_recogido "programada"
        bigint salon_entrega FK
        int id_cotizacion FK
        boolean pagado
    }
    entregas_pedido {
        bigint id_entrega PK
        varchar correlativo_pedido FK
        varchar tipo_movimiento "ENT/REC (único por pedido)"
        int cantidad_viajes_aproximados
        int cantidad_viajes_reales
        boolean pedido_finalizado
        timestamp fecha_inicio_entrega
        timestamp fecha_fin_entrega
    }
    detalle_viaje {
        bigint id_viaje PK
        bigint id_entrega FK
        timestamp fecha_inicio_viaje
        timestamp fecha_fin_viaje
    }
    detalle_viaje_items {
        bigint id_detalle PK
        bigint id_item FK
        bigint id_detalle_viaje FK
        numeric cantidad_item
    }
    items_cana {
        bigint id_item PK
        int id_tipo_item "catálogo TIPO_ITEM"
        numeric costo_item
        varchar descripcion_item
        bigint cantidad_item
        int cantidad_faltantes
        boolean estado_item
    }
    pagos_pedido {
        bigint id_pago PK
        varchar correlativo_pedido FK
        double monto_pago
        varchar tipo_pago "AN/ABO/PF/DEV"
        varchar metodo_pago "EFE/TRAN/TAR/DEP"
        varchar usuario_registro FK
        boolean estado_registro "false = anulado"
    }
    servicios_decoracion {
        int id_servicio PK
        int id_categoria FK
        varchar nombre_servicio
        varchar unidad_medida
        boolean requiere_detalle
    }
```

*(Se omiten en el gráfico las columnas secundarias; el detalle completo está en el script `sql/000_esquema_completo.sql`.)*

## 📚 Las 25 tablas por dominio

| Dominio | Tablas |
|---|---|
| **Seguridad y personas** | `usuarios` (PK = DPI/NIT), `direcciones`, `representantes_empresas` |
| **Catálogos** | `tipo_catalogos_cana` → `catalogos_cana` (roles, tipos de ítem/evento/pago, modalidades), `estados` (COT/EVE/PAGO) |
| **Cotización** | `cotizaciones`, `detalle_cotizacion` (ítems), `detalle_servicio_cotizacion`, `documentos_cotizacion` (PDF en `bytea`), `estados_cotizacion` (historial) |
| **Pedido** | `pedidos_cana`, `detalle_pedido`, `detalle_servicio_pedido` (`fecha_realizado` NULL = pendiente), `estados_pedido` (historial) |
| **Logística** | `entregas_pedido` (filas ENT/REC), `detalle_viaje`, `detalle_viaje_items`, `documentos_entrega` (constancias, incl. firmada) |
| **Pagos** | `pagos_pedido`, `estados_pago_pedido` (historial) |
| **Recursos** | `items_cana` (inventario), `categorias_servicio`, `servicios_decoracion`, `mantenimiento_salones` |

> [!note] Detalles de diseño que importan
> - `entregas_pedido` tiene **índice único** `(correlativo_pedido, tipo_movimiento)`: un pedido tiene *a lo sumo* una entrega (`ENT`) y una recolección (`REC`).
> - En `pedidos_cana`, `fecha_entrega` y `fecha_recogido` son fechas **de agenda** (pactadas); lo realmente ejecutado vive en las filas ENT/REC de `entregas_pedido`.
> - Borrado lógico en casi todo: `estado_registro` / `estado_usuario` / `estado_item` / `estado_servicio` / `estado_salon` en `false`.
> - Los PDFs y fotos se guardan **dentro de la BD** (`bytea` en `documentos_cotizacion` / `documentos_entrega`).
> - Índices de apoyo: `detalle_viaje(fecha_inicio_viaje)`, `detalle_viaje(id_entrega)`, `detalle_viaje_items(id_detalle_viaje)`, `documentos_entrega(id_entrega)`, `pagos_pedido(correlativo_pedido)`, `estados_pago_pedido(correlativo_pedido)`.

## ⚙️ Triggers de trazabilidad

```mermaid
sequenceDiagram
    participant APP as Backend (UPDATE estado_pedido)
    participant T as trg_trazabilidad_estado_pedido
    participant E as cana.estados
    participant H as cana.estados_pedido

    APP->>T: AFTER INSERT/UPDATE OF estado_pedido
    T->>T: ¿cambió el código? si no, salir
    T->>E: buscar id_estado (tipo_estado = 'EVE')
    Note over T: si el código no existe → RAISE EXCEPTION<br>(falla rápido y claro)
    T->>H: UPDATE tramo abierto → fecha_hora_fin = now()
    T->>H: INSERT tramo nuevo (fecha_hora_fin = NULL)
```

| Trigger | Tabla vigilada | Historial que alimenta | Filtro en `estados` |
|---|---|---|---|
| `trg_trazabilidad_estado_cotizacion` → `fn_trazabilidad_estado_cotizacion()` | `cotizaciones.estado_cotizacion` | `estados_cotizacion` | `tipo_estado = 'COT'` |
| `trg_trazabilidad_estado_pedido` → `fn_trazabilidad_estado_pedido()` | `pedidos_cana.estado_pedido` | `estados_pedido` | `tipo_estado = 'EVE'` |

El historial funciona por **tramos**: cada fila tiene `fecha_hora_inicio` y `fecha_hora_fin`; el tramo con `fecha_hora_fin IS NULL` es el estado vigente. *(El historial de pago —`estados_pago_pedido`— lo mantiene el backend, no un trigger.)*

## 🏷️ Catálogos precargados (`001_datos_catalogos.sql`)

| Tipo catálogo | Códigos |
|---|---|
| **ROLES** | `A` Administrador/a General · `JO` Jefe Operativo · `C` Contador · `O` Operativo |
| **TIPO_ITEM** | `C` Cristalería · `MA` Mantelería · `M` Mobiliario · `OT` Otros |
| **TIPO_EVENTOS** | 18 tipos: `BO` Boda, `XV` XV Años, `AV` Aniversario, `CP` Cumpleaños, `EC` Corporativo, `ER` Religioso, `EF` Familiar, `GR` Graduación, `ECA` Caritativo, `BS` Baby Shower, `BP` Bautizo/1ª Comunión, `FH` Funeral/Homenaje, `ED` Deportivo, `EGA` Gastronómico, `CON` Convivio, `CF` Concierto/Festival, `DS` Despedida, `OTR` Otros |
| **TIPO_PAGO** | `AN` Anticipo · `ABO` Abono · `PF` Pago final · `DEV` Devolución |
| **MODALIDADES_PAGO** | `EFE` Efectivo · `TRAN` Transferencia · `TAR` Tarjeta · `DEP` Depósito |

Los **estados** (tabla `estados`, tipos `COT`/`EVE`/`PAGO`) se detallan con sus transiciones en [[06 - Estados y Ciclo de Vida]].

## 🔧 Operación de la BD

Conectarse al servidor y ver logs en vivo:

```bash
ssh -i "~/.ssh/id_ed25519" root@167.172.155.223
```

```bash
docker compose logs -f db
```

Más comandos (backups, restauración del dump, variables `DB_*`): [[08 - Infraestructura y Operaciones]].

➡️ Siguiente: [[06 - Estados y Ciclo de Vida]]
