package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Medicalspecialty;
import com.mycompany.hospitalgeneral.services.MedicalspecialtyService;
import com.mycompany.hospitalgeneral.session.UserSession;
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
public class MedicalspecialtyController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MedicalspecialtyService service;

    @Inject
    private UserSession userSession;

    private List<Medicalspecialty> items;
    private Medicalspecialty selected;
    private Medicalspecialty newSpecialty;
    private boolean editMode;
    private String searchTerm;

    @PostConstruct
    public void init() {
        loadItems();
        prepareNew();
    }

    public void loadItems() {
        items = service.findAll();
    }

    public void prepareNew() {
        this.newSpecialty = new Medicalspecialty();
        this.editMode = false;
    }

    public void prepareEdit(Medicalspecialty specialty) {
        this.newSpecialty = specialty;
        this.editMode = true;
    }

    public void save() {
        try {
            if (newSpecialty.getName() == null || newSpecialty.getName().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre es obligatorio");
                return;
            }

            // Verificar duplicados
            if (!editMode) {
                Medicalspecialty existing = service.findByName(newSpecialty.getName().trim());
                if (existing != null) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe una especialidad con ese nombre");
                    return;
                }
            }

            Integer userId = getCurrentUserId();
            service.save(newSpecialty, userId);
            loadItems();
            prepareNew();

            String msg = editMode ? "Especialidad actualizada" : "Especialidad creada";
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg);

            PrimeFaces.current().resetInputs(":dialogs:manage-content");
            PrimeFaces.current().executeScript("PF('manageDialog').hide()");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void delete(Medicalspecialty specialty) {
        try {
            // Verificar si tiene médicos asociados
            if (specialty.getSpecialistCollection() != null && !specialty.getSpecialistCollection().isEmpty()) {
                long activeAssociations = specialty.getSpecialistCollection().stream()
                        .filter(s -> s.getDeleted() == null || !s.getDeleted())
                        .count();
                if (activeAssociations > 0) {
                    addMessage(FacesMessage.SEVERITY_WARN, "No se puede eliminar",
                            "Hay " + activeAssociations + " médicos asociados a esta especialidad");
                    return;
                }
            }

            Integer userId = getCurrentUserId();
            service.delete(specialty.getId(), userId);
            loadItems();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Especialidad eliminada");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void search() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadItems();
        } else {
            String term = "%" + searchTerm.toLowerCase() + "%";
            items = items.stream()
                    .filter(s -> s.getName().toLowerCase().contains(term.toLowerCase())
                    || (s.getDescription() != null && s.getDescription().toLowerCase().contains(term.toLowerCase())))
                    .toList();
        }
    }

    public void clearSearch() {
        searchTerm = null;
        loadItems();
    }

    private Integer getCurrentUserId() {
        if (userSession != null && userSession.getUser() != null) {
            return userSession.getUser().getId();
        }
        return 1;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Medicalspecialty> getItems() {
        return items;
    }

    public Medicalspecialty getSelected() {
        return selected;
    }

    public void setSelected(Medicalspecialty selected) {
        this.selected = selected;
    }

    public Medicalspecialty getNewSpecialty() {
        return newSpecialty;
    }

    public void setNewSpecialty(Medicalspecialty newSpecialty) {
        this.newSpecialty = newSpecialty;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    public int getTotalMedicsAssociated() {
        if (items == null) {
            return 0;
        }
        return items.stream()
                .mapToInt(s -> s.getSpecialistCollection() != null ? s.getSpecialistCollection().size() : 0)
                .sum();
    }
}
