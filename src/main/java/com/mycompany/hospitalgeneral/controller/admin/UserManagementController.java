package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Profile;
import com.mycompany.hospitalgeneral.model.Role;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.NotificationService;
import com.mycompany.hospitalgeneral.services.ProfileService;
import com.mycompany.hospitalgeneral.services.RoleService;
import com.mycompany.hospitalgeneral.services.TuserService;
import com.mycompany.hospitalgeneral.session.UserSession;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class UserManagementController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Services ──────────────────────────────────────────────────────
    @Inject
    private TuserService tuserService;
    @Inject
    private RoleService roleService;
    @Inject
    private ProfileService profileService;
    @Inject
    private MedicService medicService;

    // ── ✅ Nuevo: servicio de notificaciones ──────────────────────────
    @Inject
    private NotificationService notificationService;

    // ── Session ───────────────────────────────────────────────────────
    @Inject
    private UserSession userSession;

    // ── Listas ────────────────────────────────────────────────────────
    private List<Tuser> users;
    private List<Role> roles;

    // ── Filtro ────────────────────────────────────────────────────────
    private String searchQuery;

    // ── Usuario en edición / creación ─────────────────────────────────
    private Tuser editingUser;
    private Profile editingProfile;
    private Medic editingMedic;
    private Role selectedRole;
    private boolean creatingNew = false;

    // ── Usuario seleccionado para detalle ─────────────────────────────
    private Tuser selectedUser;

    // ── Flag de formulario dinámico ───────────────────────────────────
    private boolean showMedicFields = false;

    // ─────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────
    @PostConstruct
    public void init() {
        loadUsers();
        roles = roleService.findAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // CARGA DE DATOS
    // ─────────────────────────────────────────────────────────────────
    public void loadUsers() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            users = tuserService.findAll();
        } else {
            users = tuserService.searchFlexible(searchQuery.trim());
        }
    }

    public void clearSearch() {
        searchQuery = null;
        loadUsers();
    }

    // ─────────────────────────────────────────────────────────────────
    // CREAR USUARIO
    // ─────────────────────────────────────────────────────────────────
    public void prepareCreate() {
        creatingNew = true;
        editingUser = new Tuser();
        editingProfile = new Profile();
        editingMedic = new Medic();
        selectedRole = null;
        showMedicFields = false;
    }

    public void saveNewUser() {
        try {
            if (selectedRole == null) {
                addError("Debe seleccionar un rol");
                return;
            }

            Integer adminId = getCurrentUserId();

            // 1. Guardar Profile
            profileService.save(editingProfile, adminId);

            // 2. Configurar y guardar Tuser
            editingUser.setProfileid(editingProfile);
            editingUser.setRoleid(selectedRole);
            editingUser.setIsactive(false);
            editingUser.setEmailverified(false);
            tuserService.create(editingUser, adminId);

            // 3. Si es Médico, crear entidad Medic vinculada
            if (isMedicRole(selectedRole)) {
                editingMedic.setUserid(editingUser);
                medicService.save(editingMedic, adminId);
            }

            // 4. Enviar email de confirmación
            tuserService.sendVerificationEmail(editingUser);

            addInfo("Usuario creado. Se envió email de confirmación a " + editingUser.getEmail());
            loadUsers();

        } catch (Exception e) {
            addError("Error al crear usuario: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // EDITAR USUARIO
    // ─────────────────────────────────────────────────────────────────
    public void prepareEdit(Tuser user) {
        creatingNew = false;
        editingUser = tuserService.findById(user.getId());
        editingProfile = editingUser.getProfileid();
        selectedRole = editingUser.getRoleid();
        showMedicFields = isMedicRole(selectedRole);

        if (showMedicFields && editingUser.getMedic() != null) {
            editingMedic = editingUser.getMedic();
        } else {
            editingMedic = new Medic();
        }
    }

    public void saveEdit() {
        try {
            Integer adminId = getCurrentUserId();

            editingUser.setRoleid(selectedRole);
            profileService.update(editingProfile, adminId);
            tuserService.update(editingUser);

            if (isMedicRole(selectedRole)) {
                editingMedic.setUserid(editingUser);
                medicService.save(editingMedic, adminId);
            }

            addInfo("Usuario actualizado correctamente");
            loadUsers();

        } catch (Exception e) {
            addError("Error al guardar cambios: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // VER DETALLE
    // ─────────────────────────────────────────────────────────────────
    public void prepareDetail(Tuser user) {
        selectedUser = tuserService.findById(user.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // ACTIVAR / DESACTIVAR
    // ─────────────────────────────────────────────────────────────────
    public void toggleActive(Tuser user) {
        try {
            boolean nuevoEstado = !user.getIsactive();
            tuserService.setActive(user.getId(), nuevoEstado, getCurrentUserId());

            // ✅ Notificar a admins solo cuando se ACTIVA una cuenta
            if (nuevoEstado) {
                List<Tuser> admins = tuserService.findAllByRole("Administrador");
                notificationService.notifyAdminsAccountActivated(
                        admins,
                        user.getFullName()
                );
            }

            addInfo(user.getFullName() + (nuevoEstado ? " activado" : " desactivado"));
            loadUsers();

        } catch (Exception e) {
            addError("Error al cambiar estado: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // ELIMINAR (soft delete)
    // ─────────────────────────────────────────────────────────────────
    public void deleteUser(Tuser user) {
        try {
            tuserService.softDelete(user.getId(), getCurrentUserId());
            addInfo("Usuario eliminado: " + user.getFullName());
            loadUsers();

        } catch (Exception e) {
            addError("Error al eliminar: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // EMAIL: ENVIAR CONFIRMACIÓN
    // ─────────────────────────────────────────────────────────────────
    public void sendVerificationEmail(Tuser user) {
        try {
            tuserService.sendVerificationEmail(user);
            addInfo("Email de verificación enviado a " + user.getEmail());
        } catch (Exception e) {
            addError("Error al enviar email: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // RESETEAR CONTRASEÑA
    // ─────────────────────────────────────────────────────────────────
    public void resetPassword(Tuser user) {
        try {
            tuserService.resetPasswordAndNotify(user);

            // ✅ Notificar a todos los admins que se reseteó la contraseña
            List<Tuser> admins = tuserService.findAllByRole("Administrador");
            notificationService.notifyAdminsPasswordChanged(
                    admins,
                    user.getFullName()
            );

            addInfo("Contraseña reseteada. Email enviado a " + user.getEmail());

        } catch (Exception e) {
            addError("Error al resetear contraseña: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // CAMBIO DE ROL
    // ─────────────────────────────────────────────────────────────────
    public void onRoleChange() {
        showMedicFields = isMedicRole(selectedRole);
        if (showMedicFields && editingMedic == null) {
            editingMedic = new Medic();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────
    private boolean isMedicRole(Role role) {
        return role != null && "Médico".equals(role.getName());
    }

    private Integer getCurrentUserId() {
        Tuser u = userSession.getUser();
        return u != null ? u.getId() : null;
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

    // ─────────────────────────────────────────────────────────────────
    // GETTERS / SETTERS
    // ─────────────────────────────────────────────────────────────────
    public List<Tuser> getUsers() {
        return users;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String q) {
        this.searchQuery = q;
    }

    public Tuser getEditingUser() {
        return editingUser;
    }

    public void setEditingUser(Tuser u) {
        this.editingUser = u;
    }

    public Profile getEditingProfile() {
        return editingProfile;
    }

    public void setEditingProfile(Profile p) {
        this.editingProfile = p;
    }

    public Medic getEditingMedic() {
        return editingMedic;
    }

    public void setEditingMedic(Medic m) {
        this.editingMedic = m;
    }

    public Role getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(Role r) {
        this.selectedRole = r;
    }

    public Tuser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Tuser u) {
        this.selectedUser = u;
    }

    public boolean isCreatingNew() {
        return creatingNew;
    }

    public boolean isShowMedicFields() {
        return showMedicFields;
    }
}
