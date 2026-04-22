/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.hospitalgeneral.webservlet;

import com.mycompany.hospitalgeneral.services.TuserService;
import jakarta.inject.Inject;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author jhon
 */
@WebServlet("/activar")
public class ActivateAccountServlet extends HttpServlet {

    @Inject
    private TuserService tuserService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html;charset=UTF-8");

        String token = req.getParameter("token");
        boolean ok = tuserService.verifyEmail(token);

        String redirectUrl = req.getContextPath();

        String html;

        if (ok) {
            html = "<html>"
                    + "<head>"
                    + "<title>Cuenta activada</title>"
                    + "<script>"
                    + "let seconds = 10;"
                    + "function countdown(){"
                    + "document.getElementById('count').innerText = seconds;"
                    + "seconds--;"
                    + "if(seconds < 0){ window.location.href='" + redirectUrl + "'; }"
                    + "}"
                    + "setInterval(countdown,1000);"
                    + "</script>"
                    + "</head>"
                    + "<body style='font-family:sans-serif;text-align:center;margin-top:50px;'>"
                    + "<h2>✅ Cuenta activada correctamente</h2>"
                    + "<p>Ya puedes iniciar sesión en el sistema.</p>"
                    + "<p>Redirigiendo en <span id='count'>10</span> segundos...</p>"
                    + "<p><a href='" + redirectUrl + "'>Ir ahora</a></p>"
                    + "</body>"
                    + "</html>";
        } else {
            html = "<html>"
                    + "<head>"
                    + "<title>Error de activación</title>"
                    + "<script>"
                    + "let seconds = 10;"
                    + "function countdown(){"
                    + "document.getElementById('count').innerText = seconds;"
                    + "seconds--;"
                    + "if(seconds < 0){ window.location.href='" + redirectUrl + "'; }"
                    + "}"
                    + "setInterval(countdown,1000);"
                    + "</script>"
                    + "</head>"
                    + "<body style='font-family:sans-serif;text-align:center;margin-top:50px;'>"
                    + "<h2>❌ Token inválido o expirado</h2>"
                    + "<p>El enlace no es válido o ya fue utilizado.</p>"
                    + "<p>Solicita un nuevo correo de verificación.</p>"
                    + "<p>Redirigiendo en <span id='count'>10</span> segundos...</p>"
                    + "<p><a href='" + redirectUrl + "'>Ir al login</a></p>"
                    + "</body>"
                    + "</html>";
        }

        resp.getWriter().write(html);
    }
}
