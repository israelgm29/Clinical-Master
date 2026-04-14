package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.services.ReportService;
import com.mycompany.hospitalgeneral.session.ConsultationContext;
import com.mycompany.hospitalgeneral.session.ReportContext;
import com.mycompany.hospitalgeneral.session.UserSession;
import java.util.Base64;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import java.io.ByteArrayInputStream;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Named
@ViewScoped
public class MedicDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    // === SERVICIOS INYECTADOS ===
    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private MedicService medicService;

    @Inject
    private UserSession session;

    @Inject
    private ConsultationContext consultationContext;

    @Inject
    private ReportService reportService;

    @Inject
    private ReportContext reportContext;

    // === DATOS DEL DASHBOARD ===
    private List<Medicalrecord> pendingConsultations;
    private List<Medicalrecord> todayConsultations;
    private List<Medicalrecord> waitingPatients;

    // === CONTADORES ===
    private Long pendingCount;
    private Long todayCount;
    private Long waitingCount;

    // === MÉDICO EN SESIÓN ===
    private Medic currentMedic;
    private Integer medicId;

    // === CONSULTA SELECCIONADA ===
    private Medicalrecord selectedRecord;

    // === REPORTE ===
    private String currentPdfDataUrl;
    private String reportFileName;
    private boolean reportLoading = false;

    @PostConstruct
    public void init() {
        loadCurrentMedic();
        if (medicId != null) {
            loadDashboardData();
        }
    }

    /**
     * Carga el médico desde la sesión
     */
    private void loadCurrentMedic() {
        try {
            currentMedic = session.getMedic();

            if (currentMedic != null) {
                medicId = currentMedic.getId();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "No hay médico en sesión"));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al cargar médico: " + e.getMessage()));
        }
    }

    /**
     * Carga todos los datos del dashboard usando los servicios
     */
    public void loadDashboardData() {
        loadPendingConsultations();
        loadTodayConsultations();
        loadWaitingPatients();
        loadCounters();
    }

    /**
     * 1. CONSULTAS PENDIENTES Medicalrecord donde done = false y canceled =
     * false
     */
    public void loadPendingConsultations() {
        try {
            pendingConsultations = medicalRecordService.findPendingByMedic(medicId);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al cargar consultas pendientes: " + e.getMessage()));
            pendingConsultations = List.of();
        }
    }

    /**
     * 2. CONSULTAS DEL DÍA Filtradas por fecha de creación = hoy
     */
    public void loadTodayConsultations() {
        try {
            todayConsultations = medicalRecordService.findTodayByMedic(medicId);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al cargar consultas del día: " + e.getMessage()));
            todayConsultations = List.of();
        }
    }

    /**
     * 3. PACIENTES EN ESPERA Con signos vitales tomados por enfermería
     * (Vitalsign existe) pero consulta no realizada (done = false)
     */
    public void loadWaitingPatients() {
        try {
            waitingPatients = medicalRecordService.findWaitingPatients(medicId);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al cargar pacientes en espera: " + e.getMessage()));
            waitingPatients = List.of();
        }
    }

    /**
     * Carga los contadores para las tarjetas del dashboard
     */
    public void loadCounters() {
        try {
            pendingCount = medicalRecordService.countPendingByMedic(medicId);
            todayCount = medicalRecordService.countTodayByMedic(medicId);
            waitingCount = medicalRecordService.countWaitingPatients(medicId);
        } catch (Exception e) {
            pendingCount = 0L;
            todayCount = 0L;
            waitingCount = 0L;
        }
    }

    // ==================== ACCIONES DE NAVEGACIÓN ====================
    /**
     * Navegar a la consulta del paciente
     *
     * @param record
     * @return
     */
    public String startConsultation(Medicalrecord record) {
        if (record == null) {
            return null;
        }

        consultationContext.setCurrentMedicalRecord(record);
        consultationContext.setCurrentPatient(record.getPatientid());

        return "/views/medic/consulta.xhtml?faces-redirect=true";
    }

    /**
     * Ver historia clínica del paciente
     */
    public String viewHistory(Medicalrecord record) {
        if (record == null) {
            return null;
        }

        consultationContext.setCurrentPatient(record.getPatientid());

        return "/views/medic/historia-clinica.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS DE PRESENTACIÓN ====================
    /**
     * Obtiene los signos vitales de un registro médico
     */
    public List<Vitalsign> getVitalSigns(Medicalrecord record) {
        if (record != null && record.getVitalsignCollection() != null) {
            return record.getVitalsignCollection().stream()
                    .filter(v -> v.getDeleted() == null || !v.getDeleted())
                    .toList();
        }
        return List.of();
    }

    /**
     * Obtener el último signo vital registrado
     */
    public Vitalsign getLastVitalSign(Medicalrecord record) {
        List<Vitalsign> vitals = getVitalSigns(record);
        if (!vitals.isEmpty()) {
            return vitals.get(vitals.size() - 1);
        }
        return null;
    }

    /**
     * Formatear fecha para mostrar
     */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Formatear solo hora
     */
    public String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Obtener nombre completo del paciente
     */
    public String getPatientFullName(Patient patient) {
        if (patient == null) {
            return "N/A";
        }
        return patient.getLastname() + " " + patient.getFirstname();
    }

    /**
     * Obtener iniciales del paciente para avatar
     */
    public String getPatientInitials(Patient patient) {
        if (patient == null) {
            return "??";
        }
        String firstInitial = patient.getFirstname() != null && !patient.getFirstname().isEmpty()
                ? patient.getFirstname().substring(0, 1).toUpperCase() : "";
        String lastInitial = patient.getLastname() != null && !patient.getLastname().isEmpty()
                ? patient.getLastname().substring(0, 1).toUpperCase() : "";
        return firstInitial + lastInitial;
    }

    // ==================== REPORTE PDF ====================
    public void prepareReport(Medicalrecord record) {
        try {
            reportLoading = true;
            currentPdfDataUrl = null;
            reportFileName = null;

            ReportResult result = reportService.generateConsultationReport(record);
            reportContext.setCurrentReport(result);

            // Convertir a Base64 para el iframe
            String base64 = Base64.getEncoder().encodeToString(result.getContent());
            currentPdfDataUrl = "data:application/pdf;base64," + base64;
            reportFileName = result.getFileName();

        } catch (Exception e) {
            currentPdfDataUrl = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo generar el reporte: " + e.getMessage()));
        } finally {
            reportLoading = false;
        }
    }

    public StreamedContent downloadReport() {
        ReportResult report = reportContext.getCurrentReport();
        if (report == null) {
            return null;
        }

        return DefaultStreamedContent.builder()
                .name(report.getFileName())
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(report.getContent()))
                .build();
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Medicalrecord> getPendingConsultations() {
        return pendingConsultations;
    }

    public void setPendingConsultations(List<Medicalrecord> pendingConsultations) {
        this.pendingConsultations = pendingConsultations;
    }

    public List<Medicalrecord> getTodayConsultations() {
        return todayConsultations;
    }

    public void setTodayConsultations(List<Medicalrecord> todayConsultations) {
        this.todayConsultations = todayConsultations;
    }

    public List<Medicalrecord> getWaitingPatients() {
        return waitingPatients;
    }

    public void setWaitingPatients(List<Medicalrecord> waitingPatients) {
        this.waitingPatients = waitingPatients;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(Long todayCount) {
        this.todayCount = todayCount;
    }

    public Long getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(Long waitingCount) {
        this.waitingCount = waitingCount;
    }

    public Medic getCurrentMedic() {
        return currentMedic;
    }

    public void setCurrentMedic(Medic currentMedic) {
        this.currentMedic = currentMedic;
    }

    public Integer getMedicId() {
        return medicId;
    }

    public void setMedicId(Integer medicId) {
        this.medicId = medicId;
    }

    public Medicalrecord getSelectedRecord() {
        return selectedRecord;
    }

    public void setSelectedRecord(Medicalrecord selectedRecord) {
        this.selectedRecord = selectedRecord;
    }

    public String getCurrentPdfDataUrl() {
        return currentPdfDataUrl;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public boolean isReportLoading() {
        return reportLoading;
    }
}
