package com.mycompany.hospitalgeneral.webservlet;

import jakarta.faces.application.ViewExpiredException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filtro que intercepta ViewExpiredException para requests normales y Ajax.
 * Redirige a la página de sesión expirada en ambos casos.
 *
 * Configurar en web.xml:
 * <session-config><session-timeout>30</session-timeout></session-config>
 * <error-page>
 * <exception-type>jakarta.faces.application.ViewExpiredException</exception-type>
 * <location>/views/errors/session-expired.xhtml</location>
 * </error-page>
 */
@WebFilter(filterName = "ViewExpiredFilter", urlPatterns = {"*.xhtml"})
public class ViewExpiredFilter implements Filter {

    private static final String SESSION_EXPIRED_PAGE = "/views/errors/session-expired.xhtml";
    private static final String FACES_REQUEST_HEADER = "Faces-Request";
    private static final String AJAX_REQUEST_VALUE = "partial/ajax";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No se necesita inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);

        } catch (ServletException e) {
            // Buscar ViewExpiredException en la cadena de causas
            if (isViewExpiredException(e)) {
                handleExpiredView(req, resp);
            } else {
                throw e;
            }
        }
    }

    /**
     * Recorre la cadena de causas buscando ViewExpiredException
     */
    private boolean isViewExpiredException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ViewExpiredException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Redirige según el tipo de request: - Ajax (PrimeFaces): responde con XML
     * de redirección parcial - Normal: sendRedirect HTTP estándar
     */
    private void handleExpiredView(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        // Invalidar sesión si aún existe
        HttpSession session = req.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // La sesión ya fue invalidada, ignorar
            }
        }

        String redirectUrl = req.getContextPath() + SESSION_EXPIRED_PAGE;

        if (isAjaxRequest(req)) {
            resp.setContentType("text/xml");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().printf(
                    "<?xml version='1.0' encoding='UTF-8'?>"
                    + "<partial-response><redirect url=\"%s\"/></partial-response>",
                    redirectUrl
            );
        } else {
            resp.sendRedirect(redirectUrl);
        }
    }

    /**
     * Detecta si el request es una petición Ajax de Faces/PrimeFaces
     */
    private boolean isAjaxRequest(HttpServletRequest req) {
        return AJAX_REQUEST_VALUE.equals(req.getHeader(FACES_REQUEST_HEADER));
    }

    @Override
    public void destroy() {
        // No se necesita limpieza
    }
}
