package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Module;
import com.mycompany.hospitalgeneral.model.Permission;
import com.mycompany.hospitalgeneral.model.Role;
import com.mycompany.hospitalgeneral.services.ModuleService;
import com.mycompany.hospitalgeneral.services.PermissionService;
import com.mycompany.hospitalgeneral.services.RoleService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;

@Named
@ViewScoped
public class PermissionController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Integer DEFAULT_USER_ID = 1;

    @Inject
    private PermissionService permissionService;

    @Inject
    private RoleService roleService;

    @Inject
    private ModuleService moduleService;

    // Datos maestros
    private List<Role> roles;
    private List<Module> modules;

    // Rol seleccionado para edición detallada
    private Role selectedRole;

    // Mapa de permisos: Map<roleId, Map<moduleId, Permission>>
    private Map<Integer, Map<Integer, Permission>> permissionMatrix;

    // Permisos para el rol seleccionado (modo detalle)
    private List<PermissionWrapper> rolePermissions;

    // Cambios pendientes para guardar en lote
    private List<PermissionChange> pendingChanges;

    @PostConstruct
    public void init() {
        permissionMatrix = new HashMap<>();  // Inicializar primero
        pendingChanges = new ArrayList<>();
        loadData();  // Luego cargar datos
    }

    public void loadData() {
        roles = roleService.findAll();
        modules = moduleService.findAll();
        loadPermissionMatrix();
    }

    /**
     * Carga la matriz completa de permisos (todos los roles × todos los
     * módulos)
     */
    public void loadPermissionMatrix() {
        // Verificación de seguridad
        if (permissionMatrix == null) {
            permissionMatrix = new HashMap<>();
        }

        permissionMatrix.clear();

        for (Role role : roles) {
            Map<Integer, Permission> modulePermissions = new HashMap<>();

            for (Module module : modules) {
                Permission perm = permissionService.findByRoleAndModule(role.getId(), module.getId());

                // Si no existe, crear uno vacío (no persistido aún)
                if (perm == null) {
                    perm = new Permission();
                    perm.setRoleid(role);
                    perm.setModuleid(module);
                    perm.setCanCreate(false);
                    perm.setCanRead(false);
                    perm.setCanUpdate(false);
                    perm.setCanDelete(false);
                }

                modulePermissions.put(module.getId(), perm);
            }

            permissionMatrix.put(role.getId(), modulePermissions);
        }
    }

    /**
     * Selecciona un rol para ver/editar sus permisos en modo detalle
     */
    public void selectRole(Role role) {
        this.selectedRole = role;
        loadRolePermissions();
    }

    /**
     * Carga los permisos del rol seleccionado como lista editable
     */
    public void loadRolePermissions() {
        rolePermissions = new ArrayList<>();

        for (Module module : modules) {
            Permission perm = permissionMatrix.get(selectedRole.getId()).get(module.getId());
            rolePermissions.add(new PermissionWrapper(module, perm));
        }
    }

    /**
     * Cambia un permiso individual (C, R, U, D) en la matriz
     */
    public void togglePermission(Integer roleId, Integer moduleId, String permissionType) {
        Permission perm = permissionMatrix.get(roleId).get(moduleId);

        // Toggle del valor
        switch (permissionType) {
            case "CREATE" ->
                perm.setCanCreate(!Boolean.TRUE.equals(perm.getCanCreate()));
            case "READ" ->
                perm.setCanRead(!Boolean.TRUE.equals(perm.getCanRead()));
            case "UPDATE" ->
                perm.setCanUpdate(!Boolean.TRUE.equals(perm.getCanUpdate()));
            case "DELETE" ->
                perm.setCanDelete(!Boolean.TRUE.equals(perm.getCanDelete()));
        }

        // Marcar cambio pendiente
        addPendingChange(roleId, moduleId, permissionType, getPermissionValue(perm, permissionType));
    }

    /**
     * Concede todos los permisos CRUD a un rol para un módulo
     */
    public void grantFullPermission(Integer roleId, Integer moduleId) {
        Permission perm = permissionMatrix.get(roleId).get(moduleId);
        perm.grantAll();

        addPendingChange(roleId, moduleId, "ALL", true);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Permisos concedidos",
                        "Acceso completo otorgado"));
    }

    /**
     * Revoca todos los permisos de un rol para un módulo
     */
    public void revokeAllPermissions(Integer roleId, Integer moduleId) {
        Permission perm = permissionMatrix.get(roleId).get(moduleId);
        perm.revokeAll();

        addPendingChange(roleId, moduleId, "ALL", false);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Permisos revocados",
                        "Todos los accesos eliminados"));
    }

    /**
     * Guarda todos los cambios pendientes en la base de datos
     */
    public void saveAllChanges() {
        try {
            int savedCount = 0;

            for (PermissionChange change : pendingChanges) {
                Permission perm = permissionMatrix.get(change.getRoleId()).get(change.getModuleId());

                // Solo guardar si tiene al menos un permiso o ya existía en BD
                if (perm.getId() != null || perm.hasAnyPermission()) {
                    permissionService.save(perm, DEFAULT_USER_ID);
                    savedCount++;
                }
            }

            pendingChanges.clear();
            loadPermissionMatrix(); // Recargar para obtener IDs de nuevos registros

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            savedCount + " permisos guardados correctamente"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    /**
     * Guarda los permisos del rol seleccionado (modo detalle)
     */
    public void saveRolePermissions() {
        try {
            int savedCount = 0;

            for (PermissionWrapper wrapper : rolePermissions) {
                Permission perm = wrapper.getPermission();

                if (perm.getId() != null || perm.hasAnyPermission()) {
                    permissionService.save(perm, DEFAULT_USER_ID);
                    savedCount++;
                }
            }

            // Actualizar matriz
            loadPermissionMatrix();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Permisos del rol guardados (" + savedCount + " registros)"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    /**
     * Copia permisos de un rol a otro
     */
    public void copyPermissionsFromRole(Role sourceRole, Role targetRole) {
        try {
            permissionService.copyPermissionsFromRole(sourceRole.getId(), targetRole.getId(), DEFAULT_USER_ID);
            loadPermissionMatrix();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Permisos copiados de " + sourceRole.getName() + " a " + targetRole.getName()));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    /**
     * Inicializa permisos vacíos para un nuevo rol
     */
    public void initializeRolePermissions(Role role) {
        try {
            permissionService.initializePermissionsForNewRole(role.getId(), DEFAULT_USER_ID);
            loadPermissionMatrix();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                            "Permisos inicializados para " + role.getName()));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================
    private void addPendingChange(Integer roleId, Integer moduleId, String type, Boolean value) {
        // Eliminar cambio previo para la misma combinación
        pendingChanges.removeIf(c -> c.getRoleId().equals(roleId)
                && c.getModuleId().equals(moduleId)
                && c.getType().equals(type));

        pendingChanges.add(new PermissionChange(roleId, moduleId, type, value));
    }

    private Boolean getPermissionValue(Permission perm, String type) {
        return switch (type) {
            case "CREATE" ->
                perm.getCanCreate();
            case "READ" ->
                perm.getCanRead();
            case "UPDATE" ->
                perm.getCanUpdate();
            case "DELETE" ->
                perm.getCanDelete();
            default ->
                false;
        };
    }

    public Permission getPermission(Integer roleId, Integer moduleId) {
        return permissionMatrix.getOrDefault(roleId, new HashMap<>()).get(moduleId);
    }

    public boolean hasPendingChanges() {
        return !pendingChanges.isEmpty();
    }

    // ==================== CLASES WRAPPER ====================
    /**
     * Wrapper para mostrar permisos de un rol en modo detalle
     */
    public static class PermissionWrapper {

        private Module module;
        private Permission permission;

        public PermissionWrapper(Module module, Permission permission) {
            this.module = module;
            this.permission = permission;
        }

        public Module getModule() {
            return module;
        }

        public Permission getPermission() {
            return permission;
        }
    }

    /**
     * Registro de cambios pendientes
     */
    public static class PermissionChange {

        private Integer roleId;
        private Integer moduleId;
        private String type;
        private Boolean value;

        public PermissionChange(Integer roleId, Integer moduleId, String type, Boolean value) {
            this.roleId = roleId;
            this.moduleId = moduleId;
            this.type = type;
            this.value = value;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public Integer getModuleId() {
            return moduleId;
        }

        public String getType() {
            return type;
        }

        public Boolean getValue() {
            return value;
        }
    }

    // ==================== GETTERS Y SETTERS ====================
    public List<Role> getRoles() {
        return roles;
    }

    public List<Module> getModules() {
        return modules;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    public List<PermissionWrapper> getRolePermissions() {
        return rolePermissions;
    }

    public Map<Integer, Map<Integer, Permission>> getPermissionMatrix() {
        return permissionMatrix;
    }

    public List<PermissionChange> getPendingChanges() {
        return pendingChanges;
    }
}
