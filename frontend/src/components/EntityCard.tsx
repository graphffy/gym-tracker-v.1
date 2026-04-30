import type { ReactNode } from 'react';

interface EntityCardProps {
  title: string;
  subtitle?: string;
  meta?: ReactNode;
  actions?: ReactNode;
  children?: ReactNode;
  onClick?: () => void;
}

export function EntityCard({ title, subtitle, meta, actions, children, onClick }: EntityCardProps) {
  return (
    <article className={`entity-card ${onClick ? 'entity-card-clickable' : ''}`} onClick={onClick}>
      <div className="entity-card-top">
        <div>
          <h3>{title}</h3>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        {meta ? <div className="entity-meta">{meta}</div> : null}
      </div>
      {children ? <div className="entity-card-body">{children}</div> : null}
      {actions ? <div className="card-actions" onClick={(event) => event.stopPropagation()}>{actions}</div> : null}
    </article>
  );
}
