package com.mycompany.hospitalgeneral.session;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

/**
 * Bean que responde al ping de renovación de sesión desde el frontend.
 * Al ser @RequestScoped, cada llamada simplemente toca la sesión HTTP
 * y renueva su timestamp de último acceso automáticamente.
 *
 * Usado por el p:remoteCommand "keepAliveCommand" en session-warning.xhtml
 */
@Named
@RequestScoped
public class SessionKeepAlive {

    /**
     * Método invocado por el p:remoteCommand del dialog de advertencia.
     * No necesita hacer nada explícito: el simple hecho de recibir
     * un request JSF renueva el lastAccessedTime de la HttpSession.
     */
    public void ping() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) return;

        HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
        if (session != null) {
            // Acceder a la sesión renueva su lastAccessedTime
            // Solo logueamos si está en modo debug
            System.out.println("[SessionKeepAlive] Sesión renovada: " + session.getId()
                + " | Máx. inactividad: " + session.getMaxInactiveInterval() + "s");
        }
    }
}