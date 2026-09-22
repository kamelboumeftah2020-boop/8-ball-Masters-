// محاكاة المباراة: فيزياء الكرة، حركة اللاعبين، القواعد، الذكاء الاصطناعي، القدرات الخاصة
// تعمل على الخادم (أونلاين) وفي المتصفح (ضد البوتات) بنفس الكود
import { FIELD, BALL_R, PLAYER_R, GRAVITY, TEAM_SIZE, BTN, PHASE, STATE, DIFFICULTY } from './constants.js';
import { charOf, CHARACTERS } from './characters.js';
import { stadiumOf } from './stadiums.js';

const { L, W, GW, GH, GD, BOX_R, CIRCLE_R, POST_R } = FIELD;
const HL = L / 2, HW = W / 2;
const PI = Math.PI, TAU = PI * 2;

const clamp = (v, a, b) => (v < a ? a : v > b ? b : v);
const hyp = Math.hypot;
const r2 = (v) => Math.round(v * 100) / 100;
const r1 = (v) => Math.round(v * 10) / 10;
export function angDiff(a, b) {
  let d = (b - a) % TAU;
  if (d > PI) d -= TAU;
  if (d < -PI) d += TAU;
  return d;
}

function mulberry32(seed) {
  let a = seed >>> 0;
  return function () {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export const FX_CODE = { fire: 1, curve: 2 };
export const BUFF = { FIRE: 1, DASH: 2, CURVE: 4, MAGNET: 8, SLOW: 16 };

// تكامل حركة اللاعب (يُستخدم أيضاً للتنبؤ في المتصفح)
export function integrateMovement(p, mx, my, sprint, dt, mul = 1) {
  const len = hyp(mx, my);
  if (len > 1) { mx /= len; my /= len; }
  const spd = hyp(p.vx, p.vy);
  const maxSp = 5.7 * mul * (sprint ? 1.42 : 1);
  const dvx = mx * maxSp - p.vx, dvy = my * maxSp - p.vy;
  // زخم واقعي: الانعطاف الحاد أثناء الجري السريع أصعب
  let accel = sprint ? 13 : 16;
  if (spd > 3 && len > 0.1) {
    const dot = (p.vx * mx + p.vy * my) / (spd * Math.min(1, len));
    if (dot < 0.3) accel *= 0.55 + 0.25 * (dot + 1);
  }
  if (len < 0.1) accel = 19; // التوقف
  const dvl = hyp(dvx, dvy);
  const maxStep = accel * dt;
  let k = Math.min(1, 9 * dt);
  let sx = dvx * k, sy = dvy * k;
  const sl = hyp(sx, sy);
  if (sl > maxStep) { sx *= maxStep / sl; sy *= maxStep / sl; }
  if (dvl < 0.02) { sx = dvx; sy = dvy; }
  p.vx += sx; p.vy += sy;
  p.x += p.vx * dt; p.y += p.vy * dt;
}

export class Game {
  constructor(opts = {}) {
    this.stadium = stadiumOf(opts.stadium);
    this.duration = opts.duration || 180;
    this.diffKey = DIFFICULTY[opts.difficulty] ? opts.difficulty : 'normal';
    this.diff = DIFFICULTY[this.diffKey];
    this.rand = mulberry32(opts.seed || (Date.now() & 0xffffffff));
    this.time = 0;
    this.timeLeft = this.duration;
    this.golden = false;
    this.score = [0, 0];
    this.events = [];
    this.zones = [];
    this.players = [];
    this.lastPass = null;
    this.lastShot = null;
    this.goalInfo = null;
    this.sp = null;
    this.pendingSP = null;
    this.skipVotes = new Set();
    this.over = false;
    const roster = opts.roster || [[], []];
    const usedChars = new Set();
    for (let team = 0; team < 2; team++) {
      for (let slot = 0; slot < TEAM_SIZE; slot++) {
        const r = (roster[team] && roster[team][slot]) || null;
        let char = r && r.char;
        if (!char) {
          const pool = CHARACTERS.filter((c) => !usedChars.has(c.id + team));
          char = (pool.length ? pool : CHARACTERS)[Math.floor(this.rand() * (pool.length || CHARACTERS.length))].id;
          if (slot === 0 && this.rand() < 0.6) char = 'guardian';
        }
        usedChars.add(char + team);
        this.players.push(this.makePlayer(team, slot, r, char));
      }
    }
    this.ball = { x: 0, y: 0, z: BALL_R, vx: 0, vy: 0, vz: 0, spin: 0, owner: -1, gk: false, fx: null, fxT: 0, lastTouch: -1, lastTeam: -1, inNet: false, prevX: 0, prevY: 0, prevZ: BALL_R };
    this.resetKickoff(this.rand() < 0.5 ? 0 : 1);
  }

  makePlayer(team, slot, r, char) {
    const c = charOf(char);
    return {
      id: team * TEAM_SIZE + slot, team, slot,
      name: (r && r.name) || this.botName(c, team, slot),
      char: c.id, c, human: !!(r && r.human), cid: (r && r.cid) || null,
      x: 0, y: 0, vx: 0, vy: 0, face: 0,
      state: STATE.NORMAL, stateT: 0, emote: 0,
      stamina: 1, cd: 3 + slot, sprinting: false,
      buff: { fire: 0, dash: 0, curve: 0, magnet: 0, slow: 0 },
      input: { mx: 0, my: 0, b: 0 }, prevB: 0,
      hold: { shoot: 0, lob: 0 }, kickBuf: null,
      pickupCD: 0, tackleCD: 0, touchT: 0, heldT: 0, kickAnim: 0,
      slideDir: 0, slideHit: false,
      ai: { t: 0, tx: 0, ty: 0, sprint: false, mode: '' },
      st: { g: 0, a: 0, sh: 0, sv: 0, tk: 0, ps: 0 },
    };
  }

  botName(c, team, slot) {
    return `${c.name} 🤖`;
  }

  attackDir(team) { return team === 0 ? 1 : -1; }
  isGK(p) { return p.slot === 0; }
  inOwnBox(p, margin = 0) {
    const gx = -this.attackDir(p.team) * HL;
    return hyp(p.x - gx, p.y) < BOX_R + margin && Math.abs(p.x) <= HL + GD;
  }
  ev(o) { o.t = r2(this.time); this.events.push(o); }
  drainEvents() { const e = this.events; this.events = []; return e; }

  // ---------- إدارة اللاعبين البشر ----------
  setHuman(pid, info) {
    const p = this.players[pid];
    if (!p) return;
    p.human = true; p.cid = info.cid; p.name = info.name || p.name;
    if (info.char) { p.char = charOf(info.char).id; p.c = charOf(info.char); }
    p.input = { mx: 0, my: 0, b: 0 }; p.prevB = 0;
  }
  setBot(pid) {
    const p = this.players[pid];
    if (!p) return;
    p.human = false; p.cid = null; p.name = this.botName(p.c, p.team, p.slot);
    p.input = { mx: 0, my: 0, b: 0 };
  }
  setInput(pid, inp) {
    const p = this.players[pid];
    if (!p || !p.human) return;
    let mx = +inp.mx || 0, my = +inp.my || 0;
    const l = hyp(mx, my);
    if (l > 1) { mx /= l; my /= l; }
    p.input.mx = mx; p.input.my = my; p.input.b = (inp.b | 0) & 127;
    if (p.input.b & BTN.SKIP && this.phase === PHASE.REPLAY) this.skipVotes.add(pid);
  }
  requestEmote(pid, n) {
    const p = this.players[pid];
    if (!p) return;
    n = clamp(n | 0, 1, 5);
    p.emoteReq = n;
    if (this.phase === PHASE.GOAL || this.phase === PHASE.END || this.phase === PHASE.REPLAY) {
      if (p.state === STATE.SAD) return;
      p.state = STATE.CELEBRATE; p.emote = n; p.stateT = 4;
      this.ev({ e: 'emote', p: pid, n });
    }
  }

  roster() {
    return this.players.map((p) => ({ id: p.id, team: p.team, slot: p.slot, name: p.name, char: p.char, human: p.human, cid: p.cid }));
  }

  // ---------- المراحل ----------
  kickoffPos(p, kickTeam) {
    const d = this.attackDir(p.team);
    const kicking = p.team === kickTeam;
    const table = kicking
      ? [[-HL + 1.3, 0], [-14, -6.5], [-14, 6.5], [-0.45, 0.1], [-2.5, 8]]
      : [[-HL + 1.3, 0], [-14, -6.5], [-14, 6.5], [-CIRCLE_R - 1, -3.5], [-CIRCLE_R - 1.5, 5]];
    const [xr, y] = table[p.slot];
    return { x: xr * d, y: y * d };
  }

  resetKickoff(team) {
    this.kickoffTeam = team;
    this.phase = PHASE.KICKOFF;
    this.phaseT = 0;
    this.zones = [];
    this.sp = null; this.pendingSP = null;
    this.goalInfo = null;
    this.skipVotes.clear();
    const b = this.ball;
    Object.assign(b, { x: 0, y: 0, z: BALL_R, vx: 0, vy: 0, vz: 0, spin: 0, owner: -1, gk: false, fx: null, fxT: 0, lastTouch: -1, lastTeam: -1, inNet: false });
    for (const p of this.players) {
      const pos = this.kickoffPos(p, team);
      p.x = pos.x; p.y = pos.y; p.vx = p.vy = 0;
      p.face = p.team === 0 ? 0 : PI;
      p.state = STATE.NORMAL; p.stateT = 0; p.emote = 0;
      p.buff.fire = p.buff.dash = p.buff.curve = p.buff.magnet = p.buff.slow = 0;
      p.hold.shoot = p.hold.lob = 0; p.kickBuf = null; p.heldT = 0; p.pickupCD = 0;
    }
  }

  // ---------- الخطوة الرئيسية ----------
  step(dt) {
    if (this.over && this.phase === PHASE.END) { this.phaseT += dt; this.time += dt; this.stepCelebration(dt); return; }
    this.time += dt;
    this.phaseT += dt;
    switch (this.phase) {
      case PHASE.KICKOFF:
        if (this.phaseT >= 2.0) { this.phase = PHASE.PLAY; this.phaseT = 0; this.ev({ e: 'whistle', k: 'start' }); }
        break;
      case PHASE.PLAY:
      case PHASE.SETPIECE:
        this.timeLeft -= dt;
        if (this.timeLeft <= 0) {
          this.timeLeft = 0;
          if (!this.golden) {
            if (this.score[0] === this.score[1]) { this.golden = true; this.ev({ e: 'golden' }); }
            else return this.endMatch();
          }
        }
        break;
      case PHASE.OUT:
        if (this.phaseT >= 1.1) this.setupSetPiece();
        break;
      case PHASE.GOAL:
        if (this.phaseT >= 4.8) { this.phase = PHASE.REPLAY; this.phaseT = 0; this.skipVotes.clear(); this.ev({ e: 'replay', gt: this.goalInfo ? this.goalInfo.t : this.time }); }
        break;
      case PHASE.REPLAY: {
        const humans = this.players.filter((p) => p.human);
        const allSkip = humans.length > 0 && humans.every((p) => this.skipVotes.has(p.id));
        if (this.phaseT >= 6.2 || (allSkip && this.phaseT > 0.6)) {
          if (this.golden) return this.endMatch();
          this.resetKickoff(this.goalInfo ? 1 - this.goalInfo.team : 0);
        }
        return; // تجميد المحاكاة أثناء الإعادة
      }
      case PHASE.END:
        this.stepCelebration(dt);
        return;
    }
    if (this.phase === PHASE.GOAL) this.stepCelebration(dt);
    else this.updatePlayers(dt);
    this.updateZones(dt);
    this.updateBall(dt);
  }

  endMatch() {
    this.phase = PHASE.END; this.phaseT = 0; this.over = true;
    const w = this.score[0] > this.score[1] ? 0 : this.score[1] > this.score[0] ? 1 : -1;
    this.ev({ e: 'whistle', k: 'end' });
    this.ev({ e: 'end', w, sc: this.score.slice() });
    for (const p of this.players) {
      p.vx *= 0.3; p.vy *= 0.3;
      if (w === -1) { p.state = STATE.NORMAL; }
      else if (p.team === w) { p.state = STATE.CELEBRATE; p.emote = 1 + Math.floor(this.rand() * 5); p.stateT = 99; }
      else { p.state = STATE.SAD; p.stateT = 99; }
    }
    if (this.ball.owner >= 0) { this.ball.owner = -1; this.ball.gk = false; }
  }

  // ---------- اللاعبون ----------
  updatePlayers(dt) {
    const frozen = this.phase === PHASE.KICKOFF;
    for (const p of this.players) {
      const inp = p.human ? p.input : this.aiInput(p, dt);
      this.controlPlayer(p, inp, dt, frozen);
    }
    this.collidePlayers();
  }

  speedMul(p) {
    let m = p.c.stats.speed;
    if (p.buff.dash > 0) m *= 1.5;
    if (p.buff.slow > 0) m *= 0.48;
    if (this.ball.owner === p.id && !this.ball.gk) m *= 0.93;
    if (p.hold.shoot > 0 || p.hold.lob > 0) m *= 0.8;
    if (!p.human) m *= this.diff.speed;
    return m;
  }

  controlPlayer(p, inp, dt, frozen) {
    p.cd = Math.max(0, p.cd - dt);
    for (const k in p.buff) if (p.buff[k] > 0) p.buff[k] = Math.max(0, p.buff[k] - dt);
    p.pickupCD -= dt; p.tackleCD -= dt; p.touchT -= dt; p.kickAnim = Math.max(0, p.kickAnim - dt);
    if (p.kickBuf) { p.kickBuf.t -= dt; if (p.kickBuf.t <= 0) p.kickBuf = null; }
    const b = inp.b | 0;
    const pressed = b & ~p.prevB, released = p.prevB & ~b;
    p.prevB = b;
    const isTaker = this.phase === PHASE.SETPIECE && this.sp && this.sp.taker === p.id;

    if (p.state !== STATE.NORMAL) {
      p.stateT -= dt;
      let damp = 2.5;
      if (p.state === STATE.SLIDE) damp = 2.2;
      else if (p.state === STATE.DOWN || p.state === STATE.STUMBLE) damp = 6;
      else if (p.state === STATE.DIVE) damp = 1.6;
      const f = Math.max(0, 1 - damp * dt);
      p.vx *= f; p.vy *= f;
      p.x += p.vx * dt; p.y += p.vy * dt;
      if (p.stateT <= 0) {
        if (p.state === STATE.SLIDE) { p.state = STATE.STUMBLE; p.stateT = 0.35; }
        else if (p.state === STATE.DIVE) { p.state = STATE.STUMBLE; p.stateT = 0.45; }
        else { p.state = STATE.NORMAL; }
      }
      p.hold.shoot = p.hold.lob = 0;
      this.clampPlayer(p);
      return;
    }

    let mx = inp.mx || 0, my = inp.my || 0;
    const mlen = hyp(mx, my);
    let sprint = !!(b & BTN.SPRINT) && mlen > 0.2;
    if (p.buff.dash > 0) { sprint = mlen > 0.2; p.stamina = Math.min(1, p.stamina + dt * 0.2); }
    else if (sprint) {
      if (p.stamina > 0.02) p.stamina = Math.max(0, p.stamina - dt * 0.15 / p.c.stats.stamina);
      else sprint = false;
    } else p.stamina = Math.min(1, p.stamina + dt * (mlen > 0.2 ? 0.07 : 0.12));
    p.sprinting = sprint;

    if (frozen || isTaker) {
      p.vx = p.vy = 0;
      if (mlen > 0.2) p.face += angDiff(p.face, Math.atan2(my, mx)) * Math.min(1, 10 * dt);
    } else {
      integrateMovement(p, mx, my, sprint, dt, this.speedMul(p));
      // الاتجاه
      const spd = hyp(p.vx, p.vy);
      let want = null;
      if ((p.hold.shoot > 0 || p.hold.lob > 0 || this.ball.owner === p.id) && mlen > 0.2) want = Math.atan2(my, mx);
      else if (spd > 0.6) want = Math.atan2(p.vy, p.vx);
      else if (mlen > 0.2) want = Math.atan2(my, mx);
      if (want !== null) {
        const rate = this.ball.owner === p.id ? 9 : 12;
        const d = angDiff(p.face, want);
        p.face += clamp(d, -rate * dt, rate * dt);
      }
    }
    this.clampPlayer(p);
    if (frozen) return;

    const canAct = this.phase === PHASE.PLAY || isTaker;
    if (!canAct) { p.hold.shoot = p.hold.lob = 0; return; }

    // التسديد (اضغط مطولاً للقوة)
    if (b & BTN.SHOOT) p.hold.shoot = Math.min(1.2, p.hold.shoot + dt);
    if (released & BTN.SHOOT) {
      const pw = clamp(p.hold.shoot / 0.9, 0.2, 1);
      p.hold.shoot = 0;
      this.tryKick(p, isTaker && this.sp.kind === 'throw' ? 'lob' : 'shot', pw, { dir: this.inputDir(p, inp) });
    }
    if (b & BTN.LOB) p.hold.lob = Math.min(1.2, p.hold.lob + dt);
    if (released & BTN.LOB) {
      const pw = clamp(p.hold.lob / 0.9, 0.2, 1);
      p.hold.lob = 0;
      this.tryKick(p, 'lob', pw, { dir: this.inputDir(p, inp) });
    }
    if (pressed & BTN.PASS) this.tryKick(p, 'pass', 0.6, { dir: this.inputDir(p, inp) });
    if (isTaker) {
      // تنفيذ تلقائي إذا تأخر اللاعب
      if (this.phaseT > (p.human ? 7 : 1.3)) this.aiSetPiece(p);
      return;
    }
    if (pressed & BTN.TACKLE) this.tackle(p, inp);
    if (pressed & BTN.ABILITY) this.useAbility(p, inp);
  }

  inputDir(p, inp) {
    const l = hyp(inp.mx || 0, inp.my || 0);
    if (l > 0.2) return { x: inp.mx / l, y: inp.my / l };
    return { x: Math.cos(p.face), y: Math.sin(p.face) };
  }

  clampPlayer(p) {
    p.x = clamp(p.x, -HL - 3, HL + 3);
    p.y = clamp(p.y, -HW - 3, HW + 3);
    // لا يدخل اللاعب عمق الشباك كثيراً
    if (Math.abs(p.x) > HL && Math.abs(p.y) < GW / 2 + PLAYER_R) {
      const lim = HL + GD - PLAYER_R;
      if (Math.abs(p.x) > lim) p.x = Math.sign(p.x) * lim;
    }
  }

  collidePlayers() {
    const ps = this.players;
    for (let i = 0; i < ps.length; i++) {
      const a = ps[i];
      for (let j = i + 1; j < ps.length; j++) {
        const c = ps[j];
        const dx = c.x - a.x, dy = c.y - a.y;
        const ra = PLAYER_R * a.c.look.build, rc = PLAYER_R * c.c.look.build;
        const min = ra + rc;
        const d2 = dx * dx + dy * dy;
        if (d2 >= min * min || d2 < 1e-6) continue;
        const d = Math.sqrt(d2);
        const nx = dx / d, ny = dy / d;
        const over = (min - d);
        const wa = c.c.look.build / (a.c.look.build + c.c.look.build);
        a.x -= nx * over * wa; a.y -= ny * over * wa;
        c.x += nx * over * (1 - wa); c.y += ny * over * (1 - wa);
        const rv = (c.vx - a.vx) * nx + (c.vy - a.vy) * ny;
        if (rv < 0) {
          const imp = rv * 0.6;
          a.vx += nx * imp * wa; a.vy += ny * imp * wa;
          c.vx -= nx * imp * (1 - wa); c.vy -= ny * imp * (1 - wa);
        }
        // الانزلاق يطيح بالخصم
        for (const [s, t] of [[a, c], [c, a]]) {
          if (s.state === STATE.SLIDE && t.team !== s.team && t.state === STATE.NORMAL && hyp(s.vx, s.vy) > 3) {
            t.state = STATE.DOWN; t.stateT = s.slideHit ? 0.7 : 1.1;
            t.vx += s.vx * 0.4; t.vy += s.vy * 0.4;
            if (this.ball.owner === t.id && !this.ball.gk) this.looseBall(0.5);
            this.ev({ e: 'foul', p: s.id, v: t.id });
          }
        }
      }
    }
  }

  looseBall(speed = 2) {
    const b = this.ball;
    const p = this.players[b.owner];
    b.owner = -1; b.gk = false;
    if (p) {
      p.pickupCD = 0.5;
      const a = this.rand() * TAU;
      b.vx = p.vx * 0.5 + Math.cos(a) * speed; b.vy = p.vy * 0.5 + Math.sin(a) * speed; b.vz = 1.5;
    }
  }

  // ---------- القطع والانزلاق والارتماء ----------
  tackle(p, inp) {
    if (p.tackleCD > 0) return;
    const b = this.ball;
    const owner = b.owner >= 0 ? this.players[b.owner] : null;
    const db = hyp(b.x - p.x, b.y - p.y);
    // ارتماء الحارس
    if (this.isGK(p) && this.inOwnBox(p, 1)) {
      let dir = this.inputDir(p, inp);
      if (hyp(inp.mx || 0, inp.my || 0) < 0.2) { const d = db || 1; dir = { x: (b.x - p.x) / d, y: (b.y - p.y) / d }; }
      const sp = 6.2 * (p.c.stats.keeper ** 0.5);
      p.vx = dir.x * sp; p.vy = dir.y * sp;
      p.face = Math.atan2(dir.y, dir.x);
      p.state = STATE.DIVE; p.stateT = 0.7; p.tackleCD = 1.2;
      this.ev({ e: 'dive', p: p.id });
      return;
    }
    if (owner && owner.team !== p.team && !b.gk && db < 1.7) {
      // قطع واقف
      const behind = Math.cos(angDiff(owner.face, Math.atan2(p.y - owner.y, p.x - owner.x))) < -0.3;
      let chance = 0.55 + 0.4 * (p.c.stats.tackle - owner.c.stats.dribble) - (behind ? 0.25 : 0);
      if (!owner.human) chance += 0.08;
      p.tackleCD = 0.8;
      if (this.rand() < chance) {
        b.owner = p.id; owner.pickupCD = 0.6; p.pickupCD = 0;
        b.lastTouch = p.id; b.lastTeam = p.team;
        p.st.tk++;
        this.ev({ e: 'tackle', p: p.id, v: owner.id, ok: 1 });
      } else {
        p.state = STATE.STUMBLE; p.stateT = 0.4;
        this.ev({ e: 'tackle', p: p.id, v: owner.id, ok: 0 });
      }
      return;
    }
    // انزلاق
    const sp = Math.max(hyp(p.vx, p.vy), 5) + 2.6;
    let a = p.face;
    const ml = hyp(inp.mx || 0, inp.my || 0);
    if (ml > 0.2) a = Math.atan2(inp.my, inp.mx);
    p.face = a;
    p.vx = Math.cos(a) * sp; p.vy = Math.sin(a) * sp;
    p.state = STATE.SLIDE; p.stateT = 0.55; p.tackleCD = 1.6; p.slideHit = false;
    if (b.owner === p.id) { b.owner = -1; }
    this.ev({ e: 'slide', p: p.id });
  }

  // ---------- القدرات الخاصة ----------
  useAbility(p, inp) {
    if (p.cd > 0 || this.phase !== PHASE.PLAY || p.state !== STATE.NORMAL) return false;
    const ab = p.c.ability;
    const b = this.ball;
    const opps = this.players.filter((o) => o.team !== p.team);
    let ok = true;
    const extra = {};
    switch (ab.id) {
      case 'fire': p.buff.fire = 7; break;
      case 'curve': p.buff.curve = 7; break;
      case 'dash': p.buff.dash = 3; p.stamina = 1; break;
      case 'pull': p.buff.magnet = 1.8; break;
      case 'quake': {
        for (const o of opps) {
          const dx = o.x - p.x, dy = o.y - p.y, d = hyp(dx, dy);
          if (d < 5.5) {
            const n = d || 1;
            o.vx = (dx / n) * 9; o.vy = (dy / n) * 9;
            o.state = STATE.DOWN; o.stateT = 1.25;
            if (b.owner === o.id) { b.owner = -1; b.gk = false; o.pickupCD = 0.8; b.vx = (dx / n) * 7; b.vy = (dy / n) * 7; b.vz = 3; }
          }
        }
        const bd = hyp(b.x - p.x, b.y - p.y);
        if (b.owner < 0 && bd < 5.5 && b.z < 1.5) {
          const n = bd || 1;
          b.vx = ((b.x - p.x) / n) * 11; b.vy = ((b.y - p.y) / n) * 11; b.vz = 4;
        }
        break;
      }
      case 'ice':
        this.zones.push({ k: 'ice', x: p.x, y: p.y, r: 8, t: 3.5, T: 3.5, team: p.team });
        break;
      case 'blink': {
        const dir = this.inputDir(p, inp);
        const fx = p.x, fy = p.y;
        p.x = clamp(p.x + dir.x * 7, -HL + 0.5, HL - 0.5);
        p.y = clamp(p.y + dir.y * 7, -HW + 0.5, HW - 0.5);
        p.face = Math.atan2(dir.y, dir.x);
        p.vx = dir.x * 6; p.vy = dir.y * 6;
        if (b.owner === p.id) { b.x += p.x - fx; b.y += p.y - fy; }
        extra.fx = r2(fx); extra.fy = r2(fy);
        break;
      }
      case 'bolt': {
        let best = null, bd = 13;
        for (const o of opps) {
          const d = hyp(o.x - p.x, o.y - p.y);
          const score = d - (b.owner === o.id ? 4 : 0);
          if (score < bd && o.state !== STATE.DOWN) { bd = score; best = o; }
        }
        if (!best) { ok = false; break; }
        best.state = STATE.DOWN; best.stateT = 1.6; best.vx *= 0.1; best.vy *= 0.1;
        if (b.owner === best.id) { b.owner = -1; b.gk = false; best.pickupCD = 1; b.vz = 2.5; b.vx = (this.rand() - 0.5) * 4; b.vy = (this.rand() - 0.5) * 4; }
        extra.v = best.id;
        break;
      }
      case 'wall': {
        const d = 2.2;
        this.zones.push({ k: 'wall', x: p.x + Math.cos(p.face) * d, y: p.y + Math.sin(p.face) * d, a: p.face + PI / 2, len: 3.8, t: 4, T: 4, team: p.team });
        break;
      }
    }
    if (!ok) { p.cd = 1.5; return false; }
    p.cd = ab.cd;
    this.ev({ e: 'ability', p: p.id, k: ab.id, x: r2(p.x), y: r2(p.y), ...extra });
    return true;
  }

  updateZones(dt) {
    for (const z of this.zones) {
      z.t -= dt;
      if (z.k === 'ice') {
        for (const o of this.players) if (o.team !== z.team && hyp(o.x - z.x, o.y - z.y) < z.r) o.buff.slow = Math.max(o.buff.slow, 0.6);
      }
    }
    this.zones = this.zones.filter((z) => z.t > 0);
  }

  // ---------- الركلات ----------
  canTouch(p) {
    const b = this.ball;
    if (b.owner === p.id) return true;
    if (b.owner >= 0) return false;
    const d = hyp(b.x - p.x, b.y - p.y);
    return d < PLAYER_R + BALL_R + 0.6 && b.z < 2.4;
  }

  tryKick(p, kind, power, opt) {
    if (this.canTouch(p)) this.performKick(p, kind, power, opt);
    else p.kickBuf = { kind, power, opt, t: 0.3 };
  }

  findPassTarget(p, dir, cone = 0.85, minD = 2.5) {
    let best = null, bestS = Infinity;
    for (const m of this.players) {
      if (m.team !== p.team || m === p || m.state === STATE.DOWN) continue;
      const dx = m.x - p.x, dy = m.y - p.y, d = hyp(dx, dy);
      if (d < minD || d > 45) continue;
      const cos = (dir.x * dx + dir.y * dy) / d;
      const ang = Math.acos(clamp(cos, -1, 1));
      if (ang > cone) continue;
      const s = ang * 12 + d * 0.1 - this.openness(m) * 0.4;
      if (s < bestS) { bestS = s; best = m; }
    }
    return best;
  }

  openness(m) {
    let md = 20;
    for (const o of this.players) if (o.team !== m.team) md = Math.min(md, hyp(o.x - m.x, o.y - m.y));
    return md;
  }

  performKick(p, kind, power, opt = {}) {
    const b = this.ball;
    const st = p.c.stats;
    const header = b.z > 1.05;
    const isThrow = this.phase === PHASE.SETPIECE && this.sp && this.sp.kind === 'throw';
    let dir = opt.dir || { x: Math.cos(p.face), y: Math.sin(p.face) };
    { const l = hyp(dir.x, dir.y) || 1; dir = { x: dir.x / l, y: dir.y / l }; }
    b.owner = -1; b.gk = false; b.spin = 0; b.fx = null; b.fxT = 0;
    b.lastTouch = p.id; b.lastTeam = p.team;
    p.pickupCD = 0.3; p.kickAnim = 0.4; p.hold.shoot = p.hold.lob = 0; p.kickBuf = null;
    if (isThrow) { b.z = 2.0; }
    const acc = p.human ? 0.8 + 0.1 * st.shot : this.diff.aim;
    const dA = this.attackDir(p.team), goalX = dA * HL;
    const gdx = goalX - b.x, gdy = -b.y, gd = hyp(gdx, gdy);
    const cosToGoal = (dir.x * gdx + dir.y * gdy) / (gd || 1);
    let vx = 0, vy = 0, vz = 0, spin = 0;
    let isShot = false;

    if (kind === 'shot') {
      let spd = header ? (10 + 8 * power) * st.shot : (14 + 16 * power) * st.shot;
      const fire = p.buff.fire > 0, curve = p.buff.curve > 0;
      if (fire) spd *= 1.4;
      if (curve) spd *= 1.1;
      let target = opt.point || null;
      if (!target && cosToGoal > 0.5 && gd < 48 && Math.sign(gdx) === dA) {
        let ty = 0;
        if (Math.abs(dir.x) > 0.08 && Math.sign(dir.x) === Math.sign(gdx)) ty = b.y + dir.y * (gdx / dir.x);
        const lim = GW / 2 - 0.45;
        if (ty > lim) ty = lim + (ty - lim) * 0.3;
        else if (ty < -lim) ty = -lim + (ty + lim) * 0.3;
        target = { x: goalX, y: ty };
      }
      if (target) {
        isShot = Math.abs(target.x) >= HL - 0.1;
        const td = hyp(target.x - b.x, target.y - b.y) || 1;
        const err = (this.rand() - 0.5) * 2 * (1 - acc) * 0.1 * td * (0.4 + power);
        const ty = target.y + err;
        let h = target.z != null ? target.z : 0.25 + power * 1.5 + (this.rand() - 0.5) * (1 - acc) * 2.4;
        if (header) h = 0.3 + power * 1.3;
        h = clamp(h, 0.12, 3.6);
        let ang = Math.atan2(ty - b.y, target.x - b.x);
        const t = td / (spd * 0.88);
        vz = clamp((h - b.z) / t + 0.5 * GRAVITY * t, -7, 11);
        if (curve || !header) {
          const alpha = curve ? 0.42 : (this.rand() - 0.5) * 0.08;
          const sgn = curve ? (this.rand() < 0.5 ? 1 : -1) : 1;
          ang -= sgn * alpha;
          spin = sgn * 2 * alpha * spd / td;
        }
        vx = Math.cos(ang) * spd; vy = Math.sin(ang) * spd;
      } else {
        vx = dir.x * spd; vy = dir.y * spd; vz = header ? 1.5 : 1 + power * 4.5;
      }
      if (fire) { b.fx = 'fire'; b.fxT = 2.5; p.buff.fire = 0; }
      else if (curve) { b.fx = 'curve'; b.fxT = 2.5; p.buff.curve = 0; }
      if (isShot) { p.st.sh++; this.lastShot = { team: p.team, t: this.time, p: p.id }; }
    } else if (kind === 'pass') {
      const mate = opt.mate || this.findPassTarget(p, dir);
      if (mate) {
        let tx = mate.x, ty = mate.y, spd = 12;
        for (let i = 0; i < 3; i++) {
          const d = hyp(tx - b.x, ty - b.y);
          spd = clamp(7.5 + d * 0.72, 9, 25) * Math.sqrt(st.pass);
          const t = (d / spd) * 1.2;
          tx = mate.x + mate.vx * t * 0.9; ty = mate.y + mate.vy * t * 0.9;
        }
        let ang = Math.atan2(ty - b.y, tx - b.x) + (this.rand() - 0.5) * 0.05 * (1.2 - acc);
        if (isThrow) spd = Math.min(spd, 15);
        vx = Math.cos(ang) * spd; vy = Math.sin(ang) * spd;
        vz = isThrow ? 2.5 : b.z > 0.6 ? 0.5 : 0;
        this.lastPass = { id: p.id, team: p.team, t: this.time };
        p.st.ps++;
      } else {
        const spd = isThrow ? 12 : 13;
        vx = dir.x * spd; vy = dir.y * spd; vz = isThrow ? 2.5 : 0;
      }
    } else {
      // لوب: تمريرة عالية أو تسديدة ساقطة
      let mate = opt.mate || null;
      if (!mate && !opt.point) mate = this.findPassTarget(p, dir, 0.7, 6);
      let tx, ty, landZ = BALL_R;
      if (opt.point) { tx = opt.point.x; ty = opt.point.y; }
      else if (mate) {
        const d0 = hyp(mate.x - b.x, mate.y - b.y);
        const T0 = 0.8 + d0 * 0.045;
        tx = mate.x + mate.vx * T0 * 0.8; ty = mate.y + mate.vy * T0 * 0.8;
        landZ = 1.0;
        this.lastPass = { id: p.id, team: p.team, t: this.time };
        p.st.ps++;
      } else if (cosToGoal > 0.6 && gd < 34 && !isThrow) {
        tx = goalX; ty = clamp(b.y + dir.y * (gdx / (dir.x || 0.01)), -GW / 2 + 0.6, GW / 2 - 0.6);
        landZ = 1.6;
        isShot = true; p.st.sh++; this.lastShot = { team: p.team, t: this.time, p: p.id };
      } else {
        const dist = isThrow ? 8 + 10 * power : 10 + 24 * power;
        tx = b.x + dir.x * dist; ty = b.y + dir.y * dist;
      }
      const d = hyp(tx - b.x, ty - b.y) || 1;
      const T = 0.75 + d * 0.043;
      const hs = (d / T) * 1.07;
      vx = ((tx - b.x) / d) * hs; vy = ((ty - b.y) / d) * hs;
      vz = clamp((landZ - b.z + 0.5 * GRAVITY * T * T) / T, 2, 14);
    }
    b.vx = vx; b.vy = vy; b.vz = vz; b.spin = spin;
    if (b.z < BALL_R) b.z = BALL_R;
    this.ev({ e: 'kick', p: p.id, k: kind, pw: r2(power), h: header ? 1 : 0, s: isShot ? 1 : 0, fx: b.fx ? FX_CODE[b.fx] : 0 });
    if (this.phase === PHASE.SETPIECE) { this.phase = PHASE.PLAY; this.phaseT = 0; this.sp = null; }
  }

  // ---------- الكرة ----------
  updateBall(dt) {
    const b = this.ball;
    b.prevX = b.x; b.prevY = b.y; b.prevZ = b.z;
    if (b.fxT > 0) { b.fxT -= dt; if (b.fxT <= 0) b.fx = null; }

    if (b.owner >= 0) {
      const p = this.players[b.owner];
      if (p.state !== STATE.NORMAL && !(this.phase === PHASE.SETPIECE)) {
        b.owner = -1; b.gk = false;
      } else if (b.gk) {
        p.heldT += dt;
        if (!this.inOwnBox(p, 0.3)) { b.gk = false; }
        b.x = p.x + Math.cos(p.face) * 0.35; b.y = p.y + Math.sin(p.face) * 0.35; b.z = 1.05;
        b.vx = p.vx; b.vy = p.vy; b.vz = 0; b.spin = 0;
        if (p.heldT > 6) this.aiDistribute(p);
        return;
      } else if (this.phase === PHASE.SETPIECE && this.sp && this.sp.taker === p.id) {
        const thr = this.sp.kind === 'throw';
        b.x = thr ? p.x + Math.cos(p.face) * 0.15 : this.sp.x;
        b.y = thr ? p.y + Math.sin(p.face) * 0.15 : this.sp.y;
        b.z = thr ? 2.0 : BALL_R;
        b.vx = b.vy = b.vz = 0;
        return;
      } else {
        const spd = hyp(p.vx, p.vy);
        const fx = Math.cos(p.face), fy = Math.sin(p.face);
        const reach = PLAYER_R * p.c.look.build + BALL_R + 0.18 + spd * 0.035;
        const tx = p.x + fx * reach, ty = p.y + fy * reach;
        const k = 11 * p.c.stats.dribble;
        b.vx = p.vx + (tx - b.x) * k; b.vy = p.vy + (ty - b.y) * k;
        b.vz = 0; b.z = BALL_R;
        b.spin *= 0.9;
        b.x += b.vx * dt; b.y += b.vy * dt;
        // لمسات أطول أثناء الركض السريع
        if (p.sprinting && spd > 6.6 && p.touchT <= 0) {
          b.owner = -1;
          b.vx = p.vx * 1.38 + fx * 0.6; b.vy = p.vy * 1.38 + fy * 0.6;
          p.pickupCD = 0.22; p.touchT = 0.7;
          this.ev({ e: 'touch', p: p.id });
        }
        this.contestOwner(p);
        this.checkBounds();
        return;
      }
    }

    // جذب مغناطيسي
    for (const p of this.players) {
      if (p.buff.magnet > 0) {
        const dx = p.x - b.x, dy = p.y - b.y, d = hyp(dx, dy);
        if (d < 20 && d > 0.8) {
          b.vx += (dx / d) * 34 * dt; b.vy += (dy / d) * 34 * dt;
          const hs = hyp(b.vx, b.vy);
          if (hs > 16) { b.vx *= 16 / hs; b.vy *= 16 / hs; }
          b.vz -= b.vz * 3 * dt;
        }
      }
    }

    // تكامل الحركة
    const onGround = b.z <= BALL_R + 0.001 && Math.abs(b.vz) < 0.01;
    if (!onGround) {
      b.vz -= GRAVITY * dt;
      const sp = hyp(b.vx, b.vy, b.vz);
      const drag = Math.max(0, 1 - 0.0085 * sp * dt);
      b.vx *= drag; b.vy *= drag; b.vz *= drag;
    }
    if (b.spin) {
      const ax = -b.spin * b.vy, ay = b.spin * b.vx;
      b.vx += ax * dt; b.vy += ay * dt;
      b.spin *= Math.max(0, 1 - (onGround ? 3 : 0.4) * dt);
      if (Math.abs(b.spin) < 0.01) b.spin = 0;
    }
    b.x += b.vx * dt; b.y += b.vy * dt; b.z += b.vz * dt;
    if (b.z < BALL_R) {
      b.z = BALL_R;
      if (b.vz < -1.4) {
        const imp = -b.vz;
        b.vz = imp * 0.55 * this.stadium.bounce;
        const f = 0.9;
        b.vx *= f; b.vy *= f;
        if (imp > 4) this.ev({ e: 'bounce', v: r1(imp) });
      } else b.vz = 0;
    }
    if (b.z <= BALL_R + 0.001 && b.vz === 0) {
      const sp = hyp(b.vx, b.vy);
      if (sp > 0) {
        const fr = this.stadium.friction;
        const ns = Math.max(0, sp - (0.8 * fr + 0.34 * sp * fr) * dt);
        b.vx *= ns / sp; b.vy *= ns / sp;
      }
    }

    this.collideGoal(b);
    this.collideWalls(b);
    if (this.phase === PHASE.PLAY || this.phase === PHASE.SETPIECE || this.phase === PHASE.OUT || this.phase === PHASE.KICKOFF) this.ballPlayers(dt);
    this.checkBounds();
  }

  contestOwner(o) {
    const b = this.ball;
    const dO = hyp(b.x - o.x, b.y - o.y);
    for (const q of this.players) {
      if (q.team === o.team || q.state !== STATE.NORMAL || q.pickupCD > 0) continue;
      const dq = hyp(b.x - q.x, b.y - q.y);
      if (dq < PLAYER_R + BALL_R + 0.12 && dq < dO - 0.08) {
        b.owner = q.id; o.pickupCD = 0.55;
        b.lastTouch = q.id; b.lastTeam = q.team;
        q.st.tk++;
        this.ev({ e: 'steal', p: q.id, v: o.id });
        return;
      }
    }
  }

  ballPlayers(dt) {
    const b = this.ball;
    if (this.phase === PHASE.OUT || this.phase === PHASE.KICKOFF) return;
    let best = null, bestD = Infinity;
    const bsp = hyp(b.vx, b.vy, b.vz);
    for (const p of this.players) {
      if (p.state === STATE.DOWN || p.state === STATE.CELEBRATE || p.state === STATE.SAD) continue;
      const dx = b.x - p.x, dy = b.y - p.y, d = hyp(dx, dy);
      const pr = PLAYER_R * p.c.look.build;
      // يدا الحارس داخل منطقته
      if (this.isGK(p) && this.inOwnBox(p, 0.3) && b.lastTeam !== -1) {
        const dive = p.state === STATE.DIVE;
        const reachMul = (p.human ? 1 : this.diff.reach) * (0.85 + 0.15 * p.c.stats.keeper);
        const reach = pr + BALL_R + (dive ? 1.25 : 0.7) * reachMul;
        const hmax = dive ? 2.0 : 2.65;
        if (d < reach && b.z < hmax && p.pickupCD <= 0 && !(b.lastTouch === p.id && bsp > 3)) {
          this.keeperTouch(p, bsp);
          return;
        }
      }
      const reachFeet = pr + BALL_R + (p.state === STATE.SLIDE ? 0.55 : 0.28);
      if (d < reachFeet && b.z < 1.05) {
        if (p.state === STATE.SLIDE) {
          if (p.pickupCD > 0) continue;
          const a = Math.atan2(p.vy, p.vx);
          b.vx = Math.cos(a) * 8.5 + p.vx * 0.2; b.vy = Math.sin(a) * 8.5 + p.vy * 0.2; b.vz = 1.2;
          b.lastTouch = p.id; b.lastTeam = p.team; p.slideHit = true; p.pickupCD = 0.4;
          p.st.tk++;
          this.ev({ e: 'tackle', p: p.id, ok: 1 });
          return;
        }
        if (p.pickupCD > 0 || p.state !== STATE.NORMAL) continue;
        if (d < bestD) { bestD = d; best = p; }
      } else if (d < pr + BALL_R + 0.3 && b.z >= 1.05 && b.z < 1.95 * p.c.look.height + 0.35 && p.state === STATE.NORMAL && p.pickupCD <= 0) {
        // رأسية أو صدر
        if (p.kickBuf || (!p.human && b.lastTouch !== p.id)) {
          if (!p.human) this.aiHeader(p);
          else this.performKick(p, p.kickBuf.kind, p.kickBuf.power, p.kickBuf.opt);
          return;
        }
        // ترويض بالصدر أو ارتداد
        if (bsp < 13 && b.z < 1.6) {
          b.vx = p.vx + dx * 1.2; b.vy = p.vy + dy * 1.2; b.vz = -1;
          b.lastTouch = p.id; b.lastTeam = p.team;
        } else this.deflect(p, dx, dy, d, 0.35);
        p.pickupCD = 0.12;
        return;
      }
    }
    if (best) {
      const p = best;
      const rel = hyp(b.vx - p.vx, b.vy - p.vy);
      if (p.kickBuf) { this.performKick(p, p.kickBuf.kind, p.kickBuf.power, p.kickBuf.opt); return; }
      if (rel < 13 + 3 * p.c.stats.dribble || b.lastTeam === p.team) {
        const prevOwnerTeam = b.lastTeam;
        b.owner = p.id; b.gk = false;
        b.lastTouch = p.id; b.lastTeam = p.team; b.fx = null;
        p.touchT = 0.35;
        if (prevOwnerTeam !== p.team && prevOwnerTeam !== -1 && this.lastShot && this.time - this.lastShot.t < 1.5 && rel > 10) {
          this.ev({ e: 'block', p: p.id });
        }
        this.ev({ e: 'control', p: p.id });
      } else {
        this.deflect(p, b.x - p.x, b.y - p.y, bestD, 0.35);
        p.pickupCD = 0.15;
        this.ev({ e: 'block', p: p.id });
      }
    }
  }

  deflect(p, dx, dy, d, rest) {
    const b = this.ball;
    const n = d || 1, nx = dx / n, ny = dy / n;
    const vn = (b.vx - p.vx) * nx + (b.vy - p.vy) * ny;
    if (vn < 0) { b.vx -= (1 + rest) * vn * nx; b.vy -= (1 + rest) * vn * ny; }
    b.vx *= 0.7; b.vy *= 0.7;
    b.x = p.x + nx * (PLAYER_R + BALL_R + 0.05); b.y = p.y + ny * (PLAYER_R + BALL_R + 0.05);
    b.lastTouch = p.id; b.lastTeam = p.team;
  }

  keeperTouch(p, bsp) {
    const b = this.ball;
    const shotBy = b.lastTeam !== p.team && b.lastTeam !== -1;
    const catchLim = (13.5 + 4.5 * p.c.stats.keeper) * (p.human ? 1 : this.diff.reach) + (p.state === STATE.DIVE ? 2 : 0);
    const towardGoal = shotBy && this.lastShot && this.time - this.lastShot.t < 2.5;
    if (b.fx === 'fire' && bsp > 12) {
      // الكرة النارية تطيح بالحارس
      p.state = STATE.DOWN; p.stateT = 1.1;
      const dA = this.attackDir(p.team);
      b.vx = Math.abs(b.vx) * 0.35 * dA; b.vy = b.vy * 0.6 + (this.rand() - 0.5) * 6; b.vz = 4;
      b.fx = null; b.lastTouch = p.id; b.lastTeam = p.team;
      p.pickupCD = 1;
      if (towardGoal) { p.st.sv++; this.ev({ e: 'save', p: p.id, k: 'burn' }); }
      return;
    }
    if (bsp < catchLim) {
      b.owner = p.id; b.gk = true; p.heldT = 0;
      b.vx = b.vy = b.vz = 0; b.spin = 0; b.fx = null;
      b.lastTouch = p.id; b.lastTeam = p.team;
      if (towardGoal) { p.st.sv++; this.ev({ e: 'save', p: p.id, k: 'catch' }); }
      else this.ev({ e: 'control', p: p.id });
    } else {
      // صد بعيداً عن المرمى
      const dA = this.attackDir(p.team);
      b.vx = Math.abs(b.vx) * 0.4 * dA + dA * 2;
      b.vy = b.vy * 0.5 + (this.rand() < 0.5 ? -1 : 1) * (3 + this.rand() * 4);
      b.vz = 2 + this.rand() * 4;
      b.spin = 0;
      b.lastTouch = p.id; b.lastTeam = p.team;
      p.pickupCD = 0.5;
      if (towardGoal) { p.st.sv++; this.ev({ e: 'save', p: p.id, k: 'parry' }); }
    }
  }

  collideGoal(b) {
    for (const s of [-1, 1]) {
      const gx = s * HL;
      // القائمان
      if (b.z < GH + BALL_R) {
        for (const py of [-GW / 2, GW / 2]) {
          const dx = b.x - gx, dy = b.y - py, d = hyp(dx, dy);
          const min = POST_R + BALL_R;
          if (d < min && d > 1e-6) {
            const nx = dx / d, ny = dy / d;
            b.x = gx + nx * min; b.y = py + ny * min;
            const vn = b.vx * nx + b.vy * ny;
            if (vn < 0) {
              b.vx -= 1.7 * vn * nx; b.vy -= 1.7 * vn * ny;
              if (-vn > 3) this.ev({ e: 'post', v: r1(-vn) });
            }
          }
        }
      }
      // العارضة
      if (Math.abs(b.y) < GW / 2) {
        const dx = b.x - gx, dz = b.z - GH, d = hyp(dx, dz);
        const min = POST_R + BALL_R;
        if (d < min && d > 1e-6) {
          const nx = dx / d, nz = dz / d;
          b.x = gx + nx * min; b.z = GH + nz * min;
          const vn = b.vx * nx + b.vz * nz;
          if (vn < 0) {
            b.vx -= 1.65 * vn * nx; b.vz -= 1.65 * vn * nz;
            if (-vn > 3) this.ev({ e: 'post', v: r1(-vn), bar: 1 });
          }
        }
      }
      // الشباك
      const ax = Math.abs(b.x), pax = Math.abs(b.prevX);
      const insideGoal = ax > HL && Math.sign(b.x) === s;
      if (insideGoal && ax < HL + GD + 0.5) {
        const wasInside = Math.abs(b.prevY) < GW / 2 - BALL_R + 0.05 && b.prevZ < GH - BALL_R + 0.05;
        if (wasInside || b.inNet) {
          let hit = false;
          const back = HL + GD - BALL_R;
          if (ax > back) { b.x = s * back; b.vx *= -0.12; b.vy *= 0.5; b.vz *= 0.5; hit = true; }
          const sideLim = GW / 2 - BALL_R;
          if (Math.abs(b.y) > sideLim) { b.y = Math.sign(b.y) * sideLim; b.vy *= -0.15; b.vx *= 0.6; hit = true; }
          if (b.z > GH - BALL_R) { b.z = GH - BALL_R; b.vz = -Math.abs(b.vz) * 0.1; b.vx *= 0.6; hit = true; }
          if (hit && !b.netHitT) { this.ev({ e: 'net', x: r2(b.x), y: r2(b.y), z: r2(b.z), s: r1(hyp(b.vx, b.vy) + 3) }); b.netHitT = 0.5; }
        } else if (pax > HL) {
          // من الخارج: الشباك الجانبية والعلوية
          if (Math.abs(b.prevY) >= GW / 2 && Math.abs(b.y) < GW / 2 + BALL_R && b.z < GH) { b.y = Math.sign(b.prevY) * (GW / 2 + BALL_R); b.vy *= -0.2; b.vx *= 0.5; }
          if (b.prevZ >= GH && b.z < GH + BALL_R && Math.abs(b.y) < GW / 2) { b.z = GH + BALL_R; b.vz = Math.abs(b.vz) * 0.2; b.vx *= 0.6; }
        }
      }
    }
    if (b.netHitT) { b.netHitT -= 1 / 60; if (b.netHitT <= 0) b.netHitT = 0; }
  }

  collideWalls(b) {
    for (const z of this.zones) {
      if (z.k !== 'wall' || b.z > 2.6) continue;
      const cx = Math.cos(z.a), cy = Math.sin(z.a);
      const rx = b.x - z.x, ry = b.y - z.y;
      const along = clamp(rx * cx + ry * cy, -z.len / 2, z.len / 2);
      const px = z.x + cx * along, py = z.y + cy * along;
      const dx = b.x - px, dy = b.y - py, d = hyp(dx, dy);
      const min = BALL_R + 0.25;
      if (d < min && d > 1e-6) {
        const nx = dx / d, ny = dy / d;
        b.x = px + nx * min; b.y = py + ny * min;
        const vn = b.vx * nx + b.vy * ny;
        if (vn < 0) { b.vx -= 1.6 * vn * nx; b.vy -= 1.6 * vn * ny; this.ev({ e: 'wallhit', x: r2(b.x), y: r2(b.y) }); }
      }
    }
  }

  checkBounds() {
    if (this.phase !== PHASE.PLAY) return;
    const b = this.ball;
    const ax = Math.abs(b.x);
    if (ax > HL + BALL_R) {
      const s = Math.sign(b.x);
      if (Math.abs(b.y) < GW / 2 && b.z < GH && !b.gk) return this.goalScored(s > 0 ? 0 : 1);
      if (b.owner >= 0 && b.gk) return;
      const defTeam = s > 0 ? 1 : 0;
      const kind = b.lastTeam === defTeam ? 'corner' : 'goalkick';
      const team = kind === 'corner' ? 1 - defTeam : defTeam;
      if (this.lastShot && this.time - this.lastShot.t < 3 && Math.abs(b.y) < GW / 2 + 3 && b.lastTeam !== defTeam) this.ev({ e: 'nearmiss' });
      return this.startOut(kind, team, s, Math.sign(b.y) || 1);
    }
    if (Math.abs(b.y) > HW + BALL_R) {
      return this.startOut('throw', b.lastTeam === 0 ? 1 : 0, Math.sign(b.x), Math.sign(b.y));
    }
  }

  startOut(kind, team, sx, sy) {
    const b = this.ball;
    if (b.owner >= 0) { b.owner = -1; b.gk = false; }
    let x, y;
    if (kind === 'throw') { x = clamp(b.x, -HL + 1, HL - 1); y = sy * HW; }
    else if (kind === 'corner') { x = sx * (HL - 0.4); y = sy * (HW - 0.4); }
    else { x = sx * (HL - 3.5); y = 0; }
    this.pendingSP = { kind, team, x, y };
    this.phase = PHASE.OUT; this.phaseT = 0;
    this.ev({ e: 'out', k: kind, team });
  }

  setupSetPiece() {
    const sp = this.pendingSP;
    this.pendingSP = null;
    if (!sp) { this.phase = PHASE.PLAY; return; }
    const b = this.ball;
    const mates = this.players.filter((p) => p.team === sp.team && p.state !== STATE.DOWN);
    let taker;
    if (sp.kind === 'goalkick') taker = mates.find((p) => this.isGK(p)) || mates[0];
    else {
      let bd = Infinity;
      for (const p of mates) {
        if (this.isGK(p)) continue;
        const d = hyp(p.x - sp.x, p.y - sp.y) - (p.human ? 4 : 0);
        if (d < bd) { bd = d; taker = p; }
      }
    }
    taker = taker || mates[0];
    // وجه اللاعب نحو الملعب
    const fa = Math.atan2(-sp.y * 0.6, -sp.x * (sp.kind === 'throw' ? 0.2 : 1) + (sp.kind === 'throw' ? this.attackDir(sp.team) * 8 : 0));
    let face = sp.kind === 'goalkick' ? (this.attackDir(sp.team) > 0 ? 0 : PI) : fa;
    if (sp.kind === 'throw') face = sp.y > 0 ? -PI / 2 + this.attackDir(sp.team) * 0.5 : PI / 2 - this.attackDir(sp.team) * 0.5;
    taker.face = face;
    const back = sp.kind === 'throw' ? 0.2 : 0.55;
    taker.x = sp.x - Math.cos(face) * back; taker.y = sp.y - Math.sin(face) * back;
    taker.vx = taker.vy = 0; taker.state = STATE.NORMAL; taker.hold.shoot = taker.hold.lob = 0;
    Object.assign(b, { x: sp.x, y: sp.y, z: sp.kind === 'throw' ? 2 : BALL_R, vx: 0, vy: 0, vz: 0, spin: 0, owner: taker.id, gk: false, fx: null, lastTouch: taker.id, lastTeam: taker.team, inNet: false });
    // إبعاد الخصوم
    for (const o of this.players) {
      if (o.team === sp.team) continue;
      const dx = o.x - sp.x, dy = o.y - sp.y, d = hyp(dx, dy);
      if (d < 4.5) { const n = d || 1; o.x = sp.x + (dx / n) * 4.5; o.y = sp.y + (dy / n) * 4.5; this.clampPlayer(o); }
    }
    this.sp = { ...sp, taker: taker.id };
    this.phase = PHASE.SETPIECE; this.phaseT = 0;
    this.ev({ e: 'setpiece', k: sp.kind, p: taker.id });
  }

  goalScored(team) {
    const b = this.ball;
    b.inNet = true;
    const scorer = this.players[b.lastTouch] || null;
    const own = scorer ? scorer.team !== team : false;
    let assist = -1;
    if (!own && this.lastPass && this.lastPass.team === team && scorer && this.lastPass.id !== scorer.id && this.time - this.lastPass.t < 9) assist = this.lastPass.id;
    this.score[team]++;
    if (scorer && !own) scorer.st.g++;
    if (assist >= 0) this.players[assist].st.a++;
    this.goalInfo = { team, scorer: scorer ? scorer.id : -1, assist, own, t: this.time };
    this.phase = PHASE.GOAL; this.phaseT = 0;
    const dist = scorer ? hyp(scorer.x - Math.sign(b.x) * HL, scorer.y) : 0;
    this.ev({ e: 'goal', team, p: scorer ? scorer.id : -1, a: assist, own: own ? 1 : 0, sc: this.score.slice(), d: r1(dist), fx: b.fx ? FX_CODE[b.fx] : 0 });
    if (this.golden) this.ev({ e: 'goldengoal', team });
    for (const p of this.players) {
      p.hold.shoot = p.hold.lob = 0; p.kickBuf = null;
      if (p.state !== STATE.DOWN) p.state = STATE.NORMAL;
      if (scorer && p.id === scorer.id && !own) { p.state = STATE.CELEBRATE; p.emote = p.human && p.emoteReq ? p.emoteReq : p.c.celebration; p.stateT = 10; }
    }
  }

  // ---------- الاحتفالات ----------
  stepCelebration(dt) {
    const gi = this.goalInfo;
    const scorer = gi && gi.scorer >= 0 && !gi.own ? this.players[gi.scorer] : null;
    for (const p of this.players) {
      let tx = p.x, ty = p.y, mul = 0.9;
      if (this.phase === PHASE.GOAL) {
        if (p.team === (gi ? gi.team : -1)) {
          if (scorer && p !== scorer) {
            const a = (p.slot / 5) * TAU;
            tx = scorer.x + Math.cos(a) * 1.3; ty = scorer.y + Math.sin(a) * 1.3;
            mul = 1.25;
            if (hyp(tx - p.x, ty - p.y) < 0.8 && p.state === STATE.NORMAL && this.phaseT > 0.5) { p.state = STATE.CELEBRATE; p.emote = 6; p.stateT = 10; }
          }
        } else if (this.phaseT > 0.6 && p.state === STATE.NORMAL) { p.state = STATE.SAD; p.stateT = 10; }
      }
      if (p.state === STATE.CELEBRATE || p.state === STATE.SAD) {
        const f = Math.max(0, 1 - 4 * dt);
        p.vx *= f; p.vy *= f;
        p.x += p.vx * dt; p.y += p.vy * dt;
        if (scorer && p.state === STATE.CELEBRATE && p !== scorer) p.face += angDiff(p.face, Math.atan2(scorer.y - p.y, scorer.x - p.x)) * Math.min(1, 6 * dt);
      } else if (p.state === STATE.DOWN || p.state === STATE.STUMBLE || p.state === STATE.SLIDE || p.state === STATE.DIVE) {
        p.stateT -= dt;
        const f = Math.max(0, 1 - 5 * dt);
        p.vx *= f; p.vy *= f; p.x += p.vx * dt; p.y += p.vy * dt;
        if (p.stateT <= 0) p.state = STATE.NORMAL;
      } else {
        const dx = tx - p.x, dy = ty - p.y, d = hyp(dx, dy);
        const m = d > 0.3 ? Math.min(1, d / 1.5) : 0;
        integrateMovement(p, d ? (dx / d) * m : 0, d ? (dy / d) * m : 0, d > 4, dt, p.c.stats.speed * mul);
        if (hyp(p.vx, p.vy) > 0.5) p.face += angDiff(p.face, Math.atan2(p.vy, p.vx)) * Math.min(1, 10 * dt);
      }
      this.clampPlayer(p);
    }
    this.collidePlayers();
    if (this.phase === PHASE.END) this.updateBallFree(dt);
  }

  updateBallFree(dt) {
    const b = this.ball;
    if (b.owner >= 0) { b.owner = -1; b.gk = false; }
    b.vz -= GRAVITY * dt;
    b.x += b.vx * dt; b.y += b.vy * dt; b.z += b.vz * dt;
    if (b.z < BALL_R) { b.z = BALL_R; b.vz = b.vz < -1.4 ? -b.vz * 0.5 : 0; }
    const f = Math.max(0, 1 - 1.2 * dt);
    b.vx *= f; b.vy *= f;
    this.collideGoal(b);
  }

  // ---------- الذكاء الاصطناعي ----------
  aiInput(p, dt) {
    const ai = p.ai;
    const out = { mx: 0, my: 0, b: 0 };
    if (this.phase === PHASE.KICKOFF) return out;
    ai.t -= dt;
    if (ai.t <= 0) {
      ai.t = this.diff.think * (0.7 + this.rand() * 0.6);
      this.aiThink(p);
    }
    const dx = ai.tx - p.x, dy = ai.ty - p.y, d = hyp(dx, dy);
    if (d > 0.25) {
      const m = Math.min(1, d / 1.4);
      out.mx = (dx / d) * m; out.my = (dy / d) * m;
    }
    if (ai.sprint && d > 2.5 && (p.stamina > 0.25 || ai.mode === 'chase')) out.b |= BTN.SPRINT;
    return out;
  }

  predictBall(t) {
    const b = this.ball;
    // تقريب للاحتكاك
    const k = 0.34 * this.stadium.friction;
    const f = (1 - Math.exp(-k * t)) / k;
    return { x: b.x + b.vx * f, y: b.y + b.vy * f };
  }

  interceptPoint(p) {
    const b = this.ball;
    if (b.owner >= 0) {
      const o = this.players[b.owner];
      return { x: b.x + o.vx * 0.25, y: b.y + o.vy * 0.25 };
    }
    const sp = 5.7 * 1.35 * p.c.stats.speed;
    let t = hyp(b.x - p.x, b.y - p.y) / sp;
    let pt = this.predictBall(t);
    for (let i = 0; i < 3; i++) {
      t = hyp(pt.x - p.x, pt.y - p.y) / sp;
      if (b.z > 0.5 && b.vz !== 0) {
        const tl = (b.vz + Math.sqrt(Math.max(0, b.vz * b.vz + 2 * GRAVITY * (b.z - BALL_R)))) / GRAVITY;
        t = Math.max(t, tl * 0.9);
      }
      pt = this.predictBall(t);
    }
    return { x: clamp(pt.x, -HL - 1, HL + 1), y: clamp(pt.y, -HW - 1, HW + 1) };
  }

  formationPos(p, attacking) {
    const dA = this.attackDir(p.team);
    const b = this.ball;
    const base = [[0, 0], [-17, -7], [-17, 7], [-5, -8.5], [-5, 8.5]][p.slot];
    const bx = b.x * dA;
    let xr = base[0] + bx * 0.62 + (attacking ? 8 : -2.5);
    if (p.slot >= 3 && attacking) xr += 3;
    if (p.slot <= 2 && !attacking) xr = Math.min(xr, bx - 3);
    xr = clamp(xr, -HL + 3.5, HL - 4);
    const y = clamp(base[1] * (attacking ? 1.05 : 0.8) + b.y * 0.35, -HW + 2, HW - 2);
    return { x: xr * dA, y };
  }

  aiThink(p) {
    const ai = p.ai;
    const b = this.ball;
    const R = this.rand;
    ai.sprint = false; ai.mode = '';
    if (this.phase === PHASE.SETPIECE) {
      if (this.sp && this.sp.taker === p.id) return;
      const attacking = this.sp && this.sp.team === p.team;
      const f = this.formationPos(p, attacking);
      if (this.isGK(p)) { this.aiKeeper(p); return; }
      let tx = f.x, ty = f.y;
      if (this.sp && this.sp.kind === 'corner') {
        const gx = Math.sign(this.sp.x) * HL;
        const inward = -Math.sign(this.sp.x);
        if (attacking && p.slot >= 1) { tx = gx + inward * (4 + p.slot * 1.6); ty = (p.slot - 2.5) * 2.4; }
        if (!attacking && p.slot >= 1) { tx = gx + inward * (2.5 + p.slot); ty = (p.slot - 2.5) * 2.2; }
      }
      if (!attacking && this.sp && hyp(tx - this.sp.x, ty - this.sp.y) < 5) { tx += (tx - this.sp.x) > 0 ? 3 : -3; }
      ai.tx = tx; ai.ty = ty; ai.sprint = true;
      return;
    }
    if (this.phase === PHASE.OUT) {
      const f = this.formationPos(p, this.pendingSP && this.pendingSP.team === p.team);
      ai.tx = f.x; ai.ty = f.y;
      if (this.isGK(p)) this.aiKeeper(p);
      return;
    }
    if (this.isGK(p)) { this.aiKeeper(p); return; }
    const owner = b.owner >= 0 ? this.players[b.owner] : null;
    if (owner === p) return this.aiWithBall(p);
    const teamHas = owner && owner.team === p.team;
    if (teamHas) {
      const f = this.formationPos(p, true);
      // الابتعاد عن الرقابة
      let ox = 0, oy = 0;
      for (const o of this.players) {
        if (o.team === p.team) continue;
        const dx = f.x - o.x, dy = f.y - o.y, d = hyp(dx, dy);
        if (d < 4 && d > 0.01) { ox += (dx / d) * (4 - d); oy += (dy / d) * (4 - d); }
      }
      ai.tx = f.x + ox; ai.ty = f.y + oy;
      ai.sprint = hyp(ai.tx - p.x, ai.ty - p.y) > 7;
      return;
    }
    // الدفاع أو الكرة الحرة: من يطارد؟
    const mates = this.players.filter((m) => m.team === p.team && !this.isGK(m) && m.state !== STATE.DOWN);
    const times = mates.map((m) => ({ m, t: hyp(b.x - m.x, b.y - m.y) / (m.c.stats.speed) - (m.human ? 1.5 : 0) }));
    times.sort((a, c) => a.t - c.t);
    const rank = times.findIndex((e) => e.m === p);
    const humanFirst = times[0] && times[0].m.human;
    const chaser = rank === 0 || (rank === 1 && humanFirst && owner && owner.team !== p.team && false);
    const ownGoalX = -this.attackDir(p.team) * HL;
    if (chaser) {
      const ip = this.interceptPoint(p);
      ai.tx = ip.x; ai.ty = ip.y; ai.sprint = true; ai.mode = 'chase';
      const db = hyp(b.x - p.x, b.y - p.y);
      if (owner && owner.team !== p.team) {
        // اقترب من جهة المرمى
        const gx = ownGoalX - owner.x, gy = -owner.y, gl = hyp(gx, gy) || 1;
        if (db > 2.2) { ai.tx = owner.x + (gx / gl) * 1.2 + owner.vx * 0.3; ai.ty = owner.y + (gy / gl) * 1.2 + owner.vy * 0.3; }
        if (db < 1.7 && p.tackleCD <= 0 && R() < this.diff.tackle * 0.6) this.tackle(p, { mx: 0, my: 0 });
        else if (db < 3.2 && db > 1.4 && p.tackleCD <= 0 && R() < this.diff.tackle * 0.07 && hyp(p.vx, p.vy) > 4) {
          this.tackle(p, { mx: (b.x - p.x) / db, my: (b.y - p.y) / db });
        }
        this.aiDefAbility(p, owner, db);
      } else if (!owner && db > 7 && p.c.ability.id === 'pull' && p.cd <= 0 && R() < 0.12) this.useAbility(p, { mx: 0, my: 0 });
      else if (!owner && db > 10 && p.c.ability.id === 'dash' && p.cd <= 0 && R() < 0.08) this.useAbility(p, { mx: 0, my: 0 });
      return;
    }
    if (rank === 1 || (rank === 2 && humanFirst)) {
      // تغطية بين الكرة والمرمى
      const gx = ownGoalX - b.x, gy = -b.y, gl = hyp(gx, gy) || 1;
      const dd = Math.min(6, gl * 0.4);
      ai.tx = b.x + (gx / gl) * dd; ai.ty = b.y + (gy / gl) * dd; ai.sprint = hyp(ai.tx - p.x, ai.ty - p.y) > 5;
      return;
    }
    // مراقبة
    const f = this.formationPos(p, false);
    let mark = null, md = 9;
    for (const o of this.players) {
      if (o.team === p.team || this.isGK(o) || o === owner) continue;
      const d = hyp(o.x - f.x, o.y - f.y);
      if (d < md) { md = d; mark = o; }
    }
    if (mark) {
      const gx = ownGoalX - mark.x, gy = -mark.y, gl = hyp(gx, gy) || 1;
      ai.tx = mark.x + (gx / gl) * 1.8; ai.ty = mark.y + (gy / gl) * 1.8;
    } else { ai.tx = f.x; ai.ty = f.y; }
    ai.sprint = hyp(ai.tx - p.x, ai.ty - p.y) > 6;
  }

  aiDefAbility(p, owner, db) {
    if (p.cd > 0 || this.rand() > 0.25) return;
    const id = p.c.ability.id;
    if (id === 'bolt' && db < 11) this.useAbility(p, {});
    else if (id === 'quake' && db < 4.5) this.useAbility(p, {});
    else if (id === 'ice' && db < 6) this.useAbility(p, {});
    else if (id === 'pull' && db < 8) this.useAbility(p, {});
    else if (id === 'dash' && db > 6) this.useAbility(p, {});
  }

  aiWithBall(p) {
    const ai = p.ai;
    const R = this.rand;
    const dA = this.attackDir(p.team), goalX = dA * HL;
    const gd = hyp(goalX - p.x, p.y);
    // ضغط الخصوم
    let press = null, pd = 99;
    for (const o of this.players) {
      if (o.team === p.team) continue;
      const dx = o.x - p.x, dy = o.y - p.y, d = hyp(dx, dy);
      const ahead = (dx * dA) > -0.5;
      if (ahead && d < pd) { pd = d; press = o; }
    }
    // التسديد
    const angleOK = Math.abs(p.y) < 11 || gd < 12;
    const inRange = (p.x * dA) > HL - 23;
    if (angleOK && inRange && (gd < 11 || R() < 0.33 + (pd < 2.5 ? 0.25 : 0))) {
      const gk = this.players.find((o) => o.team !== p.team && this.isGK(o));
      let ty = (GW / 2 - 0.55) * (R() < 0.5 ? 1 : -1);
      if (gk && Math.abs(gk.y - ty) < 1.2) ty = -ty;
      const id = p.c.ability.id;
      if ((id === 'fire' || id === 'curve') && p.cd <= 0 && R() < 0.7) this.useAbility(p, {});
      const power = gd < 9 ? 0.55 + R() * 0.3 : 0.75 + R() * 0.25;
      if (R() < 0.12 && gd < 16) this.performKick(p, 'lob', power, { dir: { x: goalX - p.x, y: ty - p.y } });
      else this.performKick(p, 'shot', power, { point: { x: goalX, y: ty } });
      return;
    }
    // التمرير عند الضغط أو لزميل أفضل
    const best = this.aiBestPass(p);
    if (best && ((pd < 2.6 && R() < 0.75) || (best.gain > 7 && R() < 0.3) || (pd < 4 && best.gain > 3 && R() < 0.35))) {
      const lob = best.blocked;
      this.performKick(p, lob ? 'lob' : 'pass', 0.6, { mate: best.m, dir: { x: best.m.x - p.x, y: best.m.y - p.y } });
      return;
    }
    // قدرات هجومية
    const id = p.c.ability.id;
    if (p.cd <= 0) {
      if (id === 'blink' && pd < 3 && R() < 0.4) { this.useAbility(p, { mx: dA, my: -p.y * 0.05 }); }
      else if (id === 'dash' && pd > 5 && gd > 18 && R() < 0.3) this.useAbility(p, {});
      else if ((id === 'bolt' || id === 'quake' || id === 'ice') && pd < 3 && R() < 0.3) this.useAbility(p, {});
    }
    // المراوغة
    let tx = goalX - dA * 6, ty = p.y * 0.55;
    if (press && pd < 4.5) {
      const side = (press.y - p.y) > 0 ? -1 : 1;
      ty = p.y + side * 4;
      tx = p.x + dA * 3;
    }
    ty = clamp(ty, -HW + 1.5, HW - 1.5);
    ai.tx = tx; ai.ty = ty;
    ai.sprint = pd > 3.5 && p.stamina > 0.3 && R() < 0.8;
  }

  aiBestPass(p) {
    const dA = this.attackDir(p.team);
    let best = null;
    for (const m of this.players) {
      if (m.team !== p.team || m === p || m.state !== STATE.NORMAL) continue;
      const d = hyp(m.x - p.x, m.y - p.y);
      if (d < 4 || d > 32) continue;
      const open = this.openness(m);
      // هل الخط مقطوع؟
      let blocked = false;
      for (const o of this.players) {
        if (o.team === p.team) continue;
        const t = clamp(((o.x - p.x) * (m.x - p.x) + (o.y - p.y) * (m.y - p.y)) / (d * d), 0, 1);
        const cx = p.x + (m.x - p.x) * t, cy = p.y + (m.y - p.y) * t;
        if (hyp(o.x - cx, o.y - cy) < 1.1 && t > 0.1 && t < 0.95) { blocked = true; break; }
      }
      const gain = (m.x - p.x) * dA;
      const s = open * 1.2 + gain * 0.5 - d * 0.08 - (blocked ? 4 : 0) - (this.isGK(m) ? 8 : 0);
      if (!best || s > best.s) best = { m, s, gain, blocked, open };
    }
    return best && best.open > 1.5 ? best : null;
  }

  aiHeader(p) {
    const dA = this.attackDir(p.team), goalX = dA * HL;
    const gd = hyp(goalX - p.x, p.y);
    if (gd < 16 && Math.sign(goalX - p.x) === dA) {
      this.performKick(p, 'shot', 0.8, { point: { x: goalX, y: (this.rand() - 0.5) * (GW - 1.2) } });
    } else {
      const best = this.aiBestPass(p);
      if (best) this.performKick(p, 'pass', 0.6, { mate: best.m });
      else this.performKick(p, 'shot', 0.5, { dir: { x: dA, y: -p.y * 0.05 } });
    }
  }

  aiDistribute(p) {
    const best = this.aiBestPass(p);
    if (best) this.performKick(p, best.blocked || hyp(best.m.x - p.x, best.m.y - p.y) > 18 ? 'lob' : 'pass', 0.6, { mate: best.m });
    else this.performKick(p, 'lob', 0.9, { dir: { x: this.attackDir(p.team), y: 0 } });
  }

  aiSetPiece(p) {
    const sp = this.sp;
    const best = this.aiBestPass(p);
    if (sp && sp.kind === 'corner') {
      const gx = Math.sign(sp.x) * HL;
      const mates = this.players.filter((m) => m.team === p.team && m !== p && !this.isGK(m));
      mates.sort((a, c) => hyp(a.x - gx, a.y) - hyp(c.x - gx, c.y));
      const m = mates[0];
      if (m) return this.performKick(p, 'lob', 0.7, { mate: m });
    }
    if (best) return this.performKick(p, sp && sp.kind === 'goalkick' && best.gain > 10 ? 'lob' : 'pass', 0.6, { mate: best.m });
    const nearest = this.players.filter((m) => m.team === p.team && m !== p).sort((a, c) => hyp(a.x - p.x, a.y - p.y) - hyp(c.x - p.x, c.y - p.y))[0];
    this.performKick(p, 'pass', 0.6, { mate: nearest });
  }

  aiKeeper(p) {
    const ai = p.ai;
    const b = this.ball;
    const dA = this.attackDir(p.team);
    const gx = -dA * HL;
    ai.sprint = false;
    if (b.owner === p.id) {
      if (!b.gk || p.heldT > 0.9 + this.rand() * 0.6) this.aiDistribute(p);
      ai.tx = p.x; ai.ty = p.y;
      return;
    }
    // خطر التسديد
    if (b.owner < 0 && (b.vx * -dA) > 3) {
      const t = (gx - b.x) / b.vx;
      if (t > 0 && t < 1.6) {
        const yAt = b.y + b.vy * t;
        if (Math.abs(yAt) < GW / 2 + 1.2) {
          ai.tx = gx + dA * 0.6; ai.ty = clamp(yAt, -GW / 2 + 0.3, GW / 2 - 0.3); ai.sprint = true;
          if (p.c.ability.id === 'wall' && p.cd <= 0 && t > 0.35 && this.rand() < 0.2) { p.face = Math.atan2(b.y - p.y, b.x - p.x); this.useAbility(p, {}); }
          const lat = ai.ty - p.y;
          if (Math.abs(lat) > 0.9 && t < 0.55 && p.tackleCD <= 0 && this.phase === PHASE.PLAY && this.rand() < 0.85 * this.diff.reach) {
            this.tackle(p, { mx: (ai.tx - p.x) * 0.3, my: Math.sign(lat) });
          }
          return;
        }
      }
    }
    const db = hyp(b.x - p.x, b.y - p.y);
    const bInBox = hyp(b.x - gx, b.y) < BOX_R - 0.5;
    const owner = b.owner >= 0 ? this.players[b.owner] : null;
    if (bInBox && !owner) {
      let closerOpp = false;
      for (const o of this.players) if (o.team !== p.team && hyp(b.x - o.x, b.y - o.y) < db - 0.5) closerOpp = true;
      if (!closerOpp || db < 3) { const ip = this.interceptPoint(p); ai.tx = ip.x; ai.ty = ip.y; ai.sprint = true; return; }
    }
    if (owner && owner.team !== p.team && bInBox && db < 3.5) {
      ai.tx = b.x; ai.ty = b.y; ai.sprint = true;
      if (db < 1.8 && p.tackleCD <= 0 && this.rand() < 0.3) this.tackle(p, { mx: (b.x - p.x) / db, my: (b.y - p.y) / db });
      return;
    }
    const vx = b.x - gx, vy = b.y, vl = hyp(vx, vy) || 1;
    const dd = clamp(vl * 0.14, 0.8, 3.2);
    ai.tx = gx + (vx / vl) * dd; ai.ty = clamp((vy / vl) * dd, -GW / 2 + 0.2, GW / 2 - 0.2);
    ai.sprint = hyp(ai.tx - p.x, ai.ty - p.y) > 2;
  }

  // ---------- اللقطة ----------
  snapshot() {
    const b = this.ball;
    return {
      t: Math.round(this.time * 1000) / 1000,
      ph: this.phase,
      pt: r2(this.phaseT),
      tl: r1(this.timeLeft),
      sc: this.score,
      gd: this.golden ? 1 : 0,
      sp: this.sp ? this.sp.taker : -1,
      spk: this.sp ? this.sp.kind : (this.pendingSP ? this.pendingSP.kind : ''),
      b: [r2(b.x), r2(b.y), r2(b.z), r2(b.vx), r2(b.vy), r2(b.vz), b.owner, b.fx ? FX_CODE[b.fx] : 0, b.gk ? 1 : 0, r2(b.spin)],
      p: this.players.map((p) => [
        r2(p.x), r2(p.y), r2(p.vx), r2(p.vy), r2(p.face), p.state, p.emote,
        r2(p.stamina), r2(p.cd / p.c.ability.cd),
        (p.buff.fire > 0 ? 1 : 0) | (p.buff.dash > 0 ? 2 : 0) | (p.buff.curve > 0 ? 4 : 0) | (p.buff.magnet > 0 ? 8 : 0) | (p.buff.slow > 0 ? 16 : 0) | (p.sprinting ? 32 : 0),
        r2(Math.max(p.hold.shoot, p.hold.lob) / 0.9),
      ]),
      z: this.zones.map((z) => (z.k === 'ice' ? ['ice', r2(z.x), r2(z.y), z.r, 0, r2(z.t / z.T), z.team] : ['wall', r2(z.x), r2(z.y), z.len, r2(z.a), r2(z.t / z.T), z.team])),
    };
  }

  statsTable() {
    return this.players.map((p) => ({ id: p.id, name: p.name, team: p.team, char: p.char, human: p.human, ...p.st }));
  }
}
