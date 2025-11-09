package io.hexlet.taskmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.taskmanager.dto.auth.LoginRequest;
import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.dto.user.UserUpdateRequest;
import io.hexlet.taskmanager.model.User;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        userService.create(new UserCreateRequest(ADMIN_EMAIL, "Admin", "User", ADMIN_PASSWORD));
        adminToken = authenticate(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void shouldCreateUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "jack@example.com",
                "Jack",
                "Black",
                "secret"
        );

        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("jack@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jack"))
                .andExpect(jsonPath("$.createdAt").exists());

        User created = userRepository.findByEmail("jack@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("secret", created.getPassword())).isTrue();
    }

    @Test
    void shouldGetUser() throws Exception {
        User user = userService.create(new UserCreateRequest(
                "jane@example.com",
                "Jane",
                "Doe",
                "pass123"
        ));

        mockMvc.perform(get("/api/users/" + user.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldListUsers() throws Exception {
        userService.create(new UserCreateRequest("user1@example.com", "User1", "One", "pwd1"));
        userService.create(new UserCreateRequest("user2@example.com", "User2", "Two", "pwd2"));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    void shouldUpdateUserPartially() throws Exception {
        User user = userService.create(new UserCreateRequest(
                "old@example.com",
                "Old",
                "Name",
                "password"
        ));

        UserUpdateRequest request = new UserUpdateRequest(
                "new@example.com",
                "New",
                null,
                "newpass"
        );

        String token = authenticate("old@example.com", "password");

        mockMvc.perform(put("/api/users/" + user.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Name"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newpass", updated.getPassword())).isTrue();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = userService.create(new UserCreateRequest(
                "delete@example.com",
                "Delete",
                "Me",
                "pass123"
        ));

        String token = authenticate("delete@example.com", "pass123");

        mockMvc.perform(delete("/api/users/" + user.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    @Test
    void shouldReturnBadRequestForInvalidData() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "not-an-email",
                "Bad",
                "User",
                "pw"
        );

        mockMvc.perform(post("/api/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldForbidUpdatingAnotherUser() throws Exception {
        User first = userService.create(new UserCreateRequest("first@example.com", "First", "User", "firstpass"));
        User second = userService.create(new UserCreateRequest("second@example.com", "Second", "User", "secondpass"));

        String token = authenticate("first@example.com", "firstpass");

        UserUpdateRequest request = new UserUpdateRequest("newmail@example.com", null, null, null);

        mockMvc.perform(put("/api/users/" + second.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAuthenticateAndReturnToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotBlank();
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(ADMIN_EMAIL, "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    private String authenticate(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
