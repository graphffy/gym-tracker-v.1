export function EmptyState({ title, text }: { title: string; text?: string }) {
  return (
    <div className="state state-empty">
      <strong>{title}</strong>
      {text ? <p>{text}</p> : null}
    </div>
  );
}
