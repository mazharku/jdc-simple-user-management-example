package com.mazhar.usermanagement.api;


import com.mazhar.usermanagement.model.dto.ErrorMessage;
import com.mazhar.usermanagement.model.dto.UserLoginRequest;
import com.mazhar.usermanagement.model.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/test-data.sql")
@Tag(value = "integration")
public class UserManagementControllerTests {
    private static final String POSTGRES = "postgres:14";
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES);

    @Autowired
    protected TestRestTemplate restTemplate;


    @DynamicPropertySource
    static void setup(DynamicPropertyRegistry registry) {
        postgreSQLContainer
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");


        Startables.deepStart(postgreSQLContainer).join();
        registry.add("spring.datasource.url", () -> "jdbc:tc:postgresql:14:///testdb");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
    }

    @Test
    void createUser_InvalidEmailFormat_ThrowException() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("abc");
        registrationRequest.setPassword("123456");
        registrationRequest.setName("abc");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(registrationRequest, headers);

        ResponseEntity<ErrorMessage> response = restTemplate.exchange(
                "/api/v1/user/register",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage errorMessage = response.getBody();
        Assertions.assertNotNull(errorMessage);
        Assertions.assertEquals("invalid email format!", errorMessage.message);

    }

    @Test
    void createUser_UserExists_ThrowException() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("abc@test.com");
        registrationRequest.setPassword("123456");
        registrationRequest.setName("abc");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(registrationRequest, headers);

        ResponseEntity<ErrorMessage> response = restTemplate.exchange(
                "/api/v1/user/register",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage errorMessage = response.getBody();
        Assertions.assertNotNull(errorMessage);
        Assertions.assertEquals("user already exists!", errorMessage.message);

    }

    @Test
    void createUser_InvalidPasswordLength_ThrowException() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("abcf@test.com");
        registrationRequest.setPassword("12345");
        registrationRequest.setName("abc");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(registrationRequest, headers);

        ResponseEntity<ErrorMessage> response = restTemplate.exchange(
                "/api/v1/user/register",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage errorMessage = response.getBody();
        Assertions.assertNotNull(errorMessage);
        Assertions.assertEquals("password length must be greater than 6", errorMessage.message);

    }

    @Test
    void createUser_ValidInput_success() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("abcd@test.com");
        registrationRequest.setPassword("123456");
        registrationRequest.setName("abcd");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(registrationRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/user/register",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String message = response.getBody();
        Assertions.assertNotNull(message);
        Assertions.assertEquals("user registered successfully", message);

    }

    @Test
    void login_UserNotFound_ThrowInvalidUserError() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("abcdk@test.com");
        loginRequest.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserLoginRequest> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<ErrorMessage> response = restTemplate.exchange(
                "/api/v1/user/login",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage errorMessage = response.getBody();
        Assertions.assertNotNull(errorMessage);
        Assertions.assertEquals("Invalid email", errorMessage.message);

    }

    @Test
    void login_PasswordMismatched_ThrowsInvalidCredentialsException() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("abc@test.com");
        loginRequest.setPassword("1234568");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserLoginRequest> entity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<ErrorMessage> response = restTemplate.exchange(
                "/api/v1/user/login",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorMessage errorMessage = response.getBody();
        Assertions.assertNotNull(errorMessage);
        Assertions.assertEquals("Invalid password!", errorMessage.message);

    }

    @Test
    void login_ValidLoginRequest_GenerateToken() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("abc@test.com");
        loginRequest.setPassword("123456");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<UserLoginRequest> entity = new HttpEntity<>(loginRequest, headers);

        String token = "abc:abc@test.com";

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/user/login",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)), response.getBody());

    }
}
