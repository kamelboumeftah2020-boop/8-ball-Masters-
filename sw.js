// تخزين واجهة التطبيق وسور المصحف لتعمل دون اتصال
const SHELL = 'nur-shell-v1';
const DATA = 'nur-data-v1';
const FILES = ['./', 'index.html', 'css/style.css', 'js/app.js', 'js/surahs.js', 'js/mawaiz.js', 'manifest.webmanifest', 'icons/icon.svg'];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(SHELL).then(c => c.addAll(FILES)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(caches.keys()
    .then(keys => Promise.all(keys.filter(k => k !== SHELL && k !== DATA).map(k => caches.delete(k))))
    .then(() => self.clients.claim()));
});

self.addEventListener('fetch', e => {
  const url = new URL(e.request.url);
  if (e.request.method !== 'GET') return;

  // نص القرآن والخطوط: من التخزين أولًا
  if (url.hostname === 'api.alquran.cloud' || url.hostname.endsWith('fonts.gstatic.com') || url.hostname === 'fonts.googleapis.com') {
    e.respondWith(caches.open(DATA).then(async c => {
      const hit = await c.match(e.request);
      if (hit) return hit;
      const res = await fetch(e.request);
      if (res.ok) c.put(e.request, res.clone());
      return res;
    }));
    return;
  }

  // ملفات التطبيق: الشبكة أولًا ثم التخزين
  if (url.origin === location.origin) {
    e.respondWith(fetch(e.request)
      .then(res => { const copy = res.clone(); caches.open(SHELL).then(c => c.put(e.request, copy)); return res; })
      .catch(() => caches.match(e.request).then(r => r || caches.match('index.html'))));
  }
});
