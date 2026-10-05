import { useSyncExternalStore } from "react";

/**
 * App-wide "refresh now" signal. Screens re-fetch their data whenever it ticks:
 * when the app comes back to the foreground, when the connection returns, and
 * periodically while the app is open.
 */

const STALE_AFTER_MS = 10 * 60_000;
const INTERVAL_MS = 30 * 60_000;

let tick = 0;
let lastRefresh = Date.now();
const listeners = new Set<() => void>();

export function triggerRefresh() {
  tick++;
  lastRefresh = Date.now();
  listeners.forEach((l) => l());
}

export function useRefreshTick() {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l);
      return () => listeners.delete(l);
    },
    () => tick
  );
}

let started = false;
export function startAutoRefresh() {
  if (started) return;
  started = true;
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible" && Date.now() - lastRefresh > STALE_AFTER_MS) triggerRefresh();
  });
  window.addEventListener("online", () => triggerRefresh());
  setInterval(() => {
    if (document.visibilityState === "visible" && navigator.onLine !== false) triggerRefresh();
  }, INTERVAL_MS);
}
