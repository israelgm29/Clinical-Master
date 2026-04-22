package com.mycompany.hospitalgeneral.controller;

import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.TuserService;
import com.mycompany.hospitalgeneral.services.interfaces.ProfileData;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Named
@ViewScoped
public class ProfileController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== CONSTANTES ====================
    private static final String UPLOAD_DIR = "/opt/clinical-master/avatars/";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    // ==================== INYECCIONES ====================
    @Inject
    private UserSession session;

    @Inject
    private TuserService tuserService;

    @Inject
    private FacesContext facesContext;

    // ==================== ESTADO ====================
    private boolean editMode = false;
    private Tuser editingUser;
    private Part uploadedAvatar;
    private String modeFromUrl;

    // ==================== MÉTODOS DE ACCIÓN ====================
    public void enableEditMode() {
        Tuser currentUser = session.getUser();
        if (currentUser != null) {
            this.editingUser = currentUser;
            this.editMode = true;
        } else {
            addErrorMessage("No se pudo cargar el usuario para edición");
        }
    }

    public void cancelEdit() {
        this.editingUser = null;
        this.editMode = false;
        this.uploadedAvatar = null;

        if (session.getUser() != null) {
            Tuser refreshed = tuserService.findById(session.getUser().getId());
            session.setUser(refreshed);
        }
    }

    public String saveProfile() {
        try {
            if (editingUser == null) {
                addErrorMessage("No hay datos para guardar");
                return null;
            }

            if (!validateProfileData(editingUser)) {
                return null;
            }

            Tuser saved = tuserService.update(editingUser);
            session.setUser(saved);

            addInfoMessage("Perfil actualizado", "Los cambios se guardaron correctamente");

            cancelEdit();

        } catch (Exception e) {
            addErrorMessage("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public String goToChangePassword() {
        return "/views/shared/change-password.xhtml?faces-redirect=true";
    }

    // ==================== TEMPLATE DINÁMICO ====================
    public String loadTemplate() {
        Tuser user = session.getUser();

        if (user == null || user.getRoleName() == null) {
            return "/WEB-INF/templates/admin-template.xhtml";
        }

        String roleName = user.getRoleName();
        System.out.println(roleName);

        if (roleName == null) {
            return "/WEB-INF/templates/admin-template.xhtml";
        }

        switch (roleName) {
            case "Médico":
                return "/WEB-INF/templates/medic-template.xhtml";
            case "Enfermería":
                return "/WEB-INF/templates/nurse-template.xhtml";
            case "Administrador":
                return "/WEB-INF/templates/admin-template.xhtml";
            default:
                return "/WEB-INF/templates/admin-template.xhtml";
        }
    }

    // ==================== VALIDACIONES ====================
    private boolean validateProfileData(Tuser user) {
        ProfileData data = user;

        if (data.getEmail() == null || data.getEmail().trim().isEmpty()) {
            addErrorMessage("El correo electrónico es obligatorio");
            return false;
        }

        if (data.getFirstName() == null || data.getFirstName().trim().isEmpty()) {
            addErrorMessage("El nombre es obligatorio");
            return false;
        }

        if (data.getLastName() == null || data.getLastName().trim().isEmpty()) {
            addErrorMessage("El apellido es obligatorio");
            return false;
        }

        String email = data.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            addErrorMessage("El formato del correo electrónico no es válido");
            return false;
        }

        return true;
    }

    // ==================== FILE UPLOAD ====================
    private String saveAvatarFile(Part part, String mimeType) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extension = "image/jpeg".equals(mimeType) ? ".jpg" : ".png";
        String fileName = "avatar_" + UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(fileName);

        try (InputStream input = part.getInputStream()) {
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/avatars/" + fileName;
    }

    // ==================== URL ====================
    public void initializeFromUrl() {
        if ("EDIT".equals(modeFromUrl)) {
            enableEditMode();
        }
    }

    // ==================== MENSAJES ====================
    private void addInfoMessage(String summary, String detail) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addErrorMessage(String message) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    private void addWarningMessage(String message) {
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", message));
    }

    // ==================== GETTERS Y SETTERS ====================
    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public Tuser getEditingUser() {
        return editingUser;
    }

    public void setEditingUser(Tuser editingUser) {
        this.editingUser = editingUser;
    }

    public Part getUploadedAvatar() {
        return uploadedAvatar;
    }

    public void setUploadedAvatar(Part uploadedAvatar) {
        this.uploadedAvatar = uploadedAvatar;
    }

    public String getModeFromUrl() {
        return modeFromUrl;
    }

    public void setModeFromUrl(String modeFromUrl) {
        this.modeFromUrl = modeFromUrl;
    }
}
