import { TABLE_W, TABLE_H, BALL_R, POCKET_R, CUSHION } from './physics.js';

export function sizeCanvas(canvas, container) {
  const dpr = window.devicePixelRatio || 1;
  const availW = container.clientWidth - 16;
  const availH = container.clientHeight - 16;
  const scale = Math.min(availW / TABLE_W, availH / TABLE_H);
  const cssW = TABLE_W * scale, cssH = TABLE_H * scale;
  canvas.style.width = `${cssW}px`;
  canvas.style.height = `${cssH}px`;
  canvas.width = Math.round(cssW * dpr);
  canvas.height = Math.round(cssH * dpr);
  const ctx = canvas.getContext('2d');
  ctx.setTransform(dpr * scale, 0, 0, dpr * scale, 0, 0);
  return { scale, cssW, cssH };
}

export function drawTable(ctx, table, opts = {}) {
  const { theme, aimAngle = -Math.PI / 2, power = 0, canShoot = true } = opts;
  ctx.clearRect(0, 0, TABLE_W, TABLE_H);

  // Rail
  ctx.fillStyle = theme?.rail || '#3a2e22';
  roundRect(ctx, 0, 0, TABLE_W, TABLE_H, 14);
  ctx.fill();

  // Felt
  ctx.fillStyle = theme?.felt || '#1a5c33';
  roundRect(ctx, CUSHION - 6, CUSHION - 6, TABLE_W - (CUSHION - 6) * 2, TABLE_H - (CUSHION - 6) * 2, 6);
  ctx.fill();

  // subtle felt vignette
  const grad = ctx.createRadialGradient(TABLE_W / 2, TABLE_H / 2, 20, TABLE_W / 2, TABLE_H / 2, TABLE_H * 0.7);
  grad.addColorStop(0, 'rgba(255,255,255,0.05)');
  grad.addColorStop(1, 'rgba(0,0,0,0.25)');
  ctx.fillStyle = grad;
  roundRect(ctx, CUSHION - 6, CUSHION - 6, TABLE_W - (CUSHION - 6) * 2, TABLE_H - (CUSHION - 6) * 2, 6);
  ctx.fill();

  // Pockets
  for (const p of table.pockets) {
    ctx.beginPath();
    ctx.arc(p.x, p.y, POCKET_R, 0, Math.PI * 2);
    ctx.fillStyle = '#050505';
    ctx.fill();
    ctx.beginPath();
    ctx.arc(p.x, p.y, POCKET_R, 0, Math.PI * 2);
    ctx.strokeStyle = theme?.accent || '#c9a24b';
    ctx.lineWidth = 1.5;
    ctx.globalAlpha = 0.6;
    ctx.stroke();
    ctx.globalAlpha = 1;
  }

  // Aim assist line from cue ball
  if (canShoot && table.cue.active && table.isAllStopped()) {
    const cue = table.cue;
    const len = 260;
    const ex = cue.x + Math.cos(aimAngle) * len;
    const ey = cue.y + Math.sin(aimAngle) * len;
    ctx.save();
    ctx.setLineDash([6, 7]);
    ctx.strokeStyle = `rgba(255,212,71,${0.35 + power * 0.5})`;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(cue.x, cue.y);
    ctx.lineTo(clamp(ex, CUSHION, TABLE_W - CUSHION), clamp(ey, CUSHION, TABLE_H - CUSHION));
    ctx.stroke();
    ctx.restore();
  }

  // Balls
  for (const b of table.balls) {
    if (!b.active) continue;
    drawBall(ctx, b);
  }
}

function drawBall(ctx, b) {
  ctx.save();
  ctx.beginPath();
  ctx.arc(b.x, b.y + 1.5, b.r, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(0,0,0,0.25)';
  ctx.fill();

  ctx.beginPath();
  ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2);
  const g = ctx.createRadialGradient(b.x - b.r * 0.35, b.y - b.r * 0.35, 1, b.x, b.y, b.r);
  g.addColorStop(0, '#ffffff');
  g.addColorStop(1, '#dcdad2');
  ctx.fillStyle = g;
  ctx.fill();
  ctx.lineWidth = 1;
  ctx.strokeStyle = 'rgba(0,0,0,0.25)';
  ctx.stroke();

  if (!b.isCue) {
    ctx.fillStyle = '#161616';
    ctx.font = `bold ${b.r}px Segoe UI, sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(b.number), b.x, b.y + 0.5);
  }
  ctx.restore();
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, v)); }
