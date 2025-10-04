package com.andruy.backend.service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.andruy.backend.model.Email;

@Service
public class EmailService {
    @Value("${my.email.username}")
    private String username;
    @Value("${my.email.password}")
    private String password;
    @Value("${my.email.host}")
    private String host;
    @Value("${my.email.port}")
    private String port;
    private Session session;
    private Properties props;
    private Authenticator authenticator;
    private String feedback = "Not processed";
    private String name = "Personal Assistant";

    public void sendEmail(Email email, String type) {
        // Setup properties for the mail session
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // Get the default Session object
        session = Session.getInstance(props, authenticator);

        try {
            // Create a default MimeMessage object
            Message message = new MimeMessage(session);

            // Set From: header field of the header
            message.setFrom(new InternetAddress(username, name));

            // Set To: header field of the header
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.to()));

            // Set Subject: header field
            message.setSubject(email.subject());

            // Now set the actual message
            message.setContent(email.body(), type);

            // Send message
            Transport.send(message);

            feedback = "Email sent successfully!";
            System.out.println(feedback);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmail(Email email) {
        // Setup properties for the mail session
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // Get the default Session object
        session = Session.getInstance(props, authenticator);

        try {
            // Create a default MimeMessage object
            Message message = new MimeMessage(session);

            // Set From: header field of the header
            message.setFrom(new InternetAddress(username, name));

            // Set To: header field of the header
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.to()));

            // Set Subject: header field
            message.setSubject(email.subject());

            // Now set the actual message
            message.setText(email.body());

            // Send message
            Transport.send(message);

            feedback = "Email sent successfully!";
            System.out.println(feedback);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getFeedback() {
        return feedback;
    }
}
