import { useEffect, useRef } from "react";
import type { PluginListenerHandle } from "@capacitor/core";
import { App as CapApp } from "@capacitor/app";
import { HashRouter, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { isNative } from "./native";
import { BottomNav } from "./components/BottomNav";
import { ErrorBoundary } from "./components/ErrorBoundary";
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
  // Block body on purpose: newer WebViews return a Promise from scrollTo, and React
  // would treat a returned value as the effect's cleanup function.
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}

/** Android back button: close the player, go back, or send the app to the background. */
function BackHandler() {
  const { expanded, setExpanded, isPlaying } = usePlayer();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const latest = useRef({ expanded, setExpanded, isPlaying, navigate, pathname });
  latest.current = { expanded, setExpanded, isPlaying, navigate, pathname };

  useEffect(() => {
    if (!isNative) return;
    let handle: PluginListenerHandle | undefined;
    let cancelled = false;
    CapApp.addListener("backButton", () => {
      const s = latest.current;
      if (s.expanded) s.setExpanded(false);
      else if (s.pathname !== "/") s.navigate(-1);
      else if (s.isPlaying) CapApp.minimizeApp().catch(() => {});
      else CapApp.exitApp().catch(() => {});
    })
      .then((h) => {
        if (cancelled) h.remove().catch(() => {});
        else handle = h;
      })
      .catch((e) => console.warn("backButton listener failed", e));
    return () => {
      cancelled = true;
      handle?.remove().catch(() => {});
    };
  }, []);
  return null;
}

function Shell() {
  const { current } = usePlayer();
  const { pathname } = useLocation();
  return (
    <div className={`app ${current ? "has-player" : ""}`}>
      <ScrollToTop />
      <BackHandler />
      <main>
        <ErrorBoundary resetKey={pathname}>
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
        </ErrorBoundary>
      </main>
      <div className="dock">
        <MiniPlayer />
        <BottomNav />
      </div>
      <ErrorBoundary>
        <FullPlayer />
      </ErrorBoundary>
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
