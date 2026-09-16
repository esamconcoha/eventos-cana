# ☕ Backend — Spring Boot (repo CANA)

> [!info] Ficha técnica
> **Spring Boot 3.5.6 · Java 21 · Maven** — artefacto `com.canabackend:CANA`.
> Context path: **`/cana/api`** · Puerto: **`${PORT:8082}`** · BD: PostgreSQL, esquema **`CANA`** · `ddl-auto=none` (el esquema lo manda `sql/000_esquema_completo.sql`).

## 📦 Estructura de paquetes

```
com.canabackend.cana
├── CanaApplication          ← main
├── StartupProbe             ← instrumentación del arranque
├── controllers/   (28)      ← REST, siempre delgados
├── services/      (interfaces) + services/impl/   ← lógica de negocio
├── repositories/  (26)      ← Spring Data JPA + queries nativas
├── models/        (24)      ← entidades JPA (Lombok)
├── dtos/          (~50)     ← entrada/salida de la API
├── projections/   (~25)     ← lecturas nativas tipadas (interfaces)
├── seguridad/               ← JWT, CORS, Spring Security
├── exceptions/              ← MSCanaException + handler global
├── utils/                   ← constantes de negocio y Jasper
└── validators/              ← validaciones de items y servicios
```

```mermaid
flowchart LR
    CT[Controllers] --> SV["Services (interfaz)"]
    SV --> SI[ServicesImpl]
    SI --> RP[Repositories]
    RP --> DB[(PostgreSQL)]
    SI -.-> VA[Validators]
    SI -.-> UT["Utils / Constants"]
    RP -.-> PJ[Projections]
    CT -.-> DT[DTOs]
    EX["ControlExcepcion<br>@RestControllerAdvice"] -. captura MSCanaException .-> CT
```

> [!tip] Patrón repetido en todo el backend
> Cada dominio sigue el trío **Controller → Svc (interfaz) → SvcImpl**, con DTOs para entrada/salida y *projections* para lecturas con SQL nativo. Los errores de negocio se lanzan como `MSCanaException(ErrorEnum.X)` y `ControlExcepcion` los convierte en respuestas HTTP consistentes.

## 🔐 Seguridad (paquete `seguridad/`)

| Clase | Rol |
|---|---|
| `WebSecurityConfig` | CSRF off, CORS on, **stateless**; `/publico/**`, `/ws/**`, `/chat-socket/**` públicos, el resto autenticado. Registra el filtro JWT y el `BCryptPasswordEncoder` |
| `JwtRequestFilter` | Extrae y valida el Bearer token en cada request |
| `JwtUtilService` | Genera/valida JWT (JJWT 0.12.6). Expiración **3600 s (1 h)**, refresh 604800 s |
| `UserDetailsServiceImpl` | Carga el usuario por correo desde `cana.usuarios` |
| `CorsConfig` | Lee `cors.allowed-origins` (soporta comodines tipo `https://*.vercel.app`) |
| `AuthenticationRequest/Response` | Payloads de login. La respuesta incluye `jwt, nombre, dpi, rol, codigoRol, nombreRol` |

```mermaid
sequenceDiagram
    autonumber
    participant FE as Angular
    participant AC as AuthController /publico/authenticate
    participant US as UsuariosSvc
    participant JW as JwtUtilService

    FE->>AC: { username: correo, password }
    AC->>US: findAllByCorreo(correo)
    Note over AC: Un correo puede tener varios registros<br>(borrado lógico). Se toma el ACTIVO.<br>Sin activos → USUARIO_INACTIVO.<br>No existe → BAD_CREDENTIALS.
    AC->>AC: BCrypt.matches(password, contrasenia)
    AC->>JW: generateToken(userDetails)
    AC-->>FE: jwt + nombre + dpi + codigoRol (A/JO/C/O)
    Note over FE: guarda el token y decide pantallas<br>con permisos.ts según codigoRol
```

> [!warning] Secretos
> `application.properties` trae *fallbacks* de desarrollo para `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`. En producción **siempre** se fijan como variables de entorno (secrets de Fly); los defaults del repo se consideran quemados.

## 🧬 Diagrama de clases — dominio principal

```mermaid
classDiagram
    direction LR

    class Usuarios {
        +String dpiNitUsuario PK
        +String nombresUsuario
        +String apellidosUsuario
        +String correo
        +String contrasenia BCrypt
        +Long rol → catalogos_cana
        +Boolean estadoUsuario
        +Boolean esEmpresa
        +Boolean esRepresentante
    }

    class Cotizaciones {
        +Long idCotizacion PK
        +String nombreClienteCotizacion
        +Date fechaCotizacion
        +String estadoCotizacion P|CONF|CAN|E
        +LocalDateTime fechaHoraEvento
        +String codTipoEvento
        +Boolean cotizacionConfirmada
    }

    class PedidosCana {
        +String correlativoPedido PK "PED-…"
        +String estadoPedido CNF…FIN|ECA
        +String estadoPago
        +LocalDateTime fechaEvento
        +LocalDate fechaEntrega pactada
        +LocalDate fechaRecogido programada
        +Long salonEntrega
        +Integer idCotizacion
        +Boolean pagado
    }

    class DetallePedido {
        +Long idDetalle PK
        +Integer idItem
        +BigDecimal cantidadItemPedido
    }

    class DetalleServicioPedido {
        +Integer idDetalleServPedido PK
        +Integer idServicio
        +BigDecimal cantidad
        +BigDecimal precioAcordado
        +LocalDateTime fechaRealizado "NULL = pendiente"
    }

    class EntregasPedido {
        +Long idEntrega PK
        +String tipoMovimiento ENT|REC
        +Integer cantidadViajesAproximados
        +Integer cantidadViajesReales
        +Boolean pedidoFinalizado
        +LocalDateTime fechaInicioEntrega
        +LocalDateTime fechaFinEntrega
    }

    class DetalleViaje {
        +Long idViaje PK
        +LocalDateTime fechaInicioViaje
        +LocalDateTime fechaFinViaje
    }

    class DetalleViajeItems {
        +Long idDetalle PK
        +Long idItem
        +BigDecimal cantidadItem
    }

    class ItemsCana {
        +Long idItem PK
        +Integer idTipoItem
        +String descripcionItem
        +Long cantidadItem
        +Integer cantidadFaltantes
        +BigDecimal costoItem
    }

    class ServiciosDecoracion {
        +Integer idServicio PK
        +Integer idCategoria
        +String nombreServicio
        +String unidadMedida
        +Boolean requiereDetalle
    }

    class PagosPedido {
        +Long idPago PK
        +Double montoPago
        +String tipoPago AN|ABO|PF|DEV
        +String metodoPago EFE|TRAN|TAR|DEP
        +String referenciaPago
        +Boolean estadoRegistro "false = anulado"
    }

    class MantenimientoSalones {
        +Integer idSalon PK
        +String nombreSalon
        +String direccionSalon
    }

    Usuarios "1" --> "*" Cotizaciones : cotiza
    Cotizaciones "1" --> "0..1" PedidosCana : al confirmar genera
    Cotizaciones "1" --> "*" DetalleCotizacion
    Cotizaciones "1" --> "*" DetalleServicioCotizacion
    PedidosCana "1" --> "*" DetallePedido : ítems
    PedidosCana "1" --> "*" DetalleServicioPedido : servicios
    PedidosCana "1" --> "0..2" EntregasPedido : ENT y REC
    PedidosCana "1" --> "*" PagosPedido
    PedidosCana --> MantenimientoSalones : salonEntrega
    EntregasPedido "1" --> "*" DetalleViaje : viajes
    DetalleViaje "1" --> "*" DetalleViajeItems
    DetalleViajeItems --> ItemsCana
    DetallePedido --> ItemsCana
    DetalleServicioPedido --> ServiciosDecoracion
    ServiciosDecoracion --> CategoriasServicio
```

*(Las clases de trazabilidad `EstadosPedido`, `EstadosPagoPedido`, `DocumentosCotizacion`, `DocumentosEntrega`, etc. se detallan en [[05 - Base de Datos]].)*

## 🧾 Reportes (JasperReports 7.0.3)

- Plantillas en `src/main/resources/reports/`: `cotizacion.jrxml`, `entrega.jrxml`, `estadisticas.jrxml` (+ `logo.png`).
- Se **compilan en runtime**; la dependencia `jasperreports-jdt` embebe el compilador de Eclipse porque la imagen de producción es un JRE sin `javac`.
- Servicios dedicados: `ReporteCotizacionSvc`, `ReporteEntregaSvc`, `ReporteEstadisticoSvc`, `ReportesSvc` + `GraficoUtil` (gráficos para el PDF estadístico).
- Los PDFs generados se **persisten** en `documentos_cotizacion` / `documentos_entrega` (`bytea`), incluida la **constancia de entrega firmada** que se sube como foto (límite multipart: 10 MB).

## ⚠️ Manejo de errores

```mermaid
flowchart LR
    A["SvcImpl detecta problema"] --> B["throw MSCanaException(ErrorEnum.X)"]
    B --> C["ControlExcepcion (advice global)"]
    C --> D["Response JSON uniforme<br>código + mensaje + HTTP status (EstadoHttp)"]
```

`ErrorEnum` centraliza los códigos (`COTIZACION_NOT_FOUND`, `I_BAD_CREDENTIALS`, `USUARIO_INACTIVO`, …), y `GeneralResponseException`/`ErrorDetail` dan la forma de la respuesta.

## 🧪 Tests

| Test | Qué cubre |
|---|---|
| `CanaApplicationTests` | Arranque del contexto |
| `CicloLogisticoCompletoTest` | El ciclo entrega → viajes → recolección → finalizado de punta a punta |
| `EntregasQueriesNativasTest` | Las queries nativas de entregas/recolecciones |

## 🛠️ Comandos útiles

```bash
mvn spring-boot:run
```

```bash
mvn clean package -DskipTests
```

> [!warning] Build local en Windows
> El `JAVA_HOME` del sistema es JDK 8: hay que apuntar explícitamente a un **JDK 21** para compilar (el error que da JDK 8 es engañoso). El Dockerfile no sufre esto porque fija `maven:3.9-eclipse-temurin-21`.

➡️ Siguiente: [[03 - API REST]]
