# 🛒 SISTEMA E-COMMERCE - CATÁLOGO Y VENTAS

Sistema completo de gestión de catálogo de productos, ventas, créditos e inventario desarrollado con Spring Boot 3 y MySQL.

---

## 📋 DESCRIPCIÓN DEL PROYECTO

Sistema web de catálogo de productos (NO tienda online) donde:

- **Catálogo público**: Los clientes navegan productos sin necesidad de registro
- **Carrito temporal**: Almacenado en sesión del navegador
- **Punto de Venta (POS)**: Sistema para confirmar ventas (presencial y web)
- **Pedidos → Ventas**: Los pedidos se convierten en ventas cuando se confirman
- **Gestión de Créditos**: Ventas a cuotas con control de pagos y abonos
- **Sistema de Permisos**: Roles de Administrador y Trabajador con permisos por módulo
- **API Decolecta**: Integración para consulta de DNI/RUC automática
- **Inventario**: Control de stock con movimientos de entrada/salida

---

## 🎯 CARACTERÍSTICAS PRINCIPALES

### 👥 Gestión de Usuarios
- **Roles**: Administrador (acceso total) y Trabajador (acceso limitado)
- **Permisos por módulo**: Productos, Ventas, Clientes, Reportes
- **Sin registro de clientes**: Los clientes se registran automáticamente al hacer un pedido

### 📦 Gestión de Productos
- Categorías y Marcas
- Hasta 5 imágenes por producto (1 principal)
- Control de stock con alertas de stock mínimo
- Productos destacados y ofertas

### 💰 Sistema de Ventas
- **Tipos de pago**: Contado y Crédito
- **Métodos de pago**: Efectivo, Tarjeta, Transferencia, Yape
- **Multipagos**: Combinación de métodos en una venta
- **Créditos**: Hasta 24 cuotas con abonos parciales
- **Estados de cuota**: Pendiente, Pagada, Vencida, Pagada Parcial
- **Generación de boletas en PDF**

### 🛒 Flujo de Compra
1. Cliente navega catálogo público
2. Agrega productos al carrito (sesión)
3. En checkout ingresa DNI o RUC
4. Sistema busca en BD o consulta API Decolecta
5. Se genera PEDIDO (estado: PENDIENTE)
6. Admin confirma → se convierte en VENTA
7. Se descuenta stock automáticamente
8. Se genera boleta en PDF

### 📊 Inventario
- Movimientos de entrada (Compra, Ajuste, Devolución)
- Movimientos de salida (Venta, Ajuste, Merma)
- Auditoría completa de movimientos
- Gestión de proveedores

### 🎨 Configuración Personalizable
- Logo y colores del sitio
- Slider principal (4 imágenes)
- Footer (dirección, teléfono, redes sociales)
- Texto de bienvenida y horarios

---

## 🛠️ TECNOLOGÍAS UTILIZADAS

### Backend
- **Java 21**
- **Spring Boot 3.2.x**
- **Spring Security** (autenticación y autorización)
- **Spring Data JPA** (ORM)
- **Thymeleaf** (motor de plantillas)
- **Lombok** (reducción de código boilerplate)

### Base de Datos
- **MySQL 8**

### Frontend
- **AdminLTE 3** (panel administrativo)
- **Bootstrap 5** (catálogo público)
- **jQuery**

### Herramientas
- **Maven** (gestión de dependencias)
- **Docker & Docker Compose** (contenedores)
- **Git** (control de versiones)

### Librerías Adicionales
- **iText 7** (generación de PDFs)
- **Apache POI** (exportación a Excel)
- **Gson** (procesamiento JSON)

---

## 📋 REQUISITOS PREVIOS

Antes de instalar, asegúrate de tener instalado:

1. **Java 21** o superior
   - Descargar: https://adoptium.net/
   - Verificar: `java -version`

2. **Maven 3.8+**
   - Descargar: https://maven.apache.org/download.cgi
   - Verificar: `mvn -version`

3. **Docker Desktop**
   - Descargar: https://www.docker.com/products/docker-desktop
   - Verificar: `docker --version` y `docker-compose --version`

4. **Git**
   - Descargar: https://git-scm.com/
   - Verificar: `git --version`

5. **IDE** (opcional pero recomendado)
   - IntelliJ IDEA Community Edition
   - Visual Studio Code con extensiones de Java

---

## 🚀 INSTALACIÓN Y CONFIGURACIÓN

### PASO 1: Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/ecommerce.git
cd ecommerce
```

### PASO 2: Configurar API Decolecta

Edita `src/main/resources/application.properties`:

```properties
# API DECOLECTA (reemplaza con tu token)
api.decolecta.token=sk_9739.xfKEozZkKVg69oje8RACY8preIWY6nwh
api.decolecta.base-url=https://api.decolecta.com/v1
api.decolecta.reniec-endpoint=/reniec/dni
api.decolecta.sunat-endpoint=/sunat/ruc
```

### PASO 3: Ejecutar con Docker

#### Opción A: Docker Compose (RECOMENDADO)

```bash
# Construir y levantar los contenedores
docker-compose up --build

# Detener los contenedores
docker-compose down

# Ver logs
docker-compose logs -f app
```

La aplicación estará disponible en: **http://localhost:8080**

#### Opción B: Ejecutar localmente (sin Docker)

1. **Instalar MySQL localmente** y crear la base de datos:

```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Configurar conexión** en `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=root
spring.datasource.password=tu_password_mysql
```

3. **Compilar y ejecutar**:

```bash
mvn clean install
mvn spring-boot:run
```

---

## 🔑 CREDENCIALES POR DEFECTO

Al iniciar por primera vez, el sistema crea automáticamente:

### Administrador
- **Usuario**: `admin`
- **Contraseña**: `admin123`
- **Acceso**: Todos los módulos

### Trabajador
- **Usuario**: `trabajador`
- **Contraseña**: `trabajador123`
- **Acceso**: Productos, Ventas, Clientes, Reportes

**⚠️ IMPORTANTE**: Cambiar estas contraseñas en producción.

---

## 📁 ESTRUCTURA DEL PROYECTO

```
ecommerce/
├── src/
│   ├── main/
│   │   ├── java/com/miempresa/ecommerce/
│   │   │   ├── config/              # Configuraciones (Security, Web)
│   │   │   ├── controllers/
│   │   │   │   ├── admin/           # Controllers del panel admin
│   │   │   │   └── web/             # Controllers del catálogo público
│   │   │   ├── models/              # Entidades JPA
│   │   │   │   └── enums/           # Enumeraciones
│   │   │   ├── repositories/        # Repositorios JPA
│   │   │   ├── services/            # Lógica de negocio
│   │   │   │   └── impl/            # Implementaciones
│   │   │   ├── security/            # Spring Security
│   │   │   ├── utils/               # Utilidades (PDFs, archivos, API)
│   │   │   └── EcommerceApplication.java
│   │   └── resources/
│   │       ├── templates/           # Vistas Thymeleaf
│   │       │   ├── admin/           # Vistas del panel admin
│   │       │   ├── web/             # Vistas del catálogo público
│   │       │   └── fragments/       # Componentes reutilizables
│   │       ├── static/              # CSS, JS, imágenes
│   │       └── application.properties
│   └── test/                        # Tests unitarios
├── uploads/                         # Archivos subidos (creado automáticamente)
│   ├── productos/
│   ├── categorias/
│   ├── boletas/
│   └── config/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🌐 ENDPOINTS PRINCIPALES

### Panel Administrativo (`/admin`)

| Ruta | Método | Descripción | Permiso |
|------|--------|-------------|---------|
| `/login` | GET/POST | Página de login | Público |
| `/admin/dashboard` | GET | Dashboard principal | Autenticado |
| `/admin/productos` | GET | Lista de productos | MODULO_PRODUCTOS |
| `/admin/productos/nuevo` | GET/POST | Crear producto | MODULO_PRODUCTOS |
| `/admin/ventas` | GET | Lista de ventas | MODULO_VENTAS |
| `/admin/ventas/pos` | GET | Punto de venta | MODULO_VENTAS |
| `/admin/pedidos` | GET | Lista de pedidos | MODULO_VENTAS |
| `/admin/creditos` | GET | Gestión de créditos | MODULO_VENTAS |
| `/admin/clientes` | GET | Lista de clientes | MODULO_CLIENTES |
| `/admin/usuarios` | GET | Gestión de usuarios | ADMIN |
| `/admin/configuracion` | GET | Configuración del sistema | ADMIN |

### Catálogo Público (`/`)

| Ruta | Método | Descripción |
|------|--------|-------------|
| `/` | GET | Página principal |
| `/productos` | GET | Catálogo de productos |
| `/producto/{id}` | GET | Detalle de producto |
| `/carrito` | GET | Ver carrito |
| `/carrito/agregar` | POST | Agregar al carrito |
| `/carrito/actualizar` | POST | Actualizar cantidad |
| `/carrito/eliminar` | POST | Eliminar del carrito |
| `/checkout` | GET/POST | Checkout y creación de pedido |

---

## 📊 BASE DE DATOS

### Tablas Principales

1. **usuarios** - Usuarios del sistema
2. **perfiles** - Roles (Administrador, Trabajador)
3. **permisos** - Permisos por módulo
4. **clientes** - Clientes registrados
5. **categorias** - Categorías de productos
6. **marcas** - Marcas de productos
7. **productos** - Productos del catálogo
8. **imagenes_producto** - Imágenes de productos
9. **proveedores** - Proveedores de stock
10. **movimientos_inventario** - Historial de movimientos
11. **pedidos** - Pedidos desde web
12. **detalles_pedido** - Productos de cada pedido
13. **ventas** - Ventas confirmadas
14. **detalles_venta** - Productos de cada venta
15. **pagos** - Pagos realizados
16. **creditos** - Ventas a crédito
17. **cuotas** - Cuotas de créditos
18. **pagos_cuotas** - Distribución de pagos en cuotas
19. **configuraciones** - Configuraciones del sistema

---

## 🔧 CONFIGURACIÓN AVANZADA

### Cambiar Puerto de la Aplicación

Edita `application.properties`:

```properties
server.port=9090
```

### Habilitar HTTPS (Producción)

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=tu_password
server.ssl.key-store-type=PKCS12
```

### Configurar Email (notificaciones)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_password_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🐛 TROUBLESHOOTING (Solución de Problemas)

### Problema 1: La aplicación no inicia

**Error**: `Connection refused: connect`

**Solución**: Verifica que MySQL esté corriendo:

```bash
docker-compose ps
```

Si MySQL no está activo:

```bash
docker-compose up -d mysql
```

---

### Problema 2: Error de permisos en uploads/

**Error**: `Access Denied` al subir archivos

**Solución**:

```bash
# Linux/Mac
chmod -R 777 uploads/

# Windows (ejecutar como administrador)
icacls uploads /grant Everyone:(OI)(CI)F /T
```

---

### Problema 3: La base de datos no se crea

**Solución**: Crear manualmente la base de datos:

```bash
docker-compose exec mysql mysql -uroot -proot
```

```sql
CREATE DATABASE IF NOT EXISTS ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### Problema 4: No se cargan las imágenes

**Verificar**:
1. Que la carpeta `uploads/` existe
2. Que los archivos tienen permisos de lectura
3. Que la ruta en `application.properties` es correcta

---

### Problema 5: API Decolecta no responde

**Verificar**:
1. Token correcto en `application.properties`
2. Conexión a internet activa
3. Revisar logs: `docker-compose logs -f app`

---

## 📚 DOCUMENTACIÓN ADICIONAL

### ¿Cómo agregar un nuevo producto?

1. Ir a `/admin/productos`
2. Clic en "Nuevo Producto"
3. Llenar formulario (nombre, precio, categoría, marca, stock)
4. Subir hasta 5 imágenes
5. Seleccionar imagen principal
6. Guardar

### ¿Cómo hacer una venta?

#### Opción A: POS (Punto de Venta)
1. Ir a `/admin/ventas/pos`
2. Buscar productos y agregar al carrito
3. Ingresar DNI/RUC del cliente
4. Seleccionar método de pago (contado o crédito)
5. Si es multipago: agregar varios métodos
6. Confirmar venta
7. Descargar boleta en PDF

#### Opción B: Desde Pedido Web
1. Ir a `/admin/pedidos`
2. Ver pedidos pendientes
3. Hacer clic en "Confirmar"
4. Seleccionar método de pago
5. Confirmar conversión a venta
6. El pedido cambia a estado "Confirmado"
7. Se genera la venta automáticamente

### ¿Cómo registrar un abono a crédito?

1. Ir a `/admin/creditos`
2. Buscar el crédito
3. Ver detalle del crédito
4. Hacer clic en "Registrar Abono"
5. Ingresar monto y método de pago
6. El sistema distribuye proporcionalmente entre cuotas
7. Actualiza estado de cuotas

---

## 🔐 SEGURIDAD

### Recomendaciones de Producción

1. **Cambiar contraseñas por defecto**
2. **Usar HTTPS** (certificado SSL)
3. **Configurar firewall** (solo puertos 80, 443)
4. **Hacer backups regulares** de la base de datos
5. **Actualizar dependencias** regularmente
6. **Configurar límites de rate limiting**
7. **Usar variables de entorno** para credenciales

### Backup de Base de Datos

```bash
# Exportar
docker-compose exec mysql mysqldump -uroot -proot ecommerce_db > backup.sql

# Importar
docker-compose exec -T mysql mysql -uroot -proot ecommerce_db < backup.sql
```

---

## 📝 LICENCIA

Este proyecto es de uso interno. Todos los derechos reservados.

---

## 👨‍💻 AUTOR

**Leonardo** - Desarrollador Principal

---

## 📧 SOPORTE

Para reportar problemas o solicitar nuevas funcionalidades:

- Email: soporte@miempresa.com
- Issue Tracker: https://github.com/tu-usuario/ecommerce/issues

---

## 🎉 AGRADECIMIENTOS

- Spring Boot Team
- AdminLTE Contributors
- Comunidad de Java
- API Decolecta

---

## 📅 HISTORIAL DE VERSIONES

### v1.0.0 (2025-10-16)
- ✅ Sistema base completo
- ✅ Gestión de productos
- ✅ Sistema de ventas y créditos
- ✅ Panel administrativo
- ✅ Catálogo público
- ✅ Integración API Decolecta
- ✅ Generación de boletas en PDF

---

**¡Gracias por usar el Sistema E-Commerce!** 🚀