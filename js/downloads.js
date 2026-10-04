// تحميل التلاوات والمواعظ للاستماع دون اتصال.
// في أندرويد: تُحفظ الملفات في مساحة التطبيق الخاصة (files/audio) وتُشغَّل عبر /_nur_audio_/
// في المتصفح: تُحفظ في Cache Storage وتُشغَّل من رابط blob.
import { store, toast, arNum } from './core.js';
import { isNative, nativePlugin } from './native.js';

const CACHE = 'nur-audio-v1';
export const events = new EventTarget();
const emit = id => events.dispatchEvent(new CustomEvent('change', { detail: id }));

// معرّف ثابت من رابط المصدر
export function idOf(url) {
  let h = 0x811c9dc5;
  for (let i = 0; i < url.length; i++) { h ^= url.charCodeAt(i); h = Math.imul(h, 0x01000193); }
  return (h >>> 0).toString(36) + url.length.toString(36);
}

const registry = () => store.get('downloads', {});
const saveRegistry = r => store.set('downloads', r);
export const list = () => Object.values(registry()).sort((a, b) => b.t - a.t);
export const isDownloaded = url => !!registry()[idOf(url)];

// المهام الجارية: { id, urls, meta, received, total, status }
const jobs = new Map();
const queue = [];
let running = false;
export const job = url => jobs.get(idOf(url));
export const activeJobs = () => [...jobs.values()];

/**
 * إضافة مادة إلى قائمة التحميل.
 * key: الرابط الأساسي (معرّف المادة)، urls: روابط التحميل بالترتيب (الأساسي ثم البدائل)
 * meta: { kind: 'surah' | 'lecture', title, sub, ref }
 */
export function enqueue(key, urls, meta) {
  const id = idOf(key);
  if (registry()[id] || jobs.has(id)) return false;
  jobs.set(id, { id, key, urls, meta, received: 0, total: 0, status: 'queued' });
  queue.push(id);
  emit(id);
  pump();
  return true;
}

export function cancel(key) {
  const id = idOf(key);
  const j = jobs.get(id);
  if (!j) return;
  j.cancelled = true;
  const i = queue.indexOf(id);
  if (i >= 0) queue.splice(i, 1);
  if (j.status === 'queued') jobs.delete(id);
  emit(id);
}

async function pump() {
  if (running) return;
  running = true;
  while (queue.length) {
    const id = queue.shift();
    const j = jobs.get(id);
    if (!j || j.cancelled) { jobs.delete(id); continue; }
    j.status = 'running';
    emit(id);
    let ok = false;
    for (const url of j.urls) {
      if (j.cancelled) break;
      try {
        j.received = 0; j.total = 0;
        const size = isNative ? await downloadNative(j, url) : await downloadWeb(j, url);
        if (j.cancelled) { await removeFile(id); break; }
        const r = registry();
        r[id] = { id, key: j.key, size, t: Date.now(), ...j.meta };
        saveRegistry(r);
        ok = true;
        break;
      } catch { /* نجرّب الرابط البديل */ }
    }
    jobs.delete(id);
    if (!ok && !j.cancelled) toast(`تعذّر تحميل «${j.meta.title}»، أعد المحاولة لاحقًا`);
    emit(id);
  }
  running = false;
}

/* ── أندرويد ── */
let progressHooked = false;
async function downloadNative(j, url) {
  const FT = nativePlugin('FileTransfer'), FS = nativePlugin('Filesystem');
  if (!FT || !FS) throw new Error('unavailable');
  if (!progressHooked) {
    progressHooked = true;
    FT.addListener('progress', s => {
      for (const job of jobs.values()) {
        if (job.status === 'running' && job.currentUrl === s.url) {
          job.received = s.bytes;
          job.total = s.lengthComputable ? s.contentLength : 0;
          emit(job.id);
        }
      }
    });
  }
  await FS.mkdir({ path: 'audio', directory: 'DATA', recursive: true }).catch(() => {});
  const part = `audio/${j.id}.part`, final = `audio/${j.id}.mp3`;
  const { uri } = await FS.getUri({ path: part, directory: 'DATA' });
  j.currentUrl = url;
  await FT.downloadFile({ url, path: uri, progress: true });
  const st = await FS.stat({ path: part, directory: 'DATA' });
  if (!st.size || st.size < 10000) { await FS.deleteFile({ path: part, directory: 'DATA' }).catch(() => {}); throw new Error('empty'); }
  await FS.rename({ from: part, to: final, directory: 'DATA', toDirectory: 'DATA' });
  return st.size;
}

/* ── المتصفح ── */
async function downloadWeb(j, url) {
  const res = await fetch(url);
  if (!res.ok || !res.body) throw new Error(res.status);
  j.total = +res.headers.get('content-length') || 0;
  const reader = res.body.getReader();
  const chunks = [];
  let lastEmit = 0;
  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    if (j.cancelled) { reader.cancel(); throw new Error('cancelled'); }
    chunks.push(value);
    j.received += value.length;
    if (Date.now() - lastEmit > 250) { lastEmit = Date.now(); emit(j.id); }
  }
  const blob = new Blob(chunks, { type: 'audio/mpeg' });
  if (blob.size < 10000) throw new Error('empty');
  const c = await caches.open(CACHE);
  await c.put(`/__nur_audio__/${j.id}.mp3`, new Response(blob, { headers: { 'Content-Type': 'audio/mpeg', 'Content-Length': String(blob.size) } }));
  return blob.size;
}

/* ── التشغيل من الملف المحمّل ── */
const blobUrls = new Map();
export async function localSource(key) {
  const id = idOf(key);
  if (!registry()[id]) return null;
  if (isNative) return `${location.origin}/_nur_audio_/${id}.mp3`;
  if (blobUrls.has(id)) return blobUrls.get(id);
  try {
    const r = await (await caches.open(CACHE)).match(`/__nur_audio__/${id}.mp3`);
    if (!r) throw new Error('missing');
    const u = URL.createObjectURL(await r.blob());
    blobUrls.set(id, u);
    return u;
  } catch {
    // الملف لم يعد موجودًا: نحذفه من السجل
    const reg = registry(); delete reg[id]; saveRegistry(reg);
    return null;
  }
}

async function removeFile(id) {
  if (isNative) {
    const FS = nativePlugin('Filesystem');
    await FS?.deleteFile({ path: `audio/${id}.mp3`, directory: 'DATA' }).catch(() => {});
    await FS?.deleteFile({ path: `audio/${id}.part`, directory: 'DATA' }).catch(() => {});
  } else {
    await caches.open(CACHE).then(c => c.delete(`/__nur_audio__/${id}.mp3`)).catch(() => {});
    if (blobUrls.has(id)) { URL.revokeObjectURL(blobUrls.get(id)); blobUrls.delete(id); }
  }
}

export async function remove(key) {
  const id = idOf(key);
  await removeFile(id);
  const r = registry(); delete r[id]; saveRegistry(r);
  emit(id);
}

export async function removeAll() {
  for (const d of list()) await removeFile(d.id);
  saveRegistry({});
  emit('*');
}

export const totalSize = () => list().reduce((s, d) => s + (d.size || 0), 0);
export function fmtSize(bytes) {
  if (bytes >= 1024 ** 3) return `${arNum(+(bytes / 1024 ** 3).toFixed(1))} غ.ب`;
  return `${arNum(Math.max(1, Math.round(bytes / 1024 ** 2)))} م.ب`;
}
