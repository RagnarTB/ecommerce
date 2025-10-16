package com.miempresa.ecommerce.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * UTILIDAD: SUBIDA DE ARCHIVOS
 * 
 * Maneja la subida, eliminación y gestión de archivos.
 */

@Slf4j
public class FileUploadUtil {

    /**
     * Sube un archivo al servidor
     * 
     * @param uploadDir Directorio destino (ej: "uploads/productos")
     * @param file      Archivo a subir
     * @return Nombre del archivo guardado
     */
    public static String saveFile(String uploadDir, MultipartFile file) throws IOException {

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Directorio creado: {}", uploadPath);
        }

        // Generar nombre único para el archivo
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Ruta completa del archivo
        Path filePath = uploadPath.resolve(fileName);

        // Guardar archivo
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Archivo guardado: {}", fileName);

        return fileName;
    }

    /**
     * Elimina un archivo del servidor
     * 
     * @param uploadDir Directorio donde está el archivo
     * @param fileName  Nombre del archivo
     */
    public static void deleteFile(String uploadDir, String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Archivo eliminado: {}", fileName);
            } else {
                log.warn("Archivo no encontrado: {}", fileName);
            }

        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage(), e);
        }
    }

    /**
     * Genera un nombre único para el archivo
     * 
     * @param originalFileName Nombre original del archivo
     * @return Nombre único (UUID + extensión)
     */
    private static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Obtiene la extensión de un archivo
     * 
     * @param fileName Nombre del archivo
     * @return Extensión (.jpg, .png, .pdf, etc.)
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex == -1) {
            return "";
        }

        return fileName.substring(lastDotIndex);
    }

    /**
     * Valida si el archivo es una imagen
     * 
     * @param file Archivo a validar
     * @return true si es imagen, false si no
     */
    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            return false;
        }

        return contentType.startsWith("image/");
    }

    /**
     * Valida el tamaño del archivo
     * 
     * @param file        Archivo a validar
     * @param maxSizeInMB Tamaño máximo en MB
     * @return true si es válido, false si excede el tamaño
     */
    public static boolean isValidFileSize(MultipartFile file, long maxSizeInMB) {
        long maxSizeInBytes = maxSizeInMB * 1024 * 1024;
        return file.getSize() <= maxSizeInBytes;
    }

    /**
     * Limpia el directorio eliminando archivos antiguos
     * 
     * @param uploadDir Directorio a limpiar
     * @param daysOld   Eliminar archivos más antiguos que X días
     */
    public static void cleanOldFiles(String uploadDir, long daysOld) {
        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000);

            Files.list(uploadPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Archivo antiguo eliminado: {}", path.getFileName());
                        } catch (IOException e) {
                            log.error("Error al eliminar archivo antiguo: {}", e.getMessage());
                        }
                    });

        } catch (IOException e) {
            log.error("Error al limpiar archivos antiguos: {}", e.getMessage(), e);
        }
    }
}

/**
 * USO EN LOS SERVICES:
 * 
 * // Subir imagen
 * String fileName = FileUploadUtil.saveFile("uploads/productos", file);
 * 
 * // Validar antes de subir
 * if (!FileUploadUtil.isImageFile(file)) {
 * throw new RuntimeException("Solo se permiten imágenes");
 * }
 * 
 * if (!FileUploadUtil.isValidFileSize(file, 5)) {
 * throw new RuntimeException("El archivo no puede superar 5MB");
 * }
 * 
 * // Eliminar imagen
 * FileUploadUtil.deleteFile("uploads/productos", "abc-123.jpg");
 * 
 * // Limpiar archivos antiguos (ejecutar cada noche)
 * FileUploadUtil.cleanOldFiles("uploads/temp", 7); // Elimina archivos de más
 * de 7 días
 */