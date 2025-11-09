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
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusUpdateRequest;
import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.model.TaskStatus;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.service.TaskStatusService;
import io.hexlet.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskStatusControllerTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        userService.create(new UserCreateRequest(ADMIN_EMAIL, "Admin", "User", ADMIN_PASSWORD));
        adminToken = authenticate(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void shouldCreateTaskStatus() throws Exception {
        TaskStatusCreateRequest request = new TaskStatusCreateRequest("Draft", "draft");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Draft"))
                .andExpect(jsonPath("$.slug").value("draft"))
                .andExpect(jsonPath("$.createdAt").exists());

        TaskStatus created = taskStatusRepository.findBySlug("draft").orElseThrow();
        assertThat(created.getName()).isEqualTo("Draft");
    }

    @Test
    void shouldGetTaskStatus() throws Exception {
        TaskStatus status = taskStatusService.create(new TaskStatusCreateRequest("New", "new"));

        mockMvc.perform(get("/api/task_statuses/" + status.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(status.getId()))
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.slug").value("new"));
    }

    @Test
    void shouldListTaskStatuses() throws Exception {
        taskStatusService.create(new TaskStatusCreateRequest("First", "first"));
        taskStatusService.create(new TaskStatusCreateRequest("Second", "second"));

        mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("First"))
                .andExpect(jsonPath("$[1].name").value("Second"));
    }

    @Test
    void shouldUpdateTaskStatusPartially() throws Exception {
        TaskStatus status = taskStatusService.create(new TaskStatusCreateRequest("Old", "old"));

        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest("Updated", null);

        mockMvc.perform(put("/api/task_statuses/" + status.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.slug").value("old"));

        TaskStatus updated = taskStatusRepository.findById(status.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldDeleteTaskStatus() throws Exception {
        TaskStatus status = taskStatusService.create(new TaskStatusCreateRequest("Remove", "remove"));

        mockMvc.perform(delete("/api/task_statuses/" + status.getId())
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.existsById(status.getId())).isFalse();
    }

    @Test
    void shouldRejectDuplicateSlug() throws Exception {
        taskStatusService.create(new TaskStatusCreateRequest("Existing", "existing"));

        TaskStatusCreateRequest request = new TaskStatusCreateRequest("Other", "existing");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthenticationForWriteOperations() throws Exception {
        TaskStatusCreateRequest request = new TaskStatusCreateRequest("Name", "slug");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        TaskStatus status = taskStatusService.create(new TaskStatusCreateRequest("Temp", "temp"));

        mockMvc.perform(put("/api/task_statuses/" + status.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskStatusUpdateRequest("Changed", null))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/task_statuses/" + status.getId()))
                .andExpect(status().isUnauthorized());
    }

    private String authenticate(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
