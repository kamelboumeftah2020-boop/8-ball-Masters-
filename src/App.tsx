import { useEffect } from "react";
import { HashRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { BottomNav } from "./components/BottomNav";
import { FullPlayer } from "./components/FullPlayer";
import { MiniPlayer } from "./components/MiniPlayer";
import { Chart } from "./pages/Chart";
import { Downloads } from "./pages/Downloads";
import { Explore } from "./pages/Explore";
import { Home } from "./pages/Home";
import { Library } from "./pages/Library";
import { PodcastPage } from "./pages/PodcastPage";
import { LibraryProvider } from "./store/library";
import { PlayerProvider, usePlayer } from "./store/player";

function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => window.scrollTo(0, 0), [pathname]);
  return null;
}

function Shell() {
  const { current } = usePlayer();
  return (
    <div className={`app ${current ? "has-player" : ""}`}>
      <ScrollToTop />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/explore" element={<Explore />} />
          <Route path="/top" element={<Chart />} />
          <Route path="/genre/:id" element={<Chart />} />
          <Route path="/podcast/:id" element={<PodcastPage />} />
          <Route path="/library" element={<Library />} />
          <Route path="/downloads" element={<Downloads />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <div className="dock">
        <MiniPlayer />
        <BottomNav />
      </div>
      <FullPlayer />
    </div>
  );
}

export default function App() {
  return (
    <HashRouter>
      <LibraryProvider>
        <PlayerProvider>
          <Shell />
        </PlayerProvider>
      </LibraryProvider>
    </HashRouter>
  );
}
