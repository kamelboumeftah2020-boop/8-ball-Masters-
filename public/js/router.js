const screens = {};
let current = null;
let cleanup = null;

export function registerScreens(map) {
  Object.assign(screens, map);
}

// A screen can register a cleanup callback (e.g. unsubscribe socket listeners,
// leave a matchmaking queue) that runs right before the next navigation away from it.
export function onLeave(fn) { cleanup = fn; }

export function navigate(name, params = {}) {
  const fn = screens[name];
  if (!fn) { console.error('Unknown screen', name); return; }
  if (cleanup) { try { cleanup(); } catch (e) { console.error(e); } cleanup = null; }
  current = { name, params };
  const root = document.getElementById('app');
  root.innerHTML = '';
  fn(root, params);
}

export function currentScreen() { return current; }
export function reloadCurrent() { if (current) navigate(current.name, current.params); }
