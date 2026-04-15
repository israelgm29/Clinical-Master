package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.services.OptionService;
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
import java.time.LocalDateTime;
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
    private OptionService optionService;

    @Inject
    private MedicService medicService;

    // === SESSION ===
    @Inject
    private UserSession userSession;

    @Inject
    private ConsultationContext consultationContext;

    private Tuser currentUser;

    // === CATÁLOGOS ===
    private List<Option> genders;
    private List<Option> bloodTypes;
    private List<Option> civilStatusList;

    // === LISTAS DASHBOARD ===
    private List<Medicalrecord> waitingForVitalsigns;
    private List<Medicalrecord> waitingForMedic;

    // === BÚSQUEDA ===
    private String searchQuery;
    private List<Patient> searchResults;
    private boolean showSearchResults = false;

    // === NUEVO PACIENTE ===
    private Patient newPatient;
    private String newPatientReason;
    private boolean showNewPatientForm = false;

    private Medic selectedMedicForAssignment;
    private List<Medic> availableMedics;
    private Patient pendingPatient;

    @PostConstruct

    public void init() {
        loadCurrentUser();

        // Catálogos
        this.genders = optionService.findByGroup(3);
        this.bloodTypes = optionService.findByGroup(1);
        this.civilStatusList = optionService.findByGroup(2);

        loadAllLists();
        initNewPatient();
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
    public void loadWaitingPatients() {
        waitingForMedic = medicalRecordService.findWaitingForMedic();
    }

    public void loadPendingForVitalsigns() {
        waitingForVitalsigns = medicalRecordService.findPendingForVitalsigns();
    }

    public void loadAllLists() {
        loadPendingForVitalsigns();
        loadWaitingPatients();
    }

    // ==================== BÚSQUEDA ====================
    public void searchPatient() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Atención", "Ingrese DNI, HC o nombre"));
            return;
        }

        searchResults = patientService.searchFlexible(searchQuery.trim());
        showSearchResults = true;
        showNewPatientForm = false;

        if (searchResults.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "No encontrado",
                            "No existe paciente. Puede registrar uno nuevo."));
            showNewPatientForm = true;
        }
    }

    // ==================== SELECCIÓN PACIENTE ====================
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

            // ✅ Guardar en contexto (NO HttpSession)
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

    // ==================== NUEVO PACIENTE ====================
    private void initNewPatient() {
        newPatient = new Patient();
        newPatientReason = "";
    }

    public void showNewPatientForm() {
        showNewPatientForm = true;
        showSearchResults = false;
        initNewPatient();
    }

    public String registerNewPatientAndCreateRecord() {
        try {
            if (newPatient.getFirstname() == null || newPatient.getFirstname().trim().isEmpty()
                    || newPatient.getLastname() == null || newPatient.getLastname().trim().isEmpty()) {

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Nombre y apellido son obligatorios"));
                return null;
            }

            newPatient.setHc(patientService.generateNextHc());
            newPatient.setCreatedat(LocalDateTime.now());
            newPatient.setCreatedby(getCurrentUserId());

            patientService.save(newPatient);

            Medicalrecord record = new Medicalrecord();
            record.setPatientid(newPatient);
            record.setReason(newPatientReason != null ? newPatientReason : "");

            medicalRecordService.saveForNurse(record, getCurrentUserId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito", "Paciente registrado: " + newPatient.getHc()));

            // ✅ Contexto compartido
            consultationContext.setCurrentMedicalRecord(record);
            consultationContext.setCurrentPatient(newPatient);

            return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", e.getMessage()));
            return null;
        }
    }

    // ==================== SELECCION DE MEDICO ====================
    /**
     * Carga médicos disponibles y muestra modal
     * @param patient
     */
    public void prepareMedicAssignment(Patient patient) {
        System.out.println(patient.getFirstname());
        this.pendingPatient = patient;
        this.selectedMedicForAssignment = null;
        this.availableMedics = medicService.findAllActive();
        System.out.println(availableMedics.size());

        PrimeFaces.current().executeScript("PF('medicDialog').show()");
    }

    // ==================== NAVEGACIÓN ====================
    public String startVitalSigns(Medicalrecord record) {
        consultationContext.setCurrentMedicalRecord(record);
        consultationContext.setCurrentPatient(record.getPatientid());
        return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";
    }

    public String viewOnlyVitalSigns(Medicalrecord record) {
        consultationContext.setCurrentMedicalRecord(record);
        consultationContext.setCurrentPatient(record.getPatientid());
        return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS ====================
    public int calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
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

    public boolean isShowNewPatientForm() {
        return showNewPatientForm;
    }

    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient p) {
        this.newPatient = p;
    }

    public String getNewPatientReason() {
        return newPatientReason;
    }

    public void setNewPatientReason(String r) {
        this.newPatientReason = r;
    }

    public Tuser getCurrentUser() {
        return currentUser;
    }

    public void setShowNewPatientForm(boolean show) {
        this.showNewPatientForm = show;
        if (!show) {
            initNewPatient();
        }
    }

    public List<Option> getGenders() {
        return genders;
    }

    public List<Option> getBloodTypes() {
        return bloodTypes;
    }

    public List<Option> getCivilStatusList() {
        return civilStatusList;
    }

    public Patient getPendingPatient() {
        return pendingPatient;
    }

    public void setPendingPatient(Patient pendingPatient) {
        this.pendingPatient = pendingPatient;
    }

    public Medic getSelectedMedicForAssignment() {
        return selectedMedicForAssignment;
    }

    public void setSelectedMedicForAssignment(Medic selectedMedicForAssignment) {
        this.selectedMedicForAssignment = selectedMedicForAssignment;
    }

    public List<Medic> getAvailableMedics() {
        return availableMedics;
    }

    public void setAvailableMedics(List<Medic> availableMedics) {
        this.availableMedics = availableMedics;
    }
    
}
