package com.mazhar.usermanagement.email;

import org.thymeleaf.context.Context;

public class EmailTemplate {
    private String emailSubject;
    private String receiver;
    private Context emailContext;
    private String templateName;

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Context getEmailContext() {
        return emailContext;
    }

    public void setEmailContext(Context emailContext) {
        this.emailContext = emailContext;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
