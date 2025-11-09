package io.hexlet.taskmanager.repository;

import io.hexlet.taskmanager.model.Label;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {

    boolean existsByName(String name);

    Optional<Label> findByName(String name);
}
