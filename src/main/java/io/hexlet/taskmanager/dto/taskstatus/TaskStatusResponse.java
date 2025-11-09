package io.hexlet.taskmanager.dto.taskstatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hexlet.taskmanager.model.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record TaskStatusResponse(
        Long id,
        String name,
        String slug,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {

    public static TaskStatusResponse from(TaskStatus status) {
        return new TaskStatusResponse(
                status.getId(),
                status.getName(),
                status.getSlug(),
                toLocalDate(status.getCreatedAt())
        );
    }

    private static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
