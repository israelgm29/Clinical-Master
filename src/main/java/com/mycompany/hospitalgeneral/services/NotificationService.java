package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.model.Notification;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.websocket.NotificationMessage;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class NotificationService {

    private static final Logger LOG =
            Logger.getLogger(NotificationService.class.getName());

    private static final int MAX_NOTIFICATIONS = 20;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private NotificationPushService pushService;

    // ════════════════════════════════════════════════════════════════
    // MÉTODOS DE ALTO NIVEL
    // ════════════════════════════════════════════════════════════════

    @Transactional
    public void notifyMedic(Tuser medicUser, String patientName, String nurseName) {
        NotificationMessage msg = NotificationMessage.patientAssigned(
                patientName, nurseName, medicUser.getId());
        persist(medicUser, msg);
        pushService.notifyMedic(medicUser.getId(), msg);
    }

    @Transactional
    public void notifyAdminsAccountActivated(List<Tuser> adminUsers, String userName) {
        NotificationMessage msg = NotificationMessage.accountActivated(userName);
        persistToAll(adminUsers, msg);
        pushService.notifyAdmins(msg);
    }

    @Transactional
    public void notifyAdminsPasswordChanged(List<Tuser> adminUsers, String userName) {
        NotificationMessage msg = NotificationMessage.passwordChanged(userName);
        persistToAll(adminUsers, msg);
        pushService.notifyAdmins(msg);
    }

    @Transactional
    public void notifyNursesConsultationStatus(List<Tuser> nurseUsers,
                                               String medicName,
                                               String patientName,
                                               String status) {
        NotificationMessage msg = NotificationMessage.consultationStatus(
                medicName, patientName, status);
        persistToAll(nurseUsers, msg);
        pushService.notifyNurses(msg);
    }

    // ════════════════════════════════════════════════════════════════
    // CONSULTAS
    // ════════════════════════════════════════════════════════════════

    public List<Notification> findByUser(Integer userId) {
        return em.createNamedQuery("Notification.findByUser", Notification.class)
                 .setParameter("userId", userId)
                 .setMaxResults(MAX_NOTIFICATIONS)
                 .getResultList();
    }

    public long countUnread(Integer userId) {
        return em.createNamedQuery("Notification.countUnread", Long.class)
                 .setParameter("userId", userId)
                 .getSingleResult();
    }

    @Transactional
    public void markAllRead(Integer userId) {
        em.createNamedQuery("Notification.markAllRead")
          .setParameter("userId", userId)
          .executeUpdate();
    }

    @Transactional
    public void markRead(Integer notificationId) {
        Notification n = em.find(Notification.class, notificationId);
        if (n != null) {
            n.setIsread(true);
            em.merge(n);
        }
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVADOS
    // ════════════════════════════════════════════════════════════════

    private void persistToAll(List<Tuser> users, NotificationMessage msg) {
        for (Tuser user : users) {
            try {
                persist(user, msg);
            } catch (Exception e) {
                LOG.log(Level.WARNING,
                        "Error al persistir notificación para userId=" + user.getId(), e);
            }
        }
    }

    private void persist(Tuser user, NotificationMessage msg) {
        Notification n = new Notification(
                user, msg.getTitle(), msg.getMessage(),
                msg.getType(), msg.getIcon());
        em.persist(n);
        em.flush();

        em.createNativeQuery(
            "DELETE FROM notification WHERE userid = :userId " +
            "AND id NOT IN (" +
            "  SELECT id FROM notification " +
            "  WHERE userid = :userId " +
            "  ORDER BY createdat DESC LIMIT " + MAX_NOTIFICATIONS +
            ")"
        ).setParameter("userId", user.getId()).executeUpdate();
    }
}