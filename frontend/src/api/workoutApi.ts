import { apiClient } from './http';
import type { Workout } from './types';

export type WorkoutPayload = Omit<Workout, 'id'>;

export const workoutApi = {
  getAll: async () => (await apiClient.get<Workout[]>('/workouts')).data,
  getById: async (id: number) => (await apiClient.get<Workout>(`/workouts/${id}`)).data,
  create: async (payload: WorkoutPayload) => (await apiClient.post<Workout>('/workouts', payload)).data,
  update: async (id: number, payload: WorkoutPayload) =>
    (await apiClient.put<Workout>(`/workouts/${id}`, payload)).data,
  delete: async (id: number) => {
    await apiClient.delete(`/workouts/${id}`);
  },
};
