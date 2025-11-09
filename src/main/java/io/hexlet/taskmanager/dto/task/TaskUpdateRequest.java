package io.hexlet.taskmanager.dto.task;

import jakarta.validation.constraints.Size;
import java.util.Set;

public record TaskUpdateRequest(
        @Size(min = 3, max = 255)
        String name,
        String description,
        Long taskStatusId,
        Long executorId,
        Set<Long> labelIds
) {
    public boolean hasName() {
        return name != null;
    }

    public boolean hasTaskStatus() {
        return taskStatusId != null;
    }

    public boolean hasExecutor() {
        return executorId != null;
    }

    public boolean hasLabels() {
        return labelIds != null;
    }
}
