export function ErrorState({ message }: { message: string }) {
  return (
    <div className="state state-error">
      <strong>Не получилось загрузить</strong>
      <p>{message}</p>
    </div>
  );
}
