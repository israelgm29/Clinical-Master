package com.mycompany.hospitalgeneral.services.impl;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.services.ReportService;
import com.mycompany.hospitalgeneral.util.reports.ConsultationPdfGenerator;
import com.mycompany.hospitalgeneral.util.reports.PdfGeneratorUtil;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ReportServiceImpl implements ReportService {

    @Inject
    private ConsultationPdfGenerator consultationGenerator;
    
    @Override
    public ReportResult generateConsultationReport(Medicalrecord record) {
        String reportNumber = PdfGeneratorUtil.generateReportNumber();
        String fileName = "CONSULTA_" + reportNumber + ".pdf";
        
        byte[] pdfBytes = consultationGenerator.generate(record, reportNumber);
        
        return new ReportResult(pdfBytes, fileName, reportNumber);
    }

    @Override
    public ReportResult generatePrescriptionReport(Medicalrecord record) {
        // TODO: Implementar generador de receta
        throw new UnsupportedOperationException("Próximamente");
    }

    @Override
    public ReportResult generateExamOrdersReport(Medicalrecord record) {
        // TODO: Implementar generador de órdenes
        throw new UnsupportedOperationException("Próximamente");
    }
}