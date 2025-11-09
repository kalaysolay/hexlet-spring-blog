package io.hexlet.taskmanager.controller;

import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusResponse;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusUpdateRequest;
import io.hexlet.taskmanager.model.TaskStatus;
import io.hexlet.taskmanager.service.TaskStatusService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    public TaskStatusController(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping
    public List<TaskStatusResponse> index() {
        return taskStatusService.getAll().stream()
                .map(TaskStatusResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public TaskStatusResponse show(@PathVariable Long id) {
        TaskStatus status = taskStatusService.getById(id);
        return TaskStatusResponse.from(status);
    }

    @PostMapping
    public ResponseEntity<TaskStatusResponse> create(
            @Valid @RequestBody TaskStatusCreateRequest request
    ) {
        TaskStatus status = taskStatusService.create(request);
        return ResponseEntity
                .created(URI.create("/api/task_statuses/" + status.getId()))
                .body(TaskStatusResponse.from(status));
    }

    @PutMapping("/{id}")
    public TaskStatusResponse update(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request
    ) {
        TaskStatus status = taskStatusService.update(id, request);
        return TaskStatusResponse.from(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskStatusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
