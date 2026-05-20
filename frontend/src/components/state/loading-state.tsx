import styles from "./state.module.css";

interface LoadingStateProps {
  label?: string;
  size?: "sm" | "md" | "lg";
}

export default function LoadingState({
  label = "Loading...",
  size = "md",
}: LoadingStateProps) {
  return (
    <div
      className={`${styles.loading} ${styles[`loading_${size}`]}`}
      role="status"
      aria-live="polite"
      data-testid="loading-state"
    >
      <span className={styles.spinner} aria-hidden="true" />
      <span className={styles.loadingLabel}>{label}</span>
    </div>
  );
}
