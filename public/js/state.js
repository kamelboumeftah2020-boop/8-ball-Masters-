const listeners = new Set();

export const state = {
  user: null,
  socket: null,
  cues: [],
  avatars: [],
  tables: [],
  currentMatch: null, // live match snapshot from server
  pendingRevengeMatchId: null,
};

export function setUser(user) {
  state.user = user;
  if (user?.id) {
    try { localStorage.setItem('bm_userId', user.id); } catch {}
  }
  emit();
}

export function getSavedUserId() {
  try { return localStorage.getItem('bm_userId'); } catch { return null; }
}

export function subscribe(fn) { listeners.add(fn); return () => listeners.delete(fn); }
function emit() { for (const fn of listeners) fn(state); }
export { emit as notify };
