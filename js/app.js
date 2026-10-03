import { SURAHS } from './surahs.js';
import { MAWAIZ, CATEGORIES } from './mawaiz.js';

/* ───────── أدوات ───────── */
const $ = (s, el = document) => el.querySelector(s);
const view = $('#view');
const store = {
  get(k, d) { try { const v = localStorage.getItem('nur:' + k); return v == null ? d : JSON.parse(v); } catch { return d; } },
  set(k, v) { try { localStorage.setItem('nur:' + k, JSON.stringify(v)); } catch { /* التخزين غير متاح */ } },
};
const arNum = n => Number(n).toLocaleString('ar-EG');
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const pad3 = n => String(n).padStart(3, '0');
const normalize = s => s.replace(/[ً-ٰٟۖ-ۭـ]/g, '').replace(/[أإآٱ]/g, 'ا').replace(/ى/g, 'ي').replace(/ة/g, 'ه').trim();

function toast(msg) {
  const t = $('#toast');
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(toast.t);
  toast.t = setTimeout(() => t.classList.remove('show'), 2400);
}

async function fetchJSON(url) {
  const r = await fetch(url);
  if (!r.ok) throw new Error(r.status);
  return r.json();
}

const icons = {
  play: '<svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>',
  pause: '<svg viewBox="0 0 24 24"><path d="M7 5h4v14H7zM13 5h4v14h-4z"/></svg>',
  search: '<svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/></svg>',
  book: '<svg viewBox="0 0 24 24"><path d="M12 6c-2-1.5-5-2-8-2v14c3 0 6 .5 8 2 2-1.5 5-2 8-2V4c-3 0-6 .5-8 2zM12 6v14"/></svg>',
  headphones: '<svg viewBox="0 0 24 24"><path d="M3 14v-2a9 9 0 0 1 18 0v2"/><rect x="2.5" y="14" width="5" height="7" rx="2"/><rect x="16.5" y="14" width="5" height="7" rx="2"/></svg>',
  mosque: '<svg viewBox="0 0 24 24"><path d="M12 2c0 3-4 4-4 8v1h8v-1c0-4-4-5-4-8zM5 11h14M6 11v10h12V11M10 21v-4a2 2 0 0 1 4 0v4"/></svg>',
  star: '<svg viewBox="0 0 24 24"><path d="M12 3l2.4 4.9 5.4.8-3.9 3.8.9 5.4L12 15.4 7.2 17.9l.9-5.4L4.2 8.7l5.4-.8z"/></svg>',
  bookmark: '<svg viewBox="0 0 24 24"><path d="M6 3h12v18l-6-4-6 4z"/></svg>',
  share: '<svg viewBox="0 0 24 24"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/></svg>',
  copy: '<svg viewBox="0 0 24 24"><rect x="8" y="8" width="13" height="13" rx="3"/><path d="M16 8V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h3"/></svg>',
  bell: '<svg viewBox="0 0 24 24"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.9 1.9 0 0 0 3.4 0"/></svg>',
  bellOff: '<svg viewBox="0 0 24 24"><path d="M6 8a6 6 0 0 1 9.3-5M18 8c0 7 3 9 3 9H9M6.3 12.5C5.7 15.8 3 17 3 17h3M10.3 21a1.9 1.9 0 0 0 3.4 0M3 3l18 18"/></svg>',
  pin: '<svg viewBox="0 0 24 24"><path d="M12 22s7-6.2 7-12a7 7 0 0 0-14 0c0 5.8 7 12 7 12z"/><circle cx="12" cy="10" r="2.5"/></svg>',
  minus: '<svg viewBox="0 0 24 24"><path d="M5 12h14"/></svg>',
  plus: '<svg viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>',
  sunrise: '<svg viewBox="0 0 24 24"><path d="M17 18a5 5 0 0 0-10 0M12 2v7M4.2 10.2l1.4 1.4M1 18h2M21 18h2M18.4 11.6l1.4-1.4M23 22H1M8 6l4-4 4 4"/></svg>',
  sun: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
  sunset: '<svg viewBox="0 0 24 24"><path d="M17 18a5 5 0 0 0-10 0M12 9V2M4.2 10.2l1.4 1.4M1 18h2M21 18h2M18.4 11.6l1.4-1.4M23 22H1M16 5l-4 4-4-4"/></svg>',
  moon: '<svg viewBox="0 0 24 24"><path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z"/></svg>',
  dawn: '<svg viewBox="0 0 24 24"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9z"/><path d="M19 3v4M17 5h4"/></svg>',
};

/* ───────── المظهر ───────── */
const root = document.documentElement;
function applyTheme(t) {
  root.dataset.theme = t;
  $('meta[name="theme-color"]').content = t === 'dark' ? '#0d1513' : '#0f6b5c';
}
applyTheme(store.get('theme', matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'));
$('#themeBtn').onclick = () => {
  const t = root.dataset.theme === 'dark' ? 'light' : 'dark';
  applyTheme(t);
  store.set('theme', t);
};

/* ───────── القرّاء ───────── */
const RECITERS = [
  { id: 'afs', name: 'مشاري العفاسي', server: 'https://server8.mp3quran.net/afs/', ayah: 'ar.alafasy' },
  { id: 'basit', name: 'عبد الباسط عبد الصمد', server: 'https://server7.mp3quran.net/basit/' },
  { id: 'husr', name: 'محمود خليل الحصري', server: 'https://server13.mp3quran.net/husr/', ayah: 'ar.husary' },
  { id: 'minsh', name: 'محمد صديق المنشاوي', server: 'https://server10.mp3quran.net/minsh/', ayah: 'ar.minshawi' },
  { id: 'maher', name: 'ماهر المعيقلي', server: 'https://server12.mp3quran.net/maher/', ayah: 'ar.mahermuaiqly' },
  { id: 'sds', name: 'عبد الرحمن السديس', server: 'https://server11.mp3quran.net/sds/' },
  { id: 'gmd', name: 'سعد الغامدي', server: 'https://server7.mp3quran.net/s_gmd/' },
  { id: 'yasser', name: 'ياسر الدوسري', server: 'https://server11.mp3quran.net/yasser/' },
  { id: 'ajm', name: 'أحمد العجمي', server: 'https://server10.mp3quran.net/ajm/', ayah: 'ar.ahmedajamy' },
  { id: 'qtm', name: 'ناصر القطامي', server: 'https://server6.mp3quran.net/qtm/' },
];
const reciterById = id => RECITERS.find(r => r.id === id) || RECITERS[0];

/* ───────── المشغّل ───────── */
const audio = $('#audio');
const player = {
  // mode: 'surah' (سورة كاملة) أو 'ayah' (آية بآية من المصحف)
  mode: null, reciter: store.get('reciter', 'afs'), surah: 1,
  ayahs: null, ayahIdx: 0,
};

function surahSub(n) { const s = SURAHS[n - 1]; return `${s[2] ? 'مكية' : 'مدنية'} · ${arNum(s[1])} آية`; }

function playSurah(n, reciterId = player.reciter) {
  Object.assign(player, { mode: 'surah', surah: n, reciter: reciterId, ayahs: null });
  store.set('reciter', reciterId);
  store.set('lastListen', { surah: n, reciter: reciterId });
  audio.src = reciterById(reciterId).server + pad3(n) + '.mp3';
  audio.play().catch(() => toast('تعذّر تشغيل الصوت، تحقق من الاتصال'));
  updatePlayer();
}

function playAyahs(surah, ayahs, idx) {
  Object.assign(player, { mode: 'ayah', surah, ayahs, ayahIdx: idx });
  const ed = reciterById(player.reciter).ayah || 'ar.alafasy';
  audio.src = `https://cdn.islamic.network/quran/audio/128/${ed}/${ayahs[idx].number}.mp3`;
  audio.play().catch(() => toast('تعذّر تشغيل الصوت، تحقق من الاتصال'));
  updatePlayer();
  highlightAyah();
}

function step(dir) {
  if (player.mode === 'surah') {
    const n = player.surah + dir;
    if (n >= 1 && n <= 114) playSurah(n);
  } else if (player.mode === 'ayah') {
    const i = player.ayahIdx + dir;
    if (i >= 0 && i < player.ayahs.length) playAyahs(player.surah, player.ayahs, i);
  }
}

audio.addEventListener('ended', () => {
  if (player.mode === 'ayah' && player.ayahIdx >= player.ayahs.length - 1) return;
  if (player.mode === 'surah' && player.surah >= 114) return;
  step(1);
});
audio.addEventListener('play', updatePlayState);
audio.addEventListener('pause', updatePlayState);
audio.addEventListener('timeupdate', () => {
  if (audio.duration) $('#pBar').style.width = (audio.currentTime / audio.duration * 100) + '%';
});

$('#pPlay').onclick = () => (audio.paused ? audio.play() : audio.pause());
$('#pPrev').onclick = () => step(-1);
$('#pNext').onclick = () => step(1);
$('#pInfo').onclick = () => {
  location.hash = player.mode === 'surah' ? '#/listen' : `#/mushaf/${player.surah}`;
};

function updatePlayer() {
  $('#player').hidden = false;
  const name = 'سورة ' + SURAHS[player.surah - 1][0];
  const sub = player.mode === 'surah'
    ? reciterById(player.reciter).name
    : `الآية ${arNum(player.ayahs[player.ayahIdx].numberInSurah)} · ${reciterById(player.reciter).ayah ? reciterById(player.reciter).name : 'مشاري العفاسي'}`;
  $('#pTitle').textContent = name;
  $('#pSub').textContent = sub;
  if ('mediaSession' in navigator) {
    navigator.mediaSession.metadata = new MediaMetadata({ title: name, artist: sub, album: 'نور', artwork: [{ src: 'icons/icon.svg', sizes: '512x512', type: 'image/svg+xml' }] });
    navigator.mediaSession.setActionHandler('previoustrack', () => step(-1));
    navigator.mediaSession.setActionHandler('nexttrack', () => step(1));
  }
  updatePlayState();
}

function updatePlayState() {
  const playing = !audio.paused;
  $('#player').classList.toggle('playing', playing);
  document.querySelectorAll('[data-listen] .surah').forEach(el => {
    const isCur = player.mode === 'surah' && +el.dataset.n === player.surah;
    el.classList.toggle('current', isCur);
    $('.play-ic', el).innerHTML = isCur && playing ? icons.pause : icons.play;
  });
}

function highlightAyah() {
  document.querySelectorAll('.ayah.active').forEach(el => el.classList.remove('active'));
  if (player.mode !== 'ayah') return;
  const el = document.querySelector(`.ayah[data-s="${player.surah}"][data-i="${player.ayahIdx}"]`);
  if (el) { el.classList.add('active'); el.scrollIntoView({ behavior: 'smooth', block: 'center' }); }
}

/* ───────── مواقيت الصلاة ───────── */
const PRAYERS = [
  { key: 'Fajr', name: 'الفجر', icon: 'dawn' },
  { key: 'Sunrise', name: 'الشروق', icon: 'sunrise', noAdhan: true },
  { key: 'Dhuhr', name: 'الظهر', icon: 'sun' },
  { key: 'Asr', name: 'العصر', icon: 'sun' },
  { key: 'Maghrib', name: 'المغرب', icon: 'sunset' },
  { key: 'Isha', name: 'العشاء', icon: 'moon' },
];
const METHODS = [
  [3, 'رابطة العالم الإسلامي'], [4, 'أم القرى - مكة'], [5, 'الهيئة المصرية العامة للمساحة'],
  [19, 'الجزائر'], [21, 'المغرب'], [18, 'تونس'], [8, 'منطقة الخليج'], [9, 'الكويت'], [10, 'قطر'],
  [16, 'دبي'], [13, 'تركيا'], [1, 'جامعة العلوم الإسلامية - كراتشي'], [2, 'أمريكا الشمالية (ISNA)'], [12, 'فرنسا (UOIF)'],
];
const ADHANS = [['a1', 'الأذان الأول'], ['a2', 'الأذان الثاني'], ['a3', 'الأذان الثالث'], ['a4', 'الأذان الرابع'], ['a5', 'الأذان الخامس']];
const adhanUrl = id => `https://cdn.aladhan.com/audio/adhans/${id}.mp3`;

function guessMethod() {
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  const map = { 'Africa/Algiers': 19, 'Africa/Casablanca': 21, 'Africa/Tunis': 18, 'Africa/Cairo': 5, 'Asia/Riyadh': 4, 'Asia/Dubai': 16, 'Asia/Qatar': 10, 'Asia/Kuwait': 9, 'Europe/Istanbul': 13, 'Europe/Paris': 12 };
  return map[tz] || 3;
}

const adhanCfg = Object.assign(
  { loc: null, method: guessMethod(), sound: 'a1', alerts: false, on: { Fajr: true, Dhuhr: true, Asr: true, Maghrib: true, Isha: true } },
  store.get('adhan', {}),
);
const saveAdhan = () => store.set('adhan', adhanCfg);

const times = { today: null, tomorrow: null, hijri: '', loading: null };
const dateKey = d => `${String(d.getDate()).padStart(2, '0')}-${String(d.getMonth() + 1).padStart(2, '0')}-${d.getFullYear()}`;

async function fetchDay(d) {
  const loc = adhanCfg.loc;
  const cacheKey = `t:${dateKey(d)}:${adhanCfg.method}:${loc.type === 'gps' ? loc.lat.toFixed(2) + ',' + loc.lng.toFixed(2) : loc.city + ',' + loc.country}`;
  const cached = store.get('timesCache', {});
  if (cached[cacheKey]) return cached[cacheKey];
  const base = 'https://api.aladhan.com/v1/';
  const url = loc.type === 'gps'
    ? `${base}timings/${dateKey(d)}?latitude=${loc.lat}&longitude=${loc.lng}&method=${adhanCfg.method}`
    : `${base}timingsByCity/${dateKey(d)}?city=${encodeURIComponent(loc.city)}&country=${encodeURIComponent(loc.country)}&method=${adhanCfg.method}`;
  const { data } = await fetchJSON(url);
  const h = data.date.hijri;
  const day = { timings: data.timings, hijri: `${arNum(+h.day)} ${h.month.ar} ${arNum(+h.year)} هـ` };
  // نحتفظ بآخر أيام قليلة فقط
  const keys = Object.keys(cached);
  if (keys.length > 6) keys.slice(0, keys.length - 6).forEach(k => delete cached[k]);
  cached[cacheKey] = day;
  store.set('timesCache', cached);
  return day;
}

function loadTimes(force) {
  if (!adhanCfg.loc) return Promise.resolve();
  if (times.loading && !force) return times.loading;
  const now = new Date();
  const tmr = new Date(now); tmr.setDate(now.getDate() + 1);
  times.loading = Promise.all([fetchDay(now), fetchDay(tmr)]).then(([a, b]) => {
    times.today = a; times.tomorrow = b; times.hijri = a.hijri; times.day = dateKey(now);
  }).catch(() => { times.error = true; }).finally(() => { times.loading = null; });
  return times.loading;
}

function toDate(hm, base = new Date(), addDay = 0) {
  const [h, m] = hm.split(' ')[0].split(':').map(Number);
  const d = new Date(base); d.setDate(d.getDate() + addDay); d.setHours(h, m, 0, 0);
  return d;
}
function fmt12(hm) {
  let [h, m] = hm.split(' ')[0].split(':').map(Number);
  const p = h < 12 ? 'ص' : 'م';
  h = h % 12 || 12;
  return `${h}:${String(m).padStart(2, '0')} ${p}`;
}

function nextPrayer() {
  if (!times.today) return null;
  const now = new Date();
  for (const p of PRAYERS) {
    if (p.noAdhan) continue;
    const t = toDate(times.today.timings[p.key]);
    if (t > now) return { ...p, at: t, hm: times.today.timings[p.key] };
  }
  const hm = (times.tomorrow || times.today).timings.Fajr;
  return { ...PRAYERS[0], at: toDate(hm, now, 1), hm, tomorrow: true };
}

function countdownText(ms) {
  const s = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(s / 3600), m = Math.floor(s % 3600 / 60), sec = s % 60;
  return [h, m, sec].map(v => String(v).padStart(2, '0')).join(':');
}

function locate() {
  return new Promise((res, rej) => {
    if (!navigator.geolocation) return rej(new Error('unsupported'));
    navigator.geolocation.getCurrentPosition(
      p => res({ type: 'gps', lat: p.coords.latitude, lng: p.coords.longitude, label: 'موقعي الحالي' }),
      rej, { timeout: 15000, maximumAge: 3600e3 },
    );
  });
}

async function useGps() {
  toast('جارٍ تحديد موقعك…');
  try {
    adhanCfg.loc = await locate();
    saveAdhan();
    await loadTimes(true);
    render();
    toast('تم تحديث المواقيت');
  } catch {
    toast('تعذّر تحديد الموقع، أدخل مدينتك يدويًا');
  }
}

// فحص دخول وقت الصلاة كل ثانية
let lastFired = store.get('lastFired', '');
function checkAdhan() {
  if (!times.today) return;
  const now = new Date();
  if (times.day !== dateKey(now)) { loadTimes(true).then(render); return; }
  for (const p of PRAYERS) {
    if (p.noAdhan || !adhanCfg.on[p.key]) continue;
    const t = toDate(times.today.timings[p.key]);
    const diff = now - t;
    const id = dateKey(now) + p.key;
    if (diff >= 0 && diff < 60e3 && lastFired !== id) {
      lastFired = id; store.set('lastFired', id);
      fireAdhan(p);
    }
  }
}

function fireAdhan(p) {
  const a = $('#adhanAudio');
  a.src = adhanUrl(adhanCfg.sound);
  if (!audio.paused) audio.pause();
  a.play().catch(() => {});
  if (adhanCfg.alerts && 'Notification' in window && Notification.permission === 'granted') {
    try { new Notification(`حان الآن موعد صلاة ${p.name}`, { body: 'حيّ على الصلاة، حيّ على الفلاح', icon: 'icons/icon.svg', tag: 'adhan' }); } catch { /* بعض المتصفحات تمنعه */ }
  }
  const box = document.createElement('div');
  box.className = 'adhan-alert';
  box.innerHTML = `<div class="card">
    <div class="big-ic">${icons.mosque}</div>
    <h2>صلاة ${p.name}</h2>
    <p class="muted">حان الآن موعد الصلاة · ${fmt12(times.today.timings[p.key])}</p>
    <button class="btn" style="width:100%">إيقاف الأذان</button></div>`;
  box.querySelector('button').onclick = () => { a.pause(); box.remove(); };
  a.onended = () => box.remove();
  document.body.append(box);
}

function tick() {
  checkAdhan();
  const np = nextPrayer();
  if (!np) return;
  const txt = countdownText(np.at - new Date());
  document.querySelectorAll('[data-countdown]').forEach(el => { el.textContent = txt; });
}
setInterval(tick, 1000);

/* ───────── المصحف ───────── */
const surahCache = new Map();
async function getSurah(n) {
  if (surahCache.has(n)) return surahCache.get(n);
  const { data } = await fetchJSON(`https://api.alquran.cloud/v1/surah/${n}/quran-uthmani`);
  const ayahs = data.ayahs.map(a => ({ number: a.number, numberInSurah: a.numberInSurah, text: a.text, page: a.page, juz: a.juz }));
  // فصل البسملة عن الآية الأولى (عدا الفاتحة)
  if (n !== 1 && n !== 9) {
    const w = ayahs[0].text.split(' ');
    if (w[0].startsWith('بِسْمِ')) ayahs[0].text = w.slice(4).join(' ');
  }
  surahCache.set(n, ayahs);
  return ayahs;
}

const bookmarks = () => store.get('bookmarks', []);
function toggleBookmark(s, a) {
  let b = bookmarks();
  const i = b.findIndex(x => x.s === s && x.a === a);
  if (i >= 0) b.splice(i, 1); else b.unshift({ s, a });
  store.set('bookmarks', b);
  return i < 0;
}

/* ───────── التوجيه ───────── */
const TITLES = { home: 'نور', listen: 'القرآن المسموع', mushaf: 'المصحف', adhan: 'مواقيت الأذان', mawaiz: 'مواعظ' };
let cleanup = null;

function render() {
  if (cleanup) { cleanup(); cleanup = null; }
  document.querySelectorAll('.ayah-sheet').forEach(el => el.remove());
  const parts = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
  const tab = parts[0] || 'home';
  document.querySelectorAll('.tabbar a').forEach(a => a.classList.toggle('active', a.dataset.tab === tab));
  $('#backBtn').hidden = true;
  $('#pageTitle').textContent = TITLES[tab] || 'نور';
  const routes = { home: renderHome, listen: renderListen, mushaf: renderMushaf, adhan: renderAdhan, mawaiz: renderMawaiz };
  (routes[tab] || renderHome)(parts.slice(1));
  updatePlayState();
}
window.addEventListener('hashchange', () => { render(); window.scrollTo(0, 0); });
$('#backBtn').onclick = () => { location.hash = '#/mushaf'; };

/* ───────── الرئيسية ───────── */
function greeting() {
  const h = new Date().getHours();
  if (h < 5) return 'ليلة مباركة';
  if (h < 12) return 'صباح الخير';
  if (h < 17) return 'طاب يومك';
  return 'مساء الخير';
}

function dailyWa3z() {
  const d = new Date();
  const day = Math.floor((d - new Date(d.getFullYear(), 0, 0)) / 864e5);
  return MAWAIZ[day % MAWAIZ.length];
}

function heroHTML(withStrip = true) {
  if (!adhanCfg.loc) {
    return `<section class="hero">
      <div class="hero-top"><span>${greeting()}</span></div>
      <div class="hero-label">مواقيت الصلاة</div>
      <div class="hero-prayer" style="font-size:24px">حدّد موقعك لعرض المواقيت</div>
      <button class="btn ghost" id="heroGps" style="margin-top:16px;background:rgba(255,255,255,.16);color:#fff">${icons.pin} استخدام موقعي</button>
    </section>`;
  }
  if (!times.today) {
    return `<section class="hero"><div class="hero-top"><span>${greeting()}</span></div>
      <div class="hero-label">${times.error ? 'تعذّر تحميل المواقيت' : 'جارٍ تحميل المواقيت…'}</div><div class="hero-prayer">—</div></section>`;
  }
  const np = nextPrayer();
  const strip = PRAYERS.filter(p => !p.noAdhan).map(p => `<div class="${p.key === np.key && !np.tomorrow ? 'now' : ''}">${p.name}<b>${fmt12(times.today.timings[p.key]).replace(/ [صم]$/, '')}</b></div>`).join('');
  return `<a href="#/adhan" class="hero" style="display:block">
    <div class="hero-top"><span>${greeting()}</span><span>${times.hijri}</span></div>
    <div class="hero-label">الصلاة القادمة</div>
    <div class="hero-prayer">${np.name}</div>
    <div class="hero-time">${fmt12(np.hm)}${np.tomorrow ? ' · غدًا' : ''}</div>
    <div class="countdown">متبقٍّ <span data-countdown>--:--:--</span></div>
    ${withStrip ? `<div class="hero-strip">${strip}</div>` : ''}
  </a>`;
}

function wa3zCard(w, i) {
  const saved = store.get('savedWa3z', []).includes(i);
  return `<article class="card wa3z" data-i="${i}">
    <span class="tag">${icons.star.replace('<svg', '<svg style="width:14px;height:14px"')} ${w.type === 'ayah' ? 'آية' : 'حديث'}</span>
    <h3>${esc(w.title)}</h3>
    <p class="text ${w.type === 'ayah' ? 'ayah' : ''}">${esc(w.text)}</p>
    <div class="src">${esc(w.source)}</div>
    <div class="note">${esc(w.note)}</div>
    <div class="actions">
      <button class="icon-btn" data-act="copy" aria-label="نسخ">${icons.copy}</button>
      <button class="icon-btn" data-act="share" aria-label="مشاركة">${icons.share}</button>
      <button class="icon-btn ${saved ? 'on' : ''}" data-act="save" aria-label="حفظ">${icons.bookmark}</button>
    </div>
  </article>`;
}

async function shareText(text) {
  if (navigator.share) { try { await navigator.share({ text }); return; } catch { return; } }
  copyText(text);
}
function copyText(text) {
  navigator.clipboard?.writeText(text).then(() => toast('تم النسخ'), () => toast('تعذّر النسخ'));
}

function bindWa3zActions(container, onChange) {
  container.addEventListener('click', e => {
    const btn = e.target.closest('[data-act]');
    if (!btn) return;
    const i = +btn.closest('.wa3z').dataset.i;
    const w = MAWAIZ[i];
    const full = `${w.type === 'ayah' ? '﴿' + w.text + '﴾' : '«' + w.text + '»'}\n${w.source}`;
    if (btn.dataset.act === 'copy') copyText(full);
    if (btn.dataset.act === 'share') shareText(full);
    if (btn.dataset.act === 'save') {
      const s = store.get('savedWa3z', []);
      const k = s.indexOf(i);
      if (k >= 0) s.splice(k, 1); else s.push(i);
      store.set('savedWa3z', s);
      btn.classList.toggle('on', k < 0);
      toast(k < 0 ? 'أُضيفت إلى المحفوظات' : 'أُزيلت من المحفوظات');
      onChange?.();
    }
  });
}

function renderHome() {
  const last = store.get('lastRead');
  const lastL = store.get('lastListen');
  const w = dailyWa3z();
  let cont = '';
  if (last) {
    cont += `<a class="card continue" href="#/mushaf/${last.s}/${last.a}">
      <span class="tile-ic">${icons.book}</span>
      <div><small class="muted">تابع القراءة</small><strong>سورة ${SURAHS[last.s - 1][0]}</strong><small class="muted">الآية ${arNum(last.a)}</small></div>
    </a>`;
  }
  if (lastL) {
    cont += `<button class="card continue" id="contListen" style="width:100%;text-align:start">
      <span class="tile-ic gold">${icons.headphones}</span>
      <div><small class="muted">تابع الاستماع</small><strong>سورة ${SURAHS[lastL.surah - 1][0]}</strong><small class="muted">${reciterById(lastL.reciter).name}</small></div>
      <span class="play-btn">${icons.play}</span>
    </button>`;
  }
  view.innerHTML = `
    ${heroHTML()}
    <div class="tiles">
      <a class="tile" href="#/listen"><span class="tile-ic">${icons.headphones}</span><strong>القرآن المسموع</strong><small>${arNum(RECITERS.length)} قرّاء</small></a>
      <a class="tile" href="#/mushaf"><span class="tile-ic gold">${icons.book}</span><strong>المصحف</strong><small>١١٤ سورة</small></a>
      <a class="tile" href="#/adhan"><span class="tile-ic gold">${icons.mosque}</span><strong>الأذان</strong><small>المواقيت والتنبيه</small></a>
      <a class="tile" href="#/mawaiz"><span class="tile-ic">${icons.star}</span><strong>مواعظ</strong><small>آيات وأحاديث</small></a>
    </div>
    ${cont ? `<div class="section-head"><h2>تابع من حيث توقفت</h2></div><div class="list">${cont}</div>` : ''}
    <div class="section-head"><h2>موعظة اليوم</h2><a href="#/mawaiz">المزيد</a></div>
    <div id="dailyBox">${wa3zCard(w, MAWAIZ.indexOf(w))}</div>
  `;
  $('#heroGps')?.addEventListener('click', useGps);
  $('#contListen')?.addEventListener('click', () => {
    if (player.mode === 'surah' && player.surah === lastL.surah && audio.src) { audio.play(); } else playSurah(lastL.surah, lastL.reciter);
  });
  bindWa3zActions($('#dailyBox'));
  tick();
}

/* ───────── الاستماع ───────── */
function surahRows(filter, mode) {
  const q = normalize(filter || '');
  return SURAHS.map((s, i) => [s, i + 1])
    .filter(([s, n]) => !q || normalize(s[0]).includes(q) || String(n) === q)
    .map(([s, n]) => `<button class="surah" data-n="${n}">
      <span class="num">${arNum(n)}</span>
      <span class="meta"><strong>سورة ${s[0]}</strong><small>${surahSub(n)}</small></span>
      ${mode === 'listen' ? `<span class="play-ic">${icons.play}</span>` : ''}
    </button>`).join('') || '<div class="empty">لا توجد نتائج</div>';
}

function renderListen() {
  const r = reciterById(player.reciter);
  view.innerHTML = `
    <div class="card reciter-card">
      <span class="avatar">${r.name[0]}</span>
      <div>
        <small>القارئ</small>
        <select class="select" id="reciterSel" aria-label="اختيار القارئ">
          ${RECITERS.map(x => `<option value="${x.id}" ${x.id === r.id ? 'selected' : ''}>${x.name}</option>`).join('')}
        </select>
      </div>
    </div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة بالاسم أو الرقم"></label>
    <div class="surah-list" data-listen id="list">${surahRows('', 'listen')}</div>`;
  $('#reciterSel').onchange = e => {
    player.reciter = e.target.value;
    store.set('reciter', player.reciter);
    $('.avatar').textContent = reciterById(player.reciter).name[0];
    if (player.mode === 'surah') playSurah(player.surah);
  };
  $('#q').oninput = e => { $('#list').innerHTML = surahRows(e.target.value, 'listen'); updatePlayState(); };
  $('#list').onclick = e => {
    const b = e.target.closest('.surah');
    if (!b) return;
    const n = +b.dataset.n;
    if (player.mode === 'surah' && player.surah === n && player.reciter === $('#reciterSel').value) {
      audio.paused ? audio.play() : audio.pause();
    } else playSurah(n, $('#reciterSel').value);
  };
}

/* ───────── المصحف ───────── */
function renderMushaf(args) {
  if (args[0]) return renderReader(+args[0], +(args[1] || 0));
  const last = store.get('lastRead');
  const bm = bookmarks();
  view.innerHTML = `
    ${last ? `<a class="card continue" href="#/mushaf/${last.s}/${last.a}" style="margin-bottom:14px">
      <span class="tile-ic">${icons.book}</span>
      <div><small class="muted">آخر قراءة</small><strong>سورة ${SURAHS[last.s - 1][0]}</strong><small class="muted">الآية ${arNum(last.a)}</small></div>
    </a>` : ''}
    ${bm.length ? `<div class="chips" style="margin-bottom:12px">${bm.slice(0, 12).map(b => `<a class="chip" href="#/mushaf/${b.s}/${b.a}" style="display:inline-flex;align-items:center;gap:6px">${icons.bookmark.replace('<svg', '<svg style="width:14px;height:14px;color:var(--gold)"')} ${SURAHS[b.s - 1][0]} ${arNum(b.a)}</a>`).join('')}</div>` : ''}
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة"></label>
    <div class="surah-list" id="list">${surahRows('')}</div>`;
  $('#q').oninput = e => { $('#list').innerHTML = surahRows(e.target.value); };
  $('#list').onclick = e => {
    const b = e.target.closest('.surah');
    if (b) location.hash = `#/mushaf/${b.dataset.n}`;
  };
}

async function renderReader(n, goto) {
  if (!(n >= 1 && n <= 114)) { location.hash = '#/mushaf'; return; }
  const s = SURAHS[n - 1];
  $('#pageTitle').textContent = 'سورة ' + s[0];
  $('#backBtn').hidden = false;
  view.innerHTML = '<div class="loader"><div class="spinner"></div>جارٍ تحميل السورة…</div>';
  let ayahs;
  try { ayahs = await getSurah(n); } catch {
    view.innerHTML = `<div class="error-box">تعذّر تحميل السورة. تحقق من الاتصال بالإنترنت.<br><br><button class="btn" id="retry">إعادة المحاولة</button></div>`;
    $('#retry').onclick = () => renderReader(n, goto);
    return;
  }
  if (location.hash.split('/')[2] != n) return; // تغيّرت الصفحة أثناء التحميل
  const marks = new Set(bookmarks().filter(b => b.s === n).map(b => b.a));
  const size = store.get('qsize', 26);
  view.innerHTML = `
    <div class="reader-bar">
      <button class="icon-btn" id="fsMinus" aria-label="تصغير الخط">${icons.minus}</button>
      <button class="icon-btn" id="fsPlus" aria-label="تكبير الخط">${icons.plus}</button>
      <span class="spacer"></span>
      <button class="btn" id="listenAll">${icons.headphones} استمع للسورة</button>
    </div>
    <article class="mushaf-page" style="--qsize:${size}px">
      <header class="surah-banner"><h2>سورة ${s[0]}</h2><small>${surahSub(n)} · الجزء ${arNum(ayahs[0].juz)}</small></header>
      ${n !== 1 && n !== 9 ? '<div class="basmala">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>' : ''}
      <p class="quran-text">${ayahs.map((a, i) => `<span class="ayah ${marks.has(a.numberInSurah) ? 'marked' : ''}" data-s="${n}" data-i="${i}" id="a${a.numberInSurah}">${a.text}<span class="end">۝${arNum(a.numberInSurah)}</span></span> `).join('')}</p>
    </article>
    <div class="reader-nav">
      ${n < 114 ? `<a class="btn ghost" href="#/mushaf/${n + 1}">السورة التالية: ${SURAHS[n][0]}</a>` : ''}
      ${n > 1 ? `<a class="btn ghost" href="#/mushaf/${n - 1}">السابقة: ${SURAHS[n - 2][0]}</a>` : ''}
    </div>`;

  const page = $('.mushaf-page');
  const setSize = d => {
    const v = Math.min(40, Math.max(18, store.get('qsize', 26) + d));
    store.set('qsize', v);
    page.style.setProperty('--qsize', v + 'px');
  };
  $('#fsMinus').onclick = () => setSize(-2);
  $('#fsPlus').onclick = () => setSize(2);
  $('#listenAll').onclick = () => playAyahs(n, ayahs, 0);
  page.onclick = e => {
    const el = e.target.closest('.ayah');
    if (el) openAyahSheet(n, ayahs, +el.dataset.i, el);
  };

  const prevRead = store.get('lastRead', {});
  store.set('lastRead', { s: n, a: goto || (prevRead.s === n ? prevRead.a : 1) });
  if (goto) { const el = $('#a' + goto); if (el) setTimeout(() => el.scrollIntoView({ block: 'center' }), 50); }
  if (player.mode === 'ayah' && player.surah === n) highlightAyah();

  // حفظ موضع القراءة تلقائيًا أثناء التمرير
  const io = new IntersectionObserver(entries => {
    const vis = entries.filter(e => e.isIntersecting).map(e => +e.target.id.slice(1));
    if (vis.length) store.set('lastRead', { s: n, a: Math.min(...vis) });
  }, { rootMargin: '-30% 0px -60% 0px' });
  document.querySelectorAll('.ayah').forEach(el => io.observe(el));
  cleanup = () => io.disconnect();
}

function openAyahSheet(n, ayahs, i, el) {
  const a = ayahs[i];
  const marked = bookmarks().some(b => b.s === n && b.a === a.numberInSurah);
  const wrap = document.createElement('div');
  wrap.className = 'ayah-sheet';
  wrap.innerHTML = `<div class="sheet" role="dialog" aria-label="خيارات الآية">
    <div class="grab"></div>
    <h3>سورة ${SURAHS[n - 1][0]} · الآية ${arNum(a.numberInSurah)}</h3>
    <small class="muted">الصفحة ${arNum(a.page)} · الجزء ${arNum(a.juz)}</small>
    <div class="sheet-actions">
      <button data-a="play">${icons.play}استمع من هنا</button>
      <button data-a="mark">${icons.bookmark}${marked ? 'إزالة العلامة' : 'حفظ علامة'}</button>
      <button data-a="copy">${icons.copy}نسخ الآية</button>
    </div></div>`;
  wrap.onclick = e => {
    const b = e.target.closest('[data-a]');
    if (e.target === wrap) return wrap.remove();
    if (!b) return;
    if (b.dataset.a === 'play') playAyahs(n, ayahs, i);
    if (b.dataset.a === 'mark') {
      const on = toggleBookmark(n, a.numberInSurah);
      el.classList.toggle('marked', on);
      toast(on ? 'تم حفظ العلامة' : 'أُزيلت العلامة');
    }
    if (b.dataset.a === 'copy') copyText(`﴿${a.text}﴾ [${SURAHS[n - 1][0]}: ${a.numberInSurah}]`);
    wrap.remove();
  };
  document.body.append(wrap);
}

/* ───────── الأذان ───────── */
function renderAdhan() {
  const loc = adhanCfg.loc;
  const np = nextPrayer();
  const now = new Date();
  const list = times.today ? PRAYERS.map(p => {
    const hm = times.today.timings[p.key];
    const isNext = np && np.key === p.key && !np.tomorrow;
    const passed = toDate(hm) < now && !isNext;
    return `<div class="prayer ${isNext ? 'next' : ''} ${passed ? 'passed' : ''}">
      <span class="p-ic">${icons[p.icon]}</span>
      <span class="p-name">${p.name}${isNext ? '<small>متبقٍّ <span data-countdown></span></small>' : ''}</span>
      <span class="p-time">${fmt12(hm)}</span>
      ${p.noAdhan ? '<span class="bell"></span>' : `<button class="bell ${adhanCfg.on[p.key] ? 'on' : ''}" data-p="${p.key}" aria-label="تنبيه ${p.name}">${adhanCfg.on[p.key] ? icons.bell : icons.bellOff}</button>`}
    </div>`;
  }).join('') : '';

  view.innerHTML = `
    ${heroHTML(false)}
    ${loc && !times.today ? `<div class="loader">${times.error ? 'تعذّر تحميل المواقيت. تحقق من الاتصال أو من اسم المدينة.' : '<div class="spinner"></div>'}</div>` : ''}
    ${list ? `<div class="prayer-list">${list}</div>` : ''}

    <div class="section-head"><h2>الموقع</h2></div>
    <div class="card settings">
      ${loc ? `<div class="location-line">${icons.pin} ${loc.type === 'gps' ? 'موقعي الحالي (GPS)' : esc(loc.city + '، ' + loc.country)}</div>` : ''}
      <button class="btn" id="gpsBtn">${icons.pin} تحديد موقعي تلقائيًا</button>
      <div class="muted" style="text-align:center;font-size:13px">أو أدخل المدينة يدويًا</div>
      <div class="row">
        <input class="input" id="city" placeholder="المدينة" value="${loc?.type === 'city' ? esc(loc.city) : ''}">
        <input class="input" id="country" placeholder="الدولة" value="${loc?.type === 'city' ? esc(loc.country) : ''}">
      </div>
      <button class="btn ghost" id="cityBtn">حفظ المدينة</button>
    </div>

    <div class="section-head"><h2>الإعدادات</h2></div>
    <div class="card settings">
      <div class="field"><label for="method">طريقة الحساب</label>
        <select class="select" id="method">${METHODS.map(([v, l]) => `<option value="${v}" ${v === adhanCfg.method ? 'selected' : ''}>${l}</option>`).join('')}</select>
      </div>
      <div class="field"><label for="sound">صوت الأذان</label>
        <div class="row">
          <select class="select" id="sound">${ADHANS.map(([v, l]) => `<option value="${v}" ${v === adhanCfg.sound ? 'selected' : ''}>${l}</option>`).join('')}</select>
          <button class="btn ghost" id="preview" style="flex:none">${icons.play} تجربة</button>
        </div>
      </div>
      <label class="switch-row">
        <span>إشعارات الأذان<small>تنبيه عند دخول وقت كل صلاة</small></span>
        <span class="switch"><input type="checkbox" id="alerts" ${adhanCfg.alerts ? 'checked' : ''}><i></i></span>
      </label>
      <div class="note-box">يُرفع الأذان تلقائيًا عند دخول الوقت ما دام التطبيق مفتوحًا. لأفضل تجربة، ثبّت التطبيق على شاشتك الرئيسية من قائمة المتصفح.</div>
    </div>`;

  $('#heroGps')?.addEventListener('click', useGps);
  $('#gpsBtn').onclick = useGps;
  $('#cityBtn').onclick = async () => {
    const city = $('#city').value.trim(), country = $('#country').value.trim();
    if (!city || !country) return toast('أدخل اسم المدينة والدولة');
    adhanCfg.loc = { type: 'city', city, country };
    saveAdhan(); times.today = null; times.error = false;
    render();
    await loadTimes(true);
    render();
  };
  $('#method').onchange = async e => {
    adhanCfg.method = +e.target.value; saveAdhan();
    await loadTimes(true); render();
  };
  const prev = $('#adhanAudio');
  $('#sound').onchange = e => { adhanCfg.sound = e.target.value; saveAdhan(); prev.pause(); };
  $('#preview').onclick = () => {
    if (!prev.paused) { prev.pause(); $('#preview').innerHTML = `${icons.play} تجربة`; return; }
    prev.src = adhanUrl(adhanCfg.sound);
    prev.play().catch(() => toast('تعذّر تشغيل الصوت'));
    $('#preview').innerHTML = `${icons.pause} إيقاف`;
  };
  $('#alerts').onchange = async e => {
    adhanCfg.alerts = e.target.checked;
    if (adhanCfg.alerts && 'Notification' in window && Notification.permission === 'default') {
      const p = await Notification.requestPermission();
      if (p !== 'granted') toast('لم يُسمح بالإشعارات، سيُرفع الأذان داخل التطبيق فقط');
    }
    saveAdhan();
  };
  view.querySelectorAll('.bell[data-p]').forEach(b => {
    b.onclick = () => {
      const k = b.dataset.p;
      adhanCfg.on[k] = !adhanCfg.on[k]; saveAdhan();
      b.classList.toggle('on', adhanCfg.on[k]);
      b.innerHTML = adhanCfg.on[k] ? icons.bell : icons.bellOff;
      toast(adhanCfg.on[k] ? 'تم تفعيل الأذان' : 'تم إيقاف الأذان لهذه الصلاة');
    };
  });
  cleanup = () => { if (!prev.paused && !document.querySelector('.adhan-alert')) prev.pause(); };
  tick();
}

/* ───────── المواعظ ───────── */
function renderMawaiz() {
  let cat = store.get('wa3zCat', 'all');
  const cats = [...CATEGORIES, { id: 'saved', name: 'المحفوظة' }];
  const draw = () => {
    const saved = store.get('savedWa3z', []);
    const items = MAWAIZ.map((w, i) => [w, i]).filter(([w, i]) => cat === 'all' || (cat === 'saved' ? saved.includes(i) : w.cat === cat));
    $('#wlist').innerHTML = items.map(([w, i]) => wa3zCard(w, i)).join('') || `<div class="empty">${cat === 'saved' ? 'لم تحفظ أي موعظة بعد. اضغط على علامة الحفظ في أي موعظة.' : 'لا توجد مواعظ'}</div>`;
  };
  view.innerHTML = `
    <div class="chips" id="cats">${cats.map(c => `<button class="chip ${c.id === cat ? 'active' : ''}" data-c="${c.id}">${c.name}</button>`).join('')}</div>
    <div class="list" id="wlist" style="margin-top:10px"></div>`;
  $('#cats').onclick = e => {
    const b = e.target.closest('.chip');
    if (!b) return;
    cat = b.dataset.c; store.set('wa3zCat', cat);
    document.querySelectorAll('#cats .chip').forEach(x => x.classList.toggle('active', x === b));
    draw();
  };
  bindWa3zActions($('#wlist'), () => { if (cat === 'saved') draw(); });
  draw();
}

/* ───────── البدء ───────── */
render();
if (adhanCfg.loc) {
  loadTimes().then(() => {
    const tab = location.hash.split('/')[1] || 'home';
    if (tab === 'home' || tab === 'adhan') render();
  });
}

if ('serviceWorker' in navigator && location.protocol !== 'file:') {
  navigator.serviceWorker.register('sw.js').catch(() => {});
}
