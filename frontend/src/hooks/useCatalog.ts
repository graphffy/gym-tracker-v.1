import { useQuery } from '@tanstack/react-query';
import { categoryApi } from '../api/categoryApi';
import { exerciseApi } from '../api/exerciseApi';
import { userApi } from '../api/userApi';
import { workoutApi } from '../api/workoutApi';
import { workoutSetApi } from '../api/workoutSetApi';

export function useCatalog() {
  const categories = useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll });
  const exercises = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const users = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const workouts = useQuery({ queryKey: ['workouts'], queryFn: workoutApi.getAll });
  const sets = useQuery({ queryKey: ['sets'], queryFn: workoutSetApi.getAll });

  return {
    categories: categories.data ?? [],
    exercises: exercises.data ?? [],
    users: users.data ?? [],
    workouts: workouts.data ?? [],
    sets: sets.data ?? [],
    isLoading: categories.isLoading || exercises.isLoading || users.isLoading || workouts.isLoading || sets.isLoading,
    isError: categories.isError || exercises.isError || users.isError || workouts.isError || sets.isError,
  };
}
