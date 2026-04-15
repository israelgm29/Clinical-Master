package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.session.ReportContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Base64;

@Named
@ViewScoped
public class MedicReportController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ReportContext reportContext;

    private ReportResult currentReport;

    @PostConstruct
    public void init() {
        this.currentReport = reportContext.getCurrentReport();
    }

    public StreamedContent downloadPdf() {
        if (currentReport == null || currentReport.getContent() == null) {
            return null;
        }

        return DefaultStreamedContent.builder()
                .name(currentReport.getFileName())
                .contentType(currentReport.getContentType())
                .stream(() -> new ByteArrayInputStream(currentReport.getContent()))
                .build();
    }

    public String backToDashboard() {
        reportContext.clear(); // 🔥 limpieza correcta
        return "/views/medic/dashboard.xhtml?faces-redirect=true";
    }

    public String getPdfDataUrl() {
        if (currentReport == null || currentReport.getContent() == null) {
            return "";
        }
        // Convierte los bytes del PDF a un formato que el navegador entienda directamente
        String base64 = Base64.getEncoder().encodeToString(currentReport.getContent());
        return "data:application/pdf;base64," + base64;
    }

    public ReportResult getCurrentReport() {
        return currentReport;
    }

    public boolean isHasReport() {
        return currentReport != null;
    }

}
