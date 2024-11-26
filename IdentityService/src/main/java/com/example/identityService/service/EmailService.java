package com.example.identityService.service;

import com.example.identityService.DTO.request.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @NonFinal
    @Value(value = "${spring.mail.username}")
    private String FROM_EMAIL;

    public void sendEmail(EmailRequest dto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject(dto.getSubject());
            helper.setText(dto.getContent(), true);
            helper.setFrom(FROM_EMAIL);
            helper.setTo(dto.getRecipients().toArray(new String[0]));

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}
