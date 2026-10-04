// صفحة التنزيلات، وزر التحميل المشترك في قوائم السور والمواعظ
import { $, $$, esc, arNum, toast, icons, sheet } from '../core.js';
import * as dl from '../downloads.js';
import { SURAHS } from '../data/surahs.js';
import { player, playSurah, playLecture, downloadSurah, reciterById, toggle, surahUrl } from '../player.js';
import { getLectures } from './mawaiz.js';

const RING = 2 * Math.PI * 9;

function stateHTML(key) {
  if (dl.isDownloaded(key)) return { cls: 'done', html: icons.check, label: 'محمّلة — اضغط للحذف' };
  const j = dl.job(key);
  if (j) {
    const p = j.total ? j.received / j.total : 0;
    return {
      cls: 'busy',
      html: `<svg viewBox="0 0 24 24" class="ring-p"><circle cx="12" cy="12" r="9" class="t"/><circle cx="12" cy="12" r="9" class="v" style="stroke-dasharray:${RING};stroke-dashoffset:${RING * (1 - p)}"/><path d="M9 9h6v6H9z" class="stop"/></svg>`,
      label: j.status === 'queued' ? 'في الانتظار — اضغط للإلغاء' : 'جارٍ التحميل — اضغط للإلغاء',
    };
  }
  return { cls: '', html: icons.download, label: 'تحميل للاستماع دون إنترنت' };
}

export function dlButton(key) {
  const s = stateHTML(key);
  return `<span class="dl-btn ${s.cls}" role="button" tabindex="0" data-dl="${esc(key)}" aria-label="${s.label}">${s.html}</span>`;
}

function refreshButtons(root = document) {
  $$('.dl-btn', root).forEach(b => {
    const s = stateHTML(b.dataset.dl);
    b.className = `dl-btn ${s.cls}`;
    b.innerHTML = s.html;
    b.setAttribute('aria-label', s.label);
  });
}

export function confirmSheet(title, text, okLabel, onOk) {
  const { el, close } = sheet(`
    <div class="sheet-head"><h3>${esc(title)}</h3></div>
    <p class="muted">${text}</p>
    <div class="row2"><button class="btn ghost" data-c="no">إلغاء</button><button class="btn danger" data-c="ok">${esc(okLabel)}</button></div>`, {
    label: title,
    onClick: (e, c) => {
      const b = e.target.closest('[data-c]');
      if (!b) return;
      c();
      if (b.dataset.c === 'ok') onOk();
    },
  });
  return { el, close };
}

/**
 * يربط أزرار التحميل داخل حاوية.
 * onStart(key): يبدأ التحميل (يعيد true إن أُضيف)
 */
export function bindDlButtons(container, ctx, onStart) {
  container.addEventListener('click', e => {
    const b = e.target.closest('.dl-btn');
    if (!b) return;
    e.stopPropagation();
    const key = b.dataset.dl;
    if (dl.isDownloaded(key)) {
      confirmSheet('حذف من التنزيلات؟', 'سيُحذف الملف من جهازك، ويمكنك تحميله مرة أخرى متى شئت.', 'حذف', () => dl.remove(key).then(() => toast('حُذف الملف')));
    } else if (dl.job(key)) {
      dl.cancel(key);
    } else if (onStart(key, b)) {
      toast('بدأ التحميل');
    }
    refreshButtons(container);
  }, true);
  const onChange = () => refreshButtons(container);
  dl.events.addEventListener('change', onChange);
  ctx.cleanup(() => dl.events.removeEventListener('change', onChange));
}

// زر صفحة التنزيلات مع العدد
export function downloadsLink() {
  const n = dl.list().length, active = dl.activeJobs().length;
  return `<a class="dl-link" href="#/downloads">${icons.download}<span>التنزيلات</span>${n || active ? `<b>${arNum(n)}${active ? ` · ${arNum(active)} جارٍ` : ''}</b>` : ''}</a>`;
}

/* ── صفحة التنزيلات ── */
export function renderDownloads(view, args, ctx) {
  ctx.back('history');
  view.innerHTML = '<div id="dlPage"></div>';
  const page = $('#dlPage');
  const draw = () => {
    const items = dl.list();
    const active = dl.activeJobs();
    const surahs = items.filter(d => d.kind === 'surah');
    const lectures = items.filter(d => d.kind === 'lecture');
    const row = d => `<div class="row dl-row" data-key="${esc(d.key)}">
        <button class="play-ic" data-act="play" aria-label="تشغيل">${icons.play}</button>
        <span class="meta"><strong>${esc(d.title)}</strong><small>${esc(d.sub)} · ${dl.fmtSize(d.size)}</small></span>
        <button class="icon-btn sm" data-act="del" aria-label="حذف">${icons.close}</button>
      </div>`;
    page.innerHTML = `
      <div class="card dl-summary">
        <span class="tile-ic">${icons.download}</span>
        <div><strong>${arNum(items.length)} مادة محمّلة</strong><small class="muted">المساحة المستعملة: ${dl.fmtSize(dl.totalSize())}</small></div>
        ${items.length ? `<button class="btn ghost" id="delAll">حذف الكل</button>` : ''}
      </div>
      ${active.length ? `<div class="section-head"><h2>جارٍ التحميل</h2></div>
        <div class="list-card">${active.map(j => `<div class="row dl-row">
          ${dlButton(j.key)}
          <span class="meta"><strong>${esc(j.meta.title)}</strong><small>${esc(j.meta.sub)} · ${j.status === 'queued' ? 'في الانتظار' : j.total ? `${dl.fmtSize(j.received)} من ${dl.fmtSize(j.total)}` : 'جارٍ التحميل…'}</small>
          <span class="mini-progress"><i style="width:${j.total ? j.received / j.total * 100 : 0}%"></i></span></span>
        </div>`).join('')}</div>` : ''}
      ${surahs.length ? `<div class="section-head"><h2>التلاوات</h2></div><div class="list-card">${surahs.map(row).join('')}</div>` : ''}
      ${lectures.length ? `<div class="section-head"><h2>المواعظ</h2></div><div class="list-card">${lectures.map(row).join('')}</div>` : ''}
      ${!items.length && !active.length ? `<div class="empty">لا توجد تنزيلات بعد.<br>اضغط زر التحميل ${icons.download.replace('<svg', '<svg style="width:16px;height:16px;vertical-align:-3px"')} بجانب أي سورة أو موعظة لتستمع إليها دون إنترنت.</div>` : ''}`;
    $('#delAll')?.addEventListener('click', () => confirmSheet('حذف كل التنزيلات؟', `ستُحذف ${arNum(items.length)} مادة (${dl.fmtSize(dl.totalSize())}) من جهازك.`, 'حذف الكل', () => dl.removeAll().then(() => toast('حُذفت التنزيلات'))));
  };
  draw();
  bindDlButtons(page, ctx, () => false);
  page.addEventListener('click', async e => {
    const b = e.target.closest('[data-act]');
    if (!b) return;
    const key = b.closest('.dl-row').dataset.key;
    const d = dl.list().find(x => x.key === key);
    if (!d) return;
    if (b.dataset.act === 'del') {
      confirmSheet('حذف من التنزيلات؟', `«${esc(d.title)}» (${dl.fmtSize(d.size)})`, 'حذف', () => dl.remove(key).then(() => toast('حُذف الملف')));
      return;
    }
    if (d.kind === 'surah') {
      if (player.mode === 'surah' && player.surah === d.ref.surah && player.reciter === d.ref.reciter) toggle();
      else playSurah(d.ref.surah, d.ref.reciter);
    } else {
      const data = await getLectures();
      const sp = data.speakers.find(s => s.id === d.ref.sid);
      const list = sp && data.items[sp.id];
      const idx = list ? list.findIndex(it => it.u.includes(d.ref.key)) : -1;
      if (idx < 0) return toast('تعذّر العثور على المادة');
      playLecture(sp, list, idx, d.ref.part || 0);
    }
  });
  // تحديث مباشر للتقدم
  let t = 0;
  const onChange = () => { if (Date.now() - t > 400) { t = Date.now(); draw(); } else { clearTimeout(onChange.tm); onChange.tm = setTimeout(draw, 450); } };
  dl.events.addEventListener('change', onChange);
  ctx.cleanup(() => { dl.events.removeEventListener('change', onChange); clearTimeout(onChange.tm); });
}

// تحميل كل سور القارئ
export function downloadAllSurahs(reciterId) {
  const r = reciterById(reciterId);
  const missing = SURAHS.map((_, i) => i + 1).filter(n => !dl.isDownloaded(surahUrl(reciterId, n)) && !dl.job(surahUrl(reciterId, n)));
  if (!missing.length) return toast('كل السور محمّلة بصوت هذا القارئ');
  confirmSheet(`تحميل المصحف كاملًا بصوت ${r.name}؟`, `سيُحمَّل ${arNum(missing.length)} سورة، وقد يبلغ الحجم نحو ${arNum(Math.round(missing.length / 114 * 700))} م.ب. يُفضَّل استعمال شبكة Wi‑Fi.`, 'تحميل', () => {
    missing.forEach(n => downloadSurah(n, reciterId));
    toast('أُضيفت السور إلى التنزيلات');
  });
}
