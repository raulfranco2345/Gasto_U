# GastoU

Aplicación Android para que estudiantes universitarios controlen sus gastos, su presupuesto mensual y sus metas de ahorro. Incluye una API REST propia (carpeta `backend/`) con base de datos MongoDB desplegada en la nube.

Proyecto integrador de la materia Desarrollo de Aplicaciones Móviles — Universidad Politécnica de Chiapas.

Versión actual: **1.1.0** (`versionCode 2`).

## Funcionalidades

- Registro e inicio de sesión con la API propia (JWT); la sesión se conserva al reiniciar la app.
- Vista de perfil: datos del usuario, edición de nombre y universidad, estadísticas, resumen del mes calculado por el servidor y cierre de sesión.
- Registro de gastos con categoría, fecha, método de pago, nota y foto del ticket.
- Presupuesto mensual con avance y gráfica por categoría en el Inicio.
- Historial por mes y detalle de cada gasto, con opción de eliminar.
- Metas de ahorro: crear, abonar (el abono se registra también como gasto), completar y eliminar.
- Funciona sin internet: los cambios se guardan en el teléfono y se envían a la API al recuperar la conexión.

### Integraciones del dispositivo (requisito de 3)

| Integración | Dónde se usa | Archivo |
|---|---|---|
| **PDF** | Inicio → "Exportar reporte en PDF": estado de cuenta del mes, se comparte y se guarda en Descargas/GastoU | `integration/pdf/PdfReportGenerator.kt` |
| **Autenticación biométrica** | Perfil → "Bloquear con huella". Al abrir la app se pide huella, rostro o PIN | `integration/biometric/BiometricAuthenticator.kt` |
| **Widget** | Widget "Resumen GastoU" en la pantalla de inicio del teléfono: gastado en el mes y presupuesto restante | `integration/widget/GastoUWidget.kt` |
| Cámara (adicional) | Registrar gasto → foto del ticket | `integration/camera/ReceiptCamera.kt` |

## Tecnologías

**App:** Kotlin, Jetpack Compose (Material 3), arquitectura MVVM, ViewModel + StateFlow, corrutinas, Navigation Compose, Retrofit + OkHttp + Gson, Room, DataStore Preferences, AndroidX Biometric, Jetpack Glance (widget), PdfDocument.

**API:** Node.js 20+, Express 5, MongoDB (Mongoose), JSON Web Tokens, bcrypt. Desplegada en Render con base de datos en MongoDB Atlas.

## Arquitectura MVVM

Flujo de una operación: **Pantalla Compose → ViewModel → (caso de uso) → Repositorio → Room / API → Repositorio → ViewModel (UiState) → Pantalla**.

```
app/src/main/java/com/example/gastou/
├── GastoUApp.kt               Application: crea el contenedor de dependencias
├── MainActivity.kt            Aloja el tema y la navegación
├── MainViewModel.kt           Decide la pantalla inicial (login, bloqueo o inicio)
├── navigation/
│   ├── Routes.kt              Rutas centralizadas
│   └── AppNavGraph.kt         Destinos y conexión pantalla ↔ ViewModel
├── di/
│   ├── AppContainer.kt        Inyección de dependencias manual (Retrofit, Room, DataStore, repositorios)
│   └── AppViewModelProvider.kt Fábrica de ViewModels
├── core/
│   ├── common/                ErrorMapper (mensajes de error), Validators
│   └── ui/                    Tema y componentes reutilizables
├── data/
│   ├── remote/api/            GastoUApi (Retrofit), ApiClient + AuthInterceptor
│   ├── remote/dto/            Objetos de petición y respuesta
│   ├── local/db/              Room: entidades, DAOs y GastoUDatabase
│   ├── local/preferences/     DataStore: sesión y preferencias
│   ├── mapper/                Conversión DTO ↔ Entity ↔ modelo de dominio
│   └── repository/            Implementaciones de los repositorios
├── domain/
│   ├── model/                 Expense, SavingGoal, User, MonthlySummary, SyncOutcome
│   ├── repository/            Interfaces (contratos) de los repositorios
│   └── usecase/               SyncAllDataUseCase, AddFundsToGoalUseCase
├── presentation/              Una carpeta por módulo: Screen + ViewModel + UiState
│   ├── auth/  home/  expense/  history/  goals/  budget/  profile/  lock/
└── integration/               pdf/  biometric/  widget/  camera/
```

- Las pantallas no hacen peticiones HTTP ni consultas a la base de datos: solo observan `StateFlow` con `collectAsStateWithLifecycle` y envían eventos al ViewModel.
- Los ViewModels dependen de interfaces (`domain/repository`), no de Retrofit ni de Room.
- Las operaciones que consumen datos muestran carga, error con botón "Reintentar" y estado vacío.

### Persistencia local

| Dato | Almacenamiento | Motivo |
|---|---|---|
| Gastos, metas y presupuestos | **Room** (`gastou.db`) | Datos estructurados que se consultan por mes y se actualizan o eliminan |
| Sesión (token JWT y datos del usuario) y preferencia de biometría | **DataStore Preferences** | Pocos valores de tipo clave-valor |
| Foto del ticket | Almacenamiento interno de la app (`files/receipts`) | Archivo binario |

Room es la fuente de verdad de la interfaz. Cada registro tiene un estado de sincronización (`SYNCED`, `PENDING_UPSERT`, `PENDING_DELETE`). Al guardar se escribe primero en Room y después se envía a la API; si no hay conexión, el registro queda pendiente y se envía en la siguiente sincronización (al abrir el Inicio o con "Reintentar").

## API REST (`backend/`)

Todas las rutas, excepto `/api/health`, registro e inicio de sesión, requieren el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/health` | Estado del servicio |
| POST | `/api/auth/register` | Crear cuenta `{ nombre, email, password }` |
| POST | `/api/auth/login` | Iniciar sesión `{ email, password }` |
| GET / PUT | `/api/auth/me` | Consultar / actualizar perfil `{ nombre, universidad }` |
| GET | `/api/expenses?month=AAAA-MM` | Gastos (del mes o todos) |
| GET | `/api/expenses/summary?month=AAAA-MM` | Total, presupuesto y gasto por categoría |
| PUT | `/api/expenses/:clientId` | Crear o actualizar un gasto (idempotente) |
| DELETE | `/api/expenses/:clientId` | Eliminar un gasto |
| GET | `/api/goals` | Metas de ahorro |
| PUT / DELETE | `/api/goals/:clientId` | Crear o actualizar / eliminar meta |
| GET | `/api/budgets` | Presupuestos por mes |
| PUT | `/api/budgets/:month` | Fijar presupuesto `{ amount }` |

`clientId` es el UUID que genera la app, por eso un gasto creado sin conexión no se duplica al sincronizarse.

## Cómo ejecutar

### Requisitos

- Android Studio (versión reciente) con JDK 17 o superior.
- Emulador o teléfono con Android 8.0 (API 26) o superior.
- Node.js 20 o superior para correr la API localmente.
- Una cuenta gratuita de MongoDB Atlas (o MongoDB instalado localmente).

### 1. API en tu computadora

```bash
cd backend
cp .env.example .env      # edita MONGODB_URI y JWT_SECRET
npm install
npm run dev               # http://localhost:3000/api/health
npm test                  # pruebas de validación y autenticación
```

### 2. Base de datos en MongoDB Atlas

1. Crea un clúster gratuito (M0) en https://cloud.mongodb.com.
2. En **Database Access** crea un usuario con contraseña.
3. En **Network Access** agrega `0.0.0.0/0` (necesario para Render).
4. En **Connect → Drivers** copia la cadena de conexión y ponla en `MONGODB_URI` (agrega `/gastou` antes del `?`).

### 3. Publicar la API en Render

1. En https://render.com crea un **New → Blueprint** y selecciona este repositorio (usa `render.yaml`).
2. Captura la variable `MONGODB_URI`; `JWT_SECRET` se genera sola.
3. Al terminar, abre `https://TU-SERVICIO.onrender.com/api/health`.

En el plan gratuito, el servicio se duerme tras 15 minutos sin uso; la primera petición puede tardar cerca de un minuto. La app tiene tiempos de espera amplios para esto. Antes de la exposición, abre la URL de salud para despertarlo.

### 4. App Android

1. Abre la carpeta raíz del proyecto en Android Studio.
2. En `gradle.properties` cambia `GASTOU_API_URL`:
   - Emulador con la API local: `http://10.0.2.2:3000/`
   - API en Render: `https://TU-SERVICIO.onrender.com/`
3. Sincroniza Gradle y ejecuta la configuración **app**.

### 5. Generar el APK

**Build → Build App Bundle(s) / APK(s) → Build APK(s)**. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`. Copia el APK de la versión entregada a la carpeta `apk/` o adjúntalo a un *release* de GitHub con la etiqueta de la versión (por ejemplo `v1.1.0`).

### Pruebas

- API: `cd backend && npm test`
- App (pruebas locales): `./gradlew test`

## Recorridos para la demostración

1. Registro → Inicio (sincroniza con la API).
2. Fijar presupuesto → Registrar gasto con foto del ticket → ver avance y gráfica.
3. Historial → detalle del gasto → eliminar.
4. Metas → crear meta → abonar → completar.
5. Perfil → resumen del servidor → activar bloqueo con huella → cerrar y abrir la app.
6. Exportar PDF y compartirlo.
7. Agregar el widget "Resumen GastoU" a la pantalla de inicio.
8. Modo avión → registrar un gasto → quitar modo avión → "Reintentar" → el gasto aparece en la API.

## Evidencias

Las capturas de pantalla de los recorridos y de las integraciones van en `docs/evidencias/`.

## Control de versiones

- `master`: versión estable entregada.
- `develop`: integración de funcionalidades terminadas.
- `feature/*`: una rama por funcionalidad, que se integra a `develop` mediante Pull Request.

## Equipo

| Integrante | Matrícula | Contribución |
|---|---|---|
| (nombre) | (matrícula) | (qué desarrolló) |
| (nombre) | (matrícula) | (qué desarrolló) |
