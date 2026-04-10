package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Role;
import com.mycompany.hospitalgeneral.model.Tuser;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Stateless
public class MedicService {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private RoleService roleService;

    /**
     * Busca todos los médicos activos con sus especialidades cargadas
     */
    public List<Medic> findAllActive() {
        return em.createQuery(
                "SELECT DISTINCT m FROM Medic m "
                + "LEFT JOIN FETCH m.specialistList s "
                + "LEFT JOIN FETCH s.medicalspecialtyid "
                + "WHERE m.deleted = false "
                + "ORDER BY m.lastname ASC", Medic.class)
                .getResultList();
    }

    /**
     * Busca todos los médicos (incluyendo inactivos si necesitas)
     */
    public List<Medic> findAll() {
        return em.createQuery(
                "SELECT m FROM Medic m ORDER BY m.lastname", Medic.class)
                .getResultList();
    }

    /**
     * Busca por ID
     */
    public Medic findById(Integer id) {
        return em.find(Medic.class, id);
    }

    /**
     * Busca por DNI
     */
    public Medic findByDni(String dni) {
        try {
            return em.createQuery(
                    "SELECT m FROM Medic m WHERE m.dni = :dni AND m.deleted = false", 
                    Medic.class)
                    .setParameter("dni", dni)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda o actualiza un médico.
     * Si es nuevo, crea automáticamente el usuario del sistema (Tuser)
     */
    @Transactional
    public void save(Medic medic, Integer currentUserId) {
        // Validar que tenga al menos un documento
        if ((medic.getDni() == null || medic.getDni().trim().isEmpty()) 
            && (medic.getPassport() == null || medic.getPassport().trim().isEmpty())) {
            throw new IllegalArgumentException("Debe ingresar al menos un documento (Cédula o Pasaporte)");
        }

        // Limpiar documentos vacíos
        if (medic.getDni() != null && medic.getDni().trim().isEmpty()) {
            medic.setDni(null);
        }
        if (medic.getPassport() != null && medic.getPassport().trim().isEmpty()) {
            medic.setPassport(null);
        }

        boolean isNew = (medic.getId() == null);

        if (isNew) {
            // ===== CREAR NUEVO MÉDICO =====
            
            // 1. Crear usuario del sistema automáticamente
            Tuser user = createSystemUser(medic, currentUserId);
            
            // 2. Asociar usuario al médico
            medic.setUserid(user);
            
            // 3. Setear auditoría
            medic.setCreatedby(currentUserId);
            // createdat se setea automáticamente en @PrePersist
            
            // 4. Guardar médico
            em.persist(medic);
            
        } else {
            // ===== ACTUALIZAR MÉDICO EXISTENTE =====
            
            // Actualizar datos de auditoría
            medic.setEditedby(currentUserId);
            // editedat se setea automáticamente en @PreUpdate
            
            // Si tiene usuario asociado, actualizar email del usuario si cambió
            if (medic.getUserid() != null && medic.getEmail() != null) {
                Tuser user = medic.getUserid();
                user.setEditedby(currentUserId);
                // editedat se actualiza en @PreUpdate
                
                // Solo actualizar email si cambió
                if (!medic.getEmail().equals(user.getEmail())) {
                    user.setEmail(medic.getEmail());
                    em.merge(user);
                }
            }
            
            em.merge(medic);
        }
    }

    /**
     * Crea automáticamente el usuario del sistema para un nuevo médico
     */
    private Tuser createSystemUser(Medic medic, Integer createdBy) {
        Tuser user = new Tuser();
        
        // Datos básicos
        user.setEmail(medic.getEmail());
        user.setIsactive(true);
        user.setEmailverified(false); // Debe verificar email
        user.setCreatedby(createdBy);
        user.setDeleted(false);
        // createdat se setea automáticamente en @PrePersist
        
        // Generar contraseña temporal segura
        String tempPassword = generateSecurePassword();
        // Encriptar con BCrypt (placeholder - implementa con tu librería)
        user.setPassword(encryptPassword(tempPassword));
        
        // Asignar rol de MÉDICO
        Role medicRole = roleService.findMedicRole();
        if (medicRole == null) {
            throw new IllegalStateException("No existe el rol MÉDICO en el sistema");
        }
        user.setRoleid(medicRole);
        
        // TODO: Asignar un perfil por defecto si es necesario
        // user.setProfileid(profileService.findDefaultProfile());
        
        // Guardar usuario
        em.persist(user);
        em.flush(); // Forzar generación del ID
        
        // TODO: Enviar email con credenciales temporales
        // sendCredentialsEmail(medic.getEmail(), tempPassword);
        
        return user;
    }

    /**
     * Genera contraseña temporal segura de 10 caracteres
     */
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 10);
    }

    /**
     * Encripta la contraseña (implementa con tu método de encriptación)
     */
    private String encryptPassword(String plainPassword) {
        // TODO: Implementar con BCrypt o tu método de encriptación
        // Ejemplo con BCrypt: return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
        
        // Placeholder temporal - ¡CAMBIAR ESTO!
        return "$2a$10$" + plainPassword;
    }

    /**
     * Eliminación lógica (soft delete)
     */
    @Transactional
    public void delete(Integer medicId, Integer deletedBy) {
        Medic medic = findById(medicId);
        if (medic != null) {
            medic.setDeleted(true);
            medic.setDeletedby(deletedBy);
            // deletedat se setea automáticamente en @PreUpdate
            
            // Desactivar usuario asociado
            if (medic.getUserid() != null) {
                Tuser user = medic.getUserid();
                user.setIsactive(false);
                user.setDeleted(true);
                user.setDeletedby(deletedBy);
                // deletedat se actualiza en @PreUpdate
                em.merge(user);
            }
            
            em.merge(medic);
        }
    }
    
    /**
     * Verifica si existe un médico con el mismo DNI o registro profesional
     */
    public boolean existsByDniOrRegProfessional(String dni, String regProfessional, Integer excludeId) {
        String jpql = "SELECT COUNT(m) FROM Medic m WHERE (m.dni = :dni OR m.regprofessional = :regProfessional) "
                    + "AND m.deleted = false";
        
        if (excludeId != null) {
            jpql += " AND m.id != :excludeId";
        }
        
        var query = em.createQuery(jpql, Long.class)
                .setParameter("dni", dni)
                .setParameter("regProfessional", regProfessional);
        
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        
        return query.getSingleResult() > 0;
    }
}