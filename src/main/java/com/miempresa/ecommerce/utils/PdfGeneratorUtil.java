package com.miempresa.ecommerce.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.miempresa.ecommerce.models.Credit;
import com.miempresa.ecommerce.models.Product;
import com.miempresa.ecommerce.models.Sale;
import com.miempresa.ecommerce.models.SaleDetail;

import lombok.extern.slf4j.Slf4j;

/**
 * UTILIDAD: GENERADOR DE PDFs
 * 
 * Genera boletas, comprobantes y reportes en PDF usando iText.
 * 
 * NOTA: Esta es una implementación básica.
 * Para producción, considera usar plantillas más profesionales.
 */

@Slf4j
public class PdfGeneratorUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

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
     * Genera un reporte de ventas en PDF
     * 
     * @param ventas        Lista de ventas
     * @param total         Total de ventas
     * @param inicio        Fecha de inicio del período
     * @param fin           Fecha de fin del período
     * @param empresaNombre Nombre de la empresa
     * @param empresaRuc    RUC de la empresa
     * @return Array de bytes del PDF generado
     */
    public static byte[] generateReporteVentasPdf(
            List<Sale> ventas,
            BigDecimal total,
            LocalDateTime inicio,
            LocalDateTime fin,
            String empresaNombre,
            String empresaRuc) throws IOException {

        log.info("Generando reporte de ventas PDF");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
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

            document.add(new Paragraph("\n"));

            // ========================================
            // TÍTULO
            // ========================================

            document.add(new Paragraph("REPORTE DE VENTAS")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "Período: " + inicio.format(DATE_FORMAT) + " - " + fin.format(DATE_FORMAT))
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(
                    "Fecha de generación: " + LocalDateTime.now().format(DATE_FORMAT))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // ========================================
            // RESUMEN
            // ========================================

            document.add(new Paragraph("RESUMEN EJECUTIVO")
                    .setFontSize(12)
                    .setBold());

            document.add(new Paragraph("Total de ventas realizadas: " + ventas.size()));
            document.add(new Paragraph("Monto total recaudado: S/ " + total)
                    .setBold());

            if (!ventas.isEmpty()) {
                BigDecimal promedioVenta = total.divide(
                        BigDecimal.valueOf(ventas.size()),
                        2,
                        BigDecimal.ROUND_HALF_UP);
                document.add(new Paragraph("Promedio por venta: S/ " + promedioVenta));
            }

            document.add(new Paragraph("\n"));

            // ========================================
            // TABLA DE VENTAS
            // ========================================

            if (!ventas.isEmpty()) {
                document.add(new Paragraph("DETALLE DE VENTAS")
                        .setFontSize(12)
                        .setBold());

                float[] columnWidths = { 2, 3, 2, 1.5f };
                Table table = new Table(columnWidths);
                table.setWidth(500);

                // Encabezados
                table.addHeaderCell("Fecha");
                table.addHeaderCell("Cliente");
                table.addHeaderCell("Nº Venta");
                table.addHeaderCell("Total");

                // Filas de ventas
                for (Sale venta : ventas) {
                    table.addCell(venta.getFechaVenta().format(DATE_FORMAT));
                    table.addCell(venta.getCliente().getNombreCompleto());
                    table.addCell(venta.getNumeroVenta());
                    table.addCell("S/ " + venta.getTotal());
                }

                document.add(table);
            } else {
                document.add(new Paragraph("No se encontraron ventas en el período seleccionado.")
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(new Paragraph("\n"));

            // ========================================
            // PIE DE PÁGINA
            // ========================================

            document.add(new Paragraph("_".repeat(80))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Este documento fue generado automáticamente por el sistema")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER));

            // Cerrar documento
            document.close();

            log.info("Reporte PDF generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte PDF: {}", e.getMessage(), e);
            throw new IOException("Error al generar reporte PDF", e);
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

    /**
     * ✅ NUEVO: Genera PDF de reporte de inventario
     *
     * @param productos     Lista de productos
     * @param empresaNombre Nombre de la empresa
     * @param empresaRuc    RUC de la empresa
     * @return Array de bytes del PDF generado
     */
    public static byte[] generateReporteInventarioPdf(List<Product> productos,
            String empresaNombre, String empresaRuc) throws IOException {

        log.info("Generando reporte PDF de inventario con {} productos", productos.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Encabezado
            document.add(new Paragraph(empresaNombre)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("RUC: " + empresaRuc)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("REPORTE DE INVENTARIO")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DATE_FORMAT))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Tabla de productos
            float[] columnWidths = { 3f, 2f, 2f, 1.5f, 2f, 2f };
            Table table = new Table(columnWidths);
            table.setWidth(520);

            // Encabezados
            table.addHeaderCell(new Paragraph("Producto").setBold());
            table.addHeaderCell(new Paragraph("SKU").setBold());
            table.addHeaderCell(new Paragraph("Categoría").setBold());
            table.addHeaderCell(new Paragraph("Stock").setBold());
            table.addHeaderCell(new Paragraph("Precio").setBold());
            table.addHeaderCell(new Paragraph("Valor Total").setBold());

            // Datos
            BigDecimal valorTotalInventario = BigDecimal.ZERO;

            for (Product producto : productos) {
                if (producto == null)
                    continue;

                table.addCell(producto.getNombre() != null ? producto.getNombre() : "");
                table.addCell(producto.getCodigoSku() != null ? producto.getCodigoSku() : "");
                table.addCell(
                        producto.getCategoria() != null && producto.getCategoria().getNombre() != null
                                ? producto.getCategoria().getNombre()
                                : "");

                Integer stock = producto.getStockActual() != null ? producto.getStockActual() : 0;
                table.addCell(stock.toString());

                BigDecimal precio = producto.getPrecioActual() != null ? producto.getPrecioActual()
                        : BigDecimal.ZERO;
                table.addCell("S/ " + precio.setScale(2, java.math.RoundingMode.HALF_UP).toString());

                BigDecimal valorTotal = precio.multiply(new BigDecimal(stock));
                valorTotalInventario = valorTotalInventario.add(valorTotal);
                table.addCell("S/ " + valorTotal.setScale(2, java.math.RoundingMode.HALF_UP).toString());
            }

            document.add(table);

            // Total
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Valor Total del Inventario: S/ "
                    + valorTotalInventario.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("Total de Productos: " + productos.size())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();

            log.info("Reporte PDF de inventario generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte PDF de inventario: {}", e.getMessage(), e);
            throw new IOException("Error al generar reporte PDF de inventario", e);
        }

        return baos.toByteArray();
    }

    /**
     * ✅ NUEVO: Genera PDF de reporte de créditos
     *
     * @param creditos       Lista de créditos activos
     * @param deudaTotal     Deuda total pendiente
     * @param cuotasVencidas Cantidad de cuotas vencidas
     * @param empresaNombre  Nombre de la empresa
     * @param empresaRuc     RUC de la empresa
     * @return Array de bytes del PDF generado
     */
    public static byte[] generateReporteCreditosPdf(List<Credit> creditos,
            BigDecimal deudaTotal, int cuotasVencidas,
            String empresaNombre, String empresaRuc) throws IOException {

        log.info("Generando reporte PDF de créditos con {} créditos activos", creditos.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Encabezado
            document.add(new Paragraph(empresaNombre)
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("RUC: " + empresaRuc)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("REPORTE DE CRÉDITOS Y COBRANZAS")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DATE_FORMAT))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Resumen
            document.add(new Paragraph("RESUMEN EJECUTIVO")
                    .setFontSize(12)
                    .setBold());

            document.add(new Paragraph("Total de Créditos Activos: " + creditos.size()));
            document.add(new Paragraph("Deuda Total Pendiente: S/ "
                    + (deudaTotal != null ? deudaTotal.setScale(2, java.math.RoundingMode.HALF_UP).toString()
                            : "0.00")));
            document.add(new Paragraph("Cuotas Vencidas: " + cuotasVencidas)
                    .setFontColor(cuotasVencidas > 0 ? com.itextpdf.kernel.colors.ColorConstants.RED : null));

            document.add(new Paragraph("\n"));

            // Tabla de créditos
            float[] columnWidths = { 3f, 2f, 2f, 2f, 1.5f };
            Table table = new Table(columnWidths);
            table.setWidth(520);

            // Encabezados
            table.addHeaderCell(new Paragraph("Cliente").setBold());
            table.addHeaderCell(new Paragraph("Venta").setBold());
            table.addHeaderCell(new Paragraph("Monto Total").setBold());
            table.addHeaderCell(new Paragraph("Pendiente").setBold());
            table.addHeaderCell(new Paragraph("Cuotas").setBold());

            // Datos
            for (Credit credito : creditos) {
                if (credito == null || credito.getCliente() == null)
                    continue;

                table.addCell(credito.getCliente().getNombreCompleto() != null
                        ? credito.getCliente().getNombreCompleto()
                        : "");
                table.addCell(credito.getVenta() != null && credito.getVenta().getNumeroVenta() != null
                        ? credito.getVenta().getNumeroVenta()
                        : "");

                BigDecimal montoTotal = credito.getMontoTotal() != null ? credito.getMontoTotal() : BigDecimal.ZERO;
                table.addCell("S/ " + montoTotal.setScale(2, java.math.RoundingMode.HALF_UP).toString());

                BigDecimal montoPendiente = credito.getMontoPendiente() != null ? credito.getMontoPendiente()
                        : BigDecimal.ZERO;
                table.addCell("S/ " + montoPendiente.setScale(2, java.math.RoundingMode.HALF_UP).toString());

                table.addCell(credito.getCuotasPagadas() + " / "
                        + (credito.getNumCuotas() != null ? credito.getNumCuotas() : 0));
            }

            document.add(table);

            document.close();

            log.info("Reporte PDF de créditos generado exitosamente");

        } catch (Exception e) {
            log.error("Error al generar reporte PDF de créditos: {}", e.getMessage(), e);
            throw new IOException("Error al generar reporte PDF de créditos", e);
        }

        return baos.toByteArray();
    }

    /**
     * Genera nombre de archivo único para un reporte
     *
     * @param tipoReporte Tipo de reporte (ventas, productos, etc.)
     * @return Nombre de archivo con timestamp
     */
    public static String generarNombreArchivo(String tipoReporte) {
        return "reporte-" + tipoReporte + "-" +
                LocalDateTime.now().format(FILE_DATE_FORMAT) + ".pdf";
    }
}