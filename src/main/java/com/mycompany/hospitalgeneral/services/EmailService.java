
package com.mycompany.hospitalgeneral.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 *
 * @author jhon
 */
@ApplicationScoped
public class EmailService {

    private final String username = "tu_correo@gmail.com";
    private final String password = "app_password";

    public void enviarCorreo(String to, String subject, String html) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            message.setSubject(subject);
            message.setContent(html, "text/html; charset=utf-8");

            Transport.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
