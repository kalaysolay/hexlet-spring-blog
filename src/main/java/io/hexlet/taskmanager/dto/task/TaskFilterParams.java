package io.hexlet.taskmanager.dto.task;

public record TaskFilterParams(
        String titleCont,
        Long assigneeId,
        String status,
        Long labelId
) {
    public boolean hasTitle() {
        return titleCont != null && !titleCont.isBlank();
    }

    public boolean hasAssignee() {
        return assigneeId != null;
    }

    public boolean hasStatus() {
        return status != null && !status.isBlank();
    }

    public boolean hasLabel() {
        return labelId != null;
    }
}
