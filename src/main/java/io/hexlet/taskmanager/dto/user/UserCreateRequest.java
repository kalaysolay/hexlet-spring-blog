package io.hexlet.taskmanager.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank
        @Email
        String email,
        String firstName,
        String lastName,
        @NotBlank
        @Size(min = 3)
        String password
) {
}
