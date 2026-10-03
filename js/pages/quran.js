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

const bookmarks = () => store.get('bookmarks', []);
function toggleBookmark(s, a) {
  const b = bookmarks();
  const i = b.findIndex(x => x.s === s && x.a === a);
  if (i >= 0) b.splice(i, 1); else b.unshift({ s, a, t: Date.now() });
  store.set('bookmarks', b);
  return i < 0;
}

/* ── فهرس المصحف ── */
export function renderMushaf(view, args, ctx) {
  if (args[0]) return renderReader(view, +args[0], +(args[1] || 0), ctx);
  const last = store.get('lastRead');
  let tab = store.get('mushafTab', 'surahs');
  view.innerHTML = `
    ${segment('mushaf')}
    ${last ? `<a class="card continue" href="#/mushaf/${last.s}/${last.a}">
      <span class="tile-ic">${icons.book}</span>
      <div><small class="muted">آخر قراءة</small><strong>سورة ${surahName(last.s)}</strong><small class="muted">الآية ${arNum(last.a)}</small></div>
      <span class="chev">${icons.chevron}</span>
    </a>` : ''}
    <label class="search">${icons.search}<input id="q" type="search" placeholder="ابحث عن سورة أو كلمة في القرآن" autocomplete="off"></label>
    <div id="qsearch"></div>
    <div class="tabs" id="tabs">
      <button data-t="surahs">السور</button><button data-t="juz">الأجزاء</button><button data-t="marks">العلامات</button>
    </div>
    <div id="list"></div>`;

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
      const bm = bookmarks();
      list.className = 'list-card';
      list.innerHTML = bm.length ? bm.map(b => `<a class="row" href="#/mushaf/${b.s}/${b.a}">
        <span class="tile-ic gold sm">${icons.bookmark}</span>
        <span class="meta"><strong>سورة ${surahName(b.s)}</strong><small>الآية ${arNum(b.a)}</small></span>
        <span class="chev">${icons.chevron}</span></a>`).join('')
        : '<div class="empty">لا توجد علامات بعد.<br>اضغط على أي آية أثناء القراءة واختر «حفظ علامة».</div>';
    }
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
  const { data } = await fetchJSON(`https://api.alquran.cloud/v1/surah/${n}/quran-uthmani`);
  const ayahs = data.ayahs.map(a => ({ number: a.number, numberInSurah: a.numberInSurah, text: a.text, page: a.page, juz: a.juz, sajda: !!a.sajda }));
  // فصل البسملة عن الآية الأولى (عدا الفاتحة والتوبة)
  if (n !== 1 && n !== 9) {
    const w = ayahs[0].text.split(' ');
    if (w[0].startsWith('بِسْمِ')) ayahs[0].text = w.slice(4).join(' ');
  }
  surahCache.set(n, ayahs);
  return ayahs;
}

function highlightAyah() {
  $$('.ayah.active').forEach(el => el.classList.remove('active'));
  if (player.mode !== 'ayah') return;
  const el = document.querySelector(`.ayah[data-s="${player.surah}"][data-i="${player.ayahIdx}"]`);
  if (el) { el.classList.add('active'); el.scrollIntoView({ behavior: 'smooth', block: 'center' }); }
}

async function renderReader(view, n, goto, ctx) {
  if (!(n >= 1 && n <= 114)) { location.hash = '#/mushaf'; return; }
  ctx.title('سورة ' + surahName(n));
  ctx.back('#/mushaf');
  view.innerHTML = '<div class="loader"><div class="spinner"></div>جارٍ تحميل السورة…</div>';
  let ayahs;
  try { ayahs = await getSurah(n); } catch {
    view.innerHTML = `<div class="error-box">تعذّر تحميل السورة. تحقق من الاتصال بالإنترنت.<br><br><button class="btn" id="retry">إعادة المحاولة</button></div>`;
    $('#retry').onclick = () => renderReader(view, n, goto, ctx);
    return;
  }
  if (!ctx.alive()) return;
  const marks = new Set(bookmarks().filter(b => b.s === n).map(b => b.a));
  const size = store.get('qsize', 28);
  view.innerHTML = `
    <div class="reader-bar">
      <button class="icon-btn raised" id="fsMinus" aria-label="تصغير الخط">${icons.minus}</button>
      <button class="icon-btn raised" id="fsPlus" aria-label="تكبير الخط">${icons.plus}</button>
      <span class="spacer"></span>
      <button class="btn" id="listenAll">${icons.headphones} استمع للسورة</button>
    </div>
    <article class="mushaf-page" style="--qsize:${size}px">
      <header class="surah-banner"><h2>سورة ${surahName(n)}</h2><small>${surahSub(n)} · الجزء ${arNum(ayahs[0].juz)} · الصفحة ${arNum(ayahs[0].page)}</small></header>
      ${n !== 1 && n !== 9 ? '<div class="basmala">بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ</div>' : ''}
      <p class="quran-text">${ayahs.map((a, i) => `<span class="ayah ${marks.has(a.numberInSurah) ? 'marked' : ''}" data-s="${n}" data-i="${i}" id="a${a.numberInSurah}">${a.text}<span class="end">۝${arNum(a.numberInSurah)}</span></span> `).join('')}</p>
    </article>
    <div class="reader-nav">
      ${n > 1 ? `<a class="btn ghost" href="#/mushaf/${n - 1}">${icons.chevron.replace('<svg', '<svg style="transform:scaleX(-1)"')} ${surahName(n - 1)}</a>` : '<span></span>'}
      ${n < 114 ? `<a class="btn ghost" href="#/mushaf/${n + 1}">${surahName(n + 1)} ${icons.chevron}</a>` : ''}
    </div>`;

  const page = $('.mushaf-page');
  const setSize = d => {
    const v = Math.min(44, Math.max(18, store.get('qsize', 28) + d));
    store.set('qsize', v);
    page.style.setProperty('--qsize', v + 'px');
  };
  $('#fsMinus').onclick = () => setSize(-2);
  $('#fsPlus').onclick = () => setSize(2);
  $('#listenAll').onclick = () => playAyahs(n, ayahs, 0);
  page.onclick = e => {
    const el = e.target.closest('.ayah');
    if (el) ayahSheet(n, ayahs, +el.dataset.i, el);
  };

  const prevRead = store.get('lastRead', {});
  store.set('lastRead', { s: n, a: goto || (prevRead.s === n ? prevRead.a : 1) });
  if (goto) {
    const el = $('#a' + goto);
    if (el) setTimeout(() => { el.scrollIntoView({ block: 'center' }); el.classList.add('flash'); }, 60);
  }
  if (player.mode === 'ayah' && player.surah === n) highlightAyah();

  // حفظ موضع القراءة تلقائيًا أثناء التمرير
  const io = new IntersectionObserver(entries => {
    const vis = entries.filter(e => e.isIntersecting).map(e => +e.target.id.slice(1));
    if (vis.length) store.set('lastRead', { s: n, a: Math.min(...vis) });
  }, { rootMargin: '-30% 0px -60% 0px' });
  $$('.ayah').forEach(el => io.observe(el));
  const onChange = () => highlightAyah();
  pEvents.addEventListener('change', onChange);
  ctx.cleanup(() => { io.disconnect(); pEvents.removeEventListener('change', onChange); });
}

function ayahSheet(n, ayahs, i, el) {
  const a = ayahs[i];
  const marked = bookmarks().some(b => b.s === n && b.a === a.numberInSurah);
  const ref = `[سورة ${surahName(n)}: ${a.numberInSurah}]`;
  sheet(`
    <div class="sheet-head"><h3>سورة ${surahName(n)} · الآية ${arNum(a.numberInSurah)}</h3>
    <small class="muted">الصفحة ${arNum(a.page)} · الجزء ${arNum(a.juz)}${a.sajda ? ' · موضع سجدة' : ''}</small></div>
    <div class="sheet-actions">
      <button data-a="play">${icons.play}<span>استمع من هنا</span></button>
      <button data-a="tafsir">${icons.book}<span>التفسير</span></button>
      <button data-a="mark">${icons.bookmark}<span>${marked ? 'إزالة العلامة' : 'حفظ علامة'}</span></button>
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
      if (act === 'mark') {
        const on = toggleBookmark(n, a.numberInSurah);
        el.classList.toggle('marked', on);
        toast(on ? 'تم حفظ العلامة' : 'أُزيلت العلامة');
        close();
      }
      if (act === 'copy') { copyText(`﴿${a.text}﴾ ${ref}`); close(); }
      if (act === 'share') { shareText(`﴿${a.text}﴾ ${ref}`); close(); }
      if (act === 'tafsir') {
        const box = $('#tafsirBox');
        box.innerHTML = '<div class="loader small"><div class="spinner"></div></div>';
        try {
          const { data } = await fetchJSON(`https://api.alquran.cloud/v1/ayah/${a.number}/ar.muyassar`);
          box.innerHTML = `<div class="tafsir"><b>التفسير الميسّر</b><p>${esc(data.text)}</p><small>إعداد نخبة من العلماء — مجمع الملك فهد لطباعة المصحف الشريف</small></div>`;
        } catch { box.innerHTML = '<div class="empty">تعذّر تحميل التفسير.</div>'; }
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
