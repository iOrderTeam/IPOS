package com.ipos.pu.service;

import com.ipos.pu.model.EmailLog;
import com.ipos.pu.repository.EmailLogRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private final EmailLogRepository emailLogRepository;

    public EmailService(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    // TODO: wire up a real email provider (e.g. Gmail SMTP or Mailtrap) when ready
    public boolean sendEmail(String to, String subject, String body) {
        EmailLog log = new EmailLog();
        log.setSentAt(LocalDateTime.now());
        log.setRecipient(to);
        log.setSubject(subject);
        log.setBody(body);
        emailLogRepository.save(log);

        System.out.println("--- EMAIL STUB ---");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("------------------");
        return true;
    }
}
