import { getDb } from '../db';
import { getMushaf } from './mushaf';
import type { Riwaya } from '../db/queries';

// رقم إصدار البيانات المضمّنة: تغييره يعيد تعبئة جدول الآيات
const DATA_VERSION = 'kfgqpc-1';
const inflight: Partial<Record<Riwaya, Promise<void>>> = {};

// ينسخ نص الرواية المضمّن إلى SQLite (للحفظ والمراجعة والإحصائيات) مرة واحدة
export function ensureQuran(r: Riwaya) {
  if (!inflight[r]) {
    inflight[r] = (async () => {
      const db = await getDb();
      const key = `quran:${r}`;
      const row = await db.getFirstAsync<{ value: string }>('SELECT value FROM settings WHERE key=?', [key]);
      if (row?.value === DATA_VERSION) return;
      const { ayahs } = getMushaf(r);
      await db.withTransactionAsync(async () => {
        await db.runAsync('DELETE FROM ayahs WHERE riwaya=?', [r]);
        const st = await db.prepareAsync('INSERT INTO ayahs (riwaya,surah,ayah,text) VALUES (?,?,?,?)');
        try { for (const a of ayahs) await st.executeAsync([r, a.surah, a.ayah, a.text]); }
        finally { await st.finalizeAsync(); }
        await db.runAsync('INSERT OR REPLACE INTO settings (key,value) VALUES (?,?)', [key, DATA_VERSION]);
      });
    })();
    inflight[r]!.catch(() => { delete inflight[r]; });
  }
  return inflight[r]!;
}
