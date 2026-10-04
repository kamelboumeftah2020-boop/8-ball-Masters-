// التقويم الهجري بالمواسم المشروعة
import { $, store, arNum, esc, icons } from '../core.js';
import { HMONTHS, toHijri, monthDays, seasonsOf, upcoming, WHITE, hijriAdj } from '../hijri.js';
import { scheduleAdhans } from '../prayer.js';

const DAYS = ['السبت', 'الأحد', 'الاثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة'];
const GMONTHS = ['يناير', 'فبراير', 'مارس', 'أبريل', 'مايو', 'يونيو', 'يوليو', 'أغسطس', 'سبتمبر', 'أكتوبر', 'نوفمبر', 'ديسمبر'];
const DAY = 864e5;

function daysLeft(d) {
  const a = new Date(); a.setHours(0, 0, 0, 0);
  const b = new Date(d); b.setHours(0, 0, 0, 0);
  return Math.round((b - a) / DAY);
}
const whenLabel = s => {
  if (s.now) return 'الآن';
  const n = daysLeft(s.begin);
  return n === 1 ? 'غدًا' : n === 2 ? 'بعد يومين' : n <= 10 ? `بعد ${arNum(n)} أيام` : `بعد ${arNum(n)} يومًا`;
};

export function renderCalendar(view, args, ctx) {
  ctx.back('history');
  const today = toHijri(new Date());
  let y = today.y, m = today.m;
  const draw = () => {
    const days = monthDays(y, m);
    const first = days[0].date;
    const lead = (first.getDay() + 1) % 7; // السبت أول الأسبوع
    const g1 = days[0].date, g2 = days[days.length - 1].date;
    const gLabel = g1.getMonth() === g2.getMonth() ? `${GMONTHS[g1.getMonth()]} ${g1.getFullYear()}` : `${GMONTHS[g1.getMonth()]} – ${GMONTHS[g2.getMonth()]} ${g2.getFullYear()}`;
    const cells = days.map(({ date, h }) => {
      const ss = seasonsOf(h);
      const kind = ss.find(s => !s.whole)?.kind || (h.d >= 13 && h.d <= 15 ? 'white' : '');
      const isToday = h.y === today.y && h.m === today.m && h.d === today.d;
      const dow = date.getDay();
      return `<button class="cal-day ${kind} ${isToday ? 'today' : ''}" data-d="${h.d}">
        <b>${arNum(h.d)}</b><small>${date.getDate()}</small>${dow === 1 || dow === 4 ? '<i class="mt"></i>' : ''}</button>`;
    }).join('');
    const monthSeasons = [...new Map(days.flatMap(({ h }) => seasonsOf(h)).map(s => [s.name, s])).values()];
    const up = upcoming().slice(0, 6);
    view.innerHTML = `
      <div class="card cal">
        <div class="cal-head">
          <button class="icon-btn" id="cPrev" aria-label="الشهر السابق">${icons.chevron.replace('<svg', '<svg style="transform:scaleX(-1)"')}</button>
          <div><strong>${HMONTHS[m - 1]} ${arNum(y)} هـ</strong><small>${gLabel}</small></div>
          <button class="icon-btn" id="cNext" aria-label="الشهر التالي">${icons.chevron}</button>
        </div>
        <div class="cal-grid">${['سبت', 'أحد', 'اثنين', 'ثلاثاء', 'أربعاء', 'خميس', 'جمعة'].map(d => `<span class="dow">${d}</span>`).join('')}${'<span></span>'.repeat(lead)}${cells}</div>
        <div class="cal-legend"><span><i class="fast"></i>صيام مستحب</span><span><i class="eid"></i>عيد</span><span><i class="virtue"></i>أيام فاضلة</span><span><i class="white"></i>البيض</span><span><i class="mt dot"></i>الاثنين والخميس</span></div>
        ${y !== today.y || m !== today.m ? '<button class="btn ghost block" id="cToday">العودة إلى الشهر الحالي</button>' : ''}
      </div>
      <div id="cDay"></div>
      ${monthSeasons.length ? `<div class="section-head"><h2>في ${HMONTHS[m - 1]}</h2></div>
      <div class="list-card">${monthSeasons.map(s => `<div class="row season ${s.kind}"><span class="meta"><strong>${s.name}</strong><small>${s.text}</small><small class="src">${s.src}</small></span></div>`).join('')}</div>` : ''}
      <div class="section-head"><h2>المواسم القادمة</h2></div>
      <div class="list-card">${up.map(s => `<div class="row season ${s.kind}"><span class="cd ${s.now ? 'now' : ''}">${whenLabel(s)}</span><span class="meta"><strong>${s.name}</strong><small>${arNum(s.from)}${s.to !== s.from ? `–${arNum(s.to)}` : ''} ${HMONTHS[s.m - 1]} ${arNum(s.y)} هـ · ${s.begin.getDate()} ${GMONTHS[s.begin.getMonth()]}</small></span></div>`).join('')}</div>
      <div class="card settings">
        <div class="field"><label for="hAdj">تعديل التاريخ الهجري حسب رؤية الهلال في بلدك</label>
          <select class="select" id="hAdj">${[-2, -1, 0, 1, 2].map(v => `<option value="${v}" ${v === hijriAdj() ? 'selected' : ''}>${v === 0 ? 'بلا تعديل (أم القرى)' : `${v > 0 ? '+' : '−'}${arNum(Math.abs(v))} ${Math.abs(v) === 1 ? 'يوم' : 'يومان'}`}</option>`).join('')}</select></div>
      </div>
      <div class="note-box">${icons.info} يقتصر التقويم على المواسم الثابتة في السنة الصحيحة، ولا يذكر المناسبات التي لا أصل لها كالاحتفال بالمولد والإسراء والمعراج ورأس السنة الهجرية. وبداية الشهر تكون برؤية الهلال، فقد تختلف يومًا عن الحساب.</div>`;
    $('#cPrev').onclick = () => { m--; if (m < 1) { m = 12; y--; } draw(); };
    $('#cNext').onclick = () => { m++; if (m > 12) { m = 1; y++; } draw(); };
    $('#cToday')?.addEventListener('click', () => { y = today.y; m = today.m; draw(); });
    $('#hAdj').onchange = e => { store.set('hijriAdj', +e.target.value); scheduleAdhans(); renderCalendar(view, args, ctx); };
    view.querySelector('.cal-grid').onclick = e => {
      const b = e.target.closest('.cal-day');
      if (!b) return;
      const { date, h } = days[+b.dataset.d - 1];
      const ss = seasonsOf(h);
      const dow = date.getDay();
      const extra = [...ss, ...(h.d >= 13 && h.d <= 15 ? [WHITE] : []), ...(dow === 1 || dow === 4 ? [{ name: `يوم ${dow === 1 ? 'الاثنين' : 'الخميس'}`, text: '«تُعرض الأعمال يوم الاثنين والخميس، فأحب أن يُعرض عملي وأنا صائم».', src: 'رواه الترمذي، وصححه الألباني' }] : [])];
      $('#cDay').innerHTML = `<div class="card cal-day-info"><strong>${DAYS[(dow + 1) % 7]} ${arNum(h.d)} ${HMONTHS[h.m - 1]} ${arNum(h.y)} هـ</strong><small class="muted">${date.getDate()} ${GMONTHS[date.getMonth()]} ${date.getFullYear()}</small>
        ${extra.length ? extra.map(s => `<p><b>${s.name}</b>: ${s.text} <small class="muted">— ${s.src}</small></p>`).join('') : '<p class="muted">لا موسم خاص في هذا اليوم.</p>'}</div>`;
      $('#cDay').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    };
  };
  draw();
}
