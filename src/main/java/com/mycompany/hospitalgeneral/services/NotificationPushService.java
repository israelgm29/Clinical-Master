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
 * Reemplaza al NotificationEndpoint.java anterior.
 *
 * Los canales deben coincidir con los declarados en f:websocket del navbar:
 *   - medicChannel  → channel="medic_#{userId}"
 *   - adminChannel  → channel="admin_#{userId}"
 *   - nurseChannel  → channel="nurse_#{userId}"
 */
@Named
@ApplicationScoped
public class NotificationPushService {

    private static final Logger LOG =
            Logger.getLogger(NotificationPushService.class.getName());

    @Inject
    @Push(channel = "medicChannel")
    private PushContext medicChannel;

    @Inject
    @Push(channel = "adminChannel")
    private PushContext adminChannel;

    @Inject
    @Push(channel = "nurseChannel")
    private PushContext nurseChannel;

    /**
     * Envía al médico específico por su userId.
     * El f:websocket del médico debe tener scope de usuario.
     *
     * @param userId ID del Tuser del médico
     * @param msg    Mensaje a enviar
     */
    public void notifyMedic(Integer userId, NotificationMessage msg) {
        try {
            medicChannel.send(msg.toJson(), String.valueOf(userId));
            LOG.fine("Notificación enviada al médico userId=" + userId);
        } catch (Exception e) {
            LOG.warning("Error al notificar médico userId=" + userId + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast a todos los admins conectados.
     *
     * @param msg Mensaje a enviar
     */
    public void notifyAdmins(NotificationMessage msg) {
        try {
            adminChannel.send(msg.toJson());
            LOG.fine("Notificación broadcast a admins");
        } catch (Exception e) {
            LOG.warning("Error al notificar admins: " + e.getMessage());
        }
    }

    /**
     * Notifica a un admin específico por userId.
     *
     * @param userId ID del Tuser del admin
     * @param msg    Mensaje a enviar
     */
    public void notifyAdmin(Integer userId, NotificationMessage msg) {
        try {
            adminChannel.send(msg.toJson(), String.valueOf(userId));
        } catch (Exception e) {
            LOG.warning("Error al notificar admin userId=" + userId + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast a toda enfermería conectada.
     *
     * @param msg Mensaje a enviar
     */
    public void notifyNurses(NotificationMessage msg) {
        try {
            nurseChannel.send(msg.toJson());
            LOG.fine("Notificación broadcast a enfermería");
        } catch (Exception e) {
            LOG.warning("Error al notificar enfermería: " + e.getMessage());
        }
    }
}