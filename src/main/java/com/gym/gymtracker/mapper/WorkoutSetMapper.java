package com.gym.gymtracker.mapper;

import com.gym.gymtracker.dto.WorkoutSetDto;
import com.gym.gymtracker.model.WorkoutSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkoutSetMapper {

    public WorkoutSetDto toDto(WorkoutSet set) {
        if (set == null) {
            return null;
        }
        return WorkoutSetDto.builder()
            .id(set.getId())
            .name(set.getName())
            .weight(set.getWeight())
            .reps(set.getReps())
            .workoutId(set.getWorkout() != null ? set.getWorkout().getId() : null)
            .exerciseId(set.getExercise() != null ? set.getExercise().getId() : null)
            .build();
    }

    public List<WorkoutSetDto> toDtoList(List<WorkoutSet> sets) {
        return sets.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
