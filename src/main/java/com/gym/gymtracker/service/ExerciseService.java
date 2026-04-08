package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.ExerciseDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.ExerciseMapper;
import com.gym.gymtracker.model.Category;
import com.gym.gymtracker.model.Exercise;
import com.gym.gymtracker.repository.CategoryRepository;
import com.gym.gymtracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final CategoryRepository categoryRepository;
    private final ExerciseMapper exerciseMapper;

    @Transactional(readOnly = true)
    public List<ExerciseDto> findAll() {
        return exerciseMapper.toDtoList(exerciseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ExerciseDto findById(Long id) {
        return exerciseRepository.findById(id)
            .map(exerciseMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
    }

    @Transactional
    public ExerciseDto create(ExerciseDto dto) {
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(dto.getCategoryIds()));

        Exercise exercise = Exercise.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .categories(categories)
            .build();

        return exerciseMapper.toDto(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseDto update(Long id, ExerciseDto dto) {
        Exercise existingExercise = exerciseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));

        existingExercise.setName(dto.getName());
        existingExercise.setDescription(dto.getDescription());

        if (dto.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(dto.getCategoryIds()));
            existingExercise.setCategories(categories);
        }

        return exerciseMapper.toDto(exerciseRepository.save(existingExercise));
    }

    @Transactional
    public void delete(Long id) {
        exerciseRepository.deleteById(id);
    }
}
