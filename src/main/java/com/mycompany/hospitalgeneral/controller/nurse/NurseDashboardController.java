package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.services.PatientService;
import com.mycompany.hospitalgeneral.session.UserSession;
import com.mycompany.hospitalgeneral.session.ConsultationContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class NurseDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    // === SERVICES ===
    @Inject
    private PatientService patientService;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private MedicService medicService;

    // === SESSION ===
    @Inject
    private UserSession userSession;

    @Inject
    private ConsultationContext consultationContext;

    private Tuser currentUser;

    // === LISTAS DASHBOARD ===
    private List<Medicalrecord> waitingForVitalsigns;  // Pendientes de signos vitales
    private List<Medicalrecord> waitingForMedic;       // Con signos vitales, esperando médico
    private List<Medicalrecord> attendedToday;         // Atendidos hoy (médico finalizó)

    // === BÚSQUEDA ===
    private String searchQuery;
    private List<Patient> searchResults;
    private boolean showSearchResults = false;

    // === ASIGNACIÓN DE MÉDICO ===
    private Medic selectedMedicForAssignment;
    private List<Medic> availableMedics;
    private Patient pendingPatient;

    // ==================== INIT ====================
    @PostConstruct
    public void init() {
        loadCurrentUser();
        loadAllLists();
    }

    // ==================== SESSION ====================
    private void loadCurrentUser() {
        currentUser = userSession.getUser();
        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No hay usuario en sesión"));
        }
    }

    private Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    // ==================== LISTAS ====================
    public void loadPendingForVitalsigns() {
        waitingForVitalsigns = medicalRecordService.findPendingForVitalsigns();
    }

    public void loadWaitingPatients() {
        waitingForMedic = medicalRecordService.findWaitingForMedic();
    }

    public void loadAttendedToday() {
        // Requiere: medicalRecordService.findAttendedToday()
        // Query sugerida (ver comentario al final del archivo)
        attendedToday = medicalRecordService.findAttendedToday();
    }

    public void loadAllLists() {
        loadPendingForVitalsigns();
        loadWaitingPatients();
        loadAttendedToday();
    }

    // ==================== BÚSQUEDA ====================
    /**
     * Busca pacientes YA REGISTRADOS por DNI, HC o nombre. El registro de
     * nuevos pacientes se gestiona desde otra pantalla.
     */
    public void searchPatient() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Ingrese DNI, HC o nombre"));
            return;
        }

        searchResults = patientService.searchFlexible(searchQuery.trim());
        showSearchResults = true;

        if (searchResults.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "No encontrado",
                            "Paciente no encontrado. Regístrelo desde la pantalla de pacientes."));
        }
    }

    public void clearSearch() {
        searchQuery = null;
        searchResults = null;
        showSearchResults = false;
    }

    // ==================== SELECCIÓN PACIENTE ====================
    /**
     * Crea el registro médico y navega a signos vitales. Se llama desde el
     * modal después de seleccionar médico.
     */
    public String selectPatientAndCreateRecord(Patient patient) {
        if (patient == null) {
            patient = this.pendingPatient;
        }

        if (selectedMedicForAssignment == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Debe seleccionar un médico primero"));
            return null;
        }

        try {
            Medicalrecord record = new Medicalrecord();
            record.setPatientid(patient);
            record.setMedicid(selectedMedicForAssignment);
            record.setReason("");

            medicalRecordService.saveForNurse(record, getCurrentUserId());

            consultationContext.setCurrentMedicalRecord(record);
            consultationContext.setCurrentPatient(patient);
            consultationContext.setCurrentMedic(selectedMedicForAssignment);

            return "/views/nurse/signos-vitales.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", e.getMessage()));
            return null;
        }
    }

    // ==================== SELECCIÓN DE MÉDICO ====================
    /**
     * Carga médicos disponibles y abre el modal de asignación.
     */
    public void prepareMedicAssignment(Patient patient) {
        this.pendingPatient = patient;
        this.selectedMedicForAssignment = null;
        this.availableMedics = medicService.findAllActive();
        PrimeFaces.current().executeScript("PF('medicDialog').show()");
    }

    // ==================== NAVEGACIÓN ====================
    public String startVitalSigns(Medicalrecord record) {
        consultationContext.setCurrentMedicalRecord(record);
        consultationContext.setCurrentPatient(record.getPatientid());
        return "/views/nurse/signos-vitales.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS ====================
    public int calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }

    public Vitalsign lastVitalSign(Medicalrecord record) {
        if (record == null || record.getVitalsignCollection() == null
                || record.getVitalsignCollection().isEmpty()) {
            return null;
        }
        return record.getVitalsignCollection()
                .stream()
                .max(Comparator.comparing(Vitalsign::getCreatedat))
                .orElse(null);
    }

    public void refresh() {
        loadAllLists();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Actualizado", "Listas refrescadas"));
    }

    // ==================== GETTERS / SETTERS ====================
    public List<Medicalrecord> getWaitingForVitalsigns() {
        return waitingForVitalsigns;
    }

    public List<Medicalrecord> getWaitingForMedic() {
        return waitingForMedic;
    }

    public List<Medicalrecord> getAttendedToday() {
        return attendedToday;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String q) {
        this.searchQuery = q;
    }

    public List<Patient> getSearchResults() {
        return searchResults;
    }

    public boolean isShowSearchResults() {
        return showSearchResults;
    }

    public Tuser getCurrentUser() {
        return currentUser;
    }

    public Patient getPendingPatient() {
        return pendingPatient;
    }

    public void setPendingPatient(Patient p) {
        this.pendingPatient = p;
    }

    public Medic getSelectedMedicForAssignment() {
        return selectedMedicForAssignment;
    }

    public void setSelectedMedicForAssignment(Medic m) {
        this.selectedMedicForAssignment = m;
    }

    public List<Medic> getAvailableMedics() {
        return availableMedics;
    }

    public void setAvailableMedics(List<Medic> list) {
        this.availableMedics = list;
    }
}
