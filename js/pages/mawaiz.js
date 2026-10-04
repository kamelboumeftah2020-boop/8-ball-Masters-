// المواعظ: المسموعة (محاضرات وخطب) والمكتوبة (آيات وأحاديث وآثار)
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, copyText, shareText, icons, durLabel, fmtDur } from '../core.js';
import { MAWAIZ, CATEGORIES, TYPE_NAMES } from '../data/mawaiz.js';
import { player, audio, events as pEvents, playLecture, toggle, isCurrentLecture, lecturePos, downloadLecture } from '../player.js';
import { dlButton, bindDlButtons, downloadsLink } from './downloads.js';

let lecturesData = null;
export async function getLectures() {
  if (!lecturesData) lecturesData = fetchJSON('data/lectures.json').catch(e => { lecturesData = null; throw e; });
  return lecturesData;
}

const segment = active => `
  <div class="segmented" role="tablist">
    <a href="#/mawaiz" class="${active === 'audio' ? 'active' : ''}" role="tab">${icons.headphones} مسموعة</a>
    <a href="#/mawaiz/written" class="${active === 'written' ? 'active' : ''}" role="tab">${icons.book} مكتوبة</a>
  </div>`;

export function renderMawaiz(view, args, ctx) {
  if (args[0] === 'written') return renderWritten(view, ctx);
  if (args[0] === 's' && args[1]) return renderSpeaker(view, args[1], ctx);
  if (args[0] === 'fav' || args[0] === 'continue') return renderLibrary(view, args[0], ctx);
  return renderAudio(view, ctx);
}

/* ── المسموعة: قائمة المشايخ ── */
async function renderAudio(view, ctx) {
  view.innerHTML = `${segment('audio')}<div class="loader"><div class="spinner"></div>جارٍ تحميل المواعظ…</div>`;
  let data;
  try { data = await getLectures(); } catch {
    view.innerHTML = `${segment('audio')}<div class="error-box">تعذّر تحميل قائمة المواعظ. تحقق من الاتصال.</div>`;
    return;
  }
  if (!ctx.alive()) return;
  const last = store.get('lastLecture');
  const total = data.speakers.reduce((s, x) => s + x.count, 0);
  view.innerHTML = `
    ${segment('audio')}
    ${last ? `<button class="card continue block" id="contLec">
      <span class="tile-ic gold">${icons.mic}</span>
      <div><small class="muted">تابع الاستماع</small><strong>${esc(last.title)}</strong><small class="muted">${esc(last.speaker)}</small></div>
      <span class="play-btn sm">${icons.play}</span>
    </button>` : ''}
    <div class="lib-cards">
      <a class="card lib" href="#/mawaiz/fav"><span class="tile-ic sm gold">${icons.starFill}</span><span><b>المفضلة</b><small>${arNum(lecFavs().length)} مادة · تشغيل متتابع</small></span></a>
      <a class="card lib" href="#/mawaiz/continue"><span class="tile-ic sm">${icons.play}</span><span><b>أكمل الاستماع</b><small>${arNum(Object.keys(store.get('lecPos', {})).length)} مادة لم تكتمل</small></span></a>
    </div>
    <div class="dl-bar">${downloadsLink()}</div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث في ${arNum(total)} محاضرة وخطبة" autocomplete="off"></label>
    <div id="results"></div>
    <div id="groups">
      ${data.groups.map(g => {
        const sp = data.speakers.filter(s => s.group === g.id);
        return `<div class="section-head"><h2>${g.name}</h2></div>
        <div class="speakers ${g.id === 'mawaiz' ? 'featured' : ''}">${sp.map(s => `
          <a class="speaker" href="#/mawaiz/s/${s.id}">
            <span class="avatar ${g.id}">${esc(initials(s.name))}</span>
            <span class="meta"><strong>${g.id === 'mawaiz' || g.id === 'ulama' || g.id === 'duroos' ? 'الشيخ ' : ''}${esc(s.name)}</strong><small>${arNum(s.count)} ${g.id === 'haram' ? 'خطبة' : 'مادة صوتية'}</small></span>
          </a>`).join('')}</div>`;
      }).join('')}
    </div>
    <p class="source-note">${icons.info} محاضرات العلماء وخطب الحرمين من موقع <b>IslamHouse</b> (دار الإسلام) ومواعظ الشيخ خالد الراشد وسائر المشايخ في «مواعظ مؤثرة» من <b>أرشيف الإنترنت</b>، مقتصرة على المواعظ دون الخطب والدروس.</p>`;

  $('#contLec')?.addEventListener('click', async () => {
    const sp = data.speakers.find(s => s.id === last.sid);
    const list = data.items[last.sid];
    if (!sp || !list?.[last.idx]) return;
    if (isCurrentLecture(sp.id, last.idx)) toggle(); else playLecture(sp, list, last.idx);
  });
  $('#q').oninput = () => {
    const q = normalize($('#q').value);
    $('#groups').hidden = q.length >= 2;
    if (q.length < 2) { $('#results').innerHTML = ''; return; }
    const res = [];
    for (const sp of data.speakers) {
      data.items[sp.id].forEach((it, i) => { if (normalize(it.t).includes(q)) res.push([sp, i, it]); });
    }
    $('#results').innerHTML = res.length
      ? `<div class="section-head"><h2>النتائج</h2><span class="muted">${arNum(res.length)}</span></div><div class="list-card">${res.slice(0, 80).map(([sp, i, it]) => lectureRow(sp, i, it, true)).join('')}</div>`
      : '<div class="empty">لا توجد نتائج</div>';
    markLectures();
  };
  // تحميل محاضرة (بكل أجزائها) للاستماع دون اتصال
  const startDl = (key, btn) => {
    const row = btn.closest('.lecture');
    const sp = data.speakers.find(s => s.id === row.dataset.sid);
    const it = data.items[sp.id][+row.dataset.i];
    it.u.forEach((_, part) => downloadLecture(sp, it, part));
    return true;
  };
  bindDlButtons($('#results'), ctx, startDl);
  bindFavButtons($('#results'));
  $('#results').onclick = e => {
    const b = e.target.closest('[data-sid]');
    if (!b) return;
    const sp = data.speakers.find(s => s.id === b.dataset.sid);
    clickLecture(sp, data.items[sp.id], +b.dataset.i);
  };
  bindMarks(ctx);
}

// الحرف الأول من اسم الشهرة (آخر كلمة دون «ال»)
const initials = name => (name.split(' ').pop() || name).replace(/^ال/, '').slice(0, 1);

function lectureRow(sp, i, it, showSpeaker) {
  const parts = it.u.length;
  const pos = lecturePos(it.u[0]);
  const sub = [showSpeaker ? sp.name : '', it.d ? durLabel(it.d) : '', parts > 1 ? `${arNum(parts)} أجزاء` : '', pos ? 'استمعت لجزء منها' : ''].filter(Boolean).join(' · ');
  return `<button class="row lecture" data-sid="${sp.id}" data-i="${i}">
    <span class="play-ic">${icons.play}</span>
    <span class="meta"><strong>${esc(it.t)}</strong>${sub ? `<small>${esc(sub)}</small>` : ''}</span>
    <span class="fav-btn ${isLecFav(sp.id, it.u[0]) ? 'on' : ''}" role="button" tabindex="0" data-fav="${sp.id}" data-u="${esc(it.u[0])}" aria-label="المفضلة">${isLecFav(sp.id, it.u[0]) ? icons.starFill : icons.star2}</span>
    ${dlButton(it.u[0])}
  </button>`;
}

/* ── المفضلة: تُحفظ بالشيخ ورابط المادة (ثابت ولو تغيّر ترتيب القائمة) ── */
export const lecFavs = () => store.get('lecFav', []);
const isLecFav = (sid, u) => lecFavs().some(f => f.sid === sid && f.u === u);
function toggleLecFav(sid, u) {
  const f = lecFavs();
  const on = !f.some(x => x.sid === sid && x.u === u);
  store.set('lecFav', on ? [{ sid, u }, ...f] : f.filter(x => !(x.sid === sid && x.u === u)));
  return on;
}
function bindFavButtons(container, onChange) {
  container.addEventListener('click', e => {
    const b = e.target.closest('.fav-btn');
    if (!b) return;
    e.stopPropagation();
    const on = toggleLecFav(b.dataset.fav, b.dataset.u);
    $$(`.fav-btn[data-u="${CSS.escape(b.dataset.u)}"]`).forEach(x => { x.classList.toggle('on', on); x.innerHTML = on ? icons.starFill : icons.star2; });
    toast(on ? 'أُضيفت إلى المفضلة' : 'أُزيلت من المفضلة');
    onChange?.();
  }, true);
}

/* ── المفضلة وأكمل الاستماع ── */
async function renderLibrary(view, kind, ctx) {
  ctx.back('#/mawaiz');
  ctx.title(kind === 'fav' ? 'المفضلة' : 'أكمل الاستماع');
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  let data;
  try { data = await getLectures(); } catch {
    view.innerHTML = '<div class="error-box">تعذّر تحميل المواعظ.</div>';
    return;
  }
  if (!ctx.alive()) return;
  const bySid = Object.fromEntries(data.speakers.map(s => [s.id, s]));
  const resolve = () => {
    if (kind === 'fav') {
      return lecFavs().map(f => {
        const list = data.items[f.sid] || [];
        const i = list.findIndex(it => it.u[0] === f.u);
        return i >= 0 ? [bySid[f.sid], i, list[i]] : null;
      }).filter(Boolean);
    }
    // المواد التي توقفت في أثنائها، بترتيب آخر استماع
    const pos = store.get('lecPos', {});
    const out = [];
    for (const url of Object.keys(pos).reverse()) {
      for (const sp of data.speakers) {
        const i = data.items[sp.id].findIndex(it => it.u.includes(url));
        if (i >= 0) { if (!out.some(([s, j]) => s.id === sp.id && j === i)) out.push([sp, i, data.items[sp.id][i], pos[url], data.items[sp.id][i].u.indexOf(url)]); break; }
      }
    }
    return out;
  };
  const draw = () => {
    const rows = resolve();
    view.innerHTML = rows.length ? `
      ${kind === 'fav' ? `<button class="btn block" id="playFav">${icons.play} تشغيل المفضلة متتابعة (${arNum(rows.length)})</button>` : `<p class="muted lib-note">${icons.info} تُستكمل كل مادة من حيث توقفت.</p>`}
      <div class="list-card" id="list">${rows.map(([sp, i, it, p, part]) => lectureRow(sp, i, it, true).replace('</strong>', `</strong>${p ? `<small class="resume">توقفت عند ${fmtDur(p)}${part > 0 ? ` من الجزء ${arNum(part + 1)}` : ''}</small>` : ''}`)).join('')}</div>`
      : `<div class="empty">${kind === 'fav' ? 'لا توجد مواد في المفضلة بعد.<br>اضغط النجمة بجانب أي موعظة لإضافتها.' : 'لا توجد مواد لم تكتمل.'}</div>`;
    markLectures();
    $('#playFav')?.addEventListener('click', () => {
      // قائمة تشغيل: كل عنصر يحمل شيخه الأصلي
      const items = rows.map(([sp, i, it]) => ({ ...it, sp: sp.name, sid: sp.id, si: i }));
      playLecture({ id: 'fav', name: 'المفضلة', group: 'mawaiz' }, items, 0);
    });
    const list = $('#list');
    if (!list) return;
    bindFavButtons(list, () => { if (kind === 'fav') draw(); });
    bindDlButtons(list, ctx, (key, btn) => {
      const row = btn.closest('.lecture');
      const sp = bySid[row.dataset.sid];
      const it = data.items[sp.id][+row.dataset.i];
      it.u.forEach((_, part) => downloadLecture(sp, it, part));
      return true;
    });
    list.onclick = e => {
      const b = e.target.closest('.lecture');
      if (!b) return;
      const sp = bySid[b.dataset.sid];
      const r = rows.find(([s, i]) => s.id === sp.id && i === +b.dataset.i);
      if (isCurrentLecture(sp.id, +b.dataset.i)) toggle(); else playLecture(sp, data.items[sp.id], +b.dataset.i, r?.[4] > 0 ? r[4] : 0);
    };
  };
  draw();
  bindMarks(ctx);
}

function clickLecture(sp, list, i) {
  if (isCurrentLecture(sp.id, i)) toggle(); else playLecture(sp, list, i);
}

function markLectures() {
  $$('.lecture').forEach(el => {
    const cur = isCurrentLecture(el.dataset.sid, +el.dataset.i);
    el.classList.toggle('current', cur);
    $('.play-ic', el).innerHTML = cur && !audio.paused ? icons.pause : icons.play;
  });
}
function bindMarks(ctx) {
  pEvents.addEventListener('change', markLectures);
  pEvents.addEventListener('state', markLectures);
  ctx.cleanup(() => { pEvents.removeEventListener('change', markLectures); pEvents.removeEventListener('state', markLectures); });
}

/* ── صفحة الشيخ ── */
async function renderSpeaker(view, sid, ctx) {
  ctx.back('#/mawaiz');
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  let data;
  try { data = await getLectures(); } catch {
    view.innerHTML = '<div class="error-box">تعذّر تحميل المواعظ. تحقق من الاتصال.</div>';
    return;
  }
  if (!ctx.alive()) return;
  const sp = data.speakers.find(s => s.id === sid);
  if (!sp) { location.hash = '#/mawaiz'; return; }
  const list = data.items[sid];
  const group = data.groups.find(g => g.id === sp.group);
  ctx.title(sp.name);
  view.innerHTML = `
    <div class="speaker-hero">
      <span class="avatar xl ${sp.group}">${esc(initials(sp.name))}</span>
      <div><small>${esc(group.name)}</small><h2>${sp.group === 'haram' || sp.group === 'ruqya' ? '' : 'الشيخ '}${esc(sp.name)}</h2><span>${arNum(list.length)} ${sp.group === 'haram' ? 'خطبة' : 'مادة صوتية'}</span></div>
      <button class="play-btn" id="playAll" aria-label="تشغيل الكل">${icons.play}</button>
    </div>
    ${sid === 'rashed' ? `<p class="source-note">${icons.info} جميع ما وجدناه من محاضرات الشيخ ومقاطعه وأجوبته، مجموعة من عدة مصادر في أرشيف الإنترنت، مع روابط بديلة تُستعمل تلقائيًا إن تعذّر أحدها.</p>` : ''}
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث في مواد الشيخ" autocomplete="off"></label>
    <div class="list-card" id="list"></div>`;
  const draw = () => {
    const q = normalize($('#q').value);
    const rows = list.map((it, i) => [it, i]).filter(([it]) => !q || normalize(it.t).includes(q));
    $('#list').innerHTML = rows.map(([it, i]) => lectureRow(sp, i, it)).join('') || '<div class="empty">لا توجد نتائج</div>';
    markLectures();
  };
  $('#q').oninput = draw;
  bindDlButtons($('#list'), ctx, (key, btn) => {
    const it = list[+btn.closest('.lecture').dataset.i];
    it.u.forEach((_, part) => downloadLecture(sp, it, part));
    return true;
  });
  bindFavButtons($('#list'));
  $('#list').onclick = e => {
    const b = e.target.closest('.lecture');
    if (b) clickLecture(sp, list, +b.dataset.i);
  };
  $('#playAll').onclick = () => {
    if (player.mode === 'lecture' && player.lec.speaker.id === sid) toggle();
    else playLecture(sp, list, 0);
  };
  draw();
  bindMarks(ctx);
}

/* ── المكتوبة ── */
export function wa3zCard(w, i) {
  const saved = store.get('savedWa3z', []).includes(i);
  return `<article class="card wa3z ${w.type}" data-i="${i}">
    <div class="wa3z-top"><span class="tag">${TYPE_NAMES[w.type]}</span><h3>${esc(w.title)}</h3></div>
    <p class="text">${esc(w.text)}</p>
    <div class="src">${esc(w.source)}</div>
    <div class="note">${esc(w.note)}</div>
    <div class="actions">
      <button class="icon-btn" data-act="copy" aria-label="نسخ">${icons.copy}</button>
      <button class="icon-btn" data-act="share" aria-label="مشاركة">${icons.share}</button>
      <button class="icon-btn ${saved ? 'on' : ''}" data-act="save" aria-label="حفظ">${icons.bookmark}</button>
    </div>
  </article>`;
}

export function bindWa3zActions(container, onChange) {
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

function renderWritten(view) {
  let cat = store.get('wa3zCat', 'all');
  const cats = [...CATEGORIES, { id: 'saved', name: 'المحفوظة' }];
  if (!cats.some(c => c.id === cat)) cat = 'all';
  const draw = () => {
    const saved = store.get('savedWa3z', []);
    const items = MAWAIZ.map((w, i) => [w, i]).filter(([w, i]) => cat === 'all' || (cat === 'saved' ? saved.includes(i) : w.cat === cat));
    $('#wlist').innerHTML = items.map(([w, i]) => wa3zCard(w, i)).join('')
      || `<div class="empty">${cat === 'saved' ? 'لم تحفظ أي موعظة بعد. اضغط على علامة الحفظ في أي موعظة.' : 'لا توجد مواعظ'}</div>`;
  };
  view.innerHTML = `
    ${segment('written')}
    <div class="chips" id="cats">${cats.map(c => `<button class="chip ${c.id === cat ? 'active' : ''}" data-c="${c.id}">${c.name}</button>`).join('')}</div>
    <div class="list" id="wlist"></div>`;
  $('#cats').onclick = e => {
    const b = e.target.closest('.chip');
    if (!b) return;
    cat = b.dataset.c; store.set('wa3zCat', cat);
    $$('#cats .chip').forEach(x => x.classList.toggle('active', x === b));
    draw();
  };
  bindWa3zActions($('#wlist'), () => { if (cat === 'saved') draw(); });
  draw();
}
