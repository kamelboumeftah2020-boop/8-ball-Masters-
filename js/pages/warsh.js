// مصحف ورش عن نافع — بخط مجمع الملك فهد، ملوّن للحفظ، مع وضع التسميع
import { $, $$, store, arNum, toast, fetchJSON, copyText, shareText, icons, sheet } from '../core.js';
import { surahName } from '../player.js';
import { immersive, keepAwake } from '../native.js';
import { trackPage } from '../khatma.js';
import { goToPageSheet } from './quran.js';
import { loadWarsh, download, WARSH_SIZE_MB } from '../warshData.js';

const BASMALA = 'بِسْمِ اِ۬للَّهِ اِ۬لرَّحْمَٰنِ اِ۬لرَّحِيمِ';
let indexP = null;

// فهرس صغير مضمّن (أول آية في كل صفحة، وصفحات السور والأجزاء) ليعمل الفهرس دون اتصال
export function getWarshIndex() {
  if (!indexP) indexP = fetchJSON('data/warsh-index.json').catch(e => { indexP = null; throw e; });
  return indexP;
}

export const warshMarks = () => store.get('warshMarks', []);
function toggleMark(p) {
  const list = warshMarks();
  const on = !list.some(x => x.p === p);
  store.set('warshMarks', on ? [{ p, t: Date.now() }, ...list] : list.filter(x => x.p !== p));
  return on;
}

// إعدادات العرض: التلوين للحفظ، ووضع التسميع (إخفاء الآيات)
const opts = () => ({ color: true, hide: false, ...store.get('warshOpts', {}) });
const setOpts = o => store.set('warshOpts', { ...opts(), ...o });

export async function renderWarsh(view, args, ctx) {
  const n = Math.min(604, Math.max(1, +args[0] || 1));
  ctx.back('#/mushaf');
  view.innerHTML = '<div class="qr-loading"><div class="spinner"></div></div>';
  let data, index;
  try {
    const [pages, idx] = await Promise.all([loadWarsh(), getWarshIndex()]);
    data = pages[n - 1]; index = idx;
  } catch {
    if (!ctx.alive()) return;
    // مصحف ورش يحتاج إلى الإنترنت ما لم يُحمَّل
    view.innerHTML = `<div class="error-box">مصحف ورش يُقرأ من الإنترنت، ويمكن تحميله (نحو ${arNum(WARSH_SIZE_MB)} م.ب) ليعمل دون اتصال.<br>تحقق من الاتصال ثم أعد المحاولة.<br><br>
      <button class="btn" id="retry">إعادة المحاولة</button> <button class="btn ghost" id="dlWarsh">تحميل المصحف</button></div>`;
    $('#retry').onclick = () => renderWarsh(view, args, ctx);
    $('#dlWarsh').onclick = () => download().then(() => { toast('حُفظ مصحف ورش في جهازك'); renderWarsh(view, args, ctx); }, () => toast('تعذّر التحميل، تحقق من الاتصال'));
    return;
  }
  if (!ctx.alive()) return;

  document.body.classList.add('reader-full');
  immersive(true);
  const release = keepAwake();
  ctx.cleanup(() => {
    setTimeout(() => {
      if (!/^#\/(page|warsh)\//.test(location.hash)) { document.body.classList.remove('reader-full'); immersive(false); }
    }, 0);
    release();
  });

  const [firstS, firstA] = index.p[n - 1];
  ctx.title('سورة ' + surahName(firstS));
  store.set('warshLast', { p: n, s: firstS, a: firstA });
  const marked = warshMarks().some(x => x.p === n);
  const lines = data.b.reduce((t, b) => t + (b[0] === 't' ? b[1] : 1), 0);
  const opening = lines < 15;
  // تنتهي الصفحة بنهاية سورة إن بدأت الصفحة التالية بأول سورة
  const endsSura = n === 604 || index.p[n][1] === 1;

  let k = 0; // ترقيم الآيات في الصفحة لتعاقب الألوان
  const blockHTML = (b, i) => {
    if (b[0] === 'h') return `<div class="ql qh wl"><span>سورة ${surahName(b[1])}</span></div>`;
    if (b[0] === 'b') return `<div class="ql qb wl">${BASMALA}</div>`;
    const next = data.b[i + 1];
    const last = next ? next[0] === 'h' : endsSura;
    const ayat = b[2].map(([s, a, t, end]) => {
      let body = t, num = '';
      if (end) { const j = t.lastIndexOf(' '); if (j > 0) { body = t.slice(0, j); num = t.slice(j + 1); } }
      return `<span class="wa c${k++ % 4}" data-k="${s}:${a}">${body}${num ? ` <span class="wn">${num}</span>` : ''}</span>`;
    }).join(' ');
    return `<div class="wt ${last ? 'last' : ''}" data-n="${b[1]}">${ayat}</div>`;
  };

  const o = opts();
  view.innerHTML = `
    <div class="qr wr ${opening ? 'opening' : ''}" id="qr">
      <header class="qr-head">
        <button class="qr-pill" id="qrIndex">سورة ${surahName(firstS)}</button>
        <span class="qr-pill wr-tag">ورش</span>
        <span class="qr-pill">الجزء ${arNum(data.j)}</span>
        <button class="qr-mark ${marked ? 'on' : ''}" id="qrMark" aria-label="علامة الصفحة">${icons.bookmark}</button>
      </header>
      <div class="qr-page wr-page ${o.color ? 'colored' : ''} ${o.hide ? 'hide' : ''}" id="qpage">${data.b.map(blockHTML).join('')}</div>
      <footer class="qr-foot"><span class="qr-pill qr-num">${arNum(n)}</span></footer>
      <div class="qr-tools wr-tools" id="qrTools" hidden>
        <button data-t="exit">${icons.book}<span>الفهرس</span></button>
        <button data-t="prev" ${n <= 1 ? 'disabled' : ''}>${icons.chevron.replace('<svg', '<svg style="transform:scaleX(-1)"')}<span>السابقة</span></button>
        <button data-t="color" class="${o.color ? 'on' : ''}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a9 9 0 1 0 0 18c1.1 0 1.6-.9 1.2-1.8l-.4-.9c-.4-1 .3-2.3 1.4-2.3H17a4 4 0 0 0 4-4c0-5-4-9-9-9Z"/><circle cx="7.5" cy="11" r="1.2"/><circle cx="10" cy="7" r="1.2"/><circle cx="14.5" cy="7" r="1.2"/></svg><span>الألوان</span></button>
        <button data-t="hide" class="${o.hide ? 'on' : ''}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3l18 18M10.6 5.1A10 10 0 0 1 12 5c6 0 9.5 7 9.5 7a17 17 0 0 1-3 3.8M6.6 6.6C3.9 8.3 2.5 12 2.5 12S6 19 12 19a9.7 9.7 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2"/></svg><span>التسميع</span></button>
        <button data-t="goto">${icons.layers}<span>صفحة</span></button>
        <button data-t="next" ${n >= 604 ? 'disabled' : ''}>${icons.chevron}<span>التالية</span></button>
      </div>
    </div>`;

  const qr = $('#qr'), page = $('#qpage');

  // ملاءمة الخط: كل كتلة تملأ عدد أسطرها في المصحف المطبوع دون أن تتجاوزه
  const fit = () => {
    const h = page.clientHeight, w = page.clientWidth;
    if (!h || !w) return;
    const lineH = h / (opening ? 12 : 15);
    page.style.setProperty('--qlh', lineH + 'px');
    const blocks = $$('.wt', page);
    const fits = () => blocks.every(b => b.scrollHeight <= +b.dataset.n * lineH + 2);
    let lo = lineH * 0.25, hi = lineH * 0.62;
    for (let i = 0; i < 12; i++) {
      const mid = (lo + hi) / 2;
      page.style.setProperty('--qfs', mid + 'px');
      if (fits()) lo = mid; else hi = mid;
    }
    page.style.setProperty('--qfs', lo + 'px');
  };
  fit();
  let lastSize = '';
  const ro = new ResizeObserver(() => {
    const size = page.clientWidth + 'x' + page.clientHeight;
    if (size !== lastSize) { lastSize = size; fit(); }
  });
  ro.observe(page);
  ctx.cleanup(() => ro.disconnect());

  // الختمة: تُحتسب الصفحة بالبقاء عليها أو بالانتقال منها إلى التالية
  const advance = trackPage(n, ctx);
  const go = (p, dir) => {
    if (p < 1 || p > 604 || p === n) return;
    if (p === n + 1) advance();
    page.classList.add(dir > 0 ? 'out-next' : 'out-prev');
    setTimeout(() => { if (ctx.alive()) location.replace(`#/warsh/${p}`); }, 140);
  };

  let hideTimer;
  const tools = $('#qrTools');
  const showTools = on => {
    clearTimeout(hideTimer);
    tools.hidden = !on;
    if (on) hideTimer = setTimeout(() => { tools.hidden = true; }, 5000);
  };
  ctx.cleanup(() => clearTimeout(hideTimer));

  page.onclick = e => {
    const el = e.target.closest('.wa');
    if (!el) return showTools(tools.hidden);
    // في وضع التسميع: اللمس يكشف الآية أو يخفيها
    if (page.classList.contains('hide')) {
      $$(`.wa[data-k="${el.dataset.k}"]`, page).forEach(x => x.classList.toggle('shown'));
      return;
    }
    const [s, a] = el.dataset.k.split(':').map(Number);
    const sel = $$(`.wa[data-k="${el.dataset.k}"]`, page);
    sel.forEach(x => x.classList.add('sel'));
    ayahSheet(s, a, n, sel.map(x => x.textContent).join(' ').trim(), () => sel.forEach(x => x.classList.remove('sel')));
  };
  $('#qrIndex').onclick = () => { location.hash = '#/mushaf'; };
  $('#qrMark').onclick = () => {
    const on = toggleMark(n);
    $('#qrMark').classList.toggle('on', on);
    toast(on ? `وُضعت العلامة عند الصفحة ${arNum(n)}` : 'أُزيلت العلامة');
  };
  tools.onclick = e => {
    const b = e.target.closest('[data-t]');
    if (!b) return;
    showTools(true);
    const t = b.dataset.t;
    if (t === 'exit') location.hash = '#/mushaf';
    if (t === 'prev') go(n - 1, -1);
    if (t === 'next') go(n + 1, 1);
    if (t === 'goto') goToPageSheet(n, '#/warsh/');
    if (t === 'color') {
      const on = !opts().color;
      setOpts({ color: on });
      page.classList.toggle('colored', on);
      b.classList.toggle('on', on);
      toast(on ? 'تلوين الآيات للحفظ' : 'دون تلوين');
    }
    if (t === 'hide') {
      const on = !opts().hide;
      setOpts({ hide: on });
      page.classList.toggle('hide', on);
      $$('.wa.shown', page).forEach(x => x.classList.remove('shown'));
      b.classList.toggle('on', on);
      toast(on ? 'وضع التسميع: المس الآية لكشفها' : 'أُظهرت الآيات');
    }
  };

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

function ayahSheet(s, a, page, text, onClose) {
  const ref = `[سورة ${surahName(s)}: ${a} — رواية ورش]`;
  const plain = text.replace(/ [٠-٩]+$/, '');
  sheet(`
    <div class="sheet-head"><h3>سورة ${surahName(s)} · الآية ${arNum(a)}</h3>
    <small class="muted">رواية ورش عن نافع · الصفحة ${arNum(page)}</small></div>
    <div class="sheet-actions">
      <button data-a="mark">${icons.bookmark}<span>${warshMarks().some(x => x.p === page) ? 'إزالة العلامة' : 'علامة الصفحة'}</span></button>
      <button data-a="copy">${icons.copy}<span>نسخ</span></button>
      <button data-a="share">${icons.share}<span>مشاركة</span></button>
    </div>`, {
    label: 'خيارات الآية',
    onClose,
    onClick: (e, close) => {
      const b = e.target.closest('[data-a]');
      if (!b) return;
      close();
      if (b.dataset.a === 'mark') $('#qrMark')?.click();
      if (b.dataset.a === 'copy') copyText(`﴿${plain}﴾ ${ref}`);
      if (b.dataset.a === 'share') shareText(`﴿${plain}﴾ ${ref}`);
    },
  });
}

