package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Tuser;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar Tuser (usuarios del sistema).
 *
 * @author jhonatan
 */
@Stateless
public class TuserService {

    @PersistenceContext
    private EntityManager em;

    // ==================== BÚSQUEDAS ====================
    public Tuser findByEmail(String email) {
        try {
            return em.createNamedQuery("Tuser.findByEmail", Tuser.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Tuser findById(Integer id) {
        return em.find(Tuser.class, id);
    }

    /**
     * Lista todos los usuarios activos (no eliminados).
     */
    public List<Tuser> findAll() {
        return em.createNamedQuery("Tuser.findAll", Tuser.class)
                .getResultList();
    }

    /**
     * Búsqueda flexible por nombre, apellido o email.
     */
    public List<Tuser> searchFlexible(String query) {
        String like = "%" + query.toLowerCase() + "%";
        return em.createQuery(
                "SELECT t FROM Tuser t "
                + "WHERE (t.deleted = false OR t.deleted IS NULL) "
                + "AND (LOWER(t.email) LIKE :q "
                + "  OR LOWER(t.profileid.firstname) LIKE :q "
                + "  OR LOWER(t.profileid.lastname)  LIKE :q)",
                Tuser.class)
                .setParameter("q", like)
                .getResultList();
    }

    // ==================== CONTADORES ====================
    public int countPendingRegistrations() {
        return em.createQuery(
                "SELECT COUNT(t) FROM Tuser t "
                + "WHERE t.isactive = false AND (t.deleted = false OR t.deleted IS NULL)",
                Long.class).getSingleResult().intValue();
    }

    public int countActiveUsers() {
        return em.createQuery(
                "SELECT COUNT(t) FROM Tuser t "
                + "WHERE t.isactive = true AND (t.deleted = false OR t.deleted IS NULL)",
                Long.class).getSingleResult().intValue();
    }

    public int countAll() {
        return em.createQuery(
                "SELECT COUNT(t) FROM Tuser t "
                + "WHERE t.deleted = false OR t.deleted IS NULL",
                Long.class).getSingleResult().intValue();
    }

    // ==================== CRUD ====================
    /**
     * Crea un usuario nuevo. Hashea la contraseña con BCrypt antes de
     * persistir. El usuario queda inactivo hasta confirmar email.
     */
    @Transactional
    public void create(Tuser user, Integer createdBy) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        user.setCreatedby(createdBy);
        user.setIsactive(false);
        user.setEmailverified(false);
        user.setVerificationtoken(UUID.randomUUID().toString());
        em.persist(user);
    }

    @Transactional
    public Tuser update(Tuser user) {
        return em.merge(user);
    }

    /**
     * Activa o desactiva la cuenta.
     */
    @Transactional
    public void setActive(Integer userId, boolean active, Integer editedBy) {
        Tuser user = findById(userId);
        if (user != null) {
            user.setIsactive(active);
            user.setEditedby(editedBy);
            user.setEditedat(LocalDateTime.now());
            em.merge(user);
        }
    }

    /**
     * Soft delete: marca deleted=true, no borra de la BD.
     */
    @Transactional
    public void softDelete(Integer userId, Integer deletedBy) {
        Tuser user = findById(userId);
        if (user != null) {
            user.setDeleted(true);
            user.setDeletedat(LocalDateTime.now());
            user.setDeletedby(deletedBy);
            em.merge(user);
        }
    }

    // ==================== LOGIN ====================
    /**
     * Verifica si la contraseña en texto plano coincide con el hash almacenado.
     * Úsalo en tu LoginController en lugar de comparar strings directamente.
     */
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // ==================== APROBACIONES ====================
    public List<Tuser> findPendingRegistrations() {
        return em.createQuery(
                "SELECT t FROM Tuser t "
                + "WHERE t.isactive = false "
                + "AND (t.deleted = false OR t.deleted IS NULL) "
                + "ORDER BY t.createdat DESC",
                Tuser.class).getResultList();
    }

    @Transactional
    public void approveRegistration(Integer userId) {
        Tuser user = findById(userId);
        if (user != null) {
            user.setIsactive(true);
            user.setEditedat(LocalDateTime.now());
            em.merge(user);
        }
    }

    @Transactional
    public void rejectRegistration(Integer userId) {
        Tuser user = findById(userId);
        if (user != null) {
            user.setDeleted(true);
            user.setDeletedat(LocalDateTime.now());
            em.merge(user);
        }
    }

    // ==================== EMAIL ====================
    /**
     * Genera un token de verificación y lo persiste.
     *
     * TODO: conecta tu EmailService aquí:
     * emailService.sendVerification(user.getEmail(), token, verificationUrl);
     */
    @Transactional
    public void sendVerificationEmail(Tuser user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationtoken(token);
        em.merge(user);

        // TODO: reemplaza con tu EmailService real
        System.out.println("[EMAIL] Verificación → " + user.getEmail() + " | token: " + token);
    }

    /**
     * Genera una contraseña temporal, la hashea con BCrypt y la persiste. Envía
     * el email con la clave temporal al usuario.
     *
     * TODO: conecta tu EmailService aquí:
     * emailService.sendPasswordReset(user.getEmail(), tempPassword);
     */
    @Transactional
    public String resetPasswordAndNotify(Tuser user) {
        // Contraseña temporal legible: HG- + 8 chars en mayúsculas
        String tempPassword = "HG-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();

        // Hashear con BCrypt antes de persistir
        String hashed = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
        user.setPassword(hashed);
        user.setPassresettoken(UUID.randomUUID().toString());
        user.setEditedat(LocalDateTime.now());
        em.merge(user);

        // TODO: reemplaza con tu EmailService real
        System.out.println("[EMAIL] Reset password → " + user.getEmail()
                + " | temp: " + tempPassword);

        return tempPassword;
    }

    /**
     * Verifica el token de email y activa la cuenta. Llamar desde el endpoint
     * que el usuario visita al hacer clic en el link del correo.
     */
    @Transactional
    public boolean verifyEmail(String token) {
        try {
            Tuser user = em.createQuery(
                    "SELECT t FROM Tuser t WHERE t.verificationtoken = :token",
                    Tuser.class)
                    .setParameter("token", token)
                    .getSingleResult();

            user.setEmailverified(true);
            user.setIsactive(true);
            user.setVerificationtoken(null);
            em.merge(user);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
