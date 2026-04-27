package com.mycompany.hospitalgeneral.services;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());
    private static final String APP_NAME = "Hospital General";
    private static final String APP_URL = "http://localhost:8080/Clinical-Master";

    @Resource(name = "mail/hospitalGeneral")
    private Session mailSession;

    public void enviarCorreo(String to, String subject, String html) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(mailSession.getProperty("mail.from"), APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            LOG.info(() -> {
                return "[EmailService] Enviado a: " + to;
            });
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "[EmailService] Error enviando a: " + to, e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    public void sendVerificationEmail(String to, String fullName, String token) {
        String link = APP_URL + "/activar?token=" + token;
        String html = buildHeader()
                + "<h2 style='color:#1e293b;'>Activa tu cuenta</h2>"
                + "<p style='color:#475569;'>Hola <strong>" + fullName + "</strong>,</p>"
                + "<p style='color:#475569;'>Tu cuenta ha sido creada en " + APP_NAME + ". Haz clic para activarla:</p>"
                + buildButton(link, "Activar mi cuenta", "#0b3c5d")
                + "<p style='color:#94a3b8;font-size:13px;'>O copia este enlace: <a href='" + link + "'>" + link + "</a></p>"
                + "<hr style='border:none;border-top:1px solid #e0e0e0;margin:24px 0;'/>"
                + "<p style='color:#94a3b8;font-size:12px;'>Este enlace expira en 24 horas.</p>"
                + buildFooter();
        enviarCorreo(to, "Activa tu cuenta — " + APP_NAME, html);
    }

    public void sendPasswordReset(String to, String fullName, String tempPassword) {
        String html = buildHeader()
                + "<h2 style='color:#1e293b;'>Restablecimiento de contraseña</h2>"
                + "<p style='color:#475569;'>Hola <strong>" + fullName + "</strong>,</p>"
                + "<p style='color:#475569;'>Un administrador ha reseteado tu contraseña. Usa esta clave temporal:</p>"
                + "<div style='background:#f1f5f9;border:2px dashed #0b3c5d;border-radius:8px;padding:20px;text-align:center;margin:24px 0;'>"
                + "<span style='font-size:26px;font-weight:700;color:#0b3c5d;letter-spacing:4px;font-family:monospace;'>" + tempPassword + "</span>"
                + "</div>"
                + "<p style='color:#475569;'>Cambia tu contraseña desde tu perfil lo antes posible.</p>"
                + buildButton(APP_URL, "Ir al sistema", "#2a9d8f")
                + "<hr style='border:none;border-top:1px solid #e0e0e0;margin:24px 0;'/>"
                + "<p style='color:#94a3b8;font-size:12px;'>Si no solicitaste este cambio, contacta al administrador.</p>"
                + buildFooter();
        enviarCorreo(to, "Tu contraseña temporal — " + APP_NAME, html);
    }

    private String buildHeader() {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>"
                + "<div style='background:#0b3c5d;padding:24px;border-radius:8px 8px 0 0;text-align:center;'>"
                + "<h1 style='color:white;margin:0;font-size:22px;'>" + APP_NAME + "</h1></div>"
                + "<div style='background:#ffffff;padding:32px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px;'>";
    }

    private String buildButton(String url, String text, String color) {
        return "<div style='text-align:center;margin:28px 0;'>"
                + "<a href='" + url + "' style='background:" + color + ";color:white;padding:14px 32px;"
                + "border-radius:8px;text-decoration:none;font-weight:600;font-size:15px;'>" + text + "</a></div>";
    }

    private String buildFooter() {
        return "</div></body></html>";
    }
}
