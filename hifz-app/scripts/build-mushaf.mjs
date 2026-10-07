// يبني بيانات المصحف (صفحات مصحف المدينة) لروايتي حفص وورش من بيانات مجمع الملك فهد.
//   node scripts/build-mushaf.mjs
// المخرجات: assets/quran/hafs.json و assets/quran/warsh.json و assets/fonts/*.ttf
//
// كل آية: [سورة, آية, صفحة, سطر_البداية, سطر_النهاية, جزء, ربع(1..240), النص, آية_حفص_من, آية_حفص_إلى]
// آخر عمودين يربطان ترقيم ورش (المدني الأخير) بترقيم حفص الذي تستعمله ملفات الصوت في everyayah.
import fs from 'fs';

const KFG = 'https://raw.githubusercontent.com/thetruetruth/quran-data-kfgqpc/main';
const FAW = 'https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1';
const get = async (u, bin) => { const r = await fetch(u); if (!r.ok) throw new Error(`${r.status} ${u}`); return bin ? Buffer.from(await r.arrayBuffer()) : r.json(); };

const norm = (w) => w
  .replace(/[ً-ٰٟۖ-ۭ࣓-ࣿـ‏‎]/g, '')
  .replace(/[أإآٱا]/g, 'ا').replace(/ى/g, 'ي').replace(/[ۥۦئ]/g, (c) => (c === 'ئ' ? 'ي' : ''));
const words = (t) => t.replace(/ /g, ' ').replace(/[٠-٩\d۞۝]/g, ' ').split(/\s+/).map(norm).filter(Boolean);

// ربط كل كلمة في نص (أ) بكلمة في نص (ب) مع إعادة المزامنة عند الاختلاف
function alignWords(a, b) {
  const map = new Array(a.length); let j = 0;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[j]) {
      let found = -1;
      for (let k = 1; k <= 4 && found < 0; k++) if (b[j + k] === a[i]) found = j + k;
      if (found >= 0) j = found;
    }
    map[i] = Math.min(j, b.length - 1); j++;
  }
  return map;
}

const [hafsK, warshK, fawW, info] = await Promise.all([
  get(`${KFG}/hafs/data/hafsData_v18.json`), get(`${KFG}/warsh/data/warshData_v10.json`),
  get(`${FAW}/editions/ara-quranwarsh.min.json`), get(`${FAW}/info.json`),
]);

// الأرباع (240) بترقيم حفص
const quarterOf = new Map();
for (const q of info.maqras.references) {
  const { start, end } = q;
  for (let s = start.chapter; s <= end.chapter; s++) {
    const from = s === start.chapter ? start.verse : 1;
    const to = s === end.chapter ? end.verse : 999;
    for (let a = from; a <= to; a++) quarterOf.set(`${s}:${a}`, q.maqra);
  }
}
const qOf = (s, a) => quarterOf.get(`${s}:${a}`) ?? 1;
const clean = (t) => t.trim().replace(/\s+ /g, ' ');

const hafs = hafsK.map((x) => [x.sora, x.aya_no, +x.page, x.line_start, x.line_end, x.jozz, qOf(x.sora, x.aya_no), clean(x.aya_text), x.aya_no, x.aya_no]);

const warsh = [];
for (let s = 1; s <= 114; s++) {
  const kAyahs = warshK.filter((x) => x.sura_no === s);
  const fAyahs = fawW.quran.filter((x) => x.chapter === s);
  const a = [], aOwner = [];
  kAyahs.forEach((x, i) => words(x.aya_text).forEach((w) => { a.push(w); aOwner.push(i); }));
  const b = [], bOwner = [];
  fAyahs.forEach((x) => words(x.text).forEach((w) => { b.push(w); bOwner.push(x.verse); }));
  // في ترقيم حفص البسملة هي الآية ١ من الفاتحة، وفي ورش لا تُعدّ
  const skip = s === 1 ? words(fAyahs[0].text).length : 0;
  const m = alignWords(a, b.slice(skip));
  kAyahs.forEach((x, i) => {
    const idx = aOwner.map((o, k) => (o === i ? k : -1)).filter((k) => k >= 0);
    let from = bOwner[m[idx[0]] + skip], to = bOwner[m[idx[idx.length - 1]] + skip];
    if (s === 1 && i === 0) from = 1; // نُسمِع البسملة مع أول آية من الفاتحة
    const page = parseInt(String(x.page), 10); // آيات قليلة تمتد على صفحتين "85-86": نعتمد صفحة البداية
    const lineEnd = String(x.page).includes('-') ? 15 : x.line_end;
    warsh.push([s, x.aya_no, page, x.line_start, lineEnd, x.jozz, qOf(s, from), clean(x.aya_text), from, to]);
  });
}

// تحقق: كل آيات حفص مغطاة بالترتيب
for (let s = 1; s <= 114; s++) {
  const rows = warsh.filter((r) => r[0] === s);
  if (rows[rows.length - 1][9] !== fawW.quran.filter((x) => x.chapter === s).length) console.warn('warsh map end mismatch', s);
  for (let i = 1; i < rows.length; i++) if (rows[i][8] < rows[i - 1][9] - 0 && rows[i][8] !== rows[i - 1][9]) console.warn('warsh map order', s, rows[i][1]);
}

fs.mkdirSync('assets/quran', { recursive: true });
fs.mkdirSync('assets/fonts', { recursive: true });
fs.writeFileSync('assets/quran/hafs.json', JSON.stringify(hafs));
fs.writeFileSync('assets/quran/warsh.json', JSON.stringify(warsh));
fs.writeFileSync('assets/fonts/hafs.ttf', await get(`${KFG}/hafs/font/hafs.18.ttf`, true));
fs.writeFileSync('assets/fonts/warsh.ttf', await get(`${KFG}/warsh/font/warsh.10.ttf`, true));
console.log('hafs', hafs.length, 'ayahs ·', 'warsh', warsh.length, 'ayahs');
