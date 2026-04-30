import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Plus } from 'lucide-react';
import { workoutApi } from '../api/workoutApi';
import { userApi } from '../api/userApi';
import { exerciseApi } from '../api/exerciseApi';
import { workoutSetApi, type WorkoutSetPayload } from '../api/workoutSetApi';
import { categoryApi } from '../api/categoryApi';
import { getApiErrorMessage } from '../api/http';
import { EmptyState } from '../components/EmptyState';
import { EntityCard } from '../components/EntityCard';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { exerciseName, formatDate, userName } from './helpers';
import { WorkoutSetForm } from './WorkoutSetsPage';

export function WorkoutDetailPage() {
  const queryClient = useQueryClient();
  const { workoutId } = useParams();
  const id = Number(workoutId);
  const [isCreatingSet, setIsCreatingSet] = useState(false);
  const [error, setError] = useState('');
  const workoutQuery = useQuery({ queryKey: ['workouts', id], queryFn: () => workoutApi.getById(id), enabled: Number.isFinite(id) });
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const setsQuery = useQuery({ queryKey: ['sets'], queryFn: workoutSetApi.getAll });
  const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll });

  const saveSetMutation = useMutation({
    mutationFn: (payload: WorkoutSetPayload) => workoutSetApi.create(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['sets'] });
      setIsCreatingSet(false);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (workoutQuery.isLoading || usersQuery.isLoading || exercisesQuery.isLoading || setsQuery.isLoading || categoriesQuery.isLoading) {
    return <LoadingState />;
  }

  if (workoutQuery.isError || usersQuery.isError || exercisesQuery.isError || setsQuery.isError || categoriesQuery.isError || !workoutQuery.data) {
    return <ErrorState message="Тренировка не загрузилась." />;
  }

  const workout = workoutQuery.data;
  const users = usersQuery.data ?? [];
  const exercises = exercisesQuery.data ?? [];
  const categories = categoriesQuery.data ?? [];
  const sets = (setsQuery.data ?? []).filter((set) => set.workoutId === workout.id);

  return (
    <div className="page-stack">
      <Link className="ghost-button compact" to="/workouts">
        <ArrowLeft size={18} />
        К тренировкам
      </Link>

      <section className="detail-hero">
        <span className="eyebrow">Тренировка</span>
        <h1>{workout.name}</h1>
        <p>{userName(workout.userId, users)} · {formatDate(workout.workoutDate)}</p>
        <div className="detail-stats">
          <span>{sets.length} подходов</span>
          <span>{sets.reduce((sum, set) => sum + set.reps, 0)} повторений</span>
        </div>
      </section>

      <section>
        <div className="section-heading">
          <h2>Подходы</h2>
        </div>
        {error ? <ErrorState message={error} /> : null}
        <div className="card-grid">
          {sets.length ? (
            sets.map((set) => (
              <EntityCard
                key={set.id}
                title={set.name || exerciseName(set.exerciseId, exercises)}
                subtitle={`${set.weight} кг · ${set.reps} повторений`}
                meta={set.name ? <span>{exerciseName(set.exerciseId, exercises)}</span> : undefined}
              />
            ))
          ) : (
            <EmptyState title="Пока нет подходов" text="Добавьте подходы на странице подходов." />
          )}
          <button type="button" className="add-set-card" onClick={() => setIsCreatingSet(true)} aria-label="Добавить подход">
            <Plus size={34} />
          </button>
        </div>
      </section>

      {isCreatingSet ? (
        <WorkoutSetForm
          set={null}
          initialWorkoutId={String(workout.id)}
          workouts={[workout]}
          exercises={exercises}
          users={users}
          categories={categories}
          isBusy={saveSetMutation.isPending}
          hideWorkoutQuickCreate
          onClose={() => setIsCreatingSet(false)}
          onSubmit={(payload) => saveSetMutation.mutate(payload)}
        />
      ) : null}
    </div>
  );
}
