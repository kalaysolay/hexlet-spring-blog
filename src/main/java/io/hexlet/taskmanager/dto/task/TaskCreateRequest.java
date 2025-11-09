package io.hexlet.taskmanager.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record TaskCreateRequest(
        @NotBlank
        @Size(min = 3, max = 255)
        String name,
        String description,
        @NotNull
        Long taskStatusId,
        Long executorId,
        Set<Long> labelIds
) {
}
