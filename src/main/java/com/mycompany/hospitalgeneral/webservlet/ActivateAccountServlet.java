package com.mycompany.hospitalgeneral.webservlet;

import com.mycompany.hospitalgeneral.model.Tuser;
import com.mycompany.hospitalgeneral.services.NotificationService;
import com.mycompany.hospitalgeneral.services.TuserService;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet que procesa el enlace de activación de cuenta enviado por email. Al
 * activar correctamente, notifica a todos los admins via WebSocket.
 */
@WebServlet("/activar")
public class ActivateAccountServlet extends HttpServlet {

    @Inject
    private TuserService tuserService;

    // ✅ Inyectamos NotificationService para avisar a los admins
    @Inject
    private NotificationService notificationService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html;charset=UTF-8");

        String token = req.getParameter("token");
        String redirectUrl = req.getContextPath();

        // ✅ Buscar el usuario ANTES de verificar para tener su nombre
        Tuser activatedUser = tuserService.findByVerificationToken(token);
        boolean ok = tuserService.verifyEmail(token);

        // ✅ Si se activó correctamente, notificar a todos los admins
        if (ok && activatedUser != null) {
            try {
                List<Tuser> admins = tuserService.findAllAdmins();
                notificationService.notifyAdminsAccountActivated(
                        admins,
                        activatedUser.getFullName()
                );
            } catch (Exception e) {
                // No interrumpir el flujo si falla la notificación
                System.err.println("[ActivateAccountServlet] Error al notificar admins: "
                        + e.getMessage());
            }
        }

        String html;

        if (ok) {
            html = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'/>"
                    + "<title>Cuenta activada</title>"
                    + "<style>"
                    + "body{font-family:DM Sans,sans-serif;text-align:center;"
                    + "background:#f8fafc;display:flex;align-items:center;"
                    + "justify-content:center;min-height:100vh;margin:0;}"
                    + ".card{background:#fff;border-radius:20px;padding:48px 40px;"
                    + "box-shadow:0 8px 32px rgba(0,0,0,.1);max-width:420px;width:100%;}"
                    + ".icon{font-size:3rem;margin-bottom:16px;}"
                    + "h2{color:#16a34a;margin:0 0 12px;font-size:1.4rem;}"
                    + "p{color:#64748b;margin:0 0 8px;font-size:.92rem;}"
                    + ".count{font-weight:800;color:#0b3c5d;font-size:1.1rem;}"
                    + "a{color:#0b3c5d;font-weight:600;}"
                    + "</style>"
                    + "<script>"
                    + "var s=10;"
                    + "function tick(){"
                    + "  document.getElementById('c').innerText=s;"
                    + "  s--;"
                    + "  if(s<0)window.location.href='" + redirectUrl + "';"
                    + "}"
                    + "setInterval(tick,1000);"
                    + "</script>"
                    + "</head>"
                    + "<body><div class='card'>"
                    + "<div class='icon'>✅</div>"
                    + "<h2>¡Cuenta activada correctamente!</h2>"
                    + "<p>Ya puedes iniciar sesión en el sistema.</p>"
                    + "<p>Redirigiendo en <span id='c' class='count'>10</span> segundos...</p>"
                    + "<p><a href='" + redirectUrl + "'>Ir ahora</a></p>"
                    + "</div></body></html>";
        } else {
            html = "<!DOCTYPE html>"
                    + "<html><head><meta charset='UTF-8'/>"
                    + "<title>Error de activación</title>"
                    + "<style>"
                    + "body{font-family:DM Sans,sans-serif;text-align:center;"
                    + "background:#f8fafc;display:flex;align-items:center;"
                    + "justify-content:center;min-height:100vh;margin:0;}"
                    + ".card{background:#fff;border-radius:20px;padding:48px 40px;"
                    + "box-shadow:0 8px 32px rgba(0,0,0,.1);max-width:420px;width:100%;}"
                    + ".icon{font-size:3rem;margin-bottom:16px;}"
                    + "h2{color:#dc2626;margin:0 0 12px;font-size:1.4rem;}"
                    + "p{color:#64748b;margin:0 0 8px;font-size:.92rem;}"
                    + ".count{font-weight:800;color:#0b3c5d;font-size:1.1rem;}"
                    + "a{color:#0b3c5d;font-weight:600;}"
                    + "</style>"
                    + "<script>"
                    + "var s=10;"
                    + "function tick(){"
                    + "  document.getElementById('c').innerText=s;"
                    + "  s--;"
                    + "  if(s<0)window.location.href='" + redirectUrl + "';"
                    + "}"
                    + "setInterval(tick,1000);"
                    + "</script>"
                    + "</head>"
                    + "<body><div class='card'>"
                    + "<div class='icon'>❌</div>"
                    + "<h2>Token inválido o expirado</h2>"
                    + "<p>El enlace no es válido o ya fue utilizado.</p>"
                    + "<p>Solicita un nuevo correo de verificación.</p>"
                    + "<p>Redirigiendo en <span id='c' class='count'>10</span> segundos...</p>"
                    + "<p><a href='" + redirectUrl + "'>Ir al login</a></p>"
                    + "</div></body></html>";
        }

        resp.getWriter().write(html);
    }
}
