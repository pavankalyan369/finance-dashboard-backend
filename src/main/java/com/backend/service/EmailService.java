package com.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendActivationEmail(String email, String token) {

        String link = "http://localhost:8080/auth/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Activate Your Account");
        message.setText("Click the link to activate your account:\n" + link);

        mailSender.send(message);
    }

    public void sendReactivationEmail(String email, String token) {

        String link = "http://localhost:8080/auth/reactivate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reactivate Your Account");
        message.setText("Click the link to reactivate your account:\n" + link);

        mailSender.send(message);
    }
}