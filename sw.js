// تخزين واجهة التطبيق وسور المصحف والتفسير لتعمل دون اتصال
const SHELL = 'nur-shell-v12';
const DATA = 'nur-data-v3';
const FILES = [
  './', 'index.html', 'css/style.css', 'css/fonts.css', 'manifest.webmanifest',
  'js/app.js', 'js/core.js', 'js/native.js', 'js/mushafFont.js', 'js/warshData.js', 'js/khatma.js', 'js/reminders.js', 'js/hijri.js', 'js/search.js', 'js/downloads.js', 'js/player.js', 'js/prayer.js',
  'js/data/surahs.js', 'js/data/mawaiz.js', 'js/data/adhkar.js',
  'js/pages/home.js', 'js/pages/quran.js', 'js/pages/mawaiz.js', 'js/pages/adhkar.js', 'js/pages/adhan.js', 'js/pages/downloads.js', 'js/pages/warsh.js', 'js/pages/khatma.js', 'js/pages/hadith.js', 'js/pages/calendar.js', 'js/pages/ruqya.js', 'js/pages/library.js', 'js/data/anbiya.js',
  'data/lectures.json', 'data/warsh-index.json', 'data/library.json', 'js/vendor/pdf.min.js', 'js/vendor/pdf.worker.min.js', 'icons/icon.svg', 'icons/icon-192.png', 'icons/icon-512.png',
];

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

  // نص القرآن والتفسير والخطوط: من التخزين أولًا (لا تتغير)
  const immutable = (url.hostname === 'api.alquran.cloud' && !url.pathname.includes('/search/'))
    || url.hostname.endsWith('fonts.gstatic.com') || url.hostname === 'fonts.googleapis.com';
  if (immutable) {
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
      .then(res => { if (res.ok) { const copy = res.clone(); caches.open(SHELL).then(c => c.put(e.request, copy)); } return res; })
      .catch(() => caches.match(e.request).then(r => r || caches.match('index.html'))));
  }
});

// فتح التطبيق عند الضغط على إشعار الأذان
self.addEventListener('notificationclick', e => {
  e.notification.close();
  e.waitUntil(self.clients.matchAll({ type: 'window' }).then(list => {
    if (list.length) return list[0].focus();
    return self.clients.openWindow('./#/adhan');
  }));
});
