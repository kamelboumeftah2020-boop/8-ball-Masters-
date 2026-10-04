// المصحف والاستماع
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, copyText, shareText, icons, sheet } from '../core.js';
import { SURAHS } from '../data/surahs.js';
import { RECITERS, reciterById, player, audio, events as pEvents, playSurah, playAyahs, toggle, surahName, surahSub, surahUrl, downloadSurah } from '../player.js';
import { dlButton, bindDlButtons, downloadsLink, downloadAllSurahs } from './downloads.js';
import { loadPageFont, fontFamily, prefetch, cachedCount, downloadAll } from '../mushafFont.js';
import { immersive, keepAwake } from '../native.js';
import { getWarshIndex, warshMarks } from './warsh.js';

// بداية كل جزء [السورة، الآية]
const JUZ = [[1, 1], [2, 142], [2, 253], [3, 93], [4, 24], [4, 148], [5, 82], [6, 111], [7, 88], [8, 41], [9, 93], [11, 6], [12, 53], [15, 1], [17, 1], [18, 75], [21, 1], [23, 1], [25, 21], [27, 56], [29, 46], [33, 31], [36, 28], [39, 32], [41, 47], [46, 1], [51, 31], [58, 1], [67, 1], [78, 1]];

const segment = active => `
  <div class="segmented" role="tablist">
    <a href="#/mushaf" class="${active === 'mushaf' ? 'active' : ''}" role="tab">${icons.book} المصحف</a>
    <a href="#/listen" class="${active === 'listen' ? 'active' : ''}" role="tab">${icons.headphones} الاستماع</a>
  </div>`;

function surahRows(filter, mode, widx) {
  const q = normalize(filter || '');
  const rows = SURAHS.map((s, i) => [s, i + 1])
    .filter(([s, n]) => !q || normalize(s[0]).includes(q) || String(n) === q || arNum(n) === filter.trim());
  return rows.map(([s, n]) => `<button class="row surah" data-n="${n}">
      <span class="num">${arNum(n)}</span>
      <span class="meta"><strong>سورة ${s[0]}</strong><small>${widx ? `الصفحة ${arNum(widx.s[n - 1])} · رواية ورش` : surahSub(n)}</small></span>
      ${mode === 'listen' ? `${dlButton(surahUrl(player.reciter, n))}<span class="play-ic">${icons.play}</span>` : `<span class="chev">${icons.chevron}</span>`}
    </button>`).join('');
}


/* ── فهرس المصحف ── */
export async function renderMushaf(view, args, ctx) {
  if (args[0]) return renderReader(view, +args[0], +(args[1] || 0), ctx);
  await migrateMarks().catch(() => {});
  if (!ctx.alive()) return;
  // المصحف المختار: حفص (مصحف المدينة) أو ورش (ملوّن للحفظ)
  const warsh = store.get('mushafType', 'hafs') === 'warsh';
  const widx = warsh ? await getWarshIndex().catch(() => null) : null;
  if (!ctx.alive()) return;
  if (warsh && !widx) { store.set('mushafType', 'hafs'); return renderMushaf(view, args, ctx); }
  const base = warsh ? '#/warsh/' : '#/page/';
  const last = store.get(warsh ? 'warshLast' : 'lastRead');
  const lastHref = last ? (last.p ? `${base}${last.p}` : `#/mushaf/${last.s}/${last.a}`) : '';
  let tab = store.get('mushafTab', 'surahs');
  view.innerHTML = `
    ${segment('mushaf')}
    <div class="mushaf-pick" id="mushafPick" role="radiogroup" aria-label="اختيار المصحف">
      <button data-m="hafs" class="${warsh ? '' : 'active'}" role="radio" aria-checked="${!warsh}"><strong>رواية حفص</strong><small>مصحف المدينة النبوية</small></button>
      <button data-m="warsh" class="${warsh ? 'active' : ''}" role="radio" aria-checked="${warsh}"><span class="dots"><i style="background:#0f6b5c"></i><i style="background:#8a3412"></i><i style="background:#1d4f8c"></i><i style="background:#6b2a78"></i></span><strong>رواية ورش</strong><small>ملوّن للحفظ، مع التسميع</small></button>
    </div>
    <div class="mushaf-top">
      ${last ? `<a class="card continue" href="${lastHref}">
        <span class="tile-ic">${icons.book}</span>
        <div><small class="muted">آخر قراءة</small><strong>${last.p ? `الصفحة ${arNum(last.p)}` : `سورة ${surahName(last.s)}`}</strong><small class="muted">سورة ${surahName(last.s)}</small></div>
        <span class="chev">${icons.chevron}</span>
      </a>` : `<a class="card continue" href="${base}1">
        <span class="tile-ic">${icons.book}</span>
        <div><small class="muted">ابدأ القراءة</small><strong>من أول المصحف</strong><small class="muted">سورة الفاتحة</small></div>
        <span class="chev">${icons.chevron}</span>
      </a>`}
      <button class="card goto-page" id="gotoPage"><b>${icons.layers}</b><span>صفحة</span></button>
    </div>
    <div class="card dl-card" id="dlCard" ${warsh ? 'hidden' : ''}>
      <span class="tile-ic gold sm">${icons.download}</span>
      <div><strong>المصحف دون إنترنت</strong><small id="dlText">…</small><div class="bar"><i id="dlBar"></i></div></div>
      <button class="btn" id="dlBtn">تحميل</button>
    </div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="${warsh ? 'ابحث عن سورة بالاسم أو الرقم' : 'ابحث عن سورة أو كلمة في القرآن'}" autocomplete="off"></label>
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
      list.innerHTML = surahRows(q, 'mushaf', widx) || '<div class="empty">لا توجد سورة بهذا الاسم</div>';
    } else if (tab === 'juz') {
      list.className = 'juz-grid';
      list.innerHTML = JUZ.map(([s, a], i) => `<a class="juz" href="${warsh ? `#/warsh/${widx.j[i]}` : `#/mushaf/${s}/${a}`}"><b>${arNum(i + 1)}</b><span>الجزء</span><small>${surahName(s)} ${arNum(a)}</small></a>`).join('');
    } else {
      const bm = warsh ? warshMarks() : pageMarks();
      list.className = 'list-card';
      list.innerHTML = bm.length ? bm.map(b => {
        const [s, juz] = warsh ? [widx.p[b.p - 1][0], widx.p[b.p - 1][2]] : [pagesData[b.p - 1][0][0][0], pagesData[b.p - 1][1]];
        return `<a class="row" href="${base}${b.p}">
        <span class="tile-ic gold sm">${icons.bookmark}</span>
        <span class="meta"><strong>الصفحة ${arNum(b.p)}</strong><small>سورة ${surahName(s)} · الجزء ${arNum(juz)}</small></span>
        <span class="chev">${icons.chevron}</span></a>`;
      }).join('')
        : '<div class="empty">لا توجد علامات بعد.<br>اضغط «علامة» أسفل أي صفحة أثناء القراءة.</div>';
    }
  };
  $('#gotoPage').onclick = () => goToPageSheet(last?.p || 1, base);
  $('#mushafPick').onclick = e => {
    const b = e.target.closest('[data-m]');
    if (!b || b.dataset.m === (warsh ? 'warsh' : 'hafs')) return;
    store.set('mushafType', b.dataset.m);
    renderMushaf(view, args, ctx);
  };
  // تحميل خطوط صفحات المصحف كلها (نحو ٩٠ م.ب) للقراءة دون اتصال
  let dlRunning = false;
  const dlShow = (done, failed) => {
    if (!ctx.alive()) return;
    $('#dlBar').style.width = (done / 604 * 100) + '%';
    $('#dlText').textContent = done >= 604 ? 'المصحف كاملًا محفوظ في جهازك' : `محفوظ ${arNum(done)} من ٦٠٤ صفحة${failed ? ` · تعذّر ${arNum(failed)}` : ''}`;
    $('#dlBtn').hidden = done >= 604;
  };
  cachedCount().then(c => dlShow(c, 0));
  $('#dlBtn').onclick = async () => {
    if (dlRunning) { dlRunning = false; $('#dlBtn').textContent = 'تحميل'; return; }
    dlRunning = true;
    $('#dlBtn').textContent = 'إيقاف';
    const r = await downloadAll(dlShow, () => !dlRunning || !ctx.alive());
    dlRunning = false;
    if (!ctx.alive()) return;
    $('#dlBtn').textContent = 'تحميل';
    if (r.failed) toast('تعذّر تحميل بعض الصفحات، أعد المحاولة');
    else if (r.done >= 604) toast('تم حفظ المصحف كاملًا للقراءة دون إنترنت');
  };
  $('#tabs').onclick = e => {
    const b = e.target.closest('button');
    if (!b) return;
    tab = b.dataset.t; store.set('mushafTab', tab); draw();
  };
  $('#q').oninput = () => {
    const q = $('#q').value.trim();
    if (tab !== 'surahs') { tab = 'surahs'; }
    draw();
    $('#qsearch').innerHTML = q.length >= 3 && !/^\d+$/.test(q) && !warsh
      ? `<button class="btn ghost block" id="qBtn">${icons.search} البحث عن «${esc(q)}» في آيات القرآن</button>` : '';
    $('#qBtn')?.addEventListener('click', () => searchQuran(q));
  };
  $('#list').onclick = e => {
    const b = e.target.closest('.surah');
    if (b) location.hash = warsh ? `#/warsh/${widx.s[b.dataset.n - 1]}` : `#/mushaf/${b.dataset.n}`;
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

// نسخة احتياطية بالنص (خط أميري) إن تعذّر تحميل خط الصفحة دون اتصال
async function renderTextPage(view, args, ctx) {
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

/* ── المصحف بصفحات مصحف المدينة (خطوط مجمع الملك فهد) بملء الشاشة ── */
const mushafLines = new Map();
async function getLines(p) {
  if (!mushafLines.has(p)) mushafLines.set(p, fetchJSON(`data/mushaf/${p}.json`).catch(e => { mushafLines.delete(p); throw e; }));
  return mushafLines.get(p);
}
// أول صفحة تظهر فيها الآية، من فهرس الصفحات
async function pageOfKey(s, a) { return pageOf(s, a); }

export async function renderPage(view, args, ctx) {
  const n = Math.min(604, Math.max(1, +args[0] || 1));
  const flashKey = args[1] ? `${+args[1]}:${+args[2] || 1}` : '';
  ctx.back('#/mushaf');
  view.innerHTML = '<div class="qr-loading"><div class="spinner"></div></div>';
  let lines, pages;
  try {
    [lines, pages] = await Promise.all([getLines(n), getPages()]);
    await loadPageFont(n);
  } catch {
    if (!ctx.alive()) return;
    return renderTextPage(view, args, ctx);
  }
  if (!ctx.alive()) return;
  prefetch(n);
  if (n < 604) getLines(n + 1).catch(() => {});

  // ملء الشاشة: إخفاء أشرطة التطبيق وشريط الحالة، وإبقاء الشاشة مضاءة
  document.body.classList.add('reader-full');
  immersive(true);
  const release = keepAwake();
  ctx.cleanup(() => {
    // لا نعيد الأشرطة إن كان الانتقال إلى صفحة أخرى من المصحف
    setTimeout(() => {
      if (!location.hash.startsWith('#/page/')) { document.body.classList.remove('reader-full'); immersive(false); }
    }, 0);
    release();
  });

  const [parts, juz, hizb] = pages[n - 1];
  const firstS = parts[0][0];
  ctx.title('سورة ' + surahName(firstS));
  store.set('lastRead', { p: n, s: firstS, a: parts[0][1] });
  const marked = pageMarks().some(x => x.p === n);
  const opening = n <= 2;
  const fam = fontFamily(n);

  const lineHTML = l => {
    if (l[0] === 'h') return `<div class="ql qh"><span>سورة ${surahName(l[1])}</span></div>`;
    if (l[0] === 'b') return `<div class="ql qb">${BASMALA}</div>`;
    return `<div class="ql">${l.map(([c, k]) => `<span class="w" data-k="${k}">${c}</span>`).join('')}</div>`;
  };

  view.innerHTML = `
    <div class="qr ${opening ? 'opening' : ''}" id="qr">
      <header class="qr-head">
        <button class="qr-pill" id="qrIndex">سورة ${surahName(firstS)}</button>
        <span class="qr-pill">الجزء ${arNum(juz)}</span>
        <button class="qr-mark ${marked ? 'on' : ''}" id="qrMark" aria-label="علامة الصفحة">${icons.bookmark}</button>
      </header>
      <div class="qr-page" id="qpage" style="font-family:'${fam}'">${lines.map(lineHTML).join('')}</div>
      <footer class="qr-foot"><span class="qr-pill qr-num">${arNum(n)}</span></footer>
      <div class="qr-tools" id="qrTools" hidden>
        <button data-t="exit">${icons.book}<span>الفهرس</span></button>
        <button data-t="prev" ${n <= 1 ? 'disabled' : ''}>${icons.chevron.replace('<svg', '<svg style="transform:scaleX(-1)"')}<span>السابقة</span></button>
        <button data-t="listen">${icons.headphones}<span>استماع</span></button>
        <button data-t="goto">${icons.layers}<span>صفحة</span></button>
        <button data-t="next" ${n >= 604 ? 'disabled' : ''}>${icons.chevron}<span>التالية</span></button>
      </div>
    </div>`;

  const qr = $('#qr'), page = $('#qpage');

  // حجم الخط: ١٥ سطرًا تملأ الارتفاع، ولا يتجاوز أطول سطر عرض الصفحة
  const fit = () => {
    const h = page.clientHeight, w = page.clientWidth;
    if (!h || !w) return;
    const lineH = h / 15;
    let fs = lineH / 1.62;
    page.style.setProperty('--qfs', fs + 'px');
    page.style.setProperty('--qlh', lineH + 'px');
    page.classList.add('measure');
    let widest = 0;
    const natural = [];
    $$('.ql:not(.qh):not(.qb)', page).forEach(l => { natural.push([l, l.scrollWidth]); widest = Math.max(widest, l.scrollWidth); });
    page.classList.remove('measure');
    // الأسطر القصيرة (كأواخر السور القصار) تُوسَّط كما في المصحف المطبوع بدل مدّها
    natural.forEach(([l, nw]) => l.classList.toggle('short', nw < widest * 0.72));
    if (widest > w * 0.985) { fs *= (w * 0.985) / widest; page.style.setProperty('--qfs', fs + 'px'); }
    // تحقق أخير: لا يتجاوز أي سطر عرض الصفحة بعد الضبط
    for (let k = 0; k < 4; k++) {
      let over = 0;
      $$('.ql:not(.qh):not(.qb)', page).forEach(l => { over = Math.max(over, l.scrollWidth / l.clientWidth); });
      if (over <= 1.002) break;
      fs /= over * 1.005;
      page.style.setProperty('--qfs', fs + 'px');
    }
  };
  fit();
  // يُعاد الحساب كلما تغيّر حجم الصفحة (تدوير الشاشة، ظهور الأشرطة وإخفاؤها)
  let lastSize = '';
  const ro = new ResizeObserver(() => {
    const size = page.clientWidth + 'x' + page.clientHeight;
    if (size !== lastSize) { lastSize = size; fit(); }
  });
  ro.observe(page);
  ctx.cleanup(() => ro.disconnect());

  // تمييز آية (للانتقال من البحث أو أثناء التلاوة)
  const mark = (key, cls) => {
    $$(`.w.${cls}`, page).forEach(el => el.classList.remove(cls));
    if (key) $$(`.w[data-k="${key}"]`, page).forEach(el => el.classList.add(cls));
  };
  if (flashKey) { mark(flashKey, 'flash'); setTimeout(() => mark('', 'flash'), 2600); }
  const curKey = () => (player.mode === 'ayah' ? `${player.surah}:${player.ayahs[player.ayahIdx].numberInSurah}` : '');
  const onChange = async () => {
    const key = curKey();
    if (!key) return mark('', 'active');
    if ($(`.w[data-k="${key}"]`, page)) return mark(key, 'active');
    const [s, a] = key.split(':').map(Number);
    const p = await pageOfKey(s, a);
    if (ctx.alive() && p !== n) location.replace(`#/page/${p}`);
  };
  const onEnd = async () => {
    if (player.surah >= 114) return;
    const next = player.surah + 1;
    playAyahs(next, await getSurah(next), 0);
  };
  mark(curKey(), 'active');
  pEvents.addEventListener('change', onChange);
  pEvents.addEventListener('ayahs-end', onEnd);
  ctx.cleanup(() => { pEvents.removeEventListener('change', onChange); pEvents.removeEventListener('ayahs-end', onEnd); });

  const go = (p, dir) => {
    if (p < 1 || p > 604 || p === n) return;
    page.classList.add(dir > 0 ? 'out-next' : 'out-prev');
    setTimeout(() => { if (ctx.alive()) location.replace(`#/page/${p}`); }, 140);
  };

  // أدوات الصفحة تظهر بلمسة وتختفي تلقائيًا
  let hideTimer;
  const tools = $('#qrTools');
  const showTools = on => {
    clearTimeout(hideTimer);
    tools.hidden = !on;
    if (on) hideTimer = setTimeout(() => { tools.hidden = true; }, 5000);
  };
  ctx.cleanup(() => clearTimeout(hideTimer));

  page.onclick = async e => {
    const w = e.target.closest('.w');
    if (!w) return showTools(tools.hidden);
    const [s, a] = w.dataset.k.split(':').map(Number);
    mark(w.dataset.k, 'sel');
    const ayahs = await getSurah(s);
    ayahSheet(s, ayahs, a - 1, n, () => mark('', 'sel'));
  };
  $('#qrIndex').onclick = () => { location.hash = '#/mushaf'; };
  $('#qrMark').onclick = () => {
    const on = togglePageMark(n);
    $('#qrMark').classList.toggle('on', on);
    toast(on ? `وُضعت العلامة عند الصفحة ${arNum(n)}` : 'أُزيلت العلامة');
  };
  tools.onclick = async e => {
    const b = e.target.closest('[data-t]');
    if (!b) return;
    showTools(true);
    const t = b.dataset.t;
    if (t === 'exit') location.hash = '#/mushaf';
    if (t === 'prev') go(n - 1, -1);
    if (t === 'next') go(n + 1, 1);
    if (t === 'goto') goToPageSheet(n);
    if (t === 'listen') {
      if (player.mode === 'ayah' && $(`.w[data-k="${curKey()}"]`, page)) return toggle();
      const [s, a] = parts[0];
      playAyahs(s, await getSurah(s), a - 1);
    }
  };

  // السحب لتقليب الصفحات: إلى اليمين للتالية كما في المصحف الورقي
  let sx = 0, sy = 0, st = 0;
  qr.addEventListener('touchstart', e => { sx = e.touches[0].clientX; sy = e.touches[0].clientY; st = Date.now(); }, { passive: true });
  qr.addEventListener('touchend', e => {
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

export function goToPageSheet(current, base = '#/page/') {
  const { el, close } = sheet(`
    <div class="sheet-head"><h3>الانتقال إلى صفحة</h3><small class="muted">من ١ إلى ٦٠٤</small></div>
    <input class="input page-input" id="pgNum" type="text" inputmode="numeric" autocomplete="off" value="${current}">
    <button class="btn block" id="pgGoBtn">انتقال</button>`, { label: 'الانتقال إلى صفحة' });
  const inp = $('#pgNum', el);
  const submit = () => {
    const v = parseInt(String(inp.value).replace(/[٠-٩]/g, d => '٠١٢٣٤٥٦٧٨٩'.indexOf(d)), 10);
    if (!(v >= 1 && v <= 604)) return toast('أدخل رقمًا من ١ إلى ٦٠٤');
    close();
    location.replace(`${base}${v}`);
  };
  $('#pgGoBtn', el).onclick = submit;
  inp.addEventListener('keydown', e => { if (e.key === 'Enter') submit(); });
  setTimeout(() => inp.select(), 250);
}

function ayahSheet(n, ayahs, i, page, onClose) {
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
    onClose,
    onClick: async (e, close) => {
      const b = e.target.closest('[data-a]');
      if (!b) return;
      const act = b.dataset.a;
      if (act === 'play') { playAyahs(n, ayahs, i); close(); }
      if (act === 'mark') { close(); ($('#qrMark') || $('#pgMark'))?.click(); }
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
    <div class="dl-bar">${downloadsLink()}<button class="dl-link" id="dlAll">${icons.download}<span>تحميل كل السور</span></button></div>
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة بالاسم أو الرقم" autocomplete="off"></label>
    <div class="list-card" data-listen id="list">${surahRows('', 'listen')}</div>`;
  $('#dlAll').onclick = () => downloadAllSurahs(player.reciter);
  bindDlButtons($('#list'), ctx, key => {
    const n = SURAHS.findIndex((_, i) => surahUrl(player.reciter, i + 1) === key) + 1;
    return n > 0 && downloadSurah(n, player.reciter);
  });
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
    // أزرار التحميل تتبع القارئ المختار
    $('#list').innerHTML = surahRows($('#q').value, 'listen') || '<div class="empty">لا توجد نتائج</div>';
    mark();
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
