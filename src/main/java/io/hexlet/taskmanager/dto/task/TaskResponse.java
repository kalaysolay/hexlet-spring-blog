package io.hexlet.taskmanager.dto.task;

import java.time.Instant;
import java.util.List;

public record TaskResponse(
        Long id,
        String name,
        String description,
        Long taskStatusId,
        Long authorId,
        Long executorId,
        List<Long> labelIds,
        Instant createdAt,
        Instant updatedAt
) {
}
