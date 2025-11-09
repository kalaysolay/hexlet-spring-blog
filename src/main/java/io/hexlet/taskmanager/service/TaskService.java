package io.hexlet.taskmanager.service;

import io.hexlet.taskmanager.dto.task.TaskCreateRequest;
import io.hexlet.taskmanager.dto.task.TaskFilterParams;
import io.hexlet.taskmanager.dto.task.TaskResponse;
import io.hexlet.taskmanager.dto.task.TaskUpdateRequest;
import io.hexlet.taskmanager.model.Label;
import io.hexlet.taskmanager.model.Task;
import io.hexlet.taskmanager.model.TaskStatus;
import io.hexlet.taskmanager.model.User;
import io.hexlet.taskmanager.repository.LabelRepository;
import io.hexlet.taskmanager.repository.TaskRepository;
import io.hexlet.taskmanager.repository.TaskStatusRepository;
import io.hexlet.taskmanager.repository.UserRepository;
import io.hexlet.taskmanager.repository.specification.TaskSpecifications;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskStatusRepository taskStatusRepository,
            UserRepository userRepository,
            LabelRepository labelRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
    }

    public TaskResponse create(TaskCreateRequest request, Long authorId) {
        Task task = new Task();
        task.setName(request.name());
        task.setDescription(request.description());
        task.setTaskStatus(getTaskStatus(request.taskStatusId()));
        task.setAuthor(getUser(authorId));

        if (request.executorId() != null) {
            task.setExecutor(getUser(request.executorId()));
        }

        if (request.labelIds() != null && !request.labelIds().isEmpty()) {
            task.setLabels(getLabelsByIds(request.labelIds()));
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(TaskFilterParams filterParams) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Task> tasks;

        Specification<Task> specification = buildSpecification(filterParams);

        if (specification == null) {
            tasks = taskRepository.findAll(sort);
        } else {
            tasks = taskRepository.findAll(specification, sort);
        }

        return tasks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (request.hasName()) {
            task.setName(request.name());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.hasTaskStatus()) {
            task.setTaskStatus(getTaskStatus(request.taskStatusId()));
        }

        if (request.hasExecutor()) {
            if (request.executorId() == null) {
                task.setExecutor(null);
            } else {
                task.setExecutor(getUser(request.executorId()));
            }
        }

        if (request.hasLabels()) {
            if (request.labelIds().isEmpty()) {
                task.setLabels(new HashSet<>());
            } else {
                task.setLabels(getLabelsByIds(request.labelIds()));
            }
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        taskRepository.deleteById(id);
    }

    private Specification<Task> buildSpecification(TaskFilterParams filterParams) {
        if (filterParams == null) {
            return null;
        }

        Specification<Task> specification = Specification.where(null);

        if (filterParams.hasTitle()) {
            specification = addCondition(specification, TaskSpecifications.titleContains(filterParams.titleCont()));
        }

        if (filterParams.hasAssignee()) {
            specification = addCondition(specification, TaskSpecifications.hasExecutor(filterParams.assigneeId()));
        }

        if (filterParams.hasStatus()) {
            specification = addCondition(specification, TaskSpecifications.hasStatus(filterParams.status()));
        }

        if (filterParams.hasLabel()) {
            specification = addCondition(specification, TaskSpecifications.hasLabel(filterParams.labelId()));
        }

        return specification;
    }

    private Specification<Task> addCondition(Specification<Task> base, Specification<Task> addition) {
        if (addition == null) {
            return base;
        }
        return base == null ? Specification.where(addition) : base.and(addition);
    }

    private TaskResponse toResponse(Task task) {
        List<Long> labelIds = task.getLabels().stream()
                .map(Label::getId)
                .sorted(Comparator.naturalOrder())
                .toList();

        Long executorId = task.getExecutor() != null ? task.getExecutor().getId() : null;

        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getTaskStatus().getId(),
                task.getAuthor().getId(),
                executorId,
                labelIds,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private TaskStatus getTaskStatus(Long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task status not found"));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    private Set<Label> getLabelsByIds(Set<Long> ids) {
        List<Label> labels = labelRepository.findAllById(ids);
        if (labels.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more labels not found");
        }
        return new HashSet<>(labels);
    }
}
