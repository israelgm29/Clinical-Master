package com.mycompany.hospitalgeneral.websocket;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO que representa un mensaje de notificación enviado por WebSocket. Se
 * serializa a JSON manualmente para evitar dependencias externas.
 *
 * Tipos disponibles: - PATIENT_ASSIGNED → Médico recibe nuevo paciente -
 * ACCOUNT_ACTIVATED → Admin: usuario activó su cuenta - PASSWORD_CHANGED →
 * Admin: usuario cambió su contraseña - CONSULTATION_STATUS → Enfermería:
 * estado de consulta actualizado
 */
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Campos ───────────────────────────────────────────────────────
    private String type;        // tipo de notificación
    private String title;       // título corto
    private String message;     // mensaje descriptivo
    private String icon;        // ícono PrimeIcons (pi pi-xxx)
    private String severity;    // info | warn | error | success
    private Integer targetUserId; // usuario destino (null = broadcast)
    private LocalDateTime createdAt;

    // ── Constructor completo ─────────────────────────────────────────
    public NotificationMessage(String type, String title, String message,
            String icon, String severity, Integer targetUserId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.severity = severity;
        this.targetUserId = targetUserId;
        this.createdAt = LocalDateTime.now();
    }

    // ── Fábricas estáticas por tipo ──────────────────────────────────
    public static NotificationMessage patientAssigned(String patientName,
            String nurseName,
            Integer medicUserId) {
        return new NotificationMessage(
                "PATIENT_ASSIGNED",
                "Nuevo paciente asignado",
                patientName + " fue asignado por " + nurseName,
                "pi pi-user-plus",
                "info",
                medicUserId
        );
    }

    public static NotificationMessage accountActivated(String userName) {
        return new NotificationMessage(
                "ACCOUNT_ACTIVATED",
                "Cuenta activada",
                userName + " ha activado su cuenta",
                "pi pi-check-circle",
                "success",
                null // broadcast a todos los admins
        );
    }

    public static NotificationMessage passwordChanged(String userName) {
        return new NotificationMessage(
                "PASSWORD_CHANGED",
                "Contraseña cambiada",
                userName + " cambió su contraseña",
                "pi pi-lock",
                "warn",
                null // broadcast a todos los admins
        );
    }

    public static NotificationMessage consultationStatus(String medicName,
            String patientName,
            String status) {
        return new NotificationMessage(
                "CONSULTATION_STATUS",
                "Estado de consulta",
                medicName + " - " + patientName + ": " + status,
                "pi pi-heart",
                "info",
                null // broadcast a enfermería
        );
    }

    // ── Serialización manual a JSON ──────────────────────────────────
    // Evita dependencia de Jackson/Gson en el módulo WebSocket
    public String toJson() {
        return String.format(
                "{\"type\":\"%s\",\"title\":\"%s\",\"message\":\"%s\","
                + "\"icon\":\"%s\",\"severity\":\"%s\",\"createdAt\":\"%s\"}",
                escape(type),
                escape(title),
                escape(message),
                escape(icon),
                escape(severity),
                createdAt.format(FORMATTER)
        );
    }

    /**
     * Escapa caracteres especiales para JSON seguro
     */
    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // ── Getters ──────────────────────────────────────────────────────
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getIcon() {
        return icon;
    }

    public String getSeverity() {
        return severity;
    }

    public Integer getTargetUserId() {
        return targetUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
