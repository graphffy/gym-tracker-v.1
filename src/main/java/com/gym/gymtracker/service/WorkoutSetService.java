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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutSetService.class);

    private final WorkoutSetRepository workoutSetRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSetMapper workoutSetMapper;
    private final ConcurrentMap<WorkoutSetSearchCacheKey, Page<WorkoutSetDto>> workoutSetSearchIndex =
        new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public List<WorkoutSetDto> findAll() {
        return workoutSetMapper.toDtoList(workoutSetRepository.findAll());
    }

    @Transactional(readOnly = true)
    public WorkoutSetDto findById(Long id) {
        return workoutSetRepository.findById(id)
            .map(workoutSetMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    @Transactional
    public WorkoutSetDto create(WorkoutSetDto dto) {
        WorkoutSet workoutSet = buildWorkoutSet(
            dto.getWeight(),
            dto.getReps(),
            getWorkoutOrThrow(dto.getWorkoutId()),
            getExerciseOrThrow(dto.getExerciseId()));

        WorkoutSetDto created = workoutSetMapper.toDto(workoutSetRepository.save(workoutSet));
        invalidateWorkoutSetSearchIndex();
        return created;
    }

    public List<WorkoutSetDto> createBulkNonTransactional(BulkWorkoutSetRequestDto request) {
        Workout workout = getWorkoutOrThrow(request.getWorkoutId());
        Map<Long, Exercise> exercisesById = getExercisesById(request.getSets());

        List<WorkoutSetDto> createdSets = request.getSets().stream()
            .map(item -> createAndMapSingleSet(workout, exercisesById, item))
            .toList();

        invalidateWorkoutSetSearchIndex();
        return createdSets;
    }

    @Transactional
    public List<WorkoutSetDto> createBulkTransactional(BulkWorkoutSetRequestDto request) {
        Workout workout = getWorkoutOrThrow(request.getWorkoutId());
        Map<Long, Exercise> exercisesById = getExercisesById(request.getSets());

        List<WorkoutSet> setsToSave = request.getSets().stream()
            .map(item -> buildWorkoutSet(
                item.getWeight(),
                item.getReps(),
                workout,
                getExerciseFromMapOrThrow(exercisesById, item.getExerciseId())))
            .toList();

        List<WorkoutSetDto> createdSets = workoutSetRepository.saveAll(setsToSave).stream()
            .map(workoutSetMapper::toDto)
            .toList();

        invalidateWorkoutSetSearchIndex();
        return createdSets;
    }

    @Transactional
    public WorkoutSetDto update(Long id, WorkoutSetDto dto) {
        WorkoutSet existingSet = workoutSetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Set not found"));

        existingSet.setWeight(dto.getWeight());
        existingSet.setReps(dto.getReps());

        if (dto.getWorkoutId() != null && !dto.getWorkoutId().equals(existingSet.getWorkout().getId())) {
            existingSet.setWorkout(getWorkoutOrThrow(dto.getWorkoutId()));
        }

        if (dto.getExerciseId() != null && !dto.getExerciseId().equals(existingSet.getExercise().getId())) {
            existingSet.setExercise(getExerciseOrThrow(dto.getExerciseId()));
        }

        WorkoutSetDto updated = workoutSetMapper.toDto(workoutSetRepository.save(existingSet));
        invalidateWorkoutSetSearchIndex();
        return updated;
    }

    @Transactional
    public void delete(Long id) {
        workoutSetRepository.deleteById(id);
        invalidateWorkoutSetSearchIndex();
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSetDto> searchByUserAndExerciseJpql(String username, String exerciseName, int page, int size) {
        WorkoutSetSearchCacheKey key = new WorkoutSetSearchCacheKey(username, exerciseName, page, size, false);
        Page<WorkoutSetDto> cachedResult = workoutSetSearchIndex.get(key);
        if (cachedResult != null) {
            LOGGER.debug("WorkoutSet search cache HIT (JPQL): {}", key);
            return cachedResult;
        }
        LOGGER.debug("WorkoutSet search cache MISS (JPQL): {}", key);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<WorkoutSet> pageResult =
            workoutSetRepository.searchByUserAndExerciseJpql(username, exerciseName, pageable);
        Page<WorkoutSetDto> dtoPage = mapToDtoPage(pageResult, pageable);
        Page<WorkoutSetDto> existingResult = workoutSetSearchIndex.putIfAbsent(key, dtoPage);
        if (existingResult != null) {
            LOGGER.debug("WorkoutSet search cache filled concurrently (JPQL), using existing value: {}", key);
            return existingResult;
        }

        return dtoPage;
    }

    @Transactional(readOnly = true)
    public Page<WorkoutSetDto> searchByUserAndExerciseNative(String username, String exerciseName, int page, int size) {
        WorkoutSetSearchCacheKey key = new WorkoutSetSearchCacheKey(username, exerciseName, page, size, true);
        Page<WorkoutSetDto> cachedResult = workoutSetSearchIndex.get(key);
        if (cachedResult != null) {
            LOGGER.debug("WorkoutSet search cache HIT (NATIVE): {}", key);
            return cachedResult;
        }
        LOGGER.debug("WorkoutSet search cache MISS (NATIVE): {}", key);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<WorkoutSet> pageResult =
            workoutSetRepository.searchByUserAndExerciseNative(username, exerciseName, pageable);
        Page<WorkoutSetDto> dtoPage = mapToDtoPage(pageResult, pageable);
        Page<WorkoutSetDto> existingResult = workoutSetSearchIndex.putIfAbsent(key, dtoPage);
        if (existingResult != null) {
            LOGGER.debug("WorkoutSet search cache filled concurrently (NATIVE), using existing value: {}", key);
            return existingResult;
        }

        return dtoPage;
    }

    private Page<WorkoutSetDto> mapToDtoPage(Page<WorkoutSet> pageResult, Pageable pageable) {
        List<WorkoutSetDto> content = pageResult.getContent()
            .stream()
            .map(workoutSetMapper::toDto)
            .toList();
        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    private Workout getWorkoutOrThrow(Long workoutId) {
        return workoutRepository.findById(workoutId)
            .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));
    }

    private Exercise getExerciseOrThrow(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
    }

    private Map<Long, Exercise> getExercisesById(List<BulkWorkoutSetItemDto> items) {
        Set<Long> exerciseIds = items.stream()
            .map(BulkWorkoutSetItemDto::getExerciseId)
            .collect(Collectors.toSet());

        Map<Long, Exercise> exercisesById = exerciseRepository.findAllById(exerciseIds).stream()
            .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        exerciseIds.stream()
            .filter(id -> !exercisesById.containsKey(id))
            .findFirst()
            .ifPresent(id -> {
                throw new ResourceNotFoundException("Exercise not found");
            });

        return exercisesById;
    }

    private Exercise getExerciseFromMapOrThrow(Map<Long, Exercise> exercisesById, Long exerciseId) {
        return java.util.Optional.ofNullable(exercisesById.get(exerciseId))
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
    }

    private WorkoutSet buildWorkoutSet(Double weight, Integer reps, Workout workout, Exercise exercise) {
        return WorkoutSet.builder()
            .weight(weight)
            .reps(reps)
            .workout(workout)
            .exercise(exercise)
            .build();
    }

    private WorkoutSetDto createAndMapSingleSet(Workout workout, Map<Long, Exercise> exercisesById,
                                                BulkWorkoutSetItemDto item) {
        WorkoutSet savedSet = workoutSetRepository.save(buildWorkoutSet(
            item.getWeight(),
            item.getReps(),
            workout,
            getExerciseFromMapOrThrow(exercisesById, item.getExerciseId())));
        return workoutSetMapper.toDto(savedSet);
    }

    private void invalidateWorkoutSetSearchIndex() {
        LOGGER.debug("WorkoutSet search cache invalidated. Entries before clear: {}", workoutSetSearchIndex.size());
        workoutSetSearchIndex.clear();
    }

    private static final class WorkoutSetSearchCacheKey {
        private final String username;
        private final String exerciseName;
        private final int page;
        private final int size;
        private final boolean nativeQuery;

        private WorkoutSetSearchCacheKey(String username, String exerciseName, int page, int size,
                                         boolean nativeQuery) {
            this.username = username;
            this.exerciseName = exerciseName;
            this.page = page;
            this.size = size;
            this.nativeQuery = nativeQuery;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            WorkoutSetSearchCacheKey that = (WorkoutSetSearchCacheKey) obj;
            return page == that.page
                && size == that.size
                && nativeQuery == that.nativeQuery
                && Objects.equals(username, that.username)
                && Objects.equals(exerciseName, that.exerciseName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, exerciseName, page, size, nativeQuery);
        }

        @Override
        public String toString() {
            return "WorkoutSetSearchCacheKey{"
                + "username='" + username + '\''
                + ", exerciseName='" + exerciseName + '\''
                + ", page=" + page
                + ", size=" + size
                + ", nativeQuery=" + nativeQuery
                + '}';
        }
    }
}
