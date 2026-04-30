import type { Category, Exercise, User, Workout, WorkoutSet } from '../api/types';

export const pageSize = 8;

export function includesText(value: string | null | undefined, query: string) {
  return (value ?? '').toLowerCase().includes(query.trim().toLowerCase());
}

export function formatDate(value?: string | null) {
  if (!value) {
    return 'Дата не выбрана';
  }

  return new Intl.DateTimeFormat('ru', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function toDateTimeInput(value?: string | null) {
  if (!value) {
    return '';
  }

  return value.slice(0, 16);
}

export function fromDateTimeInput(value: string) {
  return value ? `${value}:00` : null;
}

export function categoryNames(exercise: Exercise, categories: Category[]) {
  return exercise.categoryIds
    .map((categoryId) => categories.find((category) => category.id === categoryId)?.name)
    .filter(Boolean) as string[];
}

export function userName(userId: number, users: User[]) {
  return users.find((user) => user.id === userId)?.username ?? 'Пользователь не найден';
}

export function exerciseName(exerciseId: number, exercises: Exercise[]) {
  return exercises.find((exercise) => exercise.id === exerciseId)?.name ?? 'Упражнение не найдено';
}

export function workoutName(workoutId: number, workouts: Workout[]) {
  const workout = workouts.find((item) => item.id === workoutId);
  return workout ? `${workout.name} · ${formatDate(workout.workoutDate)}` : 'Тренировка не найдена';
}

export function workoutSetsForWorkout(workout: Workout, sets: WorkoutSet[]) {
  return sets.filter((set) => set.workoutId === workout.id);
}
