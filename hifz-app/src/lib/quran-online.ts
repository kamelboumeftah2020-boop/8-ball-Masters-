import { getDb } from '../db';

export type OnlineQuranSource = 'hafs' | 'warsh';

const EDITION = { hafs: 'ara-quranuthmanihaf', warsh: 'ara-quranwarsh' };
// مصدران احتياطيان لنفس الملف
const MIRRORS = [
  (e: string) => `https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/${e}.min.json`,
  (e: string) => `https://raw.githubusercontent.com/fawazahmed0/quran-api/1/editions/${e}.min.json`,
];

type Verse = { chapter: number; verse: number; text: string };

async function fetchEdition(riwaya: OnlineQuranSource): Promise<Verse[]> {
  let lastErr: unknown;
  for (const url of MIRRORS) {
    try {
      const res = await fetch(url(EDITION[riwaya]));
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      if (Array.isArray(data?.quran) && data.quran.length > 6000) return data.quran;
      throw new Error('صيغة غير متوقعة');
    } catch (e) { lastErr = e; }
  }
  throw lastErr;
}

let inflight: Partial<Record<OnlineQuranSource, Promise<number>>> = {};

// يحمّل نص الرواية كاملًا (~2MB) ويخزنه في SQLite. onProgress بالنسبة المئوية.
export function fetchAndCacheQuran(riwaya: OnlineQuranSource, onProgress?: (pct: number) => void) {
  if (inflight[riwaya]) return inflight[riwaya]!;
  const job = (async () => {
    onProgress?.(0);
    const verses = await fetchEdition(riwaya);
    const db = await getDb();
    let done = 0;
    await db.withTransactionAsync(async () => {
      const st = await db.prepareAsync('INSERT OR REPLACE INTO ayahs (riwaya,surah,ayah,text) VALUES (?,?,?,?)');
      try {
        for (const v of verses) {
          await st.executeAsync([riwaya, v.chapter, v.verse, v.text.trim()]);
          if (++done % 300 === 0) onProgress?.(Math.round((done / verses.length) * 100));
        }
      } finally {
        await st.finalizeAsync();
      }
    });
    onProgress?.(100);
    return done;
  })();
  inflight[riwaya] = job;
  job.finally(() => { delete inflight[riwaya]; }).catch(() => {});
  return job;
}

export async function isQuranCached(riwaya: OnlineQuranSource): Promise<boolean> {
  const db = await getDb();
  const row = await db.getFirstAsync<{ c: number }>('SELECT COUNT(*) c FROM ayahs WHERE riwaya=?', [riwaya]);
  return (row?.c ?? 0) >= 6236;
}

export async function deleteQuranCache(riwaya: OnlineQuranSource) {
  const db = await getDb();
  await db.runAsync('DELETE FROM ayahs WHERE riwaya=?', [riwaya]);
}
