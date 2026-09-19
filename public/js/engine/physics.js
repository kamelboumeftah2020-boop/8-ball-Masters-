// Lightweight hand-rolled 2D billiard physics for a single isolated table.
// Each player only ever sees their OWN table (per spec) - so this never needs to be
// synced with the opponent's simulation, only the pot-count / time state is networked.

export const TABLE_W = 300;
export const TABLE_H = 540;
export const BALL_R = 11;
export const POCKET_R = 20;
export const CUSHION = 16;

// Tuning. Distances are table units (the playfield is 268 x 508 of them).
// Rolling drag plus an extra term that fades in as a ball slows, so shots stay lively
// but the table settles fast - important when the whole match clock is 20 seconds.
const ROLL_FRICTION = 400;
const SETTLE_FRICTION = 300;
const SETTLE_BELOW = 260;
const STOP_SPEED = 7;           // below this a ball is parked
const WALL_RESTITUTION = 0.80;  // cushions eat ~a third of the energy
const BALL_RESTITUTION = 0.96;
const CUE_FOLLOW = 0.16;        // rolling cue ball keeps a little forward momentum
const MAX_SHOT_SPEED = 1100;
const SUBSTEP = 1 / 240;        // fixed timestep: fast balls can't tunnel through each other

export function pocketPositions() {
  const x0 = CUSHION, x1 = TABLE_W - CUSHION;
  const y0 = CUSHION, y1 = TABLE_H - CUSHION, ym = TABLE_H / 2;
  return [
    { x: x0, y: y0 }, { x: x1, y: y0 },
    { x: x0, y: ym }, { x: x1, y: ym },
    { x: x0, y: y1 }, { x: x1, y: y1 },
  ];
}

export class Ball {
  constructor(number, x, y, isCue = false) {
    this.number = number;
    this.x = x; this.y = y;
    this.vx = 0; this.vy = 0;
    this.r = BALL_R;
    this.isCue = isCue;
    this.active = true;
  }
  get speed() { return Math.hypot(this.vx, this.vy); }
}

export class Table {
  constructor(onPot, onScratch, cueBonuses = {}) {
    this.balls = [];
    this.pockets = pocketPositions();
    this.onPot = onPot;
    this.onScratch = onScratch;
    // The equipped cue's bonuses give a small, honest edge: a slightly more
    // forgiving pocket mouth and a shot-speed multiplier.
    this.pocketForgiveness = 1 + (cueBonuses.aimBonus || 0) / 100 * 0.15;
    this.powerMult = 1 + (cueBonuses.powerBonus || 0) / 100 * 0.3;
    this.cueRespawnAt = 0;
    this.reset();
  }

  reset() {
    this.balls = [];
    this.cue = new Ball(0, TABLE_W / 2, TABLE_H - 90, true);
    this.balls.push(this.cue);
    this._scatterObjectBalls(7);
  }

  _scatterObjectBalls(count) {
    const margin = CUSHION + BALL_R + 6;
    const avoidTop = CUSHION + 40;
    const placed = [];
    let attempts = 0;
    while (placed.length < count && attempts < 4000) {
      attempts++;
      const p = {
        x: margin + Math.random() * (TABLE_W - margin * 2),
        y: avoidTop + Math.random() * (TABLE_H * 0.62 - avoidTop),
      };
      if (!this.pockets.every(pk => dist(p, pk) > POCKET_R + BALL_R + 4)) continue;
      if (dist(p, this.cue) < BALL_R * 3) continue;
      if (placed.some(q => dist(p, q) < BALL_R * 2.6)) continue;
      placed.push(p);
    }
    placed.forEach((p, i) => this.balls.push(new Ball(i + 1, p.x, p.y)));
  }

  get activeObjectBalls() {
    return this.balls.filter(b => !b.isCue && b.active);
  }

  isAllStopped() {
    if (this.cueRespawnAt) return false;
    return this.balls.every(b => !b.active || b.speed === 0);
  }

  shootCue(angle, power01) {
    if (!this.cue.active || !this.isAllStopped()) return false;
    const p = Math.max(0, Math.min(1, power01));
    // slightly curved response so soft shots are easy to feather
    const speed = (0.08 + 0.92 * Math.pow(p, 1.4)) * MAX_SHOT_SPEED * this.powerMult;
    this.cue.vx = Math.cos(angle) * speed;
    this.cue.vy = Math.sin(angle) * speed;
    return true;
  }

  step(dt) {
    if (this.cueRespawnAt && Date.now() >= this.cueRespawnAt) {
      this.cueRespawnAt = 0;
      this.cue.active = true;
      this.cue.x = TABLE_W / 2;
      this.cue.y = TABLE_H - 90;
      this.cue.vx = this.cue.vy = 0;
    }
    let remaining = Math.min(dt, 0.05);
    while (remaining > 0) {
      const h = Math.min(SUBSTEP, remaining);
      this._substep(h);
      remaining -= h;
    }
  }

  _substep(h) {
    for (const b of this.balls) {
      if (!b.active) continue;
      const sp = b.speed;
      if (sp > 0) {
        const decel = ROLL_FRICTION + SETTLE_FRICTION * (1 - Math.min(1, sp / SETTLE_BELOW));
        const next = sp - decel * h;
        if (next <= STOP_SPEED) {
          b.vx = b.vy = 0;
        } else {
          const k = next / sp;
          b.vx *= k; b.vy *= k;
          b.x += b.vx * h;
          b.y += b.vy * h;
        }
      }
      this._wallCollide(b);
    }
    this._ballCollisions();
    this._checkPockets();
  }

  _wallCollide(b) {
    const minX = CUSHION + b.r, maxX = TABLE_W - CUSHION - b.r;
    const minY = CUSHION + b.r, maxY = TABLE_H - CUSHION - b.r;
    if (b.x < minX) { b.x = minX; b.vx = Math.abs(b.vx) * WALL_RESTITUTION; }
    if (b.x > maxX) { b.x = maxX; b.vx = -Math.abs(b.vx) * WALL_RESTITUTION; }
    if (b.y < minY) { b.y = minY; b.vy = Math.abs(b.vy) * WALL_RESTITUTION; }
    if (b.y > maxY) { b.y = maxY; b.vy = -Math.abs(b.vy) * WALL_RESTITUTION; }
  }

  _ballCollisions() {
    const balls = this.balls.filter(b => b.active);
    for (let i = 0; i < balls.length; i++) {
      for (let j = i + 1; j < balls.length; j++) {
        const a = balls[i], b = balls[j];
        const dx = b.x - a.x, dy = b.y - a.y;
        const d = Math.hypot(dx, dy) || 0.0001;
        const minD = a.r + b.r;
        if (d >= minD) continue;

        const nx = dx / d, ny = dy / d;

        // separate first so balls never end up resting inside each other
        const overlap = (minD - d) / 2 + 0.01;
        a.x -= nx * overlap; a.y -= ny * overlap;
        b.x += nx * overlap; b.y += ny * overlap;

        const rvx = b.vx - a.vx, rvy = b.vy - a.vy;
        const along = rvx * nx + rvy * ny;
        if (along >= 0) continue; // already separating

        const cueSide = a.isCue ? a : (b.isCue ? b : null);
        const pre = cueSide ? { vx: cueSide.vx, vy: cueSide.vy, sp: cueSide.speed } : null;

        const impulse = -(1 + BALL_RESTITUTION) * along / 2;
        a.vx -= impulse * nx; a.vy -= impulse * ny;
        b.vx += impulse * nx; b.vy += impulse * ny;

        // A rolling cue ball doesn't stop dead on a full hit - it carries forward.
        if (pre && pre.sp > 1) {
          cueSide.vx += (pre.vx / pre.sp) * pre.sp * CUE_FOLLOW;
          cueSide.vy += (pre.vy / pre.sp) * pre.sp * CUE_FOLLOW;
        }
      }
    }
  }

  _checkPockets() {
    const mouth = (POCKET_R - 2) * this.pocketForgiveness;
    for (const b of this.balls) {
      if (!b.active) continue;
      for (const p of this.pockets) {
        if (dist(b, p) >= mouth) continue;
        b.active = false;
        b.vx = b.vy = 0;
        if (b.isCue) {
          this.cueRespawnAt = Date.now() + 450;
          this.onScratch?.();
        } else {
          this.onPot?.(b.number);
        }
        break;
      }
    }
  }
}

function dist(a, b) { return Math.hypot(a.x - b.x, a.y - b.y); }
