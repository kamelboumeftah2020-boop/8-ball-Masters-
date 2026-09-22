// تشغيل المباراة في المتصفح: الاستيفاء، التنبؤ، الإعادة، الكاميرات، الواجهة، ردود فعل الأحداث
import * as THREE from 'three';
import { World, toV } from './scene.js';
import { Effects } from './effects.js';
import { Player3D } from './player3d.js';
import { audio } from './audio.js';
import { FIELD, BALL_R, TEAMS, PHASE, STATE, BTN, EMOTES } from '/shared/constants.js';
import { charOf } from '/shared/characters.js';
import { stadiumOf } from '/shared/stadiums.js';
import { integrateMovement } from '/shared/sim.js';

const { L, W, GW } = FIELD;
const HL = L / 2, HW = W / 2;
const PI = Math.PI;
const $ = (id) => document.getElementById(id);
const lerp = (a, b, k) => a + (b - a) * k;
const lerpAng = (a, b, k) => a + Math.atan2(Math.sin(b - a), Math.cos(b - a)) * k;
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const clamp = (v, a, b) => (v < a ? a : v > b ? b : v);

const SAY = {
  goal: ['هدددددف!', 'يا سلام! هدف رائع!', 'هدف! الشباك تهتز!', 'جووول! ما أجمله من هدف!'],
  save: ['تصدي رائع من الحارس!', 'يا له من حارس!', 'أنقذ الموقف!'],
  post: ['القائم! يا للحظ!', 'العارضة تنقذ المرمى!'],
  miss: ['قريبة جداً!', 'أوه! كادت أن تدخل!'],
  fire: ['كرة نارية!'],
  start: ['انطلقت المباراة!'],
  golden: ['الهدف الذهبي! من يسجل يفوز!'],
};

function ballTexture() {
  const w = 512, h = 256;
  const c = document.createElement('canvas');
  c.width = w; c.height = h;
  const g = c.getContext('2d');
  const img = g.createImageData(w, h);
  const phi = (1 + Math.sqrt(5)) / 2;
  const verts = [];
  for (const [a, b] of [[1, phi], [-1, phi], [1, -phi], [-1, -phi]]) verts.push([0, a, b], [a, b, 0], [b, 0, a]);
  const V = verts.map((v) => { const l = Math.hypot(...v); return v.map((x) => x / l); });
  const basis = V.map((n) => {
    const up = Math.abs(n[1]) < 0.9 ? [0, 1, 0] : [1, 0, 0];
    let t1 = [up[1] * n[2] - up[2] * n[1], up[2] * n[0] - up[0] * n[2], up[0] * n[1] - up[1] * n[0]];
    const l = Math.hypot(...t1); t1 = t1.map((x) => x / l);
    const t2 = [n[1] * t1[2] - n[2] * t1[1], n[2] * t1[0] - n[0] * t1[2], n[0] * t1[1] - n[1] * t1[0]];
    return [t1, t2];
  });
  const r0 = 0.37;
  for (let y = 0; y < h; y++) {
    const lat = (0.5 - y / h) * PI;
    for (let x = 0; x < w; x++) {
      const lon = (x / w) * 2 * PI - PI;
      const d = [Math.cos(lat) * Math.cos(lon), Math.sin(lat), Math.cos(lat) * Math.sin(lon)];
      let best = -2, second = -2, bi = 0;
      for (let i = 0; i < 12; i++) {
        const dot = d[0] * V[i][0] + d[1] * V[i][1] + d[2] * V[i][2];
        if (dot > best) { second = best; best = dot; bi = i; } else if (dot > second) second = dot;
      }
      const ang = Math.acos(Math.min(1, best));
      const [t1, t2] = basis[bi];
      const pa = Math.atan2(d[0] * t2[0] + d[1] * t2[1] + d[2] * t2[2], d[0] * t1[0] + d[1] * t1[1] + d[2] * t1[2]);
      const seg = (2 * PI) / 5;
      const m = ((pa % seg) + seg) % seg - seg / 2;
      const bound = (r0 * Math.cos(PI / 5)) / Math.cos(m);
      let col = 245;
      if (ang < bound) col = 25;
      else if (best - second < 0.012) col = 150;
      const k = (y * w + x) * 4;
      img.data[k] = col; img.data[k + 1] = col; img.data[k + 2] = col + (col > 200 ? 8 : 0); img.data[k + 3] = 255;
    }
  }
  g.putImageData(img, 0, 0);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  return t;
}
let BALL_TEX = null;

export class Match {
  constructor(app, opts) {
    this.app = app;
    this.opts = opts;
    this.local = !!opts.local;
    this.transport = opts.transport;
    this.q = app.settings.quality;
    this.stadium = stadiumOf(opts.stadium);
    this.world = new World(app.renderer, this.stadium, this.q);
    this.scene = this.world.scene;
    this.fx = new Effects(this.world, this.q);
    this.fx.onBoom = () => audio.drum(audio.ctx ? audio.ctx.currentTime : 0, 0.4, 50);
    this.world.onThunder = () => audio.thunder(0.9);
    this.camera = app.camera;
    this.camMode = app.settings.camera || 'broadcast';
    this.camPos = new THREE.Vector3(0, 18, HW + 18);
    this.camLook = new THREE.Vector3();
    this.buf = [];
    this.history = [];
    this.evQueue = [];
    this.evHistory = [];
    this.offset = null;
    this.delay = this.local ? 1 / 60 : 0.1;
    this.rtt = 0.08;
    this.latest = null;
    this.replay = null;
    this.pred = null;
    this.excite = 0.2;
    this.lastPhase = null;
    this.bannerT = 0;
    this.feed = [];
    this.goalTimes = [];
    this.gameStart = null;
    this.ended = false;
    this.lastInput = { mx: 0, my: 0, b: 0 };
    this.inputSendT = 0;
    this.players3d = [];
    const blobTex = (() => {
      const c = document.createElement('canvas'); c.width = c.height = 64;
      const g = c.getContext('2d');
      const gr = g.createRadialGradient(32, 32, 0, 32, 32, 32);
      gr.addColorStop(0, 'rgba(0,0,0,0.6)'); gr.addColorStop(1, 'rgba(0,0,0,0)');
      g.fillStyle = gr; g.fillRect(0, 0, 64, 64);
      return new THREE.CanvasTexture(c);
    })();
    this.blobTex = blobTex;
    this.setRoster(opts.roster, opts.you);
    // الكرة
    if (!BALL_TEX) BALL_TEX = ballTexture();
    this.ball = new THREE.Mesh(new THREE.SphereGeometry(BALL_R, 28, 18), new THREE.MeshStandardMaterial({ map: BALL_TEX, roughness: 0.45 }));
    this.ball.castShadow = true;
    this.scene.add(this.ball);
    this.ballBlob = new THREE.Mesh(new THREE.PlaneGeometry(1, 1), new THREE.MeshBasicMaterial({ map: blobTex, transparent: true, depthWrite: false }));
    this.ballBlob.rotation.x = -PI / 2;
    this.scene.add(this.ballBlob);
    // سهم التصويب
    const arrowShape = new THREE.Shape();
    arrowShape.moveTo(0, -0.12); arrowShape.lineTo(1, -0.12); arrowShape.lineTo(1, -0.3); arrowShape.lineTo(1.5, 0); arrowShape.lineTo(1, 0.3); arrowShape.lineTo(1, 0.12); arrowShape.lineTo(0, 0.12);
    this.aim = new THREE.Mesh(new THREE.ShapeGeometry(arrowShape), new THREE.MeshBasicMaterial({ color: '#fff27a', transparent: true, opacity: 0.8, depthWrite: false, side: THREE.DoubleSide }));
    this.aim.rotation.x = -PI / 2;
    this.aim.visible = false;
    const aimWrap = new THREE.Group();
    aimWrap.add(this.aim);
    this.aimWrap = aimWrap;
    this.scene.add(aimWrap);
    // مؤشر مستلم التمريرة
    this.passRing = new THREE.Mesh(new THREE.RingGeometry(0.55, 0.78, 32), new THREE.MeshBasicMaterial({ color: '#7dffb0', transparent: true, opacity: 0.85, depthWrite: false }));
    this.passRing.rotation.x = -PI / 2;
    this.passRing.visible = false;
    this.scene.add(this.passRing);

    audio.stadiumMood = this.stadium.crowd;
    audio.drums = true;
    this.setupHUD();
    this.onResize();
  }

  // ---------- اللاعبون ----------
  setRoster(roster, you) {
    this.roster = roster;
    const youChanged = this.you !== you;
    this.you = you;
    this.pred = null;
    roster.forEach((r, i) => {
      const old = this.players3d[i];
      const key = `${r.char}|${r.name}|${r.human}|${r.id === you}`;
      if (old && old.key === key) return;
      if (old) { this.scene.remove(old.root); old.dispose(); }
      const p3 = new Player3D(r, { local: r.id === you });
      p3.key = key;
      p3.onSiu = () => audio.siu();
      if (this.q === 'low') {
        const blob = new THREE.Mesh(new THREE.PlaneGeometry(1.3, 1.3), new THREE.MeshBasicMaterial({ map: this.blobTex, transparent: true, depthWrite: false }));
        blob.rotation.x = -PI / 2; blob.position.y = 0.02;
        p3.root.add(blob);
      }
      this.players3d[i] = p3;
      this.scene.add(p3.root);
    });
    if (youChanged && this.hudReady) this.updateCard(true);
  }

  // ---------- الشبكة ----------
  onSnapshot(s, ev) {
    const now = performance.now() / 1000;
    const sample = s.t - now;
    if (this.offset === null || sample > this.offset) this.offset = sample;
    else this.offset += (sample - this.offset) * 0.02;
    this.buf.push(s);
    this.history.push(s);
    while (this.buf.length > 2 && this.buf[1].t < s.t - 1.5) this.buf.shift();
    while (this.history.length && this.history[0].t < s.t - 14) this.history.shift();
    this.latest = s;
    if (ev && ev.length) {
      for (const e of ev) { this.evQueue.push(e); this.evHistory.push(e); }
      while (this.evHistory.length && this.evHistory[0].t < s.t - 14) this.evHistory.shift();
    }
    if (this.gameStart === null) this.gameStart = s.t;
  }

  sampleFrom(arr, t) {
    if (!arr.length) return null;
    if (t <= arr[0].t) return arr[0];
    const last = arr[arr.length - 1];
    if (t >= last.t) return last;
    let i = arr.length - 1;
    while (i > 0 && arr[i - 1].t > t) i--;
    const a = arr[i - 1], b = arr[i];
    const k = (t - a.t) / Math.max(1e-6, b.t - a.t);
    return this.lerpSnap(a, b, k);
  }

  lerpSnap(a, b, k) {
    const pb = b.b, pa = a.b;
    const jumpB = Math.hypot(pb[0] - pa[0], pb[1] - pa[1]) > 6;
    const ball = pb.slice();
    if (!jumpB) for (let i = 0; i < 6; i++) ball[i] = lerp(pa[i], pb[i], k);
    const p = b.p.map((q, i) => {
      const o = a.p[i];
      if (!o || Math.hypot(q[0] - o[0], q[1] - o[1]) > 5) return q;
      const r = q.slice();
      r[0] = lerp(o[0], q[0], k); r[1] = lerp(o[1], q[1], k);
      r[2] = lerp(o[2], q[2], k); r[3] = lerp(o[3], q[3], k);
      r[4] = lerpAng(o[4], q[4], k);
      if (k < 0.5) { r[5] = o[5]; r[6] = o[6]; }
      return r;
    });
    return { ...b, b: ball, p, t: lerp(a.t, b.t, k) };
  }

  // ---------- الإطار ----------
  frame(dt, input) {
    this.frameDt = dt;
    const now = performance.now() / 1000;
    if (this.local && this.transport.update) this.transport.update(dt);
    if (!this.latest) { this.app.renderer.render(this.scene, this.camera); return; }
    const renderT = now + this.offset - this.delay;
    const latest = this.latest;
    // الإعادة
    if (latest.ph === PHASE.REPLAY && !this.replay && this.goalT != null) {
      this.replay = { t: this.goalT - 3.6, end: this.goalT + 0.9, prev: this.goalT - 3.6 };
      $('replay').classList.remove('hidden');
      this.fx.trail.pts.length = 0;
    }
    if (this.replay && latest.ph !== PHASE.REPLAY) { this.replay = null; $('replay').classList.add('hidden'); }
    let state;
    if (this.replay) {
      const r = this.replay;
      r.prev = r.t;
      r.t = Math.min(r.end, r.t + dt * 0.72);
      state = this.sampleFrom(this.history, r.t);
      for (const e of this.evHistory) if (e.t > r.prev && e.t <= r.t) this.handleEvent(e, true);
    } else {
      state = this.sampleFrom(this.buf, renderT);
      while (this.evQueue.length && this.evQueue[0].t <= renderT + 0.02) this.handleEvent(this.evQueue.shift(), false);
      if (this.evQueue.length > 200) this.evQueue.splice(0, this.evQueue.length - 200);
    }
    if (!state) return;
    this.state = state;

    // الإدخال
    const inp = this.transformInput(input);
    this.sendInput(inp, dt);

    // التنبؤ للاعب المحلي (أونلاين)
    const you = this.you;
    let meRender = null;
    if (!this.local && you >= 0 && !this.replay && latest.p[you]) meRender = this.predict(dt, inp, latest, state);

    // تحديث اللاعبين
    const ph = state.ph;
    const tNow = this.world.time;
    const showLabels = !this.replay && ph !== PHASE.GOAL;
    state.p.forEach((q, i) => {
      const p3 = this.players3d[i];
      if (!p3) return;
      if (p3.label) p3.label.visible = showLabels;
      let x = q[0], y = q[1], vx = q[2], vy = q[3], face = q[4];
      if (meRender && i === you) { x = meRender.x; y = meRender.y; vx = meRender.vx; vy = meRender.vy; face = meRender.face; }
      const owner = state.b[6] === i;
      p3.update(dt, {
        x, y, vx, vy, face, state: q[5], emote: q[6], hold: q[10] || 0,
        gkHold: owner && state.b[8] === 1,
        throwIn: state.ph === PHASE.SETPIECE && state.sp === i && state.spk === 'throw',
        ballX: state.b[0], ballY: state.b[1],
      }, tNow);
      if ((q[9] & 32) && Math.hypot(vx, vy) > 6) this.fx.sprintDust(x, y);
      if (q[9] & 31) this.fx.playerBuffs(p3, q[9], x, y);
      if (q[5] === STATE.SLIDE && Math.random() < 0.5) this.fx.slideSpray(x, y, vx, vy);
      if ((q[9] & 8) && state.b[6] < 0) this.fx.magnetPull(x, y, state.b[0], state.b[1], state.b[2]);
    });

    // الكرة
    let b = state.b;
    if (meRender && b[6] === you) {
      const sp = state.p[you];
      b = b.slice();
      b[0] = meRender.x + (b[0] - sp[0]); b[1] = meRender.y + (b[1] - sp[1]);
    }
    const bp = toV(b[0], b[1], b[2]);
    const prevBall = this.ball.position.clone();
    this.ball.position.copy(bp);
    const mv = bp.clone().sub(prevBall);
    const dist = mv.length();
    if (dist > 0.0001 && dist < 3) {
      const axis = new THREE.Vector3(0, 1, 0).cross(mv).normalize();
      if (axis.lengthSq() > 0.5) this.ball.rotateOnWorldAxis(axis, dist / BALL_R);
    }
    if (b[9]) this.ball.rotateOnWorldAxis(new THREE.Vector3(0, 1, 0), b[9] * dt * 3);
    this.ballBlob.position.set(bp.x, 0.025, bp.z);
    const hb = Math.max(0, b[2] - BALL_R);
    const bs = 0.55 + hb * 0.25;
    this.ballBlob.scale.set(bs, bs, 1);
    this.ballBlob.material.opacity = Math.max(0.15, 0.8 - hb * 0.18);
    this.fx.ballFx(b, b[7], dt);
    if (this.stadium.wet && b[2] < 0.25 && Math.hypot(b[3], b[4]) > 5 && Math.random() < 0.3) this.fx.splash(b[0], b[1]);

    // سهم التصويب
    this.updateAim(state, inp, meRender);
    this.updatePassTarget(state, inp, meRender);
    this.fx.syncZones(state.z);

    // الحماس
    const attackZone = Math.abs(b[0]) > HL - 14 ? 0.35 : 0.12;
    let target = attackZone;
    if (ph === PHASE.GOAL) target = 1;
    if (ph === PHASE.END) target = 0.8;
    this.excite += (target - this.excite) * Math.min(1, dt * 0.8);
    audio.update(dt, this.excite);

    // الكاميرا والمشهد
    this.updateCamera(dt, state, meRender);
    const focus = this.camLook;
    this.world.update(dt, focus, {
      excite: this.excite,
      jumbo: {
        score: `${state.sc[0]} - ${state.sc[1]}`,
        sub: ph === PHASE.GOAL ? 'هدف!!!' : state.gd ? 'هدف ذهبي' : this.fmtTime(state.tl),
        flash: ph === PHASE.GOAL && Math.floor(this.world.time * 4) % 2 === 0,
      },
      goalBoards: ph === PHASE.GOAL || ph === PHASE.REPLAY,
    });
    this.fx.update(dt);
    this.updateHUD(this.replay ? { ...state, ph: latest.ph, pt: latest.pt, tl: latest.tl, sc: latest.sc, gd: latest.gd } : state, dt);
    if (this.lastPhase !== ph) this.onPhase(ph, this.lastPhase, state);
    this.lastPhase = ph;
    this.app.render(this.scene, this.camera);
  }

  transformInput(input) {
    // تحويل اتجاه الشاشة إلى اتجاه الملعب حسب زاوية الكاميرا
    const fwd = new THREE.Vector3();
    this.camera.getWorldDirection(fwd);
    let fx = fwd.x, fy = -fwd.z;
    const l = Math.hypot(fx, fy) || 1;
    fx /= l; fy /= l;
    const rx = fy, ry = -fx;
    const out = { mx: input.mx * rx + input.my * fx, my: input.mx * ry + input.my * fy, b: input.b };
    this.applyTouch(out, input);
    return out;
  }

  // أزرار ذكية للجوال: تتغير حسب الموقف (هجوم / دفاع)
  applyTouch(out, raw) {
    const dt = this.frameDt || 0.016;
    const st = this.latest;
    const you = this.you;
    if (!st || you < 0) return;
    const b = st.b, me = st.p[you];
    const myTeam = this.roster[you].team;
    const ownerTeam = b[6] >= 0 && this.roster[b[6]] ? this.roster[b[6]].team : -1;
    if (!raw.tMain) {
      const ctx = ownerTeam >= 0 && ownerTeam !== myTeam ? 'def' : 'att';
      const gk = this.roster[you].slot === 0 && Math.abs(me[0] - (myTeam === 0 ? -HL : HL)) < 9;
      const key = ctx + (gk ? 'gk' : '');
      if (key !== this.touchCtx) { this.touchCtx = key; this.setTouchLabels(ctx, gk); }
    }
    const att = this.touchCtx && this.touchCtx.startsWith('att');
    if (att) {
      if (raw.tMain) out.b |= BTN.SHOOT;
      if (raw.tSec) {
        this.secT = (this.secT || 0) + dt;
        if (this.secT > 0.28) { out.b |= BTN.LOB; if (!this.lobShown) { this.lobShown = true; document.querySelector('#touch .t-sec')?.classList.add('lob'); } }
      } else {
        if (this.secT > 0 && this.secT <= 0.28) this.passPulse = 0.12;
        this.secT = 0;
        if (this.lobShown) { this.lobShown = false; document.querySelector('#touch .t-sec')?.classList.remove('lob'); }
      }
      if (this.passPulse > 0) { out.b |= BTN.PASS; this.passPulse -= dt; }
    } else {
      if (raw.tMain) out.b |= BTN.TACKLE;
      if (raw.tSec) out.b |= BTN.SPRINT;
      this.secT = 0; this.passPulse = 0;
    }
    if (raw.tMain) out.b |= BTN.SKIP;
  }

  setTouchLabels(ctx, gk) {
    const main = document.querySelector('#touch .t-main'), sec = document.querySelector('#touch .t-sec');
    if (!main) return;
    const att = ctx === 'att';
    main.classList.toggle('def', !att); sec.classList.toggle('def', !att);
    main.innerHTML = att ? '<span class="i">🦶</span><small>تسديد</small>' : gk ? '<span class="i">🧤</span><small>ارتماء</small>' : '<span class="i">🦵</span><small>افتكاك</small>';
    sec.innerHTML = att ? '<span class="i">➡️</span><small>تمرير</small>' : '<span class="i">⚡</span><small>ركض</small>';
  }

  sendInput(inp, dt) {
    const li = this.lastInput;
    this.inputSendT -= dt;
    const changed = Math.abs(inp.mx - li.mx) > 0.02 || Math.abs(inp.my - li.my) > 0.02 || inp.b !== li.b;
    if (changed || this.inputSendT <= 0) {
      const msg = { mx: Math.round(inp.mx * 100) / 100, my: Math.round(inp.my * 100) / 100, b: inp.b };
      this.transport.sendInput(msg);
      this.lastInput = msg;
      this.inputSendT = 0.1;
    }
  }

  predict(dt, inp, latest, state) {
    const you = this.you;
    const srv = latest.p[you];
    const r = this.roster[you];
    const c = charOf(r.char);
    const st = srv[5];
    const canMove = (latest.ph === PHASE.PLAY || latest.ph === PHASE.SETPIECE) && st === STATE.NORMAL && !(latest.ph === PHASE.SETPIECE && latest.sp === you);
    if (!this.pred || !canMove) {
      const s = state.p[you];
      this.pred = { x: s[0], y: s[1], vx: s[2], vy: s[3], face: s[4] };
      return this.pred;
    }
    const p = this.pred;
    const flags = srv[9];
    let mul = c.stats.speed;
    if (flags & 2) mul *= 1.5;
    if (flags & 16) mul *= 0.48;
    if (latest.b[6] === you && latest.b[8] !== 1) mul *= 0.93;
    if (srv[10] > 0) mul *= 0.8;
    const sprint = !!(inp.b & BTN.SPRINT) && (srv[7] > 0.03 || (flags & 2)) && Math.hypot(inp.mx, inp.my) > 0.2;
    integrateMovement(p, inp.mx, inp.my, sprint, dt, mul);
    const spd = Math.hypot(p.vx, p.vy);
    const ml = Math.hypot(inp.mx, inp.my);
    let want = null;
    if ((srv[10] > 0 || latest.b[6] === you) && ml > 0.2) want = Math.atan2(inp.my, inp.mx);
    else if (spd > 0.6) want = Math.atan2(p.vy, p.vx);
    if (want !== null) p.face += clamp(Math.atan2(Math.sin(want - p.face), Math.cos(want - p.face)), -12 * dt, 12 * dt);
    // تصحيح نحو موضع الخادم مع تعويض التأخير
    const lead = this.rtt * 0.5 + 0.033;
    const ex = srv[0] + srv[2] * lead, ey = srv[1] + srv[3] * lead;
    const err = Math.hypot(ex - p.x, ey - p.y);
    if (err > 3) { p.x = ex; p.y = ey; p.vx = srv[2]; p.vy = srv[3]; }
    else {
      const k = Math.min(1, dt * (err > 1 ? 8 : 4));
      p.x += (ex - p.x) * k; p.y += (ey - p.y) * k;
      p.vx += (srv[2] - p.vx) * k * 0.5; p.vy += (srv[3] - p.vy) * k * 0.5;
    }
    p.face = lerpAng(p.face, srv[4], Math.min(1, dt * 2));
    return p;
  }

  updatePassTarget(state, inp, meRender) {
    const you = this.you;
    const ring = this.passRing;
    ring.visible = false;
    if (you < 0 || this.replay || state.b[6] !== you) return;
    const me = state.p[you];
    const x = meRender ? meRender.x : me[0], y = meRender ? meRender.y : me[1];
    const ml = Math.hypot(inp.mx, inp.my);
    const a = ml > 0.2 ? Math.atan2(inp.my, inp.mx) : (meRender ? meRender.face : me[4]);
    const dx0 = Math.cos(a), dy0 = Math.sin(a);
    const team = this.roster[you].team;
    let best = null, bestS = Infinity;
    state.p.forEach((q, i) => {
      const r = this.roster[i];
      if (!r || i === you || r.team !== team || q[5] === STATE.DOWN) return;
      const dx = q[0] - x, dy = q[1] - y, d = Math.hypot(dx, dy);
      if (d < 2.5 || d > 45) return;
      const ang = Math.acos(clamp((dx0 * dx + dy0 * dy) / d, -1, 1));
      if (ang > 0.85) return;
      let open = 20;
      state.p.forEach((o, j) => { if (this.roster[j] && this.roster[j].team !== team) open = Math.min(open, Math.hypot(o[0] - q[0], o[1] - q[1])); });
      const sc = ang * 12 + d * 0.1 - open * 0.4;
      if (sc < bestS) { bestS = sc; best = q; }
    });
    if (!best) return;
    ring.visible = true;
    toV(best[0], best[1], 0.05, ring.position);
    const k = 1 + Math.sin(this.world.time * 8) * 0.12;
    ring.scale.set(k, k, k);
  }

  updateAim(state, inp, meRender) {
    const you = this.you;
    if (you < 0 || this.replay) { this.aim.visible = false; return; }
    const q = state.p[you];
    const hold = q[10] || 0;
    const owner = state.b[6] === you;
    if (!(hold > 0 || owner) || q[5] !== STATE.NORMAL) { this.aim.visible = false; return; }
    const x = meRender ? meRender.x : q[0], y = meRender ? meRender.y : q[1];
    const ml = Math.hypot(inp.mx, inp.my);
    const a = ml > 0.2 ? Math.atan2(inp.my, inp.mx) : (meRender ? meRender.face : q[4]);
    this.aim.visible = true;
    this.aimWrap.position.copy(toV(x, y, 0.04));
    this.aimWrap.rotation.y = a;
    const k = hold > 0 ? 1 + Math.min(1, hold) * 2 : 0.8;
    this.aim.scale.set(k, 1, 1);
    this.aim.position.x = 0.7;
    this.aim.material.color.set(hold > 0 ? (Math.min(1, hold) > 0.85 ? '#ff5a3a' : '#ffd23f') : '#ffffff');
    this.aim.material.opacity = hold > 0 ? 0.85 : 0.35;
  }

  // ---------- الكاميرا ----------
  updateCamera(dt, state, meRender) {
    const cam = this.camera;
    const b = state.b;
    const you = this.you;
    const me = you >= 0 ? state.p[you] : null;
    const portrait = this.app.height > this.app.width;
    const ph = state.ph;
    let pos = new THREE.Vector3(), look = new THREE.Vector3();
    let smooth = 4;
    const mx = meRender ? meRender.x : me ? me[0] : b[0];
    const my = meRender ? meRender.y : me ? me[1] : b[1];
    if (this.replay) {
      const ang = this.world.time * 0.25;
      look.copy(toV(b[0], b[1], Math.min(b[2], 2)));
      const side = Math.sign(b[0]) || 1;
      pos.set(look.x - side * 8 + Math.sin(ang) * 3, 3.2 + Math.min(b[2], 3) * 0.5, look.z + 9 + Math.cos(ang) * 2);
      smooth = 3;
    } else if (ph === PHASE.GOAL && this.goalInfo && this.goalInfo.p >= 0) {
      const s = state.p[this.goalInfo.p];
      look.copy(toV(s[0], s[1], 1.2));
      const a = this.world.time * 0.35;
      pos.set(look.x + Math.cos(a) * 6.5, 2.8, look.z + Math.sin(a) * 6.5);
      smooth = 2.5;
    } else if (ph === PHASE.END) {
      const a = this.world.time * 0.12;
      look.set(0, 1, 0);
      pos.set(Math.cos(a) * 34, 16, Math.sin(a) * 30);
      smooth = 1.5;
    } else {
      let fx = b[0], fy = b[1];
      if (me) { fx = lerp(b[0], mx, 0.45); fy = lerp(b[1], my, 0.45); }
      if (this.camMode === 'behind' && me) {
        const team = this.roster[you].team;
        const d = team === 0 ? 1 : -1;
        look.copy(toV(lerp(mx, b[0], 0.4) + d * 6, lerp(my, b[1], 0.4), 0));
        pos.copy(toV(mx - d * (portrait ? 14 : 11), my * 0.9, portrait ? 11 : 8));
      } else if (this.camMode === 'top') {
        look.copy(toV(clamp(fx, -HL + 10, HL - 10), fy * 0.5, 0));
        pos.set(look.x, portrait ? 48 : 36, look.z + 9);
      } else {
        const cx = clamp(fx, -HL + (portrait ? 6 : 8), HL - (portrait ? 6 : 8));
        look.copy(toV(cx, fy * 0.75 + (portrait ? 0 : 0.5), 0));
        const touch = document.body.classList.contains('touch');
        const h = portrait ? 30 : touch ? 10.5 : 12.5, dz = portrait ? HW + 30 : touch ? HW + 5 : HW + 8;
        pos.set(cx * 0.96, h, dz + look.z * 0.55);
      }
    }
    // ارتجاج
    if (this.world.shake > 0) {
      this.world.shake = Math.max(0, this.world.shake - dt * 1.5);
      const s = this.world.shake * 0.35;
      pos.x += (Math.random() - 0.5) * s; pos.y += (Math.random() - 0.5) * s;
    }
    const k = 1 - Math.exp(-dt * smooth);
    if (this.camSnap) { this.camPos.copy(pos); this.camLook.copy(look); this.camSnap = false; }
    this.camPos.lerp(pos, k);
    this.camLook.lerp(look, Math.min(1, k * 1.4));
    cam.position.copy(this.camPos);
    cam.lookAt(this.camLook);
    const fov = this.replay || ph === PHASE.GOAL ? 50 : portrait ? 70 : this.camMode === 'behind' ? 60 : 44;
    if (Math.abs(cam.fov - fov) > 0.1) { cam.fov += (fov - cam.fov) * Math.min(1, dt * 3); cam.updateProjectionMatrix(); }
  }

  cycleCamera() {
    const modes = ['broadcast', 'behind', 'top'];
    this.camMode = modes[(modes.indexOf(this.camMode) + 1) % modes.length];
    this.app.settings.camera = this.camMode;
    this.app.saveSettings();
    this.toast(`الكاميرا: ${{ broadcast: 'بث تلفزيوني', behind: 'خلف اللاعب', top: 'من الأعلى' }[this.camMode]}`);
  }

  // ---------- الأحداث ----------
  handleEvent(e, replay) {
    const P = this.players3d;
    const name = (id) => (this.roster[id] ? this.roster[id].name : '');
    const st = this.state || this.latest;
    const pos = (id) => (st && st.p[id] ? { x: st.p[id][0], y: st.p[id][1] } : { x: 0, y: 0 });
    switch (e.e) {
      case 'kick': {
        const throwIn = st && st.spk === 'throw' && st.sp === e.p;
        P[e.p] && P[e.p].triggerKick(!!e.h, throwIn);
        audio.kick(e.pw, !!e.h);
        if (e.k !== 'pass' && e.pw > 0.45) audio.whoosh(e.pw);
        if (!replay && e.p === this.you) this.vibrate(e.k === 'shot' ? 25 : 12);
        if (!replay && e.s) { audio.cheer(0.5 + e.pw * 0.4); if (Math.random() < 0.55) audio.say(pick(['تسديدة قوية!', 'يسدد نحو المرمى!', 'محاولة خطيرة!', `${name(e.p).replace(/[^\p{L} ]/gu, '')} يسدد!`])); }
        const p = pos(e.p);
        this.fx.kickDust(p.x, p.y, e.pw, !!e.h);
        if (!replay && e.s) { this.excite = Math.max(this.excite, 0.75); this.world.excite = Math.max(this.world.excite, 0.5); }
        if (!replay && e.fx === 1) audio.say(pick(SAY.fire), true);
        break;
      }
      case 'touch': case 'control': case 'steal': audio.touch(); break;
      case 'block': audio.kick(0.3); break;
      case 'bounce': audio.bounce(e.v); break;
      case 'board': audio.board(e.v); if (e.v > 14) this.world.shake = Math.max(this.world.shake || 0, 0.15); break;
      case 'post':
        audio.post();
        if (!replay) { this.banner('القائم!', '', 'warn'); audio.say(pick(SAY.post), true); this.excite = 0.9; }
        break;
      case 'net': {
        this.world.netHit(e.x, e.y, e.z, e.s);
        audio.net();
        break;
      }
      case 'goal': {
        this.world.netHit(Math.sign(st ? st.b[0] : 1) * HL, st ? st.b[1] : 0, 1, 20);
        if (replay) break;
        this.goalT = e.t;
        this.goalInfo = e;
        const team = TEAMS[e.team];
        const scorer = e.p >= 0 ? name(e.p) : '';
        const sub = e.own ? `هدف عكسي — ${scorer}` : `${scorer}${e.a >= 0 ? ` • صناعة ${name(e.a)}` : ''}${e.d > 20 ? ` • من ${Math.round(e.d)} م!` : ''}`;
        this.banner('هـــدف!', sub, 'goal', team.color);
        audio.roar(6, 1);
        audio.goalParty();
        this.vibrate([80, 60, 160]);
        setTimeout(() => audio.chant(0), 2600);
        audio.say(`${pick(SAY.goal)} ${e.own ? '' : scorer.replace(/[^\p{L} ]/gu, '')}`, true);
        this.fx.confetti(e.team, [team.color, team.color2, '#ffffff', '#ffd23f']);
        this.fx.fireworks([team.color, team.color2, '#ffd23f', '#ffffff'], 7);
        this.world.goalCelebration(e.team);
        this.world.shake = 0.5;
        this.flashScreen(team.color);
        const mm = this.minuteOf(e.t);
        this.addFeed(`⚽ ${scorer}${e.own ? ' (عكسي)' : ''} ${mm}'`, team.color);
        this.goalTimes.push({ team: e.team, text: `${scorer} ${mm}'` });
        this.renderScorers();
        this.camSnap = true;
        const isMine = this.you >= 0 && this.roster[this.you] && this.roster[this.you].team === e.team;
        $('emotebar').classList.toggle('hidden', !(this.you >= 0));
        if (isMine && e.p === this.you) this.toast('اختر احتفالك: 1-5 🎉');
        break;
      }
      case 'save':
        if (!replay) {
          this.banner(e.k === 'burn' ? 'صدّ الكرة النارية!' : 'تصدٍّ رائع!', name(e.p), 'save');
          audio.ooh();
          setTimeout(() => audio.applause(2.5, 1), 700);
          audio.say(pick(SAY.save), true);
          this.addFeed(`🧤 ${name(e.p)}`, '#ffffff');
        }
        break;
      case 'nearmiss':
        if (!replay) { audio.ooh(); this.banner('قريبة!', '', 'warn'); if (Math.random() < 0.5) audio.say(pick(SAY.miss)); }
        break;
      case 'out':
        if (!replay) {
          audio.whistle('short');
          const t = { throw: 'رمية تماس', corner: 'ركنية', goalkick: 'ضربة مرمى' }[e.k];
          this.banner(t, TEAMS[e.team].name, 'small', TEAMS[e.team].color);
        }
        break;
      case 'whistle':
        if (replay) break;
        if (e.k === 'start') {
          audio.whistle('short');
          audio.applause(2, 0.8);
          this.banner('انطلق!', '', 'small');
          if (!this.saidStart) { this.saidStart = true; audio.say(pick(SAY.start)); setTimeout(() => audio.chant(1), 3000); }
        } else if (e.k === 'end') audio.whistle('end');
        break;
      case 'golden':
        if (!replay) { this.banner('الهدف الذهبي!', 'التعادل — أول هدف يحسم المباراة', 'goal', '#ffd23f'); audio.say(SAY.golden[0], true); audio.horn(); }
        break;
      case 'ability': {
        const p = { x: e.x, y: e.y };
        const players = st ? st.p.map((q) => ({ x: q[0], y: q[1] })) : null;
        this.fx.ability(e.k, p.x, p.y, e, players);
        audio.ability(e.k);
        if (!replay) {
          const c = charOf(this.roster[e.p].char);
          if (e.p === this.you) { this.pulseAbility(); this.addFeed(`${c.icon} ${c.ability.name}`, TEAMS[this.roster[e.p].team].color); this.vibrate(30); }
        }
        break;
      }
      case 'tackle': if (e.ok) { audio.ability('tackle'); if (!replay && e.p === this.you) { audio.applause(1.5, 0.6); this.vibrate(20); if (Math.random() < 0.6) audio.say(pick(['افتكاك نظيف!', 'استرجع الكرة!', 'دفاع رائع!'])); } } break;
      case 'slide': audio.ability('slide'); break;
      case 'foul': audio.ability('tackle'); if (!replay) { audio.ooh(); if (e.v === this.you) this.vibrate(70); } break;
      case 'wallhit': this.fx.wallHit(e.x, e.y); audio.ability('wallhit'); break;
      case 'dive': audio.ability('slide'); break;
      case 'emote': break;
      case 'end':
        if (!replay) {
          const w = e.w;
          this.banner('نهاية المباراة', w === -1 ? 'تعادل' : `فوز ${TEAMS[w].name}`, 'goal', w >= 0 ? TEAMS[w].color : '#ffffff');
          audio.roar(5, 0.8);
          if (w >= 0) { this.fx.fireworks([TEAMS[w].color, TEAMS[w].color2, '#ffd23f'], 10); this.world.goalCelebration(w); setTimeout(() => audio.chant(3), 1500); }
          audio.say(w === -1 ? 'انتهت المباراة بالتعادل' : `انتهت المباراة! فوز ${TEAMS[w].name}`, true);
        }
        break;
    }
  }

  onPhase(ph, prev, state) {
    if (ph === PHASE.KICKOFF) { $('emotebar').classList.add('hidden'); this.camSnap = prev === PHASE.REPLAY; this.goalInfo = null; }
    if (ph === PHASE.REPLAY) { $('emotebar').classList.add('hidden'); }
    if (ph === PHASE.END) $('emotebar').classList.toggle('hidden', !(this.you >= 0));
  }

  minuteOf(t) {
    const elapsed = this.opts.duration - (this.latest ? this.latest.tl : 0);
    return Math.max(1, Math.ceil((elapsed / this.opts.duration) * 90));
  }

  // ---------- الواجهة ----------
  setupHUD() {
    $('hud').classList.remove('hidden');
    $('sb-t0').textContent = TEAMS[0].name;
    $('sb-t1').textContent = TEAMS[1].name;
    $('sb-c0').style.background = TEAMS[0].color;
    $('sb-c1').style.background = TEAMS[1].color;
    $('feed').innerHTML = '';
    $('scorers0').innerHTML = ''; $('scorers1').innerHTML = '';
    $('replay').classList.add('hidden');
    $('emotebar').classList.add('hidden');
    $('emotebar').innerHTML = EMOTES.map((e) => `<button data-emote="${e.id}" title="${e.name}"><b>${e.id}</b>${e.icon}</button>`).join('');
    $('emotebar').onclick = (ev) => { const b = ev.target.closest('[data-emote]'); if (b) this.emote(+b.dataset.emote); };
    this.mm = $('minimap').getContext('2d');
    this.hudCache = {};
    this.hudReady = true;
    const hint = document.querySelector('#touch .t-hint');
    if (hint) { hint.classList.remove('gone'); setTimeout(() => hint.classList.add('gone'), 8000); }
    this.touchCtx = null;
    this.updateCard(true);
  }

  updateCard() {
    const you = this.you;
    const card = $('pcard');
    if (you < 0 || !this.roster[you]) { card.classList.add('hidden'); return; }
    card.classList.remove('hidden');
    const c = charOf(this.roster[you].char);
    $('pc-icon').textContent = c.icon;
    $('pc-name').textContent = `${c.name} — ${c.title}`;
    $('pc-ab-name').textContent = c.ability.name;
    $('pc-ab-icon').textContent = c.icon;
    const tb = document.querySelector('#touch [data-btn="ABILITY"]');
    if (tb) tb.textContent = c.icon;
  }

  pulseAbility() {
    const el = $('pc-ab');
    el.classList.remove('pulse'); void el.offsetWidth; el.classList.add('pulse');
  }

  fmtTime(tl) {
    const t = Math.max(0, Math.ceil(tl));
    return `${Math.floor(t / 60)}:${String(t % 60).padStart(2, '0')}`;
  }

  updateHUD(state, dt) {
    const c = this.hudCache;
    const set = (id, v) => { if (c[id] !== v) { c[id] = v; $(id).textContent = v; } };
    set('sb-s0', state.sc[0]);
    set('sb-s1', state.sc[1]);
    set('sb-time', state.gd ? 'ذهبي' : this.fmtTime(state.tl));
    $('sb-time').classList.toggle('golden', !!state.gd);
    const you = this.you;
    if (you >= 0 && state.p[you]) {
      const q = this.latest.p[you];
      const stam = Math.round(q[7] * 100);
      if (c.stam !== stam) { c.stam = stam; $('pc-stam').style.width = stam + '%'; $('pc-stam').classList.toggle('low', stam < 25); }
      const cd = q[8];
      const cdk = Math.round(cd * 100);
      if (c.cd !== cdk) {
        c.cd = cdk;
        $('pc-ab').style.setProperty('--cd', `${cdk}%`);
        $('pc-ab').classList.toggle('ready', cdk === 0);
        const tb = document.querySelector('#touch [data-btn="ABILITY"]');
        if (tb) { tb.style.setProperty('--cd', `${cdk}%`); tb.classList.toggle('ready', cdk === 0); }
      }
      // شريط القوة
      const hold = q[10] || 0;
      const pw = $('power');
      if (hold > 0 && !this.replay) {
        const v = toV(this.pred ? this.pred.x : state.p[you][0], this.pred ? this.pred.y : state.p[you][1], 2.3).project(this.camera);
        pw.style.left = ((v.x + 1) / 2) * this.app.width + 'px';
        pw.style.top = ((1 - v.y) / 2) * this.app.height + 'px';
        pw.classList.remove('hidden');
        $('power-fill').style.width = Math.min(100, hold * 100) + '%';
        $('power-fill').classList.toggle('max', hold >= 0.99);
      } else pw.classList.add('hidden');
    }
    // العد التنازلي
    const cdEl = $('countdown');
    if (state.ph === PHASE.KICKOFF) {
      const n = Math.max(1, Math.ceil(2 - state.pt));
      set('countdown', String(n));
      cdEl.classList.remove('hidden');
    } else cdEl.classList.add('hidden');
    // الخريطة المصغرة
    this.drawMinimap(state);
    // اللافتة
    if (this.bannerT > 0) {
      this.bannerT -= dt;
      if (this.bannerT <= 0) $('banner').classList.remove('show');
    }
  }

  drawMinimap(state) {
    const g = this.mm;
    const cw = g.canvas.width, ch = g.canvas.height;
    const sx = cw / (L + 4), sy = ch / (W + 4);
    const X = (x) => (x + L / 2 + 2) * sx, Y = (y) => (W / 2 + 2 - y) * sy;
    g.clearRect(0, 0, cw, ch);
    g.fillStyle = 'rgba(20,70,30,0.75)';
    g.fillRect(0, 0, cw, ch);
    g.strokeStyle = 'rgba(255,255,255,0.6)'; g.lineWidth = 1;
    g.strokeRect(X(-HL), Y(HW), L * sx, W * sy);
    g.beginPath(); g.moveTo(X(0), Y(HW)); g.lineTo(X(0), Y(-HW)); g.stroke();
    g.beginPath(); g.arc(X(0), Y(0), 5 * sx, 0, PI * 2); g.stroke();
    g.fillStyle = '#fff';
    g.fillRect(X(-HL) - 2, Y(GW / 2), 2, GW * sy); g.fillRect(X(HL), Y(GW / 2), 2, GW * sy);
    state.p.forEach((q, i) => {
      const r = this.roster[i];
      if (!r) return;
      g.fillStyle = TEAMS[r.team].color;
      g.beginPath(); g.arc(X(q[0]), Y(q[1]), i === this.you ? 4.5 : 3.2, 0, PI * 2); g.fill();
      if (i === this.you) { g.strokeStyle = '#fff27a'; g.lineWidth = 2; g.stroke(); }
      else if (r.human) { g.strokeStyle = '#fff'; g.lineWidth = 1; g.stroke(); }
    });
    g.fillStyle = '#ffffff';
    g.beginPath(); g.arc(X(state.b[0]), Y(state.b[1]), 2.5, 0, PI * 2); g.fill();
  }

  banner(text, sub = '', kind = 'small', color = '') {
    const el = $('banner');
    $('banner-text').textContent = text;
    $('banner-sub').textContent = sub;
    el.className = 'banner ' + kind;
    el.style.setProperty('--bc', color || '#ffffff');
    void el.offsetWidth;
    el.classList.add('show');
    this.bannerT = kind === 'goal' ? 3.6 : 1.7;
  }

  addFeed(text, color) {
    const el = document.createElement('div');
    el.className = 'feed-item';
    el.style.borderColor = color;
    el.textContent = text;
    const f = $('feed');
    f.prepend(el);
    while (f.children.length > 5) f.lastChild.remove();
    setTimeout(() => el.classList.add('fade'), 5000);
    setTimeout(() => el.remove(), 6000);
  }

  renderScorers() {
    for (const t of [0, 1]) $(`scorers${t}`).innerHTML = this.goalTimes.filter((g) => g.team === t).map((g) => `<div>⚽ ${g.text}</div>`).join('');
  }

  flashScreen(color) {
    const f = $('flash');
    f.style.background = color;
    f.classList.remove('go'); void f.offsetWidth; f.classList.add('go');
  }

  toast(t) { this.app.toast(t); }

  vibrate(ms) { if (navigator.vibrate) try { navigator.vibrate(ms); } catch { /* ignore */ } }

  emote(n) {
    this.transport.sendEmote(n);
  }

  onResize() {
    this.fx.setScale(this.app.height);
  }

  destroy() {
    audio.drums = false;
    for (const p of this.players3d) p.dispose();
    this.world.dispose();
    $('hud').classList.add('hidden');
    $('replay').classList.add('hidden');
    $('power').classList.add('hidden');
    this.hudReady = false;
  }
}
