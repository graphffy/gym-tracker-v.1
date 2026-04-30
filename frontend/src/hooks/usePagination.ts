import { useMemo, useState } from 'react';

export function usePagination<T>(items: T[], pageSize = 8) {
  const [page, setPage] = useState(0);
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const safePage = Math.min(page, totalPages - 1);

  const pageItems = useMemo(() => {
    const start = safePage * pageSize;
    return items.slice(start, start + pageSize);
  }, [items, pageSize, safePage]);

  return {
    page: safePage,
    pageSize,
    totalPages,
    totalItems: items.length,
    pageItems,
    setPage,
  };
}
