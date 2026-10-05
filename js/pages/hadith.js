// كتب الحديث دون إنترنت: الصحيحان، والأربعون النووية، ورياض الصالحين؛ مع البحث والحفظ والمشاركة
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, copyText, shareText, icons } from '../core.js';

export const BOOKS = [
  { id: 'bukhari', split: true, name: 'صحيح البخاري', author: 'الإمام محمد بن إسماعيل البخاري', note: 'أصح كتاب بعد كتاب الله تعالى، وأحاديثه المسندة كلها صحيحة بإجماع أهل العلم. الترقيم بحسب هذه النسخة.' },
  { id: 'muslim', split: true, name: 'صحيح مسلم', author: 'الإمام مسلم بن الحجاج النيسابوري', note: 'ثاني الصحيحين، وأحاديثه المسندة صحيحة، وتلقّته الأمة بالقبول. الترقيم بحسب هذه النسخة.' },
  { id: 'nawawi', name: 'الأربعون النووية', author: 'الإمام النووي', note: 'اثنان وأربعون حديثًا جامعًا من قواعد الدين، اختارها الإمام النووي رحمه الله.' },
  { id: 'riyad', name: 'رياض الصالحين', author: 'الإمام النووي', note: 'مختارات من الأحاديث في الآداب والرقائق والأذكار، مع ذكر من رواها. وأغلب أحاديثه صحيحة، ونبّه أهل العلم على أحاديث يسيرة فيه ضعيفة؛ فانظر إلى تخريج كل حديث المذكور في آخره.' },
];
const bookById = id => BOOKS.find(b => b.id === id);

/* ── البيانات: الكتب الصغيرة ملف واحد، والصحيحان ملف لكل كتاب منهما ──
   الحديث داخل التطبيق: [الرقم، الكتاب/الباب، النص] */
const cache = new Map();
const once = (k, fn) => {
  if (!cache.has(k)) cache.set(k, fn().catch(e => { cache.delete(k); throw e; }));
  return cache.get(k);
};
const getIndex = id => once(`i:${id}`, () => fetchJSON(bookById(id).split ? `data/hadith/${id}/index.json` : `data/hadith/${id}.json`));
async function getChapter(id, ch) {
  if (!bookById(id).split) return (await getIndex(id)).h.filter(h => h[1] === ch);
  return once(`c:${id}:${ch}`, () => fetchJSON(`data/hadith/${id}/${ch}.json`).then(a => a.map(([n, t]) => [n, ch, t])));
}
// الكتاب كاملًا مع نص مُطبَّع للبحث (يُحمَّل عند أول بحث)
function getAll(id) {
  return once(`a:${id}`, async () => {
    const idx = await getIndex(id);
    const all = bookById(id).split ? (await Promise.all(idx.c.map(c => getChapter(id, c[0])))).flat() : idx.h;
    return all.map(h => [...h, normalize(h[2])]);
  });
}

const favs = () => store.get('hadithFav', []);
const isFav = k => favs().includes(k);
function toggleFav(k) {
  const f = favs();
  const on = !f.includes(k);
  store.set('hadithFav', on ? [k, ...f] : f.filter(x => x !== k));
  return on;
}
// مفتاح الحفظ: الكتاب والرقم، ومعهما رقم الكتاب الفرعي في الصحيحين ليُفتح دون تحميل الكتاب كله
const keyOf = (book, h) => `${book.id}:${h[0]}${book.split ? `:${h[1]}` : ''}`;

const card = (book, h) => {
  const k = keyOf(book, h);
  return `<article class="card hd-card" data-k="${k}">
    <div class="hd-top"><span class="hd-num">${arNum(h[0])}</span><small>${esc(book.name)}</small></div>
    <p class="hd-text">${esc(h[2]).replace(/\n/g, '<br>')}</p>
    <div class="hd-actions">
      <button class="icon-btn sm ${isFav(k) ? 'on' : ''}" data-a="fav" aria-label="حفظ">${isFav(k) ? icons.starFill : icons.star2}</button>
      <button class="icon-btn sm" data-a="copy" aria-label="نسخ">${icons.copy}</button>
      <button class="icon-btn sm" data-a="share" aria-label="مشاركة">${icons.share}</button>
    </div></article>`;
};

function bindCards(root, lookup, onFavChange) {
  root.addEventListener('click', e => {
    const b = e.target.closest('[data-a]');
    if (!b) return;
    const k = b.closest('.hd-card').dataset.k;
    const [book, h] = lookup(k);
    if (!h) return;
    const text = `${h[2]}\n\n— ${book.name}، رقم ${h[0]}`;
    if (b.dataset.a === 'copy') copyText(text);
    if (b.dataset.a === 'share') shareText(text);
    if (b.dataset.a === 'fav') {
      const on = toggleFav(k);
      b.classList.toggle('on', on);
      b.innerHTML = on ? icons.starFill : icons.star2;
      toast(on ? 'حُفظ الحديث' : 'أُزيل من المحفوظات');
      onFavChange?.();
    }
  });
}

export async function renderHadith(view, args, ctx) {
  const id = args[0];
  if (!id) return renderIndex(view, ctx);
  ctx.back('#/hadith');
  if (id === 'fav') return renderFav(view, ctx);
  const book = bookById(id);
  if (!book) { location.hash = '#/hadith'; return; }
  ctx.title(book.name);
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  const idx = await getIndex(id).catch(() => null);
  if (!ctx.alive()) return;
  if (!idx) { view.innerHTML = '<div class="error-box">تعذّر فتح الكتاب.</div>'; return; }
  const chapters = idx.c;
  const many = chapters.length > 20;
  let chapter = chapters.length > 1 ? store.get(`hadithCh:${id}`, chapters[0][0]) : chapters[0][0];
  if (!chapters.some(c => c[0] === chapter)) chapter = chapters[0][0];
  let limit = 30, shown = [];
  view.innerHTML = `
    <div class="note-box">${icons.info} ${book.note}</div>
    <label class="search">${icons.search}<input id="hq" type="search" placeholder="ابحث في ${esc(book.name)}" autocomplete="off"></label>
    ${many ? `<select class="select hd-select" id="hsel" aria-label="الكتاب">${chapters.map(c => `<option value="${c[0]}" ${c[0] === chapter ? 'selected' : ''}>${esc(c[1])}${c[2] ? ` (${arNum(c[2])})` : ''}</option>`).join('')}</select>`
      : chapters.length > 1 ? `<div class="chips" id="hch">${chapters.map(c => `<button data-c="${c[0]}">${esc(c[1].replace(/^كتاب\s*/, ''))}</button>`).join('')}</div>` : ''}
    <div id="hlist"></div>`;
  const list = $('#hlist');
  let token = 0;
  const draw = async () => {
    const my = ++token;
    const q = normalize($('#hq').value.trim());
    let items, count = '';
    if (q.length >= 2) {
      if (book.split && !cache.has(`a:${id}`)) list.innerHTML = '<div class="loader small"><div class="spinner"></div>جارٍ البحث في الكتاب كله…</div>';
      const all = await getAll(id);
      if (my !== token || !ctx.alive()) return;
      items = all.filter(h => h[3].includes(q));
      count = `<p class="muted count">${arNum(items.length)} نتيجة</p>`;
    } else {
      items = await getChapter(id, chapter);
      if (my !== token || !ctx.alive()) return;
    }
    $$('#hch button').forEach(b => b.classList.toggle('active', q.length < 2 && +b.dataset.c === chapter));
    shown = items;
    list.innerHTML = count
      + (items.length ? items.slice(0, limit).map(h => card(book, h)).join('') : '<div class="empty">لا توجد نتائج</div>')
      + (items.length > limit ? `<button class="btn ghost block" id="hmore">عرض المزيد (${arNum(items.length - limit)})</button>` : '');
    $('#hmore')?.addEventListener('click', () => { limit += 40; draw(); });
  };
  let deb;
  $('#hq').oninput = () => { limit = 30; clearTimeout(deb); deb = setTimeout(draw, 300); };
  ctx.cleanup(() => clearTimeout(deb));
  const pick = c => {
    chapter = c; limit = 30;
    store.set(`hadithCh:${id}`, chapter);
    $('#hq').value = '';
    draw();
    window.scrollTo(0, 0);
  };
  $('#hch')?.addEventListener('click', e => { const b = e.target.closest('[data-c]'); if (b) pick(+b.dataset.c); });
  $('#hsel')?.addEventListener('change', e => pick(+e.target.value));
  bindCards(list, k => [book, shown.find(h => keyOf(book, h) === k)]);
  await draw();
  $('#hch .active')?.scrollIntoView({ inline: 'center', block: 'nearest' });
}

function renderIndex(view, ctx) {
  const n = favs().length;
  view.innerHTML = `
    <div class="list-card">
      ${BOOKS.map(b => `<a class="row" href="#/hadith/${b.id}"><span class="tile-ic sm gold">${icons.scroll}</span><span class="meta"><strong>${b.name}</strong><small>${b.author}</small></span><span class="chev">${icons.chevron}</span></a>`).join('')}
      <a class="row" href="#/hadith/fav"><span class="tile-ic sm">${icons.starFill}</span><span class="meta"><strong>الأحاديث المحفوظة</strong><small>${n ? `${arNum(n)} حديثًا` : 'احفظ ما يعجبك بزر النجمة'}</small></span><span class="chev">${icons.chevron}</span></a>
    </div>
    <div class="note-box">${icons.info} الكتب مضمّنة في التطبيق وتعمل دون إنترنت، بأسانيدها كما في أصولها. النصوص من مجموعة hadith-json المفتوحة.</div>`;
}

async function renderFav(view, ctx) {
  ctx.title('الأحاديث المحفوظة');
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  // نحمّل الكتب الصغيرة كاملة، ومن الصحيحين الكتب الفرعية المحفوظ منها فقط
  const found = new Map();
  await Promise.all(favs().map(async k => {
    const [bid, n, ch] = k.split(':');
    const book = bookById(bid);
    if (!book) return;
    try {
      const items = book.split ? await getChapter(bid, +ch) : (await getIndex(bid)).h;
      const h = items.find(x => x[0] === +n);
      if (h) found.set(k, [book, h]);
    } catch { /* لا شيء */ }
  }));
  if (!ctx.alive()) return;
  const get = k => found.get(k) || [];
  const draw = () => {
    const items = favs().map(get).filter(([b, h]) => b && h);
    view.innerHTML = items.length ? `<div id="hlist">${items.map(([b, h]) => card(b, h)).join('')}</div>` : '<div class="empty">لا توجد أحاديث محفوظة بعد.<br>اضغط النجمة بجانب أي حديث لحفظه.</div>';
    const list = $('#hlist');
    if (list) bindCards(list, get, draw);
  };
  draw();
}
