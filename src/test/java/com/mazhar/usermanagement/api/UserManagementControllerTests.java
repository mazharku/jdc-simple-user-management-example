package com.mazhar.usermanagement.api;


import com.mazhar.usermanagement.TestcontainersConfiguration;
import com.mazhar.usermanagement.model.dto.ErrorMessage;
import com.mazhar.usermanagement.model.dto.UserLoginRequest;
import com.mazhar.usermanagement.model.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.org.hamcrest.Matcher;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static org.testcontainers.shaded.org.hamcrest.Matchers.notNullValue;

//@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/test-data.sql")
@Tag(value = "integration")
public class UserManagementControllerTests {
    private static final String POSTGRES = "postgres:14";
    private static final String MAIL_DEV = "maildev/maildev:2.1.0";
    @Autowired
    protected TestRestTemplate restTemplate;
    private static final GenericContainer<?> mailDevContainer = new GenericContainer<>(MAIL_DEV);
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES));

    @DynamicPropertySource
    static void setup(DynamicPropertyRegistry registry) {
        mailDevContainer.withExposedPorts(1080, 1025)
                .withAccessToHost(true)
                .withReuse(true)
                .withLabel("security","email");


        Startables.deepStart(postgresContainer, mailDevContainer).join();

        registry.add("spring.datasource.url",()-> postgresContainer.getJdbcUrl());
        registry.add("spring.datasource.username", ()-> postgresContainer.getUsername());
        registry.add("spring.datasource.password", ()-> postgresContainer.getPassword());
        registry.add("spring.mail.host", mailDevContainer::getHost);
        registry.add("spring.mail.port", () -> mailDevContainer.getMappedPort(1025).toString());
        registry.add("spring.mail.properties.mail.smtp.auth", () -> false);
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> false);
    }

    @Test
    @DisplayName("create user invalid email will throw exception")
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
        registrationRequest.setEmail("user123@test.com");
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
        registrationRequest.setEmail("xyz1@test.com");
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
        Assertions.assertTrue(isEmailExistsInMailServer());
    }

    @Test
    void login_UserNotFound_ThrowInvalidUserError() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("user123@test.com");
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

    private boolean isEmailExistsInMailServer() {
        String mailServerUrl = "http://" + mailDevContainer.getHost() + ":" + mailDevContainer.getMappedPort(1080) + "/email";

        Map[] emails =waitForResponse(() -> getEmailsFromTheServer(mailServerUrl), notNullValue()) ;

        List<LinkedHashMap<String, Object>> maps = Arrays.stream(emails).
                map(email -> (LinkedHashMap<String, Object>) email).toList();

        return !maps.isEmpty();
    }

    private Map[] getEmailsFromTheServer(String mailServerUrl) {
        var response = restTemplate.getForEntity(mailServerUrl, Map[].class);
        return response.getBody();
    }

    private  <T> T waitForResponse(Callable<T> supplier, Matcher<? super T> matcher) {
        return await().atMost(15, SECONDS)
                .pollInterval(5, SECONDS)
                .until(supplier, matcher);
    }
}
