package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.ExerciseDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.ExerciseMapper;
import com.gym.gymtracker.model.Category;
import com.gym.gymtracker.model.Exercise;
import com.gym.gymtracker.repository.CategoryRepository;
import com.gym.gymtracker.repository.ExerciseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void findAllReturnsMappedExercises() {
        List<Exercise> exercises = List.of(Exercise.builder().id(1L).name("Bench").build());
        List<ExerciseDto> dtos = List.of(ExerciseDto.builder().id(1L).name("Bench").categoryIds(Set.of(1L)).build());

        when(exerciseRepository.findAll()).thenReturn(exercises);
        when(exerciseMapper.toDtoList(exercises)).thenReturn(dtos);

        assertSame(dtos, exerciseService.findAll());
    }

    @Test
    void findByIdReturnsMappedExercise() {
        Exercise exercise = Exercise.builder().id(1L).name("Bench").build();
        ExerciseDto dto = ExerciseDto.builder().id(1L).name("Bench").categoryIds(Set.of(1L)).build();

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(exerciseMapper.toDto(exercise)).thenReturn(dto);

        assertSame(dto, exerciseService.findById(1L));
    }

    @Test
    void findByIdThrowsWhenExerciseMissing() {
        when(exerciseRepository.findById(1L)).thenReturn(Optional.empty());

        Executable action = () -> exerciseService.findById(1L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createLoadsCategoriesAndReturnsMappedExercise() {
        Category category = Category.builder().id(10L).name("Chest").build();
        Exercise saved = Exercise.builder().id(2L).name("Bench").description("desc").build();
        ExerciseDto request = ExerciseDto.builder()
            .name("Bench")
            .description("desc")
            .categoryIds(Set.of(10L))
            .build();
        ExerciseDto response = ExerciseDto.builder()
            .id(2L)
            .name("Bench")
            .description("desc")
            .categoryIds(Set.of(10L))
            .build();

        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(category));
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);
        when(exerciseMapper.toDto(saved)).thenReturn(response);

        assertSame(response, exerciseService.create(request));
    }

    @Test
    void updateReplacesCategoriesWhenIdsProvided() {
        Exercise existing = Exercise.builder().id(2L).name("Old").description("old").build();
        Category category = Category.builder().id(11L).name("Back").build();
        ExerciseDto request = ExerciseDto.builder()
            .name("New")
            .description("new")
            .categoryIds(Set.of(11L))
            .build();
        ExerciseDto response = ExerciseDto.builder()
            .id(2L)
            .name("New")
            .description("new")
            .categoryIds(Set.of(11L))
            .build();

        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findAllById(request.getCategoryIds())).thenReturn(List.of(category));
        when(exerciseRepository.save(existing)).thenReturn(existing);
        when(exerciseMapper.toDto(existing)).thenReturn(response);

        assertSame(response, exerciseService.update(2L, request));
        assertEquals("New", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(1, existing.getCategories().size());
    }

    @Test
    void updateKeepsCategoriesWhenIdsMissing() {
        Category category = Category.builder().id(11L).name("Back").build();
        Exercise existing = Exercise.builder()
            .id(2L)
            .name("Old")
            .description("old")
            .categories(Set.of(category))
            .build();
        ExerciseDto request = ExerciseDto.builder().name("New").description("new").categoryIds(null).build();
        ExerciseDto response = ExerciseDto.builder()
            .id(2L)
            .name("New")
            .description("new")
            .categoryIds(Set.of(11L))
            .build();

        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.save(existing)).thenReturn(existing);
        when(exerciseMapper.toDto(existing)).thenReturn(response);

        assertSame(response, exerciseService.update(2L, request));
        verify(categoryRepository, never()).findAllById(any());
        assertEquals(1, existing.getCategories().size());
    }

    @Test
    void updateThrowsWhenExerciseMissing() {
        when(exerciseRepository.findById(2L)).thenReturn(Optional.empty());
        ExerciseDto request = ExerciseDto.builder().build();
        Executable action = () -> exerciseService.update(2L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void deleteDelegatesToRepository() {
        exerciseService.delete(7L);

        verify(exerciseRepository).deleteById(7L);
    }
}
