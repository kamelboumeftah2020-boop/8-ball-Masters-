import { getDb } from '../db';
import { hafsAyahsOf } from './mushaf';
import type { Riwaya } from '../db/queries';

// التفسير الميسّر (مجمع الملك فهد) عبر spa5k/tafsir_api، مع تخزين محلي لكل آية تُفتح
export const TAFSIR_ID = 'ar-tafsir-muyassar';
const URLS = [
  (s: number, a: number) => `https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${TAFSIR_ID}/${s}/${a}.json`,
  (s: number, a: number) => `https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/${TAFSIR_ID}/${s}/${a}.json`,
];

async function tafsirHafs(surah: number, ayah: number): Promise<string> {
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

// تفسير آية من الرواية المختارة (التفسير مرقّم بترقيم حفص)
export async function getTafsir(r: Riwaya, surah: number, ayah: number): Promise<string> {
  const parts = await Promise.all(hafsAyahsOf(r, surah, ayah).map((a) => tafsirHafs(surah, a)));
  return [...new Set(parts)].join('\n\n');
}
