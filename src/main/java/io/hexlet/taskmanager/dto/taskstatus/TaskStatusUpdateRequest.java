package io.hexlet.taskmanager.dto.taskstatus;

import jakarta.validation.constraints.Size;

public record TaskStatusUpdateRequest(
        @Size(min = 1)
        String name,
        @Size(min = 1)
        String slug
) {
}
