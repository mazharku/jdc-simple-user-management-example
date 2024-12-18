package com.mazhar.usermanagement.email;

import com.mazhar.usermanagement.service.SystemException.SystemExceptionHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;


@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }


    @Async
    public void sentEmail(EmailTemplate template) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(template.getEmailSubject());
            String fromMail = "noreply@testmail.com";
            message.setFrom(fromMail);
            message.setTo(template.getReceiver());
            String htmlContent = templateEngine.process(template.getTemplateName(), template.getEmailContext());
            message.setText(htmlContent, true);

            javaMailSender.send(message.getMimeMessage());
            log.info("email sent successfully to {}", template.getReceiver());
        } catch (MessagingException | MailException e) {
            log.error("email sent failure due to {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
