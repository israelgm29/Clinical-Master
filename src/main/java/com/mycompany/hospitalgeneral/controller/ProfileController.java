package com.mycompany.hospitalgeneral.controller;

import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.TuserService;
import com.mycompany.hospitalgeneral.services.interfaces.ProfileData;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Controlador para la gestión del perfil de usuario. Maneja tanto la vista como
 * la edición del perfil.
 *
 * @author jhonatan
 */
@Named
@RequestScoped
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
    /**
     * Activa el modo edición. Crea una copia de trabajo del usuario en sesión.
     */
    public void enableEditMode() {
        Tuser currentUser = session.getUser();
        if (currentUser != null) {
            // Usamos el mismo objeto; JPA se encargará del merge
            this.editingUser = currentUser;
            this.editMode = true;
        } else {
            addErrorMessage("No se pudo cargar el usuario para edición");
        }
    }

    /**
     * Cancela la edición y vuelve al modo vista.
     */
    public void cancelEdit() {
        this.editingUser = null;
        this.editMode = false;
        this.uploadedAvatar = null;

        // Refrescar usuario de la base de datos para descartar cambios no guardados
        if (session.getUser() != null) {
            Tuser refreshed = tuserService.findById(session.getUser().getId());
            session.setUser(refreshed);
        }
    }

    /**
     * Guarda los cambios del perfil.
     *
     * @return null para permanecer en la misma página
     */
    public String saveProfile() {
        try {
            if (editingUser == null) {
                addErrorMessage("No hay datos para guardar");
                return null;
            }

            // Validaciones básicas
            if (!validateProfileData(editingUser)) {
                return null;
            }

            // Persistir cambios
            Tuser saved = tuserService.update(editingUser);

            // Actualizar el objeto en sesión
            session.setUser(saved);

            addInfoMessage("Perfil actualizado",
                    "Los cambios se guardaron correctamente");

            // Salir del modo edición
            cancelEdit();

        } catch (Exception e) {
            addErrorMessage("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Procesa la subida del avatar.
     *
     * public void uploadAvatar() { try { if (uploadedAvatar == null ||
     * uploadedAvatar.getSize() <= 0) {
     * addWarningMessage("Seleccione una imagen válida");
     * return;
     * }
     *
     * // Validar tamaño
     * if (uploadedAvatar.getSize() > MAX_FILE_SIZE) { addErrorMessage("La
     * imagen excede los 2MB permitidos"); return; }
     *
     * // Validar tipo de contenido real String mimeType =
     * validateImageContent(uploadedAvatar); if (mimeType == null) {
     * addErrorMessage("El archivo no es una imagen válida (JPG o PNG)");
     * return; }
     *
     * // Guardar archivo String savedPath = saveAvatarFile(uploadedAvatar,
     * mimeType);
     *
     * // Actualizar la URL en el usuario en edición if (editingUser != null) {
     * editingUser.setImageUrl(savedPath); }
     *
     * addInfoMessage("Avatar actualizado", "La imagen se cargó correctamente");
     *
     * } catch (Exception e) { addErrorMessage("Error al subir la imagen: " +
     * e.getMessage()); e.printStackTrace(); } }
    *
     */
    /**
     * Navega a la página de cambio de contraseña.
     */
    public String goToChangePassword() {
        return "/views/shared/change-password.xhtml?faces-redirect=true";
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================
    /**
     * Valida los datos del perfil antes de guardar.
     */
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

        // Validar formato de email
        String email = data.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            addErrorMessage("El formato del correo electrónico no es válido");
            return false;
        }

        return true;
    }

    /**
     * Valida que el archivo subido sea realmente una imagen.
     *
     * @return MIME type si es válido, null si no lo es.
     *
     * private String validateImageContent(Part part) { try (InputStream input =
     * part.getInputStream()) {
     *
     * String mimeType = tika.detect(input);
     *
     * if ("image/jpeg".equals(mimeType) || "image/png".equals(mimeType)) {
     * return mimeType; } } catch (IOException e) { e.printStackTrace(); }
     * return null; }
     *
     */
    /**
     * Guarda el archivo de avatar en el sistema de archivos.
     */
    private String saveAvatarFile(Part part, String mimeType) throws IOException {
        // Crear directorio si no existe
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único
        String extension = "image/jpeg".equals(mimeType) ? ".jpg" : ".png";
        String fileName = "avatar_" + UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(fileName);

        // Guardar archivo
        try (InputStream input = part.getInputStream()) {
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Retornar ruta relativa para acceso web
        return "/uploads/avatars/" + fileName;
    }

    public void initializeFromUrl() {
        if ("EDIT".equals(modeFromUrl)) {
            enableEditMode();
        }
    }

    public String getModeFromUrl() {
        return modeFromUrl;
    }

    public void setModeFromUrl(String modeFromUrl) {
        this.modeFromUrl = modeFromUrl;
    }

    // ==================== MÉTODOS AUXILIARES PARA MENSAJES ====================
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
}
