import { useParams } from "react-router-dom";

function ArticlePage() {
  const { slug } = useParams<{ slug: string }>();

  return (
    <div className="article-page">
      <div className="banner">
        <div className="container">
          <h1>Article: {slug}</h1>
        </div>
      </div>
      <div className="container page">
        <p className="text-muted">
          Article detail will be implemented in Sprint 2.
        </p>
      </div>
    </div>
  );
}

export default ArticlePage;
