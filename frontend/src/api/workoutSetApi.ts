import { apiClient } from './http';
import type { PageResponse, WorkoutSet } from './types';

export type WorkoutSetPayload = Omit<WorkoutSet, 'id'>;

export const workoutSetApi = {
  getAll: async () => (await apiClient.get<WorkoutSet[]>('/sets')).data,
  getById: async (id: number) => (await apiClient.get<WorkoutSet>(`/sets/${id}`)).data,
  search: async (params: { username?: string; exerciseName?: string; page?: number; size?: number }) =>
    (await apiClient.get<PageResponse<WorkoutSet>>('/sets/search/jpql', { params })).data,
  create: async (payload: WorkoutSetPayload) => (await apiClient.post<WorkoutSet>('/sets', payload)).data,
  update: async (id: number, payload: WorkoutSetPayload) =>
    (await apiClient.put<WorkoutSet>(`/sets/${id}`, payload)).data,
  delete: async (id: number) => {
    await apiClient.delete(`/sets/${id}`);
  },
};
