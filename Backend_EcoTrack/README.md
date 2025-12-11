# 📚 Documentación Backend - EcoTrack

## 🎯 Descripción del Proyecto

**EcoTrack** es una API REST para el seguimiento de la huella de carbono personal. Permite a los usuarios registrar sus actividades diarias (consumo eléctrico, transporte, vuelos, etc.) y calcular automáticamente las emisiones de CO2 generadas.

---

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL** (Base de datos en Supabase)
- **Maven** (Gestor de dependencias)
- **Climatiq API** (Cálculo de emisiones de CO2)

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

1. **Java JDK 17 o superior**
   - Descarga: https://adoptium.net/
   - Verifica con: `java -version`

2. **Maven**
   - Descarga: https://maven.apache.org/download.cgi
   - Verifica con: `mvn -version`

3. **Git**
   - Descarga: https://git-scm.com/downloads
   - Verifica con: `git --version`

4. **Un editor de código** (VS Code, IntelliJ IDEA, Eclipse)

---

## 🚀 Instalación y Configuración

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/TU_USUARIO/ecotrack-backend.git
cd ecotrack-backend
```

### Paso 2: Configurar Variables de Entorno

#### ⚠️ **IMPORTANTE: Archivo `.env`**

El proyecto **NO incluye** el archivo `.env` por seguridad. Debes crearlo manualmente.

1. **Crea un archivo llamado `.env`** en la raíz del proyecto (al mismo nivel que `pom.xml`)

2. **Copia y pega este contenido:**

```env
# Base de datos PostgreSQL
DB_URL=jdbc:postgresql://db.luigimufrborkamhoxll.supabase.co:5432/postgres?sslmode=require
DB_USER=postgres
DB_PASSWORD=ecoTrack123@

# Climatiq API
CLIMATIQ_API_KEY=77S78BQBMS1DQ5B6Z103WD4730
CLIMATIQ_API_URL=https://api.climatiq.io/data/v1
```

3. **Guarda el archivo**

> **Nota:** Este archivo `.env` contiene credenciales sensibles y **NO debe subirse a GitHub**. Ya está incluido en `.gitignore`.

---

### Paso 3: Instalar Dependencias

```bash
mvn clean install
```

Este comando descargará todas las dependencias necesarias.

---

### Paso 4: Ejecutar la Aplicación

#### Opción A: Desde la terminal

```bash
mvn spring-boot:run
```

#### Opción B: Desde VS Code

1. Abre el proyecto en VS Code
2. Ve a `src/main/java/ecotrack/backend/BackendApplication.java`
3. Click derecho → **"Run Java"**

#### Opción C: Desde IntelliJ IDEA

1. Abre el proyecto
2. Busca `BackendApplication.java`
3. Click en el botón ▶️ verde

---

### Paso 5: Verificar que Funciona

Si todo está correcto, verás en la consola:

```
Tomcat started on port 7878 (http)
Started BackendApplication in X seconds
```

**¡Tu backend está corriendo en:** `http://localhost:7878`

---

## 📡 Endpoints de la API

### 🔐 **Autenticación**

#### 1. Registrar Usuario

**Endpoint:** `POST /api/auth/register`

**Body (JSON):**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "password": "mipassword123"
}
```

**Respuesta:**
```json
{
  "userId": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "message": "Usuario registrado exitosamente"
}
```

---

#### 2. Iniciar Sesión

**Endpoint:** `POST /api/auth/login`

**Body (JSON):**
```json
{
  "email": "juan@example.com",
  "password": "mipassword123"
}
```

**Respuesta:**
```json
{
  "userId": 1,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "message": "Login exitoso"
}
```

> **Importante:** Guarda el `userId` que recibes. Lo necesitarás para todas las demás peticiones.

---

### 📊 **Actividades**

#### 3. Registrar Actividad

**Endpoint:** `POST /api/activities`

**Body (JSON) - Ejemplo Electricidad:**
```json
{
  "userId": 1,
  "fecha": "2025-12-06",
  "tipoActividad": "Electricidad",
  "descripcion": "Consumo mensual del hogar",
  "activityId": "electricity-supply_grid-source_supplier_mix",
  "region": "CO",
  "parameters": {
    "energy": 150,
    "energy_unit": "kWh"
  }
}
```

**Body (JSON) - Ejemplo Transporte:**
```json
{
  "userId": 1,
  "fecha": "2025-12-06",
  "tipoActividad": "Transporte",
  "descripcion": "Viaje al trabajo",
  "activityId": "passenger_vehicle-vehicle_type_car-fuel_source_petrol-distance_na",
  "region": "CO",
  "parameters": {
    "distance": 50,
    "distance_unit": "km"
  }
}
```

**Respuesta:**
```json
{
  "activityId": 1,
  "fecha": "2025-12-06",
  "tipoActividad": "Electricidad",
  "descripcion": "Consumo mensual del hogar",
  "emisiones": 27.3,
  "fuente": "climatiq"
}
```

---

### 📈 **Reportes y Estadísticas**

#### 4. Ver Todas las Actividades del Usuario

**Endpoint:** `GET /api/reports/activities/{userId}`

**Ejemplo:** `GET /api/reports/activities/1`

**Respuesta:**
```json
[
  {
    "id": 1,
    "fecha": "2025-12-06",
    "tipoActividad": "Electricidad",
    "descripcion": "Consumo mensual del hogar",
    "emisiones": 27.3,
    "activityId": "electricity-supply_grid-source_supplier_mix",
    "region": "CO"
  },
  {
    "id": 2,
    "fecha": "2025-12-05",
    "tipoActividad": "Transporte",
    "descripcion": "Viaje al trabajo",
    "emisiones": 15.8,
    "activityId": "passenger_vehicle-vehicle_type_car-fuel_source_petrol-distance_na",
    "region": "CO"
  }
]
```

---

#### 5. Ver Totales de Carbono

**Endpoint:** `GET /api/reports/totals/{userId}`

**Ejemplo:** `GET /api/reports/totals/1`

**Respuesta:**
```json
{
  "userId": 1,
  "userName": "Juan Pérez",
  "totalHistorico": 150.5,
  "totalMensual": 80.2,
  "totalSemanal": 43.1,
  "actividadesRegistradas": 12
}
```

---

#### 6. Ver Estadísticas por Tipo de Actividad

**Endpoint:** `GET /api/reports/stats/{userId}`

**Ejemplo:** `GET /api/reports/stats/1`

**Respuesta:**
```json
{
  "userId": 1,
  "emisionesPorTipo": {
    "Electricidad": 90.5,
    "Transporte": 45.2,
    "Vuelos": 14.8
  },
  "actividadesPorTipo": {
    "Electricidad": 8,
    "Transporte": 3,
    "Vuelos": 1
  },
  "tipoMasContaminante": "Electricidad",
  "totalEmisiones": 150.5
}
```

---

#### 7. Ver Actividades por Rango de Fechas

**Endpoint:** `GET /api/reports/activities/{userId}/range?start=YYYY-MM-DD&end=YYYY-MM-DD`

**Ejemplo:** `GET /api/reports/activities/1/range?start=2025-12-01&end=2025-12-31`

**Respuesta:**
```json
[
  {
    "id": 1,
    "fecha": "2025-12-15",
    "tipoActividad": "Electricidad",
    "descripcion": "Consumo del hogar",
    "emisiones": 27.3,
    "activityId": "electricity-supply_grid-source_supplier_mix",
    "region": "CO"
  }
]
```

---

### 🔍 **Climatiq - Búsqueda**

#### 8. Buscar Activity IDs Disponibles

**Endpoint:** `GET /api/climatiq/search?query=TIPO&region=REGION`

**Ejemplo:** `GET /api/climatiq/search?query=electricity&region=CO`

**Respuesta:** JSON con todos los activity IDs disponibles para ese tipo y región.

---

## 🧪 Probar con Postman

### Configurar Postman

1. **Descarga Postman:** https://www.postman.com/downloads/
2. **Crea una nueva colección** llamada "EcoTrack"
3. **Agrega las peticiones** usando los ejemplos de arriba

### Flujo de Prueba Recomendado

1. **Registrar un usuario** → Guarda el `userId`
2. **Hacer login** → Confirma que funciona
3. **Buscar activity IDs** → Encuentra el que necesitas
4. **Registrar actividad** → Usa el `userId` que guardaste
5. **Ver totales** → Verifica que se calculó correctamente
6. **Ver estadísticas** → Revisa el desglose

---

## 🗂️ Estructura del Proyecto

```
ecotrack-backend/
├── src/
│   └── main/
│       ├── java/ecotrack/backend/
│       │   ├── carbonApi/
│       │   │   ├── controllers/
│       │   │   │   └── ClimatiqController.java
│       │   │   └── services/
│       │   │       └── ClimatiqService.java
│       │   ├── controllers/
│       │   │   ├── ActivityController.java
│       │   │   ├── AuthController.java
│       │   │   └── ReportController.java
│       │   ├── dto/
│       │   │   ├── AuthResponse.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── ActivityHistoryDTO.java
│       │   │   ├── TotalsReportDTO.java
│       │   │   └── StatsByTypeDTO.java
│       │   ├── models/
│       │   │   └── entitys/
│       │   │       ├── User.java
│       │   │       ├── DailyActivity.java
│       │   │       ├── CarbonRecord.java
│       │   │       └── CarbonTotal.java
│       │   ├── repositories/
│       │   │   ├── UserRepository.java
│       │   │   ├── DailyActivityRepository.java
│       │   │   ├── CarbonRecordRepository.java
│       │   │   └── CarbonTotalRepository.java
│       │   ├── services/
│       │   │   ├── AuthService.java
│       │   │   ├── ActivityService.java
│       │   │   └── ReportService.java
│       │   └── BackendApplication.java
│       └── resources/
│           └── application.properties
├── .env (NO SUBIR A GIT)
├── .gitignore
├── pom.xml
└── README.md
```

---

## 🔒 Seguridad y Buenas Prácticas

### ⚠️ NUNCA subas a GitHub:

- ❌ El archivo `.env`
- ❌ Credenciales de base de datos
- ❌ API keys
- ❌ Contraseñas

### ✅ Archivo `.gitignore`

Asegúrate de que tu `.gitignore` incluya:

```gitignore
# Environment variables
.env

# Maven
target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
*.class

# Logs
*.log

# OS
.DS_Store
Thumbs.db
```

---

## 📤 Subir a GitHub

### Paso 1: Crear Repositorio en GitHub

1. Ve a https://github.com/new
2. Nombre: `ecotrack-backend`
3. Descripción: "API REST para tracking de huella de carbono"
4. **NO inicialices** con README (ya lo tienes)
5. Click en **"Create repository"**

### Paso 2: Subir tu Código

```bash
# Inicializar git (si no lo has hecho)
git init

# Agregar archivos
git add .

# Hacer commit
git commit -m "Initial commit - EcoTrack Backend"

# Conectar con GitHub
git remote add origin https://github.com/TU_USUARIO/ecotrack-backend.git

# Subir
git push -u origin main
```

---

## 🤝 Compartir el Proyecto con Otro Desarrollador

### Instrucciones para el Frontend Developer:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/TU_USUARIO/ecotrack-backend.git
   cd ecotrack-backend
   ```

2. **Crear archivo `.env`** (copiar el contenido que te compartí por separado)

3. **Instalar dependencias:**
   ```bash
   mvn clean install
   ```

4. **Ejecutar:**
   ```bash
   mvn spring-boot:run
   ```

5. **Probar que funciona:**
   - Abre Postman
   - Prueba: `POST http://localhost:7878/api/auth/register`

---

## ❓ Solución de Problemas Comunes

### Error: "Column password does not exist"

**Solución:** Ve a Supabase y ejecuta:
```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255);
UPDATE users SET password = 'temporal123' WHERE password IS NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
```

### Error: "Port 7878 already in use"

**Solución:** Cambia el puerto en `application.properties`:
```properties
server.port=8080
```

### Error: "Could not resolve placeholder CLIMATIQ_API_KEY"

**Solución:** Verifica que el archivo `.env` existe y tiene las variables correctas.

### Error 404 en endpoints

**Solución:** Verifica que la aplicación esté corriendo y que uses la URL correcta.

---

## 📞 Soporte

Si tienes problemas:

1. Revisa la consola de Spring Boot para ver errores
2. Verifica que el archivo `.env` existe y está configurado
3. Asegúrate de que la base de datos de Supabase está activa
4. Contacta al equipo de desarrollo

---

## 📄 Licencia

Este proyecto es privado y de uso interno.

---

**¡Listo! Ya puedes empezar a usar el backend de EcoTrack** 🌱
