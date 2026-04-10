package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Examtype;
import com.mycompany.hospitalgeneral.services.ExamtypeService;
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
public class ExamtypeController implements Serializable {

    @Inject
    private ExamtypeService examtypeService;

    private List<Examtype> items;
    private Examtype selected;
    private boolean editMode;
    
    // ID del usuario sistema/admin por defecto hasta implementar sesiones
    private static final Integer DEFAULT_USER_ID = 1;

    @PostConstruct
    public void init() {
        loadItems();
        prepareCreate();
    }

    public void loadItems() {
        items = examtypeService.findAll();
    }

    public void prepareCreate() {
        this.selected = new Examtype();
        this.editMode = false;
    }

    public void prepareEdit(Examtype examtype) {
        this.selected = examtype;
        this.editMode = true;
    }

    public void save() {
        try {
            // Validaciones
            if (selected.getName() == null || selected.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre es obligatorio"));
                return;
            }

            // Verificar duplicados en creación
            if (!editMode) {
                Examtype existing = examtypeService.findByName(selected.getName().trim());
                if (existing != null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe un tipo de examen con ese nombre"));
                    return;
                }
            }

            examtypeService.save(selected, DEFAULT_USER_ID);
            
            loadItems();
            
            String msg = editMode ? "Tipo de examen actualizado" : "Tipo de examen creado";
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));
            
            PrimeFaces.current().resetInputs(":dialogs:manage-examtype-content");
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar: " + e.getMessage()));
        }
    }

    public void delete(Examtype examtype) {
        try {
            examtypeService.delete(examtype.getId(), DEFAULT_USER_ID);
            loadItems();
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tipo de examen eliminado"));
                
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar: " + e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Examtype> getItems() {
        return items;
    }

    public Examtype getSelected() {
        return selected;
    }

    public void setSelected(Examtype selected) {
        this.selected = selected;
    }

    public boolean isEditMode() {
        return editMode;
    }
}