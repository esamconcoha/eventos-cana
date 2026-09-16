# 🔄 Estados y Ciclo de Vida

> [!info] Cómo leer esta nota
> El sistema tiene **tres máquinas de estado** (cotización `COT`, pedido/evento `EVE`, pago `PAGO`) definidas en la tabla `cana.estados`. Cada transición queda historiada automáticamente ([[05 - Base de Datos#⚙️ Triggers de trazabilidad|triggers]]) y el frontend la muestra en el modal de trazabilidad.

## 📋 Cotización (`tipo_estado = COT`)

```mermaid
stateDiagram-v2
    [*] --> P : guardarCotizacion
    P : P — Pendiente de Confirmar
    CONF : CONF — Confirmada
    CAN : CAN — Cancelada
    E : E — Eliminada (borrado lógico)

    P --> CONF : confirmarCotizacion<br>(pide fecha entrega + viajes aprox.)
    P --> CAN : cancelarCotizacion
    P --> E : eliminarCotizacion
    CONF --> [*] : nace el pedido PED-…
```

| Código | Nombre | Nota |
|---|---|---|
| `P` | Pendiente de Confirmar | Estado inicial al guardar |
| `CONF` | Confirmada | **Dispara la creación automática del pedido** copiando ítems y servicios |
| `CAN` | Cancelada | — |
| `E` | Eliminada | Borrado lógico |
| `C` | Creada | *Inactivo en el catálogo (ya no se usa)* |

## 📦 Pedido / Evento (`tipo_estado = EVE`)

```mermaid
stateDiagram-v2
    [*] --> CNF : pedido creado<br>(directo o desde cotización)
    CNF : CNF — Confirmado
    EP : EP — En proceso (cargando/alistando)
    RE : RE — En ruta de entrega
    ETR : ETR — Entregado
    REC : REC — Recolectado / en ruta a bodega
    FIN : FIN — Finalizado (guardado en bodega)
    ECA : ECA — Evento cancelado

    CNF --> EP : se alista la entrega
    EP --> RE : inicia viaje
    RE --> ETR : marcarFinalizada (entrega)
    ETR --> REC : viajes de recolección
    REC --> FIN : marcarFinalizada (recolección)
    CNF --> ECA : cancelarPedido
    EP --> ECA : cancelarPedido
```

| Código | Nombre | Quién lo mueve |
|---|---|---|
| `CNF` | Confirmado | Al crear el pedido |
| `EP` | En proceso | Logística: se está cargando/alistando |
| `RE` | En ruta de entrega | Al llevar el pedido |
| `ETR` | Entregado | Al cerrar la entrega (`ENT`) |
| `REC` | Recolectado / en ruta a bodega | Durante la recolección |
| `FIN` | Finalizado | Al cerrar la recolección (`REC`) — todo de vuelta en bodega |
| `ECA` | Evento cancelado | Cancelación por cliente o empresa |
| `M`, `PR` | Montando / Pendiente de recolección | *Inactivos en el catálogo* |

## 💰 Estado de pago (`tipo_estado = PAGO`)

```mermaid
stateDiagram-v2
    [*] --> PENDIENTE : pedido sin pagos
    PENDIENTE : PENDIENTE — sin pagos
    ANTICIPO : ANTICIPO — anticipo recibido
    PARCIAL : PARCIAL — abonos sin completar
    PAGADO : PAGADO — total cubierto
    DEVUELTO : DEVUELTO — pago devuelto

    PENDIENTE --> ANTICIPO : pago tipo AN
    ANTICIPO --> PARCIAL : abonos (ABO)
    PARCIAL --> PAGADO : pago final (PF)
    ANTICIPO --> PAGADO : si cubre el total
    PAGADO --> DEVUELTO : devolución (DEV)
    PARCIAL --> PENDIENTE : anularPago (recalcula)
```

> [!tip] El estado de pago se **recalcula** con cada movimiento
> `registrarPago` y `anularPago` recomputan el estado comparando lo pagado contra el total del pedido, actualizan `pedidos_cana.estado_pago` + flag `pagado`, y escriben el tramo en `estados_pago_pedido` (aquí lo historia el backend, no un trigger).

## 🚚 Ciclo logístico completo (entrega + recolección)

```mermaid
flowchart TD
    A["Pedido CNF<br>aparece en 'pedidosDisponibles'"] --> B["crearEntrega<br>fila ENT en entregas_pedido<br>viajes aproximados"]
    B --> C["registrarViaje (×N)<br>detalle_viaje + detalle_viaje_items<br>cantidad_viajes_reales++"]
    C --> D["Constancia PDF<br>GET /constancia/{id}"]
    D --> E["Cliente firma →<br>POST constanciaFirmada (foto ≤10 MB)<br>se guarda en documentos_entrega"]
    E --> F["marcarFinalizada entrega<br>pedido → ETR"]
    F --> G["programar recolección<br>fila REC + fecha"]
    G --> H["registrarViaje de vuelta (×N)<br>ítems retornan a bodega"]
    H --> I["marcarFinalizada recolección<br>pedido → FIN ✅"]
```

- Cada **viaje** transporta un subconjunto de los ítems del pedido (`detalle_viaje_items`); así se soportan pedidos que no caben en un solo camión.
- La fila `REC` reutiliza la misma tabla `entregas_pedido` diferenciada por `tipo_movimiento`, con la restricción de **una fila por tipo por pedido**.
- Este flujo completo está cubierto por el test `CicloLogisticoCompletoTest`.

➡️ Siguiente: [[07 - Casos de Uso]]
