// الختمة والورد اليومي: خطة بعدد الأيام، ووِرد كل يوم بالصفحات، ومتابعة الموضع من المصحف.
import { store } from './core.js';

export const PAGES = 604;
export const PLANS = [7, 10, 15, 20, 30, 60];

const DAY = 864e5;
const dayStart = d => { const x = new Date(d); x.setHours(0, 0, 0, 0); return x; };
const ymd = d => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

export const getKhatma = () => store.get('khatma', null);
const save = k => { store.set('khatma', k); window.dispatchEvent(new Event('khatma')); };

export function startKhatma(days, from = 1) {
  const old = getKhatma();
  save({ start: ymd(new Date()), days, pos: from, from, done: old?.done || 0 });
}
export function stopKhatma() {
  const old = getKhatma();
  store.set('khatma', null);
  if (old?.done) store.set('khatmaDone', old.done);
  window.dispatchEvent(new Event('khatma'));
}
export const completedCount = () => getKhatma()?.done ?? store.get('khatmaDone', 0);

// حالة اليوم: رقم اليوم، وحدود الورد، والتقدم، والتأخر أو التقدم على الخطة
export function wirdToday(k = getKhatma(), now = new Date()) {
  if (!k) return null;
  const start = dayStart(new Date(k.start + 'T00:00:00'));
  const from0 = k.from || 1;
  const total = PAGES - from0 + 1;
  const per = total / k.days;
  const day = Math.max(0, Math.min(k.days - 1, Math.round((dayStart(now) - start) / DAY)));
  const targetEnd = Math.min(PAGES, from0 - 1 + Math.round((day + 1) * per));
  const doneToday = k.pos > targetEnd;
  const finished = k.pos > PAGES;
  let from = Math.min(k.pos, PAGES), to = targetEnd;
  // أنهى ورد اليوم: نعرض ورد الغد لمن أراد الزيادة
  if (doneToday && !finished) to = Math.min(PAGES, from0 - 1 + Math.round((day + 2) * per));
  if (to < from) to = from;
  return {
    day: day + 1, days: k.days, from, to, pos: k.pos, per: Math.round(per),
    doneToday, finished,
    // الصفحات المتأخرة عن الخطة قبل ورد اليوم
    behind: finished ? 0 : Math.max(0, from0 + Math.round(day * per) - k.pos),
    progress: Math.min(1, (k.pos - from0) / total),
    remaining: Math.max(0, targetEnd - k.pos + 1),
  };
}

// قراءة صفحة في المصحف: تُحتسب إن كانت هي الموضع التالي في الختمة
export function markPageRead(p) {
  const k = getKhatma();
  if (!k || p !== k.pos) return false;
  k.pos = p + 1;
  if (k.pos > PAGES) k.done = (k.done || 0) + 1;
  save(k);
  return true;
}
export function setPos(p) {
  const k = getKhatma();
  if (!k) return;
  k.pos = Math.max(1, Math.min(PAGES + 1, p));
  save(k);
}
// «أتممت ورد اليوم»
export function finishToday() {
  const w = wirdToday();
  if (!w) return;
  const k = getKhatma();
  const before = k.pos;
  k.pos = Math.max(k.pos, w.to + 1);
  if (before <= PAGES && k.pos > PAGES) k.done = (k.done || 0) + 1;
  save(k);
}

/**
 * ربط قارئ المصحف بالختمة: تُحتسب الصفحة إذا بقي عليها القارئ ١٥ ثانية،
 * أو انتقل منها إلى التالية. تُعيد دالة تُستدعى عند الانتقال إلى الأمام.
 */
export function trackPage(p, ctx) {
  const t = setTimeout(() => markPageRead(p), 15000);
  ctx.cleanup(() => clearTimeout(t));
  return () => { clearTimeout(t); markPageRead(p); };
}
