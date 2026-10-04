// المصحف والاستماع
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, copyText, shareText, icons, sheet } from '../core.js';
import { SURAHS } from '../data/surahs.js';
import { RECITERS, reciterById, player, audio, events as pEvents, playSurah, playAyahs, toggle, surahName, surahSub } from '../player.js';

// بداية كل جزء [السورة، الآية]
const JUZ = [[1, 1], [2, 142], [2, 253], [3, 93], [4, 24], [4, 148], [5, 82], [6, 111], [7, 88], [8, 41], [9, 93], [11, 6], [12, 53], [15, 1], [17, 1], [18, 75], [21, 1], [23, 1], [25, 21], [27, 56], [29, 46], [33, 31], [36, 28], [39, 32], [41, 47], [46, 1], [51, 31], [58, 1], [67, 1], [78, 1]];

const segment = active => `
  <div class="segmented" role="tablist">
    <a href="#/mushaf" class="${active === 'mushaf' ? 'active' : ''}" role="tab">${icons.book} المصحف</a>
    <a href="#/listen" class="${active === 'listen' ? 'active' : ''}" role="tab">${icons.headphones} الاستماع</a>
  </div>`;

function surahRows(filter, mode) {
  const q = normalize(filter || '');
  const rows = SURAHS.map((s, i) => [s, i + 1])
    .filter(([s, n]) => !q || normalize(s[0]).includes(q) || String(n) === q || arNum(n) === filter.trim());
  return rows.map(([s, n]) => `<button class="row surah" data-n="${n}">
      <span class="num">${arNum(n)}</span>
      <span class="meta"><strong>سورة ${s[0]}</strong><small>${surahSub(n)}</small></span>
      ${mode === 'listen' ? `<span class="play-ic">${icons.play}</span>` : `<span class="chev">${icons.chevron}</span>`}
    </button>`).join('');
}


/* ── فهرس المصحف ── */
export async function renderMushaf(view, args, ctx) {
  if (args[0]) return renderReader(view, +args[0], +(args[1] || 0), ctx);
  await migrateMarks().catch(() => {});
  if (!ctx.alive()) return;
  const last = store.get('lastRead');
  const lastHref = last ? (last.p ? `#/page/${last.p}` : `#/mushaf/${last.s}/${last.a}`) : '';
  let tab = store.get('mushafTab', 'surahs');
  view.innerHTML = `
    ${segment('mushaf')}
    <div class="mushaf-top">
      ${last ? `<a class="card continue" href="${lastHref}">
        <span class="tile-ic">${icons.book}</span>
        <div><small class="muted">آخر قراءة</small><strong>${last.p ? `الصفحة ${arNum(last.p)}` : `سورة ${surahName(last.s)}`}</strong><small class="muted">سورة ${surahName(last.s)}</small></div>
        <span class="chev">${icons.chevron}</span>
      </a>` : `<a class="card continue" href="#/page/1">
        <span class="tile-ic">${icons.book}</span>
        <div><small class="muted">ابدأ القراءة</small><strong>من أول المصحف</strong><small class="muted">سورة الفاتحة</small></div>
        <span class="chev">${icons.chevron}</span>
      </a>`}
      <button class="card goto-page" id="gotoPage"><b>${icons.layers}</b><span>صفحة</span></button>
    </div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة أو كلمة في القرآن" autocomplete="off"></label>
    <div id="qsearch"></div>
    <div class="tabs" id="tabs">
      <button data-t="surahs">السور</button><button data-t="juz">الأجزاء</button><button data-t="marks">العلامات</button>
    </div>
    <div id="list"></div>`;

  const pagesData = await getPages().catch(() => []);
  if (!ctx.alive()) return;
  const draw = () => {
    $$('#tabs button').forEach(b => b.classList.toggle('active', b.dataset.t === tab));
    const q = $('#q').value;
    const list = $('#list');
    if (tab === 'surahs') {
      list.className = 'list-card';
      list.innerHTML = surahRows(q) || '<div class="empty">لا توجد سورة بهذا الاسم</div>';
    } else if (tab === 'juz') {
      list.className = 'juz-grid';
      list.innerHTML = JUZ.map(([s, a], i) => `<a class="juz" href="#/mushaf/${s}/${a}"><b>${arNum(i + 1)}</b><span>الجزء</span><small>${surahName(s)} ${arNum(a)}</small></a>`).join('');
    } else {
      const bm = pageMarks();
      list.className = 'list-card';
      list.innerHTML = bm.length ? bm.map(b => {
        const [parts, juz] = pagesData[b.p - 1];
        return `<a class="row" href="#/page/${b.p}">
        <span class="tile-ic gold sm">${icons.bookmark}</span>
        <span class="meta"><strong>الصفحة ${arNum(b.p)}</strong><small>سورة ${surahName(parts[0][0])} · الجزء ${arNum(juz)}</small></span>
        <span class="chev">${icons.chevron}</span></a>`;
      }).join('')
        : '<div class="empty">لا توجد علامات بعد.<br>اضغط «علامة» أسفل أي صفحة أثناء القراءة.</div>';
    }
  };
  $('#gotoPage').onclick = () => goToPageSheet(last?.p || 1);
  $('#tabs').onclick = e => {
    const b = e.target.closest('button');
    if (!b) return;
    tab = b.dataset.t; store.set('mushafTab', tab); draw();
  };
  $('#q').oninput = () => {
    const q = $('#q').value.trim();
    if (tab !== 'surahs') { tab = 'surahs'; }
    draw();
    $('#qsearch').innerHTML = q.length >= 3 && !/^\d+$/.test(q)
      ? `<button class="btn ghost block" id="qBtn">${icons.search} البحث عن «${esc(q)}» في آيات القرآن</button>` : '';
    $('#qBtn')?.addEventListener('click', () => searchQuran(q));
  };
  $('#list').onclick = e => {
    const b = e.target.closest('.surah');
    if (b) location.hash = `#/mushaf/${b.dataset.n}`;
  };
  draw();
}

async function searchQuran(q) {
  const box = $('#qsearch');
  box.innerHTML = '<div class="loader small"><div class="spinner"></div></div>';
  try {
    const { data } = await fetchJSON(`https://api.alquran.cloud/v1/search/${encodeURIComponent(q.replace(/[\u064B-\u065F\u0670]/g, ''))}/all/quran-simple-clean`);
    const m = data.matches.slice(0, 50);
    box.innerHTML = `<div class="section-head"><h2>نتائج البحث</h2><span class="muted">${arNum(data.count)} نتيجة${data.count > 50 ? ' (أول ٥٠)' : ''}</span></div>
      <div class="list-card">${m.map(x => `<a class="row result" href="#/mushaf/${x.surah.number}/${x.numberInSurah}">
        <span class="meta"><span class="q-snippet">${esc(x.text)}</span><small>سورة ${surahName(x.surah.number)} · الآية ${arNum(x.numberInSurah)}</small></span></a>`).join('')}</div>`;
  } catch {
    box.innerHTML = '<div class="empty">لا توجد نتائج، أو تعذّر الاتصال.</div>';
  }
}

/* ── قارئ السورة ── */
const surahCache = new Map();
async function getSurah(n) {
  if (surahCache.has(n)) return surahCache.get(n);
  let ayahs;
  try {
    // النص محفوظ مع التطبيق ليعمل المصحف دون اتصال
    const rows = await fetchJSON(`data/quran/${n}.json`);
    ayahs = rows.map(([number, numberInSurah, text, page, juz, sajda]) => ({ number, numberInSurah, text, page, juz, sajda: !!sajda }));
  } catch {
    const { data } = await fetchJSON(`https://api.alquran.cloud/v1/surah/${n}/quran-uthmani`);
    ayahs = data.ayahs.map(a => ({ number: a.number, numberInSurah: a.numberInSurah, text: a.text.replace(/\uFEFF/g, ''), page: a.page, juz: a.juz, sajda: !!a.sajda }));
  }
  // فصل البسملة عن الآية الأولى (عدا الفاتحة والتوبة)
  if (n !== 1 && n !== 9) {
    const w = ayahs[0].text.split(' ');
    if (w[0].startsWith('بِسْمِ')) ayahs[0].text = w.slice(4).join(' ');
  }
  surahCache.set(n, ayahs);
  return ayahs;
}

/* ── فهرس الصفحات (٦٠٤ صفحة كمصحف المدينة) ── */
let pagesIndex = null;
export async function getPages() {
  if (!pagesIndex) pagesIndex = fetchJSON('data/pages.json').catch(e => { pagesIndex = null; throw e; });
  return pagesIndex;
}
// رقم الصفحة التي فيها الآية
export async function pageOf(s, a) {
  const pages = await getPages();
  for (let i = 0; i < pages.length; i++) {
    if (pages[i][0].some(([ps, from, to]) => ps === s && a >= from && a <= to)) return i + 1;
  }
  return 1;
}

/* ── علامات الصفحات وآخر قراءة ── */
const pageMarks = () => store.get('pageMarks', []);
export function togglePageMark(p) {
  const m = pageMarks();
  const i = m.findIndex(x => x.p === p);
  if (i >= 0) m.splice(i, 1); else m.unshift({ p, t: Date.now() });
  store.set('pageMarks', m);
  return i < 0;
}
// نقل علامات الآيات القديمة إلى علامات صفحات
async function migrateMarks() {
  const old = store.get('bookmarks', []);
  if (!old.length) return;
  const m = pageMarks();
  for (const b of old) {
    const p = await pageOf(b.s, b.a);
    if (!m.some(x => x.p === p)) m.push({ p, t: b.t || Date.now() });
  }
  store.set('pageMarks', m);
  store.set('bookmarks', []);
}

/* ── القارئ: من سورة/آية إلى صفحتها ── */
async function renderReader(view, n, goto, ctx) {
  if (!(n >= 1 && n <= 114)) { location.hash = '#/mushaf'; return; }
  const p = await pageOf(n, goto || 1);
  if (!ctx.alive()) return;
  location.replace(`#/page/${p}${goto ? `/${n}/${goto}` : ''}`);
}

const BASMALA = 'بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ';

export async function renderPage(view, args, ctx) {
  const n = Math.min(604, Math.max(1, +args[0] || 1));
  const flashS = +args[1] || 0, flashA = +args[2] || 0;
  ctx.back('#/mushaf');
  document.body.classList.add('page-mode');
  ctx.cleanup(() => document.body.classList.remove('page-mode'));
  let pages, parts;
  try {
    pages = await getPages();
    parts = await Promise.all(pages[n - 1][0].map(async ([s, from, to]) => ({ s, from, to, ayahs: await getSurah(s) })));
  } catch {
    view.innerHTML = `<div class="error-box">تعذّر تحميل الصفحة.<br><br><button class="btn" id="retry">إعادة المحاولة</button></div>`;
    $('#retry').onclick = () => renderPage(view, args, ctx);
    return;
  }
  if (!ctx.alive()) return;
  const [, juz, hizb] = pages[n - 1];
  const first = parts[0];
  ctx.title('سورة ' + surahName(first.s));
  store.set('lastRead', { p: n, s: first.s, a: first.from });
  const marked = pageMarks().some(x => x.p === n);
  // الصفحتان الأوليان (الفاتحة وأول البقرة) تُعرضان في الوسط كما في المصحف
  const opening = n <= 2;

  const body = parts.map(({ s, from, to, ayahs }) => {
    let h = '';
    if (from === 1) {
      h += `<div class="m-banner"><span>سورة ${surahName(s)}</span></div>`;
      if (s !== 1 && s !== 9) h += `<div class="m-basmala">${BASMALA}</div>`;
    }
    const items = [];
    for (let i = from - 1; i < to; i++) {
      const a = ayahs[i];
      items.push(`<span class="ayah" data-s="${s}" data-i="${i}">${a.text}<span class="end">۝${arNum(a.numberInSurah)}</span></span>`);
    }
    return h + `<p class="m-text">${items.join(' ')}</p>`;
  }).join('');

  view.innerHTML = `
    <div class="m-book" id="book">
      <article class="m-page ${opening ? 'opening' : ''} ${marked ? 'marked' : ''}" id="mpage">
        <span class="ribbon" aria-hidden="true"></span>
        <header class="m-head"><span>سورة ${surahName(first.s)}</span><span>الجزء ${arNum(juz)} · الحزب ${arNum(hizb)}</span></header>
        <div class="m-body" id="mbody">${body}</div>
        <footer class="m-foot"><span>${arNum(n)}</span></footer>
      </article>
    </div>
    <div class="m-bar">
      <button class="icon-btn raised" id="pgPrev" aria-label="الصفحة السابقة" ${n <= 1 ? 'disabled' : ''}>${icons.chevron.replace('<svg', '<svg style="transform:scaleX(-1)"')}</button>
      <button class="btn ghost m-btn ${marked ? 'on' : ''}" id="pgMark">${icons.bookmark}<span>علامة</span></button>
      <button class="btn ghost m-btn" id="pgListen">${icons.headphones}<span>استماع</span></button>
      <button class="btn ghost m-btn" id="pgGo">${arNum(n)} / ${arNum(604)}</button>
      <button class="icon-btn raised" id="pgNext" aria-label="الصفحة التالية" ${n >= 604 ? 'disabled' : ''}>${icons.chevron}</button>
    </div>`;

  const go = (p, dir) => {
    if (p < 1 || p > 604 || p === n) return;
    const pg = $('#mpage');
    if (pg && dir) pg.classList.add(dir > 0 ? 'out-next' : 'out-prev');
    setTimeout(() => { if (ctx.alive()) location.replace(`#/page/${p}`); }, dir ? 160 : 0);
  };

  // ملاءمة حجم الخط لتظهر الصفحة كاملة دون تمرير
  const fit = () => {
    const pg = $('#mpage'), bd = $('#mbody');
    if (!pg || !bd) return;
    let lo = 13, hi = opening ? 34 : 30;
    for (let k = 0; k < 10; k++) {
      const mid = (lo + hi) / 2;
      bd.style.setProperty('--mfs', mid + 'px');
      if (bd.scrollHeight <= bd.clientHeight + 1) lo = mid; else hi = mid;
    }
    bd.style.setProperty('--mfs', Math.floor(lo * 4) / 4 + 'px');
    bd.classList.toggle('scroll', bd.scrollHeight > bd.clientHeight + 1);
  };
  (document.fonts?.load ? document.fonts.load('20px "Amiri Quran"').catch(() => {}) : Promise.resolve()).then(() => { if (ctx.alive()) { fit(); flash(); highlight(false); } });
  window.addEventListener('resize', fit);
  ctx.cleanup(() => window.removeEventListener('resize', fit));

  const flash = () => {
    if (!flashS) return;
    const i = flashA - 1;
    const el = $(`.ayah[data-s="${flashS}"][data-i="${i}"]`);
    if (el) { el.classList.add('flash'); el.scrollIntoView({ block: 'nearest' }); }
  };

  // تمييز الآية التي تُتلى، وقلب الصفحة مع التلاوة
  const highlight = follow => {
    $$('.ayah.active').forEach(el => el.classList.remove('active'));
    if (player.mode !== 'ayah') return;
    const el = $(`.ayah[data-s="${player.surah}"][data-i="${player.ayahIdx}"]`);
    if (el) { el.classList.add('active'); el.scrollIntoView({ block: 'nearest' }); return; }
    if (follow) {
      const a = player.ayahs[player.ayahIdx];
      if (a?.page && a.page !== n) location.replace(`#/page/${a.page}`);
    }
  };
  const onChange = () => highlight(true);
  // عند انتهاء السورة أثناء الاستماع نكمل بالسورة التالية
  const onEnd = async () => {
    if (player.surah >= 114) return;
    const next = player.surah + 1;
    const ayahs = await getSurah(next);
    playAyahs(next, ayahs, 0);
  };
  pEvents.addEventListener('change', onChange);
  pEvents.addEventListener('ayahs-end', onEnd);
  ctx.cleanup(() => { pEvents.removeEventListener('change', onChange); pEvents.removeEventListener('ayahs-end', onEnd); });

  $('#pgNext').onclick = () => go(n + 1, 1);
  $('#pgPrev').onclick = () => go(n - 1, -1);
  $('#pgMark').onclick = () => {
    const on = togglePageMark(n);
    $('#mpage').classList.toggle('marked', on);
    $('#pgMark').classList.toggle('on', on);
    toast(on ? `وُضعت العلامة عند الصفحة ${arNum(n)}` : 'أُزيلت العلامة');
  };
  $('#pgListen').onclick = () => {
    if (player.mode === 'ayah' && $(`.ayah[data-s="${player.surah}"][data-i="${player.ayahIdx}"]`)) return toggle();
    playAyahs(first.s, first.ayahs, first.from - 1);
  };
  $('#pgGo').onclick = () => goToPageSheet(n);
  $('#mbody').onclick = e => {
    const el = e.target.closest('.ayah');
    if (!el) return;
    const s = +el.dataset.s;
    const part = parts.find(x => x.s === s);
    ayahSheet(s, part.ayahs, +el.dataset.i, n);
  };

  // السحب لتقليب الصفحات: إلى اليمين للتالية كما في المصحف الورقي
  let sx = 0, sy = 0, st = 0;
  const book = $('#book');
  book.addEventListener('touchstart', e => { sx = e.touches[0].clientX; sy = e.touches[0].clientY; st = Date.now(); }, { passive: true });
  book.addEventListener('touchend', e => {
    const dx = e.changedTouches[0].clientX - sx, dy = e.changedTouches[0].clientY - sy;
    if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy) * 1.5 && Date.now() - st < 800) go(dx > 0 ? n + 1 : n - 1, dx > 0 ? 1 : -1);
  }, { passive: true });
  const onKey = e => {
    if (e.key === 'ArrowLeft') go(n + 1, 1);
    if (e.key === 'ArrowRight') go(n - 1, -1);
  };
  document.addEventListener('keydown', onKey);
  ctx.cleanup(() => document.removeEventListener('keydown', onKey));
}

function goToPageSheet(current) {
  const { el, close } = sheet(`
    <div class="sheet-head"><h3>الانتقال إلى صفحة</h3><small class="muted">من ١ إلى ٦٠٤</small></div>
    <input class="input page-input" id="pgNum" type="text" inputmode="numeric" autocomplete="off" value="${current}">
    <button class="btn block" id="pgGoBtn">انتقال</button>`, { label: 'الانتقال إلى صفحة' });
  const inp = $('#pgNum', el);
  const submit = () => {
    const v = parseInt(String(inp.value).replace(/[٠-٩]/g, d => '٠١٢٣٤٥٦٧٨٩'.indexOf(d)), 10);
    if (!(v >= 1 && v <= 604)) return toast('أدخل رقمًا من ١ إلى ٦٠٤');
    close();
    location.replace(`#/page/${v}`);
  };
  $('#pgGoBtn', el).onclick = submit;
  inp.addEventListener('keydown', e => { if (e.key === 'Enter') submit(); });
  setTimeout(() => inp.select(), 250);
}

function ayahSheet(n, ayahs, i, page) {
  const a = ayahs[i];
  const marked = pageMarks().some(x => x.p === page);
  const ref = `[سورة ${surahName(n)}: ${a.numberInSurah}]`;
  sheet(`
    <div class="sheet-head"><h3>سورة ${surahName(n)} · الآية ${arNum(a.numberInSurah)}</h3>
    <small class="muted">الصفحة ${arNum(a.page)} · الجزء ${arNum(a.juz)}${a.sajda ? ' · موضع سجدة' : ''}</small></div>
    <div class="sheet-actions">
      <button data-a="play">${icons.play}<span>استمع من هنا</span></button>
      <button data-a="tafsir">${icons.book}<span>التفسير</span></button>
      <button data-a="mark">${icons.bookmark}<span>${marked ? 'إزالة العلامة' : 'علامة الصفحة'}</span></button>
      <button data-a="copy">${icons.copy}<span>نسخ</span></button>
      <button data-a="share">${icons.share}<span>مشاركة</span></button>
    </div>
    <div id="tafsirBox"></div>`, {
    label: 'خيارات الآية',
    onClick: async (e, close) => {
      const b = e.target.closest('[data-a]');
      if (!b) return;
      const act = b.dataset.a;
      if (act === 'play') { playAyahs(n, ayahs, i); close(); }
      if (act === 'mark') { close(); $('#pgMark')?.click(); }
      if (act === 'copy') { copyText(`﴿${a.text}﴾ ${ref}`); close(); }
      if (act === 'share') { shareText(`﴿${a.text}﴾ ${ref}`); close(); }
      if (act === 'tafsir') {
        const box = $('#tafsirBox');
        box.innerHTML = '<div class="loader small"><div class="spinner"></div></div>';
        try {
          const { data } = await fetchJSON(`https://api.alquran.cloud/v1/ayah/${a.number}/ar.muyassar`);
          box.innerHTML = `<div class="tafsir"><b>التفسير الميسّر</b><p>${esc(data.text)}</p><small>إعداد نخبة من العلماء — مجمع الملك فهد لطباعة المصحف الشريف</small></div>`;
        } catch { box.innerHTML = '<div class="empty">تعذّر تحميل التفسير. تحقق من الاتصال.</div>'; }
      }
    },
  });
}

/* ── الاستماع ── */
export function renderListen(view, args, ctx) {
  const r = reciterById(player.reciter);
  view.innerHTML = `
    ${segment('listen')}
    <div class="card reciter-card">
      <span class="avatar" id="rAvatar">${r.name[0]}</span>
      <div class="grow">
        <small class="muted">القارئ</small>
        <select class="select" id="reciterSel" aria-label="اختيار القارئ">
          ${RECITERS.map(x => `<option value="${x.id}" ${x.id === r.id ? 'selected' : ''}>${x.name}</option>`).join('')}
        </select>
      </div>
    </div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة بالاسم أو الرقم" autocomplete="off"></label>
    <div class="list-card" data-listen id="list">${surahRows('', 'listen')}</div>`;
  const mark = () => {
    $$('#list .surah').forEach(el => {
      const isCur = player.mode === 'surah' && +el.dataset.n === player.surah;
      el.classList.toggle('current', isCur);
      $('.play-ic', el).innerHTML = isCur && !audio.paused ? icons.pause : icons.play;
    });
  };
  $('#reciterSel').onchange = e => {
    player.reciter = e.target.value;
    store.set('reciter', player.reciter);
    $('#rAvatar').textContent = reciterById(player.reciter).name[0];
    if (player.mode === 'surah') playSurah(player.surah);
  };
  $('#q').oninput = e => { $('#list').innerHTML = surahRows(e.target.value, 'listen') || '<div class="empty">لا توجد نتائج</div>'; mark(); };
  $('#list').onclick = e => {
    const b = e.target.closest('.surah');
    if (!b) return;
    const n = +b.dataset.n;
    if (player.mode === 'surah' && player.surah === n) toggle();
    else playSurah(n, $('#reciterSel').value);
  };
  mark();
  pEvents.addEventListener('change', mark);
  pEvents.addEventListener('state', mark);
  ctx.cleanup(() => { pEvents.removeEventListener('change', mark); pEvents.removeEventListener('state', mark); });
}
