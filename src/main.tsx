import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App";
import { installCrashHandlers, showCrash } from "./lib/crashReport";
import { startAutoRefresh } from "./lib/refresh";
import { initTheme } from "./lib/theme";
import { isNative } from "./native";

installCrashHandlers();
startAutoRefresh();
initTheme();
import "./styles.css";

createRoot(document.getElementById("root")!, {
  onUncaughtError: (error) => showCrash(error, "React"),
}).render(
  <StrictMode>
    <App />
  </StrictMode>
);

if ("serviceWorker" in navigator && import.meta.env.PROD && !isNative) {
  window.addEventListener("load", () => navigator.serviceWorker.register("./sw.js").catch(() => {}));
}
