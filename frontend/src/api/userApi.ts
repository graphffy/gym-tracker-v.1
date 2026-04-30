import { apiClient } from './http';
import type { User } from './types';

export type UserPayload = Pick<User, 'username' | 'email'>;

export const userApi = {
  getAll: async () => (await apiClient.get<User[]>('/users')).data,
  getById: async (id: number) => (await apiClient.get<User>(`/users/${id}`)).data,
  searchByUsername: async (username: string) =>
    (await apiClient.get<User>('/users/search', { params: { username } })).data,
  create: async (payload: UserPayload) => (await apiClient.post<User>('/users', payload)).data,
  update: async (id: number, payload: UserPayload) => (await apiClient.put<User>(`/users/${id}`, payload)).data,
  delete: async (id: number) => {
    await apiClient.delete(`/users/${id}`);
  },
};
