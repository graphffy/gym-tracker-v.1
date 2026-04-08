package com.gym.gymtracker.mapper;

import com.gym.gymtracker.dto.WorkoutDto;
import com.gym.gymtracker.model.Workout;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkoutMapper {

    public WorkoutDto toDto(Workout workout) {
        if (workout == null) {
            return null;
        }
        return WorkoutDto.builder()
            .id(workout.getId())
            .name(workout.getName())
            .workoutDate(workout.getWorkoutDate())
            .userId(workout.getUser() != null ? workout.getUser().getId() : null)
            .build();
    }

    public List<WorkoutDto> toDtoList(List<Workout> workouts) {
        return workouts.stream().map(this::toDto).collect(Collectors.toList());
    }
}
