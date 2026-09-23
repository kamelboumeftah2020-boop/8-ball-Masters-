// صور بورتريه للاعبين تُرسم من النموذج ثلاثي الأبعاد نفسه (لبطاقات اللاعبين بأسلوب ألتيميت تيم)
import * as THREE from 'three';
import { RoomEnvironment } from 'three/addons/environments/RoomEnvironment.js';
import { Player3D } from './player3d.js';
import { CHARACTERS } from '/shared/characters.js';
import { TEAMS } from '/shared/constants.js';

const cache = new Map();
let R = null;

function setup() {
  if (R) return R;
  const canvas = document.createElement('canvas');
  const renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true, preserveDrawingBuffer: true });
  renderer.setPixelRatio(1);
  renderer.setSize(220, 260, false);
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  const scene = new THREE.Scene();
  const pm = new THREE.PMREMGenerator(renderer);
  scene.environment = pm.fromScene(new RoomEnvironment(), 0.04).texture;
  scene.environmentIntensity = 0.6;
  scene.add(new THREE.HemisphereLight('#ffffff', '#445566', 1.2));
  const key = new THREE.DirectionalLight('#fff4e0', 2.6); key.position.set(3, 3, 2); scene.add(key);
  const rim = new THREE.DirectionalLight('#9fd8ff', 2.0); rim.position.set(-2, 2, -3); scene.add(rim);
  const cam = new THREE.PerspectiveCamera(24, 220 / 260, 0.1, 20);
  R = { renderer, scene, cam };
  return R;
}

// team: 0/1، slot: للمركز (يؤثر على طقم الحارس)
export function portrait(charId, team = 0, slot = 3) {
  const key = `${charId}|${team}|${slot}|${TEAMS[team] && TEAMS[team].id}|${TEAMS[team] && TEAMS[team].color}`;
  if (cache.has(key)) return cache.get(key);
  let url = '';
  try {
    const { renderer, scene, cam } = setup();
    const p = new Player3D({ id: 0, team, slot, name: '', char: charId, human: false }, { preview: slot !== 0 });
    p.update(0.016, { x: 0, y: 0, vx: 0, vy: 0, face: 0, state: 0, emote: 0, hold: 0 }, 0);
    p.root.rotation.y = -0.35;
    scene.add(p.root);
    const h = p.h;
    cam.position.set(2.1, 1.66 * h, 0.55);
    cam.lookAt(0, 1.56 * h, 0);
    renderer.render(scene, cam);
    url = renderer.domElement.toDataURL('image/png');
    scene.remove(p.root);
    p.dispose();
  } catch (e) { console.warn('portrait failed', e); }
  cache.set(key, url);
  return url;
}

export function allPortraits() {
  return Object.fromEntries(CHARACTERS.map((c) => [c.id, portrait(c.id)]));
}

// تقييم بأسلوب FIFA انطلاقاً من إحصائيات الشخصية
export function ratings(c, gk = false) {
  const s = c.stats;
  const v = (x, base = 74, k = 130) => Math.max(45, Math.min(99, Math.round(base + (x - 1) * k)));
  const r = {
    pac: v(s.speed), sho: v(s.shot), pas: v(s.pass), dri: v(s.dribble), def: v(s.tackle), phy: v((c.look.build + s.stamina) / 2, 72, 110),
  };
  r.gk = v(s.keeper, 72, 120);
  r.ovr = gk ? Math.round(r.gk * 0.7 + r.phy * 0.15 + r.pas * 0.15) : Math.round(r.pac * 0.2 + r.sho * 0.2 + r.pas * 0.17 + r.dri * 0.2 + r.def * 0.1 + r.phy * 0.13) + 4;
  r.pos = gk ? 'GK' : r.def > r.sho ? 'CB' : r.sho >= r.pas ? 'ST' : 'CM';
  return r;
}

export function cardHTML(c, opts = {}) {
  const r = ratings(c, opts.gk);
  const img = opts.img ?? portrait(c.id, opts.team ?? 0, opts.gk ? 0 : 3);
  const tier = r.ovr >= 86 ? 'icon' : r.ovr >= 80 ? 'gold' : 'silver';
  return `<div class="fut ${tier} ${opts.mini ? 'mini' : ''} ${opts.cls || ''}" data-char="${c.id}">
    <div class="fut-top"><b>${r.ovr}</b><span>${r.pos}</span><i>${c.icon}</i></div>
    <div class="fut-img">${img ? `<img src="${img}" alt="">` : `<span class="fb">${c.icon}</span>`}</div>
    <div class="fut-name">${c.name}</div>
    ${opts.mini ? '' : `<div class="fut-stats">
      <span><b>${r.pac}</b>سرعة</span><span><b>${r.dri}</b>مراوغة</span>
      <span><b>${r.sho}</b>تسديد</span><span><b>${r.def}</b>دفاع</span>
      <span><b>${r.pas}</b>تمرير</span><span><b>${r.phy}</b>بدني</span>
    </div>`}
  </div>`;
}
