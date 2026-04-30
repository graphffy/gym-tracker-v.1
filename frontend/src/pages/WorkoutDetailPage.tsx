import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import { workoutApi } from '../api/workoutApi';
import { userApi } from '../api/userApi';
import { exerciseApi } from '../api/exerciseApi';
import { workoutSetApi } from '../api/workoutSetApi';
import { EmptyState } from '../components/EmptyState';
import { EntityCard } from '../components/EntityCard';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { exerciseName, formatDate, userName } from './helpers';

export function WorkoutDetailPage() {
  const { workoutId } = useParams();
  const id = Number(workoutId);
  const workoutQuery = useQuery({ queryKey: ['workouts', id], queryFn: () => workoutApi.getById(id), enabled: Number.isFinite(id) });
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const setsQuery = useQuery({ queryKey: ['sets'], queryFn: workoutSetApi.getAll });

  if (workoutQuery.isLoading || usersQuery.isLoading || exercisesQuery.isLoading || setsQuery.isLoading) {
    return <LoadingState />;
  }

  if (workoutQuery.isError || usersQuery.isError || exercisesQuery.isError || setsQuery.isError || !workoutQuery.data) {
    return <ErrorState message="Тренировка не загрузилась." />;
  }

  const workout = workoutQuery.data;
  const users = usersQuery.data ?? [];
  const exercises = exercisesQuery.data ?? [];
  const sets = (setsQuery.data ?? []).filter((set) => set.workoutId === workout.id);
  const totalWeight = sets.reduce((sum, set) => sum + set.weight * set.reps, 0);

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
          <span>{Math.round(totalWeight)} кг объема</span>
        </div>
      </section>

      <section>
        <div className="section-heading">
          <h2>Подходы</h2>
          <Link to="/sets">Управлять подходами</Link>
        </div>
        {sets.length ? (
          <div className="card-grid">
            {sets.map((set) => (
              <EntityCard
                key={set.id}
                title={exerciseName(set.exerciseId, exercises)}
                subtitle={`${set.weight} кг · ${set.reps} повторений`}
                meta={<span>{Math.round(set.weight * set.reps)} кг</span>}
              />
            ))}
          </div>
        ) : (
          <EmptyState title="Пока нет подходов" text="Добавьте подходы на странице подходов." />
        )}
      </section>
    </div>
  );
}
