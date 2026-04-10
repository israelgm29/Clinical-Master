package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.model.Tgroup;
import com.mycompany.hospitalgeneral.services.OptionService;
import com.mycompany.hospitalgeneral.services.TgroupService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

@Named
@ViewScoped
public class CatalogController implements Serializable {

    @Inject
    private TgroupService tgroupService;

    @Inject
    private OptionService optionService;

    // Para Tgroup (Master) - SIN auditoría
    private List<Tgroup> groups;
    private Tgroup selectedGroup;
    private Tgroup newGroup;
    private boolean editGroupMode;

    // Para Option (Detail) - CON auditoría
    private List<Option> options;
    private Option selectedOption;
    private Option newOption;
    private boolean editOptionMode;

    private static final Integer DEFAULT_USER_ID = 1;

    @PostConstruct
    public void init() {
        System.out.println(">>> CatalogController.init() ejecutado");
        loadGroups();
        prepareNewGroup();
        prepareNewOption();
        System.out.println(">>> Grupos cargados: " + (groups != null ? groups.size() : "null"));
    }

    public void loadGroups() {
        System.out.println(">>> loadGroups() ejecutado");
        groups = tgroupService.findAll();
        System.out.println(">>> Grupos encontrados: " + (groups != null ? groups.size() : "null"));
        if (groups != null && !groups.isEmpty()) {
            for (Tgroup g : groups) {
                System.out.println("  - Grupo: " + g.getId() + " = " + g.getName());
            }
        }
    }

    public void prepareNewGroup() {
        this.newGroup = new Tgroup();
        this.editGroupMode = false;
    }

    public void prepareEditGroup(Tgroup group) {
        this.newGroup = group;
        this.editGroupMode = true;
    }

    public void saveGroup() {
        try {
            if (newGroup.getName() == null || newGroup.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre del grupo es obligatorio"));
                return;
            }

            // Verificar duplicados en creación
            if (!editGroupMode) {
                Tgroup existing = tgroupService.findByName(newGroup.getName().trim());
                if (existing != null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "Ya existe un grupo con ese nombre"));
                    return;
                }
            }

            // SIN parámetro de usuario - Tgroup no tiene auditoría
            tgroupService.save(newGroup);
            loadGroups();

            String msg = editGroupMode ? "Grupo actualizado" : "Grupo creado";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));

            PrimeFaces.current().resetInputs(":dialogs:manage-group-content");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void deleteGroup(Tgroup group) {
        try {
            // Verificar si tiene opciones asociadas activas
            List<Option> groupOptions = optionService.findByGroup(group.getId());
            long activeOptions = groupOptions.stream()
                    .filter(o -> o.getDeleted() == null || !o.getDeleted())
                    .count();

            if (activeOptions > 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "No se puede eliminar",
                                "El grupo tiene " + activeOptions + " opciones activas. Elimínelas primero."));
                return;
            }

            // SIN parámetro de usuario - Tgroup no tiene auditoría
            tgroupService.delete(group.getId());
            loadGroups();

            // Si el grupo eliminado era el seleccionado, limpiar
            if (selectedGroup != null && selectedGroup.getId().equals(group.getId())) {
                selectedGroup = null;
                options = null;
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Grupo eliminado"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // ==================== OPTION (DETAIL) - CON AUDITORÍA ====================
    public void selectGroup(Tgroup group) {
        this.selectedGroup = group;
        loadOptions();
        prepareNewOption();
    }

    public void loadOptions() {
        if (selectedGroup != null) {
            options = optionService.findByGroup(selectedGroup.getId());
        } else {
            options = null;
        }
    }

    public void prepareNewOption() {
        this.newOption = new Option();
        this.newOption.setGroupid(selectedGroup);
        this.editOptionMode = false;
    }

    public void prepareEditOption(Option option) {
        this.newOption = option;
        this.editOptionMode = true;
    }

    public void saveOption() {
        try {
            if (newOption.getName() == null || newOption.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre de la opción es obligatorio"));
                return;
            }

            // Verificar duplicados en el mismo grupo
            if (!editOptionMode) {
                List<Option> existingInGroup = optionService.findByGroup(selectedGroup.getId());
                boolean duplicate = existingInGroup.stream()
                        .anyMatch(o -> o.getName().equalsIgnoreCase(newOption.getName().trim())
                        && (o.getDeleted() == null || !o.getDeleted()));
                if (duplicate) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación",
                                    "Ya existe una opción con ese nombre en este grupo"));
                    return;
                }
            }

            // Asegurar que tenga el grupo asignado
            if (newOption.getGroupid() == null) {
                newOption.setGroupid(selectedGroup);
            }

            // CON parámetro de usuario - Option tiene auditoría
            optionService.save(newOption, DEFAULT_USER_ID);
            loadOptions();

            String msg = editOptionMode ? "Opción actualizada" : "Opción creada";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));

            PrimeFaces.current().resetInputs(":dialogs:manage-option-content");

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void deleteOption(Option option) {
        try {
            // CON parámetro de usuario - Option tiene auditoría
            optionService.delete(option.getId(), DEFAULT_USER_ID);
            loadOptions();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Opción eliminada"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void onGroupSelect(SelectEvent<Tgroup> event) {
        this.selectedGroup = event.getObject();
        System.out.println(">>> Grupo seleccionado (tabla): " + selectedGroup.getName());
        loadOptions();
        prepareNewOption();
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Tgroup> getGroups() {
        return groups;
    }

    public Tgroup getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Tgroup selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public Tgroup getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(Tgroup newGroup) {
        this.newGroup = newGroup;
    }

    public boolean isEditGroupMode() {
        return editGroupMode;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Option getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(Option selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Option getNewOption() {
        return newOption;
    }

    public void setNewOption(Option newOption) {
        this.newOption = newOption;
    }

    public boolean isEditOptionMode() {
        return editOptionMode;
    }
}
