package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.OptionService;
import com.mycompany.hospitalgeneral.services.PatientService;
import com.mycompany.hospitalgeneral.services.TuserService;
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
import java.util.List;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class PatientController implements Serializable {

    @Inject
    private PatientService patientService;
    @Inject
    private OptionService optionService;
    @Inject
    private UserSession userSession;
    @Inject
    private TuserService tuserService;

    private List<Patient> patients;
    private Patient selectedPatient;
    private Patient newPatient;

    // Listas para los combos del formulario
    private List<Option> bloodTypes;
    private List<Option> civilStatusList;
    private List<Option> genders;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        loadPatients();
        bloodTypes = optionService.findByGroup(1);
        civilStatusList = optionService.findByGroup(2);
        genders = optionService.findByGroup(3);
        prepareNewPatient();
    }

    public void loadPatients() {
        patients = patientService.findAll();
    }

    public void prepareNewPatient() {
        this.newPatient = new Patient();
        this.editMode = false;  // <-- MODO CREACIÓN
        String nextHc = patientService.generateNextHc();
        this.newPatient.setHc(nextHc);
    }

    public void savePatient() {
        try {
            String dni = (newPatient.getDni() != null) ? newPatient.getDni().trim() : "";
            String passport = (newPatient.getPassport() != null) ? newPatient.getPassport().trim() : "";
            newPatient.setDni(dni.isEmpty() ? null : dni);
            newPatient.setPassport(passport.isEmpty() ? null : passport);

            if (dni.isEmpty() && passport.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                "Debe ingresar al menos un documento (Cédula o Pasaporte)"));
                return;
            }

            if (newPatient.getId() == null) {
                newPatient.setCreatedby(getCurrentUserId());
            }

            patientService.save(newPatient);
            loadPatients();
            prepareNewPatient();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Paciente guardado correctamente"));

            // ✅ Solo resetear el formulario del diálogo
            PrimeFaces.current().resetInputs(":dialogs:manage-patient-content");

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate entry") || msg.contains("constraint") || msg.contains("unique"))) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Duplicidad",
                                "La Historia Clínica o Identificación ya existen en el sistema."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Interno",
                                "No se pudo guardar: " + e.getLocalizedMessage()));
            }
        }
    }

// ==================== MÉTODOS PARA AUDITORÍA ====================
    /**
     * Obtiene el nombre completo del usuario que creó el paciente
     */
    public String getCreatedByName() {
        if (selectedPatient == null || selectedPatient.getCreatedby() == null) {
            return "Sistema";
        }
        Tuser user = tuserService.findById(selectedPatient.getCreatedby());
        return user != null ? user.getFullName() : "Usuario #" + selectedPatient.getCreatedby();
    }

    /**
     * Obtiene el nombre completo del usuario que editó por última vez
     */
    public String getEditedByName() {
        if (selectedPatient == null || selectedPatient.getEditedby() == null) {
            return null;
        }
        Tuser user = tuserService.findById(selectedPatient.getEditedby());
        return user != null ? user.getFullName() : "Usuario #" + selectedPatient.getEditedby();
    }

    /**
     * Obtiene el nombre completo del usuario que eliminó
     */
    public String getDeletedByName() {
        if (selectedPatient == null || selectedPatient.getDeletedby() == null) {
            return null;
        }
        Tuser user = tuserService.findById(selectedPatient.getDeletedby());
        return user != null ? user.getFullName() : "Usuario #" + selectedPatient.getDeletedby();
    }

    /**
     * Obtiene el rol del usuario que creó
     */
    public String getCreatedByRole() {
        if (selectedPatient == null || selectedPatient.getCreatedby() == null) {
            return "—";
        }
        Tuser user = tuserService.findById(selectedPatient.getCreatedby());
        return user != null ? user.getRoleName() : "—";
    }

    /**
     * Obtiene el email del usuario que creó
     */
    public String getCreatedByEmail() {
        if (selectedPatient == null || selectedPatient.getCreatedby() == null) {
            return null;
        }
        Tuser user = tuserService.findById(selectedPatient.getCreatedby());
        return user != null ? user.getEmail() : null;
    }

    private Integer getCurrentUserId() {
        return userSession.getUser() != null ? userSession.getUser().getId() : null;
    }

    // ==================== MÉTRICAS PARA KPI CARDS ====================
    /**
     * Total de pacientes (activos + inactivos)
     */
    public int getTotalPatients() {
        if (patients == null) {
            return 0;
        }
        return patients.size();
    }

    /**
     * Pacientes activos (deleted = false)
     */
    public int getActivePatients() {
        if (patients == null) {
            return 0;
        }
        return (int) patients.stream().filter(p -> !p.getDeleted()).count();
    }

    /**
     * Pacientes inactivos/eliminados (deleted = true)
     */
    public int getInactivePatients() {
        if (patients == null) {
            return 0;
        }
        return (int) patients.stream().filter(p -> p.getDeleted()).count();
    }

    /**
     * Porcentaje de pacientes activos
     */
    public int getActivePercentage() {
        int total = getTotalPatients();
        if (total == 0) {
            return 0;
        }
        return (getActivePatients() * 100) / total;
    }

    /**
     * Porcentaje de pacientes inactivos
     */
    public int getInactivePercentage() {
        int total = getTotalPatients();
        if (total == 0) {
            return 0;
        }
        return (getInactivePatients() * 100) / total;
    }

    /**
     * Pacientes registrados este mes
     */
    public int getPatientsThisMonth() {
        if (patients == null) {
            return 0;
        }
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return (int) patients.stream()
                .filter(p -> p.getCreatedat() != null && p.getCreatedat().isAfter(startOfMonth))
                .count();
    }

    /**
     * Pacientes registrados el mes anterior
     */
    public int getPatientsLastMonth() {
        if (patients == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfLastMonth = now.minusMonths(1)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfLastMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusSeconds(1);

        return (int) patients.stream()
                .filter(p -> p.getCreatedat() != null
                && p.getCreatedat().isAfter(startOfLastMonth)
                && p.getCreatedat().isBefore(endOfLastMonth))
                .count();
    }

    /**
     * Tendencia mensual (porcentaje de cambio vs mes anterior)
     */
    public int getMonthlyTrendPercent() {
        int thisMonth = getPatientsThisMonth();
        int lastMonth = getPatientsLastMonth();

        if (lastMonth == 0) {
            return thisMonth > 0 ? 100 : 0;
        }

        return ((thisMonth - lastMonth) * 100) / lastMonth;
    }

    /**
     * Clase CSS para la tendencia
     */
    public String getMonthlyTrendClass() {
        int trend = getMonthlyTrendPercent();
        if (trend > 0) {
            return "up";
        }
        if (trend < 0) {
            return "down";
        }
        return "";
    }

    /**
     * Icono para la tendencia
     */
    public String getMonthlyTrendIcon() {
        int trend = getMonthlyTrendPercent();
        if (trend > 0) {
            return "up";
        }
        if (trend < 0) {
            return "down";
        }
        return "minus";
    }

    /**
     * Calcula la edad del paciente a partir de su fecha de nacimiento
     */
    public int calculateAge(LocalDate birthday) {
        if (birthday == null) {
            return 0;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * Obtiene el nombre corto del usuario que creó el paciente
     */
    public String getCreatedByShortName(Patient patient) {
        if (patient == null || patient.getCreatedby() == null) {
            return "Sistema";
        }
        try {
            Tuser user = tuserService.findById(patient.getCreatedby());
            if (user != null && user.getProfileid() != null) {
                String firstName = user.getProfileid().getFirstname();
                String lastName = user.getProfileid().getLastname();
                if (firstName != null && !firstName.isEmpty()) {
                    return firstName.substring(0, 1).toUpperCase() + ". " + lastName;
                }
                return lastName;
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "ID: " + patient.getCreatedby();
    }

    /**
     * Reactiva un paciente (elimina el soft delete)
     */
    public void reactivatePatient(Patient patient) {
        try {
            patient.setDeleted(false);
            patient.setDeletedat(null);
            patient.setDeletedby(null);
            patient.setEditedat(LocalDateTime.now());
            patient.setEditedby(getCurrentUserId());

            patientService.save(patient);
            loadPatients();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Paciente reactivado",
                            patient.getFirstname() + " " + patient.getLastname() + " ahora está activo"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo reactivar: " + e.getMessage()));
        }
    }

    /**
     * Desactiva un paciente (soft delete)
     */
    public void deactivatePatient(Patient patient) {
        try {
            patient.setDeleted(true);
            patient.setDeletedat(LocalDateTime.now());
            patient.setDeletedby(getCurrentUserId());

            patientService.save(patient);
            loadPatients();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Paciente dado de baja",
                            patient.getFirstname() + " " + patient.getLastname() + " ha sido desactivado"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo desactivar: " + e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Patient> getPatients() {
        return patients;
    }

    public Patient getNewPatient() {
        return newPatient;
    }

    public void setNewPatient(Patient newPatient) {
        this.newPatient = newPatient;
    }

    public Patient getSelectedPatient() {
        return selectedPatient;
    }

    public void setSelectedPatient(Patient selectedPatient) {
        this.selectedPatient = selectedPatient;
        if (selectedPatient != null) {
            this.newPatient = selectedPatient;
            this.editMode = true;  // <-- MODO EDICIÓN
        }
    }

    public List<Option> getBloodTypes() {
        return bloodTypes;
    }

    public List<Option> getCivilStatusList() {
        return civilStatusList;
    }

    public List<Option> getGenders() {
        return genders;
    }

    // Métodos helper eliminados - usar option.name directamente en el XHTML
    public boolean isEditMode() {
        return editMode;
    }
}
