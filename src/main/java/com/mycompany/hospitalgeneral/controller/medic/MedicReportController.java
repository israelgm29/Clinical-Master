package com.mycompany.hospitalgeneral.controller.medic;



import com.mycompany.hospitalgeneral.dto.ReportResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * Controller para visualización y descarga de reportes médicos
 */
@Named
@ViewScoped
public class MedicReportController implements Serializable {

    private static final long serialVersionUID = 1L;

    private ReportResult currentReport;

    public void init() {
        // Cargar de sesión si viene de completeConsultation
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(true);

        this.currentReport = (ReportResult) session.getAttribute("currentReport");

        // Limpiar de sesión después de cargar
        if (currentReport != null) {
            session.removeAttribute("currentReport");
        }
    }

    /**
     * Descarga el PDF generado
     */
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

    /**
     * Redirige al dashboard
     */
    public String backToDashboard() {
        return "/views/medic/dashboard.xhtml?faces-redirect=true";
    }

    // Getters
    public ReportResult getCurrentReport() {
        return currentReport;
    }

    public boolean hasReport() {
        return currentReport != null;
    }
}
