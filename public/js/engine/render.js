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

function hexToRgb(hex) {
  const h = hex.replace('#', '');
  const n = h.length === 3 ? h.split('').map(c => c + c).join('') : h;
  const num = parseInt(n, 16);
  return { r: (num >> 16) & 255, g: (num >> 8) & 255, b: num & 255 };
}
function shade(hex, amt) {
  const { r, g, b } = hexToRgb(hex);
  const f = (c) => Math.max(0, Math.min(255, Math.round(c + amt)));
  return `rgb(${f(r)}, ${f(g)}, ${f(b)})`;
}
function rgba(hex, a) {
  const { r, g, b } = hexToRgb(hex);
  return `rgba(${r}, ${g}, ${b}, ${a})`;
}

export function drawTable(ctx, table, opts = {}) {
  const { theme, aimAngle = -Math.PI / 2, power = 0, canShoot = true } = opts;
  const felt = theme?.felt || '#0f3d24';
  const rail = theme?.rail || '#3a2e22';
  const accent = theme?.accent || '#c9a24b';

  ctx.clearRect(0, 0, TABLE_W, TABLE_H);
  ctx.save();

  // Outer drop shadow so the table "sits" above the background
  ctx.save();
  ctx.shadowColor = 'rgba(0,0,0,0.65)';
  ctx.shadowBlur = 26;
  ctx.shadowOffsetY = 10;
  roundRect(ctx, 2, 2, TABLE_W - 4, TABLE_H - 4, 16);
  ctx.fillStyle = '#000';
  ctx.fill();
  ctx.restore();

  // Wooden rail with grain + radial sheen
  const railGrad = ctx.createLinearGradient(0, 0, TABLE_W, TABLE_H);
  railGrad.addColorStop(0, shade(rail, 26));
  railGrad.addColorStop(0.5, rail);
  railGrad.addColorStop(1, shade(rail, -22));
  roundRect(ctx, 0, 0, TABLE_W, TABLE_H, 16);
  ctx.fillStyle = railGrad;
  ctx.fill();
  drawWoodGrain(ctx, rail);

  // Bevel highlight around the rail's outer edge
  roundRect(ctx, 1, 1, TABLE_W - 2, TABLE_H - 2, 15);
  ctx.strokeStyle = rgba('#ffffff', 0.10);
  ctx.lineWidth = 1;
  ctx.stroke();

  // Decorative inlay line + accent-colored piping near the cushion edge
  const inlayPad = CUSHION - 9;
  roundRect(ctx, inlayPad, inlayPad, TABLE_W - inlayPad * 2, TABLE_H - inlayPad * 2, 10);
  ctx.strokeStyle = rgba(accent, 0.55);
  ctx.lineWidth = 1.4;
  ctx.stroke();

  // Felt bed
  const feltX = CUSHION - 6, feltY = CUSHION - 6;
  const feltW = TABLE_W - feltX * 2, feltH = TABLE_H - feltY * 2;
  roundRect(ctx, feltX, feltY, feltW, feltH, 7);
  ctx.save();
  ctx.clip();
  const feltGrad = ctx.createLinearGradient(0, feltY, 0, feltY + feltH);
  feltGrad.addColorStop(0, shade(felt, 20));
  feltGrad.addColorStop(0.45, felt);
  feltGrad.addColorStop(1, shade(felt, -18));
  ctx.fillStyle = feltGrad;
  ctx.fillRect(feltX, feltY, feltW, feltH);

  // soft directional light pooling near the top
  const light = ctx.createRadialGradient(TABLE_W / 2, feltY + feltH * 0.18, 10, TABLE_W / 2, feltY + feltH * 0.18, feltH * 0.65);
  light.addColorStop(0, 'rgba(255,255,255,0.10)');
  light.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = light;
  ctx.fillRect(feltX, feltY, feltW, feltH);

  // vignette
  const vin = ctx.createRadialGradient(TABLE_W / 2, TABLE_H / 2, feltH * 0.25, TABLE_W / 2, TABLE_H / 2, feltH * 0.75);
  vin.addColorStop(0, 'rgba(0,0,0,0)');
  vin.addColorStop(1, 'rgba(0,0,0,0.35)');
  ctx.fillStyle = vin;
  ctx.fillRect(feltX, feltY, feltW, feltH);

  // subtle felt weave
  ctx.globalAlpha = 0.05;
  ctx.strokeStyle = '#000';
  ctx.lineWidth = 1;
  for (let y = feltY; y < feltY + feltH; y += 4) {
    ctx.beginPath(); ctx.moveTo(feltX, y); ctx.lineTo(feltX + feltW, y); ctx.stroke();
  }
  ctx.globalAlpha = 1;

  // center spot markers (cosmetic, like real tables)
  ctx.fillStyle = rgba(accent, 0.35);
  [0.25, 0.5, 0.75].forEach((f) => {
    ctx.beginPath(); ctx.arc(TABLE_W / 2, feltY + feltH * f, 1.6, 0, Math.PI * 2); ctx.fill();
  });
  ctx.restore(); // clip

  // Cushion inner shadow (gives the felt a recessed look against the rail)
  roundRect(ctx, feltX, feltY, feltW, feltH, 7);
  ctx.save();
  ctx.clip();
  ctx.shadowColor = 'rgba(0,0,0,0.55)';
  ctx.shadowBlur = 10;
  ctx.lineWidth = 14;
  ctx.strokeStyle = 'rgba(0,0,0,0.001)';
  ctx.strokeRect(feltX, feltY, feltW, feltH);
  ctx.restore();

  // Pockets
  for (const p of table.pockets) drawPocket(ctx, p, accent);

  // Aim assist: dashed guide + ghost ball at first predicted contact
  if (canShoot && table.cue.active && table.isAllStopped()) {
    drawAimGuide(ctx, table, aimAngle, power, accent);
  }

  // Balls, sorted so lower ones render with correct shadow overlap (painter's order by y)
  const balls = table.balls.filter(b => b.active).sort((a, b) => a.y - b.y);
  for (const b of balls) drawBall(ctx, b);

  ctx.restore();
}

function drawWoodGrain(ctx, rail) {
  ctx.save();
  roundRect(ctx, 0, 0, TABLE_W, TABLE_H, 16);
  ctx.clip();
  ctx.globalAlpha = 0.10;
  ctx.strokeStyle = shade(rail, -40);
  ctx.lineWidth = 1;
  for (let i = -TABLE_H; i < TABLE_W + TABLE_H; i += 6) {
    ctx.beginPath();
    ctx.moveTo(i, 0);
    ctx.lineTo(i + TABLE_H * 0.15, TABLE_H);
    ctx.stroke();
  }
  ctx.globalAlpha = 1;
  ctx.restore();
}

function drawPocket(ctx, p, accent) {
  // cast shadow onto felt
  ctx.save();
  ctx.beginPath();
  ctx.arc(p.x, p.y, POCKET_R + 4, 0, Math.PI * 2);
  const cast = ctx.createRadialGradient(p.x, p.y, POCKET_R - 4, p.x, p.y, POCKET_R + 4);
  cast.addColorStop(0, 'rgba(0,0,0,0)');
  cast.addColorStop(1, 'rgba(0,0,0,0.35)');
  ctx.fillStyle = cast;
  ctx.fill();
  ctx.restore();

  // hole
  const hole = ctx.createRadialGradient(p.x - POCKET_R * 0.2, p.y - POCKET_R * 0.2, 1, p.x, p.y, POCKET_R);
  hole.addColorStop(0, '#2a2a2a');
  hole.addColorStop(0.35, '#0c0c0c');
  hole.addColorStop(1, '#000000');
  ctx.beginPath();
  ctx.arc(p.x, p.y, POCKET_R, 0, Math.PI * 2);
  ctx.fillStyle = hole;
  ctx.fill();

  // metallic rim
  ctx.beginPath();
  ctx.arc(p.x, p.y, POCKET_R - 1, 0, Math.PI * 2);
  const rim = ctx.createLinearGradient(p.x - POCKET_R, p.y - POCKET_R, p.x + POCKET_R, p.y + POCKET_R);
  rim.addColorStop(0, rgba(accent, 0.9));
  rim.addColorStop(0.5, rgba('#ffffff', 0.25));
  rim.addColorStop(1, rgba(accent, 0.5));
  ctx.strokeStyle = rim;
  ctx.lineWidth = 1.6;
  ctx.stroke();
}

function drawAimGuide(ctx, table, angle, power, accent) {
  const cue = table.cue;
  const dir = { x: Math.cos(angle), y: Math.sin(angle) };
  const balls = table.activeObjectBalls;

  // find nearest ball intersecting the aim ray (simple point-to-line distance test)
  let hit = null, hitDist = Infinity;
  for (const b of balls) {
    const toB = { x: b.x - cue.x, y: b.y - cue.y };
    const proj = toB.x * dir.x + toB.y * dir.y;
    if (proj <= 0) continue;
    const closestX = cue.x + dir.x * proj, closestY = cue.y + dir.y * proj;
    const dPerp = Math.hypot(b.x - closestX, b.y - closestY);
    if (dPerp < BALL_R * 2 && proj < hitDist) {
      const back = Math.sqrt(Math.max(0, (BALL_R * 2) ** 2 - dPerp ** 2));
      hitDist = proj - back;
      hit = { x: cue.x + dir.x * hitDist, y: cue.y + dir.y * hitDist, target: b };
    }
  }

  const maxLen = 320;
  const endLen = hit ? hitDist : maxLen;
  const rawEndX = cue.x + dir.x * endLen, rawEndY = cue.y + dir.y * endLen;
  const ex = clamp(rawEndX, CUSHION + BALL_R, TABLE_W - CUSHION - BALL_R);
  const ey = clamp(rawEndY, CUSHION + BALL_R, TABLE_H - CUSHION - BALL_R);

  ctx.save();
  ctx.setLineDash([5, 6]);
  ctx.lineCap = 'round';
  ctx.strokeStyle = rgba(accent, 0.3 + power * 0.5);
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.moveTo(cue.x + dir.x * (BALL_R + 2), cue.y + dir.y * (BALL_R + 2));
  ctx.lineTo(ex, ey);
  ctx.stroke();
  ctx.restore();

  if (hit) {
    ctx.save();
    ctx.beginPath();
    ctx.arc(hit.x, hit.y, BALL_R, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(255,255,255,0.75)';
    ctx.lineWidth = 1.4;
    ctx.setLineDash([2, 3]);
    ctx.stroke();
    ctx.fillStyle = 'rgba(255,255,255,0.12)';
    ctx.fill();
    ctx.restore();
  }
}

function drawBall(ctx, b) {
  ctx.save();

  // contact shadow
  ctx.beginPath();
  ctx.ellipse(b.x, b.y + b.r * 0.55, b.r * 0.92, b.r * 0.45, 0, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(0,0,0,0.38)';
  ctx.filter = 'blur(1.5px)';
  ctx.fill();
  ctx.filter = 'none';

  // base sphere
  ctx.beginPath();
  ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2);
  const base = ctx.createRadialGradient(
    b.x - b.r * 0.38, b.y - b.r * 0.42, b.r * 0.12,
    b.x, b.y, b.r * 1.05,
  );
  if (b.isCue) {
    base.addColorStop(0, '#ffffff');
    base.addColorStop(0.55, '#f3f2ee');
    base.addColorStop(1, '#cfccc2');
  } else {
    base.addColorStop(0, '#ffffff');
    base.addColorStop(0.5, '#f0eee7');
    base.addColorStop(0.85, '#d9d6cb');
    base.addColorStop(1, '#bdb9ac');
  }
  ctx.fillStyle = base;
  ctx.fill();

  // rim shading (bottom-right darker edge for volume)
  ctx.save();
  ctx.clip();
  const rim = ctx.createRadialGradient(b.x + b.r * 0.4, b.y + b.r * 0.5, b.r * 0.3, b.x, b.y, b.r * 1.15);
  rim.addColorStop(0, 'rgba(0,0,0,0)');
  rim.addColorStop(1, 'rgba(0,0,0,0.22)');
  ctx.fillStyle = rim;
  ctx.fillRect(b.x - b.r, b.y - b.r, b.r * 2, b.r * 2);
  ctx.restore();

  // number badge
  if (!b.isCue) {
    ctx.beginPath();
    ctx.arc(b.x, b.y, b.r * 0.56, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255,255,255,0.9)';
    ctx.fill();
    ctx.lineWidth = 0.6;
    ctx.strokeStyle = 'rgba(0,0,0,0.15)';
    ctx.stroke();

    ctx.fillStyle = '#161616';
    ctx.font = `bold ${b.r * 0.85}px 'Segoe UI', Arial, sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(b.number), b.x, b.y + b.r * 0.04);
  }

  // primary specular highlight
  ctx.beginPath();
  ctx.ellipse(b.x - b.r * 0.38, b.y - b.r * 0.42, b.r * 0.32, b.r * 0.2, -0.5, 0, Math.PI * 2);
  const spec = ctx.createRadialGradient(b.x - b.r * 0.38, b.y - b.r * 0.42, 0, b.x - b.r * 0.38, b.y - b.r * 0.42, b.r * 0.35);
  spec.addColorStop(0, 'rgba(255,255,255,0.95)');
  spec.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = spec;
  ctx.fill();

  // secondary faint bounce-light on the opposite edge
  ctx.beginPath();
  ctx.arc(b.x + b.r * 0.5, b.y + b.r * 0.15, b.r * 0.14, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(255,255,255,0.12)';
  ctx.fill();

  // crisp outline
  ctx.beginPath();
  ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2);
  ctx.lineWidth = 0.75;
  ctx.strokeStyle = 'rgba(0,0,0,0.28)';
  ctx.stroke();

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
