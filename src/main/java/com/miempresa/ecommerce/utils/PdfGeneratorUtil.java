package com.miempresa.ecommerce.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * UTILIDAD: GENERADOR DE PDFs
 * 
 * Genera boletas y comprobantes en PDF usando iText.
 * 
 * NOTA: Esta es una implementación básica.
 * Para producción, considera usar plantillas más profesionales.
 */

@Slf4j
public class PdfGeneratorUtil {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        /**
         * Genera una boleta en PDF
         * 
         * @param venta            Venta para generar la boleta
         * @param empresaNombre    Nombre de la empresa
         * @param empresaRuc       RUC de la empresa
         * @param empresaDireccion Dirección de la empresa
         * @return Array de bytes del PDF generado
         */
        public static byte[] generateBoletaPdf(Sale venta, String empresaNombre,
                        String empresaRuc, String empresaDireccion)
                        throws IOException {

                log.info("Generando boleta PDF para venta: {}", venta.getNumeroVenta());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                        // Crear documento PDF
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf);

                        // ========================================
                        // ENCABEZADO
                        // ========================================

                        document.add(new Paragraph(empresaNombre)
                                        .setFontSize(18)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph("RUC: " + empresaRuc)
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph(empresaDireccion)
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph("\n"));

                        // ========================================
                        // TÍTULO
                        // ========================================

                        document.add(new Paragraph("BOLETA DE VENTA")
                                        .setFontSize(14)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph(venta.getNumeroVenta())
                                        .setFontSize(12)
                                        .setTextAlignment(TextAlignment.CENTER));

                        document.add(new Paragraph("\n"));

                        // ========================================
                        // DATOS DEL CLIENTE
                        // ========================================

                        document.add(new Paragraph("DATOS DEL CLIENTE")
                                        .setFontSize(11)
                                        .setBold());

                        document.add(new Paragraph(
                                        "Cliente: " + venta.getCliente().getNombreCompleto()));

                        document.add(new Paragraph(
                                        "Documento: " + venta.getCliente().getTipoDocumento() +
                                                        " - " + venta.getCliente().getNumeroDocumento()));

                        document.add(new Paragraph(
                                        "Fecha: " + venta.getFechaVenta().format(DATE_FORMAT)));

                        document.add(new Paragraph(
                                        "Vendedor: " + venta.getUsuario().getNombreCompleto()));

                        document.add(new Paragraph("\n"));

                        // ========================================
                        // DETALLE DE PRODUCTOS
                        // ========================================

                        document.add(new Paragraph("DETALLE DE LA COMPRA")
                                        .setFontSize(11)
                                        .setBold());

                        // Crear tabla
                        float[] columnWidths = { 3, 1, 1, 1 };
                        Table table = new Table(columnWidths);
                        table.setWidth(500);

                        // Encabezados
                        table.addHeaderCell("Producto");
                        table.addHeaderCell("Cant.");
                        table.addHeaderCell("P. Unit.");
                        table.addHeaderCell("Subtotal");

                        // Filas de productos
                        for (SaleDetail detalle : venta.getDetalles()) {
                                table.addCell(detalle.getNombreProducto());
                                table.addCell(String.valueOf(detalle.getCantidad()));
                                table.addCell("S/ " + detalle.getPrecioUnitario());
                                table.addCell("S/ " + detalle.getSubtotal());
                        }

                        document.add(table);
                        document.add(new Paragraph("\n"));

                        // ========================================
                        // TOTALES
                        // ========================================

                        document.add(new Paragraph("Subtotal: S/ " + venta.getSubtotal())
                                        .setTextAlignment(TextAlignment.RIGHT));

                        if (venta.getDescuento() != null &&
                                        venta.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                                document.add(new Paragraph("Descuento: S/ " + venta.getDescuento())
                                                .setTextAlignment(TextAlignment.RIGHT));
                        }

                        if (venta.getCostoEnvio() != null &&
                                        venta.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0) {
                                document.add(new Paragraph("Envío: S/ " + venta.getCostoEnvio())
                                                .setTextAlignment(TextAlignment.RIGHT));
                        }

                        document.add(new Paragraph("IGV (18%): S/ " + venta.getIgv())
                                        .setTextAlignment(TextAlignment.RIGHT));

                        document.add(new Paragraph("TOTAL: S/ " + venta.getTotal())
                                        .setFontSize(14)
                                        .setBold()
                                        .setTextAlignment(TextAlignment.RIGHT));

                        document.add(new Paragraph("\n"));

                        // ========================================
                        // INFORMACIÓN ADICIONAL
                        // ========================================

                        document.add(new Paragraph("Tipo de Pago: " + venta.getTipoPago().getNombre())
                                        .setFontSize(9));

                        document.add(new Paragraph("\n"));

                        document.add(new Paragraph("Gracias por su compra")
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.CENTER));

                        // Cerrar documento
                        document.close();

                        log.info("Boleta PDF generada exitosamente");

                } catch (Exception e) {
                        log.error("Error al generar boleta PDF: {}", e.getMessage(), e);
                        throw new IOException("Error al generar PDF", e);
                }

                return baos.toByteArray();
        }

        /**
         * Guarda el PDF en un archivo
         * 
         * @param pdfBytes Bytes del PDF
         * @param filePath Ruta donde guardar el archivo
         */
        public static void savePdfToFile(byte[] pdfBytes, String filePath) throws IOException {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(pdfBytes);
                        log.info("PDF guardado en: {}", filePath);
                }
        }
}

/**
 * USO EN LOS SERVICES:
 * 
 * // Generar boleta
 * byte[] pdf = PdfGeneratorUtil.generateBoletaPdf(
 * venta,
 * "MI EMPRESA SAC",
 * "20123456789",
 * "Av. Principal 123, Lima"
 * );
 * 
 * // Guardar en servidor
 * String fileName = venta.getNumeroVenta() + ".pdf";
 * PdfGeneratorUtil.savePdfToFile(pdf, "uploads/boletas/" + fileName);
 * 
 * // O enviar directamente al cliente (download)
 * HttpHeaders headers = new HttpHeaders();
 * headers.setContentType(MediaType.APPLICATION_PDF);
 * headers.setContentDispositionFormData("attachment", fileName);
 * return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
 */