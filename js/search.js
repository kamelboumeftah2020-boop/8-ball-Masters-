// البحث في نص المصحف دون إنترنت (حفص من الملفات المضمّنة، وورش من نصه المحمّل).
// يُقارن «هيكل» الكلمات: بلا تشكيل ولا ألفات ولا همزات، ليطابق الرسم العثماني ما يكتبه الباحث عادةً
// (فـ«الصلاة» تطابق «ٱلصَّلَوٰة»، و«يؤمنون» تطابق «يُومِنُونَ» في ورش).
import { fetchJSON } from './core.js';
import { loadWarsh } from './warshData.js';

export function skeleton(s) {
  return String(s)
    .replace(/[ً-ٟ]/g, '')
    .replace(/وٰ/g, '')                       // الصلوٰة ← الصلة
    .replace(/[ؐ-ؚٰۖ-ۭ࣓-ࣿـ ]/g, ' ')
    .replace(/[٠-٩0-9]/g, ' ')
    .replace(/[أإآٱ]/g, 'ا').replace(/ؤ/g, 'و').replace(/[ئىے]/g, 'ي').replace(/ة/g, 'ه')
    .replace(/[اء]/g, '')
    .replace(/\s+/g, ' ').trim();
}

let hafsIdx = null;
async function hafsIndex() {
  if (!hafsIdx) {
    hafsIdx = Promise.all(Array.from({ length: 114 }, (_, i) => fetchJSON(`data/quran/${i + 1}.json`)))
      .then(all => all.flatMap((ayahs, i) => ayahs.map(a => ({ s: i + 1, a: a[1], t: a[2], k: ' ' + skeleton(a[2]) + ' ' }))))
      .catch(e => { hafsIdx = null; throw e; });
  }
  return hafsIdx;
}

let warshIdx = null;
async function warshIndex() {
  if (!warshIdx) {
    warshIdx = loadWarsh().then(pages => {
      const map = new Map();
      pages.forEach((pg, i) => pg.b.forEach(b => {
        if (b[0] !== 't') return;
        for (const [s, a, t] of b[2]) {
          const key = `${s}:${a}`;
          const old = map.get(key);
          // الآية الممتدة بين صفحتين: يُجمع جزآها، وتُنسب إلى صفحة بدايتها
          if (old) old.t += ' ' + t; else map.set(key, { s, a, t, p: i + 1 });
        }
      }));
      return [...map.values()].map(x => ({ ...x, k: ' ' + skeleton(x.t) + ' ' }));
    }).catch(e => { warshIdx = null; throw e; });
  }
  return warshIdx;
}

export async function searchMushaf(q, warsh, limit = 100) {
  const needle = skeleton(q);
  if (needle.length < 2) return { count: 0, items: [] };
  const idx = await (warsh ? warshIndex() : hafsIndex());
  const hits = idx.filter(x => x.k.includes(needle));
  return { count: hits.length, items: hits.slice(0, limit) };
}
