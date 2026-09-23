// التطبيق الرئيسي: القوائم، اختيار الشخصية، الغرف، حلقة الرسم
import * as THREE from 'three';
import { World } from './scene.js';
import { Player3D } from './player3d.js';
import { Match } from './match.js';
import { Input } from './input.js';
import { audio } from './audio.js';
import { LocalTransport, NetClient, NetTransport } from './net.js';
import { CHARACTERS, charOf } from '/shared/characters.js';
import { STADIUMS, stadiumOf } from '/shared/stadiums.js';
import { TEAMS, DIFFICULTY, DURATIONS, TEAM_SIZE, STATE, BALL_R } from '/shared/constants.js';

const $ = (id) => document.getElementById(id);
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const POS_NAMES = ['حارس', 'مدافع', 'مدافع', 'مهاجم', 'مهاجم'];
const CUP_ROUNDS = [
  { name: 'ربع النهائي', diff: 'easy' },
  { name: 'نصف النهائي', diff: 'normal' },
  { name: 'النهائي', diff: 'hard', stadium: 'neon' },
];
const isMobile = matchMedia('(pointer: coarse)').matches || /Android|iPhone|iPad/i.test(navigator.userAgent);

class App {
  constructor() {
    this.loadSettings();
    this.canvas = $('gl');
    this.initRenderer();
    this.camera = new THREE.PerspectiveCamera(42, 1, 0.1, 1200);
    this.input = new Input();
    this.net = new NetClient();
    this.match = null;
    this.room = null;
    this.paused = false;
    this.quick = { stadium: 'royal', duration: 180, difficulty: 'normal', team: 0, slot: 3 };
    if (isMobile) document.body.classList.add('touch');
    document.body.classList.toggle('lefty', !!this.settings.lefty);
    window.__noVib = this.settings.vibrate === false;
    // شاشة البداية: تفعيل الصوت + ملء الشاشة + الوضع الأفقي
    const splash = $('splash');
    splash.addEventListener('click', () => {
      audio.init(); audio.setEnabled(this.settings.sound); audio.setVolume(this.settings.volume);
      splash.classList.add('gone');
      setTimeout(() => splash.remove(), 600);
      if (isMobile) {
        const el = document.documentElement;
        Promise.resolve(el.requestFullscreen ? el.requestFullscreen({ navigationUI: 'hide' }) : null)
          .then(() => screen.orientation && screen.orientation.lock && screen.orientation.lock('landscape'))
          .catch(() => {});
      }
    }, { once: true });
    // إيقاف مؤقت عند مغادرة التطبيق
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        if (this.match && this.match.local && !this.paused) this.togglePause(true);
        if (audio.ctx) audio.ctx.suspend();
      } else if (audio.ctx && this.settings.sound) audio.ctx.resume();
    });
    // تطبيق قابل للتثبيت (PWA)
    if (!window.OFFLINE_ONLY && 'serviceWorker' in navigator && (location.protocol === 'https:' || location.hostname === 'localhost')) {
      navigator.serviceWorker.register('/sw.js').catch(() => {});
    }
    if (window.OFFLINE_ONLY) $('btn-online').classList.add('hidden');
    // ترجمة المعلق كنص على الشاشة (إذا لم يوجد صوت عربي)
    audio.onSay = (text, hot) => {
      const el = $('subtitle');
      el.textContent = '🎙️ ' + text; el.classList.toggle('hot', !!hot); el.classList.add('show');
      clearTimeout(this.subT); this.subT = setTimeout(() => el.classList.remove('show'), 2600);
    };
    this.onResize();
    window.addEventListener('resize', () => this.onResize());
    this.bindUI();
    this.buildMenus();
    this.renderCareer();
    this.setupMenuScene(STADIUMS[Math.floor(Math.random() * STADIUMS.length)].id);
    this.setupNet();
    this.last = performance.now();
    requestAnimationFrame((t) => this.loop(t));
    // رابط دعوة
    const code = new URLSearchParams(location.search).get('room');
    if (code) { this.show('online'); this.connectOnline().then(() => this.net.send({ t: 'join', code })); }
    else this.show('main');
    // تفعيل الصوت عند أول تفاعل
    const unlock = () => { audio.init(); audio.setEnabled(this.settings.sound); audio.setVolume(this.settings.volume); audio.commentary = this.settings.commentary; };
    window.addEventListener('pointerdown', unlock, { once: false });
    window.addEventListener('keydown', unlock, { once: false });
    document.addEventListener('click', (e) => { if (e.target.closest('button, .char, .stad')) audio.click(); });
  }

  // ---------- الإعدادات ----------
  loadSettings() {
    let s = {};
    try { s = JSON.parse(localStorage.getItem('fl5-settings') || '{}'); } catch { s = {}; }
    this.settings = Object.assign({
      name: '', char: 'blaze', quality: isMobile ? 'low' : 'medium', volume: 0.8, sound: true, commentary: true, camera: 'broadcast', lefty: false, vibrate: true, tutorialDone: false,
    }, s);
    if (!this.settings.name) this.settings.name = 'لاعب' + Math.floor(Math.random() * 900 + 100);
  }
  saveSettings() {
    try { localStorage.setItem('fl5-settings', JSON.stringify(this.settings)); } catch { /* ignore */ }
  }

  initRenderer() {
    const q = this.settings.quality;
    const r = new THREE.WebGLRenderer({ canvas: this.canvas, antialias: q !== 'low', powerPreference: 'high-performance' });
    r.setPixelRatio(q === 'low' ? Math.min(devicePixelRatio, 1) : q === 'medium' ? Math.min(devicePixelRatio, 1.5) : Math.min(devicePixelRatio, 2));
    r.outputColorSpace = THREE.SRGBColorSpace;
    r.toneMapping = THREE.ACESFilmicToneMapping;
    r.toneMappingExposure = 1.05;
    r.shadowMap.enabled = q !== 'low';
    r.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer = r;
    this.composer = null;
    if (q === 'high') this.initBloom();
  }

  async initBloom() {
    try {
      const [{ EffectComposer }, { RenderPass }, { UnrealBloomPass }, { OutputPass }] = await Promise.all([
        import('three/addons/postprocessing/EffectComposer.js'),
        import('three/addons/postprocessing/RenderPass.js'),
        import('three/addons/postprocessing/UnrealBloomPass.js'),
        import('three/addons/postprocessing/OutputPass.js'),
      ]);
      this.bloomMods = { EffectComposer, RenderPass, UnrealBloomPass, OutputPass };
      this.composerScene = null;
    } catch (e) { console.warn('bloom unavailable', e); }
  }

  render(scene, camera) {
    if (this.bloomMods) {
      if (this.composerScene !== scene) {
        const { EffectComposer, RenderPass, UnrealBloomPass, OutputPass } = this.bloomMods;
        this.composer = new EffectComposer(this.renderer);
        this.composer.addPass(new RenderPass(scene, camera));
        this.composer.addPass(new UnrealBloomPass(new THREE.Vector2(this.width, this.height), 0.35, 0.5, 0.85));
        this.composer.addPass(new OutputPass());
        this.composer.setSize(this.width, this.height);
        this.composerScene = scene;
      }
      this.composer.render();
    } else this.renderer.render(scene, camera);
  }

  onResize() {
    this.width = window.innerWidth; this.height = window.innerHeight;
    this.renderer.setSize(this.width, this.height, false);
    this.camera.aspect = this.width / this.height;
    this.camera.updateProjectionMatrix();
    if (this.composer) this.composer.setSize(this.width, this.height);
    if (this.match) this.match.onResize();
  }

  toast(t, ms = 2200) {
    const el = $('toast');
    el.textContent = t;
    el.classList.add('show');
    clearTimeout(this.toastT);
    this.toastT = setTimeout(() => el.classList.remove('show'), ms);
  }

  loading(on, text) {
    $('loading').classList.toggle('hidden', !on);
    if (text) $('loading-text').textContent = text;
  }

  // ---------- الشاشات ----------
  show(name) {
    for (const s of document.querySelectorAll('.screen')) s.classList.add('hidden');
    if (name) $('scr-' + name).classList.remove('hidden');
    this.screen = name;
    if (name === 'online') this.refreshRooms();
    clearInterval(this.roomsTimer);
    if (name === 'online') this.roomsTimer = setInterval(() => this.refreshRooms(), 4000);
  }

  bindUI() {
    for (const b of document.querySelectorAll('[data-go]')) b.addEventListener('click', () => { this.saveSettings(); this.show(b.dataset.go); });
    const name = $('inp-name');
    name.value = this.settings.name;
    name.addEventListener('input', () => { this.settings.name = name.value.trim().slice(0, 14) || 'لاعب'; this.saveSettings(); this.net.send({ t: 'hello', name: this.settings.name, char: this.settings.char }); });
    $('btn-quick').onclick = () => { this.show('quick'); this.setupMenuScene(this.quick.stadium); };
    $('btn-online').onclick = () => { this.show('online'); this.connectOnline(); };
    $('btn-settings').onclick = () => this.show('settings');
    $('btn-help').onclick = () => this.show('help');
    $('btn-quick-start').onclick = () => this.startQuick();
    $('btn-cup').onclick = () => this.startCup(0);
    $('btn-create').onclick = () => this.connectOnline().then(() => this.net.send({ t: 'create', settings: { ...this.quickSettings(), public: $('chk-public').checked, name: `غرفة ${this.settings.name}` } }));
    $('btn-join').onclick = () => { const c = $('inp-code').value.trim().toUpperCase(); if (c) this.connectOnline().then(() => this.net.send({ t: 'join', code: c })); };
    $('inp-code').addEventListener('keydown', (e) => { if (e.key === 'Enter') $('btn-join').click(); });
    $('btn-refresh').onclick = () => this.refreshRooms();
    $('btn-leave').onclick = () => { this.net.send({ t: 'leave' }); this.room = null; this.show('online'); };
    $('btn-spec').onclick = () => this.net.send({ t: 'slot', team: -1, slot: -1 });
    $('btn-start').onclick = () => this.net.send({ t: 'start' });
    $('btn-copy').onclick = () => {
      const url = `${location.origin}${location.pathname}?room=${this.room.code}`;
      (navigator.clipboard ? navigator.clipboard.writeText(url) : Promise.reject()).then(() => this.toast('تم نسخ رابط الدعوة ✅')).catch(() => prompt('انسخ الرابط:', url));
    };
    const sendChat = () => { const i = $('room-chat-inp'); if (i.value.trim()) { this.net.send({ t: 'chat', text: i.value.trim() }); i.value = ''; } };
    $('room-chat-send').onclick = sendChat;
    $('room-chat-inp').addEventListener('keydown', (e) => { if (e.key === 'Enter') sendChat(); });
    for (const id of ['room-stadium', 'room-dur', 'room-diff']) {
      $(id).addEventListener('change', () => this.net.send({ t: 'settings', settings: { stadium: $('room-stadium').value, duration: +$('room-dur').value, difficulty: $('room-diff').value } }));
    }
    // واجهة المباراة
    $('rot-ok').onclick = () => { this.rotateDismissed = true; $('rotate').classList.add('hidden'); };
    $('hb-cam').onclick = () => this.match && this.match.cycleCamera();
    $('hb-sound').onclick = () => this.toggleSound();
    $('hb-full').onclick = () => this.toggleFull();
    $('hb-menu').onclick = () => this.togglePause();
    $('p-resume').onclick = () => this.togglePause(false);
    $('p-cam').onclick = () => this.match && this.match.cycleCamera();
    $('p-sound').onclick = () => this.toggleSound();
    $('p-help').onclick = () => { this.togglePause(false); this.toast('حركة: WASD • تسديد: مسافة • تمرير: E • لوب: Q • افتكاك: F • مهارة: R', 4500); };
    $('p-quit').onclick = () => this.quitMatch();
    $('p-stop').onclick = () => { this.net.send({ t: 'stop' }); this.togglePause(false); };
    $('end-menu').onclick = () => { this.quitMatch(); };
    $('end-again').onclick = () => {
      $('endscreen').classList.add('hidden');
      if (this.match && this.match.local) {
        const cup = this.cup;
        this.endMatch();
        if (cup && cup.next != null) this.startCup(cup.next);
        else if (cup && cup.champion) { this.cup = null; this.setupMenuScene(this.quick.stadium); this.show('main'); this.renderCareer(); }
        else if (cup) this.startCup(0);
        else this.startQuick();
      }
      else { this.endMatch(); this.show('room'); this.renderRoom(); }
    };
    // الإدخال
    this.input.on('emote', (n) => this.match && this.match.emote(n));
    this.input.on('camera', () => this.match && this.match.cycleCamera());
    this.input.on('pause', () => this.match && this.togglePause());
    this.input.on('mute', () => this.toggleSound());
    this.input.on('chat', () => this.openChat());
    const ci = $('chat-inp');
    ci.addEventListener('keydown', (e) => {
      e.stopPropagation();
      if (e.key === 'Enter') { if (ci.value.trim()) this.net.send({ t: 'chat', text: ci.value.trim() }); ci.value = ''; ci.classList.add('hidden'); ci.blur(); }
      if (e.key === 'Escape') { ci.classList.add('hidden'); ci.blur(); }
    });
  }

  openChat() {
    if (!this.match || this.match.local) return;
    const ci = $('chat-inp');
    ci.classList.remove('hidden');
    this.input.keys.clear();
    setTimeout(() => ci.focus(), 10);
  }

  toggleSound() {
    this.settings.sound = !this.settings.sound;
    audio.init();
    audio.setEnabled(this.settings.sound);
    this.saveSettings();
    $('hb-sound').textContent = this.settings.sound ? '🔊' : '🔇';
    this.toast(this.settings.sound ? 'الصوت مفعل 🔊' : 'الصوت مكتوم 🔇');
  }

  toggleFull() {
    if (!document.fullscreenElement) document.documentElement.requestFullscreen && document.documentElement.requestFullscreen().catch(() => {});
    else document.exitFullscreen && document.exitFullscreen();
  }

  togglePause(force) {
    const on = force ?? $('pause').classList.contains('hidden');
    $('pause').classList.toggle('hidden', !on);
    this.paused = on;
    if (this.match && this.match.local) this.match.transport.paused = on;
    $('p-stop').classList.toggle('hidden', !(this.room && this.room.host === this.net.id && this.match && !this.match.local));
  }

  // ---------- بناء القوائم ----------
  buildMenus() {
    // الشخصيات
    const grid = $('char-grid');
    grid.innerHTML = CHARACTERS.map((c) => `<div class="char" data-char="${c.id}"><div class="ic">${c.icon}</div><div class="nm">${c.name}</div><div class="tt">${c.title}</div></div>`).join('');
    grid.onclick = (e) => {
      const el = e.target.closest('[data-char]');
      if (!el) return;
      this.selectChar(el.dataset.char);
    };
    this.selectChar(this.settings.char, true);
    // الملاعب
    const st = $('quick-stadiums');
    st.innerHTML = STADIUMS.map((s) => `<div class="stad" data-st="${s.id}" style="background:linear-gradient(180deg, ${s.skyTop}, ${s.skyBottom});--g1:${s.pitch.colors[0]};--g2:${s.pitch.colors[1]}"><b>${s.name}</b><small>${s.desc}</small></div>`).join('');
    st.onclick = (e) => {
      const el = e.target.closest('[data-st]');
      if (!el) return;
      this.quick.stadium = el.dataset.st;
      this.markOn(st, '[data-st]', 'st', this.quick.stadium);
      this.setupMenuScene(this.quick.stadium);
    };
    this.markOn(st, '[data-st]', 'st', this.quick.stadium);
    this.seg('quick-dur', DURATIONS.map((d) => [d, `${d / 60} د`]), () => this.quick.duration, (v) => (this.quick.duration = +v));
    this.seg('quick-diff', Object.entries(DIFFICULTY).map(([k, v]) => [k, v.label]), () => this.quick.difficulty, (v) => (this.quick.difficulty = v));
    this.seg('quick-team', TEAMS.map((t, i) => [i, t.name]), () => this.quick.team, (v) => (this.quick.team = +v));
    this.seg('quick-pos', [[3, 'مهاجم ⚽'], [1, 'مدافع 🛡️'], [0, 'حارس 🧤']], () => this.quick.slot, (v) => (this.quick.slot = +v));
    // الإعدادات
    this.seg('set-quality', [['low', 'منخفضة'], ['medium', 'متوسطة'], ['high', 'عالية']], () => this.settings.quality, (v) => {
      this.settings.quality = v; this.saveSettings();
      this.toast('سيتم تطبيق الجودة بعد إعادة التحميل…');
      setTimeout(() => location.reload(), 900);
    });
    this.seg('set-cam', [['broadcast', 'بث تلفزيوني'], ['behind', 'خلف اللاعب'], ['top', 'من الأعلى']], () => this.settings.camera, (v) => { this.settings.camera = v; this.saveSettings(); });
    $('set-vol').value = this.settings.volume;
    $('set-vol').oninput = () => { this.settings.volume = +$('set-vol').value; audio.init(); audio.setVolume(this.settings.volume); this.saveSettings(); };
    $('set-lefty').checked = !!this.settings.lefty;
    $('set-lefty').onchange = () => { this.settings.lefty = $('set-lefty').checked; document.body.classList.toggle('lefty', this.settings.lefty); this.saveSettings(); };
    $('set-vib').checked = this.settings.vibrate !== false;
    $('set-vib').onchange = () => { this.settings.vibrate = $('set-vib').checked; window.__noVib = !this.settings.vibrate; this.saveSettings(); };
    $('set-tut').checked = false;
    $('set-tut').onchange = () => { if ($('set-tut').checked) { this.settings.tutorialDone = false; this.settings.tutSeen = []; this.saveSettings(); this.toast('ستظهر النصائح في المباراة القادمة'); } };
    $('set-sound').checked = this.settings.sound;
    $('set-sound').onchange = () => { this.settings.sound = $('set-sound').checked; audio.setEnabled(this.settings.sound); this.saveSettings(); };
    $('set-comm').checked = this.settings.commentary;
    $('set-comm').onchange = () => { this.settings.commentary = $('set-comm').checked; audio.commentary = this.settings.commentary; this.saveSettings(); if (audio.commentary) audio.say('أهلاً بكم في أساطير الكرة'); };
    $('hb-sound').textContent = this.settings.sound ? '🔊' : '🔇';
    // الغرفة
    $('room-stadium').innerHTML = STADIUMS.map((s) => `<option value="${s.id}">${s.name}</option>`).join('');
    $('room-dur').innerHTML = DURATIONS.map((d) => `<option value="${d}">${d / 60} دقائق</option>`).join('');
    $('room-diff').innerHTML = Object.entries(DIFFICULTY).map(([k, v]) => `<option value="${k}">بوتات: ${v.label}</option>`).join('');
    $('room-t0').textContent = TEAMS[0].name;
    $('room-t1').textContent = TEAMS[1].name;
  }

  seg(id, options, get, set) {
    const el = $(id);
    el.innerHTML = options.map(([v, l]) => `<button data-v="${v}">${l}</button>`).join('');
    const mark = () => el.querySelectorAll('button').forEach((b) => b.classList.toggle('on', b.dataset.v === String(get())));
    el.onclick = (e) => { const b = e.target.closest('button'); if (!b) return; set(b.dataset.v); mark(); };
    mark();
  }

  markOn(root, sel, key, val) { root.querySelectorAll(sel).forEach((e) => e.classList.toggle('on', e.dataset[key] === val)); }

  selectChar(id, silent) {
    const c = charOf(id);
    this.settings.char = c.id;
    this.saveSettings();
    this.markOn($('char-grid'), '[data-char]', 'char', c.id);
    const s = c.stats;
    const bar = (label, v) => `<span>${label}</span><div class="bar"><i style="width:${Math.min(100, Math.round((v - 0.75) / 0.55 * 100))}%"></i></div>`;
    $('char-info').innerHTML = `
      <h3>${c.icon} ${c.name} <small class="muted">— ${c.title}</small></h3>
      <div class="ab">✨ <b>${c.ability.name}</b>: ${c.ability.desc} <span class="muted">(${c.ability.cd} ث)</span></div>
      <div class="stats">${bar('السرعة', s.speed)}${bar('التسديد', s.shot)}${bar('التمرير', s.pass)}${bar('المراوغة', s.dribble)}${bar('الافتكاك', s.tackle)}${bar('حراسة المرمى', s.keeper)}</div>`;
    if (!silent) {
      this.net.send({ t: 'char', char: c.id });
      this.buildPreview();
      if (this.previewP) { this.previewP.prevState = -1; this.previewCeleb = 0.01; }
    }
  }

  quickSettings() { return { stadium: this.quick.stadium, duration: this.quick.duration, difficulty: this.quick.difficulty }; }

  // ---------- مشهد القائمة (عرض الشخصية) ----------
  setupMenuScene(stadiumId) {
    if (this.menuWorld && this.menuWorld.st.id === stadiumId) return;
    if (this.menuWorld) this.menuWorld.dispose();
    this.menuWorld = new World(this.renderer, stadiumOf(stadiumId), this.settings.quality === 'high' ? 'medium' : this.settings.quality);
    this.previewBall = new THREE.Mesh(new THREE.SphereGeometry(BALL_R, 20, 14), new THREE.MeshStandardMaterial({ color: '#ffffff', roughness: 0.4 }));
    this.previewBall.castShadow = true;
    this.menuWorld.scene.add(this.previewBall);
    this.previewP = null;
    this.buildPreview();
    this.menuT = 0;
  }

  buildPreview() {
    if (!this.menuWorld) return;
    if (this.previewP) { this.menuWorld.scene.remove(this.previewP.root); this.previewP.dispose(); }
    this.previewP = new Player3D({ id: 0, team: 0, slot: 3, name: this.settings.name, char: this.settings.char, human: true }, { preview: true });
    this.previewP.onSiu = () => audio.siu();
    this.menuWorld.scene.add(this.previewP.root);
    this.previewCeleb = 0;
  }

  updateMenu(dt) {
    const w = this.menuWorld;
    if (!w) return;
    this.menuT += dt;
    const t = this.menuT;
    const p = this.previewP;
    // تنطيط الكرة ثم احتفال
    const cycle = t % 9;
    const celebrating = cycle > 6.2;
    const juggleH = Math.abs(Math.sin(t * 2.6));
    if (!celebrating) {
      if (juggleH < 0.08 && !this.kicked) { p.triggerKick(false); this.kicked = true; }
      if (juggleH > 0.3) this.kicked = false;
      p.update(dt, { x: 0, y: 0, vx: 0, vy: 0, face: -Math.PI / 2 + 0.5, state: STATE.NORMAL, emote: 0, hold: 0 }, w.time);
      this.previewBall.position.set(0.35 * Math.cos(-0.5), 0.25 + juggleH * 1.3, 0.35 * Math.sin(0.5) + 0.15);
    } else {
      p.update(dt, { x: 0, y: 0, vx: 0, vy: 0, face: -Math.PI / 2 + 0.3, state: STATE.CELEBRATE, emote: charOf(this.settings.char).celebration, hold: 0 }, w.time);
      this.previewBall.position.set(0.6, BALL_R, 0.8);
    }
    const a = t * 0.12;
    const wide = this.width > this.height;
    const r = wide ? 7 : 8;
    this.camera.position.set(Math.sin(a) * r * 0.5, 1.9, r);
    this.camera.lookAt(wide ? -0.25 : 0, wide ? 1.1 : 0.4, 0);
    if (Math.abs(this.camera.fov - 42) > 0.1) { this.camera.fov = 42; this.camera.updateProjectionMatrix(); }
    w.update(dt, new THREE.Vector3(), { excite: 0.25 });
    this.render(w.scene, this.camera);
  }

  // ---------- التقدم (المستوى والنقاط) ----------
  career() {
    const c = this.settings.career || (this.settings.career = { xp: 0, coins: 0, matches: 0, wins: 0, goals: 0, cups: 0 });
    return c;
  }
  levelOf(xp) { return Math.floor(Math.sqrt(xp / 120)) + 1; }
  levelXp(l) { return (l - 1) * (l - 1) * 120; }
  renderCareer() {
    const c = this.career();
    const l = this.levelOf(c.xp);
    const k = (c.xp - this.levelXp(l)) / (this.levelXp(l + 1) - this.levelXp(l));
    $('career').innerHTML = `<span class="lvl">⭐ ${l}</span><div class="xpbar"><i style="width:${Math.round(k * 100)}%"></i></div><span>🪙 ${c.coins}</span>${c.cups ? `<span>🏆 ${c.cups}</span>` : ''}`;
  }

  // ---------- كأس الأساطير ----------
  startCup(round = 0) {
    const R = CUP_ROUNDS[round];
    const others = STADIUMS.filter((x) => x.id !== 'neon');
    this.cup = { round, results: this.cup && round > 0 ? this.cup.results : [] };
    Object.assign(this.quick, { difficulty: R.diff, duration: 180, stadium: R.stadium || others[Math.floor(Math.random() * others.length)].id, team: 0 });
    this.startQuick(true);
    setTimeout(() => this.match && this.match.banner(`🏆 ${R.name}`, `كأس الأساطير — ${DIFFICULTY[R.diff].label}`, 'goal', '#fbbf24'), 400);
  }

  // ---------- المباراة ----------
  startQuick(keepCup) {
    if (!keepCup) this.cup = null;
    this.loading(true, 'جارٍ تجهيز الملعب…');
    setTimeout(() => {
      const roster = [[], []];
      roster[this.quick.team][this.quick.slot] = { name: this.settings.name, char: this.settings.char, human: true };
      const you = this.quick.team * TEAM_SIZE + this.quick.slot;
      const tr = new LocalTransport({ ...this.quickSettings(), roster, you, seed: Math.floor(Math.random() * 1e9) });
      this.beginMatch({ stadium: this.quick.stadium, duration: this.quick.duration, roster: tr.roster(), you, local: true, transport: tr });
      tr.onSnapshot = (s, ev) => this.match && this.match.onSnapshot(s, ev);
      tr.onEnd = (d) => this.showEnd(d);
      this.loading(false);
    }, 30);
  }

  beginMatch(opts) {
    this.endMatch();
    if (this.menuWorld) { this.menuWorld.dispose(); this.menuWorld = null; }
    this.show(null);
    $('endscreen').classList.add('hidden');
    $('pause').classList.add('hidden');
    this.paused = false;
    audio.init();
    this.perf = null;
    this.match = new Match(this, opts);
    this.composerScene = null;
    this.input.enabled = true;
    this.input.showTouch(true);
    document.body.classList.add('inmatch');
    if (isMobile && !this.rotateDismissed) {
      $('rotate').classList.remove('hidden');
      // محاولة تثبيت الوضع الأفقي (يعمل على أندرويد في وضع ملء الشاشة)
      const el = document.documentElement;
      Promise.resolve(el.requestFullscreen ? el.requestFullscreen() : null)
        .then(() => screen.orientation && screen.orientation.lock && screen.orientation.lock('landscape'))
        .catch(() => {});
    }
    this.input.keys.clear();
  }

  endMatch() {
    if (this.match) { this.match.destroy(); this.match = null; }
    this.input.enabled = false;
    this.input.showTouch(false);
    document.body.classList.remove('inmatch');
  }

  quitMatch() {
    const wasOnline = this.match && !this.match.local;
    $('pause').classList.add('hidden');
    $('endscreen').classList.add('hidden');
    this.endMatch();
    if (wasOnline || this.room) { this.net.send({ t: 'leave' }); this.room = null; }
    this.setupMenuScene(this.quick.stadium);
    this.show('main');
    this.renderCareer();
  }

  showEnd(d) {
    if (!this.match) return;
    const m = this.match;
    const you = m.you;
    const myTeam = you >= 0 ? m.roster[you].team : -1;
    const [a, b] = d.score;
    let title = a === b ? 'تعادل 🤝' : `فوز ${TEAMS[a > b ? 0 : 1].name} 🏆`;
    if (myTeam >= 0 && a !== b) title = (a > b ? 0 : 1) === myTeam ? 'فزت! 🏆🎉' : 'خسرت… حظاً أوفر 😔';
    $('end-title').textContent = title;
    $('end-score').innerHTML = `<span style="background:${TEAMS[0].color}">${TEAMS[0].name}</span> ${a} - ${b} <span style="background:${TEAMS[1].color}">${TEAMS[1].name}</span>`;
    const stats = d.stats;
    const score = (s) => s.g * 5 + s.a * 3 + s.sv * 2 + s.tk + s.sh * 0.5 + s.ps * 0.2;
    const mvp = stats.slice().sort((x, y) => score(y) - score(x))[0];
    $('end-mvp').innerHTML = mvp ? `⭐ أفضل لاعب: <b>${esc(mvp.name)}</b> ${charOf(mvp.char).icon} — ${mvp.g} أهداف، ${mvp.a} تمريرات حاسمة، ${mvp.sv} تصديات` : '';
    const rows = stats.slice().sort((x, y) => x.team - y.team || y.g - x.g).map((s) => `<tr class="${s.id === you ? 'me' : ''}"><td><span style="color:${TEAMS[s.team].color}">●</span> ${charOf(s.char).icon} ${esc(s.name)}</td><td>${s.g}</td><td>${s.a}</td><td>${s.sh}</td><td>${s.ps}</td><td>${s.tk}</td><td>${s.sv}</td></tr>`).join('');
    $('end-stats').innerHTML = `<table class="st"><tr><th>اللاعب</th><th>⚽</th><th>🅰️</th><th>تسديد</th><th>تمرير</th><th>افتكاك</th><th>تصدي</th></tr>${rows}</table>`;
    $('end-again').textContent = m.local ? '🔁 مباراة جديدة' : '↩ العودة للغرفة';
    // الاستحواذ
    if (d.poss) $('end-score').insertAdjacentHTML('beforeend', `<div class="muted small" dir="rtl" style="margin-top:4px">الاستحواذ: ${TEAMS[0].name} ${d.poss[0]}٪ • ${TEAMS[1].name} ${d.poss[1]}٪</div>`);
    // التقدم
    const won = myTeam >= 0 && a !== b && (a > b ? 0 : 1) === myTeam;
    const draw = a === b;
    let xpHtml = '';
    if (you >= 0 && !m.xpGiven) {
      m.xpGiven = true;
      const me = stats.find((x) => x.id === you) || { g: 0, a: 0, sv: 0, tk: 0 };
      const c = this.career();
      const before = this.levelOf(c.xp);
      let gain = 40 + me.g * 35 + me.a * 20 + me.sv * 15 + me.tk * 5 + (won ? 100 : draw ? 40 : 0);
      if (mvp && mvp.id === you) gain += 40;
      const coins = Math.round(gain / 4) + (this.cup && won && this.cup.round === 2 ? 250 : 0);
      c.xp += gain; c.coins += coins; c.matches++; c.goals += me.g; if (won) c.wins++;
      const after = this.levelOf(c.xp);
      xpHtml = `<span class="gain">+${gain} XP</span> &nbsp; 🪙 +${coins}${mvp && mvp.id === you ? ' &nbsp; ⭐ أفضل لاعب +40' : ''}` + (after > before ? `<br><span class="lvlup">⬆️ مستوى جديد: ${after}!</span>` : '');
      this.saveSettings();
    }
    $('end-xp').innerHTML = xpHtml;
    // الكأس
    const cupEl = $('end-cup');
    if (this.cup && m.local) {
      const cup = this.cup;
      cup.results[cup.round] = won ? 'won' : 'lost';
      cup.next = null; cup.champion = false;
      if (won && cup.round < CUP_ROUNDS.length - 1) { cup.next = cup.round + 1; $('end-again').textContent = `▶ ${CUP_ROUNDS[cup.next].name}`; }
      else if (won) {
        cup.champion = true;
        this.career().cups++; this.saveSettings();
        $('end-title').innerHTML = '<div class="trophy">🏆</div>بطل كأس الأساطير!';
        $('end-again').textContent = '🏠 احتفل وارجع للقائمة';
        audio.goalParty(); audio.chant(0);
      } else { $('end-title').textContent = 'خرجت من الكأس 😔'; $('end-again').textContent = '🔁 أعد المحاولة من البداية'; }
      cupEl.innerHTML = CUP_ROUNDS.map((r, i) => `<span class="${cup.results[i] || (i === cup.round ? 'cur' : '')}">${cup.results[i] === 'won' ? '✅' : cup.results[i] === 'lost' ? '❌' : '⚪'} ${r.name}</span>`).join('');
      cupEl.classList.remove('hidden');
    } else cupEl.classList.add('hidden');
    $('endscreen').classList.remove('hidden');
    this.input.showTouch(false);
  }

  // ---------- الشبكة ----------
  setupNet() {
    const n = this.net;
    n.on('close', () => {
      $('net-status').textContent = '⚠️ انقطع الاتصال بالخادم';
      $('net-status').className = 'status bad';
      if (this.match && !this.match.local) { this.toast('انقطع الاتصال بالخادم'); this.quitMatch(); }
      this.room = null;
    });
    n.on('rooms', (m) => this.renderRoomList(m.list));
    n.on('err', (m) => this.toast('⚠️ ' + m.msg));
    n.on('room', (m) => {
      const first = !this.room || this.room.code !== m.room.code;
      this.room = m.room;
      if (first) history.replaceState(null, '', `?room=${m.room.code}`);
      if (!this.match) { if (this.screen !== 'room') this.show('room'); this.renderRoom(); }
      else this.renderRoom();
    });
    n.on('left', () => { this.room = null; history.replaceState(null, '', location.pathname); });
    n.on('match', (m) => {
      if (this.match && !this.match.local) { this.match.setRoster(m.roster, m.you); return; }
      $('endscreen').classList.add('hidden');
      this.beginMatch({ stadium: m.stadium, duration: m.duration, roster: m.roster, you: m.you, local: false, transport: new NetTransport(n) });
      this.match.rtt = n.rtt;
      if (m.you < 0) this.toast('أنت متفرج 👁 — اختر مقعداً من القائمة للانضمام');
    });
    n.on('s', (m) => { if (this.match && !this.match.local) { this.match.rtt = n.rtt; this.match.onSnapshot(m.s, m.ev); } });
    n.on('end', (m) => this.showEnd(m));
    n.on('lobby', () => {
      if (this.match && !this.match.local) {
        this.endMatch();
        $('endscreen').classList.add('hidden');
        this.show('room');
        this.renderRoom();
      }
    });
    n.on('chat', (m) => {
      const line = `<div><b style="color:${m.team >= 0 ? TEAMS[m.team].color : '#ffd23f'}">${esc(m.from)}:</b> ${esc(m.text)}</div>`;
      const rc = $('room-chat');
      rc.insertAdjacentHTML('beforeend', line);
      rc.scrollTop = rc.scrollHeight;
      if (this.match) {
        const cl = $('chatlog');
        cl.insertAdjacentHTML('beforeend', line);
        const el = cl.lastElementChild;
        setTimeout(() => { el.style.opacity = 0; }, 7000);
        setTimeout(() => el.remove(), 8000);
        while (cl.children.length > 6) cl.firstChild.remove();
      }
    });
  }

  async connectOnline() {
    const st = $('net-status');
    if (this.net.connected) { st.textContent = '✅ متصل بالخادم'; st.className = 'status ok'; return; }
    st.textContent = 'جارٍ الاتصال بالخادم…'; st.className = 'status';
    try {
      await this.net.connect();
      this.net.send({ t: 'hello', name: this.settings.name, char: this.settings.char });
      st.textContent = '✅ متصل بالخادم — أنشئ غرفة أو انضم لأصدقائك';
      st.className = 'status ok';
      this.refreshRooms();
    } catch (e) {
      st.textContent = '❌ تعذر الاتصال بالخادم. شغّل الخادم (npm start) أو جرّب اللعب السريع.';
      st.className = 'status bad';
      throw e;
    }
  }

  refreshRooms() { if (this.net.connected) this.net.send({ t: 'list' }); }

  renderRoomList(list) {
    const el = $('room-list');
    if (!list.length) { el.innerHTML = '<div class="muted">لا توجد غرف حالياً — أنشئ واحدة!</div>'; return; }
    el.innerHTML = list.map((r) => `<div class="room-item"><div><b>${esc(r.name)}</b><br><small class="muted">${stadiumOf(r.stadium).name} • ${r.players} لاعبين ${r.playing ? '• 🔴 جارية' : ''}</small></div><button class="tiny primary" data-code="${r.code}">انضم</button></div>`).join('');
    el.onclick = (e) => { const b = e.target.closest('[data-code]'); if (b) this.net.send({ t: 'join', code: b.dataset.code }); };
  }

  renderRoom() {
    const r = this.room;
    if (!r) return;
    $('room-code').textContent = r.code;
    const me = this.net.id;
    const isHost = r.host === me;
    $('room-playing').classList.toggle('hidden', !r.playing);
    for (const team of [0, 1]) {
      const html = [];
      for (let slot = 0; slot < TEAM_SIZE; slot++) {
        const m = r.members.find((x) => x.team === team && x.slot === slot);
        if (m) {
          const c = charOf(m.char);
          html.push(`<div class="slot ${m.id === me ? 'me' : ''}"><span class="pos">${POS_NAMES[slot]}</span><span class="who">${c.icon} ${esc(m.name)} ${m.id === r.host ? '👑' : ''}</span><small class="muted">${c.name}</small></div>`);
        } else {
          html.push(`<div class="slot bot"><span class="pos">${POS_NAMES[slot]}</span><span class="who">🤖 بوت</span><button class="tiny" data-team="${team}" data-slot="${slot}">اجلس هنا</button></div>`);
        }
      }
      const el = $('slots' + team);
      el.innerHTML = html.join('');
      el.onclick = (e) => { const b = e.target.closest('[data-slot]'); if (b) this.net.send({ t: 'slot', team: +b.dataset.team, slot: +b.dataset.slot }); };
    }
    const specs = r.members.filter((m) => m.team < 0);
    $('spectators').textContent = specs.length ? `👁 متفرجون: ${specs.map((s) => s.name).join('، ')}` : '';
    for (const [id, v] of [['room-stadium', r.settings.stadium], ['room-dur', r.settings.duration], ['room-diff', r.settings.difficulty]]) {
      $(id).value = String(v);
      $(id).disabled = !isHost || r.playing;
    }
    $('host-note').textContent = isHost ? '(أنت المضيف 👑)' : '(يحددها المضيف)';
    $('btn-start').classList.toggle('hidden', !isHost || r.playing);
    if (r.playing && !this.match) $('btn-start').classList.add('hidden');
    if (!this.match) this.setupMenuScene(r.settings.stadium);
  }

  // خفض الدقة تلقائياً إذا كان الهاتف بطيئاً
  adaptQuality(dt) {
    const q = this.perf || (this.perf = { t: 0, frames: 0, time: 0, pr: this.renderer.getPixelRatio(), warned: false });
    q.t += dt; q.frames++; q.time += dt;
    if (q.t < 4) { if (q.t < 2) { q.frames = 0; q.time = 0; } return; }
    if (q.time >= 2.5) {
      const fps = q.frames / q.time;
      q.frames = 0; q.time = 0;
      if (fps < 40 && q.pr > 0.6) {
        q.pr = Math.max(0.6, q.pr - 0.2);
        this.renderer.setPixelRatio(q.pr);
        this.onResize();
        if (!q.warned) { q.warned = true; this.toast('⚙️ تم ضبط الجودة تلقائياً لتشغيل أنعم'); }
      } else if (fps < 28 && this.renderer.shadowMap.enabled && this.match) {
        this.renderer.shadowMap.enabled = false;
        this.match.world.sun.castShadow = false;
        this.match.world.scene.traverse((o) => { if (o.material) (Array.isArray(o.material) ? o.material : [o.material]).forEach((m) => (m.needsUpdate = true)); });
      }
    }
  }

  // ---------- الحلقة ----------
  loop(t) {
    requestAnimationFrame((tt) => this.loop(tt));
    const dt = Math.min(0.05, (t - this.last) / 1000);
    this.last = t;
    if (this.match) {
      const inp = this.paused ? { mx: 0, my: 0, b: 0 } : this.input.read();
      this.match.frame(dt, inp);
      this.adaptQuality(dt);
    } else this.updateMenu(dt);
  }
}

const boot = () => {
  try {
    window.app = new App();
  } catch (e) {
    console.error(e);
    document.body.insertAdjacentHTML('beforeend', `<div style="position:fixed;inset:0;display:grid;place-items:center;background:#0b1220;color:#fff;font-family:Tahoma;padding:20px;text-align:center">تعذر تشغيل اللعبة: متصفحك لا يدعم WebGL.<br>${esc(e.message)}</div>`);
  }
};
if (document.readyState === 'loading') window.addEventListener('DOMContentLoaded', boot); else boot();
