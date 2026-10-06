import { Capacitor, SystemBars, SystemBarsStyle } from "@capacitor/core";
import { load, save } from "./storage";

export type ThemePref = "system" | "dark" | "light";

const KEY = "sada.theme";
const media = window.matchMedia?.("(prefers-color-scheme: light)");

export const getThemePref = (): ThemePref => load<ThemePref>(KEY, "system");

function resolve(pref: ThemePref): "dark" | "light" {
  if (pref === "system") return media?.matches ? "light" : "dark";
  return pref;
}

export function applyTheme(pref: ThemePref = getThemePref()) {
  const theme = resolve(pref);
  document.documentElement.dataset.theme = theme;
  document.querySelector('meta[name="theme-color"]')?.setAttribute("content", theme === "light" ? "#f5f4fa" : "#0b0b14");
  if (Capacitor.isNativePlatform()) {
    // DARK style = light icons for a dark background, and vice versa.
    SystemBars.setStyle({ style: theme === "light" ? SystemBarsStyle.Light : SystemBarsStyle.Dark }).catch(() => {});
  }
}

export function setThemePref(pref: ThemePref) {
  save(KEY, pref);
  applyTheme(pref);
}

export function initTheme() {
  applyTheme();
  media?.addEventListener?.("change", () => getThemePref() === "system" && applyTheme());
}
