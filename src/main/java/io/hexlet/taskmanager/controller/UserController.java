package io.hexlet.taskmanager.controller;

import io.hexlet.taskmanager.dto.user.UserCreateRequest;
import io.hexlet.taskmanager.dto.user.UserResponse;
import io.hexlet.taskmanager.dto.user.UserUpdateRequest;
import io.hexlet.taskmanager.model.User;
import io.hexlet.taskmanager.security.UserPrincipal;
import io.hexlet.taskmanager.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> index() {
        return userService.getAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse show(@PathVariable Long id) {
        User user = userService.getById(id);
        return UserResponse.from(user);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(UserResponse.from(user));
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ensureSelfAction(id, currentUser);
        User user = userService.update(id, request);
        return UserResponse.from(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ensureSelfAction(id, currentUser);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void ensureSelfAction(Long targetUserId, UserPrincipal currentUser) {
        if (currentUser == null || !currentUser.getId().equals(targetUserId)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }
    }
}
