import styles from "./pagination.module.css";

interface PaginationProps {
  totalPages: number;
  currentPage: number;
  onPageChange: (page: number) => void;
  label?: string;
}

export default function Pagination({
  totalPages,
  currentPage,
  onPageChange,
  label = "Article pages",
}: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i + 1);

  return (
    <nav aria-label={label}>
      <ul className={styles.pagination}>
        {pages.map((page) => {
          const isActive = page === currentPage;
          return (
            <li
              key={page}
              className={`${styles.pageItem} ${isActive ? styles.pageItemActive : ""}`}
            >
              <button
                className={styles.pageLink}
                onClick={() => onPageChange(page)}
                aria-current={isActive ? "page" : undefined}
                aria-label={`Go to page ${page}`}
              >
                {page}
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
