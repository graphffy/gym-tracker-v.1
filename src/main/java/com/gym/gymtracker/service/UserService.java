package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.UserDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.UserMapper;
import com.gym.gymtracker.model.User;
import com.gym.gymtracker.model.Workout;
import com.gym.gymtracker.repository.UserRepository;
import com.gym.gymtracker.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
            .map(userMapper::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        return java.util.Optional.ofNullable(userRepository.findByUsername(username))
            .map(userMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto createWithDirtyTest(UserDto dto, boolean throwError) {
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        LOGGER.info("User saved during transaction demo with id={}", savedUser.getId());

        if (throwError) {
            LOGGER.warn("Forcing failure after user save in transaction demo");
            throw new IllegalStateException("Transaction demo failed after user save and before workout save");
        }

        Workout workout = Workout.builder()
            .name("Welcome workout")
            .user(savedUser)
            .build();
        workoutRepository.save(workout);

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto createWithFirstWorkout(UserDto dto, boolean makeError) {
        User user = userMapper.toEntity(dto);

        Workout firstWorkout = Workout.builder()
            .name("First workout")
            .workoutDate(LocalDateTime.now())
            .user(user)
            .build();

        user.getWorkouts().add(firstWorkout);

        User savedUser = userRepository.save(user);

        if (makeError) {
            throw new IllegalStateException("Transaction demo failed while creating the first workout");
        }

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }
}
