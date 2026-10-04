// صفحة مواقيت الصلاة والأذان
import { $, $$, esc, arNum, toast, icons } from '../core.js';
import { PRAYERS, METHODS, ADHANS, adhanUrl, cfg, saveCfg, times, loadTimes, nextPrayer, toDate, fmt12, useGps, events as prEvents, scheduleAdhans, enableNativeAdhan, nativeAdhanStatus, testAdhan, openExactSettings, requestIgnoreBattery } from '../prayer.js';
import { isNative, BUNDLED_ADHANS } from '../native.js';

// في التطبيق: أصوات الأذان المضمّنة فقط (لتعمل والتطبيق مغلق ودون اتصال)
const voices = () => (isNative ? ADHANS.filter(a => BUNDLED_ADHANS.includes(a[0])) : ADHANS);

export function heroHTML({ greeting = '', strip = true } = {}) {
  if (!cfg.loc) {
    return `<section class="hero">
      <div class="hero-top"><span>${greeting}</span></div>
      <div class="hero-label">مواقيت الصلاة</div>
      <div class="hero-prayer sm">حدّد موقعك لعرض المواقيت ورفع الأذان</div>
      <button class="btn on-dark" data-gps>${icons.pin} استخدام موقعي</button>
    </section>`;
  }
  if (!times.today) {
    return `<section class="hero"><div class="hero-top"><span>${greeting}</span></div>
      <div class="hero-label">${times.error ? 'تعذّر تحميل المواقيت، تحقق من الاتصال' : 'جارٍ تحميل المواقيت…'}</div>
      <div class="hero-prayer">—</div></section>`;
  }
  const np = nextPrayer();
  const stripHTML = PRAYERS.filter(p => !p.noAdhan).map(p => `<div class="${p.key === np.key && !np.tomorrow ? 'now' : ''}">${p.name}<b>${fmt12(times.today.timings[p.key], false)}</b></div>`).join('');
  return `<a href="#/adhan" class="hero">
    <div class="hero-top"><span>${greeting}</span><span>${times.hijri.text}</span></div>
    <div class="hero-main">
      <div>
        <div class="hero-label">الصلاة القادمة</div>
        <div class="hero-prayer">${np.name}</div>
        <div class="hero-time">${fmt12(np.hm)}${np.tomorrow ? ' · غدًا' : ''}</div>
      </div>
      <div class="countdown"><small>متبقٍّ</small><span data-countdown>--:--:--</span></div>
    </div>
    ${strip ? `<div class="hero-strip">${stripHTML}</div>` : ''}
  </a>`;
}

export function renderAdhan(view, args, ctx) {
  const draw = () => {
    const loc = cfg.loc;
    const np = nextPrayer();
    const now = new Date();
    const list = times.today ? PRAYERS.map(p => {
      const hm = times.today.timings[p.key];
      const isNext = np && np.key === p.key && !np.tomorrow;
      const passed = toDate(hm) < now && !isNext;
      return `<div class="row prayer ${isNext ? 'next' : ''} ${passed ? 'passed' : ''}">
        <span class="p-ic">${icons[p.icon]}</span>
        <span class="meta"><strong>${p.name}</strong>${isNext ? '<small>متبقٍّ <span data-countdown></span></small>' : ''}</span>
        <span class="p-time">${fmt12(hm)}</span>
        ${p.noAdhan ? '<span class="bell"></span>' : `<button class="bell ${cfg.on[p.key] ? 'on' : ''}" data-p="${p.key}" aria-label="أذان ${p.name}">${cfg.on[p.key] ? icons.bell : icons.bellOff}</button>`}
      </div>`;
    }).join('') : '';

    view.innerHTML = `
      ${heroHTML({ strip: false, greeting: times.today ? 'مواقيت اليوم' : '' })}
      ${loc && !times.today ? `<div class="loader">${times.error ? 'تعذّر تحميل المواقيت. تحقق من الاتصال أو من اسم المدينة.' : '<div class="spinner"></div>'}</div>` : ''}
      ${list ? `<div class="list-card mt">${list}</div>` : ''}
      ${loc ? `<a class="card continue mt" href="#/qibla"><span class="tile-ic gold">${icons.compass}</span><div><strong>اتجاه القبلة</strong><small class="muted">بوصلة تحدد اتجاه الكعبة من موقعك</small></div><span class="chev">${icons.chevron}</span></a>` : ''}

      <div class="section-head"><h2>الموقع</h2></div>
      <div class="card settings">
        ${loc ? `<div class="location-line">${icons.pin} ${loc.type === 'gps' ? 'موقعي الحالي (GPS)' : esc(loc.city + '، ' + loc.country)}</div>` : ''}
        <button class="btn block" data-gps>${icons.pin} تحديد موقعي تلقائيًا</button>
        <div class="or"><span>أو أدخل المدينة يدويًا</span></div>
        <div class="row2">
          <input class="input" id="city" placeholder="المدينة" value="${loc?.type === 'city' ? esc(loc.city) : ''}" autocomplete="address-level2">
          <input class="input" id="country" placeholder="الدولة" value="${loc?.type === 'city' ? esc(loc.country) : ''}" autocomplete="country-name">
        </div>
        <button class="btn ghost block" id="cityBtn">حفظ المدينة</button>
      </div>

      <div class="section-head"><h2>الأذان</h2></div>
      <div class="card settings">
        <div class="field"><label for="sound">صوت المؤذن (من مؤذني المسجد الحرام)</label>
          <div class="row2 tight">
            <select class="select" id="sound">${voices().map(([v, l]) => `<option value="${v}" ${v === cfg.sound ? 'selected' : ''}>${l}</option>`).join('')}</select>
            <button class="btn ghost" id="preview">${icons.play} استماع</button>
          </div>
        </div>
        <label class="switch-row">
          <span>${isNative ? 'رفع الأذان والتطبيق مغلق' : 'الإشعارات'}<small>${isNative ? 'يُرفع الأذان بصوت المؤذن عند دخول وقت كل صلاة' : 'تنبيه عند دخول وقت كل صلاة'}</small></span>
          <span class="switch"><input type="checkbox" id="alerts" ${cfg.alerts ? 'checked' : ''}><i></i></span>
        </label>
        ${isNative ? `<div class="adhan-status" id="adhanStatus"><div class="loader small"><div class="spinner"></div></div></div>
        <button class="btn ghost block" id="testAdhan">${icons.bell} تجربة الأذان الآن (بعد ١٠ ثوانٍ)</button>` : ''}
        <div class="note-box">${icons.info} ${isNative
          ? 'يُرفع الأذان على صوت المنبّه، فيُسمع حتى في الوضع الصامت؛ ويمكنك إيقافه من الإشعار. ويمكنك إيقاف الأذان لصلاة معيّنة من زر الجرس بجانبها.'
          : 'يُرفع الأذان تلقائيًا عند دخول الوقت ما دام التطبيق مفتوحًا. ويمكنك إيقاف الأذان لأي صلاة من زر الجرس بجانبها.'}</div>
      </div>

      <div class="section-head"><h2>طريقة الحساب</h2></div>
      <div class="card settings">
        <div class="field"><label for="method">الجهة المعتمدة</label>
          <select class="select" id="method">${METHODS.map(([v, l]) => `<option value="${v}" ${v === cfg.method ? 'selected' : ''}>${l}</option>`).join('')}</select>
        </div>
        <div class="field"><label for="school">وقت العصر</label>
          <select class="select" id="school">
            <option value="0" ${cfg.school === 0 ? 'selected' : ''}>الجمهور: إذا صار ظل الشيء مثله</option>
            <option value="1" ${cfg.school === 1 ? 'selected' : ''}>الحنفية: إذا صار ظل الشيء مثليه</option>
          </select>
        </div>
        <details class="tune">
          <summary>تعديل الأوقات يدويًا (بالدقائق)</summary>
          <p class="muted small">لمطابقة توقيت المسجد القريب منك.</p>
          ${PRAYERS.map(p => `<div class="tune-row"><span>${p.name}</span>
            <button class="icon-btn sm" data-tune="${p.key}" data-d="-1" aria-label="إنقاص">${icons.minus}</button>
            <b>${arNum(cfg.tune[p.key] || 0)}</b>
            <button class="icon-btn sm" data-tune="${p.key}" data-d="1" aria-label="زيادة">${icons.plus}</button></div>`).join('')}
          <button class="btn block" id="applyTune">تطبيق</button>
        </details>
      </div>`;
    bind();
  };

  const prev = $('#adhanAudio');
  const reload = async () => { times.today = null; draw(); await loadTimes(true); };
  const bind = () => {
    $$('[data-gps]', view).forEach(b => { b.onclick = e => { e.preventDefault(); useGps(); }; });
    $('#cityBtn').onclick = () => {
      const city = $('#city').value.trim(), country = $('#country').value.trim();
      if (!city || !country) return toast('أدخل اسم المدينة والدولة');
      cfg.loc = { type: 'city', city, country };
      saveCfg(); reload();
    };
    $('#method').onchange = e => { cfg.method = +e.target.value; saveCfg(); reload(); };
    $('#school').onchange = e => { cfg.school = +e.target.value; saveCfg(); reload(); };
    $('#sound').onchange = e => { cfg.sound = e.target.value; saveCfg(); prev.pause(); $('#preview').innerHTML = `${icons.play} استماع`; scheduleAdhans(); };
    $('#preview').onclick = () => {
      if (!prev.paused) { prev.pause(); $('#preview').innerHTML = `${icons.play} استماع`; return; }
      prev.src = adhanUrl(cfg.sound);
      prev.play().catch(() => toast('تعذّر تشغيل الصوت'));
      $('#preview').innerHTML = `${icons.pause} إيقاف`;
    };
    $('#alerts').onchange = async e => {
      cfg.alerts = e.target.checked;
      if (isNative) {
        if (cfg.alerts && !(await enableNativeAdhan())) { cfg.alerts = false; e.target.checked = false; }
        saveCfg();
        scheduleAdhans();
        return;
      }
      if (cfg.alerts && 'Notification' in window && Notification.permission === 'default') {
        const p = await Notification.requestPermission();
        if (p !== 'granted') toast('لم يُسمح بالإشعارات، سيُرفع الأذان داخل التطبيق فقط');
      }
      saveCfg();
    };
    $$('.bell[data-p]', view).forEach(b => {
      b.onclick = () => {
        const k = b.dataset.p;
        cfg.on[k] = !cfg.on[k]; saveCfg();
        b.classList.toggle('on', cfg.on[k]);
        b.innerHTML = cfg.on[k] ? icons.bell : icons.bellOff;
        scheduleAdhans();
        toast(cfg.on[k] ? 'تم تفعيل الأذان لهذه الصلاة' : 'تم إيقاف الأذان لهذه الصلاة');
      };
    });
    const tune = { ...cfg.tune };
    $$('[data-tune]', view).forEach(b => {
      b.onclick = () => {
        const k = b.dataset.tune;
        tune[k] = Math.max(-30, Math.min(30, (tune[k] || 0) + +b.dataset.d));
        b.parentElement.querySelector('b').textContent = arNum(tune[k]);
      };
    });
    $('#applyTune').onclick = () => { cfg.tune = tune; saveCfg(); reload(); toast('تم تعديل الأوقات'); };
  };

  draw();
  const onUpdate = () => { draw(); if (isNative) showStatus(); };
  prEvents.addEventListener('update', onUpdate);

  // حالة الأذان في الهاتف: الأذان القادم المجدول، والمنبّهات الدقيقة، وتوفير البطارية
  const showStatus = async () => {
    const box = $('#adhanStatus');
    if (!box) return;
    const st = await nativeAdhanStatus();
    if (!st || !ctx.alive() || !$('#adhanStatus')) return;
    const rows = [];
    if (!cfg.alerts) rows.push(['warn', 'الأذان متوقف. فعّله من المفتاح أعلاه.']);
    else if (st.nextAt) {
      const d = new Date(st.nextAt);
      const hm = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
      const day = d.toDateString() === new Date().toDateString() ? 'اليوم' : 'غدًا';
      rows.push(['ok', `الأذان القادم: <b>${st.nextName}</b> ${day} ${fmt12(hm)} — ومجدول ${arNum(st.upcoming)} أذانًا مقدّمًا`]);
    } else rows.push(['warn', 'لا يوجد أذان مجدول بعد. حدّد موقعك ليُجدول الأذان.']);
    rows.push(st.exact ? ['ok', 'المنبّهات الدقيقة مسموحة'] : ['warn', 'المنبّهات الدقيقة غير مسموحة، فقد يتأخر الأذان. <button class="link" data-fix="exact">السماح</button>']);
    rows.push(st.batteryIgnored ? ['ok', 'التطبيق مستثنى من توفير البطارية'] : ['warn', 'قد يؤخّر توفير البطارية الأذان في بعض الهواتف. <button class="link" data-fix="battery">استثناء التطبيق</button>']);
    if (!st.notifications) rows.push(['warn', 'الإشعارات متوقفة؛ سيُرفع الأذان دون إشعار وزر إيقاف.']);
    box.innerHTML = rows.map(([k, t]) => `<div class="st ${k}">${k === 'ok' ? icons.check : icons.info}<span>${t}</span></div>`).join('');
  };
  const onFocus = () => { if (document.visibilityState === 'visible') showStatus(); };
  if (isNative) {
    showStatus();
    prEvents.addEventListener('scheduled', showStatus);
    document.addEventListener('visibilitychange', onFocus);
    ctx.cleanup(() => { prEvents.removeEventListener('scheduled', showStatus); document.removeEventListener('visibilitychange', onFocus); });
    const onClick = e => {
      const f = e.target.closest('[data-fix]');
      if (f) (f.dataset.fix === 'exact' ? openExactSettings() : requestIgnoreBattery());
      if (e.target.closest('#testAdhan')) {
        testAdhan(10);
        toast('سيُرفع أذان التجربة بعد ١٠ ثوانٍ؛ يمكنك إغلاق التطبيق للتأكد');
      }
    };
    view.addEventListener('click', onClick);
    ctx.cleanup(() => view.removeEventListener('click', onClick));
  }
  ctx.cleanup(() => {
    prEvents.removeEventListener('update', onUpdate);
    if (!prev.paused && !document.querySelector('.adhan-alert')) prev.pause();
  });
}
