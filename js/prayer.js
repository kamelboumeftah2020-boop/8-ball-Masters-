// مواقيت الصلاة، ورفع الأذان، واتجاه القبلة
import { $, store, arNum, fetchJSON, toast, icons } from './core.js';
import { audio } from './player.js';
import { isNative, Notifications, BUNDLED_ADHANS, nativePlugin } from './native.js';
import { scheduleReminders, remCfg, minLabel } from './reminders.js';

export const PRAYERS = [
  { key: 'Fajr', name: 'الفجر', icon: 'dawn' },
  { key: 'Sunrise', name: 'الشروق', icon: 'sunrise', noAdhan: true },
  { key: 'Dhuhr', name: 'الظهر', icon: 'sun' },
  { key: 'Asr', name: 'العصر', icon: 'sun' },
  { key: 'Maghrib', name: 'المغرب', icon: 'sunset' },
  { key: 'Isha', name: 'العشاء', icon: 'moon' },
];
export const METHODS = [
  [4, 'أم القرى - مكة المكرمة'], [3, 'رابطة العالم الإسلامي'], [5, 'الهيئة المصرية العامة للمساحة'],
  [19, 'الجزائر'], [21, 'المغرب'], [18, 'تونس'], [8, 'منطقة الخليج'], [9, 'الكويت'], [10, 'قطر'],
  [16, 'دبي'], [13, 'تركيا'], [1, 'جامعة العلوم الإسلامية - كراتشي'], [2, 'أمريكا الشمالية (ISNA)'], [12, 'فرنسا (UOIF)'],
];
// أذان مؤذني المسجد الحرام — من موقع IslamHouse
export const ADHANS = [
  ['008', 'علي بن أحمد ملا'], ['007', 'عبد الملك ملا'], ['001', 'أحمد بصراوي'], ['002', 'توفيق خوج'],
  ['003', 'حسان زبيدي'], ['004', 'حسن شحات'], ['005', 'عبد العزيز ريس'], ['006', 'عبد الله ريس'],
  ['009', 'غازي السعدون'], ['010', 'فاروق حضراوي'],
];
export const adhanUrl = id => `https://d1.islamhouse.com/data/ar/ih_sounds/chain/Athan/ar_${id}_Athan.mp3`;
const KAABA = { lat: 21.422487, lng: 39.826206 };

function guessMethod() {
  const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
  const map = { 'Africa/Algiers': 19, 'Africa/Casablanca': 21, 'Africa/Tunis': 18, 'Africa/Cairo': 5, 'Asia/Riyadh': 4, 'Asia/Dubai': 16, 'Asia/Qatar': 10, 'Asia/Kuwait': 9, 'Europe/Istanbul': 13, 'Europe/Paris': 12 };
  return map[tz] || 4;
}

export const cfg = Object.assign(
  { loc: null, method: guessMethod(), school: 0, tune: {}, sound: '008', alerts: false, on: { Fajr: true, Dhuhr: true, Asr: true, Maghrib: true, Isha: true } },
  store.get('adhan', {}),
);
if (!ADHANS.some(a => a[0] === cfg.sound)) cfg.sound = '008';
export const saveCfg = () => store.set('adhan', cfg);

export const times = { today: null, tomorrow: null, hijri: null, error: false, loading: null, day: null };
export const events = new EventTarget();
const dateKey = d => `${String(d.getDate()).padStart(2, '0')}-${String(d.getMonth() + 1).padStart(2, '0')}-${d.getFullYear()}`;

// نجلب مواقيت الشهر كاملًا ونحفظها؛ فتعمل المواقيت دون اتصال، ويمكن جدولة الأذان مسبقًا
const paramsKey = () => {
  const loc = cfg.loc;
  const tune = ['Imsak', 'Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Sunset', 'Isha', 'Midnight'].map(k => cfg.tune[k] || 0).join(',');
  const where = loc.type === 'gps' ? `${loc.lat.toFixed(3)},${loc.lng.toFixed(3)}` : `${loc.city},${loc.country}`;
  return { tune, key: `${cfg.method}|${cfg.school}|${tune}|${where}` };
};
const monthLoads = new Map();
async function fetchMonth(y, m) {
  const { tune, key } = paramsKey();
  const cacheKey = `${y}-${m}|${key}`;
  const cached = store.get('calCache', {});
  if (cached[cacheKey]) return cached[cacheKey];
  if (monthLoads.has(cacheKey)) return monthLoads.get(cacheKey);
  const loc = cfg.loc;
  const base = 'https://api.aladhan.com/v1/';
  const q = `method=${cfg.method}&school=${cfg.school}&tune=${tune}`;
  const url = loc.type === 'gps'
    ? `${base}calendar/${y}/${m}?latitude=${loc.lat}&longitude=${loc.lng}&${q}`
    : `${base}calendarByCity/${y}/${m}?city=${encodeURIComponent(loc.city)}&country=${encodeURIComponent(loc.country)}&${q}`;
  const p = fetchJSON(url).then(({ data }) => {
    const days = {};
    for (const x of data) {
      const h = x.date.hijri;
      const t = {};
      for (const k of ['Fajr', 'Sunrise', 'Dhuhr', 'Asr', 'Maghrib', 'Isha']) t[k] = x.timings[k].split(' ')[0];
      days[x.date.gregorian.date] = {
        timings: t,
        hijri: { day: +h.day, month: h.month.ar, monthNum: h.month.number, year: +h.year, text: `${arNum(+h.day)} ${h.month.ar} ${arNum(+h.year)} هـ` },
        coords: { lat: x.meta.latitude, lng: x.meta.longitude },
      };
    }
    const all = store.get('calCache', {});
    const keys = Object.keys(all);
    if (keys.length > 3) keys.slice(0, keys.length - 3).forEach(k => delete all[k]);
    all[cacheKey] = days;
    store.set('calCache', all);
    return days;
  }).finally(() => monthLoads.delete(cacheKey));
  monthLoads.set(cacheKey, p);
  return p;
}

export async function fetchDay(d) {
  const days = await fetchMonth(d.getFullYear(), d.getMonth() + 1);
  const day = days[dateKey(d)];
  if (!day) throw new Error('no-day');
  return day;
}

export function loadTimes(force) {
  if (!cfg.loc) return Promise.resolve();
  if (times.loading && !force) return times.loading;
  const now = new Date();
  const tmr = new Date(now); tmr.setDate(now.getDate() + 1);
  times.error = false;
  times.loading = Promise.all([fetchDay(now), fetchDay(tmr)]).then(([a, b]) => {
    Object.assign(times, { today: a, tomorrow: b, hijri: a.hijri, day: dateKey(now) });
    if (cfg.loc.type === 'city') { cfg.loc.lat = a.coords.lat; cfg.loc.lng = a.coords.lng; saveCfg(); }
  }).catch(() => { times.error = true; }).finally(() => {
    times.loading = null;
    events.dispatchEvent(new Event('update'));
  });
  resolvePlace();
  return times.loading;
}

// اسم المدينة: المكتوب يدويًا، أو المستخرج من إحداثيات GPS (يُحفظ فيعمل دون اتصال)
export function placeName() {
  const loc = cfg.loc;
  if (!loc) return '';
  return loc.type === 'city' ? loc.city : loc.name || '';
}
let placeJob = null;
function resolvePlace() {
  const loc = cfg.loc;
  if (!loc || loc.type !== 'gps' || loc.name || placeJob) return;
  const url = `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${loc.lat}&longitude=${loc.lng}&localityLanguage=ar`;
  placeJob = fetchJSON(url).then(d => {
    const name = d.city || d.locality || d.principalSubdivision || '';
    if (!name || cfg.loc !== loc) return;
    loc.name = name;
    if (d.countryName) loc.country = d.countryName;
    saveCfg();
    events.dispatchEvent(new Event('update'));
    scheduleAdhans();
  }).catch(() => {}).finally(() => { placeJob = null; });
}

export function toDate(hm, base = new Date(), addDay = 0) {
  const [h, m] = hm.split(' ')[0].split(':').map(Number);
  const d = new Date(base); d.setDate(d.getDate() + addDay); d.setHours(h, m, 0, 0);
  return d;
}
export function fmt12(hm, withSuffix = true) {
  let [h, m] = hm.split(' ')[0].split(':').map(Number);
  const p = h < 12 ? 'ص' : 'م';
  h = h % 12 || 12;
  return `${h}:${String(m).padStart(2, '0')}${withSuffix ? ' ' + p : ''}`;
}

export function nextPrayer() {
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

// الفترة الحالية: لاقتراح أذكار الصباح أو المساء أو النوم
export function currentPeriod() {
  if (!times.today) {
    const h = new Date().getHours();
    return h >= 4 && h < 12 ? 'morning' : h >= 15 && h < 20 ? 'evening' : h >= 21 || h < 4 ? 'sleep' : null;
  }
  const now = new Date(), t = k => toDate(times.today.timings[k]);
  if (now >= t('Fajr') && now < t('Dhuhr')) return 'morning';
  if (now >= t('Asr') && now < t('Isha')) return 'evening';
  if (now >= new Date(t('Isha').getTime() + 3600e3) || now < t('Fajr')) return 'sleep';
  return null;
}

export function countdownText(ms) {
  const s = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(s / 3600), m = Math.floor(s % 3600 / 60), sec = s % 60;
  return [h, m, sec].map(v => String(v).padStart(2, '0')).join(':');
}

export function locate() {
  return new Promise((res, rej) => {
    if (!navigator.geolocation) return rej(new Error('unsupported'));
    navigator.geolocation.getCurrentPosition(
      p => res({ type: 'gps', lat: p.coords.latitude, lng: p.coords.longitude }),
      rej, { timeout: 15000, maximumAge: 3600e3 },
    );
  });
}

export async function useGps() {
  toast('جارٍ تحديد موقعك…');
  try {
    cfg.loc = await locate();
    saveCfg();
    times.today = null;
    await loadTimes(true);
    toast('تم تحديث المواقيت');
  } catch {
    toast('تعذّر تحديد الموقع، أدخل مدينتك يدويًا');
  }
}

/* ── القبلة ── */
export function qibla(lat, lng) {
  const r = Math.PI / 180;
  const φ1 = lat * r, φ2 = KAABA.lat * r, Δλ = (KAABA.lng - lng) * r;
  const y = Math.sin(Δλ);
  const x = Math.cos(φ1) * Math.tan(φ2) - Math.sin(φ1) * Math.cos(Δλ);
  const bearing = (Math.atan2(y, x) / r + 360) % 360;
  const a = Math.sin((φ2 - φ1) / 2) ** 2 + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) ** 2;
  const km = 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return { bearing, km };
}

/* ── رفع الأذان عند دخول الوقت ── */
let lastFired = store.get('lastFired', '');
let lastPre = '';
function checkAdhan() {
  if (!times.today) return;
  const now = new Date();
  if (times.day !== dateKey(now)) { if (!times.loading) loadTimes(true); return; }
  // التذكير باقتراب الأذان داخل الصفحة (في أندرويد يصل بإشعار مجدول ولو كان التطبيق مغلقًا)
  const rc = remCfg();
  if (!isNative && rc.pre) {
    for (const p of PRAYERS) {
      if (p.noAdhan) continue;
      const left = toDate(times.today.timings[p.key]) - now;
      const id = dateKey(now) + p.key;
      if (left <= rc.preMin * 60e3 && left > (rc.preMin - 1) * 60e3 && lastPre !== id) {
        lastPre = id;
        toast(`اقترب أذان ${p.name} (بعد ${minLabel(rc.preMin)})، فتهيّأ للصلاة`);
      }
    }
  }
  for (const p of PRAYERS) {
    if (p.noAdhan || !cfg.on[p.key]) continue;
    const diff = now - toDate(times.today.timings[p.key]);
    const id = dateKey(now) + p.key;
    if (diff >= 0 && diff < 60e3 && lastFired !== id) {
      lastFired = id; store.set('lastFired', id);
      fireAdhan(p);
    }
  }
}

const AFTER_ADHAN = 'اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ';

export function fireAdhan(p) {
  const a = $('#adhanAudio');
  if (!audio.paused) audio.pause();
  // في التطبيق يُرفع الأذان بصوت الإشعار المجدول، فلا نشغّله مرتين
  if (!isNative) {
    a.src = adhanUrl(cfg.sound);
    a.play().catch(() => {});
  }
  if (!isNative && cfg.alerts && 'Notification' in window && Notification.permission === 'granted') {
    const opts = { body: 'حيّ على الصلاة، حيّ على الفلاح', icon: 'icons/icon-192.png', tag: 'adhan', lang: 'ar', dir: 'rtl' };
    navigator.serviceWorker?.ready.then(r => r.showNotification(`حان الآن موعد صلاة ${p.name}`, opts))
      .catch(() => { try { new Notification(`حان الآن موعد صلاة ${p.name}`, opts); } catch { /* غير مدعوم */ } });
  }
  document.querySelector('.adhan-alert')?.remove();
  const box = document.createElement('div');
  box.className = 'adhan-alert';
  box.innerHTML = `<div class="card">
    <div class="big-ic">${icons.mosque}</div>
    <h2>صلاة ${p.name}</h2>
    <p class="muted">حان الآن موعد الصلاة · ${fmt12(times.today.timings[p.key])}</p>
    <div class="adhan-sunnah" id="adhanSunnah">
      <b>من السنة عند سماع الأذان</b>
      <p>أن تقول مثل ما يقول المؤذن، إلا في «حيّ على الصلاة» و«حيّ على الفلاح» فتقول: لا حول ولا قوة إلا بالله.</p>
      <small>متفق عليه، والاستثناء عند مسلم</small>
    </div>
    <button class="btn" style="width:100%">إيقاف الأذان</button></div>`;
  const showDua = () => {
    $('#adhanSunnah', box).innerHTML = `<b>بعد الأذان</b><p>صلِّ على النبي ﷺ، ثم قل:</p><p class="dua">${AFTER_ADHAN}</p><small>رواه البخاري — «حلّت له شفاعتي يوم القيامة»</small>`;
    $('.btn', box).textContent = 'إغلاق';
  };
  let stopped = false;
  $('.btn', box).onclick = () => {
    if (!stopped) {
      stopped = true;
      a.pause();
      Notifications()?.removeAllDeliveredNotifications().catch(() => {});
      nativePlugin('NurAdhan')?.stop().catch(() => {});
      showDua();
    } else box.remove();
  };
  a.onended = () => { stopped = true; showDua(); };
  document.body.append(box);
}

setInterval(() => {
  checkAdhan();
  const np = nextPrayer();
  if (!np) return;
  const txt = countdownText(np.at - new Date());
  document.querySelectorAll('[data-countdown]').forEach(el => { el.textContent = txt; });
}, 1000);

/* ── أندرويد: جدولة الأذان إشعاراتٍ بصوت المؤذن، فيُرفع والتطبيق مغلق ── */
let scheduling = null;
export function scheduleAdhans() {
  if (!isNative || !cfg.loc) return Promise.resolve();
  scheduling = (scheduling || Promise.resolve()).then(doSchedule).catch(() => {});
  return scheduling;
}
async function doSchedule() {
  const NA = nativePlugin('NurAdhan');
  const LN = Notifications();
  // إلغاء إشعارات الأذان القديمة (من الإصدارات السابقة) حتى لا يتكرر الأذان
  if (LN) {
    try {
      const old = await LN.getPending();
      if (old.notifications.length) await LN.cancel({ notifications: old.notifications.map(n => ({ id: n.id })) });
    } catch { /* لا شيء */ }
  }
  if (!NA) return;
  const now = new Date();
  // مواقيت ٣٠ يومًا مقدّمًا (من الشهر المحفوظ)، فيستمر الأذان ولو لم يُفتح التطبيق مدة طويلة
  const days = [];
  for (let i = 0; i < 30; i++) {
    const d = new Date(now); d.setDate(now.getDate() + i);
    try { days.push({ date: d, day: await fetchDay(d) }); } catch { /* شهر غير محفوظ بعد */ }
  }
  const items = [], all = [];
  for (const { date: d, day } of days) {
    PRAYERS.forEach((p, idx) => {
      if (p.noAdhan) return;
      const at = toDate(day.timings[p.key], d);
      if (at <= now) return;
      all.push({ at: at.getTime(), name: p.name, key: p.key });
      if (cfg.on[p.key]) items.push({ id: (d.getMonth() + 1) * 100000 + d.getDate() * 1000 + idx, at: at.getTime(), name: p.name, key: p.key });
    });
  }
  // ويدجت الشاشة الرئيسية: كل المواقيت، ولو كان الأذان متوقفًا
  NA.widget?.({ items: all, city: placeName() }).catch(() => {});
  // التذكيرات تُحسب من المواقيت نفسها
  scheduleReminders(days).catch(() => {});
  if (!cfg.alerts) { await NA.cancelAll(); events.dispatchEvent(new CustomEvent('scheduled', { detail: {} })); return; }
  const r = await NA.schedule({ items, sound: BUNDLED_ADHANS.includes(cfg.sound) ? cfg.sound : '008' });
  events.dispatchEvent(new CustomEvent('scheduled', { detail: r }));
}

// حالة الأذان في الجهاز: المنبّه الدقيق، والبطارية، والإشعارات، والأذان القادم
export async function nativeAdhanStatus() {
  const NA = nativePlugin('NurAdhan');
  if (!NA) return null;
  try { return await NA.status(); } catch { return null; }
}
export function testAdhan(seconds = 10) {
  return nativePlugin('NurAdhan')?.test({ seconds, sound: BUNDLED_ADHANS.includes(cfg.sound) ? cfg.sound : '008', name: 'تجربة الأذان' });
}
export const openExactSettings = () => nativePlugin('NurAdhan')?.openExactSettings();
export const openFullScreenSettings = () => nativePlugin('NurAdhan')?.openFullScreenSettings();
export const openAutostart = () => nativePlugin('NurAdhan')?.openAutostart();
export const openLockScreenSettings = () => nativePlugin('NurAdhan')?.openLockScreenSettings();
export const requestIgnoreBattery = () => nativePlugin('NurAdhan')?.requestIgnoreBattery();

export async function enableNativeAdhan() {
  // إذن الإشعارات لإظهار إشعار الأذان وزر الإيقاف (الأذان يُرفع حتى بدونه)
  const LN = Notifications();
  if (LN) {
    try {
      let perm = await LN.checkPermissions();
      if (perm.display !== 'granted') perm = await LN.requestPermissions();
      if (perm.display !== 'granted') toast('لم يُسمح بالإشعارات؛ سيُرفع الأذان دون إشعار');
    } catch { /* لا شيء */ }
  }
  const st = await nativeAdhanStatus();
  // نفتح إعداد المنبّهات الدقيقة تلقائيًا مرة واحدة فقط، ويبقى رابطه في صفحة المواقيت
  if (st && !st.exact && !store.get('exactAsked')) {
    store.set('exactAsked', true);
    toast('اسمح لـ«نور» بالمنبّهات الدقيقة ليُرفع الأذان في وقته');
    await openExactSettings();
  } else if (st && st.fullScreen === false && !store.get('fsAsked')) {
    // شاشة الأذان الكاملة فوق القفل تحتاج إذنًا في أندرويد ١٤ فما بعد
    store.set('fsAsked', true);
    toast('اسمح لـ«نور» بإظهار شاشة الأذان كاملة فوق القفل');
    await openFullScreenSettings();
  }
  await scheduleAdhans();
  return true;
}

// في التطبيق يكون الأذان مفعّلًا افتراضيًا، ونطلب الإذن مرة واحدة بعد تحديد الموقع
if (isNative && store.get('adhan', {}).alerts === undefined) cfg.alerts = true;
export async function initNativeAdhan() {
  const LN = Notifications();
  if (!LN || !cfg.loc || !cfg.alerts) return scheduleAdhans();
  const perm = await LN.checkPermissions().catch(() => ({}));
  if (perm.display === 'prompt' || perm.display === 'prompt-with-rationale') return enableNativeAdhan();
  return scheduleAdhans();
}

if (isNative) {
  events.addEventListener('update', () => { if (times.today) initNativeAdhan(); });
  // الضغط على إشعار الأذان يفتح صفحة المواقيت
  Notifications()?.addListener('localNotificationActionPerformed', () => { location.hash = '#/adhan'; });
}
