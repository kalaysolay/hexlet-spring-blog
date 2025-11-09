package io.hexlet.taskmanager.repository.specification;

import io.hexlet.taskmanager.model.Label;
import io.hexlet.taskmanager.model.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> titleContains(String titleCont) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%");
    }

    public static Specification<Task> hasExecutor(Long assigneeId) {
        return (root, query, cb) -> cb.equal(root.join("executor", JoinType.LEFT).get("id"), assigneeId);
    }

    public static Specification<Task> hasStatus(String statusSlug) {
        return (root, query, cb) -> cb.equal(root.join("taskStatus").get("slug"), statusSlug);
    }

    public static Specification<Task> hasLabel(Long labelId) {
        return (root, query, cb) -> {
            Join<Task, Label> labels = root.join("labels");
            query.distinct(true);
            return cb.equal(labels.get("id"), labelId);
        };
    }
}
