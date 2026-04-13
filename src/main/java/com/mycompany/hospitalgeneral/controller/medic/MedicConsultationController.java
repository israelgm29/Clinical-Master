package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.model.*;
import com.mycompany.hospitalgeneral.services.*;
import com.mycompany.hospitalgeneral.session.ConsultationContext;
import com.mycompany.hospitalgeneral.session.ReportContext;
import com.mycompany.hospitalgeneral.session.UserSession;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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

    // === SERVICES ===
    @Inject
    private ConsultationService consultationService;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private ReportService reportService;

    // === SESSION CONTEXT ===
    @Inject
    private UserSession userSession;

    @Inject
    private ConsultationContext consultationContext;

    @Inject
    private ReportContext reportContext;

    // === DATA ===
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
    private Disease selectedDisease;
    private List<Disease> availableDiseases;

    // === EXÁMENES ===
    private List<Medicalexam> requestedExams;
    private Exam selectedExam;
    private List<Exam> availableExams;

    // === PRESCRIPCIONES ===
    private List<Prescription> prescriptions;
    private String newMedication;
    private String newDose;
    private String newFrequency;
    private String newDuration;
    private String newRoute;
    private String newInstructions;

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
        currentMedic = userSession.getMedic();

        if (currentMedic != null) {
            medicId = currentMedic.getId();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No hay médico en sesión"));
        }
    }

    /**
     * Carga todos los datos de la consulta desde la sesión
     */
    public void loadConsultationData() {
        try {
            medicalRecord = consultationContext.getCurrentMedicalRecord();
            patient = consultationContext.getCurrentPatient();

            if (medicalRecord == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "No hay consulta activa"));
                return;
            }

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
            loadPrescriptions();
            loadPatientHistory();
            loadAvailableOptions();

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
        // Query directa a BD - evita caché del EntityManager
        diagnostics = consultationService.findDiagnosticsByRecord(recordId);
    }

    private void loadExams() {
        // Query directa a BD - evita caché del EntityManager
        requestedExams = consultationService.findExamsByRecord(recordId);
        if (requestedExams == null) {
            requestedExams = new ArrayList<>();
        }
    }

    private void loadPrescriptions() {
        prescriptions = consultationService.findPrescriptionsByRecord(recordId);
    }

    // ==================== ACCIONES - PRESCRIPCIONES ====================
    public void addPrescription() {
        if (newMedication == null || newMedication.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "El medicamento es obligatorio"));
            return;
        }
        try {
            Prescription p = consultationService.addPrescription(
                    recordId, newMedication.trim(), newDose, newFrequency,
                    newDuration, newRoute, newInstructions, medicId);
            if (p != null) {
                loadPrescriptions();
                clearPrescriptionForm();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Medicamento agregado"));
            }
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo agregar: " + cause.getMessage()));
        }
    }

    public void removePrescription(Prescription prescription) {
        try {
            consultationService.removePrescription(prescription.getId(), medicId);
            loadPrescriptions();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Medicamento eliminado"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    private void clearPrescriptionForm() {
        newMedication = null;
        newDose = null;
        newFrequency = null;
        newDuration = null;
        newRoute = null;
        newInstructions = null;
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
        if (selectedDisease == null) {
            return;
        }

        consultationService.addDiagnostic(recordId, selectedDisease.getId(), medicId);
        loadDiagnostics();
        selectedDisease = null;
    }

    public void removeDiagnostic(Diagnostic diagnostic) {
        consultationService.removeDiagnostic(diagnostic.getId(), medicId);
        loadDiagnostics();
    }

    public void requestExam() {
        if (selectedExam == null) {
            return;
        }

        consultationService.requestExam(recordId, selectedExam.getId(), medicId);
        loadExams();
        selectedExam = null;
    }

    public void cancelExam(Medicalexam exam) {
        consultationService.cancelExamRequest(exam.getId(), medicId);
        loadExams();
    }

    public String completeConsultation() {
        try {
            consultationService.completeConsultation(recordId, medicId);

            Medicalrecord completed = consultationService.loadMedicalRecord(recordId);
            ReportResult report = reportService.generateConsultationReport(completed);

            consultationContext.clear();

            reportContext.setCurrentReport(report);

            return "/views/medic/view-report.xhtml?faces-redirect=true";

        } catch (Exception e) {
            return null;
        }
    }

    public String cancelConsultation() {
        consultationContext.clear();
        return "/views/medic/dashboard.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS ====================
    public int getPatientAge() {
        if (patient == null || patient.getBirthday() == null) {
            return 0;
        }
        return Period.between(patient.getBirthday(), LocalDate.now()).getYears();
    }

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

    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    // ==================== GETTERS ====================
    public Medicalrecord getMedicalRecord() {
        return medicalRecord;
    }

    public Patient getPatient() {
        return patient;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public List<Medicalexam> getRequestedExams() {
        return requestedExams;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public Disease getSelectedDisease() {
        return selectedDisease;
    }

    public void setSelectedDisease(Disease d) {
        this.selectedDisease = d;
    }

    public Exam getSelectedExam() {
        return selectedExam;
    }

    public void setSelectedExam(Exam e) {
        this.selectedExam = e;
    }

    public List<Disease> getAvailableDiseases() {
        return availableDiseases;
    }

    public List<Exam> getAvailableExams() {
        return availableExams;
    }

    public String getNewMedication() {
        return newMedication;
    }

    public void setNewMedication(String v) {
        this.newMedication = v;
    }

    public String getNewDose() {
        return newDose;
    }

    public void setNewDose(String v) {
        this.newDose = v;
    }

    public String getNewFrequency() {
        return newFrequency;
    }

    public void setNewFrequency(String v) {
        this.newFrequency = v;
    }

    public String getNewDuration() {
        return newDuration;
    }

    public void setNewDuration(String v) {
        this.newDuration = v;
    }

    public String getNewRoute() {
        return newRoute;
    }

    public void setNewRoute(String v) {
        this.newRoute = v;
    }

    public String getNewInstructions() {
        return newInstructions;
    }

    public void setNewInstructions(String v) {
        this.newInstructions = v;
    }

    public List<Vitalsign> getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(List<Vitalsign> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public MedicalRecordService getMedicalRecordService() {
        return medicalRecordService;
    }

    public void setMedicalRecordService(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
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

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Antecedent getAntecedent() {
        return antecedent;
    }

    public void setAntecedent(Antecedent antecedent) {
        this.antecedent = antecedent;
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

    public List<Medicalrecord> getPatientHistory() {
        return patientHistory;
    }

    public void setPatientHistory(List<Medicalrecord> patientHistory) {
        this.patientHistory = patientHistory;
    }

}
