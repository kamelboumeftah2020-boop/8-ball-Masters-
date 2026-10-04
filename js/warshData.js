// مصحف ورش: يُقرأ من الإنترنت، ويمكن تحميله للقراءة دون اتصال.
// المصدر: بيانات مجمع الملك فهد (KFGQPC Warsh v10) وخطه، ولكل آية رقم صفحتها وسطرا بدايتها ونهايتها.
const BASE = 'https://cdn.jsdelivr.net/gh/thetruetruth/quran-data-kfgqpc@main/warsh/';
const SRC = BASE + 'data/warshData_v10.json';
const FONT = BASE + 'font/warsh.10.woff2';
const CACHE = 'nur-warsh-v1';
export const WARSH_SIZE_MB = 3;

let job = null;

async function cached(url) {
  if (!('caches' in window)) return null;
  try { return await (await caches.open(CACHE)).match(url); } catch { return null; }
}

async function get(url) {
  const hit = await cached(url);
  if (hit) return hit;
  const r = await fetch(url);
  if (!r.ok) throw new Error(r.status);
  return r;
}

export async function isDownloaded() {
  return !!((await cached(SRC)) && (await cached(FONT)));
}

export async function download() {
  const c = await caches.open(CACHE);
  for (const url of [FONT, SRC]) {
    if (await c.match(url)) continue;
    const r = await fetch(url);
    if (!r.ok) throw new Error(r.status);
    await c.put(url, r);
  }
}

export async function removeDownload() {
  if ('caches' in window) await caches.delete(CACHE);
}

const clean = t => t.replace(/‏/g, '').trim().replace(/[ \t]+/g, ' ');

// آية ممتدة بين صفحتين: تُقسم كلماتها بنسبة عدد أسطرها في كل صفحة
function splitWords(text, first, second) {
  const j = text.lastIndexOf(' ');
  const body = text.slice(0, j), num = text.slice(j + 1);
  const words = body.split(' ');
  const total = words.reduce((t, w) => t + w.length, 0);
  const target = total * first / (first + second);
  let acc = 0, k = 0;
  while (k < words.length - 1 && acc + words[k].length / 2 < target) { acc += words[k].length + 1; k++; }
  return [words.slice(0, k).join(' '), words.slice(k).join(' ') + ' ' + num];
}

/* يبني الصفحات: لكل صفحة {j: الجزء، b: كتل}، والكتلة:
   ['h', سورة] اسم السورة، ['b'] البسملة، ['t', عدد الأسطر، [[سورة، آية، نص، ختم]...]] */
function build(data) {
  const pages = Array.from({ length: 605 }, () => []);
  for (const x of data) {
    const ps = String(x.page).split('-').map(Number);
    const text = clean(x.aya_text);
    const base = { s: x.sura_no, a: x.aya_no, j: x.jozz };
    if (ps.length === 1) {
      pages[ps[0]].push({ ...base, t: text, ls: x.line_start, le: x.line_end, end: 1 });
    } else {
      const [t1, t2] = splitWords(text, 15 - x.line_start + 1, x.line_end);
      pages[ps[0]].push({ ...base, t: t1, ls: x.line_start, le: 15, end: 0 });
      pages[ps[1]].unshift({ ...base, t: t2, ls: 1, le: x.line_end, end: 1 });
    }
  }
  const out = [];
  for (let p = 1; p <= 604; p++) {
    const items = pages[p];
    const blocks = [];
    let cur = 0, block = null, start = 0;
    for (const it of items) {
      if (it.a === 1 && it.end === 1 && it.ls > cur) {
        const gap = it.ls - cur - 1;
        block = null;
        if (gap >= 1) blocks.push(['h', it.s]);
        if (gap >= 2 && it.s !== 9) blocks.push(['b']);
      }
      if (!block) {
        start = Math.max(it.ls, cur + 1);
        block = ['t', 0, []];
        blocks.push(block);
      }
      block[2].push([it.s, it.a, it.t, it.end]);
      cur = Math.max(cur, it.le);
      block[1] = cur - start + 1;
    }
    out.push({ j: items[0]?.j || 1, b: blocks });
  }
  return out;
}

// يحمّل البيانات والخط مرة واحدة في الجلسة
export function loadWarsh() {
  if (!job) {
    job = (async () => {
      const [dataRes, fontRes] = await Promise.all([get(SRC), get(FONT)]);
      const [data, font] = await Promise.all([dataRes.json(), fontRes.arrayBuffer()]);
      const face = new FontFace('KFGQPC Warsh', font, { display: 'block' });
      await face.load();
      document.fonts.add(face);
      return build(data);
    })().catch(e => { job = null; throw e; });
  }
  return job;
}
