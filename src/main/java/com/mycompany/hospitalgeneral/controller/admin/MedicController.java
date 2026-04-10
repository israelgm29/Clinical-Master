package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.MedicService;
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
public class MedicController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicService medicService;

    private List<Medic> items;
    private Medic selectedMedic;
    private Medic newMedic;
    private boolean editMode = false;

    @PostConstruct
    public void init() {
        loadMedics();
        prepareNewMedic();
    }

    /**
     * Carga todos los médicos activos
     */
    public void loadMedics() {
        items = medicService.findAllActive();
    }

    /**
     * Prepara un nuevo médico (modo creación)
     */
    public void prepareNewMedic() {
        this.newMedic = new Medic();
        this.editMode = false;
    }

    /**
     * Prepara la edición de un médico existente
     * @param selectedMedic
     */
    public void setSelectedMedic(Medic selectedMedic) {
        this.selectedMedic = selectedMedic;
        if (selectedMedic != null) {
            this.newMedic = selectedMedic;
            this.editMode = true;
        }
    }

    /**
     * Guarda o actualiza el médico
     */
    public void saveMedic() {
        try {
            // Limpiar documentos vacíos
            String dni = (newMedic.getDni() != null) ? newMedic.getDni().trim() : "";
            String passport = (newMedic.getPassport() != null) ? newMedic.getPassport().trim() : "";
            newMedic.setDni(dni.isEmpty() ? null : dni);
            newMedic.setPassport(passport.isEmpty() ? null : passport);

            // Validar que tenga al menos un documento
            if (dni.isEmpty() && passport.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                "Debe ingresar al menos un documento (Cédula o Pasaporte)"));
                return;
            }

            // Validar campos obligatorios
            if (newMedic.getLastname() == null || newMedic.getLastname().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                "El apellido es obligatorio"));
                return;
            }

            if (newMedic.getFirstname() == null || newMedic.getFirstname().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                "El nombre es obligatorio"));
                return;
            }

            if (newMedic.getAddress() == null || newMedic.getAddress().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                "La dirección es obligatoria"));
                return;
            }

            // Para nuevos médicos, el email es obligatorio
            if (newMedic.getId() == null) {
                if (newMedic.getEmail() == null || newMedic.getEmail().trim().isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                                    "El correo electrónico es obligatorio para nuevos médicos"));
                    return;
                }
            }

            // Setear createdby para nuevos registros
            if (newMedic.getId() == null) {
                newMedic.setCreatedby(getCurrentUserId());
            }

            // Guardar médico
            medicService.save(newMedic, getCurrentUserId());
            
            // Recargar la lista
            loadMedics();
            
            // Limpiar formulario
            prepareNewMedic();

            // Mensaje de éxito
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                            editMode ? "Médico actualizado correctamente" : "Médico registrado correctamente"));

            // Resetear el formulario del diálogo
            PrimeFaces.current().resetInputs(":dialogs:manage-medic-content");
            
            // Cerrar diálogo y actualizar tabla
            PrimeFaces.current().executeScript("PF('manageMedicDialog').hide()");
            PrimeFaces.current().ajax().update("form:dt-medics", "form:messages");

        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Datos incompletos", e.getMessage()));
        } catch (IllegalStateException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de configuración", e.getMessage()));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate entry") || msg.contains("constraint") || msg.contains("unique"))) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Duplicidad",
                                "El DNI, Pasaporte o Correo electrónico ya existen en el sistema."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Interno",
                                "No se pudo guardar: " + e.getLocalizedMessage()));
            }
            e.printStackTrace();
        }
    }

    /**
     * Eliminación lógica del médico
     */
    public void deleteMedic() {
        if (selectedMedic == null || selectedMedic.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                            "No hay médico seleccionado"));
            return;
        }

        try {
            medicService.delete(selectedMedic.getId(), getCurrentUserId());
            
            // Remover de la lista local
            items.remove(selectedMedic);
            selectedMedic = null;
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Eliminado", 
                            "Médico retirado del sistema correctamente"));
            
            PrimeFaces.current().ajax().update("form:dt-medics", "form:messages");
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                            "No se pudo eliminar: " + e.getMessage()));
        }
    }

    /**
     * Obtiene el ID del usuario actual
     * TODO: Implementar según tu sistema de autenticación
     */
    private Integer getCurrentUserId() {
        // Por ahora retorna 1 (usuario admin)
        // Debes implementar esto con tu sistema de sesión
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            Tuser currentUser = (Tuser) context.getExternalContext().getSessionMap().get("currentUser");
            if (currentUser != null) {
                return currentUser.getId();
            }
        }
        return 1; // Default: usuario administrador
    }

    // ============================================
    // Getters y Setters (igual que PatientController)
    // ============================================
    
    public List<Medic> getItems() {
        return items;
    }

    public void setItems(List<Medic> items) {
        this.items = items;
    }

    public Medic getSelectedMedic() {
        return selectedMedic;
    }

    public Medic getNewMedic() {
        return newMedic;
    }

    public void setNewMedic(Medic newMedic) {
        this.newMedic = newMedic;
    }

    public boolean isEditMode() {
        return editMode;
    }

    /**
     * Total de médicos activos
     */
    public int getTotalActiveMedics() {
        return items != null ? items.size() : 0;
    }

    /**
     * Obtiene el nombre completo del médico para la vista
     */
    public String getFullName(Medic medic) {
        if (medic == null) return "";
        return medic.getLastname() + " " + medic.getFirstname();
    }
}