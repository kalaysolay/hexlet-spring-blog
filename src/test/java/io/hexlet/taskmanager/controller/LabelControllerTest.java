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
import io.hexlet.taskmanager.dto.label.LabelCreateRequest;
import io.hexlet.taskmanager.dto.label.LabelUpdateRequest;
import io.hexlet.taskmanager.dto.task.TaskCreateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.repository.LabelRepository;
import io.hexlet.taskmanager.repository.TaskRepository;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.service.LabelService;
import io.hexlet.taskmanager.service.TaskService;
import io.hexlet.taskmanager.service.TaskStatusService;
import io.hexlet.taskmanager.service.UserService;
import java.util.List;
import java.util.Set;
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
class LabelControllerTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelService labelService;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

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
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        userService.create(new UserCreateRequest(ADMIN_EMAIL, "Admin", "User", ADMIN_PASSWORD));
        adminToken = authenticate(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void shouldCreateLabel() throws Exception {
        LabelCreateRequest request = new LabelCreateRequest("Feature");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Feature"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(labelRepository.existsByName("Feature")).isTrue();
    }

    @Test
    void shouldListLabels() throws Exception {
        labelService.create(new LabelCreateRequest("Feature"));
        labelService.create(new LabelCreateRequest("Bug"));

        mockMvc.perform(get("/api/labels")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Feature"))
                .andExpect(jsonPath("$[1].name").value("Bug"));
    }

    @Test
    void shouldUpdateLabel() throws Exception {
        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();

        LabelUpdateRequest request = new LabelUpdateRequest("Improvement");

        mockMvc.perform(put("/api/labels/" + labelId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Improvement"));

        assertThat(labelRepository.findById(labelId)).get()
                .extracting(label -> label.getName())
                .isEqualTo("Improvement");
    }

    @Test
    void shouldRejectDuplicateLabelName() throws Exception {
        labelService.create(new LabelCreateRequest("Feature"));

        LabelCreateRequest request = new LabelCreateRequest("Feature");

        mockMvc.perform(post("/api/labels")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventDeletionWhenLabelLinkedToTask() throws Exception {
        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();
        Long statusId = taskStatusService.create(new TaskStatusCreateRequest("Draft", "draft")).getId();
        Long authorId = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow().getId();

        taskService.create(new TaskCreateRequest("Task", "desc", statusId, null, Set.of(labelId)), authorId);

        mockMvc.perform(delete("/api/labels/" + labelId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        LabelCreateRequest request = new LabelCreateRequest("Feature");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();

        mockMvc.perform(put("/api/labels/" + labelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LabelUpdateRequest("Other"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/labels/" + labelId))
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
