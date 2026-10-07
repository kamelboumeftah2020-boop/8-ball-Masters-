// بيانات مصحف المدينة (مجمع الملك فهد) مضمّنة في التطبيق، تُحمَّل عند أول استعمال لكل رواية
import type { Riwaya } from '../db/queries';

// [سورة, آية, صفحة, سطر_البداية, سطر_النهاية, جزء, ربع, النص, آية_حفص_من, آية_حفص_إلى]
type Raw = [number, number, number, number, number, number, number, string, number, number];

export type MAyah = {
  surah: number; ayah: number; page: number; lineStart: number; lineEnd: number;
  juz: number; quarter: number; text: string; hafsFrom: number; hafsTo: number;
};
export type Mushaf = {
  ayahs: MAyah[];
  pages: MAyah[][];                 // pages[n] = آيات الصفحة n (1..604)
  counts: number[];                 // counts[s-1] = عدد آيات السورة
  surahPage: number[];              // surahPage[s-1] = صفحة بداية السورة
  juzPage: number[];                // juzPage[j-1] = صفحة بداية الجزء
  find: (surah: number, ayah: number) => MAyah | undefined;
};

export const PAGE_COUNT = 604;
export const LINES = 15;

const loaders: Record<Riwaya, () => Raw[]> = {
  hafs: () => require('../../assets/quran/hafs.json'),
  warsh: () => require('../../assets/quran/warsh.json'),
};
const cache: Partial<Record<Riwaya, Mushaf>> = {};

export function getMushaf(r: Riwaya): Mushaf {
  const hit = cache[r];
  if (hit) return hit;
  const ayahs: MAyah[] = loaders[r]().map(([surah, ayah, page, lineStart, lineEnd, juz, quarter, text, hafsFrom, hafsTo]) =>
    ({ surah, ayah, page, lineStart, lineEnd, juz, quarter, text, hafsFrom, hafsTo }));
  const pages: MAyah[][] = Array.from({ length: PAGE_COUNT + 1 }, () => []);
  const counts = new Array(114).fill(0), surahPage = new Array(114).fill(0), juzPage = new Array(30).fill(0);
  const index = new Map<number, MAyah>();
  for (const a of ayahs) {
    pages[a.page].push(a);
    counts[a.surah - 1] = Math.max(counts[a.surah - 1], a.ayah);
    if (!surahPage[a.surah - 1]) surahPage[a.surah - 1] = a.page;
    if (!juzPage[a.juz - 1]) juzPage[a.juz - 1] = a.page;
    index.set(a.surah * 1000 + a.ayah, a);
  }
  const m: Mushaf = { ayahs, pages, counts, surahPage, juzPage, find: (s, a) => index.get(s * 1000 + a) };
  cache[r] = m;
  return m;
}

export const pageOf = (r: Riwaya, surah: number, ayah: number) => getMushaf(r).find(surah, ayah)?.page ?? getMushaf(r).surahPage[surah - 1] ?? 1;

// ملفات الصوت (ترقيم حفص) التي تغطي آية في الرواية المختارة
export function hafsAyahsOf(r: Riwaya, surah: number, ayah: number): number[] {
  if (r === 'hafs') return [ayah];
  const a = getMushaf(r).find(surah, ayah);
  if (!a) return [ayah];
  const out: number[] = [];
  for (let i = a.hafsFrom; i <= a.hafsTo; i++) out.push(i);
  return out;
}

// تسمية الحزب والربع كما في هامش المصحف
const FRACTION = ['', '¼ ', '½ ', '¾ '];
export function hizbLabel(quarter: number, arNum: (n: number) => string) {
  return `${FRACTION[(quarter - 1) % 4]}الحزب ${arNum(Math.ceil(quarter / 4))}`;
}

export const BASMALA: Record<Riwaya, string> = {
  hafs: 'بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ',
  warsh: 'بِسْمِ اِ۬للَّهِ اِ۬لرَّحْمَٰنِ اِ۬لرَّحِيمِ',
};

// في حفص بسملة الفاتحة هي آيتها الأولى، وفي ورش لا تُعدّ؛ والتوبة بلا بسملة
export const hasBasmalaHeader = (r: Riwaya, surah: number) => surah !== 9 && !(r === 'hafs' && surah === 1);

// تقسيم الصفحة إلى كتل مرتبة بالسطر: عنوان سورة، بسملة، أو فقرة آيات
export type Block =
  | { kind: 'title'; surah: number; line: number }
  | { kind: 'basmala'; line: number }
  | { kind: 'text'; ayahs: MAyah[]; line: number; lines: number };

export function pageBlocks(r: Riwaya, page: number): Block[] {
  const rows = getMushaf(r).pages[page] ?? [];
  const blocks: Block[] = [];
  let i = 0, prevEnd = 0;
  while (i < rows.length) {
    const s = rows[i].surah;
    const seg: MAyah[] = [];
    while (i < rows.length && rows[i].surah === s) seg.push(rows[i++]);
    const first = seg[0], last = seg[seg.length - 1];
    if (first.ayah === 1) {
      const room = first.lineStart - prevEnd - 1; // الأسطر الفارغة قبل أول آية
      const basmala = hasBasmalaHeader(r, s);
      const titleLine = Math.max(1, first.lineStart - (basmala ? 2 : 1));
      blocks.push({ kind: 'title', surah: s, line: room >= 1 ? titleLine : Math.max(1, first.lineStart - 1) });
      if (basmala && room >= 2) blocks.push({ kind: 'basmala', line: first.lineStart - 1 });
    }
    blocks.push({ kind: 'text', ayahs: seg, line: first.lineStart, lines: Math.max(1, last.lineEnd - first.lineStart + 1) });
    prevEnd = last.lineEnd;
  }
  return blocks;
}
