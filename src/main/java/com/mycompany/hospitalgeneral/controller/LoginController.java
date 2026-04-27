package com.mycompany.hospitalgeneral.controller;

import com.mycompany.hospitalgeneral.model.Medic;
import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.MedicService;
import com.mycompany.hospitalgeneral.services.TuserService;
import com.mycompany.hospitalgeneral.session.UserSession;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.mindrot.jbcrypt.BCrypt;

@Named
@RequestScoped
public class LoginController {

    private String email;
    private String password;

    @Inject
    private TuserService tuserService;

    @Inject
    private UserSession session;

    @Inject
    private MedicService medicService;

    public String login() {
        try {
            Tuser user = tuserService.findByEmail(email);

            if (user == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Usuario o contraseña incorrectos"));
                return null;
            }

            if (!user.getIsactive()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Usuario inactivo"));
                return null;
            }

            // Validar contraseña
            if (!BCrypt.checkpw(password, user.getPassword())) {
                System.out.println(password);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Usuario o contraseña incorrectos"));
                return null;
            }

            // ✅ GUARDAR EN SESSION BEAN
            session.setUser(user);

            // ✅ Cargar entidad según rol
            String role = user.getRoleid().getName();

            if ("Médico".equals(role)) {
                Medic medic = medicService.findByUserId(user.getProfileid().getId());
                session.setMedic(medic);
            }

            // 🔁 Redirección (REFACTORIZADA: Sin el prefijo /faces)
            String redirect = "?faces-redirect=true";

            return switch (role) {
                case "Administración" -> "/views/admin/dashboard/dashboard.xhtml" + redirect;
                case "Médico" -> "/views/medic/dashboard.xhtml" + redirect;
                case "Enfermería" -> "/views/nurse/dashboard.xhtml" + redirect;
                case "Analista" -> "/views/analista/dashboard.xhtml" + redirect;
                default -> "/login.xhtml" + redirect;
            }; // Es preferible redirigir a una página de error o al login si el rol no existe

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error del sistema", "Intente más tarde"));
            e.printStackTrace();
            return null;
        }
    }

    public String logout() {
        session.clear();
        return "/login.xhtml?faces-redirect=true";
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
