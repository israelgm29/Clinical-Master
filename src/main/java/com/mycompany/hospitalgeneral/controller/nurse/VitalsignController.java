package com.mycompany.hospitalgeneral.controller.nurse;

import com.mycompany.hospitalgeneral.model.Medicalrecord;
import com.mycompany.hospitalgeneral.model.Patient;
import com.mycompany.hospitalgeneral.model.Vitalsign;
import com.mycompany.hospitalgeneral.services.VitalsignService;
import com.mycompany.hospitalgeneral.session.ConsultationContext;
import com.mycompany.hospitalgeneral.session.UserSession;

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

    @Inject
    private UserSession userSession;

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
            validateVitalRanges();
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
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Valor fuera de rango", e.getMessage()));

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    /**
     * Validacion de rangos
     */
    private void validateVitalRanges() {
        Vitalsign v = newVitalsign;

        if (v.getTemperature() != null) {
            double temp = Double.parseDouble(v.getTemperature());
            if (temp < 34 || temp > 43) {
                throw new IllegalArgumentException(
                        "Temperatura fuera de rango válido (34-43°C): " + temp + "°C");
            }
        }

        if (v.getOxygensaturation() != null) {
            double spo2 = Double.parseDouble(v.getOxygensaturation());
            if (spo2 < 50 || spo2 > 100) {
                throw new IllegalArgumentException(
                        "SpO2 fuera de rango válido (50-100%): " + spo2 + "%");
            }
        }

        if (v.getSystolicpressure() != null) {
            int sys = Integer.parseInt(v.getSystolicpressure());
            if (sys < 60 || sys > 260) {
                throw new IllegalArgumentException(
                        "Presión sistólica fuera de rango (60-260 mmHg): " + sys);
            }
        }

        if (v.getPulse() != null) {
            int pulse = Integer.parseInt(v.getPulse());
            if (pulse < 30 || pulse > 220) {
                throw new IllegalArgumentException(
                        "Pulso fuera de rango (30-220 bpm): " + pulse);
            }
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
        return userSession.getUser() != null ? userSession.getUser().getId() : null;
    }

    /**
     * Retorna clase CSS según rango normal/anormal
     */
    public String getTemperatureClass(String temp) {
        if (temp == null) {
            return "";
        }
        try {
            double value = Double.parseDouble(temp);
            if (value >= 37.5) {
                return "vital-warning";  // Fiebre
            }
            if (value < 36.0) {
                return "vital-danger";    // Hipotermia
            }
            return "vital-normal";
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public String getSpo2Class(String spo2) {
        if (spo2 == null) {
            return "";
        }
        try {
            double value = Double.parseDouble(spo2);
            if (value < 90) {
                return "vital-danger";     // Crítico
            }
            if (value < 95) {
                return "vital-warning";    // Precaución
            }
            return "vital-normal";
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public String getPressureClass(String systolic, String diastolic) {
        if (systolic == null || diastolic == null) {
            return "";
        }
        try {
            int sys = Integer.parseInt(systolic);
            int dia = Integer.parseInt(diastolic);
            if (sys >= 140 || dia >= 90) {
                return "vital-danger";   // Hipertensión
            }
            if (sys < 90 || dia < 60) {
                return "vital-warning";    // Hipotensión
            }
            return "vital-normal";
        } catch (NumberFormatException e) {
            return "";
        }
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
