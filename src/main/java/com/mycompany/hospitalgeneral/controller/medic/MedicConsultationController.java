package com.mycompany.hospitalgeneral.controller.medic;

import com.mycompany.hospitalgeneral.dto.ReportResult;
import com.mycompany.hospitalgeneral.model.Antecedent;
import com.mycompany.hospitalgeneral.model.Cros;
import com.mycompany.hospitalgeneral.model.Diagnostic;
import com.mycompany.hospitalgeneral.model.Disease;
import com.mycompany.hospitalgeneral.model.Exam;
import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalexam;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Prescription;
import com.mycompany.hospitalgeneral.model.Rpe;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.services.ConsultationService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.services.ReportService;
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

    @Inject
    private ReportService reportService;

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
    private Disease selectedDisease;  // 👈 Cambio: de Integer a Disease
    private List<Disease> availableDiseases;

    // === EXÁMENES ===
    private List<Medicalexam> requestedExams;
    private Exam selectedExam;  // 👈 Cambio: de Integer a Exam
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
                loadPrescriptions();
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
        medicalRecord = consultationService.loadMedicalRecord(recordId);
        System.out.println(">>> loadDiagnostics - recordId: " + recordId);
        System.out.println(">>> loadDiagnostics - collection size: "
                + (medicalRecord.getDiagnosticCollection() != null
                ? medicalRecord.getDiagnosticCollection().size() : "NULL"));

        if (medicalRecord.getDiagnosticCollection() != null) {
            diagnostics = medicalRecord.getDiagnosticCollection().stream()
                    .filter(d -> d.getDeleted() == null || !d.getDeleted())
                    .toList();
        } else {
            diagnostics = new ArrayList<>();
        }
        System.out.println(">>> loadDiagnostics - diagnostics final: " + diagnostics.size());
    }

    private void loadExams() {
        // Detach completamente la entidad vieja
        if (medicalRecord != null) {
            consultationService.detachMedicalRecord(medicalRecord);
        }

        // Cargar NUEVA instancia sin cache
        medicalRecord = consultationService.loadMedicalRecord(recordId);

        System.out.println(">>> loadExams - medicalRecord: " + medicalRecord);
        System.out.println(">>> loadExams - getMedicalexamCollection: "
                + (medicalRecord.getMedicalexamCollection() != null
                ? medicalRecord.getMedicalexamCollection().size() : "NULL"));

        if (medicalRecord.getMedicalexamCollection() != null) {
            medicalRecord.getMedicalexamCollection().size();
            requestedExams = new ArrayList<>(
                    medicalRecord.getMedicalexamCollection().stream()
                            .filter(e -> e.getDeleted() == null || !e.getDeleted())
                            .toList()
            );
        } else {
            requestedExams = new ArrayList<>();
        }
        System.out.println(">>> loadExams - requestedExams final: " + requestedExams.size());
        System.out.println(">>> loadExams - requestedExams content: " + requestedExams);
    }

    private void loadPrescriptions() {
        prescriptions = consultationService.findPrescriptionsByRecord(recordId);
        System.out.println(">>> loadPrescriptions - total: " + prescriptions.size());
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
        System.out.println(">>> addDiagnostic llamado - selectedDisease: " + selectedDisease);

        if (selectedDisease == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Seleccione una enfermedad"));
            return;
        }

        try {
            // Pasar el ID de la enfermedad, no la enfermedad completa
            Diagnostic newDiagnostic = consultationService.addDiagnostic(recordId, selectedDisease.getId(), medicId);
            if (newDiagnostic != null) {
                loadDiagnostics();
                System.out.println(">>> diagnostics después de reload: " + diagnostics.size());
                selectedDisease = null;  // 👈 Cambio: Limpiar el objeto Disease, no Integer
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

        if (selectedExam == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Seleccione un examen"));
            return;
        }

        try {

            Medicalexam newExam = consultationService.requestExam(recordId, selectedExam.getId(), medicId);

            if (newExam != null) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                loadExams();

                selectedExam = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Éxito", "Examen solicitado"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "newExam es null"));
            }
        } catch (Exception e) {
            System.out.println(">>> EXCEPTION: " + e.getMessage());
            e.printStackTrace();
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
            // 1. Completar la consulta (cambiar estado a COMPLETADA)
            consultationService.completeConsultation(recordId, medicId);

            // 2. Recargar el registro con todos los datos actualizados
            Medicalrecord completedRecord = consultationService.loadMedicalRecord(recordId);

            // 3. GENERAR REPORTE PDF AUTOMÁTICAMENTE
            ReportResult reportResult = reportService.generateConsultationReport(completedRecord);

            // 4. Guardar el reporte en sesión para poder descargarlo/verlo
            session.setAttribute("currentReport", reportResult);
            session.setAttribute("lastCompletedRecordId", recordId);

            // 5. Limpiar sesión de consulta activa
            session.removeAttribute("currentMedicalRecord");
            session.removeAttribute("currentPatient");

            // 6. Mensaje de éxito con info del reporte
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            "Consulta completada. Reporte generado: " + reportResult.getReportNumber()));

            // 7. Redirigir a la vista del reporte (no al dashboard directamente)
            return "/views/medic/view-report.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo completar la consulta: " + e.getMessage()));
            e.printStackTrace();
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

    public String formatLocalDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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

    public Disease getSelectedDisease() {
        return selectedDisease;
    }

    public void setSelectedDisease(Disease selectedDisease) {
        this.selectedDisease = selectedDisease;
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

    public Exam getSelectedExam() {
        return selectedExam;
    }

    public void setSelectedExam(Exam selectedExam) {
        this.selectedExam = selectedExam;
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

    // GETTERS Y SETTERS - PRESCRIPCIONES (agregados)
    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<Prescription> p) {
        this.prescriptions = p;
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
}
