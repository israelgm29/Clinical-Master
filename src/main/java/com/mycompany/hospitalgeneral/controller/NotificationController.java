package com.mycompany.hospitalgeneral.controller;

import com.mycompany.hospitalgeneral.model.Notification;
import com.mycompany.hospitalgeneral.services.NotificationService;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Controller del navbar de notificaciones.
 *
 * @ViewScoped porque vive mientras el usuario está en la vista.
 *
 * El p:socket del frontend llama a refreshNotifications() cada vez que llega un
 * mensaje WebSocket.
 */
@Named
@ViewScoped
public class NotificationController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserSession userSession;

    private List<Notification> notifications;
    private long unreadCount;

    @PostConstruct
    public void init() {
        loadNotifications();
    }

    public void loadNotifications() {
        if (userSession == null || userSession.getUser() == null) {
            return;
        }
        Integer userId = userSession.getUser().getId();
        notifications = notificationService.findByUser(userId);
        unreadCount = notificationService.countUnread(userId);
    }

    /**
     * Llamado por el listener del p:socket cuando llega un mensaje. Recarga las
     * notificaciones en tiempo real.
     */
    public void refreshNotifications() {
        loadNotifications();
    }

    /**
     * Marca todas como leídas — llamado al abrir el dropdown.
     */
    public void markAllRead() {
        if (userSession == null || userSession.getUser() == null) {
            return;
        }
        notificationService.markAllRead(userSession.getUser().getId());
        loadNotifications();
    }

    /**
     * Marca una notificación específica como leída.
     * @param notification
     */
    public void markRead(Notification notification) {
        notificationService.markRead(notification.getId());
        loadNotifications();
    }

    // ── Getters ──────────────────────────────────────────────────────
    public List<Notification> getNotifications() {
        return notifications;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public boolean isHasUnread() {
        return unreadCount > 0;
    }
}
