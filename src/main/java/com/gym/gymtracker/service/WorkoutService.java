package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.WorkoutDto;
import com.gym.gymtracker.exception.BulkWorkoutDemoException;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.WorkoutMapper;
import com.gym.gymtracker.model.User;
import com.gym.gymtracker.model.Workout;
import com.gym.gymtracker.repository.UserRepository;
import com.gym.gymtracker.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutMapper workoutMapper;

    @Transactional(readOnly = true)
    public List<WorkoutDto> findAll() {
        return workoutMapper.toDtoList(workoutRepository.findAll());
    }

    @Transactional(readOnly = true)
    public WorkoutDto findById(Long id) {
        return workoutRepository.findById(id)
            .map(workoutMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));
    }

    @Transactional
    public WorkoutDto create(WorkoutDto dto) {
        User user = getUserOrThrow(dto.getUserId());

        Workout workout = buildWorkout(dto, user);

        return workoutMapper.toDto(workoutRepository.save(workout));
    }

    public List<WorkoutDto> createBulkNonTransactional(List<WorkoutDto> dtos) {
        List<WorkoutDto> createdWorkouts = new ArrayList<>();

        for (WorkoutDto dto : dtos) {
            validateBulkWorkout(dto);
            createdWorkouts.add(createBulkWorkout(dto));
        }

        return createdWorkouts;
    }

    @Transactional
    public List<WorkoutDto> createBulkTransactional(List<WorkoutDto> dtos) {
        List<WorkoutDto> createdWorkouts = new ArrayList<>();

        for (WorkoutDto dto : dtos) {
            validateBulkWorkout(dto);
            createdWorkouts.add(createBulkWorkout(dto));
        }

        return createdWorkouts;
    }

    @Transactional
    public WorkoutDto update(Long id, WorkoutDto dto) {
        Workout existingWorkout = workoutRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        existingWorkout.setName(dto.getName());
        if (dto.getWorkoutDate() != null) {
            existingWorkout.setWorkoutDate(dto.getWorkoutDate());
        }

        if (dto.getUserId() != null && !dto.getUserId().equals(existingWorkout.getUser().getId())) {
            User newUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("New user not found"));
            existingWorkout.setUser(newUser);
        }

        return workoutMapper.toDto(workoutRepository.save(existingWorkout));
    }

    @Transactional
    public void delete(Long id) {
        workoutRepository.deleteById(id);
    }

    private Workout buildWorkout(WorkoutDto dto, User user) {
        return Workout.builder()
            .name(dto.getName())
            .workoutDate(dto.getWorkoutDate() != null ? dto.getWorkoutDate() : LocalDateTime.now())
            .user(user)
            .build();
    }

    private WorkoutDto createBulkWorkout(WorkoutDto dto) {
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new BulkWorkoutDemoException("Bulk demo failed because user was not found: "
                + dto.getUserId()));

        return workoutMapper.toDto(workoutRepository.save(buildWorkout(dto, user)));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateBulkWorkout(WorkoutDto dto) {
        if (dto.getName() != null && "FAIL".equals(dto.getName().trim().toUpperCase(Locale.ROOT))) {
            throw new BulkWorkoutDemoException("Bulk demo failed on workout name: FAIL");
        }
    }
}
