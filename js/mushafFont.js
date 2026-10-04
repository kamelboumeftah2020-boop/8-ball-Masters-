// خطوط صفحات مصحف المدينة (مجمع الملك فهد، الإصدار الثاني).
// في تطبيق أندرويد تأتي الخطوط الـ٦٠٤ مضمّنة (fonts/qcf) فيعمل المصحف دون إنترنت من أول فتح؛
// وفي نسخة الويب تُحمَّل من quran.com عند أول فتح ثم تُحفظ في الجهاز.
const LOCAL_URL = p => `fonts/qcf/p${p}.woff2`;
const FONT_URL = p => `https://verses.quran.foundation/fonts/quran/hafs/v2/woff2/p${p}.woff2`;
const CACHE = 'nur-mushaf-fonts-v2';
const loaded = new Map();

export const fontFamily = p => `QCF2_P${p}`;

let bundledP = null;
export function isBundled() {
  if (!bundledP) bundledP = fetch(LOCAL_URL(1)).then(r => r.ok, () => false);
  return bundledP;
}

async function fontData(p) {
  if (await isBundled()) {
    const r = await fetch(LOCAL_URL(p));
    if (r.ok) return r.arrayBuffer();
  }
  const url = FONT_URL(p);
  if ('caches' in window) {
    try {
      const c = await caches.open(CACHE);
      let r = await c.match(url);
      if (!r) {
        r = await fetch(url);
        if (!r.ok) throw new Error(r.status);
        await c.put(url, r.clone());
      }
      return await r.arrayBuffer();
    } catch (e) {
      if (!navigator.onLine) throw e;
    }
  }
  const r = await fetch(url);
  if (!r.ok) throw new Error(r.status);
  return r.arrayBuffer();
}

export function loadPageFont(p) {
  if (!loaded.has(p)) {
    const job = fontData(p).then(async buf => {
      const face = new FontFace(fontFamily(p), buf, { display: 'block' });
      await face.load();
      document.fonts.add(face);
    }).catch(e => { loaded.delete(p); throw e; });
    loaded.set(p, job);
  }
  return loaded.get(p);
}

// تحميل الصفحات المجاورة مسبقًا ليكون التقليب فوريًا
export function prefetch(p) {
  for (const q of [p + 1, p - 1, p + 2]) if (q >= 1 && q <= 604) loadPageFont(q).catch(() => {});
}

export async function cachedCount() {
  if (await isBundled()) return 604;
  if (!('caches' in window)) return 0;
  try { return (await (await caches.open(CACHE)).keys()).length; } catch { return 0; }
}

// تحميل المصحف كاملًا للقراءة دون اتصال
export async function downloadAll(onProgress, isCancelled) {
  const c = await caches.open(CACHE);
  const have = new Set((await c.keys()).map(r => r.url));
  const todo = [];
  for (let p = 1; p <= 604; p++) if (!have.has(FONT_URL(p))) todo.push(p);
  let done = 604 - todo.length, failed = 0;
  onProgress(done, failed);
  const worker = async () => {
    while (todo.length && !isCancelled()) {
      const p = todo.shift();
      try {
        const r = await fetch(FONT_URL(p));
        if (!r.ok) throw new Error(r.status);
        await c.put(FONT_URL(p), r);
        done++;
      } catch { failed++; }
      onProgress(done, failed);
    }
  };
  await Promise.all([worker(), worker(), worker(), worker()]);
  return { done, failed };
}
