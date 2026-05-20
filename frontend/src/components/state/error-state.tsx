import styles from "./state.module.css";

interface ErrorStateProps {
  errors: string[];
  title?: string;
}

export default function ErrorState({ errors, title }: ErrorStateProps) {
  if (errors.length === 0) return null;
  return (
    <div className={styles.errorBox} role="alert" data-testid="error-state">
      {title && <p className={styles.errorTitle}>{title}</p>}
      <ul className={styles.errorList}>
        {errors.map((err, i) => (
          <li key={i}>{err}</li>
        ))}
      </ul>
    </div>
  );
}
