import { useParams } from "react-router-dom";

function ProfilePage() {
  const { username } = useParams<{ username: string }>();

  return (
    <div className="profile-page">
      <div className="user-info">
        <div className="container">
          <div className="row">
            <div className="col-xs-12 col-md-10 offset-md-1">
              <h4>{username}</h4>
              <p className="text-muted">
                Profile will be implemented in Sprint 3.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProfilePage;
