package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Option;
import com.mycompany.hospitalgeneral.model.Tgroup;
import com.mycompany.hospitalgeneral.services.OptionService;
import com.mycompany.hospitalgeneral.services.TgroupService;
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
public class CatalogController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TgroupService tgroupService;

    @Inject
    private OptionService optionService;

    @Inject
    private UserSession userSession;

    // Para Tgroup (Master)
    private List<Tgroup> groups;
    private Tgroup selectedGroup;
    private Tgroup newGroup;
    private boolean editGroupMode;

    // Para Option (Detail)
    private List<Option> options;
    private Option newOption;
    private boolean editOptionMode;

    // Grupo actual para el diálogo de opciones
    private Tgroup currentGroup;

    @PostConstruct
    public void init() {
        loadGroups();
        prepareNewGroup();
        prepareNewOption();
    }

    // ==================== MÉTODOS PARA GRUPOS ====================
    public void loadGroups() {
        groups = tgroupService.findAll();
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
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación",
                                "El nombre del grupo es obligatorio"));
                return;
            }

            // Verificar duplicados en creación
            if (!editGroupMode) {
                Tgroup existing = tgroupService.findByName(newGroup.getName().trim());
                if (existing != null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación",
                                    "Ya existe un grupo con ese nombre"));
                    return;
                }
            }

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

    // ==================== MÉTODOS PARA OPCIONES ====================
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

    /**
     * Prepara una nueva opción para un grupo específico (desde la tarjeta)
     */
    public void prepareNewOptionForGroup(Tgroup group) {
        this.currentGroup = group;
        this.selectedGroup = group;
        this.newOption = new Option();
        this.newOption.setGroupid(group);
        this.editOptionMode = false;
    }

    public void prepareEditOption(Option option) {
        this.newOption = option;
        this.selectedGroup = option.getGroupid();
        this.currentGroup = option.getGroupid();
        this.editOptionMode = true;
    }

    /**
     * Prepara edición de una opción desde la tarjeta
     */
    public void prepareEditOptionFromGroup(Option option, Tgroup group) {
        this.currentGroup = group;
        this.selectedGroup = group;
        this.newOption = option;
        this.editOptionMode = true;
    }

    public void saveOption() {
        try {
            if (newOption.getName() == null || newOption.getName().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación",
                                "El nombre de la opción es obligatorio"));
                return;
            }

            // Verificar duplicados en el mismo grupo
            if (!editOptionMode) {
                Tgroup group = newOption.getGroupid();
                if (group == null) {
                    group = currentGroup;
                    newOption.setGroupid(group);
                }

                List<Option> existingInGroup = optionService.findByGroup(group.getId());
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
            if (newOption.getGroupid() == null && currentGroup != null) {
                newOption.setGroupid(currentGroup);
            }

            Integer userId = getCurrentUserId();
            optionService.save(newOption, userId);
            loadGroups(); // Recargar grupos para actualizar contadores

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
            Integer userId = getCurrentUserId();
            optionService.delete(option.getId(), userId);
            loadGroups(); // Recargar grupos para actualizar contadores

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Opción eliminada"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private Integer getCurrentUserId() {
        if (userSession != null && userSession.getUser() != null) {
            return userSession.getUser().getId();
        }
        return 1; // Usuario administrador por defecto
    }

    /**
     * Obtiene la clase CSS del icono según el nombre del grupo
     */
    public String getCardIconClass(String groupName) {
        if (groupName == null) {
            return "default";
        }

        String name = groupName.toLowerCase();
        if (name.contains("sangre")) {
            return "blood";
        }
        if (name.contains("civil")) {
            return "civil";
        }
        if (name.contains("sexo") || name.contains("género")) {
            return "gender";
        }
        if (name.contains("documento")) {
            return "document";
        }
        if (name.contains("parentesco")) {
            return "kinship";
        }

        return "default";
    }

    /**
     * Obtiene el icono de PrimeFaces según el nombre del grupo
     */
    public String getGroupIcon(String groupName) {
        if (groupName == null) {
            return "pi-folder";
        }

        String name = groupName.toLowerCase();
        if (name.contains("sangre")) {
            return "pi-heart";
        }
        if (name.contains("civil")) {
            return "pi-heart-fill";
        }
        if (name.contains("sexo") || name.contains("género")) {
            return "pi-users";
        }
        if (name.contains("documento")) {
            return "pi-id-card";
        }
        if (name.contains("parentesco")) {
            return "pi-user-plus";
        }

        return "pi-folder";
    }

    /**
     * Total de opciones en todos los grupos
     */
    public int getTotalOptions() {
        if (groups == null) {
            return 0;
        }
        return groups.stream()
                .mapToInt(g -> g.getOptionCollection() != null ? g.getOptionCollection().size() : 0)
                .sum();
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Tgroup> getGroups() {
        return groups;
    }

    public void setGroups(List<Tgroup> groups) {
        this.groups = groups;
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

    public void setEditGroupMode(boolean editGroupMode) {
        this.editGroupMode = editGroupMode;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
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

    public void setEditOptionMode(boolean editOptionMode) {
        this.editOptionMode = editOptionMode;
    }

    public Tgroup getCurrentGroup() {
        return currentGroup;
    }

    public void setCurrentGroup(Tgroup currentGroup) {
        this.currentGroup = currentGroup;
    }
}
