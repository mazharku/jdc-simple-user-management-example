package com.mazhar.usermanagement.service;


import com.mazhar.usermanagement.model.dto.UserLoginRequest;
import com.mazhar.usermanagement.model.dto.UserRegistrationRequest;
import com.mazhar.usermanagement.model.dto.exceptions.InvalidRequestType;
import com.mazhar.usermanagement.model.entity.UserEntity;
import com.mazhar.usermanagement.repository.UserRepository;
import com.mazhar.usermanagement.service.event.UserRegisterEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class UserManagementService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public UserManagementService(UserRepository userRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void createUser(UserRegistrationRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(e -> {
            throw new InvalidRequestType("user already exists!");
        });
        if (request.getPassword().length() < 6) {
            throw new InvalidRequestType("password length must be greater than 6");
        }
        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        userRepository.save(user);
        //applicationEventPublisher.publishEvent(new UserRegisterEvent(user));
    }

    public String login(UserLoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail()).
                orElseThrow(() -> new InvalidRequestType("Invalid email"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidRequestType("Invalid password!");
        }

        String credentials = user.getName() + ":" + user.getEmail();
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
