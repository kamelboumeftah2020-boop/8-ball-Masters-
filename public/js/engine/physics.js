// Lightweight hand-rolled 2D billiard physics for a single isolated table.
// Each player only ever sees their OWN table (per spec) - so this never needs to be
// synced with the opponent's simulation, only the pot-count / time state is networked.

export const TABLE_W = 300;
export const TABLE_H = 540;
export const BALL_R = 11;
export const POCKET_R = 20;
export const CUSHION = 16;

const FRICTION = 620; // px/s^2 deceleration (rolling resistance)
const WALL_RESTITUTION = 0.86;
const BALL_RESTITUTION = 0.98;
const MIN_SPEED = 4; // below this, a ball is considered stopped
const MAX_SHOT_SPEED = 900;

export function pocketPositions() {
  const x0 = CUSHION, x1 = TABLE_W - CUSHION, xm = TABLE_W / 2;
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
    this.active = true; // false once potted / removed
  }
  get speed() { return Math.hypot(this.vx, this.vy); }
}

export class Table {
  constructor(onPot, onScratch, cueBonuses = {}) {
    this.balls = [];
    this.pockets = pocketPositions();
    this.onPot = onPot; // (ballNumber) => void
    this.onScratch = onScratch; // () => void
    // Equipped cue's aim/power bonuses give a small, honest mechanical edge:
    // a slightly more forgiving pocket radius, and a shot-speed multiplier.
    this.pocketForgiveness = 1 + (cueBonuses.aimBonus || 0) / 100 * 0.15;
    this.powerMult = 1 + (cueBonuses.powerBonus || 0) / 100 * 0.3;
    this.reset();
  }

  reset() {
    this.balls = [];
    const cueStart = { x: TABLE_W / 2, y: TABLE_H - 90 };
    this.cue = new Ball(0, cueStart.x, cueStart.y, true);
    this.balls.push(this.cue);
    this._scatterObjectBalls(7);
  }

  _scatterObjectBalls(count) {
    const margin = CUSHION + BALL_R + 6;
    const avoidTop = CUSHION + 40; // keep clear of top pockets slightly
    const placed = [];
    let n = 1;
    let attempts = 0;
    while (placed.length < count && attempts < 4000) {
      attempts++;
      const x = margin + Math.random() * (TABLE_W - margin * 2);
      const y = avoidTop + Math.random() * (TABLE_H * 0.62 - avoidTop);
      const candidate = { x, y };
      let ok = this._farFromPockets(candidate, POCKET_R + BALL_R + 4)
        && this._farFrom(candidate, this.cue, BALL_R * 3);
      for (const p of placed) {
        if (dist(candidate, p) < BALL_R * 2.6) { ok = false; break; }
      }
      if (ok) placed.push(candidate);
    }
    for (const p of placed) {
      this.balls.push(new Ball(n++, p.x, p.y));
    }
  }

  _farFromPockets(p, minDist) {
    return this.pockets.every(pk => dist(p, pk) > minDist);
  }
  _farFrom(p, ball, minDist) {
    return dist(p, ball) > minDist;
  }

  get activeObjectBalls() {
    return this.balls.filter(b => !b.isCue && b.active);
  }

  isAllStopped() {
    return this.balls.every(b => !b.active || b.speed < MIN_SPEED);
  }

  shootCue(angle, power01) {
    if (!this.cue.active) return false;
    if (!this.isAllStopped()) return false;
    const speed = Math.max(0.12, Math.min(1, power01)) * MAX_SHOT_SPEED * this.powerMult;
    this.cue.vx = Math.cos(angle) * speed;
    this.cue.vy = Math.sin(angle) * speed;
    return true;
  }

  step(dt) {
    for (const b of this.balls) {
      if (!b.active) continue;
      const sp = b.speed;
      if (sp > 0) {
        const decel = FRICTION * dt;
        const newSp = Math.max(0, sp - decel);
        const scale = sp > 0 ? newSp / sp : 0;
        b.vx *= scale; b.vy *= scale;
        if (newSp < MIN_SPEED && newSp > 0) { b.vx = 0; b.vy = 0; }
      }
      b.x += b.vx * dt;
      b.y += b.vy * dt;
      this._wallCollide(b);
    }
    this._ballCollisions();
    this._checkPockets();
  }

  _wallCollide(b) {
    const minX = CUSHION + b.r, maxX = TABLE_W - CUSHION - b.r;
    const minY = CUSHION + b.r, maxY = TABLE_H - CUSHION - b.r;
    if (b.x < minX) { b.x = minX; b.vx = -b.vx * WALL_RESTITUTION; }
    if (b.x > maxX) { b.x = maxX; b.vx = -b.vx * WALL_RESTITUTION; }
    if (b.y < minY) { b.y = minY; b.vy = -b.vy * WALL_RESTITUTION; }
    if (b.y > maxY) { b.y = maxY; b.vy = -b.vy * WALL_RESTITUTION; }
  }

  _ballCollisions() {
    const balls = this.balls.filter(b => b.active);
    for (let i = 0; i < balls.length; i++) {
      for (let j = i + 1; j < balls.length; j++) {
        const a = balls[i], b = balls[j];
        const dx = b.x - a.x, dy = b.y - a.y;
        const d = Math.hypot(dx, dy) || 0.001;
        const minD = a.r + b.r;
        if (d < minD) {
          const overlap = (minD - d) / 2;
          const nx = dx / d, ny = dy / d;
          a.x -= nx * overlap; a.y -= ny * overlap;
          b.x += nx * overlap; b.y += ny * overlap;

          const rvx = b.vx - a.vx, rvy = b.vy - a.vy;
          const velAlongNormal = rvx * nx + rvy * ny;
          if (velAlongNormal < 0) {
            const impulse = -(1 + BALL_RESTITUTION) * velAlongNormal / 2;
            a.vx -= impulse * nx; a.vy -= impulse * ny;
            b.vx += impulse * nx; b.vy += impulse * ny;
          }
        }
      }
    }
  }

  _checkPockets() {
    for (const b of this.balls) {
      if (!b.active) continue;
      for (const p of this.pockets) {
        if (dist(b, p) < (POCKET_R - 2) * this.pocketForgiveness) {
          b.active = false;
          b.vx = 0; b.vy = 0;
          if (b.isCue) {
            this.onScratch?.();
            setTimeout(() => {
              this.cue.active = true;
              this.cue.x = TABLE_W / 2;
              this.cue.y = TABLE_H - 90;
              this.cue.vx = 0; this.cue.vy = 0;
            }, 500);
          } else {
            this.onPot?.(b.number);
          }
          break;
        }
      }
    }
  }
}

function dist(a, b) { return Math.hypot(a.x - b.x, a.y - b.y); }
