package io.hexlet.taskmanager.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hexlet.taskmanager.model.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                toLocalDate(user.getCreatedAt())
        );
    }

    private static LocalDate toLocalDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
