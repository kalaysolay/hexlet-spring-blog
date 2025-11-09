package io.hexlet.taskmanager.service;

import io.hexlet.taskmanager.dto.label.LabelCreateRequest;
import io.hexlet.taskmanager.dto.label.LabelResponse;
import io.hexlet.taskmanager.dto.label.LabelUpdateRequest;
import io.hexlet.taskmanager.model.Label;
import io.hexlet.taskmanager.repository.LabelRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class LabelService {

    private final LabelRepository labelRepository;

    public LabelService(LabelRepository labelRepository) {
        this.labelRepository = labelRepository;
    }

    public LabelResponse create(LabelCreateRequest request) {
        String normalizedName = normalizeName(request.name());

        if (labelRepository.existsByName(normalizedName)) {
            throw new ResponseStatusException(BAD_REQUEST, "Label name already exists");
        }

        Label label = new Label();
        label.setName(normalizedName);
        Label saved = labelRepository.save(label);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LabelResponse getById(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Label not found"));
        return toResponse(label);
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> getAll() {
        return labelRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LabelResponse update(Long id, LabelUpdateRequest request) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Label not found"));

        if (request.hasName()) {
            String normalizedName = normalizeName(request.name());
            label.setName(normalizedName);
        }

        if (labelRepository.existsByName(label.getName())
                && !label.getId().equals(id)) {
            throw new ResponseStatusException(BAD_REQUEST, "Label name already exists");
        }

        Label saved = labelRepository.save(label);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Label not found"));

        if (!label.getTasks().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Label is associated with tasks");
        }

        labelRepository.delete(label);
    }

    private LabelResponse toResponse(Label label) {
        return new LabelResponse(label.getId(), label.getName(), label.getCreatedAt());
    }

    private String normalizeName(String name) {
        String normalized = name.trim();
        if (normalized.length() < 3 || normalized.length() > 1000) {
            throw new ResponseStatusException(BAD_REQUEST, "Label name length is invalid");
        }
        return normalized;
    }
}
