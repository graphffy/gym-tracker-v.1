import { apiClient } from './http';
import type { Exercise } from './types';

export type ExercisePayload = Omit<Exercise, 'id'>;

export const exerciseApi = {
  getAll: async () => (await apiClient.get<Exercise[]>('/exercises')).data,
  getById: async (id: number) => (await apiClient.get<Exercise>(`/exercises/${id}`)).data,
  create: async (payload: ExercisePayload) => (await apiClient.post<Exercise>('/exercises', payload)).data,
  update: async (id: number, payload: ExercisePayload) =>
    (await apiClient.put<Exercise>(`/exercises/${id}`, payload)).data,
  delete: async (id: number) => {
    await apiClient.delete(`/exercises/${id}`);
  },
};
