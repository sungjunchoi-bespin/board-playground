import { useState, FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import { loginApi } from "@/api/auth";
import { parseApiErrors } from "@/utils/errors";
import styles from "./auth-page.module.css";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErrors([]);
    setLoading(true);

    try {
      const user = await loginApi(email, password);
      login(user);
      navigate("/");
    } catch (err) {
      setErrors(parseApiErrors(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="container page">
        <div className="row">
          <div className="col-md-6 offset-md-3 col-xs-12">
            <h1 className="text-xs-center">Sign in</h1>
            <p className="text-xs-center">
              <Link to="/register">Need an account?</Link>
            </p>

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
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </fieldset>
                <fieldset className="form-group">
                  <input
                    className="form-control form-control-lg"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </fieldset>
                <button
                  className={`btn btn-lg btn-primary pull-xs-right ${styles.submitBtn}`}
                  type="submit"
                >
                  Sign in
                </button>
              </fieldset>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
