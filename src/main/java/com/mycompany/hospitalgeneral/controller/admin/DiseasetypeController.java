package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Diseasetype;
import com.mycompany.hospitalgeneral.services.DiseasetypeService;
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
public class DiseasetypeController implements Serializable {

    @Inject
    private DiseasetypeService diseasetypeService;

    private List<Diseasetype> items;
    private Diseasetype selected;
    private boolean editMode;
    
    private static final Integer DEFAULT_USER_ID = 1;

    @PostConstruct
    public void init() {
        loadItems();
        prepareCreate();
    }

    public void loadItems() {
        items = diseasetypeService.findAll();
    }

    public void prepareCreate() {
        this.selected = new Diseasetype();
        this.editMode = false;
    }

    public void prepareEdit(Diseasetype diseasetype) {
        this.selected = diseasetype;
        this.editMode = true;
    }

    public void save() {
        try {
            if (selected.getName() == null || selected.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre es obligatorio"));
                return;
            }

            if (!editMode && diseasetypeService.findByName(selected.getName().trim()) != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe un tipo con ese nombre"));
                return;
            }

            diseasetypeService.save(selected, DEFAULT_USER_ID);
            loadItems();
            
            String msg = editMode ? "Tipo actualizado" : "Tipo creado";
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));
            
            PrimeFaces.current().resetInputs(":dialogs:manage-diseasetype-content");
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void delete(Diseasetype diseasetype) {
        try {
            diseasetypeService.delete(diseasetype.getId(), DEFAULT_USER_ID);
            loadItems();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tipo eliminado"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Diseasetype> getItems() { return items; }
    public Diseasetype getSelected() { return selected; }
    public void setSelected(Diseasetype selected) { this.selected = selected; }
    public boolean isEditMode() { return editMode; }
}