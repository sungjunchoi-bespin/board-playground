import { Link } from "react-router-dom";

function LoginPage() {
  return (
    <div className="auth-page">
      <div className="container page">
        <div className="row">
          <div className="col-md-6 offset-md-3 col-xs-12">
            <h1 className="text-xs-center">Sign in</h1>
            <p className="text-xs-center">
              <Link to="/register">Need an account?</Link>
            </p>
            <p className="text-xs-center text-muted">
              Login form will be implemented in Sprint 1.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
