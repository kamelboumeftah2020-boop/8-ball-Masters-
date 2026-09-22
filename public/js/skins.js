// نظام الأطقم (السكينات): كل طقم = بيانات فقط، والرسم يتم إجرائياً كخامة (texture)
// لإضافة سكين جديد مستقبلاً: أضف عنصراً إلى KITS أو SKINS ثم مرّره إلى Player3D عبر opts.skin
import * as THREE from 'three';

// أنماط القمصان المدعومة: plain, pinstripes, stripes, hoops, sash, halves, gradient, camo, chevron
export const KITS = {
  home0: { pattern: 'pinstripes', base: '#c8102e', second: '#e8e8e8', trim: '#ffffff', text: '#ffffff', sponsor: 'LEGENDS', shorts: '#f4f4f4', shortsTrim: '#c8102e', socks: '#c8102e', sockBand: '#ffffff' },
  home1: { pattern: 'hoops', base: '#1e5bd6', second: '#15409e', trim: '#ffd23f', text: '#ffd23f', sponsor: '5v5 ONLINE', shorts: '#0d2c66', shortsTrim: '#ffd23f', socks: '#1e5bd6', sockBand: '#ffd23f' },
  gk0: { pattern: 'gradient', base: '#1f9d55', second: '#0b4f29', trim: '#111111', text: '#ffffff', sponsor: 'LEGENDS', shorts: '#151515', shortsTrim: '#1f9d55', socks: '#1f9d55', sockBand: '#111111', longSleeves: true },
  gk1: { pattern: 'chevron', base: '#f08a00', second: '#b85f00', trim: '#111111', text: '#111111', sponsor: '5v5 ONLINE', shorts: '#222222', shortsTrim: '#f08a00', socks: '#f08a00', sockBand: '#111111', longSleeves: true },
};

// سكينات إضافية جاهزة للمتجر مستقبلاً
export const SKINS = {
  gold: { name: 'الذهبي', pattern: 'sash', base: '#d4a017', second: '#fff1b0', trim: '#111111', text: '#111111', sponsor: 'LEGEND', shorts: '#111111', shortsTrim: '#d4a017', socks: '#111111', sockBand: '#d4a017' },
  camo: { name: 'العسكري', pattern: 'camo', base: '#4b5a36', second: '#2e3824', trim: '#c9b27a', text: '#f0e6c8', sponsor: 'ELITE', shorts: '#2e3824', shortsTrim: '#c9b27a', socks: '#4b5a36', sockBand: '#c9b27a' },
  retro: { name: 'الكلاسيكي', pattern: 'halves', base: '#ffffff', second: '#0a0a0a', trim: '#0a0a0a', text: '#c8102e', sponsor: '1990', shorts: '#0a0a0a', shortsTrim: '#ffffff', socks: '#ffffff', sockBand: '#0a0a0a' },
};

export function kitFor(team, slot, skin) {
  if (skin && SKINS[skin]) return SKINS[skin];
  return KITS[(slot === 0 ? 'gk' : 'home') + team];
}

const texCache = new Map();

function fabricNoise(g, w, h, amt = 0.05) {
  const img = g.getImageData(0, 0, w, h);
  const d = img.data;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const i = (y * w + x) * 4;
      // نسيج قماشي: تشابك خيوط + ضوضاء
      const weave = ((x + y) % 4 < 2 ? 1 : -1) * 3 + ((x * 7 + y * 13) % 5) - 2;
      const n = (Math.random() - 0.5) * 255 * amt + weave;
      d[i] += n; d[i + 1] += n; d[i + 2] += n;
    }
  }
  g.putImageData(img, 0, 0);
}

function drawPattern(g, kit, w, h) {
  g.fillStyle = kit.base; g.fillRect(0, 0, w, h);
  g.fillStyle = kit.second;
  switch (kit.pattern) {
    case 'pinstripes': for (let x = 0; x < w; x += 22) g.fillRect(x, 0, 3, h); break;
    case 'stripes': for (let x = 0; x < w; x += 64) g.fillRect(x, 0, 32, h); break;
    case 'hoops': for (let y = 30; y < h; y += 44) g.fillRect(0, y, w, 20); break;
    case 'halves': g.fillRect(w * 0.25, 0, w * 0.5, h); break;
    case 'sash':
      for (const cx of [w * 0.25, w * 0.75]) { g.save(); g.translate(cx, h / 2); g.rotate(-0.7); g.fillRect(-w, -18, w * 2, 36); g.restore(); }
      break;
    case 'gradient': {
      const gr = g.createLinearGradient(0, 0, 0, h);
      gr.addColorStop(0, kit.base); gr.addColorStop(1, kit.second);
      g.fillStyle = gr; g.fillRect(0, 0, w, h);
      g.globalAlpha = 0.25; g.fillStyle = '#000';
      for (let x = 0; x < w; x += 12) g.fillRect(x, 0, 1, h);
      g.globalAlpha = 1;
      break;
    }
    case 'chevron':
      for (let y = 20; y < h; y += 50) { g.beginPath(); for (let x = 0; x <= w; x += 32) g.lineTo(x, y + ((x / 32) % 2 ? 16 : 0)); g.lineTo(w, y + 14); for (let x = w; x >= 0; x -= 32) g.lineTo(x, y + 14 + ((x / 32) % 2 ? 16 : 0)); g.fill(); }
      break;
    case 'camo':
      for (let i = 0; i < 70; i++) { g.beginPath(); g.ellipse(Math.random() * w, Math.random() * h, 10 + Math.random() * 26, 6 + Math.random() * 14, Math.random() * 3, 0, Math.PI * 2); g.fill(); }
      break;
  }
}

// خامة القميص: u حول الجسم (0.25 = الصدر، 0.75 = الظهر)، v من الورك (أسفل) للرقبة (أعلى)
export function jerseyTexture(kit, number, name) {
  const key = `j|${JSON.stringify(kit)}|${number}|${name}`;
  if (texCache.has(key)) return texCache.get(key);
  const w = 512, h = 256;
  const c = document.createElement('canvas'); c.width = w; c.height = h;
  const g = c.getContext('2d');
  drawPattern(g, kit, w, h);
  // ألواح جانبية أغمق
  for (const cx of [0, w / 2, w]) {
    const gr = g.createLinearGradient(cx - 40, 0, cx + 40, 0);
    gr.addColorStop(0, 'rgba(0,0,0,0)'); gr.addColorStop(0.5, 'rgba(0,0,0,0.28)'); gr.addColorStop(1, 'rgba(0,0,0,0)');
    g.fillStyle = gr; g.fillRect(cx - 40, 0, 80, h);
  }
  // الياقة (V أمامية)
  g.fillStyle = kit.trim;
  g.fillRect(0, 0, w, 10);
  g.beginPath(); g.moveTo(w * 0.25 - 26, 0); g.lineTo(w * 0.25, 40); g.lineTo(w * 0.25 + 26, 0); g.closePath(); g.fill();
  g.fillStyle = kit.base;
  g.beginPath(); g.moveTo(w * 0.25 - 18, 0); g.lineTo(w * 0.25, 28); g.lineTo(w * 0.25 + 18, 0); g.closePath(); g.fill();
  // حافة سفلية
  g.fillStyle = 'rgba(0,0,0,0.25)'; g.fillRect(0, h - 8, w, 8);
  g.fillStyle = kit.text;
  g.textAlign = 'center'; g.textBaseline = 'middle';
  // الراعي في الأمام
  g.font = 'bold 26px Arial Black, Arial, sans-serif';
  g.fillText(kit.sponsor || '', w * 0.25, 128);
  // شعار النادي والرقم الصغير
  g.beginPath(); g.arc(w * 0.25 - 42, 72, 13, 0, Math.PI * 2); g.fill();
  g.fillStyle = kit.base; g.beginPath(); g.arc(w * 0.25 - 42, 72, 8, 0, Math.PI * 2); g.fill();
  g.fillStyle = kit.text;
  g.font = 'bold 26px Arial Black, Arial';
  g.fillText(String(number), w * 0.25 + 42, 74);
  // الاسم والرقم على الظهر
  g.font = 'bold 22px Arial, Tahoma, sans-serif';
  g.fillText(String(name || '').replace(/[^\p{L}\p{N} ]/gu, '').trim().slice(0, 12).toUpperCase(), w * 0.75, 52);
  g.font = 'bold 104px Arial Black, Arial, sans-serif';
  g.lineWidth = 5; g.strokeStyle = 'rgba(0,0,0,0.35)';
  g.strokeText(String(number), w * 0.75, 142);
  g.fillText(String(number), w * 0.75, 142);
  fabricNoise(g, w, h, 0.05);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  t.anisotropy = 4;
  texCache.set(key, t);
  return t;
}

// خامة الأكمام / الجوارب / الشورت: لون + شريط
export function bandTexture(color, band, bandAt = 'bottom', stripes = false) {
  const key = `b|${color}|${band}|${bandAt}|${stripes}`;
  if (texCache.has(key)) return texCache.get(key);
  const w = 128, h = 64;
  const c = document.createElement('canvas'); c.width = w; c.height = h;
  const g = c.getContext('2d');
  g.fillStyle = color; g.fillRect(0, 0, w, h);
  g.fillStyle = band;
  if (bandAt === 'bottom') g.fillRect(0, h - 9, w, 6);
  else if (bandAt === 'top') { g.fillRect(0, 4, w, 7); g.fillRect(0, 14, w, 3); }
  if (stripes) { for (const x of [0, w / 2]) g.fillRect(x - 3, 0, 6, h); }
  fabricNoise(g, w, h, 0.05);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  texCache.set(key, t);
  return t;
}
