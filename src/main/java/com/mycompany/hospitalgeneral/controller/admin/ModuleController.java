package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Module;
import com.mycompany.hospitalgeneral.services.ModuleService;
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
public class ModuleController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Integer DEFAULT_USER_ID = 1;

    @Inject
    private ModuleService moduleService;

    private List<Module> modules;
    private Module selectedModule;
    private Module newModule;
    private boolean editMode;

    @PostConstruct
    public void init() {
        loadModules();
        prepareNewModule();
    }

    public void loadModules() {
        modules = moduleService.findAll();
    }

    public void prepareNewModule() {
        this.newModule = new Module();
        this.editMode = false;
    }

    public void prepareEdit(Module module) {
        this.newModule = module;
        this.editMode = true;
    }

    // Cambiar estos métodos:
    public void save() {
        try {
            if (newModule.getName() == null || newModule.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre del módulo es obligatorio"));
                return;
            }

            if (!editMode) {
                Module existing = moduleService.findByName(newModule.getName().trim());
                if (existing != null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe un módulo con ese nombre"));
                    return;
                }
            }

            moduleService.save(newModule); // Sin userId
            loadModules();

            String msg = editMode ? "Módulo actualizado" : "Módulo creado";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));

            PrimeFaces.current().resetInputs(":dialogs:manage-module-content");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void delete(Module module) {
        try {
            moduleService.delete(module.getId()); // Sin userId
            loadModules();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Módulo eliminado"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Module> getModules() {
        return modules;
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(Module selectedModule) {
        this.selectedModule = selectedModule;
    }

    public Module getNewModule() {
        return newModule;
    }

    public void setNewModule(Module newModule) {
        this.newModule = newModule;
    }

    public boolean isEditMode() {
        return editMode;
    }
}
