import { Link } from "react-router-dom";

function RegisterPage() {
  return (
    <div className="auth-page">
      <div className="container page">
        <div className="row">
          <div className="col-md-6 offset-md-3 col-xs-12">
            <h1 className="text-xs-center">Sign up</h1>
            <p className="text-xs-center">
              <Link to="/login">Have an account?</Link>
            </p>
            <p className="text-xs-center text-muted">
              Register form will be implemented in Sprint 1.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
