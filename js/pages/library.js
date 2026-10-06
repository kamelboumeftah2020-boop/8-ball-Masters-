// المكتبة: كتب العلماء (PDF من IslamHouse) تُقرأ داخل التطبيق أو تُحمَّل للقراءة دون اتصال، وقصص الأنبياء مكتوبة
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, icons, sheet } from '../core.js';
import * as dl from '../downloads.js';
import { dlButton, bindDlButtons } from './downloads.js';
import { ANBIYA } from '../data/anbiya.js';

let libData = null;
export function getLibrary() {
  if (!libData) {
    libData = fetchJSON('data/library.json').then(d => {
      // [المعرّف، العنوان، المؤلف، التصنيف، الوصف، الملفات] ← كائنات، مع نص مُطبَّع للبحث
      d.books = d.b.map(([id, title, author, cat, desc, files]) => ({ id, title, author, cat, desc, files, n: normalize(`${title} ${author}`) }));
      d.byId = new Map(d.books.map(b => [b.id, b]));
      return d;
    }).catch(e => { libData = null; throw e; });
  }
  return libData;
}

const sizeLabel = kb => kb >= 1024 ? `${arNum(+(kb / 1024).toFixed(1))} م.ب` : `${arNum(Math.max(1, kb))} ك.ب`;
const bookSize = b => b.files.reduce((s, f) => s + f[1], 0);
const fileTitle = (b, i) => b.files[i][2] || (b.files.length > 1 ? `الجزء ${arNum(i + 1)}` : b.title);

function downloadBook(b, i) {
  const f = b.files[i];
  return dl.enqueue(f[0], [f[0]], { kind: 'book', ext: 'pdf', title: b.files.length > 1 ? `${b.title} — ${fileTitle(b, i)}` : b.title, sub: b.author, ref: { id: b.id, i } });
}

const bookRow = b => `<a class="row" href="#/library/b/${b.id}">
    <span class="tile-ic sm ${b.cat === 'anbiya' ? 'gold' : ''}">${icons.book}</span>
    <span class="meta"><strong>${esc(b.title)}</strong><small>${esc(b.author)} · ${sizeLabel(bookSize(b))}${b.files.length > 1 ? ` · ${arNum(b.files.length)} ملفات` : ''}</small></span>
    ${b.files.some(f => dl.isDownloaded(f[0])) ? `<span class="badge-dl">${icons.check}</span>` : `<span class="chev">${icons.chevron}</span>`}
  </a>`;

export async function renderLibrary(view, args, ctx) {
  if (args[0] === 'read') return renderReader(view, +args[1], +(args[2] || 0), ctx);
  view.innerHTML = '<div class="loader"><div class="spinner"></div>جارٍ فتح المكتبة…</div>';
  const data = await getLibrary().catch(() => null);
  if (!ctx.alive()) return;
  if (!data) { view.innerHTML = '<div class="error-box">تعذّر فتح المكتبة.</div>'; return; }
  if (args[0] === 'b') return renderBook(view, data, +args[1], ctx);
  if (args[0] === 'c') return renderCat(view, data, args[1], ctx);
  if (args[0] === 'mine') return renderMine(view, data, ctx);
  return renderIndex(view, data, ctx);
}

function renderIndex(view, data, ctx) {
  const counts = {};
  data.books.forEach(b => { counts[b.cat] = (counts[b.cat] || 0) + 1; });
  const mine = dl.list().filter(d => d.kind === 'book').length;
  const last = store.get('lastBook');
  const lastBook = last && data.byId.get(last.id);
  view.innerHTML = `
    <a class="card promo anbiya-promo" href="#/stories">
      <span class="tile-ic gold">${icons.scroll}</span>
      <span><small>مكتوبة داخل التطبيق</small><b>قصص الأنبياء</b><small>${arNum(ANBIYA.length)} قصة من القرآن والسنة الصحيحة</small></span>
      <span class="chev">${icons.chevron}</span>
    </a>
    ${lastBook ? `<a class="card continue block" href="#/library/read/${lastBook.id}/${last.i || 0}">
      <span class="tile-ic">${icons.book}</span>
      <div><small class="muted">تابع القراءة · صفحة ${arNum(last.p || 1)}</small><strong>${esc(lastBook.title)}</strong><small class="muted">${esc(lastBook.author)}</small></div>
      <span class="chev">${icons.chevron}</span>
    </a>` : ''}
    <label class="search">${icons.search}<input id="lq" type="search" placeholder="ابحث في ${arNum(data.books.length)} كتابًا بالعنوان أو المؤلف" autocomplete="off"></label>
    <div id="lres"></div>
    <div id="lmain">
      <div class="list-card">
        <a class="row" href="#/library/mine"><span class="tile-ic sm gold">${icons.download}</span><span class="meta"><strong>كتبي المحمّلة</strong><small>${mine ? `${arNum(mine)} ملفًا للقراءة دون إنترنت` : 'حمّل الكتب لتقرأها دون إنترنت'}</small></span><span class="chev">${icons.chevron}</span></a>
      </div>
      <div class="section-head"><h2>الأقسام</h2></div>
      <div class="cat-grid">${data.cats.filter(c => counts[c[0]]).map(([id, name], i) => `
        <a class="card cat-card" href="#/library/c/${id}"><span class="tile-ic sm ${i % 2 ? 'gold' : ''}">${icons[CAT_ICONS[id] || 'book']}</span><b>${esc(name)}</b><small>${arNum(counts[id])} كتابًا</small></a>`).join('')}
      </div>
    </div>
    <p class="source-note">${icons.info} الكتب من موقع <b>IslamHouse</b> (دار الإسلام)، مختارة من مؤلفات العلماء المعروفين بمنهج أهل السنة والجماعة (كابن تيمية وابن القيم وابن كثير والسعدي وابن باز والعثيمين والألباني والفوزان وغيرهم)، وتُقرأ داخل التطبيق.</p>`;
  let deb;
  $('#lq').oninput = () => {
    clearTimeout(deb);
    deb = setTimeout(() => {
      const q = normalize($('#lq').value.trim());
      $('#lmain').hidden = q.length >= 2;
      if (q.length < 2) { $('#lres').innerHTML = ''; return; }
      const words = q.split(' ');
      const res = data.books.filter(b => words.every(w => b.n.includes(w)));
      $('#lres').innerHTML = res.length
        ? `<p class="muted count">${arNum(res.length)} نتيجة</p><div class="list-card">${res.slice(0, 80).map(bookRow).join('')}</div>`
        : '<div class="empty">لا توجد نتائج</div>';
    }, 250);
  };
  ctx.cleanup(() => clearTimeout(deb));
}

const CAT_ICONS = { anbiya: 'scroll', aqida: 'star', quran: 'book', hadith: 'scroll', fiqh: 'mosque', raqaiq: 'heart', usra: 'home', other: 'layers' };

function renderCat(view, data, id, ctx) {
  ctx.back('#/library');
  const cat = data.cats.find(c => c[0] === id);
  if (!cat) { location.hash = '#/library'; return; }
  ctx.title(cat[1]);
  const all = data.books.filter(b => b.cat === id);
  const authors = [...new Set(all.map(b => b.author))];
  let author = '', limit = 40;
  view.innerHTML = `
    <label class="search">${icons.search}<input id="cq" type="search" placeholder="ابحث في ${esc(cat[1])}" autocomplete="off"></label>
    ${authors.length > 1 ? `<select class="select hd-select" id="csel" aria-label="المؤلف"><option value="">كل المؤلفين (${arNum(all.length)})</option>${authors.map(a => `<option value="${esc(a)}">${esc(a)} (${arNum(all.filter(b => b.author === a).length)})</option>`).join('')}</select>` : ''}
    <div id="clist"></div>`;
  const draw = () => {
    const q = normalize($('#cq').value.trim());
    const items = all.filter(b => (!author || b.author === author) && (q.length < 2 || b.n.includes(q)));
    $('#clist').innerHTML = (items.length ? `<div class="list-card">${items.slice(0, limit).map(bookRow).join('')}</div>` : '<div class="empty">لا توجد نتائج</div>')
      + (items.length > limit ? `<button class="btn ghost block" id="cmore">عرض المزيد (${arNum(items.length - limit)})</button>` : '');
    $('#cmore')?.addEventListener('click', () => { limit += 60; draw(); });
  };
  $('#cq').oninput = () => { limit = 40; draw(); };
  $('#csel')?.addEventListener('change', e => { author = e.target.value; limit = 40; draw(); });
  draw();
}

function renderMine(view, data, ctx) {
  ctx.back('#/library');
  ctx.title('كتبي المحمّلة');
  const draw = () => {
    const items = dl.list().filter(d => d.kind === 'book');
    view.innerHTML = items.length
      ? `<div class="list-card">${items.map(d => `<a class="row" href="#/library/read/${d.ref.id}/${d.ref.i}">
          <span class="tile-ic sm gold">${icons.book}</span>
          <span class="meta"><strong>${esc(d.title)}</strong><small>${esc(d.sub)} · ${dl.fmtSize(d.size)}</small></span>
          ${dlButton(d.key)}</a>`).join('')}</div>`
      : '<div class="empty">لم تحمّل أي كتاب بعد.<br>افتح أي كتاب واضغط «تحميل» لتقرأه دون إنترنت.</div>';
  };
  draw();
  bindDlButtons(view, ctx, () => false);
  dl.events.addEventListener('change', draw);
  ctx.cleanup(() => dl.events.removeEventListener('change', draw));
}

function renderBook(view, data, id, ctx) {
  ctx.back('history');
  const b = data.byId.get(id);
  if (!b) { view.innerHTML = '<div class="error-box">الكتاب غير موجود.</div>'; return; }
  ctx.title('كتاب');
  const cat = data.cats.find(c => c[0] === b.cat);
  view.innerHTML = `
    <div class="card book-head">
      <span class="book-cover">${icons.book}</span>
      <h2>${esc(b.title)}</h2>
      <p class="muted">${esc(b.author)}</p>
      <div class="book-tags"><a href="#/library/c/${b.cat}">${esc(cat[1])}</a><span>${sizeLabel(bookSize(b))}</span><span>PDF</span></div>
      ${b.desc ? `<p class="book-desc">${esc(b.desc)}</p>` : ''}
    </div>
    <div class="section-head"><h2>${b.files.length > 1 ? `الملفات (${arNum(b.files.length)})` : 'الكتاب'}</h2></div>
    <div class="list-card" id="bfiles">${b.files.map((f, i) => `
      <div class="row">
        <a class="meta" href="#/library/read/${b.id}/${i}"><strong>${esc(fileTitle(b, i))}</strong><small>${sizeLabel(f[1])} · اضغط للقراءة</small></a>
        <a class="btn sm" href="#/library/read/${b.id}/${i}">قراءة</a>
        ${dlButton(f[0])}
      </div>`).join('')}
    </div>
    <p class="source-note">${icons.info} يمكنك القراءة مباشرة مع الاتصال بالإنترنت، أو تحميل الكتاب ${icons.download.replace('<svg', '<svg style="width:15px;height:15px;vertical-align:-3px"')} لقراءته دون اتصال.</p>`;
  bindDlButtons($('#bfiles'), ctx, key => {
    const i = b.files.findIndex(f => f[0] === key);
    return i >= 0 && downloadBook(b, i);
  });
}

/* ── قارئ PDF (pdf.js) ── */
let pdfjsReady = null;
function loadPdfJs() {
  if (!pdfjsReady) {
    pdfjsReady = new Promise((resolve, reject) => {
      const s = document.createElement('script');
      s.src = 'js/vendor/pdf.min.js';
      s.onload = () => {
        window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'js/vendor/pdf.worker.min.js';
        resolve(window.pdfjsLib);
      };
      s.onerror = () => { pdfjsReady = null; s.remove(); reject(new Error('pdf.js')); };
      document.head.append(s);
    });
  }
  return pdfjsReady;
}

async function renderReader(view, id, fi, ctx) {
  ctx.back('history');
  view.innerHTML = '<div class="loader"><div class="spinner"></div>جارٍ فتح الكتاب…</div>';
  const data = await getLibrary().catch(() => null);
  const b = data?.byId.get(id);
  if (!ctx.alive()) return;
  if (!b || !b.files[fi]) { view.innerHTML = '<div class="error-box">الكتاب غير موجود.</div>'; return; }
  ctx.title(b.title);
  const url = b.files[fi][0];
  const local = await dl.localSource(url);
  if (!local && !navigator.onLine) {
    view.innerHTML = '<div class="error-box">هذا الكتاب غير محمّل، والجهاز غير متصل بالإنترنت.<br>حمّل الكتاب عند الاتصال لتقرأه دون إنترنت.</div>';
    return;
  }
  let pdf;
  try {
    const lib = await loadPdfJs();
    const task = lib.getDocument({
      url: local || url,
      cMapUrl: 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/cmaps/', cMapPacked: true,
      standardFontDataUrl: 'https://cdn.jsdelivr.net/npm/pdfjs-dist@3.11.174/standard_fonts/',
      disableAutoFetch: true, isEvalSupported: false,
    });
    ctx.cleanup(() => task.destroy());
    if (!local) {
      task.onProgress = p => {
        const el = $('.loader small', view);
        if (el && p.total) el.textContent = `${arNum(Math.round(p.loaded / p.total * 100))}٪`;
      };
      $('.loader', view).insertAdjacentHTML('beforeend', '<small></small>');
    }
    pdf = await task.promise;
  } catch {
    if (ctx.alive()) view.innerHTML = '<div class="error-box">تعذّر فتح الكتاب. تحقق من الاتصال ثم أعد المحاولة.</div>';
    return;
  }
  if (!ctx.alive()) return;

  const key = `${id}:${fi}`;
  const positions = store.get('bookPos', {});
  const total = pdf.numPages;
  let current = Math.min(positions[key] || 1, total);
  let zoom = store.get('bookZoom', 1);
  let night = store.get('bookNight', false);
  const first = await pdf.getPage(1);
  const vp1 = first.getViewport({ scale: 1 });
  const ratio = vp1.height / vp1.width;

  view.innerHTML = `
    <div class="pdf-wrap ${night ? 'night' : ''}" id="pdfWrap">
      <div class="pdf-pages" id="pdfPages" style="--z:${zoom}">${Array.from({ length: total }, (_, i) => `<div class="pdf-page" data-p="${i + 1}" style="aspect-ratio:${1 / ratio}"><span class="pdf-num">${arNum(i + 1)}</span></div>`).join('')}</div>
    </div>
    <div class="pdf-bar">
      <button class="icon-btn sm" id="pzOut" aria-label="تصغير">${icons.minus}</button>
      <button class="pdf-count" id="pGo">صفحة <b id="pCur">${arNum(current)}</b> من ${arNum(total)}</button>
      <button class="icon-btn sm" id="pzIn" aria-label="تكبير">${icons.plus}</button>
      <button class="icon-btn sm ${night ? 'on' : ''}" id="pNight" aria-label="الوضع الليلي">${icons.moon}</button>
      ${local ? '' : `<span class="pdf-dl">${dlButton(url)}</span>`}
    </div>`;
  if (!local) bindDlButtons($('.pdf-bar'), ctx, () => downloadBook(b, fi));

  const pages = $$('.pdf-page', view);
  const rendered = new Map(); // رقم الصفحة ← مهمة الرسم
  async function draw(n) {
    if (rendered.has(n)) return;
    const holder = pages[n - 1];
    const job = { cancelled: false };
    rendered.set(n, job);
    try {
      const page = await pdf.getPage(n);
      if (job.cancelled) return;
      const base = page.getViewport({ scale: 1 });
      holder.style.aspectRatio = `${base.width / base.height}`;
      const cssW = holder.clientWidth || view.clientWidth;
      const scale = Math.min(cssW * (window.devicePixelRatio || 1) / base.width, 4);
      const vp = page.getViewport({ scale });
      const canvas = document.createElement('canvas');
      canvas.width = Math.floor(vp.width); canvas.height = Math.floor(vp.height);
      job.task = page.render({ canvasContext: canvas.getContext('2d'), viewport: vp });
      await job.task.promise;
      if (job.cancelled) return;
      holder.querySelector('canvas')?.remove();
      holder.append(canvas);
      holder.classList.add('ready');
    } catch {
      rendered.delete(n);
    }
  }
  function release(n) {
    const job = rendered.get(n);
    if (!job) return;
    job.cancelled = true;
    job.task?.cancel();
    rendered.delete(n);
    const c = pages[n - 1].querySelector('canvas');
    if (c) { c.width = c.height = 0; c.remove(); }
    pages[n - 1].classList.remove('ready');
  }
  // نرسم الصفحات القريبة من الظاهرة فقط، ونحرر البعيدة لتوفير الذاكرة
  const visible = new Set();
  const io = new IntersectionObserver(entries => {
    for (const e of entries) {
      const n = +e.target.dataset.p;
      if (e.isIntersecting) visible.add(n); else visible.delete(n);
    }
    if (!visible.size) return;
    const top = Math.min(...visible);
    if (top !== current) {
      current = top;
      $('#pCur').textContent = arNum(current);
      positions[key] = current;
      store.set('bookPos', positions);
      store.set('lastBook', { id, i: fi, p: current });
    }
    for (let n = Math.max(1, top - 1); n <= Math.min(total, Math.max(...visible) + 1); n++) draw(n);
    for (const n of [...rendered.keys()]) if (n < top - 3 || n > Math.max(...visible) + 3) release(n);
  }, { rootMargin: '600px 0px' });
  pages.forEach(p => io.observe(p));
  ctx.cleanup(() => { io.disconnect(); [...rendered.keys()].forEach(release); pdf.destroy(); });
  store.set('lastBook', { id, i: fi, p: current });
  if (current > 1) requestAnimationFrame(() => pages[current - 1].scrollIntoView());

  const redraw = () => {
    $('#pdfPages').style.setProperty('--z', zoom);
    store.set('bookZoom', zoom);
    [...rendered.keys()].forEach(release);
    requestAnimationFrame(() => { pages[current - 1].scrollIntoView(); [...visible].forEach(draw); });
  };
  $('#pzIn').onclick = () => { if (zoom < 2.5) { zoom = +(zoom + .25).toFixed(2); redraw(); } };
  $('#pzOut').onclick = () => { if (zoom > 1) { zoom = +(zoom - .25).toFixed(2); redraw(); } };
  $('#pNight').onclick = e => {
    night = !night;
    store.set('bookNight', night);
    $('#pdfWrap').classList.toggle('night', night);
    e.currentTarget.classList.toggle('on', night);
  };
  $('#pGo').onclick = () => {
    const { el, close } = sheet(`
      <div class="sheet-head"><h3>الانتقال إلى صفحة</h3><small class="muted">من ١ إلى ${arNum(total)}</small></div>
      <input class="input" id="pIn" type="number" inputmode="numeric" min="1" max="${total}" value="${current}">
      <button class="btn block" id="pOk">انتقال</button>`, { label: 'الانتقال إلى صفحة' });
    const go = () => {
      const n = Math.round(+$('#pIn', el).value);
      if (n >= 1 && n <= total) { close(); pages[n - 1].scrollIntoView(); } else toast('رقم صفحة غير صحيح');
    };
    $('#pOk', el).onclick = go;
    $('#pIn', el).onkeydown = e => { if (e.key === 'Enter') go(); };
    setTimeout(() => $('#pIn', el)?.select(), 200);
  };
}

/* ── قصص الأنبياء (مكتوبة) ── */
export function renderStories(view, args, ctx) {
  const i = args[0] !== undefined ? +args[0] : -1;
  if (i < 0 || !ANBIYA[i]) {
    ctx.back('#/library');
    view.innerHTML = `
      <div class="note-box">${icons.info} قصص الأنبياء عليهم السلام كما جاءت في القرآن الكريم والسنة الصحيحة، دون الإسرائيليات التي لا أصل لها. وتحت كل قصة مواضعها في القرآن.</div>
      <div class="list-card">${ANBIYA.map((p, k) => `<a class="row" href="#/stories/${k}">
        <span class="num-badge">${arNum(k + 1)}</span>
        <span class="meta"><strong>${esc(p.name)}</strong><small>${esc(p.sub)}</small></span>
        ${store.get('storiesRead', []).includes(k) ? `<span class="badge-dl">${icons.check}</span>` : `<span class="chev">${icons.chevron}</span>`}
      </a>`).join('')}</div>
      <a class="card promo" href="#/library/c/anbiya"><span class="tile-ic">${icons.book}</span><span><small>للتوسع</small><b>كتب قصص الأنبياء والسيرة</b><small>كقصص الأنبياء لابن كثير، والرحيق المختوم</small></span><span class="chev">${icons.chevron}</span></a>`;
    return;
  }
  ctx.back('#/stories');
  const p = ANBIYA[i];
  ctx.title(p.name);
  const read = store.get('storiesRead', []);
  if (!read.includes(i)) store.set('storiesRead', [...read, i]);
  let size = store.get('storySize', 19);
  view.innerHTML = `
    <article class="card story" style="--fs:${size}px">
      <div class="story-head"><span class="tile-ic gold">${icons.scroll}</span><div><h2>${esc(p.name)}</h2><small class="muted">${esc(p.sub)}</small></div></div>
      ${p.body.map(par => par.startsWith('﴿') ? `<p class="story-ayah">${esc(par)}</p>` : par.startsWith('##') ? `<h3>${esc(par.slice(2).trim())}</h3>` : `<p>${esc(par)}</p>`).join('')}
      ${p.lessons?.length ? `<div class="story-lessons"><h3>من العبر</h3><ul>${p.lessons.map(l => `<li>${esc(l)}</li>`).join('')}</ul></div>` : ''}
      <p class="story-refs">${icons.book} ${esc(p.refs)}</p>
      <div class="story-tools">
        <button class="icon-btn sm" id="sMinus" aria-label="تصغير الخط">${icons.minus}</button>
        <button class="icon-btn sm" id="sPlus" aria-label="تكبير الخط">${icons.plus}</button>
      </div>
    </article>
    <div class="row2 story-nav">
      ${i > 0 ? `<a class="btn ghost" href="#/stories/${i - 1}">${esc(ANBIYA[i - 1].name)}</a>` : '<span></span>'}
      ${i < ANBIYA.length - 1 ? `<a class="btn" href="#/stories/${i + 1}">${esc(ANBIYA[i + 1].name)}</a>` : '<span></span>'}
    </div>`;
  const setSize = d => {
    size = Math.max(15, Math.min(28, size + d));
    store.set('storySize', size);
    $('.story').style.setProperty('--fs', `${size}px`);
  };
  $('#sMinus').onclick = () => setSize(-1);
  $('#sPlus').onclick = () => setSize(1);
}
