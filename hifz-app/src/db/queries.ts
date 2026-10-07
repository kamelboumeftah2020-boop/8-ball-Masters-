import { getDb } from './index';
import { nextState } from '../lib/srs';
import { todayStr } from '../lib/surahs';

export type Riwaya = 'hafs' | 'warsh';
export type Ayah = { riwaya: Riwaya; surah: number; ayah: number; text: string };

export async function surahAyahs(r: Riwaya, surah: number) {
  const db = await getDb();
  return db.getAllAsync<Ayah>('SELECT * FROM ayahs WHERE riwaya=? AND surah=? ORDER BY ayah', [r, surah]);
}
export async function surahCounts(r: Riwaya) {
  const db = await getDb();
  const rows = await db.getAllAsync<{ surah: number; c: number }>('SELECT surah, COUNT(*) c FROM ayahs WHERE riwaya=? GROUP BY surah', [r]);
  const m: Record<number, number> = {}; rows.forEach((x) => (m[x.surah] = x.c)); return m;
}
export async function rangeAyahs(r: Riwaya, surah: number, from: number, to: number) {
  const db = await getDb();
  return db.getAllAsync<Ayah>('SELECT * FROM ayahs WHERE riwaya=? AND surah=? AND ayah BETWEEN ? AND ? ORDER BY ayah', [r, surah, from, to]);
}
export async function dueAyahs(r: Riwaya, limit = 8) {
  const db = await getDb();
  return db.getAllAsync<Ayah>(
    `SELECT a.* FROM memorization m JOIN ayahs a ON a.riwaya=m.riwaya AND a.surah=m.surah AND a.ayah=m.ayah
     WHERE m.riwaya=? AND m.next_review<=? ORDER BY m.next_review, m.surah, m.ayah LIMIT ?`, [r, todayStr(), limit]);
}
export async function rate(r: Riwaya, ayahs: Ayah[], rating: 0 | 1 | 2, kind: 'hifz' | 'review') {
  const db = await getDb();
  await db.withTransactionAsync(async () => {
    for (const a of ayahs) {
      const p = await db.getFirstAsync<{ ease: number; interval_days: number; lapses: number }>(
        'SELECT ease, interval_days, lapses FROM memorization WHERE riwaya=? AND surah=? AND ayah=?', [r, a.surah, a.ayah]);
      const n = nextState({ ease: p?.ease ?? 2.5, interval: p?.interval_days ?? 0, lapses: p?.lapses ?? 0 }, rating);
      const status = rating === 2 && n.interval >= 3 ? 'memorized' : 'learning';
      await db.runAsync(
        `INSERT INTO memorization (riwaya,surah,ayah,status,ease,interval_days,next_review,last_review,lapses)
         VALUES (?,?,?,?,?,?,?,?,?)
         ON CONFLICT(riwaya,surah,ayah) DO UPDATE SET status=?, ease=?, interval_days=?, next_review=?, last_review=?, lapses=?`,
        [r, a.surah, a.ayah, status, n.ease, n.interval, n.next, todayStr(), n.lapses,
         status, n.ease, n.interval, n.next, todayStr(), n.lapses]);
    }
    await db.runAsync('INSERT INTO sessions (date,riwaya,kind,ayahs_count,rating) VALUES (?,?,?,?,?)', [todayStr(), r, kind, ayahs.length, rating]);
  });
}
export async function stats(r: Riwaya) {
  const db = await getDb();
  const one = async (sql: string, p: any[]) => (await db.getFirstAsync<{ c: number }>(sql, p))?.c ?? 0;
  const due = await one('SELECT COUNT(*) c FROM memorization WHERE riwaya=? AND next_review<=?', [r, todayStr()]);
  const memorized = await one("SELECT COUNT(*) c FROM memorization WHERE riwaya=? AND status='memorized'", [r]);
  const learning = await one("SELECT COUNT(*) c FROM memorization WHERE riwaya=? AND status='learning'", [r]);
  const total = await one('SELECT COUNT(*) c FROM ayahs WHERE riwaya=?', [r]);
  const todayCount = await one('SELECT COALESCE(SUM(ayahs_count),0) c FROM sessions WHERE date=?', [todayStr()]);
  const days = await db.getAllAsync<{ date: string }>('SELECT DISTINCT date FROM sessions ORDER BY date DESC LIMIT 400');
  const set = new Set(days.map((d) => d.date));
  let streak = 0; const cur = new Date();
  if (!set.has(todayStr(cur))) cur.setDate(cur.getDate() - 1);
  while (set.has(todayStr(cur))) { streak++; cur.setDate(cur.getDate() - 1); }
  return { due, memorized, learning, total, todayCount, streak };
}
export async function surahProgress(r: Riwaya) {
  const db = await getDb();
  const rows = await db.getAllAsync<{ surah: number; c: number }>("SELECT surah, COUNT(*) c FROM memorization WHERE riwaya=? AND status='memorized' GROUP BY surah", [r]);
  const m: Record<number, number> = {}; rows.forEach((x) => (m[x.surah] = x.c)); return m;
}
export async function bookmarksOf(r: Riwaya, surah: number) {
  const db = await getDb();
  const rows = await db.getAllAsync<{ ayah: number }>('SELECT ayah FROM bookmarks WHERE riwaya=? AND surah=?', [r, surah]);
  return new Set(rows.map((x) => x.ayah));
}
export async function toggleBookmark(r: Riwaya, surah: number, ayah: number) {
  const db = await getDb();
  const ex = await db.getFirstAsync('SELECT 1 x FROM bookmarks WHERE riwaya=? AND surah=? AND ayah=?', [r, surah, ayah]);
  if (ex) await db.runAsync('DELETE FROM bookmarks WHERE riwaya=? AND surah=? AND ayah=?', [r, surah, ayah]);
  else await db.runAsync('INSERT INTO bookmarks VALUES (?,?,?)', [r, surah, ayah]);
}
export async function allBookmarks(r: Riwaya) {
  const db = await getDb();
  return db.getAllAsync<{ surah: number; ayah: number; text: string | null }>(
    `SELECT b.surah, b.ayah, a.text FROM bookmarks b LEFT JOIN ayahs a ON a.riwaya=b.riwaya AND a.surah=b.surah AND a.ayah=b.ayah
     WHERE b.riwaya=? ORDER BY b.surah, b.ayah`, [r]);
}
