// يجلب خطوط صفحات مصحف المدينة الـ٦٠٤ (مجمع الملك فهد، الإصدار الثاني) إلى fonts/qcf
// لتُضمَّن في تطبيق أندرويد فيعمل المصحف دون إنترنت. يتخطى ما جُلب من قبل.
import { mkdirSync, existsSync, writeFileSync, statSync } from 'node:fs';

const DIR = 'fonts/qcf';
const URL = p => `https://verses.quran.foundation/fonts/quran/hafs/v2/woff2/p${p}.woff2`;
mkdirSync(DIR, { recursive: true });
const todo = [];
for (let p = 1; p <= 604; p++) {
  const f = `${DIR}/p${p}.woff2`;
  if (!existsSync(f) || statSync(f).size < 1000) todo.push(p);
}
let failed = [];
async function worker() {
  while (todo.length) {
    const p = todo.shift();
    for (let i = 0; ; i++) {
      try {
        const r = await fetch(URL(p));
        if (!r.ok) throw new Error(r.status);
        writeFileSync(`${DIR}/p${p}.woff2`, Buffer.from(await r.arrayBuffer()));
        break;
      } catch (e) {
        if (i >= 3) { failed.push(p); break; }
        await new Promise(res => setTimeout(res, 1000 * (i + 1)));
      }
    }
  }
}
const n = todo.length;
await Promise.all(Array.from({ length: 12 }, worker));
if (failed.length) { console.error('تعذّر جلب الصفحات:', failed.join(' ')); process.exit(1); }
console.log(n ? `جُلب ${n} خطًا` : 'خطوط المصحف كاملة');
