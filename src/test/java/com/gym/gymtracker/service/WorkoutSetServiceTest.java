package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.BulkWorkoutSetItemDto;
import com.gym.gymtracker.dto.BulkWorkoutSetRequestDto;
import com.gym.gymtracker.dto.WorkoutSetDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.WorkoutSetMapper;
import com.gym.gymtracker.model.Exercise;
import com.gym.gymtracker.model.Workout;
import com.gym.gymtracker.model.WorkoutSet;
import com.gym.gymtracker.repository.ExerciseRepository;
import com.gym.gymtracker.repository.WorkoutRepository;
import com.gym.gymtracker.repository.WorkoutSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutSetServiceTest {

    @Mock
    private WorkoutSetRepository workoutSetRepository;

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutSetMapper workoutSetMapper;

    @InjectMocks
    private WorkoutSetService workoutSetService;

    @Test
    void findAllReturnsMappedSets() {
        List<WorkoutSet> sets = List.of(WorkoutSet.builder().id(1L).build());
        List<WorkoutSetDto> dtos = List.of(WorkoutSetDto.builder().id(1L).build());

        when(workoutSetRepository.findAll()).thenReturn(sets);
        when(workoutSetMapper.toDtoList(sets)).thenReturn(dtos);

        assertSame(dtos, workoutSetService.findAll());
    }

    @Test
    void findByIdReturnsMappedSet() {
        WorkoutSet set = WorkoutSet.builder().id(1L).build();
        WorkoutSetDto dto = WorkoutSetDto.builder().id(1L).build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(set));
        when(workoutSetMapper.toDto(set)).thenReturn(dto);

        assertSame(dto, workoutSetService.findById(1L));
    }

    @Test
    void findByIdThrowsWhenSetMissing() {
        when(workoutSetRepository.findById(1L)).thenReturn(Optional.empty());

        Executable action = () -> workoutSetService.findById(1L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createSavesAndReturnsMappedSet() {
        Workout workout = Workout.builder().id(10L).build();
        Exercise exercise = Exercise.builder().id(20L).build();
        WorkoutSet saved = WorkoutSet.builder().id(1L).workout(workout).exercise(exercise).weight(80.0).reps(10).build();
        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(10L).exerciseId(20L).weight(80.0).reps(10).build();
        WorkoutSetDto response = WorkoutSetDto.builder().id(1L).workoutId(10L).exerciseId(20L).weight(80.0).reps(10).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(20L)).thenReturn(Optional.of(exercise));
        when(workoutSetRepository.save(any(WorkoutSet.class))).thenReturn(saved);
        when(workoutSetMapper.toDto(saved)).thenReturn(response);

        assertSame(response, workoutSetService.create(request));
    }

    @Test
    void createThrowsWhenWorkoutMissing() {
        when(workoutRepository.findById(10L)).thenReturn(Optional.empty());
        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(10L).exerciseId(20L).weight(80.0).reps(10).build();
        Executable action = () -> workoutSetService.create(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createThrowsWhenExerciseMissing() {
        Workout workout = Workout.builder().id(10L).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findById(20L)).thenReturn(Optional.empty());

        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(10L).exerciseId(20L).weight(80.0).reps(10).build();
        Executable action = () -> workoutSetService.create(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createBulkNonTransactionalSavesEachItemSeparately() {
        Workout workout = Workout.builder().id(10L).build();
        Exercise firstExercise = Exercise.builder().id(20L).build();
        Exercise secondExercise = Exercise.builder().id(21L).build();
        BulkWorkoutSetRequestDto request = bulkRequest(10L, List.of(
            item(20L, 80.0, 10),
            item(21L, 60.0, 12)));
        WorkoutSet firstSaved = WorkoutSet.builder().id(1L).workout(workout).exercise(firstExercise).build();
        WorkoutSet secondSaved = WorkoutSet.builder().id(2L).workout(workout).exercise(secondExercise).build();
        WorkoutSetDto firstDto = WorkoutSetDto.builder().id(1L).build();
        WorkoutSetDto secondDto = WorkoutSetDto.builder().id(2L).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findAllById(any())).thenReturn(List.of(firstExercise, secondExercise));
        when(workoutSetRepository.save(any(WorkoutSet.class))).thenReturn(firstSaved, secondSaved);
        when(workoutSetMapper.toDto(firstSaved)).thenReturn(firstDto);
        when(workoutSetMapper.toDto(secondSaved)).thenReturn(secondDto);

        List<WorkoutSetDto> result = workoutSetService.createBulkNonTransactional(request);

        assertEquals(List.of(firstDto, secondDto), result);
        verify(workoutSetRepository, times(2)).save(any(WorkoutSet.class));
    }

    @Test
    void createBulkNonTransactionalThrowsWhenWorkoutMissing() {
        when(workoutRepository.findById(10L)).thenReturn(Optional.empty());
        BulkWorkoutSetRequestDto request = bulkRequest(10L, List.of(item(20L, 80.0, 10)));
        Executable action = () -> workoutSetService.createBulkNonTransactional(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createBulkNonTransactionalThrowsWhenExerciseMissing() {
        Workout workout = Workout.builder().id(10L).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findAllById(any())).thenReturn(List.of());

        BulkWorkoutSetRequestDto request = bulkRequest(10L, List.of(item(20L, 80.0, 10)));
        Executable action = () -> workoutSetService.createBulkNonTransactional(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createBulkTransactionalSavesAllAndMapsResult() {
        Workout workout = Workout.builder().id(10L).build();
        Exercise exercise = Exercise.builder().id(20L).build();
        WorkoutSet saved = WorkoutSet.builder().id(1L).workout(workout).exercise(exercise).build();
        WorkoutSetDto dto = WorkoutSetDto.builder().id(1L).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findAllById(any())).thenReturn(List.of(exercise));
        when(workoutSetRepository.saveAll(any())).thenReturn(List.of(saved));
        when(workoutSetMapper.toDto(saved)).thenReturn(dto);

        List<WorkoutSetDto> result = workoutSetService.createBulkTransactional(
            bulkRequest(10L, List.of(item(20L, 80.0, 10))));

        assertEquals(List.of(dto), result);
    }

    @Test
    void createBulkTransactionalThrowsWhenWorkoutMissing() {
        when(workoutRepository.findById(10L)).thenReturn(Optional.empty());
        BulkWorkoutSetRequestDto request = bulkRequest(10L, List.of(item(20L, 80.0, 10)));
        Executable action = () -> workoutSetService.createBulkTransactional(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createBulkTransactionalThrowsWhenExerciseMissing() {
        Workout workout = Workout.builder().id(10L).build();

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));
        when(exerciseRepository.findAllById(any())).thenReturn(List.of());

        BulkWorkoutSetRequestDto request = bulkRequest(10L, List.of(item(20L, 80.0, 10)));
        Executable action = () -> workoutSetService.createBulkTransactional(request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void updateChangesWorkoutAndExerciseWhenIdsDiffer() {
        Workout oldWorkout = Workout.builder().id(10L).build();
        Workout newWorkout = Workout.builder().id(11L).build();
        Exercise oldExercise = Exercise.builder().id(20L).build();
        Exercise newExercise = Exercise.builder().id(21L).build();
        WorkoutSet existing = WorkoutSet.builder()
            .id(1L)
            .workout(oldWorkout)
            .exercise(oldExercise)
            .weight(70.0)
            .reps(8)
            .build();
        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(11L).exerciseId(21L).weight(90.0).reps(10).build();
        WorkoutSetDto response = WorkoutSetDto.builder().id(1L).workoutId(11L).exerciseId(21L).weight(90.0).reps(10).build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workoutRepository.findById(11L)).thenReturn(Optional.of(newWorkout));
        when(exerciseRepository.findById(21L)).thenReturn(Optional.of(newExercise));
        when(workoutSetRepository.save(existing)).thenReturn(existing);
        when(workoutSetMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutSetService.update(1L, request));
        assertSame(newWorkout, existing.getWorkout());
        assertSame(newExercise, existing.getExercise());
        assertEquals(90.0, existing.getWeight());
        assertEquals(10, existing.getReps());
    }

    @Test
    void updateKeepsWorkoutAndExerciseWhenIdsSameOrNull() {
        Workout workout = Workout.builder().id(10L).build();
        Exercise exercise = Exercise.builder().id(20L).build();
        WorkoutSet existing = WorkoutSet.builder().id(1L).workout(workout).exercise(exercise).weight(70.0).reps(8).build();
        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(10L).exerciseId(null).weight(75.0).reps(9).build();
        WorkoutSetDto response = WorkoutSetDto.builder().id(1L).workoutId(10L).exerciseId(20L).weight(75.0).reps(9).build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workoutSetRepository.save(existing)).thenReturn(existing);
        when(workoutSetMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutSetService.update(1L, request));
        assertSame(workout, existing.getWorkout());
        assertSame(exercise, existing.getExercise());
    }

    @Test
    void updateKeepsWorkoutWhenWorkoutIdIsNullAndExerciseWhenIdIsSame() {
        Workout workout = Workout.builder().id(10L).build();
        Exercise exercise = Exercise.builder().id(20L).build();
        WorkoutSet existing = WorkoutSet.builder().id(1L).workout(workout).exercise(exercise).weight(70.0).reps(8).build();
        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(null).exerciseId(20L).weight(75.0).reps(9).build();
        WorkoutSetDto response = WorkoutSetDto.builder().id(1L).workoutId(10L).exerciseId(20L).weight(75.0).reps(9).build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workoutSetRepository.save(existing)).thenReturn(existing);
        when(workoutSetMapper.toDto(existing)).thenReturn(response);

        assertSame(response, workoutSetService.update(1L, request));
        assertSame(workout, existing.getWorkout());
        assertSame(exercise, existing.getExercise());
    }

    @Test
    void updateThrowsWhenSetMissing() {
        when(workoutSetRepository.findById(1L)).thenReturn(Optional.empty());
        WorkoutSetDto request = WorkoutSetDto.builder().build();
        Executable action = () -> workoutSetService.update(1L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void updateThrowsWhenNewWorkoutMissing() {
        WorkoutSet existing = WorkoutSet.builder()
            .id(1L)
            .workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build())
            .build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workoutRepository.findById(11L)).thenReturn(Optional.empty());

        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(11L).exerciseId(20L).weight(75.0).reps(9).build();
        Executable action = () -> workoutSetService.update(1L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void updateThrowsWhenNewExerciseMissing() {
        WorkoutSet existing = WorkoutSet.builder()
            .id(1L)
            .workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build())
            .build();

        when(workoutSetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.findById(21L)).thenReturn(Optional.empty());

        WorkoutSetDto request = WorkoutSetDto.builder().workoutId(10L).exerciseId(21L).weight(75.0).reps(9).build();
        Executable action = () -> workoutSetService.update(1L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void deleteDelegatesToRepository() {
        workoutSetService.delete(3L);

        verify(workoutSetRepository).deleteById(3L);
    }

    @Test
    void searchByUserAndExerciseJpqlReturnsCachedResultOnSecondCall() {
        WorkoutSet set = WorkoutSet.builder().id(1L).workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build()).build();
        WorkoutSetDto dto = WorkoutSetDto.builder().id(1L).build();
        Page<WorkoutSet> page = new PageImpl<>(List.of(set));

        when(workoutSetRepository.searchByUserAndExerciseJpql(eq("ivan"), eq("bench"), any(Pageable.class)))
            .thenReturn(page);
        when(workoutSetMapper.toDto(set)).thenReturn(dto);

        Page<WorkoutSetDto> first = workoutSetService.searchByUserAndExerciseJpql("ivan", "bench", 0, 10);
        Page<WorkoutSetDto> second = workoutSetService.searchByUserAndExerciseJpql("ivan", "bench", 0, 10);

        assertEquals(1, first.getTotalElements());
        assertSame(first, second);
    }

    @Test
    void searchByUserAndExerciseJpqlReturnsExistingValueFromPutIfAbsent() throws Exception {
        WorkoutSet set = WorkoutSet.builder().id(1L).workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build()).build();
        WorkoutSetDto computedDto = WorkoutSetDto.builder().id(1L).build();
        WorkoutSetDto existingDto = WorkoutSetDto.builder().id(99L).build();
        Page<WorkoutSet> page = new PageImpl<>(List.of(set));
        Page<WorkoutSetDto> existingPage = new PageImpl<>(List.of(existingDto));

        setCustomCacheMap(new ReturningExistingMap(createKey("ivan", "bench", 0, 10, false), existingPage));
        when(workoutSetRepository.searchByUserAndExerciseJpql(eq("ivan"), eq("bench"), any(Pageable.class)))
            .thenReturn(page);
        when(workoutSetMapper.toDto(set)).thenReturn(computedDto);

        Page<WorkoutSetDto> result = workoutSetService.searchByUserAndExerciseJpql("ivan", "bench", 0, 10);

        assertSame(existingPage, result);
    }

    @Test
    void searchByUserAndExerciseNativeReturnsCachedResultOnSecondCall() {
        WorkoutSet set = WorkoutSet.builder().id(1L).workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build()).build();
        WorkoutSetDto dto = WorkoutSetDto.builder().id(1L).build();
        Page<WorkoutSet> page = new PageImpl<>(List.of(set));

        when(workoutSetRepository.searchByUserAndExerciseNative(eq("ivan"), eq("bench"), any(Pageable.class)))
            .thenReturn(page);
        when(workoutSetMapper.toDto(set)).thenReturn(dto);

        Page<WorkoutSetDto> first = workoutSetService.searchByUserAndExerciseNative("ivan", "bench", 0, 10);
        Page<WorkoutSetDto> second = workoutSetService.searchByUserAndExerciseNative("ivan", "bench", 0, 10);

        assertEquals(1, first.getTotalElements());
        assertSame(first, second);
    }

    @Test
    void searchByUserAndExerciseNativeReturnsExistingValueFromPutIfAbsent() throws Exception {
        WorkoutSet set = WorkoutSet.builder().id(1L).workout(Workout.builder().id(10L).build())
            .exercise(Exercise.builder().id(20L).build()).build();
        WorkoutSetDto computedDto = WorkoutSetDto.builder().id(1L).build();
        WorkoutSetDto existingDto = WorkoutSetDto.builder().id(99L).build();
        Page<WorkoutSet> page = new PageImpl<>(List.of(set));
        Page<WorkoutSetDto> existingPage = new PageImpl<>(List.of(existingDto));

        setCustomCacheMap(new ReturningExistingMap(createKey("ivan", "bench", 0, 10, true), existingPage));
        when(workoutSetRepository.searchByUserAndExerciseNative(eq("ivan"), eq("bench"), any(Pageable.class)))
            .thenReturn(page);
        when(workoutSetMapper.toDto(set)).thenReturn(computedDto);

        Page<WorkoutSetDto> result = workoutSetService.searchByUserAndExerciseNative("ivan", "bench", 0, 10);

        assertSame(existingPage, result);
    }

    @Test
    void workoutSetSearchCacheKeyMethodsAreCovered() throws Exception {
        Object first = createKey("ivan", "bench", 0, 10, false);
        Object same = createKey("ivan", "bench", 0, 10, false);
        Object different = createKey("petr", "row", 1, 5, true);
        Object usernameDifferent = createKey("petr", "bench", 0, 10, false);
        Object exerciseDifferent = createKey("ivan", "row", 0, 10, false);
        Object pageDifferent = createKey("ivan", "bench", 1, 10, false);
        Object sizeDifferent = createKey("ivan", "bench", 0, 11, false);
        Object nativeDifferent = createKey("ivan", "bench", 0, 10, true);
        Method hashCodeMethod = first.getClass().getDeclaredMethod("hashCode");
        Method toStringMethod = first.getClass().getDeclaredMethod("toString");
        hashCodeMethod.setAccessible(true);
        toStringMethod.setAccessible(true);

        assertSame(first, first);
        assertEquals(first, same);
        assertNotEquals(first, different);
        assertNotEquals(first, usernameDifferent);
        assertNotEquals(first, exerciseDifferent);
        assertNotEquals(first, pageDifferent);
        assertNotEquals(first, sizeDifferent);
        assertNotEquals(first, nativeDifferent);
        assertNotEquals(null, first);
        assertNotEquals("other", first);
        assertEquals(hashCodeMethod.invoke(first), hashCodeMethod.invoke(same));
        assertNotEquals(hashCodeMethod.invoke(first), hashCodeMethod.invoke(different));
        assertEquals(
            "WorkoutSetSearchCacheKey{username='ivan', exerciseName='bench', page=0, size=10, nativeQuery=false}",
            toStringMethod.invoke(first));
    }

    @Test
    void getExerciseFromMapOrThrowThrowsWhenExerciseMissing() throws Exception {
        Method method = WorkoutSetService.class.getDeclaredMethod("getExerciseFromMapOrThrow", Map.class, Long.class);
        method.setAccessible(true);
        Executable action = () -> invokeGetExerciseFromMapOrThrow(method, Map.of(), 20L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    private BulkWorkoutSetRequestDto bulkRequest(Long workoutId, List<BulkWorkoutSetItemDto> items) {
        return BulkWorkoutSetRequestDto.builder().workoutId(workoutId).sets(items).build();
    }

    private BulkWorkoutSetItemDto item(Long exerciseId, Double weight, Integer reps) {
        return BulkWorkoutSetItemDto.builder().exerciseId(exerciseId).weight(weight).reps(reps).build();
    }

    private Object createKey(String username, String exerciseName, int page, int size, boolean nativeQuery)
        throws Exception {
        Class<?> keyClass = Class.forName("com.gym.gymtracker.service.WorkoutSetService$WorkoutSetSearchCacheKey");
        Constructor<?> constructor =
            keyClass.getDeclaredConstructor(String.class, String.class, int.class, int.class, boolean.class);
        constructor.setAccessible(true);
        return constructor.newInstance(username, exerciseName, page, size, nativeQuery);
    }

    private void setCustomCacheMap(ConcurrentMap<Object, Page<WorkoutSetDto>> map) throws Exception {
        Field field = WorkoutSetService.class.getDeclaredField("workoutSetSearchIndex");
        field.setAccessible(true);
        field.set(workoutSetService, map);
    }

    private void invokeGetExerciseFromMapOrThrow(Method method, Map<Long, Exercise> exercisesById, Long exerciseId)
        throws Throwable {
        try {
            method.invoke(workoutSetService, exercisesById, exerciseId);
        } catch (ReflectiveOperationException ex) {
            throw ex.getCause();
        }
    }

    private static final class ReturningExistingMap extends ConcurrentHashMap<Object, Page<WorkoutSetDto>> {
        private final Object targetKey;
        private final Page<WorkoutSetDto> existingPage;

        private ReturningExistingMap(Object targetKey, Page<WorkoutSetDto> existingPage) {
            this.targetKey = targetKey;
            this.existingPage = existingPage;
        }

        @Override
        public Page<WorkoutSetDto> get(Object key) {
            return null;
        }

        @Override
        public Page<WorkoutSetDto> putIfAbsent(Object key, Page<WorkoutSetDto> value) {
            if (targetKey.equals(key)) {
                return existingPage;
            }
            return super.putIfAbsent(key, value);
        }
    }
}
