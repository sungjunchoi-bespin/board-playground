import { Routes, Route } from "react-router-dom";

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <div className="container">
            <h1>Conduit</h1>
            <p>A place to share your knowledge.</p>
          </div>
        }
      />
    </Routes>
  );
}

export default App;
