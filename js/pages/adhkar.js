// الأذكار، والسبحة، والقبلة
import { $, $$, store, arNum, esc, toast, copyText, icons, vibrate, todayKey } from '../core.js';
import { ADHKAR, TASBIH } from '../data/adhkar.js';
import { cfg, qibla, useGps } from '../prayer.js';

export function renderAdhkar(view, args, ctx) {
  if (args[0]) return renderCategory(view, args[0], ctx);
  const prog = store.get('adhkarProg', {});
  const done = id => {
    const p = prog[todayKey() + id];
    return p ? ADHKAR.find(c => c.id === id).items.filter((_, i) => p[i] === 0).length : 0;
  };
  view.innerHTML = `
    <div class="adhkar-grid">
      ${ADHKAR.map(c => {
        const n = done(c.id), total = c.items.length;
        return `<a class="dhikr-cat" href="#/adhkar/${c.id}">
          <span class="tile-ic ${c.id === 'evening' || c.id === 'sleep' ? 'gold' : ''}">${icons[c.icon]}</span>
          <strong>${c.name}</strong><small>${c.hint}</small>
          <span class="mini-progress"><i style="width:${n / total * 100}%"></i></span>
          <small class="count">${n === total ? 'أتممتها اليوم ✓' : `${arNum(total)} ذكرًا`}</small>
        </a>`;
      }).join('')}
    </div>
    <div class="section-head"><h2>أدوات</h2></div>
    <div class="tiles two">
      <a class="tile" href="#/tasbih"><span class="tile-ic">${icons.beads}</span><strong>السبحة</strong><small>عدّاد الأذكار</small></a>
      <a class="tile" href="#/qibla"><span class="tile-ic gold">${icons.compass}</span><strong>القبلة</strong><small>اتجاه الكعبة</small></a>
    </div>
    <p class="source-note">${icons.info} اقتصرنا على الأذكار الثابتة في الصحيحين أو ما صححه أهل العلم، مع ذكر المصدر تحت كل ذكر.</p>`;
}

function renderCategory(view, id, ctx) {
  const cat = ADHKAR.find(c => c.id === id);
  if (!cat) { location.hash = '#/adhkar'; return; }
  ctx.title(cat.name);
  ctx.back('#/adhkar');
  const key = todayKey() + id;
  const all = store.get('adhkarProg', {});
  // نحتفظ بتقدم اليوم فقط
  Object.keys(all).forEach(k => { if (!k.startsWith(todayKey())) delete all[k]; });
  const left = all[key] || cat.items.map(x => x.n);
  const save = () => { all[key] = left; store.set('adhkarProg', all); };

  view.innerHTML = `
    <div class="adhkar-progress"><span id="pText"></span><div class="bar"><i id="pBarA"></i></div></div>
    <div class="list" id="dlist">
      ${cat.items.map((d, i) => `
        <article class="card dhikr ${left[i] === 0 ? 'done' : ''}" data-i="${i}">
          ${d.title ? `<h3>${esc(d.title)}</h3>` : ''}
          <p class="dhikr-text ${d.q ? 'quran' : ''}">${esc(d.t).replace(/\n/g, '<br>')}</p>
          ${d.fadl ? `<p class="fadl">${esc(d.fadl)}</p>` : ''}
          <div class="dhikr-foot">
            <small class="src">${esc(d.src)}</small>
            <button class="icon-btn sm" data-copy aria-label="نسخ">${icons.copy}</button>
            <button class="counter" aria-label="عدّ"><b>${left[i] === 0 ? '✓' : arNum(left[i])}</b><span>${d.n > 1 ? `من ${arNum(d.n)}` : left[i] === 0 ? 'تم' : 'مرة'}</span></button>
          </div>
        </article>`).join('')}
    </div>
    <button class="btn ghost block" id="resetAll">${icons.reset} إعادة البدء</button>`;

  const updateProgress = () => {
    const d = left.filter(x => x === 0).length;
    $('#pText').textContent = d === cat.items.length ? 'أتممت الأذكار، تقبّل الله منك' : `${arNum(d)} من ${arNum(cat.items.length)}`;
    $('#pBarA').style.width = (d / cat.items.length * 100) + '%';
  };
  updateProgress();

  $('#dlist').onclick = e => {
    const card = e.target.closest('.dhikr');
    if (!card) return;
    const i = +card.dataset.i;
    if (e.target.closest('[data-copy]')) return copyText(cat.items[i].t);
    if (left[i] === 0) return;
    left[i]--;
    vibrate(left[i] === 0 ? [30, 40, 30] : 12);
    const c = $('.counter', card);
    c.querySelector('b').textContent = left[i] === 0 ? '✓' : arNum(left[i]);
    c.classList.remove('pop'); void c.offsetWidth; c.classList.add('pop');
    if (left[i] === 0) {
      card.classList.add('done');
      c.querySelector('span').textContent = 'تم';
      const next = $$('.dhikr:not(.done)')[0];
      if (next) setTimeout(() => next.scrollIntoView({ behavior: 'smooth', block: 'center' }), 250);
      else toast('أتممت الأذكار، تقبّل الله منك');
    }
    save(); updateProgress();
  };
  $('#resetAll').onclick = () => {
    cat.items.forEach((d, i) => { left[i] = d.n; });
    save();
    renderCategory(view, id, ctx);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };
}

/* ── السبحة ── */
export function renderTasbih(view, args, ctx) {
  ctx.back('history');
  const st = Object.assign({ idx: 0, count: 0, target: 33, day: todayKey(), total: 0 }, store.get('tasbih', {}));
  if (st.day !== todayKey()) Object.assign(st, { day: todayKey(), total: 0 });
  const save = () => store.set('tasbih', st);
  const TARGETS = [33, 100, 0];

  view.innerHTML = `
    <div class="chips" id="phrases">${TASBIH.map((p, i) => `<button class="chip ${i === st.idx ? 'active' : ''}" data-i="${i}">${p.t}</button>`).join('')}</div>
    <div class="tasbih">
      <div class="tasbih-phrase" id="phrase"></div>
      <small class="muted" id="psrc"></small>
      <button class="tasbih-btn" id="tap" aria-label="سبّح">
        <svg viewBox="0 0 200 200" class="ring"><circle cx="100" cy="100" r="92" class="track"/><circle cx="100" cy="100" r="92" class="prog" id="ring"/></svg>
        <b id="count"></b><span id="ofTarget"></span>
      </button>
      <div class="tasbih-ctrls">
        <button class="btn ghost" id="target"></button>
        <button class="btn ghost" id="reset">${icons.reset} تصفير</button>
      </div>
      <p class="muted">مجموع تسبيحك اليوم: <b id="total"></b></p>
    </div>
    <div class="card sunnah-note">
      <b>${icons.info} من السنة</b>
      <p>كان النبي ﷺ يعقد التسبيح بيمينه، وقال للنساء: «واعقدن بالأنامل، فإنهن مسؤولات مستنطقات».</p>
      <small>رواه أبو داود والترمذي، وحسنه الألباني — فالأفضل العدّ بالأصابع، وهذا العدّاد للتذكير والمساعدة.</small>
    </div>`;

  const ring = $('#ring');
  const C = 2 * Math.PI * 92;
  ring.style.strokeDasharray = C;
  const draw = () => {
    $('#phrase').textContent = TASBIH[st.idx].t;
    $('#psrc').textContent = TASBIH[st.idx].src;
    $('#count').textContent = arNum(st.count);
    $('#ofTarget').textContent = st.target ? `من ${arNum(st.target)}` : 'بلا حد';
    $('#target').textContent = st.target ? `الهدف: ${arNum(st.target)}` : 'الهدف: مفتوح';
    $('#total').textContent = arNum(st.total);
    const p = st.target ? (st.count % st.target || (st.count ? st.target : 0)) / st.target : 0;
    ring.style.strokeDashoffset = C * (1 - p);
  };
  draw();
  $('#tap').onclick = () => {
    st.count++; st.total++;
    if (st.target && st.count % st.target === 0) { vibrate([40, 60, 40]); toast(`أتممت ${arNum(st.target)}`); } else vibrate(10);
    const b = $('#tap'); b.classList.remove('pop'); void b.offsetWidth; b.classList.add('pop');
    save(); draw();
  };
  $('#reset').onclick = () => { st.count = 0; save(); draw(); };
  $('#target').onclick = () => { st.target = TARGETS[(TARGETS.indexOf(st.target) + 1) % TARGETS.length]; st.count = 0; save(); draw(); };
  $('#phrases').onclick = e => {
    const b = e.target.closest('.chip');
    if (!b) return;
    st.idx = +b.dataset.i; st.count = 0; save();
    $$('#phrases .chip').forEach(x => x.classList.toggle('active', x === b));
    draw();
  };
}

/* ── القبلة ── */
export function renderQibla(view, args, ctx) {
  ctx.back('history');
  const loc = cfg.loc;
  if (!loc || loc.lat == null) {
    view.innerHTML = `<div class="card center-card">
      <span class="tile-ic big">${icons.compass}</span>
      <h3>حدّد موقعك أولًا</h3>
      <p class="muted">نحتاج موقعك لحساب اتجاه القبلة بدقة.</p>
      <button class="btn block" id="gps">${icons.pin} استخدام موقعي</button>
      <a class="btn ghost block" href="#/adhan">إدخال المدينة يدويًا</a></div>`;
    $('#gps').onclick = async () => { await useGps(); if (ctx.alive()) renderQibla(view, args, ctx); };
    return;
  }
  const { bearing, km } = qibla(loc.lat, loc.lng);
  view.innerHTML = `
    <div class="qibla">
      <div class="compass" id="compass">
        <div class="dial" id="dial">
          <span class="n">ش</span><span class="e">ق</span><span class="s">ج</span><span class="w">غ</span>
          ${Array.from({ length: 72 }, (_, i) => `<i style="transform:rotate(${i * 5}deg)" class="${i % 18 === 0 ? 'major' : i % 6 === 0 ? 'mid' : ''}"></i>`).join('')}
          <div class="qibla-needle" style="transform:rotate(${bearing}deg)"><span class="kaaba"></span></div>
        </div>
        <div class="pointer"></div>
      </div>
      <div class="qibla-info">
        <div><b>${arNum(Math.round(bearing))}°</b><small>اتجاه القبلة من الشمال</small></div>
        <div><b>${arNum(Math.round(km))}</b><small>كم إلى مكة المكرمة</small></div>
      </div>
      <p class="muted center" id="qStatus">ضع الهاتف بشكل مسطّح، ثم فعّل البوصلة.</p>
      <button class="btn block" id="startCompass">${icons.compass} تفعيل البوصلة</button>
      <p class="source-note">${icons.info} البوصلة تتأثر بالمعادن والأجهزة القريبة؛ حرّك الهاتف على شكل رقم 8 لمعايرتها. وإن لم تتوفر البوصلة، فاتجاه القبلة ${arNum(Math.round(bearing))}° مع عقارب الساعة من الشمال.</p>
    </div>`;

  const dial = $('#dial');
  let got = false;
  const onOrient = e => {
    let heading = null;
    if (typeof e.webkitCompassHeading === 'number') heading = e.webkitCompassHeading;
    else if (e.absolute && e.alpha != null) heading = 360 - e.alpha;
    if (heading == null) return;
    got = true;
    dial.style.transform = `rotate(${-heading}deg)`;
    const diff = Math.abs(((bearing - heading + 540) % 360) - 180);
    const aligned = diff < 5;
    $('#compass').classList.toggle('aligned', aligned);
    $('#qStatus').textContent = aligned ? 'أنت الآن باتجاه القبلة' : `أدِر الهاتف ${arNum(Math.round(diff))}° حتى يتطابق المؤشر مع الكعبة`;
  };
  const start = async () => {
    try {
      if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
        const p = await DeviceOrientationEvent.requestPermission();
        if (p !== 'granted') return toast('لم يُسمح باستخدام البوصلة');
      }
    } catch { /* تجاهل */ }
    window.addEventListener('deviceorientationabsolute', onOrient);
    window.addEventListener('deviceorientation', onOrient);
    $('#startCompass').hidden = true;
    setTimeout(() => { if (!got && ctx.alive()) $('#qStatus').textContent = 'البوصلة غير متاحة في هذا الجهاز أو المتصفح.'; }, 2500);
  };
  $('#startCompass').onclick = start;
  ctx.cleanup(() => {
    window.removeEventListener('deviceorientationabsolute', onOrient);
    window.removeEventListener('deviceorientation', onOrient);
  });
}
