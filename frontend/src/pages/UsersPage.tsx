import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { userApi, type UserPayload } from '../api/userApi';
import type { User } from '../api/types';
import { workoutApi } from '../api/workoutApi';
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
import { usePagination } from '../hooks/usePagination';
import { formatDate, includesText, pageSize } from './helpers';

export function UsersPage() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState<User | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [deleting, setDeleting] = useState<User | null>(null);
  const [error, setError] = useState('');

  const usersQuery = useQuery({ queryKey: ['users'], queryFn: userApi.getAll });
  const workoutsQuery = useQuery({ queryKey: ['workouts'], queryFn: workoutApi.getAll });
  const users = usersQuery.data ?? [];
  const workouts = workoutsQuery.data ?? [];

  const filtered = useMemo(() => users.filter((user) => includesText(user.username, query) || includesText(user.email, query)), [users, query]);
  const pagination = usePagination(filtered, pageSize);

  const saveMutation = useMutation({
    mutationFn: (payload: UserPayload & { current?: User }) =>
      payload.current
        ? userApi.update(payload.current.id, { username: payload.username, email: payload.email })
        : userApi.create({ username: payload.username, email: payload.email }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['users'] });
      setIsCreating(false);
      setEditing(null);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (user: User) => userApi.delete(user.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['users'] });
      setDeleting(null);
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (usersQuery.isLoading || workoutsQuery.isLoading) {
    return <LoadingState />;
  }

  if (usersQuery.isError || workoutsQuery.isError) {
    return <ErrorState message="Пользователи не загрузились. Проверьте backend." />;
  }

  return (
    <div className="page-stack">
      <div className="page-title">
        <div>
          <span className="eyebrow dark">Люди</span>
          <h1>Профили спортсменов</h1>
          <p>Имя, почта и тренировки человека в одном понятном месте.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setIsCreating(true)}>
          <Plus size={18} />
          Добавить человека
        </button>
      </div>

      {error ? <ErrorState message={error} /> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Найти человека" />
      </div>

      {pagination.pageItems.length ? (
        <div className="card-grid">
          {pagination.pageItems.map((user) => {
            const userWorkouts = workouts.filter((workout) => workout.userId === user.id);
            return (
              <EntityCard
                key={user.id}
                title={user.username}
                subtitle={user.email}
                meta={<span>{userWorkouts.length} тренировок</span>}
                actions={
                  <>
                    <button type="button" className="icon-button" onClick={() => setEditing(user)} aria-label="Редактировать">
                      <Edit size={18} />
                    </button>
                    <button type="button" className="icon-button danger" onClick={() => setDeleting(user)} aria-label="Удалить">
                      <Trash2 size={18} />
                    </button>
                  </>
                }
              >
                <div className="mini-list">
                  {userWorkouts.slice(0, 3).map((workout) => <span key={workout.id}>{workout.name} · {formatDate(workout.workoutDate)}</span>)}
                </div>
              </EntityCard>
            );
          })}
        </div>
      ) : (
        <EmptyState title="Ничего не найдено" text="Попробуйте другой поиск или добавьте человека." />
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.setPage} />

      {(isCreating || editing) ? (
        <UserForm
          user={editing}
          isBusy={saveMutation.isPending}
          onClose={() => { setIsCreating(false); setEditing(null); }}
          onSubmit={(payload) => saveMutation.mutate({ ...payload, current: editing ?? undefined })}
        />
      ) : null}

      {deleting ? (
        <ConfirmDialog
          title="Удалить человека?"
          text={`Профиль "${deleting.username}" будет удален.`}
          isBusy={deleteMutation.isPending}
          onCancel={() => setDeleting(null)}
          onConfirm={() => deleteMutation.mutate(deleting)}
        />
      ) : null}
    </div>
  );
}

function UserForm({ user, isBusy, onClose, onSubmit }: {
  user: User | null;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: UserPayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<UserPayload>({
    defaultValues: { username: user?.username ?? '', email: user?.email ?? '' },
  });

  return (
    <Modal title={user ? 'Изменить профиль' : 'Новый человек'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Имя" registration={register('username', { required: 'Напишите имя', minLength: { value: 3, message: 'Минимум 3 символа' } })} error={errors.username?.message} autoFocus />
        <FormField label="Email" type="email" registration={register('email', { required: 'Напишите email' })} error={errors.email?.message} />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Сохранить</button>
        </div>
      </form>
    </Modal>
  );
}
