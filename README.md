# ServiSerc — Administración de Clientes (Edición Modernizada JavaFX)

Aplicación de escritorio para la administración tributaria, contable y de cobranza de clientes de **ServiSerc**, modernizada integralmente desde Java Swing hacia **JavaFX 21** con persistencia **JPA (EclipseLink 2.7.12)** y soporte dual para **MySQL 8.x** y base de datos local compatible con MySQL (**H2 Database**).

---

## 1. Requisitos del Entorno

- **Java JDK:** 21 o superior (probado en OpenJDK Temurin 21.0.12).
- **Apache Maven:** 3.9+ (incluido mediante **Maven Wrapper** `./mvnw` y `.\mvnw.cmd`, por lo que no requiere instalación global de Maven).
- **Base de Datos:**
  - **Modo `auto` (Predeterminado):** Intenta conectarse a MySQL local (`127.0.0.1:3306/serviserc`). Si el servidor MySQL no está en ejecución, activa automáticamente una base de datos persistente local en `./data/serviserc_db` con compatibilidad MySQL (`MODE=MySQL`).
  - **Modo `mysql`:** Conexión exclusiva a un servidor MySQL externo o local configurable mediante archivo de propiedades, variables de entorno o propiedades JVM.
  - **Modo `embedded`:** Conexión directa a la base de datos local embebida en `./data/serviserc_db`.

---

## 2. Configuración de Base de Datos y Credenciales

Las credenciales fueron desacopladas del código fuente y de `persistence.xml`. Puede configurarlas copiando el archivo de plantilla:

```powershell
Copy-Item config\database.properties.example config\database.properties
```

Propiedades admitidas (en `config/database.properties`, propiedades `-D` de JVM o variables de entorno):

| Propiedad JVM / Archivo | Variable de Entorno | Valor por Defecto | Descripción |
| :--- | :--- | :--- | :--- |
| `serviserc.db.mode` | `SERVISERC_DB_MODE` | `auto` | `auto`, `mysql` o `embedded` |
| `serviserc.db.url` | `SERVISERC_DB_URL` | `jdbc:mysql://127.0.0.1:3306/serviserc?...` | URL JDBC de conexión |
| `serviserc.db.user` | `SERVISERC_DB_USER` | `root` (MySQL) / `sa` (Embedded) | Usuario de base de datos |
| `serviserc.db.password` | `SERVISERC_DB_PASSWORD` | *(vacío)* | Contraseña de base de datos |
| `serviserc.db.driver` | `SERVISERC_DB_DRIVER` | `com.mysql.cj.jdbc.Driver` | Driver JDBC |

> **Nota:** El archivo `config/database.properties` y el directorio `data/` están excluidos en `.gitignore` para evitar publicar credenciales privadas o datos locales en el repositorio.

---

## 3. Ejecución de la Aplicación JavaFX

### En Windows (PowerShell / CMD)

```powershell
.\mvnw.cmd javafx:run
```

O utilizando el plugin de ejecución estándar apuntando a `com.sptv.mavenproject2.Launcher`:

```powershell
.\mvnw.cmd compile exec:java
```

### En Linux / macOS

```bash
./mvnw javafx:run
```

---

## 4. Credenciales Iniciales de Acceso al Sistema

Cuando la aplicación se inicia sobre una base de datos vacía, el servicio `AuthService` inicializa automáticamente una cuenta semilla (cifrada con **SHA-256**) y una contraseña general de seguridad:

- **Usuario del Programa (Inicio de Sesión):** `admin`
- **Contraseña del Usuario:** `admin123`
- **Contraseña General de Seguridad (Módulo de Usuarios):** `serviserc2024`

---

## 5. Ejecución de Pruebas Automatizadas

Para ejecutar la suite de pruebas de integración con **JUnit 5**:

```powershell
.\mvnw.cmd test
```

Para ejecutar la prueba de humo de arranque y renderizado de la interfaz **JavaFX**:

```powershell
.\mvnw.cmd javafx:run "-Djavafx.args=--smoke-test"
```

---

## 6. Módulos y Funcionalidades

1. **Inicio de Sesión (`LoginView`):** Autenticación de operadores con verificación de hash SHA-256 y estado en vivo del motor de persistencia.
2. **Panel Principal (`MainDashboardView`):** Indicadores clave (KPIs) en tiempo real y navegación lateral hacia todos los módulos.
3. **Tabla General de Clientes (`ClientesTableView`):** Búsqueda instantánea y edición rápida en celda o en panel inferior de Nombre, Correo, Honorarios, N° de Cliente, Régimen y Teléfono.
4. **Registro y Edición Integral de Clientes (`ClienteFormView`):**
   - Creación transaccional de `Cliente`, `Clavesol`, `Libro`, `Cobranza` y generación automática de **84 cuotas de `PagoMensual`** (Enero a Diciembre, años 2024 a 2030).
   - Edición integral por RUC incluyendo los 3 libros contables (`Registro de Ventas`, `Registro de Compras`, `Diario de Formato Simplificado`) y los 3 estados de cobranza (`Mensual`, `Anual`, `Total`).
   - Eliminación integral en cascada controlada.
5. **Consulta de Expediente por RUC (`BuscarRucView`):** Visualización estructurada de la ficha tributaria, credenciales SOL, libros y cobranza a partir del RUC de 11 dígitos.
6. **Matriz Anual de Pagos (`PagosMatrixView`):** Selector de ejercicio fiscal (`2024–2030`), edición de montos mensuales de Enero a Diciembre, edición de nombre de cliente y cálculo automático del acumulado anual.
7. **Seguridad y Administración de Usuarios (`UsuariosSeguridadView`):** Compuerta protegida por `ContrasenaGeneral`, listado de usuarios, creación con hash SHA-256, baja validada por `ID + Nombre` y cambio de contraseña general.
