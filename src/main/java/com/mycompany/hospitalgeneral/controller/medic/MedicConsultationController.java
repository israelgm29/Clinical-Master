package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.model.Antecedent;
import com.mycompany.hospitalgeneral.model.Cros;
import com.mycompany.hospitalgeneral.model.Diagnostic;
import com.mycompany.hospitalgeneral.model.Disease;
import com.mycompany.hospitalgeneral.model.Exam;
import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalexam;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Rpe;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.services.ConsultationService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jhonatan
 */
@Named
@ViewScoped
public class MedicConsultationController implements Serializable {

    private static final long serialVersionUID = 1L;

    // === SERVICIOS ===
    @Inject
    private ConsultationService consultationService;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private HttpSession session;

    // === DATOS DE LA CONSULTA ===
    private Medicalrecord medicalRecord;
    private Patient patient;
    private Medic currentMedic;
    private Integer medicId;
    private Integer recordId;

    // === ANTECEDENTES ===
    private Antecedent antecedent;

    // === SIGNOS VITALES ===
    private List<Vitalsign> vitalSigns;
    private Vitalsign lastVitalSign;

    // === FORMULARIOS ===
    private String reason;
    private String currentIllness;

    // === CROS (Revisión por Sistemas) ===
    private Cros cros;
    private boolean crosExists = false;

    // === RPE (Revisión Física por Regiones) ===
    private Rpe rpe;
    private boolean rpeExists = false;

    // === DIAGNÓSTICOS ===
    private List<Diagnostic> diagnostics;
    private Integer selectedDiseaseId;
    private List<Disease> availableDiseases;

    // === EXÁMENES ===
    private List<Medicalexam> requestedExams;
    private Integer selectedExamId;
    private List<Exam> availableExams;

    // === HISTORIAL ===
    private List<Medicalrecord> patientHistory;

    @PostConstruct
    public void init() {
        loadCurrentMedic();
        loadConsultationData();
    }

    /**
     * Carga el médico desde la sesión
     */
    private void loadCurrentMedic() {
        currentMedic = (Medic) session.getAttribute("currentMedic");
        if (currentMedic != null) {
            medicId = currentMedic.getId();
        } else {
            // Para pruebas
            medicId = 1;
        }
    }

    /**
     * Carga todos los datos de la consulta desde la sesión
     */
    public void loadConsultationData() {
        try {
            // Obtener MedicalRecord de la sesión (viene del dashboard)
            medicalRecord = (Medicalrecord) session.getAttribute("currentMedicalRecord");
            System.out.println(medicalRecord);
            patient = (Patient) session.getAttribute("currentPatient");
            System.out.println(patient);

            if (medicalRecord != null) {
                recordId = medicalRecord.getId();

                // Recargar desde BD para tener datos frescos
                medicalRecord = consultationService.loadMedicalRecord(recordId);
                patient = medicalRecord.getPatientid();

                // Cargar datos
                loadAntecedents();
                loadVitalSigns();
                loadFormData();
                loadCros();
                loadRpe();
                loadDiagnostics();
                loadExams();
                loadPatientHistory();
                loadAvailableOptions();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "No se encontró la consulta en sesión"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Error al cargar datos: " + e.getMessage()));
        }
    }

    // ==================== CARGA DE DATOS ====================
    private void loadAntecedents() {
        if (patient != null) {
            antecedent = consultationService.findAntecedentsByPatient(patient.getId());
        }
    }

    private void loadVitalSigns() {
        vitalSigns = consultationService.findVitalSignsByRecord(recordId);
        if (!vitalSigns.isEmpty()) {
            lastVitalSign = vitalSigns.get(vitalSigns.size() - 1);
        }
    }

    private void loadFormData() {
        reason = medicalRecord.getReason();
        currentIllness = medicalRecord.getCurrentillness();
    }

    private void loadCros() {
        if (medicalRecord.getCrosCollection() != null && !medicalRecord.getCrosCollection().isEmpty()) {
            cros = medicalRecord.getCrosCollection().iterator().next();
            crosExists = true;
        } else {
            cros = new Cros();
            crosExists = false;
        }
    }

    private void loadRpe() {
        if (medicalRecord.getRpeCollection() != null && !medicalRecord.getRpeCollection().isEmpty()) {
            rpe = medicalRecord.getRpeCollection().iterator().next();
            rpeExists = true;
        } else {
            rpe = new Rpe();
            rpeExists = false;
        }
    }

    private void loadDiagnostics() {
        if (medicalRecord.getDiagnosticCollection() != null) {
            diagnostics = medicalRecord.getDiagnosticCollection().stream()
                    .filter(d -> d.getDeleted() == null || !d.getDeleted())
                    .toList();
        } else {
            diagnostics = new ArrayList<>();
        }
    }

    private void loadExams() {
        if (medicalRecord.getMedicalexamCollection() != null) {
            requestedExams = medicalRecord.getMedicalexamCollection().stream()
                    .filter(e -> e.getDeleted() == null || !e.getDeleted())
                    .toList();
        } else {
            requestedExams = new ArrayList<>();
        }
    }

    private void loadPatientHistory() {
        if (patient != null) {
            patientHistory = consultationService.findPatientHistory(patient.getId(), recordId);
        }
    }

    private void loadAvailableOptions() {
        availableDiseases = consultationService.findAllDiseases();
        availableExams = consultationService.findAllExams();
    }

    // ==================== ACCIONES - GUARDAR ====================
    /**
     * Guarda motivo y enfermedad actual
     */
    public void saveMedicalRecordInfo() {
        try {
            consultationService.updateMedicalRecordInfo(recordId, reason, currentIllness, medicId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Información guardada correctamente"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    /**
     * Guarda la Revisión por Sistemas (Cros)
     */
    public void saveCros() {
        try {
            cros = consultationService.saveCros(cros, recordId, medicId);
            crosExists = true;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Revisión por sistemas guardada"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    /**
     * Guarda la Revisión Física por Regiones (Rpe)
     */
    public void saveRpe() {
        try {
            rpe = consultationService.saveRpe(rpe, recordId, medicId);
            rpeExists = true;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Revisión física guardada"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    // ==================== ACCIONES - DIAGNÓSTICOS ====================
    /**
     * Agrega un diagnóstico
     */
    public void addDiagnostic() {
        if (selectedDiseaseId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Seleccione una enfermedad"));
            return;
        }

        try {
            Diagnostic newDiagnostic = consultationService.addDiagnostic(recordId, selectedDiseaseId, medicId);
            if (newDiagnostic != null) {
                loadDiagnostics();
                selectedDiseaseId = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Diagnóstico agregado"));
            }
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            System.out.println(">>> [ERROR addDiagnostic] " + cause.getClass().getName() + ": " + cause.getMessage());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo agregar: " + cause.getMessage()));

        }
    }

    /**
     * Elimina un diagnóstico
     */
    public void removeDiagnostic(Diagnostic diagnostic) {
        try {
            consultationService.removeDiagnostic(diagnostic.getId(), medicId);
            loadDiagnostics();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Diagnóstico eliminado"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    // ==================== ACCIONES - EXÁMENES ====================
    /**
     * Solicita un examen
     */
    public void requestExam() {
        if (selectedExamId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Seleccione un examen"));
            return;
        }

        try {
            Medicalexam newExam = consultationService.requestExam(recordId, selectedExamId, medicId);
            if (newExam != null) {
                loadExams();
                selectedExamId = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Examen solicitado"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo solicitar: " + e.getMessage()));
        }
    }

    /**
     * Cancela solicitud de examen
     */
    public void cancelExam(Medicalexam exam) {
        try {
            consultationService.cancelExamRequest(exam.getId(), medicId);
            loadExams();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Solicitud cancelada"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo cancelar: " + e.getMessage()));
        }
    }

    // ==================== ACCIONES - COMPLETAR ====================
    /**
     * Completa la consulta
     */
    public String completeConsultation() {
        try {
            consultationService.completeConsultation(recordId, medicId);

            // Limpiar sesión
            session.removeAttribute("currentMedicalRecord");
            session.removeAttribute("currentPatient");

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Consulta completada correctamente"));

            return "/views/medic/dashboard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo completar: " + e.getMessage()));
            return null;
        }
    }

    /**
     * Cancela y vuelve al dashboard
     */
    public String cancelConsultation() {
        session.removeAttribute("currentMedicalRecord");
        session.removeAttribute("currentPatient");
        return "/views/medic/dashboard.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS ====================
    public String getPatientFullName() {
        if (patient == null) {
            return "N/A";
        }
        return patient.getLastname() + " " + patient.getFirstname();
    }

    public String getPatientInitials() {
        if (patient == null) {
            return "??";
        }
        String first = patient.getFirstname() != null ? patient.getFirstname().substring(0, 1).toUpperCase() : "";
        String last = patient.getLastname() != null ? patient.getLastname().substring(0, 1).toUpperCase() : "";
        return first + last;
    }

    public int getPatientAge() {
        if (patient == null || patient.getBirthday() == null) {
            return 0;
        }
        return Period.between(patient.getBirthday(), LocalDate.now()).getYears();
    }

    public String formatDate(java.util.Date date) {
        if (date == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate().format(formatter);
        }
        return new java.sql.Date(date.getTime()).toLocalDate().format(formatter);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    public String getSexText(Integer sex) {
        if (sex == null) {
            return "N/A";
        }
        return sex == 1 ? "Masculino" : "Femenino";
    }

    public String getBloodTypeText(Integer bloodType) {
        if (bloodType == null) {
            return "N/A";
        }
        String[] types = {"", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        return bloodType >= 1 && bloodType <= 8 ? types[bloodType] : "N/A";
    }

    // ==================== GETTERS Y SETTERS ====================
    public Medicalrecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(Medicalrecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Antecedent getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(Antecedent antecedent) {
        this.antecedent = antecedent;
    }

    public List<Vitalsign> getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(List<Vitalsign> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public Vitalsign getLastVitalSign() {
        return lastVitalSign;
    }

    public void setLastVitalSign(Vitalsign lastVitalSign) {
        this.lastVitalSign = lastVitalSign;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCurrentIllness() {
        return currentIllness;
    }

    public void setCurrentIllness(String currentIllness) {
        this.currentIllness = currentIllness;
    }

    public Cros getCros() {
        return cros;
    }

    public void setCros(Cros cros) {
        this.cros = cros;
    }

    public boolean isCrosExists() {
        return crosExists;
    }

    public void setCrosExists(boolean crosExists) {
        this.crosExists = crosExists;
    }

    public Rpe getRpe() {
        return rpe;
    }

    public void setRpe(Rpe rpe) {
        this.rpe = rpe;
    }

    public boolean isRpeExists() {
        return rpeExists;
    }

    public void setRpeExists(boolean rpeExists) {
        this.rpeExists = rpeExists;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Integer getSelectedDiseaseId() {
        return selectedDiseaseId;
    }

    public void setSelectedDiseaseId(Integer selectedDiseaseId) {
        this.selectedDiseaseId = selectedDiseaseId;
    }

    public List<Disease> getAvailableDiseases() {
        return availableDiseases;
    }

    public void setAvailableDiseases(List<Disease> availableDiseases) {
        this.availableDiseases = availableDiseases;
    }

    public List<Medicalexam> getRequestedExams() {
        return requestedExams;
    }

    public void setRequestedExams(List<Medicalexam> requestedExams) {
        this.requestedExams = requestedExams;
    }

    public Integer getSelectedExamId() {
        return selectedExamId;
    }

    public void setSelectedExamId(Integer selectedExamId) {
        this.selectedExamId = selectedExamId;
    }

    public List<Exam> getAvailableExams() {
        return availableExams;
    }

    public void setAvailableExams(List<Exam> availableExams) {
        this.availableExams = availableExams;
    }

    public List<Medicalrecord> getPatientHistory() {
        return patientHistory;
    }

    public void setPatientHistory(List<Medicalrecord> patientHistory) {
        this.patientHistory = patientHistory;
    }
}
