package com.mycompany.hospitalgeneral.util.reports;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mycompany.hospitalgeneral.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static java.lang.classfile.Attributes.record;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generador de PDF para reportes de consulta médica
 * Usa iText 9.5
 */
public class ConsultationPdfGenerator {
    
    private PdfFont regularFont;
    private PdfFont boldFont;
    
    public byte[] generate(Medicalrecord record, String reportNumber) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            // Inicializar fuentes
            this.regularFont = ReportStyles.getRegularFont();
            this.boldFont = ReportStyles.getBoldFont();
            
            // Crear documento
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(36, 36, 36, 36);
            
            // Construir secciones
            buildHeader(document, reportNumber);
            buildPatientInfo(document, record.getPatientid());
            buildVitalSigns(document, record);
            buildConsultationData(document, record);
            buildCros(document, record);
            buildRpe(document, record);
            buildDiagnostics(document, record);
            buildExams(document, record);
            buildPrescriptions(document, record);
            buildFooter(document, record);
            buildSignatures(document, record);
            
            document.close();
            
        } catch (IOException e) {
            throw new RuntimeException("Error generando PDF de consulta: " + e.getMessage(), e);
        }
        
        return baos.toByteArray();
    }
    
    // ==================== SECCIONES ====================
    
    private void buildHeader(Document doc, String reportNumber) {
        // Nombre del hospital
        Paragraph hospital = new Paragraph("HOSPITAL GENERAL")
            .setFont(boldFont)
            .setFontSize(ReportStyles.FONT_SIZE_TITLE)
            .setFontColor(ReportStyles.PRIMARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER);
        
        Paragraph subtitle = new Paragraph("Reporte de Consulta Médica")
            .setFont(regularFont)
            .setFontSize(ReportStyles.FONT_SIZE_NORMAL)
            .setFontColor(ReportStyles.TEXT_MUTED)
            .setTextAlignment(TextAlignment.CENTER);
        
        doc.add(hospital);
        doc.add(subtitle);
        doc.add(PdfGeneratorUtil.createLineSeparator());
        
        // Info del reporte
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        
        infoTable.addCell(new Cell()
            .add(new Paragraph("N° Reporte: " + reportNumber).setFont(boldFont).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setBorder(Border.NO_BORDER));
        
        infoTable.addCell(new Cell()
            .add(new Paragraph("Fecha: " + PdfGeneratorUtil.formatDateTime(LocalDateTime.now()))
                .setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(Border.NO_BORDER));
        
        doc.add(infoTable);
        doc.add(new Paragraph("\n"));
    }
    
    private void buildPatientInfo(Document doc, Patient patient) {
        doc.add(ReportStyles.createSectionTitle("INFORMACIÓN DEL PACIENTE", boldFont));
        
        String[][] data = {
            {"Nombre:", patient.getLastname() + " " + patient.getFirstname()},
            {"HC:", patient.getHc()},
            {"Edad:", PdfGeneratorUtil.calculateAge(patient.getBirthday()) + " años"},
            {"Sexo:", patient.getSex() != null ? patient.getSex().getName(): "N/A"},
            {"Tipo Sangre:", patient.getBlootype() != null ? patient.getBlootype().getName(): "N/A"},
            {"Identificación:", patient.getDni() != null ? patient.getDni() : patient.getPassport()}
        };
        
        doc.add(PdfGeneratorUtil.createLabelValueTable(data, boldFont, regularFont));
        doc.add(new Paragraph("\n"));
    }
    
    private void buildVitalSigns(Document doc, Medicalrecord record) {
        List<Vitalsign> signs = record.getVitalsignCollection() != null 
            ? record.getVitalsignCollection().stream().collect(Collectors.toList())
            : List.of();
        
        if (signs.isEmpty()) return;
        
        doc.add(ReportStyles.createSectionTitle("SIGNOS VITALES", boldFont));
        
        Vitalsign last = signs.get(signs.size() - 1);
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Fila 1
        table.addCell(createVitalCell("Presión Arterial", 
            last.getSystolicpressure() + "/" + last.getDiastolicpressure()));
        table.addCell(createVitalCell("Pulso", last.getPulse() + " bpm"));
        table.addCell(createVitalCell("Temperatura", last.getTemperature() + " °C"));
        table.addCell(createVitalCell("Frec. Respiratoria", last.getBreathingfrequency() + " rpm"));
        
        // Fila 2
        table.addCell(createVitalCell("SpO2", last.getOxygensaturation() + "%"));
        table.addCell(createVitalCell("Peso", last.getWeight() + " kg"));
        table.addCell(createVitalCell("Talla", last.getTall() + " cm"));
        table.addCell(createVitalCell("IMC", last.getMass() != null ? last.getMass().toString() : "N/A"));
        
        doc.add(table);
        doc.add(new Paragraph("\n"));
    }
    
    private void buildConsultationData(Document doc, Medicalrecord record) {
        doc.add(ReportStyles.createSectionTitle("MOTIVO DE CONSULTA", boldFont));
        doc.add(new Paragraph(safeText(record.getReason()))
            .setFont(regularFont)
            .setFontSize(ReportStyles.FONT_SIZE_NORMAL));
        
        doc.add(ReportStyles.createSectionTitle("ENFERMEDAD ACTUAL", boldFont));
        doc.add(new Paragraph(safeText(record.getCurrentillness()))
            .setFont(regularFont)
            .setFontSize(ReportStyles.FONT_SIZE_NORMAL));
        
        doc.add(new Paragraph("\n"));
    }
    
    private void buildCros(Document doc, Medicalrecord record) {
        if (record.getCrosCollection() == null || record.getCrosCollection().isEmpty()) return;
        
        Cros cros = record.getCrosCollection().iterator().next();
        doc.add(ReportStyles.createSectionTitle("REVISIÓN POR SISTEMAS", boldFont));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        addSystemCheck(table, "Órganos de los sentidos", cros.getSenseorgans());
        addSystemCheck(table, "Respiratorio", cros.getRespiratory());
        addSystemCheck(table, "Cardiovascular", cros.getCardiovascular());
        addSystemCheck(table, "Digestivo", cros.getDigestive());
        addSystemCheck(table, "Genital", cros.getGenital());
        addSystemCheck(table, "Urinario", cros.getUrinary());
        addSystemCheck(table, "Musculoesquelético", cros.getSkeletalmuscle());
        addSystemCheck(table, "Endocrino", cros.getEndocrine());
        addSystemCheck(table, "Linfático/Hematológico", cros.getLymphaticheme());
        addSystemCheck(table, "Nervioso", cros.getNervous());
        
        doc.add(table);
        
        if (hasText(cros.getObservations())) {
            doc.add(new Paragraph("Observaciones:").setFont(boldFont).setFontSize(ReportStyles.FONT_SIZE_SMALL));
            doc.add(new Paragraph(cros.getObservations()).setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_SMALL));
        }
        doc.add(new Paragraph("\n"));
    }
    
    private void buildRpe(Document doc, Medicalrecord record) {
        if (record.getRpeCollection() == null || record.getRpeCollection().isEmpty()) return;
        
        Rpe rpe = record.getRpeCollection().iterator().next();
        doc.add(ReportStyles.createSectionTitle("REVISIÓN FÍSICA POR REGIONES", boldFont));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        addSystemCheck(table, "Cabeza", rpe.getHead());
        addSystemCheck(table, "Cuello", rpe.getNeck());
        addSystemCheck(table, "Tórax", rpe.getChest());
        addSystemCheck(table, "Abdomen", rpe.getAbdomen());
        addSystemCheck(table, "Pelvis", rpe.getPelvis());
        addSystemCheck(table, "Extremidades", rpe.getExtremities());
        
        doc.add(table);
        
        if (hasText(rpe.getObservations())) {
            doc.add(new Paragraph("Observaciones:").setFont(boldFont).setFontSize(ReportStyles.FONT_SIZE_SMALL));
            doc.add(new Paragraph(rpe.getObservations()).setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_SMALL));
        }
        doc.add(new Paragraph("\n"));
    }
    
    private void buildDiagnostics(Document doc, Medicalrecord record) {
        doc.add(ReportStyles.createSectionTitle("DIAGNÓSTICOS", boldFont));
        
        List<Diagnostic> diags = record.getDiagnosticCollection() != null
            ? record.getDiagnosticCollection().stream()
                .filter(d -> d.getDeleted() == null || !d.getDeleted())
                .collect(Collectors.toList())
            : List.of();
        
        if (diags.isEmpty()) {
            doc.add(new Paragraph("Sin diagnósticos registrados").setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_NORMAL));
        } else {
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            table.addHeaderCell(PdfGeneratorUtil.createHeaderCell("Código", boldFont));
            table.addHeaderCell(PdfGeneratorUtil.createHeaderCell("Descripción", boldFont));
            
            for (Diagnostic d : diags) {
                table.addCell(PdfGeneratorUtil.createValueCell(d.getDiseaseid().getCode(), regularFont));
                table.addCell(PdfGeneratorUtil.createValueCell(d.getDiseaseid().getName(), regularFont));
            }
            doc.add(table);
        }
        doc.add(new Paragraph("\n"));
    }
    
    private void buildExams(Document doc, Medicalrecord record) {
        doc.add(ReportStyles.createSectionTitle("EXÁMENES SOLICITADOS", boldFont));
        
        List<Medicalexam> exams = record.getMedicalexamCollection() != null
            ? record.getMedicalexamCollection().stream()
                .filter(e -> e.getDeleted() == null || !e.getDeleted())
                .collect(Collectors.toList())
            : List.of();
        
        if (exams.isEmpty()) {
            doc.add(new Paragraph("No se solicitaron exámenes").setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_NORMAL));
        } else {
            for (Medicalexam exam : exams) {
                doc.add(new Paragraph("• " + exam.getExamid().getName())
                    .setFont(regularFont)
                    .setFontSize(ReportStyles.FONT_SIZE_NORMAL));
            }
        }
        doc.add(new Paragraph("\n"));
    }
    
    private void buildPrescriptions(Document doc, Medicalrecord record) {
        doc.add(ReportStyles.createSectionTitle("PRESCRIPCIONES", boldFont));
        
        List<Prescription> prescriptions = record.getPrescriptionCollection() != null
            ? record.getPrescriptionCollection().stream().collect(Collectors.toList())
            : List.of();
        
        if (prescriptions.isEmpty()) {
            doc.add(new Paragraph("No se prescribieron medicamentos").setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_NORMAL));
        } else {
            int i = 1;
            for (Prescription p : prescriptions) {
                // Nombre y dosis en negrita
                Paragraph med = new Paragraph()
                    .add(new Text(i + ". " + p.getMedication()).setFont(boldFont))
                    .add(new Text(p.getDose() != null ? " - " + p.getDose() : "").setFont(regularFont))
                    .setFontSize(ReportStyles.FONT_SIZE_NORMAL);
                
                // Detalles
                String details = String.format("   Vía: %s | Frecuencia: %s | Duración: %s",
                    safeText(p.getRoute()),
                    safeText(p.getFrequency()),
                    safeText(p.getDuration()));
                
                Paragraph det = new Paragraph(details)
                    .setFont(regularFont)
                    .setFontSize(ReportStyles.FONT_SIZE_SMALL)
                    .setFontColor(ReportStyles.TEXT_MUTED);
                
                doc.add(med);
                doc.add(det);
                
                if (hasText(p.getInstructions())) {
                    doc.add(new Paragraph("   Indicaciones: " + p.getInstructions())
                        .setFont(regularFont)
                        .setFontSize(ReportStyles.FONT_SIZE_SMALL));
                }
                i++;
            }
        }
        doc.add(new Paragraph("\n"));
    }
    
    private void buildFooter(Document doc, Medicalrecord record) {
        Paragraph footer = new Paragraph("Documento generado electrónicamente el " 
            + PdfGeneratorUtil.formatDateTime(java.time.LocalDateTime.now()))
            .setFont(regularFont)
            .setFontSize(ReportStyles.FONT_SIZE_TINY)
            .setFontColor(ReportStyles.TEXT_MUTED)
            .setTextAlignment(TextAlignment.CENTER);
        
        doc.add(footer);
    }
    
    private void buildSignatures(Document doc, Medicalrecord record) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Firma médico
        String medicName = record.getMedicid() != null 
            ? "Dr./Dra. " + record.getMedicid().getLastname() + " " + record.getMedicid().getFirstname()
            : "Médico no registrado";
        
        Cell medicCell = new Cell()
            .add(new Paragraph("\n\n\n"))
            .add(new Paragraph("_________________________").setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph(medicName).setFont(boldFont).setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph("Médico Tratante").setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_TINY).setTextAlignment(TextAlignment.CENTER))
            .setBorder(Border.NO_BORDER);
        
        // Firma paciente
        Cell patientCell = new Cell()
            .add(new Paragraph("\n\n\n"))
            .add(new Paragraph("_________________________").setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph("Firma del Paciente").setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_TINY).setTextAlignment(TextAlignment.CENTER))
            .setBorder(Border.NO_BORDER);
        
        table.addCell(medicCell);
        table.addCell(patientCell);
        
        doc.add(table);
    }
    
    // ==================== HELPERS ====================
    
    private Cell createVitalCell(String label, String value) {
        return new Cell()
            .add(new Paragraph(label).setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_TINY).setFontColor(ReportStyles.TEXT_MUTED))
            .add(new Paragraph(value != null ? value : "N/A").setFont(boldFont).setFontSize(ReportStyles.FONT_SIZE_NORMAL).setFontColor(ReportStyles.PRIMARY_COLOR))
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(6)
            .setBorder(new SolidBorder(ReportStyles.BORDER_COLOR, 1));
    }
    
    private void addSystemCheck(Table table, String name, Boolean value) {
        boolean isNormal = value == null || !value;
        String status = isNormal ? "✓ Normal" : "⚠ Alterado";
        
        table.addCell(new Cell()
            .add(new Paragraph(name).setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setPadding(4));
        
        table.addCell(new Cell()
            .add(new Paragraph(status).setFont(regularFont).setFontSize(ReportStyles.FONT_SIZE_SMALL))
            .setFontColor(isNormal ? ReportStyles.ACCENT_COLOR : ReportStyles.DANGER_COLOR)
            .setPadding(4));
    }
    
    private String safeText(String text) {
        return (text != null && !text.trim().isEmpty()) ? text : "No registrado";
    }
    
    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}