import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, Eye, Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { workoutApi, type WorkoutPayload } from '../api/workoutApi';
import type { Workout } from '../api/types';
import { userApi, type UserPayload } from '../api/userApi';
import { exerciseApi } from '../api/exerciseApi';
import { workoutSetApi } from '../api/workoutSetApi';
import { getApiErrorMessage } from '../api/http';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { EntityCard } from '../components/EntityCard';
import { ErrorState } from '../components/ErrorState';
import { FormField } from '../components/FormField';
import { LoadingState } from '../components/LoadingState';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { SearchInput } from '../components/SearchInput';
import { SelectField } from '../components/SelectField';
import { usePagination } from '../hooks/usePagination';
import { exerciseName, formatDate, fromDateTimeInput, includesText, pageSize, toDateTimeInput, userName, workoutSetsForWorkout } from './helpers';

type WorkoutFormValues = {
  name: string;
  workoutDate: string;
  userId: string;
};

export function WorkoutsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [userFilter, setUserFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');
  const [exerciseFilter, setExerciseFilter] = useState('');
  const [editing, setEditing] = useState<Workout | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [deleting, setDeleting] = useState<Workout | null>(null);
  const [error, setError] = useState('');

  const workoutsQuery = useQuery({ queryKey: ['workouts'], queryFn: workoutApi.getAll });
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const setsQuery = useQuery({ queryKey: ['sets'], queryFn: workoutSetApi.getAll });

  const workouts = workoutsQuery.data ?? [];
  const users = usersQuery.data ?? [];
  const exercises = exercisesQuery.data ?? [];
  const sets = setsQuery.data ?? [];

  const filtered = useMemo(() => workouts.filter((workout) => {
    const workoutSets = workoutSetsForWorkout(workout, sets);
    const matchesText = includesText(workout.name, query) || includesText(userName(workout.userId, users), query);
    const matchesUser = !userFilter || workout.userId === Number(userFilter);
    const matchesDate = !dateFilter || (workout.workoutDate ?? '').startsWith(dateFilter);
    const matchesExercise = !exerciseFilter || workoutSets.some((set) => set.exerciseId === Number(exerciseFilter));
    return matchesText && matchesUser && matchesDate && matchesExercise;
  }), [workouts, sets, query, users, userFilter, dateFilter, exerciseFilter]);
  const pagination = usePagination(filtered, pageSize);

  const saveMutation = useMutation({
    mutationFn: (payload: WorkoutPayload & { current?: Workout }) =>
      payload.current
        ? workoutApi.update(payload.current.id, { name: payload.name, workoutDate: payload.workoutDate, userId: payload.userId })
        : workoutApi.create({ name: payload.name, workoutDate: payload.workoutDate, userId: payload.userId }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['workouts'] });
      setIsCreating(false);
      setEditing(null);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (workout: Workout) => workoutApi.delete(workout.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['workouts'] });
      setDeleting(null);
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (workoutsQuery.isLoading || usersQuery.isLoading || exercisesQuery.isLoading || setsQuery.isLoading) {
    return <LoadingState />;
  }

  if (workoutsQuery.isError || usersQuery.isError || exercisesQuery.isError || setsQuery.isError) {
    return <ErrorState message="Тренировки не загрузились. Проверьте backend." />;
  }

  return (
    <div className="page-stack">
      <div className="page-title">
        <div>
          <span className="eyebrow dark">Тренировки</span>
          <h1>Журнал занятий</h1>
          <p>Дата, человек, упражнения и подходы собраны в одну понятную карточку.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setIsCreating(true)}>
          <Plus size={18} />
          Добавить тренировку
        </button>
      </div>

      {error ? <ErrorState message={error} /> : null}

      <div className="toolbar filters">
        <SearchInput value={query} onChange={setQuery} placeholder="Найти тренировку" />
        <select className="inline-select" value={userFilter} onChange={(event) => setUserFilter(event.target.value)}>
          <option value="">Все люди</option>
          {users.map((user) => <option key={user.id} value={user.id}>{user.username}</option>)}
        </select>
        <input className="inline-select" type="date" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} />
        <select className="inline-select" value={exerciseFilter} onChange={(event) => setExerciseFilter(event.target.value)}>
          <option value="">Все упражнения</option>
          {exercises.map((exercise) => <option key={exercise.id} value={exercise.id}>{exercise.name}</option>)}
        </select>
      </div>

      {pagination.pageItems.length ? (
        <div className="card-grid">
          {pagination.pageItems.map((workout) => {
            const workoutSets = workoutSetsForWorkout(workout, sets);
            const names = [...new Set(workoutSets.map((set) => exerciseName(set.exerciseId, exercises)))];
            return (
              <EntityCard
                key={workout.id}
                title={workout.name}
                subtitle={`${userName(workout.userId, users)} · ${formatDate(workout.workoutDate)}`}
                meta={<span>{workoutSets.length} подходов</span>}
                onClick={() => navigate(`/workouts/${workout.id}`)}
                actions={
                  <>
                    <button type="button" className="icon-button" onClick={() => navigate(`/workouts/${workout.id}`)} aria-label="Открыть">
                      <Eye size={18} />
                    </button>
                    <button type="button" className="icon-button" onClick={() => setEditing(workout)} aria-label="Редактировать">
                      <Edit size={18} />
                    </button>
                    <button type="button" className="icon-button danger" onClick={() => setDeleting(workout)} aria-label="Удалить">
                      <Trash2 size={18} />
                    </button>
                  </>
                }
              >
                <div className="tag-row">
                  {names.slice(0, 5).map((name) => <span className="tag" key={name}>{name}</span>)}
                </div>
              </EntityCard>
            );
          })}
        </div>
      ) : (
        <EmptyState title="Пока нет тренировок" text="Добавьте тренировку или измените фильтры." />
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.setPage} />

      {(isCreating || editing) ? (
        <WorkoutForm
          workout={editing}
          users={users}
          isBusy={saveMutation.isPending}
          onClose={() => { setIsCreating(false); setEditing(null); }}
          onSubmit={(payload) => saveMutation.mutate({ ...payload, current: editing ?? undefined })}
        />
      ) : null}

      {deleting ? (
        <ConfirmDialog
          title="Удалить тренировку?"
          text={`Тренировка "${deleting.name}" будет удалена.`}
          isBusy={deleteMutation.isPending}
          onCancel={() => setDeleting(null)}
          onConfirm={() => deleteMutation.mutate(deleting)}
        />
      ) : null}
    </div>
  );
}

function WorkoutForm({ workout, users, isBusy, onClose, onSubmit }: {
  workout: Workout | null;
  users: Array<{ id: number; username: string }>;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: WorkoutPayload) => void;
}) {
  const queryClient = useQueryClient();
  const [isUserOpen, setIsUserOpen] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<WorkoutFormValues>({
    defaultValues: {
      name: workout?.name ?? '',
      workoutDate: toDateTimeInput(workout?.workoutDate) || toDateTimeInput(new Date().toISOString()),
      userId: workout?.userId ? String(workout.userId) : '',
    },
  });
  const userMutation = useMutation({
    mutationFn: (payload: UserPayload) => userApi.create(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsUserOpen(false);
    },
  });

  return (
    <Modal title={workout ? 'Изменить тренировку' : 'Новая тренировка'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit((values) => onSubmit({
        name: values.name,
        workoutDate: fromDateTimeInput(values.workoutDate),
        userId: Number(values.userId),
      }))}>
        <FormField label="Название" registration={register('name', { required: 'Напишите название' })} error={errors.name?.message} autoFocus />
        <FormField label="Дата и время" type="datetime-local" registration={register('workoutDate')} />
        <SelectField label="Человек" registration={register('userId', { required: 'Выберите человека' })} error={errors.userId?.message} options={users.map((user) => ({ value: user.id, label: user.username }))} />
        <button type="button" className="ghost-button quick-create-button" onClick={() => setIsUserOpen(true)}>
          Добавить нового человека
        </button>
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Сохранить</button>
        </div>
      </form>
      {isUserOpen ? (
        <QuickUserModal
          isBusy={userMutation.isPending}
          onClose={() => setIsUserOpen(false)}
          onSubmit={(payload) => userMutation.mutate(payload)}
        />
      ) : null}
    </Modal>
  );
}

function QuickUserModal({ isBusy, onClose, onSubmit }: {
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: UserPayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<UserPayload>({
    defaultValues: { username: '', email: '' },
  });

  return (
    <Modal title="Добавить нового человека" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Имя" registration={register('username', { required: 'Напишите имя', minLength: { value: 3, message: 'Минимум 3 символа' } })} error={errors.username?.message} autoFocus />
        <FormField label="Email" type="email" registration={register('email', { required: 'Напишите email' })} error={errors.email?.message} />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Добавить</button>
        </div>
      </form>
    </Modal>
  );
}
