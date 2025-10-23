# ============================================
# DOCKERFILE PARA E-COMMERCE SPRING BOOT
# ============================================

# ============================================
# ETAPA 1: CONSTRUCCIÓN (Build)
# ============================================
FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar todo el código fuente
COPY src ./src

# Compilar el proyecto y generar el archivo .jar
RUN mvn clean package -DskipTests


# ============================================
# ETAPA 2: EJECUCIÓN (Runtime)
# ============================================
FROM amazoncorretto:21-alpine

# Información del mantenedor
LABEL maintainer="ldtb2001@gmail.com"
LABEL description="Sistema E-commerce GAMER con Spring Boot y Thymeleaf"

# Instalar wget para health check
RUN apk add --no-cache wget

# ✅ Crear usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# ✅ Crear directorios necesarios y dar permisos al usuario 'spring'
RUN mkdir -p /app/uploads/productos /app/uploads/categorias /app/uploads/marcas /app/uploads/boletas /app/logs && \
    chown -R spring:spring /app

# ✅ Copiar el .jar asignando propiedad al usuario 'spring'
COPY --chown=spring:spring --from=build /app/target/*.jar app.jar

# ✅ Ejecutar como usuario no-root
USER spring:spring

# Exponer el puerto 8080
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Comando para ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ============================================
# HEALTH CHECK
# ============================================
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/ || exit 1
