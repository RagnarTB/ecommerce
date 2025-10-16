# ============================================
# DOCKERFILE PARA E-COMMERCE SPRING BOOT
# ============================================
# Este archivo crea una imagen Docker con tu aplicación

# ============================================
# ETAPA 1: CONSTRUCCIÓN (Build)
# ============================================
# Usamos Maven con Java 21 para compilar el proyecto
FROM maven:3.9-amazoncorretto-21 AS build

# Establecer directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (esto se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar todo el código fuente
COPY src ./src

# Compilar el proyecto y generar el archivo .jar
# -DskipTests: omite las pruebas para compilar más rápido
RUN mvn clean package -DskipTests

# ============================================
# ETAPA 2: EJECUCIÓN (Runtime)
# ============================================
# Imagen más ligera solo con Java para ejecutar la aplicación
FROM amazoncorretto:21-alpine

# Información del mantenedor
LABEL maintainer="ldtb2001@gmail.com"
LABEL description="Sistema E-commerce con Spring Boot y Thymeleaf"

# Crear un usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Crear directorio para archivos subidos
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copiar el archivo .jar generado en la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar al usuario no-root
USER spring:spring

# Exponer el puerto 8080
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Comando para ejecutar la aplicación
# java -jar app.jar ejecuta tu aplicación Spring Boot
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ============================================
# HEALTH CHECK
# ============================================
# Verifica que la aplicación esté funcionando
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1