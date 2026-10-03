// أدوات مشتركة
export const $ = (s, el = document) => el.querySelector(s);
export const $$ = (s, el = document) => [...el.querySelectorAll(s)];

export const store = {
  get(k, d) { try { const v = localStorage.getItem('nur:' + k); return v == null ? d : JSON.parse(v); } catch { return d; } },
  set(k, v) { try { localStorage.setItem('nur:' + k, JSON.stringify(v)); } catch { /* التخزين غير متاح */ } },
};

export const arNum = n => Number(n).toLocaleString('ar-EG', { useGrouping: false });
export const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
export const pad3 = n => String(n).padStart(3, '0');
export const normalize = s => String(s)
  .replace(/[ً-ٰٟۖ-ۭـ]/g, '')
  .replace(/[أإآٱ]/g, 'ا').replace(/ى/g, 'ي').replace(/ة/g, 'ه').toLowerCase().trim();

export function fmtDur(sec) {
  if (!isFinite(sec) || sec <= 0) return '0:00';
  sec = Math.floor(sec);
  const h = Math.floor(sec / 3600), m = Math.floor(sec % 3600 / 60), s = sec % 60;
  return (h ? h + ':' + String(m).padStart(2, '0') : m) + ':' + String(s).padStart(2, '0');
}
export function durLabel(sec) {
  const m = Math.round(sec / 60);
  if (m < 60) return `${arNum(m)} دقيقة`;
  return `${arNum(Math.floor(m / 60))} س ${m % 60 ? arNum(m % 60) + ' د' : ''}`;
}

export const todayKey = (d = new Date()) => `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`;

export function toast(msg) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(toast.t);
  toast.t = setTimeout(() => t.classList.remove('show'), 2600);
}

export async function fetchJSON(url) {
  const r = await fetch(url);
  if (!r.ok) throw new Error(r.status);
  return r.json();
}

export async function shareText(text) {
  if (navigator.share) { try { await navigator.share({ text }); } catch { /* أُلغيت المشاركة */ } return; }
  copyText(text);
}
export function copyText(text) {
  if (!navigator.clipboard) return toast('النسخ غير متاح في هذا المتصفح');
  navigator.clipboard.writeText(text).then(() => toast('تم النسخ'), () => toast('تعذّر النسخ'));
}

export function vibrate(p) { try { navigator.vibrate?.(p); } catch { /* غير مدعوم */ } }

// نافذة سفلية عامة
export function sheet(html, { onClick, label = 'نافذة' } = {}) {
  const wrap = document.createElement('div');
  wrap.className = 'sheet-wrap';
  wrap.innerHTML = `<div class="sheet" role="dialog" aria-label="${esc(label)}"><div class="grab"></div>${html}</div>`;
  const close = () => { wrap.classList.add('closing'); setTimeout(() => wrap.remove(), 180); };
  wrap.addEventListener('click', e => {
    if (e.target === wrap) return close();
    onClick?.(e, close);
  });
  document.body.append(wrap);
  return { el: wrap, close };
}

const S = (d, extra = '') => `<svg viewBox="0 0 24 24"${extra}>${d}</svg>`;
export const icons = {
  play: S('<path class="fill" d="M8 5.5v13a1 1 0 0 0 1.5.86l10.4-6.5a1 1 0 0 0 0-1.72L9.5 4.64A1 1 0 0 0 8 5.5z"/>'),
  pause: S('<rect class="fill" x="6" y="5" width="4.2" height="14" rx="1.2"/><rect class="fill" x="13.8" y="5" width="4.2" height="14" rx="1.2"/>'),
  prev: S('<path class="fill" d="M18 6.5v11a1 1 0 0 1-1.6.8L9 12.8a1 1 0 0 1 0-1.6l7.4-5.5a1 1 0 0 1 1.6.8z"/><path d="M6 6v12"/>'),
  next: S('<path class="fill" d="M6 6.5v11a1 1 0 0 0 1.6.8l7.4-5.5a1 1 0 0 0 0-1.6L7.6 5.7A1 1 0 0 0 6 6.5z"/><path d="M18 6v12"/>'),
  back15: S('<path d="M4 12a8 8 0 1 0 2.3-5.6M4 4v4h4"/><text x="12" y="15.5" text-anchor="middle" font-size="7.5" font-weight="700" fill="currentColor" stroke="none">15</text>'),
  fwd15: S('<path d="M20 12a8 8 0 1 1-2.3-5.6M20 4v4h-4"/><text x="12" y="15.5" text-anchor="middle" font-size="7.5" font-weight="700" fill="currentColor" stroke="none">15</text>'),
  search: S('<circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/>'),
  book: S('<path d="M12 6c-2-1.5-5-2-8-2v14c3 0 6 .5 8 2 2-1.5 5-2 8-2V4c-3 0-6 .5-8 2zM12 6v14"/>'),
  headphones: S('<path d="M3 14v-2a9 9 0 0 1 18 0v2"/><rect x="2.5" y="14" width="5" height="7" rx="2"/><rect x="16.5" y="14" width="5" height="7" rx="2"/>'),
  mic: S('<rect x="9" y="3" width="6" height="11" rx="3"/><path d="M5 11a7 7 0 0 0 14 0M12 18v3"/>'),
  mosque: S('<path d="M12 2.5c0 2.6-4 3.6-4 7.5v1h8v-1c0-3.9-4-4.9-4-7.5zM5 11h14M6 11v10h12V11M10 21v-4a2 2 0 0 1 4 0v4"/>'),
  star: S('<path d="M12 3l2.4 4.9 5.4.8-3.9 3.8.9 5.4L12 15.4 7.2 17.9l.9-5.4L4.2 8.7l5.4-.8z"/>'),
  beads: S('<circle cx="12" cy="4.5" r="1.8"/><circle cx="17.3" cy="6.7" r="1.8"/><circle cx="19.5" cy="12" r="1.8"/><circle cx="17.3" cy="17.3" r="1.8"/><circle cx="6.7" cy="6.7" r="1.8"/><circle cx="4.5" cy="12" r="1.8"/><circle cx="6.7" cy="17.3" r="1.8"/><path d="M12 19v3"/>'),
  compass: S('<circle cx="12" cy="12" r="9"/><path class="fill" d="M15.5 8.5l-2 5-5 2 2-5z"/>'),
  hands: S('<path d="M8 21v-5.5L5.5 12a2 2 0 0 1 .3-2.6L9 6.5V3.5a1.5 1.5 0 0 1 3 0V12M16 21v-5.5l2.5-3.5a2 2 0 0 0-.3-2.6L15 6.5V3.5a1.5 1.5 0 0 0-3 0"/>'),
  bookmark: S('<path d="M6 3h12v18l-6-4-6 4z"/>'),
  share: S('<circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/>'),
  copy: S('<rect x="8" y="8" width="13" height="13" rx="3"/><path d="M16 8V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h3"/>'),
  download: S('<path d="M12 3v12M7 10l5 5 5-5M5 21h14"/>'),
  bell: S('<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.9 1.9 0 0 0 3.4 0"/>'),
  bellOff: S('<path d="M6 8a6 6 0 0 1 9.3-5M18 8c0 7 3 9 3 9H9M6.3 12.5C5.7 15.8 3 17 3 17h3M10.3 21a1.9 1.9 0 0 0 3.4 0M3 3l18 18"/>'),
  pin: S('<path d="M12 22s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12z"/><circle cx="12" cy="10" r="2.5"/>'),
  minus: S('<path d="M5 12h14"/>'),
  plus: S('<path d="M12 5v14M5 12h14"/>'),
  check: S('<path d="M5 12.5l4.5 4.5L19 7.5"/>'),
  reset: S('<path d="M3 12a9 9 0 1 0 3-6.7L3 8M3 3v5h5"/>'),
  close: S('<path d="M6 6l12 12M18 6L6 18"/>'),
  chevron: S('<path d="M15 6l-6 6 6 6"/>'),
  moonSleep: S('<path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z"/><path d="M15 4h4l-4 4h4"/>'),
  repeat: S('<path d="M17 2l3 3-3 3M3 11V9a4 4 0 0 1 4-4h13M7 22l-3-3 3-3M21 13v2a4 4 0 0 1-4 4H4"/>'),
  sunrise: S('<path d="M17 18a5 5 0 0 0-10 0M12 2v7M4.2 10.2l1.4 1.4M1 18h2M21 18h2M18.4 11.6l1.4-1.4M23 22H1M8 6l4-4 4 4"/>'),
  sun: S('<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>'),
  sunset: S('<path d="M17 18a5 5 0 0 0-10 0M12 9V2M4.2 10.2l1.4 1.4M1 18h2M21 18h2M18.4 11.6l1.4-1.4M23 22H1M16 5l-4 4-4-4"/>'),
  moon: S('<path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z"/>'),
  dawn: S('<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/><path d="M19 3v4M17 5h4"/>'),
  info: S('<circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/>'),
  calendar: S('<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M3 10h18M8 3v4M16 3v4"/>'),
  layers: S('<path d="M12 3l9 5-9 5-9-5zM3 13l9 5 9-5"/>'),
};
