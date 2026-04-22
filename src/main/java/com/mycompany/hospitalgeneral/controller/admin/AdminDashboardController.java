package com.mycompany.hospitalgeneral.controller.admin;

import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.TuserService;
import com.mycompany.hospitalgeneral.services.RoleService;
import com.mycompany.hospitalgeneral.services.AuditLogService;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para el Dashboard de Administración. Proporciona métricas y datos
 * para la vista del administrador.
 *
 * @author jhonatan
 */
@Named
@RequestScoped
public class AdminDashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== INYECCIONES ====================
    @Inject
    private TuserService tuserService;

    @Inject
    private RoleService roleService;

    @Inject
    private AuditLogService auditLogService;

    @Inject
    private UserSession userSession;

    @Inject
    private FacesContext facesContext;

    // ==================== MÉTRICAS ====================
    private int pendingRequests;
    private int activeUsers;
    private int totalUsers;
    private int totalRoles;
    private int activeRoles;
    private int todayLogs;
    private int errorLogs;

    private List<PendingRegistrationDTO> pendingRegistrationList;
    private List<RecentActivityDTO> recentActivity;

    // ==================== INICIALIZACIÓN ====================
    @PostConstruct
    public void init() {
        loadMetrics();
        loadPendingRegistrations();
        loadRecentActivity();
    }

    // ==================== CARGA DE MÉTRICAS ====================
    private void loadMetrics() {
        // Estas son consultas de ejemplo - Ajústalas a tu modelo real
        pendingRequests = tuserService.countPendingRegistrations();
        activeUsers = tuserService.countActiveUsers();
        totalUsers = tuserService.countAll();
        todayLogs = auditLogService.countTodayLogs();
        errorLogs = auditLogService.countTodayErrors();
    }

    private void loadPendingRegistrations() {
        List<Tuser> pendingUsers = tuserService.findPendingRegistrations();
        pendingRegistrationList = pendingUsers.stream()
                .map(PendingRegistrationDTO::new)
                .limit(3) // Solo mostrar 3 en el dashboard
                .collect(Collectors.toList());
    }

    private void loadRecentActivity() {
        // Datos de ejemplo - Reemplazar con tu AuditLogService real
        recentActivity = new ArrayList<>();

        recentActivity.add(new RecentActivityDTO(
                "approval", "check_circle",
                "Solicitud Aprobada",
                "Admin aprobó solicitud de registro de Dr. Juan Pérez",
                "Hace 15 minutos"
        ));

        recentActivity.add(new RecentActivityDTO(
                "login", "login",
                "Inicio de Sesión",
                "Usuario 'enfermera1' inició sesión desde 192.168.1.45",
                "Hace 45 minutos"
        ));

        recentActivity.add(new RecentActivityDTO(
                "config", "settings",
                "Configuración Actualizada",
                "Parámetro 'email.smtp.host' modificado por Admin",
                "Hace 2 horas"
        ));

        recentActivity.add(new RecentActivityDTO(
                "system", "backup",
                "Backup Automático",
                "Copia de seguridad completada exitosamente",
                "Hace 5 horas"
        ));

        recentActivity.add(new RecentActivityDTO(
                "login", "error",
                "Intento Fallido",
                "3 intentos fallidos de login para usuario 'admin'",
                "Hace 6 horas"
        ));
    }

    // ==================== ACCIONES ====================
    public void approveRequest(Integer requestId) {
        try {
            tuserService.approveRegistration(requestId);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Solicitud aprobada", "El usuario ha sido activado correctamente"));
            loadMetrics();
            loadPendingRegistrations();
        } catch (Exception e) {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo aprobar: " + e.getMessage()));
        }
    }

    public void rejectRequest(Integer requestId) {
        try {
            tuserService.rejectRegistration(requestId);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Solicitud rechazada", "La solicitud ha sido rechazada"));
            loadMetrics();
            loadPendingRegistrations();
        } catch (Exception e) {
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "No se pudo rechazar: " + e.getMessage()));
        }
    }

    // ==================== GETTERS ====================
    public int getPendingRequests() {
        return pendingRequests;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getTotalRoles() {
        return totalRoles;
    }

    public int getActiveRoles() {
        return activeRoles;
    }

    public int getTodayLogs() {
        return todayLogs;
    }

    public int getErrorLogs() {
        return errorLogs;
    }

    public List<PendingRegistrationDTO> getPendingRegistrationList() {
        return pendingRegistrationList;
    }

    public List<RecentActivityDTO> getRecentActivity() {
        return recentActivity;
    }

    // ==================== DTOs INTERNOS ====================
    /**
     * DTO para mostrar solicitudes pendientes en el dashboard
     */
    public static class PendingRegistrationDTO implements Serializable {

        private final Long id;
        private final String fullName;
        private final String email;
        private final String roleName;
        private final String requestDate;
        private final String initials;

        public PendingRegistrationDTO(Tuser user) {
            this.id = user.getId() != null ? user.getId().longValue() : null;
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.roleName = user.getRoleName();
            this.requestDate = user.getCreatedat() != null
                    ? user.getCreatedat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";

            // Generar iniciales
            String name = user.getFirstName();
            this.initials = (name != null && !name.isEmpty())
                    ? name.substring(0, 1).toUpperCase() : "U";
        }

        public Long getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getRoleName() {
            return roleName;
        }

        public String getRequestDate() {
            return requestDate;
        }

        public String getInitials() {
            return initials;
        }
    }

    /**
     * DTO para mostrar actividad reciente
     */
    public static class RecentActivityDTO implements Serializable {

        private final String iconClass;
        private final String icon;
        private final String title;
        private final String description;
        private final String timeAgo;

        public RecentActivityDTO(String iconClass, String icon, String title,
                String description, String timeAgo) {
            this.iconClass = iconClass;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.timeAgo = timeAgo;
        }

        public String getIconClass() {
            return iconClass;
        }

        public String getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getTimeAgo() {
            return timeAgo;
        }
    }
}
