package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.UserDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.UserMapper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findAllMapsAllUsers() {
        User user = User.builder().id(1L).username("ivan").email("i@mail.com").workouts(new ArrayList<>()).build();
        UserDto dto = UserDto.builder().id(1L).username("ivan").email("i@mail.com").build();

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @Test
    void findByUsernameReturnsMappedUser() {
        User user = User.builder().id(1L).username("ivan").email("i@mail.com").workouts(new ArrayList<>()).build();
        UserDto dto = UserDto.builder().id(1L).username("ivan").email("i@mail.com").build();

        when(userRepository.findByUsername("ivan")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        assertSame(dto, userService.findByUsername("ivan"));
    }

    @Test
    void findByUsernameThrowsWhenUserMissing() {
        when(userRepository.findByUsername("ivan")).thenReturn(null);

        Executable action = () -> userService.findByUsername("ivan");

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void findByIdReturnsMappedUser() {
        User user = User.builder().id(2L).username("petr").email("p@mail.com").workouts(new ArrayList<>()).build();
        UserDto dto = UserDto.builder().id(2L).username("petr").email("p@mail.com").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        assertSame(dto, userService.findById(2L));
    }

    @Test
    void findByIdThrowsWhenUserMissing() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Executable action = () -> userService.findById(2L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createMapsSavesAndReturnsUser() {
        UserDto request = UserDto.builder().username("mike").email("m@mail.com").build();
        User entity = User.builder().username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();
        User saved = User.builder().id(3L).username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();
        UserDto response = UserDto.builder().id(3L).username("mike").email("m@mail.com").build();

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(response);

        assertSame(response, userService.create(request));
    }

    @Test
    void createWithDirtyTestThrowsAfterSavingUser() {
        UserDto request = UserDto.builder().username("mike").email("m@mail.com").build();
        User entity = User.builder().username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();
        User saved = User.builder().id(3L).username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);

        Executable action = () -> userService.createWithDirtyTest(request, true);

        assertThrows(IllegalStateException.class, action);
    }

    @Test
    void createWithDirtyTestCreatesWorkoutWhenNoError() {
        UserDto request = UserDto.builder().username("mike").email("m@mail.com").build();
        User entity = User.builder().username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();
        User saved = User.builder().id(3L).username("mike").email("m@mail.com").workouts(new ArrayList<>()).build();
        UserDto response = UserDto.builder().id(3L).username("mike").email("m@mail.com").build();

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(response);

        assertSame(response, userService.createWithDirtyTest(request, false));
        verify(workoutRepository).save(any(Workout.class));
    }

    @Test
    void createWithFirstWorkoutReturnsMappedUser() {
        UserDto request = UserDto.builder().username("kate").email("k@mail.com").build();
        User entity = User.builder().username("kate").email("k@mail.com").workouts(new ArrayList<>()).build();
        User saved = User.builder().id(4L).username("kate").email("k@mail.com").workouts(new ArrayList<>()).build();
        UserDto response = UserDto.builder().id(4L).username("kate").email("k@mail.com").build();

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(response);

        assertSame(response, userService.createWithFirstWorkout(request, false));
        assertEquals(1, entity.getWorkouts().size());
    }

    @Test
    void createWithFirstWorkoutThrowsWhenErrorRequested() {
        UserDto request = UserDto.builder().username("kate").email("k@mail.com").build();
        User entity = User.builder().username("kate").email("k@mail.com").workouts(new ArrayList<>()).build();
        User saved = User.builder().id(4L).username("kate").email("k@mail.com").workouts(new ArrayList<>()).build();

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);

        Executable action = () -> userService.createWithFirstWorkout(request, true);

        assertThrows(IllegalStateException.class, action);
        assertEquals(1, entity.getWorkouts().size());
    }

    @Test
    void deleteDelegatesToRepository() {
        userService.delete(9L);

        verify(userRepository).deleteById(9L);
    }

    @Test
    void updateChangesFieldsAndReturnsMappedUser() {
        User existing = User.builder().id(5L).username("old").email("old@mail.com").workouts(new ArrayList<>()).build();
        UserDto request = UserDto.builder().username("new").email("new@mail.com").build();
        UserDto response = UserDto.builder().id(5L).username("new").email("new@mail.com").build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toDto(existing)).thenReturn(response);

        assertSame(response, userService.update(5L, request));
        assertEquals("new", existing.getUsername());
        assertEquals("new@mail.com", existing.getEmail());
    }

    @Test
    void updateThrowsWhenUserMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        UserDto request = UserDto.builder().build();
        Executable action = () -> userService.update(5L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }
}
