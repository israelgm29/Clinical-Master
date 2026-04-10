package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.services.OptionService;
import com.mycompany.hospitalgeneral.services.PatientService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class PatientController implements Serializable {

    @Inject
    private PatientService patientService;
    @Inject
    private OptionService optionService;

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
        // Carga de catálogos
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
                newPatient.setCreatedby(0);
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

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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

    
    public String getBloodTypeName(Integer id) {
        if (id == null) {
            return "No definido";
        }
        return bloodTypes.stream()
                .filter(o -> o.getId().equals(id))
                .map(Option::getName)
                .findFirst()
                .orElse("No definido");
    }

    public String getCivilStatusName(Integer id) {
        if (id == null) {
            return "No definido";
        }
        return civilStatusList.stream()
                .filter(o -> o.getId().equals(id))
                .map(Option::getName)
                .findFirst()
                .orElse("No definido");
    }

    public String getGenderName(Integer id) {
        if (id == null) {
            return "No definido";
        }
        return genders.stream()
                .filter(o -> o.getId().equals(id))
                .map(Option::getName)
                .findFirst()
                .orElse("No definido");
    }
    
     public boolean isEditMode() {
        return editMode;
    }
}
