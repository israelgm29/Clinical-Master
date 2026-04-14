package com.mycompany.hospitalgeneral.util.reports;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.IOException;

/**
 * Estilos centralizados para reportes médicos Basado en el diseño de tu CSS
 * (--primary: #0b3c5d, --accent: #2a9d8f)
 */
public final class ReportStyles {

    // Colores institucionales (de tu CSS)
    public static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(11, 60, 93);      // #0b3c5d
    public static final DeviceRgb PRIMARY_LIGHT = new DeviceRgb(29, 95, 139);   // #1d5f8b
    public static final DeviceRgb ACCENT_COLOR = new DeviceRgb(42, 157, 143);   // #2a9d8f
    public static final DeviceRgb SUCCESS_COLOR = new DeviceRgb(42, 157, 143);  // #2a9d8f
    public static final DeviceRgb DANGER_COLOR = new DeviceRgb(214, 40, 40);    // #d62828
    public static final DeviceRgb WARNING_COLOR = new DeviceRgb(244, 162, 97);  // #f4a261

    // Grises
    public static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 249, 251);    // #f8f9fb
    public static final DeviceRgb BORDER_COLOR = new DeviceRgb(224, 224, 224);  // #e0e0e0
    public static final DeviceRgb TEXT_MUTED = new DeviceRgb(108, 117, 125);    // #6c757d

    // Fuentes
    public static PdfFont getRegularFont() throws IOException {
        return PdfFontFactory.createFont("Helvetica");
    }

    public static PdfFont getBoldFont() throws IOException {
        return PdfFontFactory.createFont("Helvetica-Bold");
    }

    // Bordes comunes
    public static final Border BORDER_LIGHT = new SolidBorder(BORDER_COLOR, 1);
    public static final Border BORDER_PRIMARY = new SolidBorder(PRIMARY_COLOR, 2);

    // Alineaciones
    public static final TextAlignment ALIGN_CENTER = TextAlignment.CENTER;
    public static final TextAlignment ALIGN_LEFT = TextAlignment.LEFT;
    public static final TextAlignment ALIGN_RIGHT = TextAlignment.RIGHT;

    // Tamaños de fuente
    public static final float FONT_SIZE_TITLE = 20f;
    public static final float FONT_SIZE_HEADER = 14f;
    public static final float FONT_SIZE_SECTION = 11f;
    public static final float FONT_SIZE_NORMAL = 10f;
    public static final float FONT_SIZE_SMALL = 9f;
    public static final float FONT_SIZE_TINY = 8f;

    public static Paragraph createSectionTitle(String title, PdfFont font) {
        return new Paragraph(title)
                .setFont(font)
                .setFontSize(FONT_SIZE_SECTION)
                .setFontColor(PRIMARY_COLOR)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(4)
                .setMarginTop(8)
                .setMarginBottom(6);
    }

    private ReportStyles() {
        // Clase utilitaria, no instanciar
    }
}
