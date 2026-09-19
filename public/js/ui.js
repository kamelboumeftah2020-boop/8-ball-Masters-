import { state } from './state.js';

export function formatCoins(n) {
  n = Number(n) || 0;
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(n % 1_000_000 === 0 ? 0 : 1) + 'M';
  if (n >= 1_000) return (n / 1_000).toFixed(n % 1_000 === 0 ? 0 : 1) + 'K';
  return String(Math.round(n));
}

export function formatTime(seconds) {
  const s = Math.max(0, seconds);
  const whole = Math.floor(s);
  const cs = Math.floor((s - whole) * 100);
  return `${String(whole).padStart(2, '0')}.${String(cs).padStart(2, '0')}`;
}

export function avatarInfo(avatarId) {
  const a = state.avatars.find(x => x.id === avatarId);
  if (!a) return { emoji: '🎱', gradient: ['#333', '#111'] };
  return a;
}

export function avatarHtml(avatarId, size = '') {
  const a = avatarInfo(avatarId);
  const g = a.gradient || ['#333', '#111'];
  return `<div class="avatar ${size}" style="background:linear-gradient(135deg, ${g[0]}, ${g[1]})">${a.emoji}</div>`;
}

export function toast(msg, ms = 2600) {
  const root = document.getElementById('toast-root');
  const el = document.createElement('div');
  el.className = 'toast';
  el.textContent = msg;
  root.appendChild(el);
  setTimeout(() => el.remove(), ms);
}

export function closeModal() {
  document.getElementById('modal-root').innerHTML = '';
}

export function openModal(innerHtml, { onMount } = {}) {
  const root = document.getElementById('modal-root');
  root.innerHTML = `<div class="modal-backdrop" id="modal-bg"><div class="modal-box">${innerHtml}</div></div>`;
  const bg = document.getElementById('modal-bg');
  bg.addEventListener('click', (e) => { if (e.target === bg) closeModal(); });
  if (onMount) onMount(root);
}

export function el(html) {
  const tpl = document.createElement('template');
  tpl.innerHTML = html.trim();
  return tpl.content.firstElementChild;
}
