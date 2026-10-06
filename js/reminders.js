// التذكيرات بإشعارات تعمل والتطبيق مغلق (أندرويد): تُحسب أوقاتها من مواقيت الصلاة لثلاثين يومًا.
import { store, arNum } from './core.js';
import { nativePlugin } from './native.js';
import { getKhatma, wirdToday } from './khatma.js';
import { toHijri } from './hijri.js';

export const PRE_MINUTES = [5, 10, 15, 20, 30];
export const REMINDERS = [
  { key: 'pre', name: 'تذكير باقتراب الأذان', hint: 'قبل كل صلاة بالمدة التي تختارها، لتتهيأ للصلاة' },
  { key: 'morning', name: 'أذكار الصباح', hint: 'بعد الفجر بعشرين دقيقة' },
  { key: 'evening', name: 'أذكار المساء', hint: 'بعد العصر بعشرين دقيقة' },
  { key: 'kahf', name: 'سورة الكهف يوم الجمعة', hint: 'صباح الجمعة قبل الظهر بساعة ونصف' },
  { key: 'fast', name: 'صيام الاثنين والخميس', hint: 'ليلة الأحد والأربعاء بعد العشاء' },
  { key: 'white', name: 'صيام الأيام البيض', hint: 'ليالي ١٢ و١٣ و١٤ من الشهر الهجري بعد العشاء' },
  { key: 'seasons', name: 'المواسم', hint: 'قبل عاشوراء وعرفة، وأول عشر ذي الحجة، والعشر الأواخر، وست شوّال، واقتراب رمضان' },
  { key: 'wird', name: 'الورد اليومي من الختمة', hint: 'في الوقت الذي تختاره، ما دامت لك ختمة' },
];
const DEFAULTS = { pre: true, preMin: 10, morning: true, evening: true, kahf: true, fast: true, white: true, seasons: true, wird: true, wirdTime: '20:00' };
export const remCfg = () => ({ ...DEFAULTS, ...store.get('reminders', {}) });
export const setRem = o => store.set('reminders', { ...remCfg(), ...o });

// التذكير باقتراب الأذان: الصلوات الخمس، مع تذكير بسنّة من سنن الاستعداد للصلاة
const PRE_PRAYERS = [['Fajr', 'الفجر'], ['Dhuhr', 'الظهر'], ['Asr', 'العصر'], ['Maghrib', 'المغرب'], ['Isha', 'العشاء']];
const PRE_TEXT = [
  'تهيّأ للصلاة: «من توضأ فأحسن الوضوء خرجت خطاياه من جسده حتى تخرج من تحت أظفاره» (رواه مسلم).',
  'تهيّأ للصلاة: «لو يعلم الناس ما في النداء والصف الأول ثم لم يجدوا إلا أن يستهموا عليه لاستهموا» (متفق عليه).',
  'تهيّأ للصلاة: «من غدا إلى المسجد أو راح أعدّ الله له في الجنة نُزُلًا كلما غدا أو راح» (متفق عليه).',
  'تهيّأ للصلاة: «لا يزال أحدكم في صلاة ما دامت الصلاة تحبسه» (متفق عليه).',
  'تهيّأ للصلاة: «الدعاء لا يُردّ بين الأذان والإقامة» (رواه أبو داود والترمذي، وصححه الألباني).',
];
const PRE_JUMUA = '«من اغتسل يوم الجمعة ثم راح فكأنما قرّب بدنة…» (متفق عليه). واستمع للخطبة وأنصت.';
export const minLabel = n => n === 10 ? '١٠ دقائق' : n <= 10 ? `${arNum(n)} دقائق` : `${arNum(n)} دقيقة`;

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
    if (c.pre) {
      PRE_PRAYERS.forEach(([key, name], idx) => {
        const when = at(t[key], d, -c.preMin);
        if (when <= now) return;
        const jumua = key === 'Dhuhr' && dow === 5;
        items.push({
          id: 100000 + ((when.getMonth() + 1) * 100 + when.getDate()) * 10 + idx, at: when.getTime(),
          title: jumua ? `اقتربت صلاة الجمعة (بعد ${minLabel(c.preMin)})` : `اقترب أذان ${name} (بعد ${minLabel(c.preMin)})`,
          text: jumua ? PRE_JUMUA : PRE_TEXT[idx % PRE_TEXT.length],
          route: '#/adhan',
        });
      });
    }
    if (c.morning) add(1, at(t.Fajr, d, 20), 'أذكار الصباح', 'حان وقت أذكار الصباح، فاجعل أول يومك ذكرًا لله.', '#/adhkar/morning');
    if (c.evening) add(2, at(t.Asr, d, 20), 'أذكار المساء', 'حان وقت أذكار المساء.', '#/adhkar/evening');
    if (c.kahf && dow === 5) add(3, at(t.Dhuhr, d, -90), 'يوم الجمعة: سورة الكهف', '«من قرأ سورة الكهف يوم الجمعة أضاء له من النور ما بين الجمعتين» — صححه الألباني. وأكثروا من الصلاة على النبي ﷺ.', '#/mushaf/18');
    if (c.fast && (dow === 0 || dow === 3)) add(4, at(t.Isha, d, 30), `غدًا يوم ${dow === 0 ? 'الاثنين' : 'الخميس'}`, '«تُعرض الأعمال يوم الاثنين والخميس، فأحب أن يُعرض عملي وأنا صائم» — صححه الألباني. لا تنسَ السحور.', '#/');
    // التاريخ الهجري لليوم التالي (بتقويم أم القرى مع تعديل المستخدم، كما في صفحة التقويم)
    const tm = new Date(d); tm.setDate(d.getDate() + 1);
    const nh = toHijri(tm), h = toHijri(d);
    if (c.white && nh.d >= 13 && nh.d <= 15) add(5, at(t.Isha, d, 35), 'غدًا من الأيام البيض', `غدًا اليوم ${arNum(nh.d)} من الشهر الهجري. صيام ثلاثة أيام من كل شهر صيام الدهر كله (متفق عليه).`, '#/calendar');
    if (c.seasons) {
      const eve = (title, text) => add(7, at(t.Isha, d, 40), title, text, '#/calendar');
      const morn = (title, text) => add(8, at(t.Fajr, d, 30), title, text, '#/calendar');
      if (nh.m === 1 && nh.d === 9) eve('غدًا تاسوعاء وبعده عاشوراء', 'صيام عاشوراء يكفّر السنة التي قبله، ويُستحب صيام التاسع معه (رواه مسلم).');
      if (nh.m === 12 && nh.d === 9) eve('غدًا يوم عرفة', 'صيامه يكفّر السنة الماضية والباقية لغير الحاج (رواه مسلم). وأكثر من الدعاء والتهليل.');
      if (h.m === 12 && h.d === 1) morn('بدأت عشر ذي الحجة', '«ما من أيامٍ العملُ الصالح فيهن أحبّ إلى الله من هذه الأيام العشر» (رواه البخاري). أكثروا من التكبير.');
      if (nh.m === 9 && nh.d === 21) eve('الليلة أول العشر الأواخر', '«تحرّوا ليلة القدر في الوتر من العشر الأواخر من رمضان» (رواه البخاري).');
      if (h.m === 10 && h.d === 2) morn('ست من شوّال', '«من صام رمضان ثم أتبعه ستًّا من شوّال كان كصيام الدهر» (رواه مسلم).');
      if (h.m === 8 && h.d === 25) morn('اقترب رمضان', 'بقي على رمضان أيام قليلة؛ فاستعد له بالتوبة وقضاء ما عليك من صيام.');
    }
    if (c.wird && k && !w?.finished) {
      const isToday = d.toDateString() === now.toDateString();
      if (!(isToday && w?.doneToday)) add(6, at(c.wirdTime, d), 'وردك من القرآن', isToday && w ? `وردك اليوم من صفحة ${arNum(w.from)} إلى ${arNum(w.to)}.` : 'حان وقت وردك اليومي من القرآن.', '#/khatma');
    }
  }
  await NA.reminders({ items });
}
