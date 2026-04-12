package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.model.Medicalrecord;

/**
 * Servicio para generación de reportes médicos
 */
public interface ReportService {
    
    /**
     * Genera reporte completo de consulta médica en PDF
     * @param record Registro médico completado
     * @return Datos del reporte generado (bytes, nombre, número)
     */
    ReportResult generateConsultationReport(Medicalrecord record);
    
    /**
     * Genera receta médica separada (para farmacia)
     */
    ReportResult generatePrescriptionReport(Medicalrecord record);
    
    /**
     * Genera órdenes de examen (para laboratorio)
     */
    ReportResult generateExamOrdersReport(Medicalrecord record);
}