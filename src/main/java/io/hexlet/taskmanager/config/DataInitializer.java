package io.hexlet.taskmanager.config;

import io.hexlet.taskmanager.dto.label.LabelCreateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.repository.LabelRepository;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.service.LabelService;
import io.hexlet.taskmanager.service.TaskStatusService;
import io.hexlet.taskmanager.service.UserService;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final String DEFAULT_EMAIL = "hexlet@example.com";
    private static final String DEFAULT_PASSWORD = "qwerty";

    private static final List<TaskStatusDefinition> DEFAULT_STATUSES = List.of(
            new TaskStatusDefinition("Draft", "draft"),
            new TaskStatusDefinition("To review", "to_review"),
            new TaskStatusDefinition("To be fixed", "to_be_fixed"),
            new TaskStatusDefinition("To publish", "to_publish"),
            new TaskStatusDefinition("Published", "published")
    );

    private static final List<String> DEFAULT_LABELS = List.of(
            "feature",
            "bug"
    );

    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final TaskStatusService taskStatusService;
    private final LabelService labelService;

    public DataInitializer(
            UserRepository userRepository,
            UserService userService,
            TaskStatusRepository taskStatusRepository,
            TaskStatusService taskStatusService,
            LabelRepository labelRepository,
            LabelService labelService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.taskStatusRepository = taskStatusRepository;
        this.taskStatusService = taskStatusService;
        this.labelRepository = labelRepository;
        this.labelService = labelService;
    }

    @PostConstruct
    public void init() {
        initDefaultUser();
        initDefaultStatuses();
        initDefaultLabels();
    }

    private void initDefaultUser() {
        if (userRepository.existsByEmail(DEFAULT_EMAIL)) {
            return;
        }

        UserCreateRequest request = new UserCreateRequest(
                DEFAULT_EMAIL,
                "Hexlet",
                "Admin",
                DEFAULT_PASSWORD
        );
        userService.create(request);
    }

    private void initDefaultStatuses() {
        for (TaskStatusDefinition status : DEFAULT_STATUSES) {
            if (taskStatusRepository.existsBySlug(status.slug())) {
                continue;
            }
            TaskStatusCreateRequest request = new TaskStatusCreateRequest(status.name(), status.slug());
            taskStatusService.create(request);
        }
    }

    private void initDefaultLabels() {
        for (String labelName : DEFAULT_LABELS) {
            if (labelRepository.existsByName(labelName)) {
                continue;
            }
            LabelCreateRequest request = new LabelCreateRequest(labelName);
            labelService.create(request);
        }
    }

    private record TaskStatusDefinition(String name, String slug) {
    }
}
