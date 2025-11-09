package io.hexlet.taskmanager.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Email
        String email,
        String firstName,
        String lastName,
        @Size(min = 3)
        String password
) {
}
