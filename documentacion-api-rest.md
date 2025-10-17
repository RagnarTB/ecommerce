# API REST - Sistema E-commerce

## 📋 Resumen General

Se han creado **5 controladores REST** que exponen APIs JSON para conectar cualquier frontend moderno (React, Vue, Angular, Mobile, etc.) con tu backend Spring Boot.

---

## 🎯 Controladores Creados

### 1. **ProductRestController** (`/api/productos`)
**Funcionalidades:**
- ✅ CRUD completo de productos
- ✅ Búsquedas (por nombre, categoría, marca, filtros avanzados)
- ✅ Productos destacados y con ofertas
- ✅ Gestión de stock (aumentar, disminuir, alertas)
- ✅ Gestión de imágenes (subir, eliminar, establecer principal)

**Endpoints principales:**
```
GET    /api/productos                    - Lista todos los productos
GET    /api/productos/{id}               - Obtiene un producto
POST   /api/productos                    - Crea un producto
PUT    /api/productos/{id}               - Actualiza un producto
DELETE /api/productos/{id}               - Elimina un producto

GET    /api/productos/buscar?nombre=...  - Busca por nombre
GET    /api/productos/categoria/{id}     - Filtra por categoría
GET    /api/productos/marca/{id}         - Filtra por marca
GET    /api/productos/destacados         - Productos destacados
GET    /api/productos/ofertas            - Productos con oferta

PUT    /api/productos/{id}/stock/aumentar?cantidad=10
PUT    /api/productos/{id}/stock/disminuir?cantidad=5
GET    /api/productos/stock/bajo         - Alerta de stock bajo
GET    /api/productos/stock/sin-stock    - Sin stock

POST   /api/productos/{id}/imagenes      - Sube imagen
DELETE /api/productos/imagenes/{imagenId}
PUT    /api/productos/imagenes/{imagenId}/principal
```

---

### 2. **CustomerRestController** (`/api/clientes`)
**Funcionalidades:**
- ✅ CRUD completo de clientes
- ✅ Búsquedas (por nombre, documento, avanzadas)
- ✅ Integración con API Decolecta (DNI/RUC)
- ✅ Validación de existencia
- ✅ Estadísticas

**Endpoints principales:**
```
GET    /api/clientes                     - Lista todos los clientes
GET    /api/clientes/{id}                - Obtiene un cliente
POST   /api/clientes                     - Crea un cliente
PUT    /api/clientes/{id}                - Actualiza un cliente
DELETE /api/clientes/{id}                - Elimina un cliente

GET    /api/clientes/buscar?nombre=...   - Busca por nombre
GET    /api/clientes/documento/{numero}  - Busca por documento
GET    /api/clientes/existe/{numero}     - Verifica existencia

POST   /api/clientes/obtener-o-crear     - Obtiene o crea desde API
GET    /api/clientes/consultar-api/{numero}

PUT    /api/clientes/{id}/estado         - Activa/desactiva
GET    /api/clientes/estadisticas        - Estadísticas generales
```

---

### 3. **SaleRestController** (`/api/ventas`)
**Funcionalidades:**
- ✅ Creación de ventas (contado y crédito)
- ✅ Consultas por periodo (día, mes, rango)
- ✅ Búsquedas por cliente
- ✅ Anulación de ventas
- ✅ Registro de abonos a créditos
- ✅ Estadísticas y reportes

**Endpoints principales:**
```
POST   /api/ventas                       - Crea una venta
GET    /api/ventas                       - Lista todas las ventas
GET    /api/ventas/{id}                  - Obtiene una venta
GET    /api/ventas/numero/{numero}       - Busca por número

GET    /api/ventas/cliente/{clienteId}   - Ventas de un cliente
GET    /api/ventas/dia                   - Ventas del día
GET    /api/ventas/mes                   - Ventas del mes
GET    /api/ventas/rango?fechaInicio=...&fechaFin=...

POST   /api/ventas/{id}/anular           - Anula una venta

POST   /api/ventas/creditos/{id}/abono   - Registra abono

GET    /api/ventas/estadisticas          - Estadísticas generales
GET    /api/ventas/estadisticas/dia      - Estadísticas del día
```

**Ejemplo de creación de venta:**
```json
POST /api/ventas
{
  "venta": {
    "cliente": { "id": 1 },
    "tipoPago": "CREDITO",
    "subtotal": 1000.00,
    "total": 1000.00
  },
  "detalles": [
    {
      "producto": { "id": 5 },
      "cantidad": 2,
      "precioUnitario": 500.00
    }
  ],
  "pagos": [],
  "numCuotas": 12
}
```

---

### 4. **OrderRestController** (`/api/pedidos`)
**Funcionalidades:**
- ✅ Creación de pedidos desde web
- ✅ Consultas por estado y cliente
- ✅ Confirmación y cancelación
- ✅ Conversión a venta
- ✅ Estadísticas

**Endpoints principales:**
```
POST   /api/pedidos                      - Crea un pedido
GET    /api/pedidos                      - Lista todos los pedidos
GET    /api/pedidos/{id}                 - Obtiene un pedido
GET    /api/pedidos/numero/{numero}      - Busca por número

GET    /api/pedidos/cliente/{clienteId}  - Pedidos de un cliente
GET    /api/pedidos/pendientes           - Pedidos pendientes

PUT    /api/pedidos/{id}/confirmar       - Confirma un pedido
PUT    /api/pedidos/{id}/cancelar        - Cancela un pedido

POST   /api/pedidos/{id}/convertir-venta - Convierte a venta

GET    /api/pedidos/estadisticas         - Estadísticas generales
```

**Ejemplo de conversión a venta:**
```json
POST /api/pedidos/123/convertir-venta
{
  "tipoPago": "CREDITO",
  "numCuotas": 6
}
```

---

### 5. **CreditRestController** (`/api/creditos`)
**Funcionalidades:**
- ✅ Consultas de créditos y cuotas
- ✅ Alertas de vencimientos
- ✅ Registro de abonos
- ✅ Anulación de créditos
- ✅ Estadísticas de cobranza
- ✅ Mantenimiento de estados

**Endpoints principales:**
```
GET    /api/creditos                     - Lista todos los créditos
GET    /api/creditos/{id}                - Obtiene un crédito
GET    /api/creditos/venta/{ventaId}     - Crédito de una venta
GET    /api/creditos/cliente/{clienteId} - Créditos de un cliente
GET    /api/creditos/activos             - Créditos activos

GET    /api/creditos/con-cuotas-vencidas - Alertas de vencidos
GET    /api/creditos/proximos-a-vencer?dias=7

GET    /api/creditos/{id}/cuotas         - Cuotas de un crédito
GET    /api/creditos/cuotas/vencidas     - Todas las cuotas vencidas
GET    /api/creditos/cuotas/vencen-hoy   - Cuotas que vencen hoy
GET    /api/creditos/cuotas/proximas-a-vencer?dias=7

POST   /api/creditos/{id}/abono          - Registra un abono
POST   /api/creditos/{id}/anular         - Anula un crédito

GET    /api/creditos/estadisticas        - Estadísticas generales
GET    /api/creditos/cliente/{id}/deuda  - Deuda total del cliente

POST   /api/creditos/actualizar-estados  - Actualiza estados de cuotas
```

**Ejemplo de registro de abono:**
```json
POST /api/creditos/10/abono
{
  "monto": 250.00,
  "metodoPago": "EFECTIVO",
  "referencia": "Abono cuota 3"
}
```

---

## 📦 Formato de Respuesta Estándar

Todas las APIs siguen el mismo formato JSON:

### ✅ Respuesta exitosa:
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": { ... },
  "total": 10
}
```

### ❌ Respuesta con error:
```json
{
  "success": false,
  "error": "Mensaje descriptivo del error"
}
```

---

## 🔐 Autenticación

Los controladores están preparados para trabajar con **Spring Security**. Algunos endpoints requieren autenticación:

```java
@AuthenticationPrincipal UserDetails userDetails
```

El usuario autenticado se obtiene automáticamente desde el token/sesión.

---

## 🚀 Códigos HTTP Utilizados

- **200 OK** - Operación exitosa
- **201 Created** - Recurso creado exitosamente
- **400 Bad Request** - Error de validación o datos incorrectos
- **404 Not Found** - Recurso no encontrado
- **500 Internal Server Error** - Error del servidor

---

## 📝 Notas Importantes

### 1. **Parsing de JSON**
Los métodos `parsearXXX()` están simplificados. En producción, deberías usar:
- `@RequestBody` con DTOs específicos
- `ObjectMapper` de Jackson para conversiones complejas

### 2. **Validación**
Considera agregar validaciones con:
- `@Valid` en los `@RequestBody`
- `@NotNull`, `@NotBlank`, `@Min`, `@Max` en DTOs

### 3. **Paginación**
Para listas grandes, implementa paginación:
```java
@GetMapping
public Page<Product> obtenerTodos(Pageable pageable) {
    return productService.obtenerTodos(pageable);
}
```

### 4. **CORS**
Para permitir requests desde otros dominios:
```java
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ProductRestController { ... }
```

O configurar globalmente en `SecurityConfig`.

### 5. **Documentación con Swagger**
Considera agregar Swagger/OpenAPI para documentación automática:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
</dependency>
```

---

## 🧪 Testing con Postman/cURL

### Ejemplo 1: Crear producto
```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Laptop HP",
    "descripcion": "Laptop HP 15.6 pulgadas",
    "precioBase": 2500.00,
    "stockActual": 10,
    "categoria": {"id": 1},
    "marca": {"id": 1}
  }'
```

### Ejemplo 2: Obtener productos
```bash
curl http://localhost:8080/api/productos
```

### Ejemplo 3: Crear venta
```bash
curl -X POST http://localhost:8080/api/ventas \
  -H "Content-Type: application/json" \
  -d '{
    "venta": {
      "cliente": {"id": 1},
      "tipoPago": "CONTADO"
    },
    "detalles": [
      {
        "producto": {"id": 5},
        "cantidad": 1,
        "precioUnitario": 2500.00
      }
    ],
    "pagos": [
      {
        "monto": 2500.00,
        "metodoPago": "EFECTIVO"
      }
    ]
  }'
```

---

## 🎨 Integración con Frontend

### React Example:
```javascript
// Obtener productos
const productos = await fetch('http://localhost:8080/api/productos')
  .then(res => res.json())
  .then(data => data.data);

// Crear venta
const venta = await fetch('http://localhost:8080/api/ventas', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    venta: { cliente: { id: 1 }, tipoPago: 'CONTADO' },
    detalles: [{ producto: { id: 5 }, cantidad: 1 }],
    pagos: [{ monto: 2500, metodoPago: 'EFECTIVO' }]
  })
}).then(res => res.json());
```

---

## ✅ Checklist de Implementación

- [x] ProductRestController
- [x] CustomerRestController
- [x] SaleRestController
- [x] OrderRestController
- [x] CreditRestController
- [ ] Configurar CORS
- [ ] Agregar validaciones con @Valid
- [ ] Implementar paginación
- [ ] Documentar con Swagger
- [ ] Testing unitario
- [ ] Manejo global de excepciones

---

## 📚 Recursos Adicionales

- **Spring Boot REST**: https://spring.io/guides/gs/rest-service/
- **Spring Security**: https://spring.io/guides/gs/securing-web/
- **Swagger/OpenAPI**: https://springdoc.org/

---

🎉 **¡Tus APIs REST están listas para conectar con cualquier frontend moderno!**