// الصفحة الرئيسية
import { $, store, arNum, esc, icons } from '../core.js';
import { MAWAIZ } from '../data/mawaiz.js';
import { ADHKAR } from '../data/adhkar.js';
import { times, currentPeriod, useGps, events as prEvents } from '../prayer.js';
import { player, audio, playSurah, playLecture, toggle, isCurrentLecture, reciterById, surahName } from '../player.js';
import { heroHTML } from './adhan.js';
import { wa3zCard, bindWa3zActions, getLectures } from './mawaiz.js';
import { wirdCardHTML } from './khatma.js';

function dailyWa3z() {
  const d = new Date();
  const day = Math.floor((d - new Date(d.getFullYear(), 0, 0)) / 864e5);
  return MAWAIZ[day % MAWAIZ.length];
}

// تذكيرات بالسنن بحسب الوقت واليوم
function reminders() {
  const out = [];
  const now = new Date();
  const dow = now.getDay(); // 0 الأحد … 5 الجمعة
  const period = currentPeriod();
  if (period) {
    const c = ADHKAR.find(x => x.id === period);
    const prog = store.get('adhkarProg', {})[`${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}${period}`];
    const done = prog && prog.every(x => x === 0);
    if (!done) out.push({ href: `#/adhkar/${period}`, icon: c.icon, gold: period !== 'morning', title: `حان وقت ${c.name}`, text: c.hint });
  }
  if (dow === 5) {
    out.push({ href: '#/mushaf/18', icon: 'book', title: 'يوم الجمعة: سورة الكهف', text: '«من قرأ سورة الكهف يوم الجمعة أضاء له من النور ما بين الجمعتين» — رواه الحاكم والبيهقي، وصححه الألباني' });
    out.push({ href: '#/adhkar/misc', icon: 'star', gold: true, title: 'أكثروا من الصلاة على النبي ﷺ', text: '«إن من أفضل أيامكم يوم الجمعة… فأكثروا عليّ من الصلاة فيه» — رواه أبو داود، وصححه الألباني' });
  }
  if (dow === 0 || dow === 3) {
    out.push({ icon: 'calendar', title: `غدًا يوم ${dow === 0 ? 'الاثنين' : 'الخميس'}`, text: '«تُعرض الأعمال يوم الاثنين والخميس، فأحب أن يُعرض عملي وأنا صائم» — رواه الترمذي، وصححه الألباني' });
  }
  const hd = times.hijri?.day;
  if (hd >= 12 && hd <= 15) {
    out.push({ icon: 'moon', gold: true, title: hd === 12 ? 'غدًا تبدأ الأيام البيض' : 'الأيام البيض', text: 'صيام ثلاثة أيام من كل شهر كصيام الدهر (متفق عليه)، وأفضلها الثالث عشر والرابع عشر والخامس عشر — رواه الترمذي والنسائي، وحسنه الألباني' });
  }
  return out;
}

export function renderHome(view, args, ctx) {
  const draw = () => {
    const last = store.get('lastRead');
    const lastL = store.get('lastListen');
    const lastLec = store.get('lastLecture');
    const w = dailyWa3z();
    const rem = reminders();
    const cont = [];
    if (last) cont.push(`<a class="mini-card" href="${last.p ? `#/page/${last.p}` : `#/mushaf/${last.s}/${last.a}`}"><span class="tile-ic sm">${icons.book}</span><span><small>تابع القراءة</small><b>${last.p ? `صفحة ${arNum(last.p)} · ` : ''}${surahName(last.s)}</b></span></a>`);
    if (lastL) cont.push(`<button class="mini-card" id="contListen"><span class="tile-ic sm gold">${icons.headphones}</span><span><small>تابع التلاوة</small><b>${surahName(lastL.surah)} · ${esc(reciterById(lastL.reciter).name.split(' ').slice(-1)[0])}</b></span></button>`);
    if (lastLec) cont.push(`<button class="mini-card" id="contLec"><span class="tile-ic sm">${icons.mic}</span><span><small>تابع الموعظة</small><b>${esc(lastLec.title)}</b></span></button>`);

    view.innerHTML = `
      ${heroHTML({ greeting: 'السلام عليكم ورحمة الله' })}
      ${rem.length ? `<div class="reminders">${rem.map(r => `
        <${r.href ? `a href="${r.href}"` : 'div'} class="reminder">
          <span class="tile-ic sm ${r.gold ? 'gold' : ''}">${icons[r.icon]}</span>
          <span><b>${r.title}</b><small>${esc(r.text)}</small></span>
        </${r.href ? 'a' : 'div'}>`).join('')}</div>` : ''}
      <div class="tiles three">
        <a class="tile" href="#/mushaf"><span class="tile-ic">${icons.book}</span><strong>المصحف</strong></a>
        <a class="tile" href="#/listen"><span class="tile-ic gold">${icons.headphones}</span><strong>التلاوات</strong></a>
        <a class="tile" href="#/mawaiz"><span class="tile-ic">${icons.mic}</span><strong>المواعظ</strong></a>
        <a class="tile" href="#/adhkar"><span class="tile-ic gold">${icons.hands}</span><strong>الأذكار</strong></a>
        <a class="tile" href="#/qibla"><span class="tile-ic">${icons.compass}</span><strong>القبلة</strong></a>
        <a class="tile" href="#/tasbih"><span class="tile-ic gold">${icons.beads}</span><strong>السبحة</strong></a>
        <a class="tile" href="#/khatma"><span class="tile-ic">${icons.target}</span><strong>الختمة</strong></a>
        <a class="tile" href="#/hadith"><span class="tile-ic gold">${icons.scroll}</span><strong>الحديث</strong></a>
        <a class="tile" href="#/reminders"><span class="tile-ic">${icons.bell}</span><strong>التذكيرات</strong></a>
        <a class="tile" href="#/calendar"><span class="tile-ic gold">${icons.calendar}</span><strong>التقويم</strong></a>
        <a class="tile" href="#/ruqya"><span class="tile-ic">${icons.heart}</span><strong>الرقية</strong></a>
        <a class="tile" href="#/library"><span class="tile-ic gold">${icons.book}</span><strong>المكتبة</strong></a>
        <a class="tile" href="#/stories"><span class="tile-ic">${icons.scroll}</span><strong>قصص الأنبياء</strong></a>
        <a class="tile" href="#/mawaiz/written"><span class="tile-ic gold">${icons.list}</span><strong>مواعظ مكتوبة</strong></a>
        <a class="tile" href="#/downloads"><span class="tile-ic">${icons.download}</span><strong>التنزيلات</strong></a>
      </div>
      <div id="wirdBox"></div>
      ${cont.length ? `<div class="section-head"><h2>تابع من حيث توقفت</h2></div><div class="mini-cards">${cont.join('')}</div>` : ''}
      <div class="section-head"><h2>موعظة اليوم</h2><a href="#/mawaiz/written">المزيد</a></div>
      <div id="dailyBox">${wa3zCard(w, MAWAIZ.indexOf(w))}</div>
      <a class="card promo" href="#/mawaiz/s/rashed">
        <span class="avatar mawaiz">ر</span>
        <span><small>مواعظ مسموعة</small><b>الشيخ خالد الراشد وكبار المشايخ</b><small>أكثر من ٦٠٠٠ موعظة ومحاضرة</small></span>
        <span class="play-btn sm">${icons.play}</span>
      </a>`;

    view.querySelector('[data-gps]')?.addEventListener('click', e => { e.preventDefault(); useGps(); });
    $('#contListen')?.addEventListener('click', () => {
      if (player.mode === 'surah' && player.surah === lastL.surah && audio.src) toggle(); else playSurah(lastL.surah, lastL.reciter);
    });
    $('#contLec')?.addEventListener('click', async () => {
      try {
        const data = await getLectures();
        const sp = data.speakers.find(s => s.id === lastLec.sid);
        if (isCurrentLecture(sp.id, lastLec.idx)) toggle(); else playLecture(sp, data.items[sp.id], lastLec.idx);
      } catch { /* غير متصل */ }
    });
    bindWa3zActions($('#dailyBox'));
    wirdCardHTML().then(h => { if (ctx.alive() && $('#wirdBox')) $('#wirdBox').innerHTML = h; });
  };
  draw();
  prEvents.addEventListener('update', draw);
  ctx.cleanup(() => prEvents.removeEventListener('update', draw));
}
