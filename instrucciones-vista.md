# 📝 INSTRUCCIONES DE USO - VISTAS LOGIN Y DASHBOARD

¡Perfecto Leonardo! Has creado exitosamente las vistas base del sistema. Aquí te explico todo.

---

## ✅ ARCHIVOS CREADOS

### 1️⃣ **login.html**
- **Ubicación**: `src/main/resources/templates/login.html`
- **Función**: Página de inicio de sesión
- **Características**:
  - Diseño moderno con AdminLTE 3
  - Gradiente morado/azul
  - Muestra mensajes de error/logout
  - Credenciales de prueba visibles
  - Checkbox "Recordarme"

### 2️⃣ **admin/fragments/sidebar.html**
- **Ubicación**: `src/main/resources/templates/admin/fragments/sidebar.html`
- **Función**: Menú lateral dinámico
- **Características**:
  - Muestra módulos según permisos del usuario
  - **Administrador** ve: Dashboard, Productos, Ventas, Pedidos, Clientes, Proveedores, Inventario, Reportes, Usuarios, Configuración
  - **Trabajador** ve solo: Dashboard, Productos, Ventas, Clientes, Reportes
  - Menús desplegables con submenús
  - Badge de pedidos pendientes
  - Link al catálogo público

### 3️⃣ **admin/fragments/header.html**
- **Ubicación**: `src/main/resources/templates/admin/fragments/header.html`
- **Función**: Barra superior con notificaciones
- **Características**:
  - Búsqueda rápida de productos
  - Notificaciones de:
    - Pedidos pendientes (badge naranja)
    - Stock bajo (badge rojo)
    - Cuotas vencidas (badge rojo)
  - Menú de usuario con perfil y logout
  - Botón fullscreen

### 4️⃣ **admin/dashboard.html**
- **Ubicación**: `src/main/resources/templates/admin/dashboard.html`
- **Función**: Panel principal del sistema
- **Características**:
  - **Small Boxes**: Ventas del día, pedidos pendientes, total productos, total clientes
  - **Info Boxes**: Stock bajo, créditos activos, deuda total, cuotas vencidas
  - **Tabla de últimas ventas** del día
  - **Widget de productos con stock bajo**
  - **Tabla de pedidos pendientes** con botón de confirmar
  - **Accesos rápidos**: POS, Nuevo Producto, Clientes, etc.

### 5️⃣ **admin/fragments/footer.html**
- **Ubicación**: `src/main/resources/templates/admin/fragments/footer.html`
- **Función**: Footer y scripts reutilizables
- **Incluye**:
  - Copyright y versión
  - Scripts de jQuery, Bootstrap, AdminLTE
  - SweetAlert2 para alertas bonitas
  - Funciones JavaScript comunes

### 6️⃣ **admin/fragments/base.html**
- **Ubicación**: `src/main/resources/templates/admin/fragments/base.html`
- **Función**: Layout base reutilizable
- **Uso**: Para crear nuevas páginas rápidamente
- **Incluye**:
  - Estructura HTML completa
  - Header, Sidebar, Footer automáticos
  - Manejo de alertas (success, error, warning, info)
  - DataTables configurado en español

---

## 🗂️ ESTRUCTURA DE CARPETAS

Debes crear esta estructura en tu proyecto:

```
src/main/resources/templates/
├── login.html
├── admin/
│   ├── dashboard.html
│   └── fragments/
│       ├── header.html
│       ├── sidebar.html
│       ├── footer.html
│       └── base.html
```

---

## 🚀 CÓMO PROBAR EL LOGIN Y DASHBOARD

### PASO 1: Verificar que los archivos estén en su lugar

```bash
# Verificar estructura
src/main/resources/templates/
  ✓ login.html
  ✓ admin/dashboard.html
  ✓ admin/fragments/header.html
  ✓ admin/fragments/sidebar.html
  ✓ admin/fragments/footer.html
  ✓ admin/fragments/base.html
```

### PASO 2: Ejecutar la aplicación

```bash
# Con Docker (RECOMENDADO)
docker-compose up --build

# Sin Docker
mvn spring-boot:run
```

### PASO 3: Acceder al sistema

1. Abre tu navegador en: **http://localhost:8080/login**

2. Usa las credenciales:
   - **Administrador**: `admin` / `admin123`
   - **Trabajador**: `trabajador` / `trabajador123`

3. Después de login, serás redirigido a: **http://localhost:8080/admin/dashboard**

---

## 🎨 CARACTERÍSTICAS DEL DASHBOARD

### Para ADMINISTRADOR verás:
- ✅ Todas las estadísticas
- ✅ Todos los módulos en el sidebar
- ✅ Todas las notificaciones
- ✅ Acceso a configuración y usuarios

### Para TRABAJADOR verás:
- ✅ Estadísticas limitadas
- ✅ Solo módulos: Productos, Ventas, Clientes, Reportes
- ❌ No verás: Usuarios, Configuración, Proveedores, Inventario completo

---

## 📊 DATOS QUE MUESTRA EL DASHBOARD

El dashboard obtiene estos datos del **DashboardController**:

```java
// Ventas del día
BigDecimal ventasDelDia

// Pedidos pendientes
long pedidosPendientes

// Total productos
long totalProductos

// Total clientes
long totalClientes

// Productos con stock bajo
int productosStockBajo

// Créditos activos
long creditosActivos

// Deuda total pendiente
BigDecimal deudaTotal

// Cuotas vencidas
long cuotasVencidas

// Listas
List<Sale> ultimasVentas
List<Order> ultimosPedidos
List<Product> productosAlerta
```

---

## 🔧 PERSONALIZACIÓN

### Cambiar colores del login

Edita `login.html` línea 28-30:

```css
.login-page {
    background: linear-gradient(135deg, #TU_COLOR1 0%, #TU_COLOR2 100%);
}
```

### Cambiar logo

Edita `sidebar.html` línea 17:

```html
<img src="/img/tu-logo.png" alt="Logo">
```

### Agregar más estadísticas

Edita `dashboard.html` y agrega más **small-box**:

```html
<div class="col-lg-3 col-6">
    <div class="small-box bg-purple">
        <div class="inner">
            <h3 th:text="${tuEstadistica}">0</h3>
            <p>Tu Nueva Estadística</p>
        </div>
        <div class="icon">
            <i class="fas fa-tu-icono"></i>
        </div>
        <a href="#" class="small-box-footer">
            Ver más <i class="fas fa-arrow-circle-right"></i>
        </a>
    </div>
</div>
```

---

## 🎯 PRÓXIMOS PASOS

Ahora que tienes Login y Dashboard funcionando, puedes crear:

1. **CRUD de Productos** (lista, formulario, detalle)
2. **Punto de Venta (POS)** - interfaz de ventas
3. **Gestión de Pedidos** - confirmar y convertir a ventas
4. **Gestión de Clientes** - ver y buscar clientes
5. **Gestión de Créditos** - cuotas y abonos
6. **Catálogo Público** - home, catálogo, carrito, checkout

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Problema 1: "Error al cargar el dashboard"

**Causa**: Algún servicio retorna null

**Solución**: Verifica que todos los services estén retornando datos por defecto:

```java
// En el controller
model.addAttribute("ventasDelDia", ventasDelDia != null ? ventasDelDia : BigDecimal.ZERO);
```

### Problema 2: "No se ve el sidebar"

**Causa**: Thymeleaf no encuentra el fragment

**Solución**: Verifica la ruta:

```html
<!-- Correcto -->
<div th:replace="~{admin/fragments/sidebar :: sidebar}"></div>

<!-- Incorrecto -->
<div th:replace="admin/fragments/sidebar"></div>
```

### Problema 3: "Estilos de AdminLTE no cargan"

**Causa**: CDN bloqueado o sin internet

**Solución**: Descarga AdminLTE y ponlo en `/static/adminlte/`

```html
<!-- Cambiar de CDN a local -->
<link rel="stylesheet" href="/adminlte/css/adminlte.min.css">
```

### Problema 4: "sec:authorize no funciona"

**Causa**: Falta dependencia de Thymeleaf Spring Security

**Solución**: Agrega al `pom.xml`:

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

---

## 📚 RECURSOS ÚTILES

- **AdminLTE**: https://adminlte.io/docs/3.2/
- **Thymeleaf**: https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html
- **Font Awesome Icons**: https://fontawesome.com/icons
- **Bootstrap 4**: https://getbootstrap.com/docs/4.6/

---

## ✅ CHECKLIST DE VERIFICACIÓN

Antes de continuar, verifica que:

- [ ] Login se muestra correctamente
- [ ] Puedes hacer login con admin/admin123
- [ ] Dashboard carga sin errores
- [ ] Sidebar muestra los módulos correctos según rol
- [ ] Header muestra notificaciones
- [ ] Estadísticas muestran valores (aunque sean 0)
- [ ] Logout funciona correctamente
- [ ] Alerts se auto-cierran después de 5 segundos

---

## 🎉 ¡FELICIDADES!

Has completado exitosamente:
- ✅ Login funcional con diseño moderno
- ✅ Dashboard completo con estadísticas
- ✅ Sidebar dinámico según permisos
- ✅ Header con notificaciones
- ✅ Sistema de fragments reutilizables

**Estás listo para continuar con las demás vistas del sistema.**

---

## 💬 ¿SIGUIENTE PASO?

¿Qué quieres crear ahora?

**A)** CRUD de Productos (lista, form, detalle)  
**B)** Punto de Venta (POS)  
**C)** Gestión de Pedidos  
**D)** Catálogo Público (home, catálogo, carrito)  

Responde con la letra y continuamos. 🚀