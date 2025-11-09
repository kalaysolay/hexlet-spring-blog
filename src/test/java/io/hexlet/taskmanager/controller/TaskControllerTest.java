package io.hexlet.taskmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.taskmanager.dto.auth.LoginRequest;
import io.hexlet.taskmanager.dto.label.LabelCreateRequest;
import io.hexlet.taskmanager.dto.task.TaskCreateRequest;
import io.hexlet.taskmanager.dto.task.TaskUpdateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.model.Task;
import io.hexlet.taskmanager.repository.LabelRepository;
import io.hexlet.taskmanager.repository.TaskRepository;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.service.LabelService;
import io.hexlet.taskmanager.service.TaskStatusService;
import io.hexlet.taskmanager.service.UserService;
import java.util.ArrayList;
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
class TaskControllerTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "adminpass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private LabelService labelService;

    @Autowired
    private LabelRepository labelRepository;

    private String adminToken;

    private Long statusId;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();

        userService.create(new UserCreateRequest(ADMIN_EMAIL, "Admin", "User", ADMIN_PASSWORD));
        adminToken = authenticate(ADMIN_EMAIL, ADMIN_PASSWORD);
        statusId = taskStatusService.create(new TaskStatusCreateRequest("Draft", "draft")).getId();
    }

    @Test
    void shouldCreateTaskWithLabels() throws Exception {
        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();

        TaskCreateRequest request = new TaskCreateRequest("Task name", "Description", statusId, null, Set.of(labelId));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Task name"))
                .andExpect(jsonPath("$.labelIds[0]").value(labelId));

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getLabels()).extracting(label -> label.getId()).containsExactly(labelId);
    }

    @Test
    void shouldUpdateTaskLabels() throws Exception {
        Long firstLabelId = labelService.create(new LabelCreateRequest("Feature")).id();
        Long secondLabelId = labelService.create(new LabelCreateRequest("Bug")).id();

        TaskCreateRequest createRequest = new TaskCreateRequest("Task name", "Description", statusId, null, Set.of(firstLabelId));

        MvcResult creationResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asLong();

        TaskUpdateRequest updateRequest = new TaskUpdateRequest(null, null, null, null, Set.of(secondLabelId));

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelIds[0]").value(secondLabelId));

        Task updated = taskRepository.findById(taskId).orElseThrow();
        assertThat(updated.getLabels()).extracting(label -> label.getId()).containsExactly(secondLabelId);
    }

    @Test
    void shouldReturnTasksList() throws Exception {
        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("Task name", "Description", statusId, null, Set.of(labelId)))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Task name"))
                .andExpect(jsonPath("$[0].labelIds[0]").value(labelId));
    }

    @Test
    void shouldFilterTasksByQueryParams() throws Exception {
        Long bugLabelId = labelService.create(new LabelCreateRequest("Bug")).id();
        Long featureLabelId = labelService.create(new LabelCreateRequest("Feature")).id();

        Long inProgressStatusId = taskStatusService.create(new TaskStatusCreateRequest("In progress", "in_progress")).getId();

        Long executorId = userService.create(new UserCreateRequest("executor@example.com", "Executor", "User", "secret"))
                .getId();

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest(
                                "Create API", "Description", statusId, executorId, Set.of(featureLabelId)
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest(
                                "Fix bug", "Bug desc", inProgressStatusId, null, Set.of(bugLabelId)
                        ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest(
                                "Review specs", "Specs desc", inProgressStatusId, executorId, Set.of(bugLabelId)
                        ))))
                .andExpect(status().isCreated());

        MvcResult titleResult = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .param("titleCont", "create"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode titleTasks = objectMapper.readTree(titleResult.getResponse().getContentAsString());
        assertThat(titleTasks).hasSize(1);
        assertThat(titleTasks.get(0).get("name").asText()).isEqualTo("Create API");

        MvcResult assigneeResult = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .param("assigneeId", executorId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode assigneeTasks = objectMapper.readTree(assigneeResult.getResponse().getContentAsString());
        List<String> assigneeNames = new ArrayList<>();
        assigneeTasks.forEach(node -> assigneeNames.add(node.get("name").asText()));
        assertThat(assigneeNames).containsExactly("Create API", "Review specs");

        MvcResult statusResult = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "in_progress"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode statusTasks = objectMapper.readTree(statusResult.getResponse().getContentAsString());
        List<String> statusNames = new ArrayList<>();
        statusTasks.forEach(node -> statusNames.add(node.get("name").asText()));
        assertThat(statusNames).containsExactly("Fix bug", "Review specs");

        MvcResult labelResult = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .param("labelId", bugLabelId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode labelTasks = objectMapper.readTree(labelResult.getResponse().getContentAsString());
        List<String> labelNames = new ArrayList<>();
        labelTasks.forEach(node -> labelNames.add(node.get("name").asText()));
        assertThat(labelNames).containsExactly("Fix bug", "Review specs");
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Long labelId = labelService.create(new LabelCreateRequest("Feature")).id();

        MvcResult creationResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskCreateRequest("Task name", "Description", statusId, null, Set.of(labelId)))))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(taskId)).isFalse();
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
