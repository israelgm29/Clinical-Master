package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.services.MedicalRecordService;
import com.mycompany.hospitalgeneral.services.OptionService;
import com.mycompany.hospitalgeneral.services.PatientService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Named
@ViewScoped
public class NurseDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PatientService patientService;

    @Inject
    private MedicalRecordService medicalRecordService;

    @Inject
    private HttpSession session;

    @Inject
    private OptionService optionService;

    private Tuser currentUser;

    // Listas para combos del formulario paciente nuevo
    private List<Option> genders;
    private List<Option> bloodTypes;
    private List<Option> civilStatusList;

    // Listas para la vista
    private List<Medicalrecord> waitingForVitalsigns;  // Sin signos vitales aún
    private List<Medicalrecord> waitingForMedic;       // Con signos vitales, esperando médico

    // Búsqueda
    private String searchQuery;
    private List<Patient> searchResults;
    private boolean showSearchResults = false;

    // Formulario paciente nuevo
    private Patient newPatient;
    private String newPatientReason;  // Motivo de consulta (va a Medicalrecord, no a Patient)
    private boolean showNewPatientForm = false;

    @PostConstruct
    public void init() {
        loadCurrentUser();
       
        // Cargar catálogos para el formulario de paciente nuevo
        this.genders = optionService.findByGroup(3);
        this.bloodTypes = optionService.findByGroup(1);
        this.civilStatusList = optionService.findByGroup(2);
        
        loadAllLists();
        initNewPatient();
    }

    private void loadCurrentUser() {
        currentUser = (Tuser) session.getAttribute("currentUser");
    }

    public void loadWaitingPatients() {
        waitingForMedic = medicalRecordService.findWaitingForMedic();
    }

    public void loadPendingForVitalsigns() {
        waitingForVitalsigns = medicalRecordService.findPendingForVitalsigns();
    }

    private void initNewPatient() {
        newPatient = new Patient();
        newPatientReason = "";
    }

    public void loadAllLists() {
        loadPendingForVitalsigns();
        loadWaitingPatients();
    }

    // ==================== BÚSQUEDA ====================
    public void searchPatient() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Ingrese DNI, HC o nombre"));
            return;
        }

        searchResults = patientService.searchFlexible(searchQuery.trim());
        showSearchResults = true;
        showNewPatientForm = false;

        if (searchResults.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "No encontrado",
                            "No existe paciente. Puede registrar uno nuevo."));
            showNewPatientForm = true;
        }
    }

    /**
     * Selecciona paciente existente y crea MedicalRecord para esta visita
     */
    public String selectPatientAndCreateRecord(Patient patient) {
        try {
            // Crear MedicalRecord para esta visita
            Medicalrecord record = new Medicalrecord();
            record.setPatientid(patient);
            record.setReason(""); // Se puede pedir después o dejar vacío
            // medicid = null (se asignará cuando el médico atienda)

            medicalRecordService.saveForNurse(record, getCurrentUserId());

            // Guardar en sesión y redirigir
            session.setAttribute("currentNurseRecord", record);
            session.setAttribute("currentNursePatient", patient);

            return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    // ==================== PACIENTE NUEVO ====================
    public void showNewPatientForm() {
        showNewPatientForm = true;
        showSearchResults = false;
        initNewPatient();
    }

    public String registerNewPatientAndCreateRecord() {
        try {
            // Validaciones
            if (newPatient.getFirstname() == null || newPatient.getFirstname().trim().isEmpty()
                    || newPatient.getLastname() == null || newPatient.getLastname().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nombre y apellido son obligatorios"));
                return null;
            }

            // Generar HC automático
            newPatient.setHc(patientService.generateNextHc());
            newPatient.setCreatedat(LocalDateTime.now());
            newPatient.setCreatedby(getCurrentUserId());

            patientService.save(newPatient);

            // Crear MedicalRecord automáticamente
            Medicalrecord record = new Medicalrecord();
            record.setPatientid(newPatient);
            record.setReason(newPatientReason != null ? newPatientReason : "");
            // medicid = null por ahora

            medicalRecordService.saveForNurse(record, getCurrentUserId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Paciente registrado: " + newPatient.getHc()));

            // Redirigir a toma de signos vitales
            session.setAttribute("currentNurseRecord", record);
            session.setAttribute("currentNursePatient", newPatient);

            return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
            return null;
        }
    }

    // ==================== NAVEGACIÓN ====================
    public String startVitalSigns(Medicalrecord record) {
        session.setAttribute("currentNurseRecord", record);
        session.setAttribute("currentNursePatient", record.getPatientid());
        return "/views/nurse/SignosVitales.xhtml?faces-redirect=true";
    }

    public String viewOnlyVitalSigns(Medicalrecord record) {
        session.setAttribute("currentNurseRecord", record);
        session.setAttribute("currentNursePatient", record.getPatientid());
        session.setAttribute("vitalsignReadOnly", true);
        return "/views/nurse/vitalsigns.xhtml?faces-redirect=true";
    }

    // ==================== HELPERS ====================
    private Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : 1;
    }

    public int calculateAge(java.time.LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }

    public void refresh() {
        loadAllLists();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Actualizado", "Listas refrescadas"));
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
}
