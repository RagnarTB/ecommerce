# 🐳 GUÍA DE COMANDOS DOCKER - E-COMMERCE

## 📋 Pre-requisitos

1. **Docker Desktop instalado**
   - Windows: https://www.docker.com/products/docker-desktop
   - Mac: https://www.docker.com/products/docker-desktop
   - Linux: `sudo apt install docker.io docker-compose`

2. **Verificar instalación**
   ```bash
   docker --version
   docker-compose --version
   ```

---

## 🚀 INICIAR LA APLICACIÓN

### Primera vez (construir imágenes)
```bash
# Construir y levantar los contenedores
docker-compose up --build

# O en segundo plano (detached mode)
docker-compose up --build -d
```

### Después de la primera vez
```bash
# Levantar los contenedores existentes
docker-compose up

# O en segundo plano
docker-compose up -d
```

---

## 🛑 DETENER LA APLICACIÓN

### Detener contenedores (conserva datos)
```bash
docker-compose down
```

### Detener y eliminar volúmenes (⚠️ BORRA LA BASE DE DATOS)
```bash
docker-compose down -v
```

### Detener y eliminar todo (imágenes, volúmenes, redes)
```bash
docker-compose down --rmi all -v
```

---

## 📊 MONITOREO

### Ver logs en tiempo real
```bash
# Logs de todos los contenedores
docker-compose logs -f

# Logs solo de la aplicación
docker-compose logs -f app

# Logs solo de MySQL
docker-compose logs -f mysql

# Últimas 100 líneas
docker-compose logs --tail=100 app
```

### Ver estado de contenedores
```bash
docker-compose ps
```

### Ver uso de recursos
```bash
docker stats
```

---

## 🔧 COMANDOS ÚTILES

### Entrar al contenedor de la aplicación
```bash
docker exec -it ecommerce_app sh
```

### Entrar a MySQL
```bash
# Opción 1: Desde el contenedor
docker exec -it ecommerce_mysql mysql -uroot -proot123 ecommerce_db

# Opción 2: Desde tu PC (requiere cliente MySQL instalado)
mysql -h 127.0.0.1 -P 3306 -uroot -proot123 ecommerce_db
```

### Reiniciar solo un contenedor
```bash
docker-compose restart app
docker-compose restart mysql
```

### Reconstruir solo la aplicación
```bash
docker-compose up --build app
```

### Ver volúmenes
```bash
docker volume ls
```

### Inspeccionar volumen
```bash
docker volume inspect ecommerce_mysql_data
docker volume inspect ecommerce_uploads_data
```

---

## 🐛 RESOLUCIÓN DE PROBLEMAS

### Problema: "Puerto 3306 ya está en uso"
```bash
# Detener MySQL local
# Windows (Servicios)
services.msc → MySQL → Detener

# Mac/Linux
sudo service mysql stop
```

### Problema: "Puerto 8080 ya está en uso"
```bash
# Ver qué proceso usa el puerto
# Windows
netstat -ano | findstr :8080

# Mac/Linux
lsof -i :8080

# Matar el proceso
# Windows
taskkill /PID <PID> /F

# Mac/Linux
kill -9 <PID>
```

### Problema: Contenedor no inicia
```bash
# Ver logs detallados
docker-compose logs app

# Verificar health check
docker inspect ecommerce_mysql | grep Health
```

### Problema: Base de datos corrupta
```bash
# Eliminar volumen de MySQL y empezar de cero
docker-compose down -v
docker volume rm ecommerce_mysql_data
docker-compose up --build
```

### Problema: Cambios en código no se reflejan
```bash
# Reconstruir imagen sin caché
docker-compose build --no-cache app
docker-compose up -d
```

---

## 🔄 WORKFLOW DE DESARROLLO

### 1. Desarrollo local (SIN Docker)
```bash
# Editar código
# Usar application.properties (no application-prod.properties)
mvn spring-boot:run
```

### 2. Probar en Docker
```bash
# Reconstruir y levantar
docker-compose up --build

# Ver logs
docker-compose logs -f app
```

### 3. Commit y push
```bash
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main
```

---

## 📦 BACKUP Y RESTORE

### Backup de la base de datos
```bash
# Crear backup
docker exec ecommerce_mysql mysqldump -uroot -proot123 ecommerce_db > backup.sql

# Backup con fecha
docker exec ecommerce_mysql mysqldump -uroot -proot123 ecommerce_db > backup_$(date +%Y%m%d).sql
```

### Restore de la base de datos
```bash
# Restaurar desde backup
docker exec -i ecommerce_mysql mysql -uroot -proot123 ecommerce_db < backup.sql
```

### Backup de archivos subidos
```bash
# Crear backup del volumen
docker run --rm -v ecommerce_uploads_data:/data -v $(pwd):/backup alpine tar czf /backup/uploads_backup.tar.gz -C /data .

# Restaurar backup
docker run --rm -v ecommerce_uploads_data:/data -v $(pwd):/backup alpine tar xzf /backup/uploads_backup.tar.gz -C /data
```

---

## 🌐 ACCESO A LA APLICACIÓN

- **Aplicación Web**: http://localhost:8080
- **MySQL**: localhost:3306
- **Usuario Admin**: 
  - Username: `admin`
  - Password: `admin123`

---

## 📝 VARIABLES DE ENTORNO

Puedes sobrescribir variables en `docker-compose.yml`:

```yaml
environment:
  EMPRESA_NOMBRE: "Mi Tienda"
  EMPRESA_RUC: "20123456789"
  APP_UPLOAD_DIR: "/app/uploads/"
```

---

## ⚠️ IMPORTANTE

1. **NUNCA** subas `docker-compose.yml` con credenciales reales a GitHub
2. **CAMBIA** la contraseña del admin en producción
3. **HABILITA** HTTPS en producción
4. **CONFIGURA** backups automáticos en producción
5. **USA** variables de entorno para secretos

---

## 🎯 COMANDOS RÁPIDOS

```bash
# Iniciar todo
docker-compose up -d

# Ver logs
docker-compose logs -f app

# Detener todo
docker-compose down

# Reiniciar app
docker-compose restart app

# Limpiar todo y empezar de cero
docker-compose down -v --rmi all
docker-compose up --build -d
```

---

## 📞 SOPORTE

Si tienes problemas:
1. Revisa los logs: `docker-compose logs`
2. Verifica que los puertos estén libres
3. Asegúrate que Docker Desktop esté corriendo
4. Prueba reconstruir sin caché: `docker-compose build --no-cache`