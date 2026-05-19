import { useState, useEffect, FormEvent, KeyboardEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  createArticleApi,
  getArticleApi,
  updateArticleApi,
} from "@/api/articles";
import { parseApiErrors } from "@/utils/errors";
import styles from "./editor-page.module.css";

function EditorPage() {
  const { slug } = useParams<{ slug: string }>();
  const isEdit = Boolean(slug);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [body, setBody] = useState("");
  const [tagInput, setTagInput] = useState("");
  const [tagList, setTagList] = useState<string[]>([]);
  const [errors, setErrors] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (slug) {
      getArticleApi(slug).then((article) => {
        setTitle(article.title);
        setDescription(article.description);
        setBody(article.body);
        setTagList(article.tagList);
      });
    }
  }, [slug]);

  function handleTagKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") {
      e.preventDefault();
      const tag = tagInput.trim();
      if (tag && !tagList.includes(tag)) {
        setTagList([...tagList, tag]);
      }
      setTagInput("");
    }
  }

  function removeTag(tag: string) {
    setTagList(tagList.filter((t) => t !== tag));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErrors([]);
    setLoading(true);

    try {
      let article;
      if (isEdit && slug) {
        article = await updateArticleApi(slug, { title, description, body });
      } else {
        article = await createArticleApi({
          title,
          description,
          body,
          tagList,
        });
      }
      navigate(`/article/${article.slug}`);
    } catch (err) {
      setErrors(parseApiErrors(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="editor-page">
      <div className="container page">
        <div className="row">
          <div className="col-md-10 offset-md-1 col-xs-12">
            {errors.length > 0 && (
              <ul className={styles.errorMessages}>
                {errors.map((msg) => (
                  <li key={msg}>{msg}</li>
                ))}
              </ul>
            )}

            <form onSubmit={handleSubmit}>
              <fieldset disabled={loading}>
                <fieldset className="form-group">
                  <input
                    className="form-control form-control-lg"
                    type="text"
                    placeholder="Article Title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                  />
                </fieldset>
                <fieldset className="form-group">
                  <input
                    className="form-control"
                    type="text"
                    placeholder="What's this article about?"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                  />
                </fieldset>
                <fieldset className="form-group">
                  <textarea
                    className="form-control"
                    rows={8}
                    placeholder="Write your article (in markdown)"
                    value={body}
                    onChange={(e) => setBody(e.target.value)}
                    required
                  />
                </fieldset>
                <fieldset className="form-group">
                  <input
                    className="form-control"
                    type="text"
                    placeholder="Enter tags"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyDown={handleTagKeyDown}
                  />
                  <div className={styles.tagList}>
                    {tagList.map((tag) => (
                      <span key={tag} className={styles.tag}>
                        <button
                          type="button"
                          className={styles.tagDelete}
                          onClick={() => removeTag(tag)}
                        >
                          ×
                        </button>
                        {tag}
                      </span>
                    ))}
                  </div>
                </fieldset>
                <button
                  className={`btn btn-lg pull-xs-right btn-primary ${styles.publishBtn}`}
                  type="submit"
                >
                  Publish Article
                </button>
              </fieldset>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EditorPage;
