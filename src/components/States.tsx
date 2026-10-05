import type { ReactNode } from "react";

export const Spinner = ({ small }: { small?: boolean }) => <span className={`spinner ${small ? "small" : ""}`} role="status" aria-label="جارٍ التحميل" />;

export function Loading() {
  return (
    <div className="state">
      <Spinner />
    </div>
  );
}

export function Empty({ icon, title, children }: { icon?: ReactNode; title: string; children?: ReactNode }) {
  return (
    <div className="state empty">
      {icon && <div className="state-icon">{icon}</div>}
      <h3>{title}</h3>
      {children && <p>{children}</p>}
    </div>
  );
}

export function ErrorState({ onRetry }: { onRetry?: () => void }) {
  return (
    <Empty title="تعذّر تحميل المحتوى">
      تأكد من اتصالك بالإنترنت.
      {onRetry && (
        <>
          <br />
          <button className="btn ghost" onClick={onRetry}>إعادة المحاولة</button>
        </>
      )}
    </Empty>
  );
}

export function SkeletonGrid({ count = 8 }: { count?: number }) {
  return (
    <div className="grid">
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="card skeleton-card">
          <div className="skeleton square" />
          <div className="skeleton line" />
          <div className="skeleton line short" />
        </div>
      ))}
    </div>
  );
}
