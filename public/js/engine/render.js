import { TABLE_W, TABLE_H, BALL_R, POCKET_R, CUSHION, applyRot } from './physics.js';

// The canvas is bigger than the physics area so the wooden frame can sit *outside*
// the playfield. All drawing happens in physics coordinates: the context is
// translated by FRAME, so (0,0) is still the playfield's top-left corner.
export const FRAME = 17;
// Transparent margin outside the frame so the cue stick can reach past the table,
// the way it does in a real overhead shot.
export const BLEED = 32;
const OFF = FRAME + BLEED;
export const CANVAS_W = TABLE_W + OFF * 2;
export const CANVAS_H = TABLE_H + OFF * 2;

const CUSH_D = 9;                       // cushion depth (rail edge -> nose)
const PLAY_L = CUSHION, PLAY_R = TABLE_W - CUSHION;
const PLAY_T = CUSHION, PLAY_B = TABLE_H - CUSHION;
const RAIL_L = PLAY_L - CUSH_D, RAIL_R = PLAY_R + CUSH_D;
const RAIL_T = PLAY_T - CUSH_D, RAIL_B = PLAY_B + CUSH_D;
const JAW_NOSE = 21, JAW_BASE = 11;     // pocket jaw cut-back at nose / base
const MID_Y = TABLE_H / 2;

// Standard pool solids 1-7.
const BALL_COLORS = {
  1: '#f2c200', 2: '#1f5fbf', 3: '#d62828', 4: '#6a2d9b',
  5: '#f07b12', 6: '#0f8f45', 7: '#8c1f1f',
};

const CUE_STYLES = {
  wood: { shaft: '#e3b678', shaftDark: '#a0703a', butt: '#5a3418', accent: '#c9a24b' },
  flag: { shaft: '#e6c48a', shaftDark: '#a3803f', butt: '#22303f', accent: '#dfe4ea' },
  dragon: { shaft: '#f0c090', shaftDark: '#a66a35', butt: '#7a1616', accent: '#ff7a2f' },
  galaxy: { shaft: '#cfd4ff', shaftDark: '#6f74b5', butt: '#2a1b55', accent: '#a56bff' },
  laser: { shaft: '#d8f6ff', shaftDark: '#5aa7c0', butt: '#08313d', accent: '#28e0ff' },
  gold: { shaft: '#ffe9a8', shaftDark: '#c9a227', butt: '#6b4c06', accent: '#ffd447' },
  special: { shaft: '#e8e2d8', shaftDark: '#8f8578', butt: '#2b2b33', accent: '#b060ff' },
};
export function cueStyleFor(category) { return CUE_STYLES[category] || CUE_STYLES.wood; }

export function sizeCanvas(canvas, container) {
  const dpr = window.devicePixelRatio || 1;
  const availW = container.clientWidth - 12;
  const availH = container.clientHeight - 12;
  const scale = Math.min(availW / CANVAS_W, availH / CANVAS_H);
  const cssW = CANVAS_W * scale, cssH = CANVAS_H * scale;
  canvas.style.width = `${cssW}px`;
  canvas.style.height = `${cssH}px`;
  canvas.width = Math.round(cssW * dpr);
  canvas.height = Math.round(cssH * dpr);
  const px = dpr * scale;
  const ctx = canvas.getContext('2d');
  ctx.setTransform(px, 0, 0, px, px * OFF, px * OFF);
  canvas.__pxScale = px;
  return { scale, cssW, cssH };
}

// Converts a normalized (0..1) point on the canvas into playfield coordinates.
export function canvasNormToTable(px, py) {
  return { x: px * CANVAS_W - OFF, y: py * CANVAS_H - OFF };
}

/* ------------------------------------------------------------------ colors */
function hexToRgb(hex) {
  const h = String(hex).replace('#', '');
  const n = h.length === 3 ? h.split('').map(c => c + c).join('') : h;
  const num = parseInt(n, 16);
  return { r: (num >> 16) & 255, g: (num >> 8) & 255, b: num & 255 };
}
function shade(hex, amt) {
  const { r, g, b } = hexToRgb(hex);
  const f = (c) => Math.max(0, Math.min(255, Math.round(c + amt)));
  return `rgb(${f(r)}, ${f(g)}, ${f(b)})`;
}
function mix(hex, target, ratio) {
  const a = hexToRgb(hex), b = hexToRgb(target);
  const f = (x, y) => Math.round(x + (y - x) * ratio);
  return `rgb(${f(a.r, b.r)}, ${f(a.g, b.g)}, ${f(a.b, b.b)})`;
}
function rgba(hex, a) {
  const { r, g, b } = hexToRgb(hex);
  return `rgba(${r}, ${g}, ${b}, ${a})`;
}

/* ------------------------------------------------- cached table background */
const tableCache = new Map();

function tableKey(theme, px) {
  return `${theme?.felt}|${theme?.rail}|${theme?.accent}@${px.toFixed(2)}`;
}

function getTableBackground(theme, px) {
  const key = tableKey(theme, px);
  if (tableCache.has(key)) return tableCache.get(key);
  const c = document.createElement('canvas');
  c.width = Math.ceil(CANVAS_W * px);
  c.height = Math.ceil(CANVAS_H * px);
  const g = c.getContext('2d');
  g.setTransform(px, 0, 0, px, px * OFF, px * OFF);
  paintTable(g, theme);
  if (tableCache.size > 6) tableCache.clear();
  tableCache.set(key, c);
  return c;
}

const FRAME_W = TABLE_W + FRAME * 2;
const FRAME_H = TABLE_H + FRAME * 2;
const FRAME_R = 20;

function framePath(ctx, inset = 0, radius = FRAME_R) {
  roundRect(ctx, -FRAME + inset, -FRAME + inset, FRAME_W - inset * 2, FRAME_H - inset * 2, radius);
}

function paintTable(ctx, theme) {
  const felt = theme?.felt || '#0f3d24';
  const rail = theme?.rail || '#3a2e22';
  const accent = theme?.accent || '#c9a24b';

  /* --- Outer wooden frame, beveled so it reads as a raised 3D body --- */
  ctx.save();
  ctx.shadowColor = 'rgba(0,0,0,0.75)';
  ctx.shadowBlur = 26;
  ctx.shadowOffsetY = 12;
  framePath(ctx);
  ctx.fillStyle = '#000';
  ctx.fill();
  ctx.restore();

  const woodGrad = ctx.createLinearGradient(-FRAME, -FRAME, TABLE_W + FRAME, TABLE_H + FRAME);
  woodGrad.addColorStop(0, shade(rail, 40));
  woodGrad.addColorStop(0.35, shade(rail, 8));
  woodGrad.addColorStop(0.7, shade(rail, -14));
  woodGrad.addColorStop(1, shade(rail, -40));
  framePath(ctx);
  ctx.fillStyle = woodGrad;
  ctx.fill();

  // grain
  ctx.save();
  framePath(ctx);
  ctx.clip();
  ctx.globalAlpha = 0.12;
  ctx.strokeStyle = shade(rail, -55);
  ctx.lineWidth = 1;
  for (let i = -FRAME_H; i < FRAME_W + FRAME_H; i += 5) {
    ctx.beginPath();
    ctx.moveTo(i - FRAME, -FRAME);
    ctx.lineTo(i - FRAME + FRAME_H * 0.12, TABLE_H + FRAME);
    ctx.stroke();
  }
  ctx.globalAlpha = 1;
  ctx.restore();

  // top bevel highlight + bottom shade => the frame looks like it has thickness
  framePath(ctx, 1.2, FRAME_R - 1);
  ctx.strokeStyle = 'rgba(255,255,255,0.18)';
  ctx.lineWidth = 2;
  ctx.stroke();
  framePath(ctx, 3, FRAME_R - 3);
  ctx.strokeStyle = 'rgba(0,0,0,0.30)';
  ctx.lineWidth = 2;
  ctx.stroke();

  // accent piping just outside the cushions
  roundRect(ctx, RAIL_L - 3, RAIL_T - 3, (RAIL_R - RAIL_L) + 6, (RAIL_B - RAIL_T) + 6, 8);
  ctx.strokeStyle = rgba(accent, 0.5);
  ctx.lineWidth = 1.4;
  ctx.stroke();

  /* --- Felt bed (extends under the cushions) --- */
  ctx.save();
  roundRect(ctx, RAIL_L, RAIL_T, RAIL_R - RAIL_L, RAIL_B - RAIL_T, 6);
  ctx.clip();

  const feltGrad = ctx.createLinearGradient(0, RAIL_T, 0, RAIL_B);
  feltGrad.addColorStop(0, shade(felt, 26));
  feltGrad.addColorStop(0.42, shade(felt, 6));
  feltGrad.addColorStop(1, shade(felt, -22));
  ctx.fillStyle = feltGrad;
  ctx.fillRect(RAIL_L, RAIL_T, RAIL_R - RAIL_L, RAIL_B - RAIL_T);

  // overhead lamp pool
  const lamp = ctx.createRadialGradient(TABLE_W / 2, TABLE_H * 0.3, 12, TABLE_W / 2, TABLE_H * 0.3, TABLE_H * 0.62);
  lamp.addColorStop(0, 'rgba(255,255,255,0.13)');
  lamp.addColorStop(0.55, 'rgba(255,255,255,0.03)');
  lamp.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = lamp;
  ctx.fillRect(RAIL_L, RAIL_T, RAIL_R - RAIL_L, RAIL_B - RAIL_T);

  // cloth weave
  ctx.globalAlpha = 0.045;
  ctx.strokeStyle = '#000';
  ctx.lineWidth = 1;
  for (let y = RAIL_T; y < RAIL_B; y += 3) {
    ctx.beginPath(); ctx.moveTo(RAIL_L, y); ctx.lineTo(RAIL_R, y); ctx.stroke();
  }
  for (let x = RAIL_L; x < RAIL_R; x += 3) {
    ctx.beginPath(); ctx.moveTo(x, RAIL_T); ctx.lineTo(x, RAIL_B); ctx.stroke();
  }
  ctx.globalAlpha = 1;

  // vignette so the corners fall into shadow
  const vin = ctx.createRadialGradient(TABLE_W / 2, TABLE_H / 2, TABLE_H * 0.22, TABLE_W / 2, TABLE_H / 2, TABLE_H * 0.72);
  vin.addColorStop(0, 'rgba(0,0,0,0)');
  vin.addColorStop(1, 'rgba(0,0,0,0.42)');
  ctx.fillStyle = vin;
  ctx.fillRect(RAIL_L, RAIL_T, RAIL_R - RAIL_L, RAIL_B - RAIL_T);

  // foot spot
  ctx.fillStyle = rgba(accent, 0.28);
  ctx.beginPath(); ctx.arc(TABLE_W / 2, PLAY_T + (PLAY_B - PLAY_T) * 0.25, 1.8, 0, Math.PI * 2); ctx.fill();
  ctx.restore();

  /* --- Cushions: raised rubber with an angled face + pocket jaws --- */
  const cushions = [
    // top
    { nose: [[PLAY_L + JAW_NOSE, PLAY_T], [PLAY_R - JAW_NOSE, PLAY_T]], base: [[PLAY_L + JAW_BASE, RAIL_T], [PLAY_R - JAW_BASE, RAIL_T]] },
    // bottom
    { nose: [[PLAY_L + JAW_NOSE, PLAY_B], [PLAY_R - JAW_NOSE, PLAY_B]], base: [[PLAY_L + JAW_BASE, RAIL_B], [PLAY_R - JAW_BASE, RAIL_B]] },
    // left upper / lower
    { nose: [[PLAY_L, PLAY_T + JAW_NOSE], [PLAY_L, MID_Y - JAW_NOSE]], base: [[RAIL_L, PLAY_T + JAW_BASE], [RAIL_L, MID_Y - JAW_BASE]] },
    { nose: [[PLAY_L, MID_Y + JAW_NOSE], [PLAY_L, PLAY_B - JAW_NOSE]], base: [[RAIL_L, MID_Y + JAW_BASE], [RAIL_L, PLAY_B - JAW_BASE]] },
    // right upper / lower
    { nose: [[PLAY_R, PLAY_T + JAW_NOSE], [PLAY_R, MID_Y - JAW_NOSE]], base: [[RAIL_R, PLAY_T + JAW_BASE], [RAIL_R, MID_Y - JAW_BASE]] },
    { nose: [[PLAY_R, MID_Y + JAW_NOSE], [PLAY_R, PLAY_B - JAW_NOSE]], base: [[RAIL_R, MID_Y + JAW_BASE], [RAIL_R, PLAY_B - JAW_BASE]] },
  ];
  for (const c of cushions) drawCushion(ctx, c, felt);

  /* --- Pockets, cut through the cushions and clipped by the frame silhouette
         so a corner pocket reads as a hole drilled into the corner --- */
  ctx.save();
  framePath(ctx, 2.5, FRAME_R - 2);
  ctx.clip();
  for (const p of pocketList()) drawPocket(ctx, p, accent);
  ctx.restore();

  /* --- Diamond sights on the wood --- */
  drawDiamonds(ctx, accent);
}

// Visual pocket centres are nudged a couple of px inward at the corners so the wood
// visibly wraps the hole instead of the hole bulging over the frame edge. Physics
// still uses the exact corner positions; the offset is far below ball radius.
const CORNER_IN = 2.5;
function pocketList() {
  return [
    { x: PLAY_L + CORNER_IN, y: PLAY_T + CORNER_IN, r: POCKET_R * 0.94 },
    { x: PLAY_R - CORNER_IN, y: PLAY_T + CORNER_IN, r: POCKET_R * 0.94 },
    { x: PLAY_L, y: MID_Y, r: POCKET_R * 0.94 },
    { x: PLAY_R, y: MID_Y, r: POCKET_R * 0.94 },
    { x: PLAY_L + CORNER_IN, y: PLAY_B - CORNER_IN, r: POCKET_R * 0.94 },
    { x: PLAY_R - CORNER_IN, y: PLAY_B - CORNER_IN, r: POCKET_R * 0.94 },
  ];
}

function drawCushion(ctx, c, felt) {
  const [n0, n1] = c.nose, [b0, b1] = c.base;

  // cast shadow on the playfield, just inside the nose
  ctx.save();
  ctx.beginPath();
  ctx.moveTo(n0[0], n0[1]); ctx.lineTo(n1[0], n1[1]);
  ctx.strokeStyle = 'rgba(0,0,0,0.42)';
  ctx.lineWidth = 6;
  ctx.filter = 'blur(2px)';
  ctx.stroke();
  ctx.restore();

  // the sloped rubber face
  ctx.beginPath();
  ctx.moveTo(n0[0], n0[1]);
  ctx.lineTo(n1[0], n1[1]);
  ctx.lineTo(b1[0], b1[1]);
  ctx.lineTo(b0[0], b0[1]);
  ctx.closePath();
  const grad = ctx.createLinearGradient(b0[0], b0[1], n0[0], n0[1]);
  grad.addColorStop(0, shade(felt, 52));
  grad.addColorStop(0.38, shade(felt, 22));
  grad.addColorStop(0.75, shade(felt, -18));
  grad.addColorStop(1, shade(felt, -52));
  ctx.fillStyle = grad;
  ctx.fill();

  // bright lip where the cushion meets the wood
  ctx.beginPath();
  ctx.moveTo(b0[0], b0[1]); ctx.lineTo(b1[0], b1[1]);
  ctx.strokeStyle = 'rgba(255,255,255,0.26)';
  ctx.lineWidth = 1.6;
  ctx.stroke();

  // crisp nose edge
  ctx.beginPath();
  ctx.moveTo(n0[0], n0[1]); ctx.lineTo(n1[0], n1[1]);
  ctx.strokeStyle = 'rgba(255,255,255,0.10)';
  ctx.lineWidth = 1;
  ctx.stroke();
}

function drawPocket(ctx, p, accent) {
  const R = p.r || POCKET_R;

  // leather / metal liner around the opening
  ctx.beginPath();
  ctx.arc(p.x, p.y, R + 3.5, 0, Math.PI * 2);
  const liner = ctx.createLinearGradient(p.x - R, p.y - R, p.x + R, p.y + R);
  liner.addColorStop(0, '#4a4036');
  liner.addColorStop(0.45, '#241f1a');
  liner.addColorStop(1, '#100d0a');
  ctx.fillStyle = liner;
  ctx.fill();

  // the hole itself: a bowl that gets darker toward the bottom-right
  ctx.beginPath();
  ctx.arc(p.x, p.y, R, 0, Math.PI * 2);
  const bowl = ctx.createRadialGradient(
    p.x + R * 0.42, p.y + R * 0.45, R * 0.06,
    p.x - R * 0.05, p.y - R * 0.05, R * 1.08,
  );
  bowl.addColorStop(0, '#000000');
  bowl.addColorStop(0.45, '#0a0a0a');
  bowl.addColorStop(0.82, '#24211d');
  bowl.addColorStop(1, '#3b352d');
  ctx.fillStyle = bowl;
  ctx.fill();

  // hard inner shadow on the lit side => the hole reads as sunken
  ctx.save();
  ctx.beginPath();
  ctx.arc(p.x, p.y, R, 0, Math.PI * 2);
  ctx.clip();
  ctx.beginPath();
  ctx.arc(p.x - R * 0.22, p.y - R * 0.24, R, 0, Math.PI * 2);
  ctx.strokeStyle = 'rgba(0,0,0,0.85)';
  ctx.lineWidth = 6;
  ctx.filter = 'blur(2.5px)';
  ctx.stroke();
  ctx.restore();

  // rim highlight on the far edge
  ctx.beginPath();
  ctx.arc(p.x, p.y, R + 1.2, Math.PI * 0.15, Math.PI * 0.85);
  ctx.strokeStyle = rgba(accent, 0.55);
  ctx.lineWidth = 1.6;
  ctx.stroke();
  ctx.beginPath();
  ctx.arc(p.x, p.y, R + 1.2, Math.PI * 1.1, Math.PI * 1.85);
  ctx.strokeStyle = 'rgba(255,255,255,0.13)';
  ctx.lineWidth = 1.2;
  ctx.stroke();
}

function drawDiamonds(ctx, accent) {
  const woodMidX = (RAIL_L - FRAME) / 2 - 1;      // centre of the side wood band
  const woodMidY = (RAIL_T - FRAME) / 2 - 1;
  const put = (x, y) => {
    ctx.save();
    ctx.translate(x, y);
    ctx.rotate(Math.PI / 4);
    ctx.fillStyle = 'rgba(255,255,255,0.55)';
    ctx.shadowColor = rgba(accent, 0.6);
    ctx.shadowBlur = 4;
    ctx.fillRect(-1.7, -1.7, 3.4, 3.4);
    ctx.restore();
  };
  for (let i = 1; i <= 3; i++) {
    const x = PLAY_L + (PLAY_R - PLAY_L) * (i / 4);
    put(x, woodMidY);
    put(x, TABLE_H - woodMidY);
  }
  for (let i = 1; i <= 3; i++) {
    const yTop = PLAY_T + (MID_Y - PLAY_T) * (i / 4);
    const yBot = MID_Y + (PLAY_B - MID_Y) * (i / 4);
    put(woodMidX, yTop); put(TABLE_W - woodMidX, yTop);
    put(woodMidX, yBot); put(TABLE_W - woodMidX, yBot);
  }
}

/* ---------------------------------------------------------- ball rendering
   A ball is drawn in three passes so it can actually *roll*:
     1. cached body sprite  - the coloured sphere and its cloth shadow
     2. live markings       - the number discs, projected through the ball's 3D
                              orientation, so they turn and slide over the horizon
     3. cached gloss sprite - limb darkening, specular highlight and rim, which
                              belong to the light and the camera, not to the ball
*/
const ballSprites = new Map();

// Markings live on the ball's surface as unit vectors. Real solids carry the
// numbered white circle on two opposite poles; a spot cue ball carries two red dots.
const NUMBER_MARKS = [
  { dir: [0, 0, 1], up: [0, 1, 0] },
  { dir: [0, 0, -1], up: [0, -1, 0] },
];
const CUE_MARKS = [
  { dir: [0, 0, 1], up: [0, 1, 0] },
  { dir: [0, 0, -1], up: [0, -1, 0] },
];
const MARK_SIN = 0.56;     // angular radius of the number disc, as sin(alpha)
const CUE_DOT_SIN = 0.22;

function getBallSprites(ball, px) {
  const key = `${ball.isCue ? 'cue' : ball.number}@${px.toFixed(2)}`;
  if (ballSprites.has(key)) return ballSprites.get(key);
  const pad = 8;
  const size = (BALL_R + pad) * 2;

  const make = (painter) => {
    const c = document.createElement('canvas');
    c.width = Math.ceil(size * px);
    c.height = Math.ceil(size * px);
    const g = c.getContext('2d');
    g.setTransform(px, 0, 0, px, (size / 2) * px, (size / 2) * px);
    painter(g);
    return c;
  };

  const color = ball.isCue ? '#f6f4ef' : (BALL_COLORS[ball.number] || '#cccccc');
  const sprite = {
    body: make((g) => paintBallBody(g, color)),
    gloss: make(paintBallGloss),
    size, half: size / 2,
  };
  if (ballSprites.size > 40) ballSprites.clear();
  ballSprites.set(key, sprite);
  return sprite;
}

function paintBallBody(ctx, color) {
  const R = BALL_R;

  // contact shadow on the cloth
  ctx.save();
  ctx.beginPath();
  ctx.ellipse(R * 0.18, R * 0.62, R * 0.95, R * 0.5, 0, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(0,0,0,0.45)';
  ctx.filter = 'blur(2px)';
  ctx.fill();
  ctx.restore();

  ctx.beginPath();
  ctx.arc(0, 0, R, 0, Math.PI * 2);
  const body = ctx.createRadialGradient(-R * 0.36, -R * 0.42, R * 0.08, 0, 0, R * 1.12);
  body.addColorStop(0, mix(color, '#ffffff', 0.62));
  body.addColorStop(0.3, mix(color, '#ffffff', 0.22));
  body.addColorStop(0.64, color);
  body.addColorStop(1, mix(color, '#000000', 0.3));
  ctx.fillStyle = body;
  ctx.fill();
}

function paintBallGloss(ctx) {
  const R = BALL_R;

  // limb darkening - also shades whatever marking is underneath
  ctx.save();
  ctx.beginPath();
  ctx.arc(0, 0, R, 0, Math.PI * 2);
  ctx.clip();
  const limb = ctx.createRadialGradient(-R * 0.25, -R * 0.3, R * 0.25, 0, 0, R * 1.02);
  limb.addColorStop(0, 'rgba(0,0,0,0)');
  limb.addColorStop(0.72, 'rgba(0,0,0,0.05)');
  limb.addColorStop(1, 'rgba(0,0,0,0.45)');
  ctx.fillStyle = limb;
  ctx.fillRect(-R, -R, R * 2, R * 2);

  // bounce light from the cloth along the lower-right limb
  const bounce = ctx.createRadialGradient(R * 0.45, R * 0.5, 0, R * 0.3, R * 0.36, R * 0.8);
  bounce.addColorStop(0, 'rgba(255,255,255,0.20)');
  bounce.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = bounce;
  ctx.fillRect(-R, -R, R * 2, R * 2);
  ctx.restore();

  // specular highlight
  ctx.save();
  ctx.translate(-R * 0.38, -R * 0.44);
  ctx.rotate(-0.5);
  ctx.beginPath();
  ctx.ellipse(0, 0, R * 0.34, R * 0.2, 0, 0, Math.PI * 2);
  const spec = ctx.createRadialGradient(0, 0, 0, 0, 0, R * 0.34);
  spec.addColorStop(0, 'rgba(255,255,255,0.98)');
  spec.addColorStop(0.5, 'rgba(255,255,255,0.55)');
  spec.addColorStop(1, 'rgba(255,255,255,0)');
  ctx.fillStyle = spec;
  ctx.fill();
  ctx.restore();

  ctx.beginPath();
  ctx.arc(R * 0.34, -R * 0.26, R * 0.09, 0, Math.PI * 2);
  ctx.fillStyle = 'rgba(255,255,255,0.35)';
  ctx.fill();

  ctx.beginPath();
  ctx.arc(0, 0, R - 0.2, 0, Math.PI * 2);
  ctx.strokeStyle = 'rgba(0,0,0,0.32)';
  ctx.lineWidth = 0.8;
  ctx.stroke();
}

// Orthographic projection of a disc that sits on the sphere's surface.
function drawMark(ctx, ball, mark, sinA, paint) {
  const C = applyRot(ball.rot, mark.dir);
  if (C[2] <= 0.02) return;                       // facing away from the camera

  const U = applyRot(ball.rot, mark.up);
  // tangent frame at the marking's centre
  const d = U[0] * C[0] + U[1] * C[1] + U[2] * C[2];
  let t1 = [U[0] - d * C[0], U[1] - d * C[1], U[2] - d * C[2]];
  const l = Math.hypot(t1[0], t1[1], t1[2]);
  if (l < 1e-4) return;
  t1 = [t1[0] / l, t1[1] / l, t1[2] / l];
  // t1 x C (not C x t1): the screen's y axis points down, so this ordering is the
  // one that keeps the projected basis right-handed and the digits un-mirrored.
  const t2 = [
    t1[1] * C[2] - t1[2] * C[1],
    t1[2] * C[0] - t1[0] * C[2],
    t1[0] * C[1] - t1[1] * C[0],
  ];

  const R = ball.r;
  const s = R * sinA;
  // The projected disc is exactly the unit circle mapped by the tangent vectors'
  // screen components - foreshortening near the limb falls out of the maths.
  ctx.save();
  ctx.globalAlpha = Math.min(1, Math.max(0, (C[2] - 0.02) / 0.16));
  ctx.translate(ball.x + C[0] * R * 0.995, ball.y + C[1] * R * 0.995);
  ctx.transform(t2[0] * s, t2[1] * s, t1[0] * s, t1[1] * s, 0, 0);
  paint(ctx);
  ctx.restore();
}

function paintNumberDisc(number) {
  return (ctx) => {
    ctx.beginPath();
    ctx.arc(0, 0, 1, 0, Math.PI * 2);
    ctx.fillStyle = '#f7f5ee';
    ctx.fill();
    ctx.lineWidth = 0.06;
    ctx.strokeStyle = 'rgba(0,0,0,0.16)';
    ctx.stroke();

    // work in a 10x space so the glyph rasterises crisply under the tiny transform
    ctx.scale(0.1, 0.1);
    ctx.fillStyle = '#1b1b1b';
    ctx.font = `bold 12px 'Segoe UI', Arial, sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(number), 0, 0.5);
  };
}

function paintCueDot(ctx) {
  ctx.beginPath();
  ctx.arc(0, 0, 1, 0, Math.PI * 2);
  ctx.fillStyle = '#d23b3b';
  ctx.fill();
}

function drawBall(ctx, ball, px) {
  const s = getBallSprites(ball, px);
  ctx.drawImage(s.body, ball.x - s.half, ball.y - s.half, s.size, s.size);

  ctx.save();
  ctx.beginPath();
  ctx.arc(ball.x, ball.y, ball.r, 0, Math.PI * 2);
  ctx.clip();
  if (ball.isCue) {
    for (const m of CUE_MARKS) drawMark(ctx, ball, m, CUE_DOT_SIN, paintCueDot);
  } else {
    const paint = paintNumberDisc(ball.number);
    for (const m of NUMBER_MARKS) drawMark(ctx, ball, m, MARK_SIN, paint);
  }
  ctx.restore();

  ctx.drawImage(s.gloss, ball.x - s.half, ball.y - s.half, s.size, s.size);
}

/* ----------------------------------------------------------- cue stick */
function drawCueStick(ctx, cueBall, angle, pullback, style) {
  const dx = Math.cos(angle), dy = Math.sin(angle);
  const px = -dy, py = dx;                         // perpendicular
  const tipX = cueBall.x - dx * (BALL_R + 2 + pullback);
  const tipY = cueBall.y - dy * (BALL_R + 2 + pullback);
  // Shorten the stick rather than let it get hard-clipped at the canvas edge.
  const exit = rayBoxExit(tipX, tipY, -dx, -dy, -OFF, -OFF, TABLE_W + OFF, TABLE_H + OFF);
  const LEN = Math.max(105, Math.min(215, exit - 3));

  // t runs 0 (tip) .. 1 (butt); radius grows toward the butt
  const at = (t) => ({ x: tipX - dx * LEN * t, y: tipY - dy * LEN * t });
  const rad = (t) => 1.5 + t * 2.3;

  const seg = (t0, t1, fill, stroke) => {
    const a = at(t0), b = at(t1), r0 = rad(t0), r1 = rad(t1);
    ctx.beginPath();
    ctx.moveTo(a.x + px * r0, a.y + py * r0);
    ctx.lineTo(b.x + px * r1, b.y + py * r1);
    ctx.lineTo(b.x - px * r1, b.y - py * r1);
    ctx.lineTo(a.x - px * r0, a.y - py * r0);
    ctx.closePath();
    ctx.fillStyle = fill;
    ctx.fill();
    if (stroke) { ctx.strokeStyle = stroke; ctx.lineWidth = 0.5; ctx.stroke(); }
  };

  // shadow on the cloth
  ctx.save();
  ctx.translate(4, 6);
  ctx.globalAlpha = 0.33;
  ctx.filter = 'blur(2px)';
  seg(0, 1, '#000');
  ctx.restore();

  const shaftGrad = ctx.createLinearGradient(
    tipX + px * 4, tipY + py * 4,
    tipX - px * 4, tipY - py * 4,
  );
  shaftGrad.addColorStop(0, style.shaftDark);
  shaftGrad.addColorStop(0.42, style.shaft);
  shaftGrad.addColorStop(0.55, mix(style.shaft, '#ffffff', 0.45));
  shaftGrad.addColorStop(1, style.shaftDark);

  const buttGrad = ctx.createLinearGradient(
    tipX + px * 5, tipY + py * 5,
    tipX - px * 5, tipY - py * 5,
  );
  buttGrad.addColorStop(0, mix(style.butt, '#000000', 0.45));
  buttGrad.addColorStop(0.45, style.butt);
  buttGrad.addColorStop(0.58, mix(style.butt, '#ffffff', 0.32));
  buttGrad.addColorStop(1, mix(style.butt, '#000000', 0.5));

  seg(0.0, 0.022, '#2b2b2b');                 // leather tip
  seg(0.022, 0.06, '#f4f1e6');                // ferrule
  seg(0.06, 0.55, shaftGrad);                 // shaft
  seg(0.55, 0.585, style.accent);             // joint collar
  seg(0.585, 0.86, buttGrad);                 // forearm
  seg(0.83, 0.86, style.accent);              // wrap ring
  seg(0.86, 0.97, mix(style.butt, '#000000', 0.25));
  seg(0.97, 1, '#141414');                    // bumper

  // long specular stripe so the stick reads as a cylinder
  const a = at(0.06), b = at(0.97);
  ctx.beginPath();
  ctx.moveTo(a.x + px * 0.7, a.y + py * 0.7);
  ctx.lineTo(b.x + px * 1.1, b.y + py * 1.1);
  ctx.strokeStyle = 'rgba(255,255,255,0.3)';
  ctx.lineWidth = 0.9;
  ctx.stroke();
}

/* --------------------------------------------------------- aim guide */
function drawAimGuide(ctx, table, angle, power, accent) {
  const cue = table.cue;
  const dir = { x: Math.cos(angle), y: Math.sin(angle) };

  let hit = null, hitDist = Infinity;
  for (const b of table.activeObjectBalls) {
    const toB = { x: b.x - cue.x, y: b.y - cue.y };
    const proj = toB.x * dir.x + toB.y * dir.y;
    if (proj <= 0) continue;
    const cx = cue.x + dir.x * proj, cy = cue.y + dir.y * proj;
    const perp = Math.hypot(b.x - cx, b.y - cy);
    if (perp < BALL_R * 2 && proj < hitDist) {
      const back = Math.sqrt(Math.max(0, (BALL_R * 2) ** 2 - perp ** 2));
      hitDist = proj - back;
      hit = { x: cue.x + dir.x * hitDist, y: cue.y + dir.y * hitDist, target: b };
    }
  }

  const endLen = hit ? hitDist : 340;
  const ex = clamp(cue.x + dir.x * endLen, PLAY_L + 1, PLAY_R - 1);
  const ey = clamp(cue.y + dir.y * endLen, PLAY_T + 1, PLAY_B - 1);

  ctx.save();
  ctx.setLineDash([4, 6]);
  ctx.lineCap = 'round';
  ctx.strokeStyle = `rgba(255,255,255,${0.35 + power * 0.45})`;
  ctx.lineWidth = 1.8;
  ctx.beginPath();
  ctx.moveTo(cue.x + dir.x * (BALL_R + 2), cue.y + dir.y * (BALL_R + 2));
  ctx.lineTo(ex, ey);
  ctx.stroke();
  ctx.restore();

  if (hit) {
    // ghost cue ball at the contact point
    ctx.save();
    ctx.beginPath();
    ctx.arc(hit.x, hit.y, BALL_R, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255,255,255,0.16)';
    ctx.fill();
    ctx.setLineDash([2.5, 3]);
    ctx.strokeStyle = 'rgba(255,255,255,0.85)';
    ctx.lineWidth = 1.3;
    ctx.stroke();
    ctx.restore();

    // predicted path of the object ball
    const ox = hit.target.x - hit.x, oy = hit.target.y - hit.y;
    const len = Math.hypot(ox, oy) || 1;
    ctx.save();
    ctx.strokeStyle = rgba(accent, 0.65);
    ctx.lineWidth = 1.4;
    ctx.setLineDash([3, 4]);
    ctx.beginPath();
    ctx.moveTo(hit.target.x, hit.target.y);
    ctx.lineTo(hit.target.x + (ox / len) * 44, hit.target.y + (oy / len) * 44);
    ctx.stroke();
    ctx.restore();
  }
}

/* ------------------------------------------------------------- main draw */
export function drawTable(ctx, table, opts = {}) {
  const {
    theme, aimAngle = -Math.PI / 2, power = 0, canShoot = true,
    stick = null, cueStyle = CUE_STYLES.wood,
  } = opts;
  const px = ctx.canvas.__pxScale || 2;
  const accent = theme?.accent || '#c9a24b';

  ctx.clearRect(-OFF, -OFF, CANVAS_W, CANVAS_H);
  ctx.drawImage(getTableBackground(theme, px), -OFF, -OFF, CANVAS_W, CANVAS_H);

  if (canShoot && table.cue.active && table.isAllStopped()) {
    drawAimGuide(ctx, table, aimAngle, power, accent);
  }

  const balls = table.balls.filter(b => b.active).sort((a, b) => a.y - b.y);
  for (const b of balls) drawBall(ctx, b, px);

  if (stick && stick.visible && table.cue.active) {
    drawCueStick(ctx, table.cue, stick.angle, stick.pullback, cueStyle);
  }
}

// Distance from (x,y) along (dx,dy) until the ray leaves the axis-aligned box.
function rayBoxExit(x, y, dx, dy, minX, minY, maxX, maxY) {
  let t = Infinity;
  if (dx > 0.0001) t = Math.min(t, (maxX - x) / dx);
  if (dx < -0.0001) t = Math.min(t, (minX - x) / dx);
  if (dy > 0.0001) t = Math.min(t, (maxY - y) / dy);
  if (dy < -0.0001) t = Math.min(t, (minY - y) / dy);
  return Number.isFinite(t) ? t : 215;
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
