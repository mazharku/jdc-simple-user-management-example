package com.mazhar.usermanagement.service.event;

import com.mazhar.usermanagement.email.EmailService;
import com.mazhar.usermanagement.email.EmailTemplate;
import com.mazhar.usermanagement.model.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;


@Component
public class UserRegisterEventListener implements ApplicationListener<UserRegisterEvent> {
    private static final Logger log = LoggerFactory.getLogger(UserRegisterEventListener.class);
    private final EmailService emailService;

    public UserRegisterEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onApplicationEvent(UserRegisterEvent event) {
        UserEntity user = event.getUser();

        final Context context = new Context();
        context.setVariable("name", user.getName());

        EmailTemplate template = new EmailTemplate();
        template.setTemplateName("user-registration-mail");
        template.setReceiver(user.getEmail());
        template.setEmailSubject("User Registration");
        template.setEmailContext(context);

        emailService.sentEmail(template);
        log.info("user registration mail sent");
    }
}
