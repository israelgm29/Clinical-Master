package com.mycompany.hospitalgeneral.services.interfaces;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * @author jhonatan
 */
public interface ProfileData {
    
    // ==================== DATOS BÁSICOS DE USUARIO ====================
    
    /**
     * @return Email del usuario (de Tuser.email)
     */
    String getEmail();
    void setEmail(String email);
    
    /**
     * @return Estado activo/inactivo (de Tuser.isactive)
     */
    boolean getIsactive();
    void setIsactive(boolean isactive);
    
    /**
     * @return Nombre del rol (de Role.name vía Tuser.roleid)
     */
    String getRoleName();
    
    // ==================== DATOS PERSONALES (PROFILE) ====================
    
    /**
     * @return Nombre completo formateado (firstname + lastname)
     */
    String getFullName();
    
    /**
     * @return Primer nombre (de Profile.firstname)
     */
    String getFirstName();
    void setFirstName(String firstName);
    
    /**
     * @return Apellido (de Profile.lastname)
     */
    String getLastName();
    void setLastName(String lastName);
    
    /**
     * @return DNI o documento de identidad (de Profile.dni)
     */
    String getDni();
    void setDni(String dni);
    
    /**
     * @return Pasaporte (de Profile.passport) - Puede ser null
     */
    String getPassport();
    void setPassport(String passport);
    
    /**
     * @return Teléfono fijo (de Profile.telephone)
     */
    String getTelephone();
    void setTelephone(String telephone);
    
    /**
     * @return Teléfono móvil (de Profile.mobile)
     */
    String getMobile();
    void setMobile(String mobile);
    
    /**
     * @return Dirección física (de Profile.address)
     */
    String getAddress();
    void setAddress(String address);
    
    /**
     * @return Ruta o nombre de la imagen de perfil (de Profile.image)
     */
    String getImageUrl();
    void setImageUrl(String imageUrl);
    
    // ==================== DATOS ESPECÍFICOS DE MÉDICO ====================
    
    /**
     * @return true si el usuario tiene rol "Médico" y datos de Medic disponibles
     */
    boolean isMedic();
    
    /**
     * @return Número de registro profesional / colegiatura (de Medic.regprofessional)
     *         Retorna null o String vacío si no es médico.
     */
    String getProfessionalId();
    void setProfessionalId(String professionalId);
    
    /**
     * @return Lista de nombres de especialidades médicas.
     *         Retorna lista vacía si no es médico o no tiene especialidades.
     */
    List<String> getSpecialties();
    
    // ==================== DATOS DE AUDITORÍA (SOLO LECTURA) ====================
    
    /**
     * @return Fecha de creación del registro (de Tuser.createdat)
     */
    LocalDateTime getCreatedAt();
    
    /**
     * @return Fecha de última edición (de Tuser.editedat o Profile.editedat)
     */
    LocalDateTime getUpdatedAt();
    
    // ==================== MÉTODOS DE CONVENIENCIA ====================
    
    /**
     * @return Iniciales para el avatar (primera letra del nombre)
     */
    default String getInitials() {
        String name = getFirstName();
        if (name == null || name.isEmpty()) {
            return "U";
        }
        return name.substring(0, 1).toUpperCase();
    }
    
    /**
     * @return Nombre completo para mostrar en el header
     */
    default String getDisplayName() {
        String firstName = getFirstName();
        String lastName = getLastName();
        
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return getEmail();
    }
    
    /**
     * @return true si la imagen de perfil está configurada
     */
    default boolean hasProfileImage() {
        String img = getImageUrl();
        return img != null && !img.isEmpty();
    }
}