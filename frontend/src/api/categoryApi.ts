import { apiClient } from './http';
import type { Category } from './types';

export type CategoryPayload = Pick<Category, 'name'>;

export const categoryApi = {
  getAll: async () => (await apiClient.get<Category[]>('/categories')).data,
  getById: async (id: number) => (await apiClient.get<Category>(`/categories/${id}`)).data,
  create: async (payload: CategoryPayload) => (await apiClient.post<Category>('/categories', payload)).data,
  update: async (id: number, payload: CategoryPayload) =>
    (await apiClient.put<Category>(`/categories/${id}`, payload)).data,
  delete: async (id: number) => {
    await apiClient.delete(`/categories/${id}`);
  },
};
