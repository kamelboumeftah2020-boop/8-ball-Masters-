import { useEffect } from "react";
import { App as CapApp } from "@capacitor/app";
import { HashRouter, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { isNative } from "./native";
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

/** Android back button: close the player, go back, or send the app to the background. */
function BackHandler() {
  const { expanded, setExpanded, isPlaying } = usePlayer();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  useEffect(() => {
    if (!isNative) return;
    const sub = CapApp.addListener("backButton", () => {
      if (expanded) setExpanded(false);
      else if (pathname !== "/") navigate(-1);
      else if (isPlaying) CapApp.minimizeApp();
      else CapApp.exitApp();
    });
    return () => {
      sub.then((s) => s.remove());
    };
  }, [expanded, setExpanded, isPlaying, navigate, pathname]);
  return null;
}

function Shell() {
  const { current } = usePlayer();
  return (
    <div className={`app ${current ? "has-player" : ""}`}>
      <ScrollToTop />
      <BackHandler />
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
