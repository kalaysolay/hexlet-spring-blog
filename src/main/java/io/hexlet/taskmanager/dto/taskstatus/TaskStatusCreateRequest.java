package io.hexlet.taskmanager.dto.taskstatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskStatusCreateRequest(
        @NotBlank
        @Size(min = 1)
        String name,
        @NotBlank
        @Size(min = 1)
        String slug
) {
}
