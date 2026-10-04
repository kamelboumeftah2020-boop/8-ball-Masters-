// مشغّل الصوت الموحّد: سورة كاملة، أو آية بآية، أو محاضرة
import { $, store, arNum, esc, pad3, fmtDur, toast, icons, sheet } from './core.js';
import { SURAHS } from './data/surahs.js';
import { isNative, mediaPlaying, mediaStopped, openExternal } from './native.js';

// server: السور كاملة (mp3quran) — ayah: مجلد التلاوة آية بآية (everyayah)
export const RECITERS = [
  { id: 'afs', name: 'مشاري العفاسي', server: 'https://server8.mp3quran.net/afs/', ayah: 'Alafasy_128kbps' },
  { id: 'basit', name: 'عبد الباسط عبد الصمد', server: 'https://server7.mp3quran.net/basit/', ayah: 'Abdul_Basit_Murattal_192kbps' },
  { id: 'husr', name: 'محمود خليل الحصري', server: 'https://server13.mp3quran.net/husr/', ayah: 'Husary_128kbps' },
  { id: 'minsh', name: 'محمد صديق المنشاوي', server: 'https://server10.mp3quran.net/minsh/', ayah: 'Minshawy_Murattal_128kbps' },
  { id: 'maher', name: 'ماهر المعيقلي', server: 'https://server12.mp3quran.net/maher/', ayah: 'MaherAlMuaiqly128kbps' },
  { id: 'sds', name: 'عبد الرحمن السديس', server: 'https://server11.mp3quran.net/sds/', ayah: 'Abdurrahmaan_As-Sudais_192kbps' },
  { id: 'shur', name: 'سعود الشريم', server: 'https://server7.mp3quran.net/shur/', ayah: 'Saood_ash-Shuraym_128kbps' },
  { id: 'gmd', name: 'سعد الغامدي', server: 'https://server7.mp3quran.net/s_gmd/', ayah: 'Ghamadi_40kbps' },
  { id: 'yasser', name: 'ياسر الدوسري', server: 'https://server11.mp3quran.net/yasser/', ayah: 'Yasser_Ad-Dussary_128kbps' },
  { id: 'ajm', name: 'أحمد العجمي', server: 'https://server10.mp3quran.net/ajm/', ayah: 'Ahmed_ibn_Ali_al-Ajamy_128kbps_ketaballah.net' },
  { id: 'qtm', name: 'ناصر القطامي', server: 'https://server6.mp3quran.net/qtm/', ayah: 'Nasser_Alqatami_128kbps' },
];
export const reciterById = id => RECITERS.find(r => r.id === id) || RECITERS[0];
export const surahName = n => SURAHS[n - 1][0];
export const surahSub = n => { const s = SURAHS[n - 1]; return `${s[2] ? 'مكية' : 'مدنية'} · ${arNum(s[1])} آية`; };

export const audio = $('#audio');
export const player = {
  mode: null, // 'surah' | 'ayah' | 'lecture'
  reciter: store.get('reciter', 'afs'),
  surah: 1, ayahs: null, ayahIdx: 0,
  lec: null, // { speaker, list, idx, part }
  repeat: false,
  speed: 1,
};
export const events = new EventTarget();
const emit = type => events.dispatchEvent(new Event(type));

/* ── التشغيل ── */
// مصادر المقطع الحالي: الرابط الأساسي ثم الروابط البديلة، ونعيد المرور عليها مرة ثانية
let sources = [], attempt = 0;
function start(src, alts = []) {
  sources = [src, ...alts];
  attempt = 0;
  load(src);
}
function load(src) {
  audio.src = src;
  audio.playbackRate = player.mode === 'lecture' ? player.speed : 1;
  audio.play().catch(err => { if (err.name === 'NotAllowedError') toast('اضغط على زر التشغيل لبدء الاستماع'); });
  refresh();
  emit('change');
}

export function playSurah(n, reciterId = player.reciter) {
  Object.assign(player, { mode: 'surah', surah: n, reciter: reciterId, ayahs: null, lec: null });
  store.set('reciter', reciterId);
  store.set('lastListen', { surah: n, reciter: reciterId });
  start(reciterById(reciterId).server + pad3(n) + '.mp3');
}

// رابط الآية بصوت القارئ المختار، ثم بديل احتياطي بصوت العفاسي
function ayahSources(surah, a) {
  const file = pad3(surah) + pad3(a.numberInSurah) + '.mp3';
  const dir = reciterById(player.reciter).ayah;
  return [
    `https://everyayah.com/data/${dir}/${file}`,
    `https://mirrors.quranicaudio.com/everyayah/${dir}/${file}`,
    `https://cdn.islamic.network/quran/audio/128/ar.alafasy/${a.number}.mp3`,
  ];
}
export function playAyahs(surah, ayahs, idx) {
  Object.assign(player, { mode: 'ayah', surah, ayahs, ayahIdx: idx, lec: null });
  const [src, ...alts] = ayahSources(surah, ayahs[idx]);
  start(src, alts);
}

export function playLecture(speaker, list, idx, part = 0) {
  Object.assign(player, { mode: 'lecture', lec: { speaker, list, idx, part } });
  const item = list[idx];
  store.set('lastLecture', { sid: speaker.id, idx, title: item.t, speaker: speaker.name });
  start(item.u[part], part === 0 ? item.a || [] : []);
}

export const toggle = () => (audio.paused ? audio.play().catch(() => {}) : audio.pause());
export const isCurrentLecture = (sid, idx) => player.mode === 'lecture' && player.lec.speaker.id === sid && player.lec.idx === idx;

export function step(dir) {
  if (player.mode === 'surah') {
    const n = player.surah + dir;
    if (n >= 1 && n <= 114) playSurah(n);
  } else if (player.mode === 'ayah') {
    const i = player.ayahIdx + dir;
    if (i >= 0 && i < player.ayahs.length) playAyahs(player.surah, player.ayahs, i);
  } else if (player.mode === 'lecture') {
    const { speaker, list, idx, part } = player.lec;
    const item = list[idx];
    if (dir > 0 && part < item.u.length - 1) return playLecture(speaker, list, idx, part + 1);
    if (dir < 0 && part > 0) return playLecture(speaker, list, idx, part - 1);
    const j = idx + dir;
    if (j >= 0 && j < list.length) playLecture(speaker, list, j, 0);
  }
}
export function seekBy(sec) {
  if (audio.duration) audio.currentTime = Math.max(0, Math.min(audio.duration - 1, audio.currentTime + sec));
}

/* ── متابعة المحاضرات من حيث توقفت ── */
const posKey = () => player.mode === 'lecture' ? player.lec.list[player.lec.idx].u[player.lec.part] : null;
export const lecturePos = url => store.get('lecPos', {})[url] || 0;
let lastSaved = 0;
function savePos(force) {
  const k = posKey();
  if (!k || !audio.duration) return;
  if (!force && Math.abs(audio.currentTime - lastSaved) < 5) return;
  lastSaved = audio.currentTime;
  const all = store.get('lecPos', {});
  if (audio.currentTime > audio.duration - 15) delete all[k];
  else all[k] = Math.floor(audio.currentTime);
  const keys = Object.keys(all);
  if (keys.length > 150) delete all[keys[0]];
  store.set('lecPos', all);
}
audio.addEventListener('loadedmetadata', () => {
  const k = posKey();
  if (!k) return;
  const p = lecturePos(k);
  if (p > 15 && p < audio.duration - 15) {
    audio.currentTime = p;
    toast(`استكمال من ${fmtDur(p)}`);
  }
});

/* ── مؤقت النوم ── */
let sleepAt = null, sleepEnd = false, sleepTimer = null, sleepStep = 0;
const SLEEP_STEPS = [0, 15, 30, 60, 'end'];
function sleepLabel() {
  if (sleepEnd) return 'نهاية المقطع';
  if (sleepAt) return `${arNum(Math.max(1, Math.round((sleepAt - Date.now()) / 60000)))} د`;
  return 'إيقاف';
}
function cycleSleep() {
  if (!sleepAt && !sleepEnd) sleepStep = 0;
  sleepStep = (sleepStep + 1) % SLEEP_STEPS.length;
  const next = SLEEP_STEPS[sleepStep];
  clearTimeout(sleepTimer); sleepAt = null; sleepEnd = false;
  if (next === 'end') sleepEnd = true;
  else if (next) {
    sleepAt = Date.now() + next * 60000;
    sleepTimer = setTimeout(() => { audio.pause(); sleepAt = null; toast('توقف التشغيل بمؤقت النوم'); refreshSheet(); }, next * 60000);
  }
  toast(next ? `مؤقت النوم: ${sleepLabel()}` : 'أُلغي مؤقت النوم');
}

audio.addEventListener('ended', () => {
  savePos(true);
  if (sleepEnd) { sleepEnd = false; refreshSheet(); return; }
  if (player.repeat) { audio.currentTime = 0; audio.play(); return; }
  if (player.mode === 'ayah' && player.ayahIdx >= player.ayahs.length - 1) { emit('ayahs-end'); return; }
  if (player.mode === 'surah' && player.surah >= 114) return;
  step(1);
});
// خوادم الأرشيف تخفق أحيانًا: ننتقل إلى الرابط البديل، ثم نعيد المرور على الروابط مرة أخرى
audio.addEventListener('error', () => {
  if (!player.mode || !audio.getAttribute('src') || !sources.length) return;
  attempt++;
  if (attempt < sources.length * 2) {
    const src = sources[attempt % sources.length];
    const round = Math.floor(attempt / sources.length);
    setTimeout(() => load(round ? src + (src.includes('?') ? '&' : '?') + 'retry=' + round : src), 600 + round * 900);
  } else {
    toast('تعذّر تشغيل هذه المادة الآن، تحقق من الاتصال أو جرّب لاحقًا');
  }
});
// مؤشر التحميل أثناء انتظار الخادم
const setLoading = on => { $('#player').classList.toggle('loading', on); $('#fullPlayer')?.classList.toggle('loading', on); };
audio.addEventListener('loadstart', () => setLoading(true));
audio.addEventListener('waiting', () => setLoading(true));
audio.addEventListener('playing', () => setLoading(false));
audio.addEventListener('canplay', () => setLoading(false));

// في أندرويد: خدمة في الخلفية تُبقي التشغيل مستمرًا والشاشة مطفأة
let stopTimer = null;
audio.addEventListener('play', () => {
  clearTimeout(stopTimer);
  const info = trackInfo();
  mediaPlaying(info.title, info.sub);
  refreshState(); emit('state');
});
audio.addEventListener('pause', () => {
  savePos(true); refreshState(); emit('state');
  // مهلة قصيرة حتى لا تتوقف الخدمة بين مقطع وآخر
  clearTimeout(stopTimer);
  stopTimer = setTimeout(() => { if (audio.paused) mediaStopped(); }, 4000);
});
audio.addEventListener('timeupdate', () => {
  if (audio.duration) $('#pBar').style.width = (audio.currentTime / audio.duration * 100) + '%';
  savePos(false);
  refreshSheetTime();
});

/* ── المشغّل المصغّر ── */
$('#pPlay').onclick = toggle;
$('#pPrev').onclick = () => step(-1);
$('#pNext').onclick = () => step(1);
$('#pInfo').onclick = openFullPlayer;
$('#pClose').onclick = () => { audio.pause(); audio.removeAttribute('src'); audio.load(); player.mode = null; $('#player').hidden = true; document.body.classList.remove('has-player'); emit('change'); };

export function trackInfo() {
  if (player.mode === 'surah') return { title: 'سورة ' + surahName(player.surah), sub: reciterById(player.reciter).name, link: '#/listen' };
  if (player.mode === 'ayah') {
    const r = reciterById(player.reciter);
    return { title: 'سورة ' + surahName(player.surah), sub: `الآية ${arNum(player.ayahs[player.ayahIdx].numberInSurah)} · ${r.name}`, link: `#/mushaf/${player.surah}` };
  }
  if (player.mode === 'lecture') {
    const { speaker, list, idx, part } = player.lec;
    const item = list[idx];
    return { title: item.t, sub: speaker.name + (item.u.length > 1 ? ` · الجزء ${arNum(part + 1)} من ${arNum(item.u.length)}` : ''), link: `#/mawaiz/s/${speaker.id}` };
  }
  return { title: '', sub: '', link: '#/' };
}

function refresh() {
  $('#player').hidden = false;
  document.body.classList.add('has-player');
  const info = trackInfo();
  $('#pTitle').textContent = info.title;
  $('#pSub').textContent = info.sub;
  $('#pArt').innerHTML = player.mode === 'lecture' ? icons.mic : icons.book;
  if ('mediaSession' in navigator) {
    navigator.mediaSession.metadata = new MediaMetadata({ title: info.title, artist: info.sub, album: 'نور', artwork: [{ src: 'icons/icon-512.png', sizes: '512x512', type: 'image/png' }] });
    navigator.mediaSession.setActionHandler('previoustrack', () => step(-1));
    navigator.mediaSession.setActionHandler('nexttrack', () => step(1));
    navigator.mediaSession.setActionHandler('seekbackward', () => seekBy(-15));
    navigator.mediaSession.setActionHandler('seekforward', () => seekBy(15));
  }
  refreshState();
  refreshSheet();
}
function refreshState() {
  $('#player').classList.toggle('playing', !audio.paused);
  const s = $('#fullPlayer');
  if (s) s.classList.toggle('playing', !audio.paused);
}

/* ── المشغّل الكامل ── */
function openFullPlayer() {
  if (!player.mode) return;
  sheet(`<div id="fullPlayer" class="full-player"></div>`, {
    label: 'المشغّل',
    onClick: (e, close) => {
      const b = e.target.closest('[data-pa]');
      if (!b) return;
      const a = b.dataset.pa;
      if (a === 'play') toggle();
      if (a === 'prev') step(-1);
      if (a === 'next') step(1);
      if (a === 'back') seekBy(-15);
      if (a === 'fwd') seekBy(15);
      if (a === 'speed') {
        const speeds = [1, 1.25, 1.5, 1.75, 2, 0.75];
        player.speed = speeds[(speeds.indexOf(player.speed) + 1) % speeds.length];
        audio.playbackRate = player.speed;
      }
      if (a === 'repeat') { player.repeat = !player.repeat; toast(player.repeat ? 'تكرار المقطع الحالي' : 'أُلغي التكرار'); }
      if (a === 'sleep') cycleSleep();
      if (a === 'download') { if (isNative) { e.preventDefault(); openExternal(b.getAttribute('href')); } return; }
      if (a === 'go') { close(); location.hash = trackInfo().link; return; }
      refreshSheet();
    },
  });
  refreshSheet();
}
let seeking = false;

function refreshSheet() {
  const el = $('#fullPlayer');
  if (!el || !player.mode) return;
  const info = trackInfo();
  const lec = player.mode === 'lecture';
  const item = lec ? player.lec.list[player.lec.idx] : null;
  el.classList.toggle('playing', !audio.paused);
  el.innerHTML = `
    <div class="fp-art">${lec ? icons.mic : icons.book}</div>
    <div class="fp-title">${esc(info.title)}</div>
    <div class="fp-sub">${esc(info.sub)}</div>
    <input type="range" id="fpSeek" class="seek" min="0" max="1000" value="${audio.duration ? audio.currentTime / audio.duration * 1000 : 0}" aria-label="موضع التشغيل">
    <div class="fp-times"><span id="fpCur">${fmtDur(audio.currentTime)}</span><span id="fpDur">${fmtDur(audio.duration)}</span></div>
    <div class="fp-ctrls">
      <button class="icon-btn" data-pa="back" aria-label="رجوع ١٥ ثانية">${icons.back15}</button>
      <button class="icon-btn" data-pa="prev" aria-label="السابق">${icons.prev}</button>
      <button class="play-btn big" data-pa="play" aria-label="تشغيل/إيقاف"><span class="i-play">${icons.play}</span><span class="i-pause">${icons.pause}</span></button>
      <button class="icon-btn" data-pa="next" aria-label="التالي">${icons.next}</button>
      <button class="icon-btn" data-pa="fwd" aria-label="تقديم ١٥ ثانية">${icons.fwd15}</button>
    </div>
    <div class="fp-opts">
      ${lec ? `<button class="opt" data-pa="speed"><b>${arNum(player.speed)}×</b><span>السرعة</span></button>` : `<button class="opt ${player.repeat ? 'on' : ''}" data-pa="repeat">${icons.repeat}<span>تكرار</span></button>`}
      <button class="opt ${sleepAt || sleepEnd ? 'on' : ''}" data-pa="sleep">${icons.moonSleep}<span>${sleepAt || sleepEnd ? sleepLabel() : 'مؤقت النوم'}</span></button>
      ${lec ? `<a class="opt" data-pa="download" href="${esc(item.u[player.lec.part])}" target="_blank" rel="noopener" download>${icons.download}<span>تحميل</span></a>` : ''}
      <button class="opt" data-pa="go">${lec ? icons.mic : icons.book}<span>${lec ? 'قائمة الشيخ' : 'فتح السورة'}</span></button>
    </div>`;
  const seek = $('#fpSeek', el);
  seek.addEventListener('input', () => { seeking = true; $('#fpCur').textContent = fmtDur(seek.value * (audio.duration || 0) / 1000); });
  seek.addEventListener('change', () => { if (audio.duration) audio.currentTime = seek.value * audio.duration / 1000; seeking = false; });
}
function refreshSheetTime() {
  if (seeking) return;
  const seek = $('#fpSeek');
  if (!seek) return;
  if (audio.duration) seek.value = audio.currentTime / audio.duration * 1000;
  $('#fpCur').textContent = fmtDur(audio.currentTime);
  $('#fpDur').textContent = fmtDur(audio.duration);
}
