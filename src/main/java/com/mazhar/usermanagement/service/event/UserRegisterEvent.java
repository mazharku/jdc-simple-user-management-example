package com.mazhar.usermanagement.service.event;

import com.mazhar.usermanagement.model.entity.UserEntity;
import org.springframework.context.ApplicationEvent;

public class UserRegisterEvent extends ApplicationEvent {
    private final UserEntity user;

    public UserRegisterEvent(UserEntity user) {
        super(user);
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }
}
