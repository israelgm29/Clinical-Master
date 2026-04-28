package com.mycompany.hospitalgeneral.services;

import com.mycompany.hospitalgeneral.websocket.NotificationMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.logging.Logger;

/**
 * Servicio de push usando PushContext nativo de Jakarta Faces 4.1.
 *
 * Un solo canal "notifications" para todos los roles. El filtrado por
 * usuario/broadcast lo maneja PushContext internamente:
 *
 * send(msg, userId) → solo al usuario con ese ID send(msg) → broadcast a todos
 * los conectados al canal
 *
 * El f:websocket en el navbar usa scope="session" para evitar conflictos de
 * registro entre páginas.
 */
@Named
@ApplicationScoped
public class NotificationPushService {

    private static final Logger LOG
            = Logger.getLogger(NotificationPushService.class.getName());

    // ✅ Un solo canal para todos los roles
    @Inject
    @Push(channel = "notifications")
    private PushContext notificationsChannel;

    /**
     * Envía notificación a un médico específico por su userId. Solo recibe la
     * notificación ese usuario.
     *
     * @param userId ID del Tuser del médico
     * @param msg Mensaje a enviar
     */
    public void notifyMedic(Integer userId, NotificationMessage msg) {
        send(msg, String.valueOf(userId), "médico userId=" + userId);
    }

    /**
     * Notifica a un admin específico por userId.
     *
     * @param userId ID del Tuser del admin
     * @param msg Mensaje a enviar
     */
    public void notifyAdmin(Integer userId, NotificationMessage msg) {
        send(msg, String.valueOf(userId), "admin userId=" + userId);
    }

    /**
     * Broadcast a todos los usuarios conectados al canal. Usado para notificar
     * a todos los admins o toda enfermería.
     *
     * @param msg Mensaje a enviar
     */
    public void notifyAdmins(NotificationMessage msg) {
        broadcastAll(msg, "todos los admins");
    }

    /**
     * Broadcast a toda enfermería conectada.
     *
     * @param msg Mensaje a enviar
     */
    public void notifyNurses(NotificationMessage msg) {
        broadcastAll(msg, "toda enfermería");
    }

    // ── Privados ─────────────────────────────────────────────────────
    private void send(NotificationMessage msg, String userId, String target) {
        try {
            notificationsChannel.send(msg.toJson(), userId);
            LOG.fine("Notificación enviada a " + target);
        } catch (Exception e) {
            LOG.warning("Error al notificar a " + target + ": " + e.getMessage());
        }
    }

    private void broadcastAll(NotificationMessage msg, String target) {
        try {
            notificationsChannel.send(msg.toJson());
            LOG.fine("Broadcast enviado a " + target);
        } catch (Exception e) {
            LOG.warning("Error al hacer broadcast a " + target + ": " + e.getMessage());
        }
    }
}
