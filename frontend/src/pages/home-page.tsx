function HomePage() {
  return (
    <div className="home-page">
      <div className="banner">
        <div className="container">
          <h1 className="logo-font">conduit</h1>
          <p>A place to share your knowledge.</p>
        </div>
      </div>
      <div className="container page">
        <div className="row">
          <div className="col-md-9">
            <p>Articles will appear here.</p>
          </div>
          <div className="col-md-3">
            <p>Popular tags will appear here.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
