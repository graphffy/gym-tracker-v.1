package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.WorkoutDto;
import com.gym.gymtracker.service.WorkoutService;
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
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Workouts", description = "Управление тренировками")
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "Получить все тренировки")
    @GetMapping
    public List<WorkoutDto> getAll() {
        return workoutService.findAll();
    }

    @Operation(summary = "Получить тренировку по ID")
    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable Long id) {
        return workoutService.findById(id);
    }

    @Operation(summary = "Создать тренировку")
    @PostMapping
    public WorkoutDto create(@Valid @RequestBody WorkoutDto dto) {
        return workoutService.create(dto);
    }

    @Operation(summary = "Обновить тренировку")
    @PutMapping("/{id}")
    public WorkoutDto update(@PathVariable Long id, @Valid @RequestBody WorkoutDto dto) {
        return workoutService.update(id, dto);
    }

    @Operation(summary = "Удалить тренировку")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workoutService.delete(id);
    }
}
