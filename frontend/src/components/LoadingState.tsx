export function LoadingState({ label = 'Загружаем данные...' }: { label?: string }) {
  return (
    <div className="state state-loading">
      <span className="spinner" />
      <p>{label}</p>
    </div>
  );
}
