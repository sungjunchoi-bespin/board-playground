import { Routes, Route } from "react-router-dom";
import { useAuthProvider } from "@/hooks/use-auth";
import Header from "@/components/header";
import Footer from "@/components/footer";
import HomePage from "@/pages/home-page";
import LoginPage from "@/pages/login-page";
import RegisterPage from "@/pages/register-page";
import SettingsPage from "@/pages/settings-page";
import EditorPage from "@/pages/editor-page";
import ArticlePage from "@/pages/article-page";
import ProfilePage from "@/pages/profile-page";

function App() {
  const { value, Provider } = useAuthProvider();

  return (
    <Provider value={value}>
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="/editor" element={<EditorPage />} />
        <Route path="/editor/:slug" element={<EditorPage />} />
        <Route path="/article/:slug" element={<ArticlePage />} />
        <Route path="/profile/:username" element={<ProfilePage />} />
        <Route path="/profile/:username/favorites" element={<ProfilePage />} />
      </Routes>
      <Footer />
    </Provider>
  );
}

export default App;
