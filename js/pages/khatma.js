// صفحة الختمة والورد اليومي، وصفحة التذكيرات
import { $, $$, store, arNum, esc, toast, icons } from '../core.js';
import { surahName } from '../player.js';
import { PLANS, PAGES, getKhatma, startKhatma, stopKhatma, wirdToday, setPos, finishToday, completedCount } from '../khatma.js';
import { REMINDERS, PRE_MINUTES, remCfg, setRem, minLabel } from '../reminders.js';
import { scheduleAdhans } from '../prayer.js';
import { isNative } from '../native.js';
import { getPages } from './quran.js';
import { confirmSheet } from './downloads.js';

const mushafBase = () => (store.get('mushafType', 'hafs') === 'warsh' ? '#/warsh/' : '#/page/');

// اسم السورة في صفحة من مصحف المدينة
async function pageSurah(p) {
  try { const pages = await getPages(); return surahName(pages[p - 1][0][0][0]); } catch { return ''; }
}

export async function wirdCardHTML() {
  const w = wirdToday();
  if (!w) return '';
  const s = await pageSurah(w.from);
  const title = w.finished ? 'أتممت الختمة، تقبّل الله منك' : w.doneToday ? 'أتممت ورد اليوم، بارك الله فيك' : `ورد اليوم: من صفحة ${arNum(w.from)} إلى ${arNum(w.to)}`;
  const sub = w.finished ? 'ابدأ ختمة جديدة من صفحة الختمة' : w.doneToday ? `يمكنك المتابعة: من صفحة ${arNum(w.from)}` : `${s ? 'سورة ' + s + ' · ' : ''}اليوم ${arNum(w.day)} من ${arNum(w.days)}${w.behind ? ` · متأخر ${arNum(w.behind)} صفحة` : ''}`;
  return `<a class="card wird-card" href="#/khatma">
    <span class="ring" style="--p:${Math.round(w.progress * 100)}"><b>${arNum(Math.round(w.progress * 100))}٪</b></span>
    <span class="meta"><b>${title}</b><small>${sub}</small></span>
    <span class="chev">${icons.chevron}</span></a>`;
}

export async function renderKhatma(view, args, ctx) {
  ctx.back('history');
  const draw = async () => {
    const k = getKhatma();
    const w = wirdToday(k);
    const done = completedCount();
    if (!k) {
      let pick = 30;
      view.innerHTML = `
        <div class="card khatma-intro">
          <span class="tile-ic gold">${icons.target}</span>
          <h2>ختمة القرآن والورد اليومي</h2>
          <p class="muted">اختر في كم يومًا تريد أن تختم، فيحسب التطبيق وردك كل يوم بالصفحات، ويتابع تقدّمك من المصحف تلقائيًا${isNative ? '، ويذكّرك بوردك في الوقت الذي تختاره' : ''}.</p>
          ${done ? `<p class="done-count">${icons.check} أتممت ${arNum(done)} ${done === 1 ? 'ختمة' : 'ختمات'}</p>` : ''}
        </div>
        <div class="section-head"><h2>مدة الختمة</h2></div>
        <div class="plan-grid" id="plans">${PLANS.map(d => `<button data-d="${d}" class="${d === pick ? 'active' : ''}"><b>${arNum(d)}</b><span>يومًا</span><small>${arNum(Math.ceil(PAGES / d))} صفحة يوميًا</small></button>`).join('')}</div>
        <div class="card settings">
          <div class="field"><label for="customDays">أو عدد أيام آخر</label><input class="input" id="customDays" type="text" inputmode="numeric" placeholder="مثلًا ٩٠"></div>
          <div class="field"><label for="fromPage">البدء من صفحة</label><input class="input" id="fromPage" type="text" inputmode="numeric" value="1"></div>
        </div>
        <button class="btn block" id="startK">ابدأ الختمة</button>`;
      $('#plans').onclick = e => {
        const b = e.target.closest('[data-d]');
        if (!b) return;
        pick = +b.dataset.d;
        $('#customDays').value = '';
        $$('#plans button').forEach(x => x.classList.toggle('active', x === b));
      };
      const num = v => parseInt(String(v).replace(/[٠-٩]/g, d => '٠١٢٣٤٥٦٧٨٩'.indexOf(d)), 10);
      $('#startK').onclick = () => {
        const custom = num($('#customDays').value);
        const days = custom > 0 ? Math.min(365, custom) : pick;
        const from = Math.min(PAGES, Math.max(1, num($('#fromPage').value) || 1));
        startKhatma(days, from);
        scheduleAdhans();
        toast('بدأت الختمة، أعانك الله');
        draw();
      };
      return;
    }
    const s = await pageSurah(w.from);
    if (!ctx.alive()) return;
    const c = remCfg();
    view.innerHTML = `
      <div class="card khatma-head">
        <span class="ring big" style="--p:${Math.round(w.progress * 100)}"><b>${arNum(Math.round(w.progress * 100))}٪</b><small>من الختمة</small></span>
        <div>
          <strong>اليوم ${arNum(w.day)} من ${arNum(w.days)}</strong>
          <small class="muted">ختمة في ${arNum(w.days)} يومًا · نحو ${arNum(w.per)} صفحة يوميًا</small>
          <small class="muted">الموضع الحالي: صفحة ${arNum(Math.min(w.pos, PAGES))}${done ? ` · أتممت ${arNum(done)} ${done === 1 ? 'ختمة' : 'ختمات'}` : ''}</small>
        </div>
      </div>
      ${w.finished ? `<div class="card wird-today done"><b>ما شاء الله، أتممت الختمة</b><p class="muted">تقبّل الله منك. يمكنك أن تبدأ ختمة جديدة.</p><button class="btn block" id="newK">ختمة جديدة</button></div>` : `
      <div class="card wird-today ${w.doneToday ? 'done' : ''}">
        <small class="muted">${w.doneToday ? 'أتممت ورد اليوم، وهذا ورد الغد لمن أراد' : 'ورد اليوم'}</small>
        <b>من صفحة ${arNum(w.from)} إلى صفحة ${arNum(w.to)}</b>
        <small>${s ? 'تبدأ من سورة ' + esc(s) + ' · ' : ''}${arNum(w.to - w.from + 1)} صفحة</small>
        ${w.behind ? `<p class="behind">${icons.info} متأخر عن الخطة ${arNum(w.behind)} صفحة، وأُضيفت إلى ورد اليوم.</p>` : ''}
        <div class="bar"><i style="width:${Math.round(w.progress * 100)}%"></i></div>
        <div class="row2">
          <a class="btn" href="${mushafBase()}${w.from}">${icons.book} اقرأ الورد</a>
          ${w.doneToday ? '' : `<button class="btn ghost" id="finishT">${icons.check} أتممت ورد اليوم</button>`}
        </div>
        <small class="muted tip">تُحتسب الصفحة إذا قلّبتها إلى التالية أو بقيت عليها ربع دقيقة.</small>
      </div>`}
      ${isNative ? `<div class="section-head"><h2>تذكير الورد</h2></div>
      <div class="card settings">
        <label class="switch-row"><span>ذكّرني بالورد كل يوم<small>إشعار في الوقت الذي تختاره</small></span><span class="switch"><input type="checkbox" id="wirdOn" ${c.wird ? 'checked' : ''}><i></i></span></label>
        <div class="field"><label for="wirdTime">وقت التذكير</label><input class="input" id="wirdTime" type="time" value="${c.wirdTime}"></div>
      </div>` : ''}
      <div class="section-head"><h2>إعدادات</h2></div>
      <div class="list-card">
        <button class="row" id="setPos"><span class="tile-ic sm">${icons.layers}</span><span class="meta"><strong>تعديل الموضع</strong><small>إن قرأت من مصحف ورقي</small></span><span class="chev">${icons.chevron}</span></button>
        <button class="row" id="stopK"><span class="tile-ic sm gold">${icons.close}</span><span class="meta"><strong>${w.finished ? 'إنهاء' : 'إيقاف الختمة'}</strong><small>يمكنك بدء ختمة جديدة بعدها</small></span><span class="chev">${icons.chevron}</span></button>
      </div>`;
    $('#finishT')?.addEventListener('click', () => { finishToday(); scheduleAdhans(); toast('تقبّل الله منك'); draw(); });
    $('#newK')?.addEventListener('click', () => { stopKhatma(); draw(); });
    $('#stopK').onclick = () => confirmSheet('إيقاف الختمة؟', 'سيُحذف موضعك الحالي وخطة الختمة، ويبقى عدد الختمات التي أتممتها.', 'إيقاف', () => { stopKhatma(); scheduleAdhans(); draw(); });
    $('#setPos').onclick = () => {
      const v = prompt('رقم الصفحة التي ستقرأ منها (١–٦٠٤)', String(Math.min(w.pos, PAGES)));
      const n = parseInt(String(v || '').replace(/[٠-٩]/g, d => '٠١٢٣٤٥٦٧٨٩'.indexOf(d)), 10);
      if (n >= 1 && n <= PAGES) { setPos(n); scheduleAdhans(); draw(); }
    };
    const onRem = () => { setRem({ wird: $('#wirdOn').checked, wirdTime: $('#wirdTime').value || '20:00' }); scheduleAdhans(); toast('حُفظ التذكير'); };
    $('#wirdOn')?.addEventListener('change', onRem);
    $('#wirdTime')?.addEventListener('change', onRem);
  };
  await draw();
}

export function renderReminders(view, args, ctx) {
  ctx.back('history');
  const c = remCfg();
  view.innerHTML = `
    <div class="note-box">${icons.info} ${isNative ? 'تصلك هذه التذكيرات بإشعار في وقتها حتى والتطبيق مغلق، وتُحسب من مواقيت الصلاة في مدينتك.' : 'التذكيرات بالإشعارات تعمل في تطبيق أندرويد. وفي المتصفح تظهر التذكيرات في الصفحة الرئيسية.'}</div>
    <div class="card settings" id="rems">
      ${REMINDERS.map(r => `<label class="switch-row"><span>${r.name}<small>${r.hint}</small></span><span class="switch"><input type="checkbox" data-k="${r.key}" ${c[r.key] ? 'checked' : ''}><i></i></span></label>`).join('')}
      <div class="field"><label for="preMin">التذكير قبل الأذان بـ</label><select class="select" id="preMin">${PRE_MINUTES.map(n => `<option value="${n}" ${n === c.preMin ? 'selected' : ''}>${minLabel(n)}</option>`).join('')}</select></div>
      <div class="field"><label for="wirdTime">وقت تذكير الورد</label><input class="input" id="wirdTime" type="time" value="${c.wirdTime}"></div>
    </div>
    <a class="btn ghost block" href="#/khatma">${icons.target} الختمة والورد اليومي</a>`;
  const save = () => {
    const o = { wirdTime: $('#wirdTime').value || '20:00', preMin: +$('#preMin').value };
    $$('#rems [data-k]').forEach(i => { o[i.dataset.k] = i.checked; });
    setRem(o);
    scheduleAdhans();
  };
  $('#rems').addEventListener('change', () => { save(); toast('حُفظت التذكيرات'); });
}
