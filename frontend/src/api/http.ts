import axios, { AxiosError } from 'axios';
import type { ApiErrorResponse } from './types';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const response = (error as AxiosError<ApiErrorResponse>).response?.data;
    if (response?.validationErrors?.length) {
      return response.validationErrors.map((item) => item.message).join('. ');
    }
    return response?.message ?? error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Что-то пошло не так. Попробуйте еще раз.';
}
