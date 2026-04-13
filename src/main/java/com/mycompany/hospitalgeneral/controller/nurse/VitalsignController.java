package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.VitalsignService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class VitalsignController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private VitalsignService vitalsignService;

    @Inject
    private HttpSession session;

    private Patient currentPatient;
    private Vitalsign newVitalsign;
    private Vitalsign selectedVitalsign;
    private Medicalrecord selectedRecord;
    private List<Vitalsign> vitalsigns;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        // Cargar record y paciente desde sesión
        selectedRecord = (Medicalrecord) session.getAttribute("currentNurseRecord");
        currentPatient = (Patient) session.getAttribute("currentNursePatient");

        prepareNewVitalsign();

        // Cargar signos vitales existentes si hay un record
        if (selectedRecord != null) {
            vitalsigns = vitalsignService.findByMedicalRecord(selectedRecord.getId());
        }
    }

    /**
     * Inicializa un nuevo registro de signos vitales
     */
    public void prepareNewVitalsign() {
        this.newVitalsign = new Vitalsign();
        this.editMode = false;
    }

    /**
     * Prepara la edición de un registro existente
     */
    public void setSelectedVitalsign(Vitalsign vitalsign) {
        this.selectedVitalsign = vitalsign;
        if (vitalsign != null) {
            this.newVitalsign = vitalsign;
            this.editMode = true;
        }
    }

    /**
     * Carga los signos vitales de una historia clínica
     */
    public void loadVitalsigns(Medicalrecord record) {
        this.selectedRecord = record;
        this.vitalsigns = vitalsignService.findByMedicalRecord(record.getId());
    }

    /**
     * Recarga los signos vitales del record actual
     */
    public void reloadVitalsigns() {
        if (selectedRecord != null) {
            vitalsigns = vitalsignService.findByMedicalRecord(selectedRecord.getId());
        }
    }

    /**
     * Calcula el IMC automáticamente cuando se ingresan peso y talla
     */
    public void calculateIMC() {
        try {
            if (newVitalsign.getWeight() != null && newVitalsign.getTall() != null) {
                double peso = Double.parseDouble(newVitalsign.getWeight().toString());
                double talla = Double.parseDouble(newVitalsign.getTall().toString()) / 100.0; // cm a m
                if (talla > 0) {
                    double imc = peso / (talla * talla);
                    BigDecimal imcRounded = new BigDecimal(imc).setScale(2, RoundingMode.HALF_UP);
                    newVitalsign.setMass(imcRounded.toString());
                }
            }
        } catch (Exception e) {
            // Si los valores no son numéricos, no calcular
        }
    }

    /**
     * Guarda o actualiza un registro de signos vitales
     */
    public void saveVitalsign() {
        try {
            if (selectedRecord == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar una historia clínica"));
                return;
            }

            newVitalsign.setMedicalrecordid(selectedRecord);

            if (newVitalsign.getId() == null) {
                vitalsignService.save(newVitalsign, getCurrentUserId());
            } else {
                vitalsignService.update(newVitalsign, getCurrentUserId());
            }

            // Recargar lista
            loadVitalsigns(selectedRecord);

            // Limpiar formulario
            prepareNewVitalsign();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            editMode ? "Signos vitales actualizados correctamente" : "Signos vitales registrados correctamente"));

            PrimeFaces.current().ajax().update("form:vitalsignsTable", "form:messages");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    /**
     * Eliminación lógica de un registro de signos vitales
     */
    public void deleteVitalsign() {
        if (selectedVitalsign == null || selectedVitalsign.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No hay registro seleccionado"));
            return;
        }

        try {
            selectedVitalsign.setDeleted(true);
            vitalsignService.update(selectedVitalsign, getCurrentUserId());

            vitalsigns.remove(selectedVitalsign);
            selectedVitalsign = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Eliminado", "Registro de signos vitales eliminado correctamente"));

            PrimeFaces.current().ajax().update("form:vitalsignsTable", "form:messages");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    /**
     * Obtiene el ID del usuario actual (igual que en MedicController)
     */
    private Integer getCurrentUserId() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            Tuser currentUser = (Tuser) context.getExternalContext().getSessionMap().get("currentUser");
            if (currentUser != null) {
                return currentUser.getId();
            }
        }
        return 1; // Default: usuario enfermera/admin
    }

    // Getters y Setters
    public Vitalsign getNewVitalsign() {
        return newVitalsign;
    }

    public void setNewVitalsign(Vitalsign newVitalsign) {
        this.newVitalsign = newVitalsign;
    }

    public Vitalsign getSelectedVitalsign() {
        return selectedVitalsign;
    }

    public List<Vitalsign> getVitalsigns() {
        return vitalsigns;
    }

    public void setVitalsigns(List<Vitalsign> vitalsigns) {
        this.vitalsigns = vitalsigns;
    }

    public Medicalrecord getSelectedRecord() {
        return selectedRecord;
    }

    public void setSelectedRecord(Medicalrecord selectedRecord) {
        this.selectedRecord = selectedRecord;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(Patient currentPatient) {
        this.currentPatient = currentPatient;
    }
}
