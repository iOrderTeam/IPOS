package com.ipos.pu.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // TODO: wire up a real email provider (e.g. Gmail SMTP or Mailtrap) when ready
    public boolean sendEmail(String to, String subject, String body) {
        System.out.println("--- EMAIL STUB ---");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        System.out.println("------------------");
        return true;
    }
}
