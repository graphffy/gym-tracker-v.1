import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { CategoriesPage } from './pages/CategoriesPage';
import { ExercisesPage } from './pages/ExercisesPage';
import { UsersPage } from './pages/UsersPage';
import { WorkoutsPage } from './pages/WorkoutsPage';
import { WorkoutDetailPage } from './pages/WorkoutDetailPage';
import { WorkoutSetsPage } from './pages/WorkoutSetsPage';
import './styles/app.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'categories', element: <CategoriesPage /> },
      { path: 'exercises', element: <ExercisesPage /> },
      { path: 'users', element: <UsersPage /> },
      { path: 'workouts', element: <WorkoutsPage /> },
      { path: 'workouts/:workoutId', element: <WorkoutDetailPage /> },
      { path: 'sets', element: <WorkoutSetsPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </React.StrictMode>,
);
