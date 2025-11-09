package io.hexlet.taskmanager.repository;

import io.hexlet.taskmanager.model.TaskStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<TaskStatus> findBySlug(String slug);
}
