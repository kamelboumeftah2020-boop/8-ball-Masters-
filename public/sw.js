// عامل الخدمة: يجعل اللعبة قابلة للتثبيت وتعمل بدون إنترنت (وضع البوتات)
const CACHE = 'fl5-v1';
self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(['/', '/index.html', '/css/style.css', '/icon.svg', '/manifest.webmanifest'])).then(() => self.skipWaiting()));
});
self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((ks) => Promise.all(ks.filter((k) => k !== CACHE).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});
// الشبكة أولاً (للحصول على التحديثات) ثم النسخة المخزنة عند انقطاع الإنترنت
self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).pathname.startsWith('/ws')) return;
  e.respondWith(
    fetch(req).then((res) => {
      if (res.ok && new URL(req.url).origin === location.origin) {
        const copy = res.clone();
        caches.open(CACHE).then((c) => c.put(req, copy));
      }
      return res;
    }).catch(() => caches.match(req).then((r) => r || caches.match('/index.html'))),
  );
});
