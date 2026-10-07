import { Audio, AVPlaybackStatus } from 'expo-av';
import * as FileSystem from 'expo-file-system';
import { getDb } from '../db';
import { ayahCount } from './surahs';
import type { Riwaya } from '../db/queries';

export type Reciter = { id: string; name: string; riwaya: Riwaya; baseUrl: string };

export const RECITERS: Reciter[] = [
  { id: 'afasy', name: 'مشاري العفاسي - حفص', riwaya: 'hafs', baseUrl: 'https://everyayah.com/data/Alafasy_64kbps' },
  { id: 'yassin_warsh', name: 'ياسين الجزائري - ورش', riwaya: 'warsh', baseUrl: 'https://everyayah.com/data/warsh/warsh_yassin_al_jazaery_64kbps' },
];

export const reciterFor = (r: Riwaya) => RECITERS.find((x) => x.riwaya === r) ?? RECITERS[0];
const getRec = (id: string) => RECITERS.find((r) => r.id === id) ?? RECITERS[0];
const fileName = (s: number, a: number) => `${String(s).padStart(3, '0')}${String(a).padStart(3, '0')}.mp3`;
const dirOf = (rec: Reciter) => `${FileSystem.documentDirectory}audio/${rec.id}/`;
export const remoteUrl = (rec: Reciter, s: number, a: number) => `${rec.baseUrl}/${fileName(s, a)}`;

let modeSet = false;
async function ensureMode() {
  if (modeSet) return;
  modeSet = true;
  await Audio.setAudioModeAsync({ staysActiveInBackground: true, playsInSilentModeIOS: true, shouldDuckAndroid: true }).catch(() => {});
}

async function sourceFor(rec: Reciter, s: number, a: number) {
  const local = dirOf(rec) + fileName(s, a);
  const info = await FileSystem.getInfoAsync(local).catch(() => null);
  return { uri: info?.exists ? local : remoteUrl(rec, s, a) };
}

let sound: Audio.Sound | null = null;
let token = 0; // يلغي أي تشغيل متتابع قديم

export async function stopAudio() {
  token++;
  const s = sound; sound = null;
  if (s) { try { await s.stopAsync(); await s.unloadAsync(); } catch {} }
}

// يشغّل آية واحدة. onEnd يُستدعى عند انتهائها.
export async function playAyah(s: number, a: number, reciterId = 'afasy', onEnd?: () => void) {
  await stopAudio();
  await ensureMode();
  const my = token;
  const rec = getRec(reciterId);
  const { sound: snd } = await Audio.Sound.createAsync(await sourceFor(rec, s, a), { shouldPlay: true },
    (st: AVPlaybackStatus) => { if (st.isLoaded && st.didJustFinish && my === token) onEnd?.(); });
  if (my !== token) { snd.unloadAsync().catch(() => {}); return; }
  sound = snd;
}

// تشغيل متتابع من آية إلى آخر السورة (أو حتى `to`)، مع تكرار كل آية `repeat` مرات
export async function playRange(surah: number, from: number, reciterId: string,
  opts: { to?: number; repeat?: number; onAyah?: (a: number | null) => void } = {}) {
  const to = opts.to ?? ayahCount(surah);
  const repeat = Math.max(1, opts.repeat ?? 1);
  let a = from, n = 1;
  const step = async (): Promise<void> => {
    if (a > to) { opts.onAyah?.(null); return; }
    opts.onAyah?.(a);
    const mine = token + 1; // playAyah يزيد token مرة واحدة
    try {
      await playAyah(surah, a, reciterId, () => {
        if (mine !== token) return;
        if (n < repeat) n++; else { n = 1; a++; }
        step();
      });
    } catch { opts.onAyah?.(null); }
  };
  await step();
}

// ---------- التحميل للـ offline ----------

export async function isSurahAudioDownloaded(surah: number, reciterId: string) {
  const db = await getDb();
  const row = await db.getFirstAsync<{ status: string }>(
    "SELECT status FROM downloads WHERE type='audio' AND riwaya=? AND surah=? AND reciter=?", [getRec(reciterId).riwaya, surah, reciterId]);
  return row?.status === 'done';
}

export async function downloadedSurahs(reciterId: string) {
  const db = await getDb();
  const rows = await db.getAllAsync<{ surah: number }>(
    "SELECT surah FROM downloads WHERE type='audio' AND reciter=? AND status='done'", [reciterId]);
  return new Set(rows.map((r) => r.surah));
}

// يحمّل كل آيات السورة (4 ملفات في نفس الوقت)، ويتجاوز المحمّل سابقًا
export async function downloadSurahForOffline(surah: number, reciterId: string, onProgress?: (done: number, total: number) => void) {
  const rec = getRec(reciterId);
  const dir = dirOf(rec);
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true }).catch(() => {});
  const total = ayahCount(surah);
  let next = 1, done = 0, failed = 0;
  const worker = async () => {
    while (next <= total) {
      const a = next++;
      const dest = dir + fileName(surah, a);
      try {
        const info = await FileSystem.getInfoAsync(dest);
        if (!info.exists || !info.size) {
          const r = await FileSystem.downloadAsync(remoteUrl(rec, surah, a), dest + '.part');
          if (r.status !== 200) throw new Error(`HTTP ${r.status}`);
          await FileSystem.moveAsync({ from: dest + '.part', to: dest });
        }
      } catch { failed++; FileSystem.deleteAsync(dest + '.part', { idempotent: true }).catch(() => {}); }
      onProgress?.(++done, total);
    }
  };
  await Promise.all([worker(), worker(), worker(), worker()]);
  const db = await getDb();
  await db.runAsync('INSERT OR REPLACE INTO downloads (type,riwaya,surah,reciter,status,progress) VALUES (?,?,?,?,?,?)',
    ['audio', rec.riwaya, surah, rec.id, failed ? 'partial' : 'done', Math.round(((total - failed) / total) * 100)]);
  if (failed) throw new Error(`${failed} ملف لم يتحمّل`);
}

export async function deleteSurahAudio(surah: number, reciterId: string) {
  const rec = getRec(reciterId);
  for (let a = 1; a <= ayahCount(surah); a++)
    await FileSystem.deleteAsync(dirOf(rec) + fileName(surah, a), { idempotent: true }).catch(() => {});
  const db = await getDb();
  await db.runAsync("DELETE FROM downloads WHERE type='audio' AND surah=? AND reciter=?", [surah, rec.id]);
}

export async function audioStorageMB() {
  let bytes = 0;
  for (const rec of RECITERS) {
    const dir = dirOf(rec);
    const files = await FileSystem.readDirectoryAsync(dir).catch(() => [] as string[]);
    for (const f of files) {
      const i = await FileSystem.getInfoAsync(dir + f).catch(() => null);
      if (i?.exists && !i.isDirectory) bytes += i.size ?? 0;
    }
  }
  return Math.round(bytes / 1048576);
}
