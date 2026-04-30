import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  return (
    <div className="pagination">
      <button type="button" className="ghost-button" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>
        <ChevronLeft size={18} />
        Назад
      </button>
      <span>Страница {page + 1} из {totalPages}</span>
      <button
        type="button"
        className="ghost-button"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Вперед
        <ChevronRight size={18} />
      </button>
    </div>
  );
}
