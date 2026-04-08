package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.ExerciseDto;
import com.gym.gymtracker.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@Validated
@Tag(name = "Exercises", description = "Управление упражнениями")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Operation(summary = "Получить все упражнения")
    @GetMapping
    public List<ExerciseDto> getAll() {
        return exerciseService.findAll();
    }

    @Operation(summary = "Получить упражнение по ID")
    @GetMapping("/{id}")
    public ExerciseDto getById(@PathVariable Long id) {
        return exerciseService.findById(id);
    }

    @Operation(summary = "Создать упражнение")
    @PostMapping
    public ExerciseDto create(@Valid @RequestBody ExerciseDto dto) {
        return exerciseService.create(dto);
    }

    @Operation(summary = "Обновить упражнение")
    @PutMapping("/{id}")
    public ExerciseDto update(@PathVariable Long id, @Valid @RequestBody ExerciseDto dto) {
        return exerciseService.update(id, dto);
    }

    @Operation(summary = "Удалить упражнение")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        exerciseService.delete(id);
    }
}

