package com.mycompany.hospitalgeneral.session;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ReportContext implements Serializable {

    private ReportResult currentReport;

    public boolean hasReport() {
        return currentReport != null;
    }

    public void clear() {
        currentReport = null;
    }

    // Getter & Setter
    public ReportResult getCurrentReport() {
        return currentReport;
    }

    public void setCurrentReport(ReportResult currentReport) {
        this.currentReport = currentReport;
    }
}
