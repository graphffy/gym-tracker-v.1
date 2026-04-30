import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { exerciseApi, type ExercisePayload } from '../api/exerciseApi';
import type { Exercise } from '../api/types';
import { categoryApi, type CategoryPayload } from '../api/categoryApi';
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
import { categoryNames, includesText, pageSize } from './helpers';

type ExerciseFormValues = {
  name: string;
  description?: string;
  categoryIds: string[];
};

export function ExercisesPage() {
  const queryClient = useQueryClient();
  const [query, setQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [editing, setEditing] = useState<Exercise | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [deleting, setDeleting] = useState<Exercise | null>(null);
  const [error, setError] = useState('');

  const exercisesQuery = useQuery({ queryKey: ['exercises'], queryFn: exerciseApi.getAll });
  const categoriesQuery = useQuery({ queryKey: ['categories'], queryFn: categoryApi.getAll });
  const exercises = exercisesQuery.data ?? [];
  const categories = categoriesQuery.data ?? [];

  const filtered = useMemo(() => exercises.filter((exercise) => {
    const matchesSearch = includesText(exercise.name, query) || includesText(exercise.description, query);
    const matchesCategory = !categoryFilter || exercise.categoryIds.includes(Number(categoryFilter));
    return matchesSearch && matchesCategory;
  }), [exercises, query, categoryFilter]);
  const pagination = usePagination(filtered, pageSize);

  const saveMutation = useMutation({
    mutationFn: (payload: ExercisePayload & { current?: Exercise }) =>
      payload.current
        ? exerciseApi.update(payload.current.id, { name: payload.name, description: payload.description, categoryIds: payload.categoryIds })
        : exerciseApi.create({ name: payload.name, description: payload.description, categoryIds: payload.categoryIds }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['exercises'] });
      setIsCreating(false);
      setEditing(null);
      setError('');
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (exercise: Exercise) => exerciseApi.delete(exercise.id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['exercises'] });
      setDeleting(null);
    },
    onError: (err) => setError(getApiErrorMessage(err)),
  });

  if (exercisesQuery.isLoading || categoriesQuery.isLoading) {
    return <LoadingState />;
  }

  if (exercisesQuery.isError || categoriesQuery.isError) {
    return <ErrorState message="Упражнения не загрузились. Проверьте backend." />;
  }

  return (
    <div className="page-stack">
      <div className="page-title">
        <div>
          <span className="eyebrow dark">Упражнения</span>
          <h1>Библиотека движений</h1>
          <p>Карточки упражнений с понятными категориями вместо технических полей.</p>
        </div>
        <button type="button" className="primary-button" onClick={() => setIsCreating(true)}>
          <Plus size={18} />
          Добавить упражнение
        </button>
      </div>

      {error ? <ErrorState message={error} /> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Найти упражнение" />
        <select className="inline-select" value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
          <option value="">Все категории</option>
          {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
        </select>
      </div>

      {pagination.pageItems.length ? (
        <div className="card-grid">
          {pagination.pageItems.map((exercise) => (
            <EntityCard
              key={exercise.id}
              title={exercise.name}
              subtitle={exercise.description || 'Описание не добавлено'}
              actions={
                <>
                  <button type="button" className="icon-button" onClick={() => setEditing(exercise)} aria-label="Редактировать">
                    <Edit size={18} />
                  </button>
                  <button type="button" className="icon-button danger" onClick={() => setDeleting(exercise)} aria-label="Удалить">
                    <Trash2 size={18} />
                  </button>
                </>
              }
            >
              <div className="tag-row">
                {categoryNames(exercise, categories).map((name) => <span className="tag accent" key={name}>{name}</span>)}
              </div>
            </EntityCard>
          ))}
        </div>
      ) : (
        <EmptyState title="Ничего не найдено" text="Попробуйте другой поиск или категорию." />
      )}

      <Pagination page={pagination.page} totalPages={pagination.totalPages} onPageChange={pagination.setPage} />

      {(isCreating || editing) ? (
        <ExerciseForm
          exercise={editing}
          categories={categories}
          isBusy={saveMutation.isPending}
          onClose={() => { setIsCreating(false); setEditing(null); }}
          onSubmit={(payload) => saveMutation.mutate({ ...payload, current: editing ?? undefined })}
        />
      ) : null}

      {deleting ? (
        <ConfirmDialog
          title="Удалить упражнение?"
          text={`Упражнение "${deleting.name}" исчезнет из библиотеки.`}
          isBusy={deleteMutation.isPending}
          onCancel={() => setDeleting(null)}
          onConfirm={() => deleteMutation.mutate(deleting)}
        />
      ) : null}
    </div>
  );
}

function ExerciseForm({ exercise, categories, isBusy, onClose, onSubmit }: {
  exercise: Exercise | null;
  categories: Array<{ id: number; name: string }>;
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: ExercisePayload) => void;
}) {
  const queryClient = useQueryClient();
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<ExerciseFormValues>({
    defaultValues: {
      name: exercise?.name ?? '',
      description: exercise?.description ?? '',
      categoryIds: exercise?.categoryIds.map(String) ?? [],
    },
  });
  const categoryMutation = useMutation({
    mutationFn: (payload: CategoryPayload) => categoryApi.create(payload),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['categories'] });
      setIsCategoryOpen(false);
    },
  });

  return (
    <Modal title={exercise ? 'Изменить упражнение' : 'Новое упражнение'} onClose={onClose}>
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
        <button type="button" className="ghost-button quick-create-button" onClick={() => setIsCategoryOpen(true)}>
          Добавить новую категорию
        </button>
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Сохранить</button>
        </div>
      </form>
      {isCategoryOpen ? (
        <QuickCategoryModal
          isBusy={categoryMutation.isPending}
          onClose={() => setIsCategoryOpen(false)}
          onSubmit={(payload) => categoryMutation.mutate(payload)}
        />
      ) : null}
    </Modal>
  );
}

function QuickCategoryModal({ isBusy, onClose, onSubmit }: {
  isBusy: boolean;
  onClose: () => void;
  onSubmit: (payload: CategoryPayload) => void;
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<CategoryPayload>({ defaultValues: { name: '' } });

  return (
    <Modal title="Добавить новую категорию" onClose={onClose}>
      <form className="form" onSubmit={handleSubmit(onSubmit)}>
        <FormField label="Название категории" registration={register('name', { required: 'Напишите название' })} error={errors.name?.message} autoFocus />
        <div className="form-actions">
          <button type="button" className="ghost-button" onClick={onClose}>Отмена</button>
          <button type="submit" className="primary-button" disabled={isBusy}>Добавить</button>
        </div>
      </form>
    </Modal>
  );
}
