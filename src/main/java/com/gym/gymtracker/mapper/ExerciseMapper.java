package com.gym.gymtracker.mapper;

import com.gym.gymtracker.dto.ExerciseDto;
import com.gym.gymtracker.model.Category;
import com.gym.gymtracker.model.Exercise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExerciseMapper {

    public ExerciseDto toDto(Exercise exercise) {
        if (exercise == null) {
            return null;
        }

        return ExerciseDto.builder()
            .id(exercise.getId())
            .name(exercise.getName())
            .description(exercise.getDescription())
            .categoryIds(exercise.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toSet()))
            .build();
    }

    public List<ExerciseDto> toDtoList(List<Exercise> exercises) {
        return exercises.stream().map(this::toDto).collect(Collectors.toList());
    }
}
