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

    @Inject
    private ModuleService moduleService;

    private List<Module> modules;
    private List<Module> filteredModules; // ← para el filtro global del dataTable
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

    public void save() {
        try {
            if (newModule.getName() == null || newModule.getName().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre del módulo es obligatorio");
                return;
            }

            if (!editMode) {
                Module existing = moduleService.findByName(newModule.getName().trim());
                if (existing != null) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe un módulo con ese nombre");
                    return;
                }
            }

            moduleService.save(newModule);
            loadModules();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    editMode ? "Módulo actualizado" : "Módulo creado");

            PrimeFaces.current().resetInputs(":dialogs:manage-module-content");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void delete(Module module) {
        try {
            // Verificar si tiene permisos asignados antes de eliminar
            if (module.getPermissionCollection() != null
                    && !module.getPermissionCollection().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "No se puede eliminar",
                        "El módulo '" + module.getName() + "' tiene "
                        + module.getPermissionCollection().size()
                        + " permiso(s) asignado(s)");
                return;
            }

            moduleService.delete(module.getId());
            loadModules();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Módulo eliminado");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════
    // KPIs — calculados desde la lista ya cargada
    // No hacen queries adicionales, reusan `modules`
    // ══════════════════════════════════════════════════
    /**
     * Total de módulos registrados
     */
    public int getTotalModules() {
        return modules != null ? modules.size() : 0;
    }

    /**
     * Módulos que tienen al menos un permiso asignado
     */
    public int getActiveCount() {
        if (modules == null) {
            return 0;
        }
        return (int) modules.stream()
                .filter(m -> m.getPermissionCollection() != null
                && !m.getPermissionCollection().isEmpty())
                .count();
    }

    /**
     * Total de permisos en todos los módulos
     */
    public int getTotalPermissions() {
        if (modules == null) {
            return 0;
        }
        return modules.stream()
                .filter(m -> m.getPermissionCollection() != null)
                .mapToInt(m -> m.getPermissionCollection().size())
                .sum();
    }

    // ══════════════════════════════════════════════════
    // Helper privado para mensajes
    // ══════════════════════════════════════════════════
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // ══════════════════════════════════════════════════
    // Getters y Setters
    // ══════════════════════════════════════════════════
    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getFilteredModules() {
        return filteredModules;
    }

    public void setFilteredModules(List<Module> filteredModules) {
        this.filteredModules = filteredModules;
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
