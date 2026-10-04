// التاريخ الهجري (تقويم أم القرى في المتصفح نفسه) والمواسم المشروعة، مع تعديل يدوي لرؤية الهلال في بلدك.
import { store } from './core.js';

export const HMONTHS = ['محرّم', 'صفر', 'ربيع الأول', 'ربيع الآخر', 'جمادى الأولى', 'جمادى الآخرة', 'رجب', 'شعبان', 'رمضان', 'شوّال', 'ذو القعدة', 'ذو الحجة'];
const fmt = new Intl.DateTimeFormat('en-u-ca-islamic-umalqura-nu-latn', { day: 'numeric', month: 'numeric', year: 'numeric' });
const DAY = 864e5;

export const hijriAdj = () => store.get('hijriAdj', 0);
export function toHijri(date) {
  const d = new Date(date); d.setHours(12, 0, 0, 0);
  d.setTime(d.getTime() + hijriAdj() * DAY);
  const p = Object.fromEntries(fmt.formatToParts(d).map(x => [x.type, x.value]));
  return { y: +p.year, m: +p.month, d: +p.day };
}

// أول يوم ميلادي من الشهر الهجري (y, m)
export function monthStart(y, m) {
  const now = new Date(); now.setHours(12, 0, 0, 0);
  const h = toHijri(now);
  const est = new Date(now.getTime() + Math.round((y - h.y) * 354.367 + (m - h.m) * 29.53 + (1 - h.d)) * DAY);
  for (let k = -4; k <= 4; k++) {
    const t = new Date(est.getTime() + k * DAY);
    const x = toHijri(t);
    if (x.y === y && x.m === m && x.d === 1) return t;
  }
  return est;
}
export function monthDays(y, m) {
  const out = [];
  let t = monthStart(y, m);
  for (let i = 0; i < 31; i++) {
    const x = toHijri(t);
    if (x.m !== m) break;
    out.push({ date: new Date(t), h: x });
    t = new Date(t.getTime() + DAY);
  }
  return out;
}

/* المواسم الثابتة في السنة الصحيحة فقط؛ ولا يذكر التقويم المولد ولا الإسراء والمعراج ولا رأس السنة ونحوها */
export const SEASONS = [
  { m: 1, from: 1, to: 29, whole: true, name: 'شهر الله المحرّم', kind: 'virtue', text: '«أفضل الصيام بعد رمضان شهرُ الله المحرّم».', src: 'رواه مسلم' },
  { m: 1, from: 9, to: 10, name: 'تاسوعاء وعاشوراء', kind: 'fast', text: 'صيام يوم عاشوراء يكفّر السنة التي قبله، ويُستحب صيام التاسع معه.', src: 'رواه مسلم' },
  { m: 8, from: 1, to: 29, whole: true, name: 'شعبان', kind: 'virtue', text: 'كان النبي ﷺ يُكثر الصيام في شعبان، وفيه تُرفع الأعمال.', src: 'متفق عليه، والرفع عند النسائي وحسّنه الألباني' },
  { m: 9, from: 1, to: 30, name: 'شهر رمضان', kind: 'ramadan', text: '«من صام رمضان إيمانًا واحتسابًا غُفر له ما تقدّم من ذنبه».', src: 'متفق عليه' },
  { m: 9, from: 21, to: 30, name: 'العشر الأواخر وليلة القدر', kind: 'virtue', text: '«تحرّوا ليلة القدر في الوتر من العشر الأواخر من رمضان».', src: 'رواه البخاري' },
  { m: 10, from: 1, to: 1, name: 'عيد الفطر', kind: 'eid', text: 'يحرم صيام يوم العيد، ويُسنّ التكبير وأكل تمرات قبل الخروج إلى المصلّى.', src: 'متفق عليه، والتمرات عند البخاري' },
  { m: 10, from: 2, to: 29, name: 'ست من شوّال', kind: 'fast', text: '«من صام رمضان ثم أتبعه ستًّا من شوّال كان كصيام الدهر».', src: 'رواه مسلم' },
  { m: 12, from: 1, to: 10, name: 'عشر ذي الحجة', kind: 'virtue', text: '«ما من أيامٍ العملُ الصالح فيهن أحبّ إلى الله من هذه الأيام العشر». فأكثروا فيها من التكبير والتهليل.', src: 'رواه البخاري' },
  { m: 12, from: 9, to: 9, name: 'يوم عرفة', kind: 'fast', text: 'صيامه يكفّر السنة الماضية والسنة الباقية، لغير الحاج.', src: 'رواه مسلم' },
  { m: 12, from: 10, to: 10, name: 'عيد الأضحى', kind: 'eid', text: 'يوم النحر، أعظم الأيام عند الله، ويُشرع فيه ذبح الأضحية بعد صلاة العيد.', src: 'رواه أبو داود، وصححه الألباني' },
  { m: 12, from: 11, to: 13, name: 'أيام التشريق', kind: 'eid', text: '«أيام التشريق أيام أكلٍ وشربٍ وذكرٍ لله»، فلا تُصام.', src: 'رواه مسلم' },
];
export const WHITE = { name: 'الأيام البيض', text: 'صيام ثلاثة أيام من كل شهر صيام الدهر كله، وأفضلها ١٣ و١٤ و١٥.', src: 'متفق عليه، والأيام البيض عند الترمذي والنسائي وحسّنه الألباني' };

// مواسم يوم بعينه (الأيام الخاصة أولًا، ثم الأشهر)
export function seasonsOf(h) {
  return SEASONS.filter(s => s.m === h.m && h.d >= s.from && h.d <= s.to).sort((a, b) => (a.whole ? 1 : 0) - (b.whole ? 1 : 0) || (a.to - a.from) - (b.to - b.from));
}

// المواسم القادمة (مع الجاري الآن) مرتبة بحسب قربها
export function upcoming(now = new Date()) {
  const h = toHijri(now);
  const out = [];
  for (const s of SEASONS) {
    if (s.whole) continue;
    for (const y of [h.y, h.y + 1]) {
      const start = monthStart(y, s.m);
      const begin = new Date(start.getTime() + (s.from - 1) * DAY);
      // آخر الشهر قد يكون ٢٩ يومًا
      const to = s.to >= 29 ? monthDays(y, s.m).length : s.to;
      const end = new Date(start.getTime() + (to - 1) * DAY);
      end.setHours(23, 59, 59);
      if (end < now) continue;
      out.push({ ...s, to, begin, end, y, now: begin <= now });
      break;
    }
  }
  return out.sort((a, b) => a.begin - b.begin);
}
