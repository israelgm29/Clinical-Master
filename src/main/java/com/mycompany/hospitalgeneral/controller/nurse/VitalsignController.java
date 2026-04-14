package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.VitalsignService;
import com.mycompany.hospitalgeneral.session.ConsultationContext;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class VitalsignController implements Serializable {

    private static final long serialVersionUID = 1L;

    // === SERVICES ===
    @Inject
    private VitalsignService vitalsignService;

    // === SESSION CONTEXT (REUTILIZADO) ===
    @Inject
    private ConsultationContext consultationContext;

    // === DATOS ===
    private Patient currentPatient;
    private Medicalrecord selectedRecord;

    private Vitalsign newVitalsign;
    private Vitalsign selectedVitalsign;
    private List<Vitalsign> vitalsigns;

    private boolean editMode = false;

    // ==================== INIT ====================
    @PostConstruct
    public void init() {
        loadContextData();
        prepareNewVitalsign();

        if (selectedRecord != null) {
            loadVitalsigns();
        }
    }

    /**
     * Cargar datos desde sesión (ConsultationContext)
     */
    private void loadContextData() {
        selectedRecord = consultationContext.getCurrentMedicalRecord();
        currentPatient = consultationContext.getCurrentPatient();

        if (selectedRecord == null || currentPatient == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "No hay paciente en contexto"));
        }
    }

    // ==================== CRUD ====================
    public void prepareNewVitalsign() {
        this.newVitalsign = new Vitalsign();
        this.editMode = false;
    }

    public void setSelectedVitalsign(Vitalsign vitalsign) {
        this.selectedVitalsign = vitalsign;

        if (vitalsign != null) {
            this.newVitalsign = vitalsign;
            this.editMode = true;
        }
    }

    public void loadVitalsigns() {
        if (selectedRecord != null) {
            vitalsigns = vitalsignService.findByMedicalRecord(selectedRecord.getId());
        }
    }

    public void reloadVitalsigns() {
        loadVitalsigns();
    }

    /**
     * Guardar o actualizar signos vitales
     */
    public void saveVitalsign() {
        try {
            if (selectedRecord == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Advertencia", "Debe seleccionar una historia clínica"));
                return;
            }

            newVitalsign.setMedicalrecordid(selectedRecord);

            if (newVitalsign.getId() == null) {
                vitalsignService.save(newVitalsign, getCurrentUserId());
            } else {
                vitalsignService.update(newVitalsign, getCurrentUserId());
            }

            // Recargar lista
            loadVitalsigns();

            // Limpiar
            prepareNewVitalsign();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Éxito",
                            editMode ? "Signos vitales actualizados correctamente"
                                    : "Signos vitales registrados correctamente"));

            PrimeFaces.current().ajax().update("vitalForm:vitalsignsTable", "vitalForm");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    /**
     * Eliminación lógica
     */
    public void deleteVitalsign() {
        if (selectedVitalsign == null || selectedVitalsign.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Advertencia", "No hay registro seleccionado"));
            return;
        }

        try {
            selectedVitalsign.setDeleted(true);
            vitalsignService.update(selectedVitalsign, getCurrentUserId());

            vitalsigns.remove(selectedVitalsign);
            selectedVitalsign = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Eliminado", "Registro eliminado correctamente"));

            PrimeFaces.current().ajax().update("vitalForm:vitalsignsTable", "vitalForm");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    // ==================== LÓGICA ====================
    /**
     * Cálculo de IMC automático
     */
    public void calculateIMC() {
        try {
            if (newVitalsign.getWeight() != null && newVitalsign.getTall() != null) {

                double peso = Double.parseDouble(newVitalsign.getWeight().toString());
                double talla = Double.parseDouble(newVitalsign.getTall().toString()) / 100.0;

                if (talla > 0) {
                    double imc = peso / (talla * talla);

                    BigDecimal imcRounded = new BigDecimal(imc)
                            .setScale(2, RoundingMode.HALF_UP);

                    newVitalsign.setMass(imcRounded.toString());
                }
            }
        } catch (Exception e) {
            // ignorar errores de conversión
        }
    }

    // ==================== HELPERS ====================
    private Integer getCurrentUserId() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context != null) {
            Tuser currentUser = (Tuser) context.getExternalContext()
                    .getSessionMap()
                    .get("currentUser");

            if (currentUser != null) {
                return currentUser.getId();
            }
        }

        return 1; // fallback
    }

    // ==================== GETTERS ====================
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

    public Medicalrecord getSelectedRecord() {
        return selectedRecord;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }
}
