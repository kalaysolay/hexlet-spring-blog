package io.hexlet.taskmanager.dto.label;

import jakarta.validation.constraints.Size;

public record LabelUpdateRequest(
        @Size(min = 3, max = 1000)
        String name
) {
    public boolean hasName() {
        return name != null;
    }
}
