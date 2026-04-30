export type Id = number;

export interface Category {
  id: Id;
  name: string;
}

export interface Exercise {
  id: Id;
  name: string;
  description?: string | null;
  categoryIds: Id[];
}

export interface User {
  id: Id;
  username: string;
  email: string;
  workouts?: Workout[];
}

export interface Workout {
  id: Id;
  name: string;
  workoutDate?: string | null;
  userId: Id;
}

export interface WorkoutSet {
  id: Id;
  name?: string | null;
  weight: number;
  reps: number;
  workoutId: Id;
  exerciseId: Id;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ApiValidationError {
  field: string;
  rejectedValue?: unknown;
  message: string;
}

export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: ApiValidationError[];
}
