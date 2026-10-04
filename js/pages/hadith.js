// كتب الحديث دون إنترنت: الأربعون النووية ورياض الصالحين، مع البحث والحفظ والمشاركة
import { $, $$, store, arNum, esc, normalize, toast, fetchJSON, copyText, shareText, icons } from '../core.js';

export const BOOKS = [
  { id: 'nawawi', name: 'الأربعون النووية', author: 'الإمام النووي', note: 'اثنان وأربعون حديثًا جامعًا من قواعد الدين، اختارها الإمام النووي رحمه الله.' },
  { id: 'riyad', name: 'رياض الصالحين', author: 'الإمام النووي', note: 'مختارات من الأحاديث في الآداب والرقائق والأذكار، مع ذكر من رواها. وأغلب أحاديثه صحيحة، ونبّه أهل العلم على أحاديث يسيرة فيه ضعيفة؛ فانظر إلى تخريج كل حديث المذكور في آخره.' },
];
const cache = new Map();
const getBook = id => {
  if (!cache.has(id)) cache.set(id, fetchJSON(`data/hadith/${id}.json`).catch(e => { cache.delete(id); throw e; }));
  return cache.get(id);
};
const favs = () => store.get('hadithFav', []);
const isFav = k => favs().includes(k);
function toggleFav(k) {
  const f = favs();
  const on = !f.includes(k);
  store.set('hadithFav', on ? [k, ...f] : f.filter(x => x !== k));
  return on;
}

const card = (book, h) => {
  const k = `${book.id}:${h[0]}`;
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
    const el = b.closest('.hd-card');
    const k = el.dataset.k;
    const [book, h] = lookup(k);
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
  const book = BOOKS.find(b => b.id === id);
  if (!book) { location.hash = '#/hadith'; return; }
  ctx.title(book.name);
  view.innerHTML = '<div class="loader"><div class="spinner"></div></div>';
  const data = await getBook(id).catch(() => null);
  if (!ctx.alive()) return;
  if (!data) { view.innerHTML = '<div class="error-box">تعذّر فتح الكتاب.</div>'; return; }
  let chapter = data.c.length > 1 ? store.get(`hadithCh:${id}`, data.c[0][0]) : null;
  let limit = 30;
  view.innerHTML = `
    <div class="note-box">${icons.info} ${book.note}</div>
    <label class="search">${icons.search}<input id="hq" type="search" placeholder="ابحث في ${esc(book.name)}" autocomplete="off"></label>
    ${data.c.length > 1 ? `<div class="chips" id="hch">${data.c.map(c => `<button data-c="${c[0]}">${esc(c[1].replace(/^كتاب\s*/, ''))}</button>`).join('')}</div>` : ''}
    <div id="hlist"></div>`;
  const list = $('#hlist');
  const draw = () => {
    const q = normalize($('#hq').value.trim());
    let items = data.h;
    if (q.length >= 2) items = items.filter(h => normalize(h[2]).includes(q));
    else if (chapter !== null) items = items.filter(h => h[1] === chapter);
    $$('#hch button').forEach(b => b.classList.toggle('active', q.length < 2 && +b.dataset.c === chapter));
    const shown = items.slice(0, limit);
    list.innerHTML = (q.length >= 2 ? `<p class="muted count">${arNum(items.length)} نتيجة</p>` : '')
      + (shown.length ? shown.map(h => card(book, h)).join('') : '<div class="empty">لا توجد نتائج</div>')
      + (items.length > limit ? `<button class="btn ghost block" id="hmore">عرض المزيد (${arNum(items.length - limit)})</button>` : '');
    $('#hmore')?.addEventListener('click', () => { limit += 40; draw(); });
  };
  $('#hq').oninput = () => { limit = 30; draw(); };
  $('#hch')?.addEventListener('click', e => {
    const b = e.target.closest('[data-c]');
    if (!b) return;
    chapter = +b.dataset.c; limit = 30;
    store.set(`hadithCh:${id}`, chapter);
    $('#hq').value = '';
    draw();
    window.scrollTo(0, 0);
  });
  bindCards(list, k => [book, data.h.find(h => `${id}:${h[0]}` === k)]);
  draw();
  $('#hch .active')?.scrollIntoView({ inline: 'center', block: 'nearest' });
}

function renderIndex(view, ctx) {
  const n = favs().length;
  view.innerHTML = `
    <div class="list-card">
      ${BOOKS.map(b => `<a class="row" href="#/hadith/${b.id}"><span class="tile-ic sm gold">${icons.scroll}</span><span class="meta"><strong>${b.name}</strong><small>${b.author}</small></span><span class="chev">${icons.chevron}</span></a>`).join('')}
      <a class="row" href="#/hadith/fav"><span class="tile-ic sm">${icons.starFill}</span><span class="meta"><strong>الأحاديث المحفوظة</strong><small>${n ? `${arNum(n)} حديثًا` : 'احفظ ما يعجبك بزر النجمة'}</small></span><span class="chev">${icons.chevron}</span></a>
    </div>
    <div class="note-box">${icons.info} الكتب مضمّنة في التطبيق وتعمل دون إنترنت. النصوص من مجموعة hadith-json المفتوحة.</div>`;
}

async function renderFav(view, ctx) {
  ctx.title('الأحاديث المحفوظة');
  const f = favs();
  const books = {};
  for (const b of BOOKS) if (f.some(k => k.startsWith(b.id + ':'))) books[b.id] = await getBook(b.id).catch(() => null);
  if (!ctx.alive()) return;
  const get = k => { const [bid, n] = k.split(':'); return [BOOKS.find(b => b.id === bid), books[bid]?.h.find(h => h[0] === +n)]; };
  const draw = () => {
    const items = favs().map(get).filter(([b, h]) => b && h);
    view.innerHTML = items.length ? `<div id="hlist">${items.map(([b, h]) => card(b, h)).join('')}</div>` : '<div class="empty">لا توجد أحاديث محفوظة بعد.<br>اضغط النجمة بجانب أي حديث لحفظه.</div>';
    const list = $('#hlist');
    if (list) bindCards(list, get, draw);
  };
  draw();
}
