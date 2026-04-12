package com.mycompany.hospitalgeneral.util.reports;

import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.UnitValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades comunes para generación de PDFs
 */
public final class PdfGeneratorUtil {
    
    /**
     * Genera número de reporte único
     * Formato: RPT-20260412-0001
     */
    public static String generateReportNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", (int) (Math.random() * 10000));
        return "RPT-" + date + "-" + sequence;
    }
    
    /**
     * Formatea fecha y hora
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Formatea solo fecha
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Crea tabla de 2 columnas para etiqueta-valor
     */
    public static Table createLabelValueTable(String[][] data, com.itextpdf.kernel.font.PdfFont labelFont, 
                                               com.itextpdf.kernel.font.PdfFont valueFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        for (String[] row : data) {
            table.addCell(createLabelCell(row[0], labelFont));
            table.addCell(createValueCell(row[1], valueFont));
        }
        
        return table;
    }
    
    /**
     * Celda de etiqueta (gris, negrita)
     */
    public static Cell createLabelCell(String text, com.itextpdf.kernel.font.PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setBackgroundColor(ReportStyles.LIGHT_GRAY)
            .setPadding(4);
    }
    
    /**
     * Celda de valor
     */
    public static Cell createValueCell(String text, com.itextpdf.kernel.font.PdfFont font) {
        return new Cell()
            .add(new Paragraph(text != null ? text : "N/A").setFont(font).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setPadding(4);
    }
    
    /**
     * Celda de encabezado de tabla
     */
    public static Cell createHeaderCell(String text, com.itextpdf.kernel.font.PdfFont font) {
        return new Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setBackgroundColor(ReportStyles.PRIMARY_COLOR)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
            .setPadding(4);
    }
    
    /**
     * Crea separador de línea
     */
    public static LineSeparator createLineSeparator() {
        return new LineSeparator((ILineDrawer) new SolidBorder(ReportStyles.PRIMARY_COLOR, 2))
            .setWidth(UnitValue.createPercentValue(100));
    }
    
    /**
     * Crea título de sección con fondo gris
     */
    public static Paragraph createSectionTitle(String title, com.itextpdf.kernel.font.PdfFont font) {
        return new Paragraph(title)
            .setFont(font)
            .setFontSize(ReportStyles.FONT_SIZE_SECTION)
            .setFontColor(ReportStyles.PRIMARY_COLOR)
            .setBackgroundColor(ReportStyles.LIGHT_GRAY)
            .setPadding(4)
            .setMarginBottom(8);
    }
    
    /**
     * Calcula edad desde fecha de nacimiento
     */
    public static int calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) return 0;
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }
    
    private PdfGeneratorUtil() {
        // Clase utilitaria
    }
}