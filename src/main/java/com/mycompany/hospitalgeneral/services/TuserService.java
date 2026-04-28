package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Tuser;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Stateless
public class TuserService {

    @Inject
    private EmailService emailService;

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

    public List<Tuser> findAll() {
        return em.createNamedQuery("Tuser.findAll", Tuser.class)
                .getResultList();
    }

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

    /**
     * ✅ Busca todos los usuarios activos de un rol específico.
     * Usado por NotificationService para broadcast a admins/enfermería.
     *
     * @param roleName Nombre exacto del rol: "Administrador", "Enfermería", etc.
     */
    public List<Tuser> findAllByRole(String roleName) {
        return em.createQuery(
                "SELECT t FROM Tuser t "
                + "WHERE t.roleid.name = :roleName "
                + "AND t.isactive = true "
                + "AND (t.deleted = false OR t.deleted IS NULL)",
                Tuser.class)
                .setParameter("roleName", roleName)
                .getResultList();
    }

    /** ✅ Atajo para admins activos */
    public List<Tuser> findAllAdmins() {
        return findAllByRole("Administrador");
    }

    /** ✅ Atajo para enfermeros activos */
    public List<Tuser> findAllNurses() {
        return findAllByRole("Enfermería");
    }

    /**
     * ✅ Busca un usuario por su token de verificación.
     * Usado por ActivateAccountServlet para obtener el nombre antes de verificar.
     */
    public Tuser findByVerificationToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return em.createQuery(
                    "SELECT t FROM Tuser t WHERE t.verificationtoken = :token",
                    Tuser.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
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

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
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

    @Transactional
    public void sendVerificationEmail(Tuser user) {
        String token = UUID.randomUUID().toString();
        user.setVerificationtoken(token);
        user.setVerificationTokenExpiration(LocalDateTime.now().plusHours(24));
        em.merge(user);

        emailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                token
        );
    }

    @Transactional
    public String resetPasswordAndNotify(Tuser user) {
        String tempPassword = "HG-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        String hashed = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
        user.setPassword(hashed);
        user.setPassresettoken(UUID.randomUUID().toString());
        user.setEditedat(LocalDateTime.now());
        em.merge(user);
        System.out.println("[EMAIL] Reset password → " + user.getEmail()
                + " | temp: " + tempPassword);
        return tempPassword;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        try {
            Tuser user = em.createQuery(
                    "SELECT t FROM Tuser t WHERE t.verificationtoken = :token",
                    Tuser.class)
                    .setParameter("token", token)
                    .getSingleResult();

            if (user.getVerificationTokenExpiration() == null
                    || user.getVerificationTokenExpiration().isBefore(LocalDateTime.now())) {
                return false;
            }

            user.setEmailverified(true);
            user.setIsactive(true);
            user.setVerificationtoken(null);
            user.setVerificationTokenExpiration(null);
            em.merge(user);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}