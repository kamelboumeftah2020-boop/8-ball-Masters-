import { getDb } from '../db';

// التفسير الميسّر (مجمع الملك فهد) عبر spa5k/tafsir_api، مع تخزين محلي لكل آية تُفتح
export const TAFSIR_ID = 'ar-tafsir-muyassar';
const URLS = [
  (s: number, a: number) => `https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${TAFSIR_ID}/${s}/${a}.json`,
  (s: number, a: number) => `https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/${TAFSIR_ID}/${s}/${a}.json`,
];

export async function getTafsir(surah: number, ayah: number): Promise<string> {
  const db = await getDb();
  const row = await db.getFirstAsync<{ text: string }>(
    'SELECT text FROM tafasir WHERE tafsir_id=? AND surah=? AND ayah=?', [TAFSIR_ID, surah, ayah]);
  if (row) return row.text;
  for (const url of URLS) {
    try {
      const res = await fetch(url(surah, ayah));
      if (!res.ok) continue;
      const text = String((await res.json())?.text ?? '').trim();
      if (!text) continue;
      await db.runAsync('INSERT OR REPLACE INTO tafasir (tafsir_id,surah,ayah,text) VALUES (?,?,?,?)', [TAFSIR_ID, surah, ayah, text]);
      return text;
    } catch {}
  }
  throw new Error('offline');
}
