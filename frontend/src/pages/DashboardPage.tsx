import { Link } from 'react-router-dom';
import { Dumbbell, FolderPlus, Plus, Trophy } from 'lucide-react';
import { EntityCard } from '../components/EntityCard';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { LoadingState } from '../components/LoadingState';
import { useCatalog } from '../hooks/useCatalog';
import { exerciseName, formatDate, userName, workoutSetsForWorkout } from './helpers';

export function DashboardPage() {
  const { categories, exercises, users, workouts, sets, isLoading, isError } = useCatalog();

  if (isLoading) {
    return <LoadingState />;
  }

  if (isError) {
    return <ErrorState message="Проверьте, запущен ли Spring Boot backend." />;
  }

  const latestWorkouts = [...workouts]
    .sort((a, b) => new Date(b.workoutDate ?? 0).getTime() - new Date(a.workoutDate ?? 0).getTime())
    .slice(0, 4);

  return (
    <div className="page-stack">
      <section className="hero">
        <div>
          <span className="eyebrow">GTracker</span>
          <h1>Тренировки, которые легко вести каждый день</h1>
          <p>Добавляйте упражнения, собирайте тренировки и смотрите прогресс без технической путаницы.</p>
          <div className="hero-actions">
            <Link className="primary-button" to="/workouts">
              <Plus size={18} />
              Добавить тренировку
            </Link>
            <Link className="ghost-button light" to="/exercises">
              <Dumbbell size={18} />
              Добавить упражнение
            </Link>
          </div>
        </div>
        <div className="hero-panel" aria-hidden="true">
          <Trophy size={42} />
          <strong>{sets.reduce((sum, set) => sum + set.reps, 0)}</strong>
          <span>повторений записано</span>
        </div>
      </section>

      <section className="stats-grid">
        <StatCard label="Категории" value={categories.length} />
        <StatCard label="Упражнения" value={exercises.length} />
        <StatCard label="Люди" value={users.length} />
        <StatCard label="Тренировки" value={workouts.length} />
      </section>

      <section className="toolbar">
        <h2>Быстрые действия</h2>
        <div className="toolbar-actions">
          <Link className="primary-button" to="/workouts">Добавить тренировку</Link>
          <Link className="ghost-button" to="/exercises">Добавить упражнение</Link>
          <Link className="ghost-button" to="/categories">
            <FolderPlus size={18} />
            Добавить категорию
          </Link>
        </div>
      </section>

      <section>
        <div className="section-heading">
          <h2>Последние тренировки</h2>
          <Link to="/workouts">Все тренировки</Link>
        </div>
        {latestWorkouts.length ? (
          <div className="card-grid">
            {latestWorkouts.map((workout) => {
              const workoutSets = workoutSetsForWorkout(workout, sets);
              const names = [...new Set(workoutSets.map((set) => exerciseName(set.exerciseId, exercises)))];
              return (
                <EntityCard
                  key={workout.id}
                  title={workout.name}
                  subtitle={`${userName(workout.userId, users)} · ${formatDate(workout.workoutDate)}`}
                  meta={<span>{workoutSets.length} подходов</span>}
                >
                  <div className="tag-row">
                    {names.slice(0, 4).map((name) => <span className="tag" key={name}>{name}</span>)}
                  </div>
                </EntityCard>
              );
            })}
          </div>
        ) : (
          <EmptyState title="Пока нет тренировок" text="Добавьте первую тренировку, и она появится здесь." />
        )}
      </section>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <article className="stat-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}
