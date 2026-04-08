package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.WorkoutDto;
import com.gym.gymtracker.exception.BulkWorkoutDemoException;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.WorkoutMapper;
import com.gym.gymtracker.model.User;
import com.gym.gymtracker.model.Workout;
import com.gym.gymtracker.repository.UserRepository;
import com.gym.gymtracker.repository.WorkoutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    void findAllReturnsMappedWorkouts() {
        List<Workout> workouts = List.of(Workout.builder().id(1L).name("Leg Day").build());
        List<WorkoutDto> dtos = List.of(WorkoutDto.builder().id(1L).name("Leg Day").userId(1L).build());

        when(workoutRepository.findAll()).thenReturn(workouts);
        when(workoutMapper.toDtoList(workouts)).thenReturn(dtos);

        assertSame(dtos, workoutService.findAll());
    }

    @Test
    void findByIdReturnsMappedWorkout() {
        Workout workout = Workout.builder().id(1L).name("Leg Day").build();
        WorkoutDto dto = WorkoutDto.builder().id(1L).name("Leg Day").userId(1L).build();

        when(workoutRepository.findById(1L)).thenReturn(Optional.of(workout));
        when(workoutMapper.toDto(workout)).thenReturn(dto);

        assertSame(dto, workoutService.findById(1L));
    }

    @Test
    void findByIdThrowsWhenWorkoutMissing() {
        when(workoutRepository.findById(1L)).thenReturn(Optional.empty());

        Executable action = () -> workoutService.findById(1L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createUsesProvidedWorkoutDate() {
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 10, 0);
        User user = User.builder().id(1L).build();
        Workout saved = Workout.builder().id(5L).name("Leg Day").workoutDate(date).user(user).build();
        WorkoutDto request = WorkoutDto.builder().name("Leg Day").workoutDate(date).userId(1L).build();
        WorkoutDto response = WorkoutDto.builder().id(5L).name("Leg Day").workoutDate(date).userId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(response);

        assertSame(response, workoutService.create(request));
    }

    @Test
    void createUsesCurrentTimeWhenDateMissing() {
        User user = User.builder().id(1L).build();
        Workout saved = Workout.builder().id(5L).name("Leg Day").user(user).build();
        WorkoutDto request = WorkoutDto.builder().name("Leg Day").workoutDate(null).userId(1L).build();
        WorkoutDto response = WorkoutDto.builder().id(5L).name("Leg Day").userId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(response);

        assertSame(response, workoutService.create(request));
        verify(workoutRepository).save(any(Workout.class));
    }

    @Test
    void createThrowsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        WorkoutDto request = WorkoutDto.builder().name("Leg Day").userId(1L).build();
        Executable action = () -> workoutService.create(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createBulkNonTransactionalSavesEarlierItemsBeforeFailure() {
        User user = User.builder().id(1L).build();
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 10, 0);
        Workout saved = Workout.builder().id(5L).name("Chest Day").workoutDate(date).user(user).build();
        WorkoutDto savedDto = WorkoutDto.builder()
            .id(5L)
            .name("Chest Day")
            .workoutDate(date)
            .userId(1L)
            .build();
        List<WorkoutDto> request = List.of(
            WorkoutDto.builder().name("Chest Day").workoutDate(date).userId(1L).build(),
            WorkoutDto.builder().name("FAIL").workoutDate(date).userId(1L).build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(savedDto);

        Executable action = () -> workoutService.createBulkNonTransactional(request);

        BulkWorkoutDemoException exception = assertThrows(BulkWorkoutDemoException.class, action);
        assertEquals("Bulk demo failed on workout name: FAIL (non-transactional)", exception.getMessage());
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }

    @Test
    void createBulkTransactionalThrowsOnFailure() {
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 10, 0);
        User user = User.builder().id(1L).build();
        Workout saved = Workout.builder().id(5L).name("Chest Day").workoutDate(date).user(user).build();
        WorkoutDto savedDto = WorkoutDto.builder()
            .id(5L)
            .name("Chest Day")
            .workoutDate(date)
            .userId(1L)
            .build();
        List<WorkoutDto> request = List.of(
            WorkoutDto.builder().name("Chest Day").workoutDate(date).userId(1L).build(),
            WorkoutDto.builder().name("FAIL").workoutDate(date).userId(1L).build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(savedDto);

        Executable action = () -> workoutService.createBulkTransactional(request);

        BulkWorkoutDemoException exception = assertThrows(BulkWorkoutDemoException.class, action);
        assertEquals("Bulk demo failed on workout name: FAIL (transactional)", exception.getMessage());
    }

    @Test
    void createBulkNonTransactionalThrowsDemoExceptionWhenUserMissing() {
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 10, 0);
        User user = User.builder().id(1L).build();
        Workout saved = Workout.builder().id(5L).name("Chest Day").workoutDate(date).user(user).build();
        WorkoutDto savedDto = WorkoutDto.builder()
            .id(5L)
            .name("Chest Day")
            .workoutDate(date)
            .userId(1L)
            .build();
        List<WorkoutDto> request = List.of(
            WorkoutDto.builder().name("Chest Day").workoutDate(date).userId(1L).build(),
            WorkoutDto.builder().name("n").workoutDate(date).userId(800L).build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(800L)).thenReturn(Optional.empty());
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(savedDto);

        Executable action = () -> workoutService.createBulkNonTransactional(request);

        BulkWorkoutDemoException exception = assertThrows(BulkWorkoutDemoException.class, action);
        assertEquals("Bulk demo failed because user was not found: 800", exception.getMessage());
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }

    @Test
    void createBulkTransactionalThrowsDemoExceptionWhenUserMissing() {
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 10, 0);
        User user = User.builder().id(1L).build();
        Workout saved = Workout.builder().id(5L).name("Chest Day").workoutDate(date).user(user).build();
        WorkoutDto savedDto = WorkoutDto.builder()
            .id(5L)
            .name("Chest Day")
            .workoutDate(date)
            .userId(1L)
            .build();
        List<WorkoutDto> request = List.of(
            WorkoutDto.builder().name("Chest Day").workoutDate(date).userId(1L).build(),
            WorkoutDto.builder().name("n").workoutDate(date).userId(800L).build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(800L)).thenReturn(Optional.empty());
        when(workoutRepository.save(any(Workout.class))).thenReturn(saved);
        when(workoutMapper.toDto(saved)).thenReturn(savedDto);

        Executable action = () -> workoutService.createBulkTransactional(request);

        BulkWorkoutDemoException exception = assertThrows(BulkWorkoutDemoException.class, action);
        assertEquals("Bulk demo failed because user was not found: 800", exception.getMessage());
    }

    @Test
    void updateChangesWorkoutDateAndUser() {
        User oldUser = User.builder().id(1L).build();
        User newUser = User.builder().id(2L).build();
        LocalDateTime newDate = LocalDateTime.of(2026, 4, 8, 12, 0);
        Workout existing = Workout.builder().id(5L).name("Old").workoutDate(LocalDateTime.now()).user(oldUser).build();
        WorkoutDto request = WorkoutDto.builder().name("New").workoutDate(newDate).userId(2L).build();
        WorkoutDto response = WorkoutDto.builder().id(5L).name("New").workoutDate(newDate).userId(2L).build();

        when(workoutRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(workoutRepository.save(existing)).thenReturn(existing);
        when(workoutMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutService.update(5L, request));
        assertEquals("New", existing.getName());
        assertEquals(newDate, existing.getWorkoutDate());
        assertSame(newUser, existing.getUser());
    }

    @Test
    void updateKeepsDateAndUserWhenNoChangesNeeded() {
        User user = User.builder().id(1L).build();
        LocalDateTime date = LocalDateTime.of(2026, 4, 8, 8, 0);
        Workout existing = Workout.builder().id(5L).name("Old").workoutDate(date).user(user).build();
        WorkoutDto request = WorkoutDto.builder().name("New").workoutDate(null).userId(1L).build();
        WorkoutDto response = WorkoutDto.builder().id(5L).name("New").workoutDate(date).userId(1L).build();

        when(workoutRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(workoutRepository.save(existing)).thenReturn(existing);
        when(workoutMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutService.update(5L, request));
        assertEquals(date, existing.getWorkoutDate());
        assertSame(user, existing.getUser());
    }

    @Test
    void updateKeepsUserWhenUserIdIsNull() {
        User user = User.builder().id(1L).build();
        Workout existing = Workout.builder().id(5L).name("Old").workoutDate(LocalDateTime.now()).user(user).build();
        WorkoutDto request = WorkoutDto.builder().name("New").workoutDate(null).userId(null).build();
        WorkoutDto response = WorkoutDto.builder().id(5L).name("New").userId(1L).build();

        when(workoutRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(workoutRepository.save(existing)).thenReturn(existing);
        when(workoutMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutService.update(5L, request));
        assertSame(user, existing.getUser());
    }

    @Test
    void updateThrowsWhenWorkoutMissing() {
        when(workoutRepository.findById(5L)).thenReturn(Optional.empty());
        WorkoutDto request = WorkoutDto.builder().build();
        Executable action = () -> workoutService.update(5L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void updateThrowsWhenNewUserMissing() {
        User oldUser = User.builder().id(1L).build();
        Workout existing = Workout.builder().id(5L).name("Old").user(oldUser).build();

        when(workoutRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        WorkoutDto request = WorkoutDto.builder().name("New").userId(2L).build();
        Executable action = () -> workoutService.update(5L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void deleteDelegatesToRepository() {
        workoutService.delete(8L);

        verify(workoutRepository).deleteById(8L);
    }
}
