// التذكيرات بإشعارات تعمل والتطبيق مغلق (أندرويد): تُحسب أوقاتها من مواقيت الصلاة لثلاثين يومًا.
import { store, arNum } from './core.js';
import { nativePlugin } from './native.js';
import { getKhatma, wirdToday } from './khatma.js';

export const REMINDERS = [
  { key: 'morning', name: 'أذكار الصباح', hint: 'بعد الفجر بعشرين دقيقة' },
  { key: 'evening', name: 'أذكار المساء', hint: 'بعد العصر بعشرين دقيقة' },
  { key: 'kahf', name: 'سورة الكهف يوم الجمعة', hint: 'صباح الجمعة قبل الظهر بساعة ونصف' },
  { key: 'fast', name: 'صيام الاثنين والخميس', hint: 'ليلة الأحد والأربعاء بعد العشاء' },
  { key: 'white', name: 'صيام الأيام البيض', hint: 'ليالي ١٢ و١٣ و١٤ من الشهر الهجري بعد العشاء' },
  { key: 'wird', name: 'الورد اليومي من الختمة', hint: 'في الوقت الذي تختاره، ما دامت لك ختمة' },
];
const DEFAULTS = { morning: true, evening: true, kahf: true, fast: true, white: true, wird: true, wirdTime: '20:00' };
export const remCfg = () => ({ ...DEFAULTS, ...store.get('reminders', {}) });
export const setRem = o => store.set('reminders', { ...remCfg(), ...o });

// "05:17 (CET)" → تاريخ في يوم d
function at(hhmm, d, addMin = 0) {
  const [h, m] = String(hhmm).match(/\d+/g).map(Number);
  const x = new Date(d); x.setHours(h, m + addMin, 0, 0);
  return x;
}

export async function scheduleReminders(days) {
  const NA = nativePlugin('NurAdhan');
  if (!NA?.reminders) return;
  const c = remCfg();
  const now = new Date();
  const items = [];
  const add = (type, when, title, text, route) => {
    if (when <= now) return;
    items.push({ id: ((when.getMonth() + 1) * 100 + when.getDate()) * 10 + type, at: when.getTime(), title, text, route });
  };
  const k = getKhatma();
  const w = wirdToday(k);
  for (const { date: d, day } of days) {
    const t = day.timings;
    const dow = d.getDay();
    if (c.morning) add(1, at(t.Fajr, d, 20), 'أذكار الصباح', 'حان وقت أذكار الصباح، فاجعل أول يومك ذكرًا لله.', '#/adhkar/morning');
    if (c.evening) add(2, at(t.Asr, d, 20), 'أذكار المساء', 'حان وقت أذكار المساء.', '#/adhkar/evening');
    if (c.kahf && dow === 5) add(3, at(t.Dhuhr, d, -90), 'يوم الجمعة: سورة الكهف', '«من قرأ سورة الكهف يوم الجمعة أضاء له من النور ما بين الجمعتين» — صححه الألباني. وأكثروا من الصلاة على النبي ﷺ.', '#/mushaf/18');
    if (c.fast && (dow === 0 || dow === 3)) add(4, at(t.Isha, d, 30), `غدًا يوم ${dow === 0 ? 'الاثنين' : 'الخميس'}`, '«تُعرض الأعمال يوم الاثنين والخميس، فأحب أن يُعرض عملي وأنا صائم» — صححه الألباني. لا تنسَ السحور.', '#/');
    const hd = day.hijri?.day;
    if (c.white && hd >= 12 && hd <= 14) add(5, at(t.Isha, d, 35), 'غدًا من الأيام البيض', `غدًا اليوم ${arNum(hd + 1)} من ${day.hijri.month}. صيام ثلاثة أيام من كل شهر صيام الدهر كله (متفق عليه).`, '#/');
    if (c.wird && k && !w?.finished) {
      const isToday = d.toDateString() === now.toDateString();
      if (!(isToday && w?.doneToday)) add(6, at(c.wirdTime, d), 'وردك من القرآن', isToday && w ? `وردك اليوم من صفحة ${arNum(w.from)} إلى ${arNum(w.to)}.` : 'حان وقت وردك اليومي من القرآن.', '#/khatma');
    }
  }
  await NA.reminders({ items });
}
