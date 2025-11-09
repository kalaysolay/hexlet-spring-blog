package io.hexlet.taskmanager.service;

import io.hexlet.taskmanager.dto.taskstatus.TaskStatusCreateRequest;
import io.hexlet.taskmanager.dto.taskstatus.TaskStatusUpdateRequest;
import io.hexlet.taskmanager.model.TaskStatus;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatusService(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    public TaskStatus create(TaskStatusCreateRequest request) {
        ensureUniqueName(request.name());
        ensureUniqueSlug(request.slug());

        TaskStatus status = new TaskStatus();
        status.setName(request.name());
        status.setSlug(request.slug());

        return taskStatusRepository.save(status);
    }

    public TaskStatus getById(Long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task status not found"));
    }

    public List<TaskStatus> getAll() {
        return taskStatusRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public TaskStatus update(Long id, TaskStatusUpdateRequest request) {
        TaskStatus status = getById(id);

        if (request.name() != null) {
            if (!StringUtils.hasText(request.name())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be blank");
            }
            if (!request.name().equals(status.getName())) {
                ensureUniqueName(request.name());
                status.setName(request.name());
            }
        }

        if (request.slug() != null) {
            if (!StringUtils.hasText(request.slug())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug must not be blank");
            }
            if (!request.slug().equals(status.getSlug())) {
                ensureUniqueSlug(request.slug());
                status.setSlug(request.slug());
            }
        }

        try {
            return taskStatusRepository.save(status);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task status data", e);
        }
    }

    public void delete(Long id) {
        if (!taskStatusRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task status not found");
        }
        taskStatusRepository.deleteById(id);
    }

    private void ensureUniqueName(String name) {
        if (taskStatusRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task status name already exists");
        }
    }

    private void ensureUniqueSlug(String slug) {
        if (taskStatusRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task status slug already exists");
        }
    }
}
