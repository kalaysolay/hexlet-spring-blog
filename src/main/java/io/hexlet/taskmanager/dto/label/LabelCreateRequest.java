package io.hexlet.taskmanager.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LabelCreateRequest(
        @NotBlank
        @Size(min = 3, max = 1000)
        String name
) {
}
