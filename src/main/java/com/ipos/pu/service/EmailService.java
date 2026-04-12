package com.ipos.pu.service;

import com.ipos.pu.model.EmailLog;
import com.ipos.pu.repository.EmailLogRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private final EmailLogRepository emailLogRepository;
    private final JavaMailSender mailSender;

    public EmailService(EmailLogRepository emailLogRepository, JavaMailSender mailSender) {
        this.emailLogRepository = emailLogRepository;
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String to, String subject, String body) {
        EmailLog log = new EmailLog();
        log.setSentAt(LocalDateTime.now());
        log.setRecipient(to);
        log.setSubject(subject);
        log.setBody(body);
        emailLogRepository.save(log);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("seyer.city@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            System.out.println("--- EMAIL SENT ---");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("------------------");
            return true;
        } catch (Exception e) {
            System.err.println("--- EMAIL FAILED ---");
            System.err.println("To: " + to);
            System.err.println("Error: " + e.getMessage());
            System.err.println("--------------------");
            return false;
        }
    }
}
