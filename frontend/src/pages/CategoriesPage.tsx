import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { categoryApi, type CategoryPayload } from '../api/categoryApi';
import type { Category } from '../api/types';
import { exerciseApi } from '../api/exerciseApi';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { EntityCard } from '../components/EntityCard';
import { ErrorState } from '../components/ErrorState';
import { FormField } from '../components/FormField';
import { LoadingState } from '../components/LoadingState';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { SearchInput } from '../components/SearchInput';
import { getApiErrorMessage } from '../api/http';
import { includesText, pageSize } from './helpers';
import { usePagination } from '../hooks/usePagination';

export function CategoriesPage() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState<Category | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [deleting, setDeleting] = useState<Category | null>(null);
  const [error, setError] = useState('');

  const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll });
  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });

  const categories = categoriesQuery.data ?? [];
  const exercises = exercisesQuery.data ?? [];
  const filtered = useMemo(() => categories.filter((category) => includesText(category.name, query)), [categories, query]);
  const pagination = usePagination(filtered, pageSize);

  const saveMutation = useMutation({
    mutationFn: (payload: CategoryPayload & { current?: Category }) =>
      payload.current ? categoryApi.update(payload.current.id, { name: payload.name }) : categoryApi.create({ name: payload.name }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categories'] });
      setEditing(null);
      setIsCreating(false);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (category: Category) => categoryApi.delete(category.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categories'] });
      setDeleting(null);
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (categoriesQuery.isLoading || exercisesQuery.isLoading) {
    return <LoadingState />;
  }

  if (categoriesQuery.isError || exercisesQuery.isError) {
    return <ErrorState message="Категории не загрузились. Проверьте backend." />;
  }

  return (
    <div className="page-stack">
      <div className="page-title">
        <div>
          <span className="eyebrow dark">Категории</span>
          <h1>Группы упражнений</h1>
          <p>Мышечные группы и направления, чтобы упражнения было легко находить.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setIsCreating(true)}>
          <Plus size={18} />
          Добавить категорию
        </button>
      </div>

      {error ? <ErrorState message={error} /> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Найти категорию" />
      </div>

      {pagination.pageItems.length ? (
        <div className="card-grid">
          {pagination.pageItems.map((category) => {
            const linkedExercises = exercises.filter((exercise) => exercise.categoryIds.includes(category.id));
            return (
              <EntityCard
                key={category.id}
                title={category.name}
                subtitle={linkedExercises.length ? `${linkedExercises.length} упражнений` : 'Упражнений пока нет'}
                actions={
                  <>
                    <button type="button" className="icon-button" onClick={() => setEditing(category)} aria-label="Редактировать">
                      <Edit size={18} />
                    </button>
                    <button type="button" className="icon-button danger" onClick={() => setDeleting(category)} aria-label="Удалить">
                      <Trash2 size={18} />
                    </button>
                  </>
                }
              >
                <div className="tag-row">
                  {linkedExercises.slice(0, 6).map((exercise) => <span className="tag" key={exercise.id}>{exercise.name}</span>)}
                </div>
              </EntityCard>
            );
          })}
        </div>
      ) : (
        <EmptyState title="Ничего не найдено" text="Попробуйте изменить поиск или добавить новую категорию." />
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.setPage} />

      {(isCreating || editing) ? (
        <CategoryForm
          category={editing}
          isBusy={saveMutation.isPending}
          onClose={() => { setIsCreating(false); setEditing(null); }}
          onSubmit={(payload) => saveMutation.mutate({ ...payload, current: editing ?? undefined })}
        />
      ) : null}

      {deleting ? (
        <ConfirmDialog
          title="Удалить категорию?"
          text={`Категория "${deleting.name}" исчезнет из списка.`}
          isBusy={deleteMutation.isPending}
          onCancel={() => setDeleting(null)}
          onConfirm={() => deleteMutation.mutate(deleting)}
        />
      ) : null}
    </div>
  );
}

function CategoryForm({ category, isBusy, onClose, onSubmit }: {
  category: Category | null;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: CategoryPayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<CategoryPayload>({
    defaultValues: { name: category?.name ?? '' },
  });

  return (
    <Modal title={category ? 'Изменить категорию' : 'Новая категория'} onClose={onClose}>
      <form className="form" onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Название" registration={register('name', { required: 'Напишите название' })} error={errors.name?.message} autoFocus />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Сохранить</button>
        </div>
      </form>
    </Modal>
  );
}
