import { $, $$, store } from './core.js';
import './player.js';
import { cfg, loadTimes } from './prayer.js';
import { isNative, setBarsStyle } from './native.js';
import { renderHome } from './pages/home.js';
import { renderMushaf, renderListen } from './pages/quran.js';
import { renderMawaiz } from './pages/mawaiz.js';
import { renderAdhkar, renderTasbih, renderQibla } from './pages/adhkar.js';
import { renderAdhan } from './pages/adhan.js';

/* ── المظهر ── */
const root = document.documentElement;
function applyTheme(t) {
  root.dataset.theme = t;
  $('meta[name="theme-color"]').content = t === 'dark' ? '#0d1513' : '#f5f3ec';
  setBarsStyle(t === 'dark');
}
applyTheme(store.get('theme', matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'));
$('#themeBtn').onclick = () => {
  const t = root.dataset.theme === 'dark' ? 'light' : 'dark';
  applyTheme(t);
  store.set('theme', t);
};

/* ── التوجيه ── */
const ROUTES = {
  home: { render: renderHome, title: 'نور', tab: 'home' },
  mushaf: { render: renderMushaf, title: 'القرآن الكريم', tab: 'quran' },
  listen: { render: renderListen, title: 'القرآن الكريم', tab: 'quran' },
  mawaiz: { render: renderMawaiz, title: 'المواعظ', tab: 'mawaiz' },
  adhkar: { render: renderAdhkar, title: 'الأذكار', tab: 'adhkar' },
  tasbih: { render: renderTasbih, title: 'السبحة', tab: 'adhkar' },
  qibla: { render: renderQibla, title: 'اتجاه القبلة', tab: 'adhan' },
  adhan: { render: renderAdhan, title: 'مواقيت الصلاة', tab: 'adhan' },
};

const view = $('#view');
let cleanups = [];
let renderId = 0;
let backTarget = null;

function render() {
  cleanups.forEach(fn => { try { fn(); } catch { /* تجاهل */ } });
  cleanups = [];
  $$('.sheet-wrap').forEach(el => el.remove());
  const id = ++renderId;
  const parts = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean).map(decodeURIComponent);
  const route = ROUTES[parts[0]] || ROUTES.home;
  $$('.tabbar a').forEach(a => a.classList.toggle('active', a.dataset.tab === route.tab));
  backTarget = null;
  $('#backBtn').hidden = true;
  $('#pageTitle').textContent = route.title;
  document.title = route.title === 'نور' ? 'نور' : `${route.title} · نور`;
  const ctx = {
    alive: () => id === renderId,
    cleanup: fn => cleanups.push(fn),
    title: t => { if (id === renderId) { $('#pageTitle').textContent = t; document.title = `${t} · نور`; } },
    back: target => { if (id === renderId) { backTarget = target; $('#backBtn').hidden = false; } },
  };
  view.classList.remove('enter'); void view.offsetWidth; view.classList.add('enter');
  route.render(view, parts.slice(1), ctx);
}

window.addEventListener('hashchange', () => { render(); window.scrollTo(0, 0); });
$('#backBtn').onclick = () => {
  if (backTarget === 'history' && history.length > 1) history.back();
  else location.hash = backTarget && backTarget !== 'history' ? backTarget : '#/';
};

render();
if (cfg.loc) loadTimes();
if (isNative) document.documentElement.classList.add('native');

/* ── التثبيت والعمل دون اتصال ── */
if (!isNative && 'serviceWorker' in navigator && location.protocol !== 'file:') {
  navigator.serviceWorker.register('sw.js').catch(() => {});
}
