package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.WorkoutSetDto;
import com.gym.gymtracker.service.WorkoutSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Workout Sets", description = "Управление подходами")
public class WorkoutSetController {

    private final WorkoutSetService workoutSetService;

    @Operation(summary = "Получить все подходы")
    @GetMapping
    public List<WorkoutSetDto> getAll() {
        return workoutSetService.findAll();
    }

    @Operation(summary = "Получить подход по ID")
    @GetMapping("/{id}")
    public WorkoutSetDto getById(@PathVariable Long id) {
        return workoutSetService.findById(id);
    }

    @Operation(summary = "Поиск подходов (JPQL)")
    @GetMapping("/search/jpql")
    public Page<WorkoutSetDto> searchByUserAndExerciseJpql(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String exerciseName,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return workoutSetService.searchByUserAndExerciseJpql(username, exerciseName, page, size);
    }

    @Operation(summary = "Поиск подходов (native SQL)")
    @GetMapping("/search/native")
    public Page<WorkoutSetDto> searchByUserAndExerciseNative(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String exerciseName,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) int size
    ) {
        return workoutSetService.searchByUserAndExerciseNative(username, exerciseName, page, size);
    }

    @Operation(summary = "Создать подход")
    @PostMapping
    public WorkoutSetDto create(@Valid @RequestBody WorkoutSetDto dto) {
        return workoutSetService.create(dto);
    }

    @Operation(summary = "Обновить подход")
    @PutMapping("/{id}")
    public WorkoutSetDto update(@PathVariable Long id, @Valid @RequestBody WorkoutSetDto dto) {
        return workoutSetService.update(id, dto);
    }

    @Operation(summary = "Удалить подход")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workoutSetService.delete(id);
    }
}
