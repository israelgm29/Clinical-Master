package com.mycompany.hospitalgeneral.dto;

import java.time.LocalDateTime;

/**
 * Resultado de generación de reporte
 */
public class ReportResult {
    private final byte[] content;
    private final String fileName;
    private final String reportNumber;
    private final LocalDateTime generatedAt;
    private final String contentType;
    private final long size;
    
    public ReportResult(byte[] content, String fileName, String reportNumber) {
        this.content = content;
        this.fileName = fileName;
        this.reportNumber = reportNumber;
        this.generatedAt = LocalDateTime.now();
        this.contentType = "application/pdf";
        this.size = content != null ? content.length : 0;
    }
    
    // Getters
    public byte[] getContent() { return content; }
    public String getFileName() { return fileName; }
    public String getReportNumber() { return reportNumber; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
}