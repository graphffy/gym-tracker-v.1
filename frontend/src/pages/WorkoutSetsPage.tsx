import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { workoutSetApi, type WorkoutSetPayload } from '../api/workoutSetApi';
import type { WorkoutSet } from '../api/types';
import { exerciseApi, type ExercisePayload } from '../api/exerciseApi';
import { userApi } from '../api/userApi';
import { workoutApi, type WorkoutPayload } from '../api/workoutApi';
import { categoryApi } from '../api/categoryApi';
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
import { exerciseName, formatDate, includesText, pageSize, userName, workoutName } from './helpers';

type SetFormValues = {
  weight: number;
  reps: number;
  workoutId: string;
  exerciseId: string;
};

export function WorkoutSetsPage() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [workoutFilter, setWorkoutFilter] = useState('');
  const [exerciseFilter, setExerciseFilter] = useState('');
  const [userFilter, setUserFilter] = useState('');
  const [editing, setEditing] = useState<WorkoutSet | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [deleting, setDeleting] = useState<WorkoutSet | null>(null);
  const [error, setError] = useState('');

  const setsQuery = useQuery({ queryKey: ['sets'], queryFn: workoutSetApi.getAll });
  const workoutsQuery = useQuery({ queryKey: ['workouts'], queryFn: workoutApi.getAll });
  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const usersQuery = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll });

  const sets = setsQuery.data ?? [];
  const workouts = workoutsQuery.data ?? [];
  const exercises = exercisesQuery.data ?? [];
  const users = usersQuery.data ?? [];
  const categories = categoriesQuery.data ?? [];

  const filtered = useMemo(() => sets.filter((set) => {
    const workout = workouts.find((item) => item.id === set.workoutId);
    const person = workout ? userName(workout.userId, users) : '';
    const movement = exerciseName(set.exerciseId, exercises);
    const matchesText = includesText(workout?.name, query) || includesText(person, query) || includesText(movement, query);
    const matchesWorkout = !workoutFilter || set.workoutId === Number(workoutFilter);
    const matchesExercise = !exerciseFilter || set.exerciseId === Number(exerciseFilter);
    const matchesUser = !userFilter || workout?.userId === Number(userFilter);
    return matchesText && matchesWorkout && matchesExercise && matchesUser;
  }), [sets, workouts, exercises, users, query, workoutFilter, exerciseFilter, userFilter]);
  const pagination = usePagination(filtered, pageSize);

  const saveMutation = useMutation({
    mutationFn: (payload: WorkoutSetPayload & { current?: WorkoutSet }) =>
      payload.current
        ? workoutSetApi.update(payload.current.id, { weight: payload.weight, reps: payload.reps, workoutId: payload.workoutId, exerciseId: payload.exerciseId })
        : workoutSetApi.create({ weight: payload.weight, reps: payload.reps, workoutId: payload.workoutId, exerciseId: payload.exerciseId }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['sets'] });
      setIsCreating(false);
      setEditing(null);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (set: WorkoutSet) => workoutSetApi.delete(set.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['sets'] });
      setDeleting(null);
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (setsQuery.isLoading || workoutsQuery.isLoading || exercisesQuery.isLoading || usersQuery.isLoading || categoriesQuery.isLoading) {
    return <LoadingState />;
  }

  if (setsQuery.isError || workoutsQuery.isError || exercisesQuery.isError || usersQuery.isError || categoriesQuery.isError) {
    return <ErrorState message="Подходы не загрузились. Проверьте backend." />;
  }

  return (
    <div className="page-stack">
      <div className="page-title">
        <div>
          <span className="eyebrow dark">Подходы</span>
          <h1>Вес и повторения</h1>
          <p>Каждый подход показывает тренировку, человека и упражнение понятными словами.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setIsCreating(true)}>
          <Plus size={18} />
          Добавить подход
        </button>
      </div>

      {error ? <ErrorState message={error} /> : null}

      <div className="toolbar filters">
        <SearchInput value={query} onChange={setQuery} placeholder="Найти подход" />
        <select className="inline-select" value={userFilter} onChange={(event) => setUserFilter(event.target.value)}>
          <option value="">Все люди</option>
          {users.map((user) => <option key={user.id} value={user.id}>{user.username}</option>)}
        </select>
        <select className="inline-select" value={workoutFilter} onChange={(event) => setWorkoutFilter(event.target.value)}>
          <option value="">Все тренировки</option>
          {workouts.map((workout) => <option key={workout.id} value={workout.id}>{workout.name} · {formatDate(workout.workoutDate)}</option>)}
        </select>
        <select className="inline-select" value={exerciseFilter} onChange={(event) => setExerciseFilter(event.target.value)}>
          <option value="">Все упражнения</option>
          {exercises.map((exercise) => <option key={exercise.id} value={exercise.id}>{exercise.name}</option>)}
        </select>
      </div>

      {pagination.pageItems.length ? (
        <div className="card-grid dense">
          {pagination.pageItems.map((set) => {
            const workout = workouts.find((item) => item.id === set.workoutId);
            return (
              <EntityCard
                key={set.id}
                title={exerciseName(set.exerciseId, exercises)}
                subtitle={`${set.weight} кг · ${set.reps} повторений`}
                meta={<span>{workout ? userName(workout.userId, users) : 'Без человека'}</span>}
                actions={
                  <>
                    <button type="button" className="icon-button" onClick={() => setEditing(set)} aria-label="Редактировать">
                      <Edit size={18} />
                    </button>
                    <button type="button" className="icon-button danger" onClick={() => setDeleting(set)} aria-label="Удалить">
                      <Trash2 size={18} />
                    </button>
                  </>
                }
              >
                <div className="mini-list">
                  <span>{workoutName(set.workoutId, workouts)}</span>
                  <span>Объем: {Math.round(set.weight * set.reps)} кг</span>
                </div>
              </EntityCard>
            );
          })}
        </div>
      ) : (
        <EmptyState title="Ничего не найдено" text="Добавьте подход или измените фильтры." />
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.setPage} />

      {(isCreating || editing) ? (
        <WorkoutSetForm
          set={editing}
          workouts={workouts}
          exercises={exercises}
          users={users}
          categories={categories}
          isBusy={saveMutation.isPending}
          onClose={() => { setIsCreating(false); setEditing(null); }}
          onSubmit={(payload) => saveMutation.mutate({ ...payload, current: editing ?? undefined })}
        />
      ) : null}

      {deleting ? (
        <ConfirmDialog
          title="Удалить подход?"
          text={`Подход "${exerciseName(deleting.exerciseId, exercises)}" будет удален.`}
          isBusy={deleteMutation.isPending}
          onCancel={() => setDeleting(null)}
          onConfirm={() => deleteMutation.mutate(deleting)}
        />
      ) : null}
    </div>
  );
}

function WorkoutSetForm({ set, workouts, exercises, users, categories, isBusy, onClose, onSubmit }: {
  set: WorkoutSet | null;
  workouts: Array<{ id: number; name: string; workoutDate?: string | null }>;
  exercises: Array<{ id: number; name: string }>;
  users: Array<{ id: number; username: string }>;
  categories: Array<{ id: number; name: string }>;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: WorkoutSetPayload) => void;
}) {
  const queryClient = useQueryClient();
  const [isWorkoutOpen, setIsWorkoutOpen] = useState(false);
  const [isExerciseOpen, setIsExerciseOpen] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<SetFormValues>({
    defaultValues: {
      weight: set?.weight ?? 20,
      reps: set?.reps ?? 10,
      workoutId: set?.workoutId ? String(set.workoutId) : '',
      exerciseId: set?.exerciseId ? String(set.exerciseId) : '',
    },
  });
  const workoutMutation = useMutation({
    mutationFn: (payload: WorkoutPayload) => workoutApi.create(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['workouts'] });
      setIsWorkoutOpen(false);
    },
  });
  const exerciseMutation = useMutation({
    mutationFn: (payload: ExercisePayload) => exerciseApi.create(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['exercises'] });
      setIsExerciseOpen(false);
    },
  });

  return (
    <Modal title={set ? 'Изменить подход' : 'Новый подход'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit((values) => onSubmit({
        weight: Number(values.weight),
        reps: Number(values.reps),
        workoutId: Number(values.workoutId),
        exerciseId: Number(values.exerciseId),
      }))}>
        <SelectField label="Тренировка" registration={register('workoutId', { required: 'Выберите тренировку' })} error={errors.workoutId?.message} options={workouts.map((workout) => ({ value: workout.id, label: `${workout.name} · ${formatDate(workout.workoutDate)}` }))} />
        <button type="button" className="ghost-button quick-create-button" onClick={() => setIsWorkoutOpen(true)}>
          Добавить новую тренировку
        </button>
        <SelectField label="Упражнение" registration={register('exerciseId', { required: 'Выберите упражнение' })} error={errors.exerciseId?.message} options={exercises.map((exercise) => ({ value: exercise.id, label: exercise.name }))} />
        <button type="button" className="ghost-button quick-create-button" onClick={() => setIsExerciseOpen(true)}>
          Добавить новое упражнение
        </button>
        <div className="form-grid">
          <FormField label="Вес, кг" type="number" step="0.5" min="0.5" registration={register('weight', { required: 'Укажите вес', min: { value: 0.5, message: 'Вес должен быть больше нуля' } })} error={errors.weight?.message} />
          <FormField label="Повторения" type="number" min="1" registration={register('reps', { required: 'Укажите повторения', min: { value: 1, message: 'Минимум 1 повторение' } })} error={errors.reps?.message} />
        </div>
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Сохранить</button>
        </div>
      </form>
      {isWorkoutOpen ? (
        <QuickWorkoutModal
          users={users}
          isBusy={workoutMutation.isPending}
          onClose={() => setIsWorkoutOpen(false)}
          onSubmit={(payload) => workoutMutation.mutate(payload)}
        />
      ) : null}
      {isExerciseOpen ? (
        <QuickExerciseModal
          categories={categories}
          isBusy={exerciseMutation.isPending}
          onClose={() => setIsExerciseOpen(false)}
          onSubmit={(payload) => exerciseMutation.mutate(payload)}
        />
      ) : null}
    </Modal>
  );
}

function QuickWorkoutModal({ users, isBusy, onClose, onSubmit }: {
  users: Array<{ id: number; username: string }>;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: WorkoutPayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<{ name: string; workoutDate: string; userId: string }>({
    defaultValues: { name: '', workoutDate: '', userId: '' },
  });

  return (
    <Modal title="Добавить новую тренировку" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit((values) => onSubmit({
        name: values.name,
        workoutDate: values.workoutDate ? `${values.workoutDate}:00` : null,
        userId: Number(values.userId),
      }))}>
        <FormField label="Название" registration={register('name', { required: 'Напишите название' })} error={errors.name?.message} autoFocus />
        <FormField label="Дата и время" type="datetime-local" registration={register('workoutDate')} />
        <SelectField label="Человек" registration={register('userId', { required: 'Выберите человека' })} error={errors.userId?.message} options={users.map((user) => ({ value: user.id, label: user.username }))} />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Добавить</button>
        </div>
      </form>
    </Modal>
  );
}

function QuickExerciseModal({ categories, isBusy, onClose, onSubmit }: {
  categories: Array<{ id: number; name: string }>;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: ExercisePayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<{ name: string; description?: string; categoryIds: string[] }>({
    defaultValues: { name: '', description: '', categoryIds: [] },
  });

  return (
    <Modal title="Добавить новое упражнение" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit((values) => onSubmit({
        name: values.name,
        description: values.description ?? '',
        categoryIds: values.categoryIds.map(Number),
      }))}>
        <FormField label="Название" registration={register('name', { required: 'Напишите название' })} error={errors.name?.message} autoFocus />
        <FormField label="Описание" multiline registration={register('description')} />
        <SelectField
          label="Категории"
          multiple
          size={Math.min(5, Math.max(3, categories.length))}
          registration={register('categoryIds', { validate: (value) => value.length > 0 || 'Выберите категорию' })}
          error={errors.categoryIds?.message}
          options={categories.map((category) => ({ value: category.id, label: category.name }))}
        />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Добавить</button>
        </div>
      </form>
    </Modal>
  );
}
