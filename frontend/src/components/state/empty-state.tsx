import type { ReactNode } from "react";
import styles from "./state.module.css";

interface EmptyStateProps {
  icon?: string;
  title: string;
  hint?: string;
  action?: ReactNode;
}

export default function EmptyState({
  icon = "📭",
  title,
  hint,
  action,
}: EmptyStateProps) {
  return (
    <div className={styles.empty} data-testid="empty-state">
      <div className={styles.emptyIcon} aria-hidden="true">
        {icon}
      </div>
      <p className={styles.emptyTitle}>{title}</p>
      {hint && <p className={styles.emptyHint}>{hint}</p>}
      {action && <div className={styles.emptyAction}>{action}</div>}
    </div>
  );
}
