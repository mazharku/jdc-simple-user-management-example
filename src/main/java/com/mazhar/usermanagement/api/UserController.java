package com.mazhar.usermanagement.api;


import com.mazhar.usermanagement.model.dto.UserLoginRequest;
import com.mazhar.usermanagement.model.dto.UserRegistrationRequest;
import com.mazhar.usermanagement.service.UserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@Tag(
        name = "User Management",
        description = "*Endpoints for managing user management*"
)
public class UserController {

    private final UserManagementService service;

    public UserController(UserManagementService service) {
        this.service = service;
    }


    @PostMapping("/register")
    ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest
    ) {
        service.createUser(registrationRequest);
        return ResponseEntity.ok("user registered successfully");
    }

    @PostMapping("/login")
    ResponseEntity<String> login(@Valid @RequestBody UserLoginRequest request
    ) {
        String token = service.login(request);
        return ResponseEntity.ok(token);
    }
}
