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
@Tag(name = "Workouts", description = "Workout management")
public class WorkoutController {

    private final WorkoutService workoutService;

    @Operation(summary = "Get all workouts")
    @GetMapping
    public List<WorkoutDto> getAll() {
        return workoutService.findAll();
    }

    @Operation(summary = "Get workout by id")
    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable Long id) {
        return workoutService.findById(id);
    }

    @Operation(summary = "Create workout")
    @PostMapping
    public WorkoutDto create(@Valid @RequestBody WorkoutDto dto) {
        return workoutService.create(dto);
    }

    @Operation(summary = "Bulk create workouts without a shared transaction")
    @PostMapping("/bulk/non-transactional")
    public List<WorkoutDto> createBulkNonTransactional(@Valid @RequestBody List<WorkoutDto> dtos) {
        return workoutService.createBulkNonTransactional(dtos);
    }

    @Operation(summary = "Bulk create workouts in a single transaction")
    @PostMapping("/bulk/transactional")
    public List<WorkoutDto> createBulkTransactional(@Valid @RequestBody List<WorkoutDto> dtos) {
        return workoutService.createBulkTransactional(dtos);
    }

    @Operation(summary = "Update workout")
    @PutMapping("/{id}")
    public WorkoutDto update(@PathVariable Long id, @Valid @RequestBody WorkoutDto dto) {
        return workoutService.update(id, dto);
    }

    @Operation(summary = "Delete workout")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        workoutService.delete(id);
    }
}
