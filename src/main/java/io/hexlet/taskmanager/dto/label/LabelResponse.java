package io.hexlet.taskmanager.dto.label;

import java.time.Instant;

public record LabelResponse(
        Long id,
        String name,
        Instant createdAt
) {
}
