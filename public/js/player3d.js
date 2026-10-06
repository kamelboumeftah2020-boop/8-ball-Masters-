// اللاعب ثلاثي الأبعاد: نموذج بشري واقعي + حركات Motion Capture حقيقية
//   الجسم والحركات: Quaternius (CC0) — الركلات: قاعدة CMU لتصوير الحركة (Vicon)
//   الطقم يُرسم مباشرة على الجسم داخل الشيدر (ألوان + أنماط + رقم واسم على الظهر) → يدعم السكينات
import * as THREE from 'three';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import { MeshoptDecoder } from 'three/addons/libs/meshopt_decoder.module.js';
import * as SkeletonUtils from 'three/addons/utils/SkeletonUtils.js';
import { STATE, TEAMS } from '/shared/constants.js';
import { charOf } from '/shared/characters.js';
import { kitFor } from './skins.js';
import { Player3D as LegacyPlayer3D, makeLabel } from './player3d-legacy.js';

export { makeLabel };
const PI = Math.PI;
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const sm = (a, b, v) => { const t = clamp((v - a) / (b - a), 0, 1); return t * t * (3 - 2 * t); };

// ---------- تحميل النموذج (مرة واحدة) ----------
const PA = { gltf: null, clips: {}, failed: false };
export function playerAssetsReady() { return !!PA.gltf; }
export async function loadPlayerAssets() {
  if (PA.gltf || PA.failed) return;
  try {
    const loader = new GLTFLoader().setMeshoptDecoder(MeshoptDecoder);
    let gltf;
    if (window.PLAYERS_GLB_B64) {
      const bin = Uint8Array.from(atob(window.PLAYERS_GLB_B64), (c) => c.charCodeAt(0));
      gltf = await loader.parseAsync(bin.buffer, '');
    } else {
      gltf = await loader.loadAsync(window.PLAYERS_GLB_URL || '/models/players.glb');
    }
    PA.gltf = gltf;
    for (const c of gltf.animations) PA.clips[c.name] = c;
    // حذف قنوات الانتقال للحوض من حلقات الجري (تبقى في مكانها) ما عدا التذبذب الرأسي
    gltf.scene.traverse((o) => { if (o.isMesh) { o.castShadow = true; o.frustumCulled = false; } });
  } catch (e) {
    console.warn('players.glb failed, using legacy players', e);
    PA.failed = true;
  }
}

// ---------- شيدر الطقم ----------
const PATTERNS = { plain: 0, pinstripes: 1, stripes: 2, hoops: 3, sash: 4, halves: 5, gradient: 6, camo: 7, chevron: 8 };
const KIT_GLSL = /* glsl */`
uniform vec3 uShirt, uShirt2, uTrim, uShorts, uShortsTrim, uSocks, uSockBand, uBoots, uGlove, uSkinTint, uNumCol;
uniform float uPat, uSleeve, uGloves;
uniform sampler2D uNum;
varying vec3 vRest; varying vec3 vRestN;
float bnd(float x, float a, float b) { return smoothstep(a - 0.006, a + 0.006, x) * (1.0 - smoothstep(b - 0.006, b + 0.006, x)); }
// المناطق في وضعية الربط (T-pose): y للارتفاع و |x| للذراعين
void kitZones(vec3 p, out float shirt, out float sleeve, out float shorts, out float socks, out float boots, out float glove) {
  float ax = abs(p.x), y = p.y;
  float armZone = step(1.26, y) * smoothstep(0.19, 0.205, ax);
  sleeve = armZone * bnd(ax, 0.0, uSleeve);
  shirt = (1.0 - armZone) * bnd(y, 0.965, 1.525) * step(ax, 0.3);
  shorts = bnd(y, 0.665, 0.985) * step(ax, 0.3) * (1.0 - shirt);
  socks = bnd(y, 0.115, 0.52) * step(y, 1.0);
  boots = 1.0 - smoothstep(0.105, 0.12, y);
  glove = armZone * smoothstep(0.665, 0.68, ax) * uGloves;
}
`;

function kitMaterial(base, kit, opts) {
  const m = base.clone();
  const C = (c) => new THREE.Color(c);
  const U = {
    uShirt: { value: C(kit.base) }, uShirt2: { value: C(kit.second || kit.base) }, uTrim: { value: C(kit.trim || '#ffffff') },
    uShorts: { value: C(kit.shorts) }, uShortsTrim: { value: C(kit.shortsTrim || kit.trim || '#ffffff') },
    uSocks: { value: C(kit.socks) }, uSockBand: { value: C(kit.sockBand || kit.trim || '#ffffff') },
    uBoots: { value: C(opts.boots || '#202020') }, uGlove: { value: C(opts.glove || '#f2f2f2') }, uSkinTint: { value: opts.skinTint },
    uNumCol: { value: C(kit.text || '#ffffff') },
    uPat: { value: PATTERNS[kit.pattern] ?? 0 }, uSleeve: { value: kit.longSleeves ? 0.72 : 0.36 }, uGloves: { value: opts.gloves ? 1 : 0 },
    uNum: { value: opts.numTex },
  };
  m.userData.kitUniforms = U;
  m.onBeforeCompile = (sh) => {
    Object.assign(sh.uniforms, U);
    sh.vertexShader = sh.vertexShader
      .replace('#include <common>', `#include <common>\n${KIT_GLSL}`)
      .replace('#include <begin_vertex>', `#include <begin_vertex>
        vRest = position; vRestN = normal;
        { float s1, s2, s3, s4, s5, s6; kitZones(position, s1, s2, s3, s4, s5, s6);
          // سماكة القماش: القميص والشورت أوسع قليلاً من الجلد
          transformed += normal * (max(s1, s2) * 0.011 + s3 * 0.014 + s4 * 0.004); }`);
    sh.fragmentShader = sh.fragmentShader
      .replace('#include <common>', `#include <common>\n${KIT_GLSL}\nfloat kCloth;`)
      .replace('#include <map_fragment>', `#include <map_fragment>
        {
          float shirt, sleeve, shorts, socks, boots, glove;
          kitZones(vRest, shirt, sleeve, shorts, socks, boots, glove);
          vec3 p = vRest; float ax = abs(p.x);
          // نمط القميص
          float pat = 0.0;
          if (uPat < 0.5) pat = 0.0;
          else if (uPat < 1.5) pat = step(fract(p.x * 42.0), 0.14);
          else if (uPat < 2.5) pat = step(fract(p.x * 7.5 + 0.5), 0.5);
          else if (uPat < 3.5) pat = step(fract(p.y * 9.0), 0.42);
          else if (uPat < 4.5) pat = step(abs(p.x - (p.y - 1.24) * 0.9), 0.055);
          else if (uPat < 5.5) pat = step(0.0, p.x);
          else if (uPat < 6.5) pat = smoothstep(1.45, 0.98, p.y) * 0.85;
          else if (uPat < 7.5) pat = step(0.55, fract(sin(dot(floor(p.xy * vec2(18.0, 14.0)), vec2(12.9898, 78.233))) * 43758.5453));
          else pat = step(fract(p.y * 8.0 + ax * 4.0), 0.35);
          vec3 shirtC = mix(uShirt, uShirt2, pat);
          // ياقة + أطراف الأكمام
          shirtC = mix(shirtC, uTrim, bnd(p.y, 1.488, 1.525) * step(ax, 0.13));
          vec3 sleeveC = mix(uShirt, uTrim, bnd(ax, uSleeve - 0.028, uSleeve));
          // الرقم والاسم على الظهر
          float back = smoothstep(-0.15, -0.35, vRestN.z) * shirt;
          vec2 nuv = vec2((0.165 - p.x) / 0.33, (p.y - 1.06) / 0.38);
          float num = 0.0;
          if (nuv.x > 0.0 && nuv.x < 1.0 && nuv.y > 0.0 && nuv.y < 1.0) num = texture2D(uNum, nuv).a * back;
          shirtC = mix(shirtC, uNumCol, num);
          vec3 shortsC = mix(uShorts, uShortsTrim, bnd(ax, 0.172, 0.196) * shorts);
          vec3 socksC = mix(uSocks, uSockBand, bnd(p.y, 0.455, 0.5));
          vec3 bootC = mix(uBoots, vec3(0.08), 1.0 - smoothstep(0.018, 0.026, p.y));
          diffuseColor.rgb *= uSkinTint;
          vec3 kitC = diffuseColor.rgb;
          kitC = mix(kitC, socksC, socks);
          kitC = mix(kitC, shortsC, shorts);
          kitC = mix(kitC, sleeveC, sleeve);
          kitC = mix(kitC, shirtC, shirt);
          kitC = mix(kitC, bootC, boots);
          kitC = mix(kitC, uGlove, glove);
          kCloth = clamp(shirt + sleeve + shorts + socks + boots + glove, 0.0, 1.0);
          diffuseColor.rgb = kitC;
        }`)
      .replace('#include <roughnessmap_fragment>', `#include <roughnessmap_fragment>
        roughnessFactor = mix(roughnessFactor, 0.86, kCloth);`)
      .replace('#include <normal_fragment_maps>', `#include <normal_fragment_maps>
        normal = normalize(mix(normal, nonPerturbedNormal, kCloth * 0.88));`);
  };
  m.customProgramCacheKey = () => 'kit-v1';
  return m;
}

// رقم واسم اللاعب (قناع ألفا يُلوَّن في الشيدر)
const numCache = new Map();
function numberTexture(num, name) {
  const key = `${num}|${name}`;
  if (numCache.has(key)) return numCache.get(key);
  const c = document.createElement('canvas'); c.width = 256; c.height = 256;
  const g = c.getContext('2d');
  g.fillStyle = '#fff'; g.textAlign = 'center'; g.textBaseline = 'middle';
  const nm = String(name || '').replace(/[^\p{L}\p{N} ]/gu, '').trim().slice(0, 10);
  if (nm) { g.font = 'bold 34px Changa, Cairo, sans-serif'; g.fillText(nm, 128, 222); }
  g.font = 'bold 150px Changa, Oswald, Impact, sans-serif';
  g.fillText(String(num), 128, 110);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  numCache.set(key, t);
  return t;
}

const HAIR = { spiky: 'simpleparted', mohawk: 'buzzed', bald: null, long: 'long', hood: 'buzzed', afro: 'buns', bun: 'buns', short: 'buzzed', curly: 'buns' };
const SKIN_REF = new THREE.Color('#e9b997');
const geoCache = new Map();
const geo = (k, f) => { if (!geoCache.has(k)) geoCache.set(k, f()); return geoCache.get(k); };
const LOCO = [['idle', 0], ['walk', 1.25], ['jog', 3.3], ['sprint', 7.0]];
const KICKS = ['kick', 'kick_b', 'kick_c'];
const _q = new THREE.Quaternion(), _q2 = new THREE.Quaternion(), _v = new THREE.Vector3(), _v2 = new THREE.Vector3();

export class Player3D {
  constructor(info, opts = {}) {
    if (!PA.gltf) return new LegacyPlayer3D(info, opts);
    this.info = info;
    this.c = charOf(info.char);
    const look = this.c.look;
    const team = TEAMS[info.team] || TEAMS[0];
    const gk = info.slot === 0 && !opts.preview;
    const h = look.height, b = look.build;
    this.h = h;
    this.root = new THREE.Group();
    // lean: ميلان في المنعطفات (حول محور الاتجاه) — body: يدير النموذج ليواجه +X مثل بقية اللعبة
    this.lean = new THREE.Group();
    this.root.add(this.lean);
    this.body = new THREE.Group();
    this.body.rotation.order = 'YXZ';
    this.body.rotation.y = PI / 2;
    this.lean.add(this.body);
    const model = SkeletonUtils.clone(PA.gltf.scene);
    model.scale.set(h * b ** 0.6, h, h * b ** 0.5);
    this.body.add(model);
    this.model = model;
    this.bones = {};
    model.traverse((o) => { if (o.isBone) this.bones[o.name] = o; });

    // الخامات: الجلد + الطقم في خامة واحدة، والشعر بلون الشخصية
    const kit = kitFor(info.team, gk ? 0 : 1, opts.skin);
    const number = info.slot === 0 ? 1 : [0, 4, 5, 10, 9][info.slot] || 7;
    const skin = new THREE.Color(look.skin);
    const tint = new THREE.Color(clamp(skin.r / SKIN_REF.r, 0.15, 1.15), clamp(skin.g / SKIN_REF.g, 0.12, 1.15), clamp(skin.b / SKIN_REF.b, 0.1, 1.15));
    this.mats = [];
    const hairKey = HAIR[look.hair] === undefined ? 'buzzed' : HAIR[look.hair];
    model.traverse((o) => {
      if (o.name.startsWith('hair_')) {
        const want = o.name === `hair_${hairKey}` || (o.name === 'hair_beard' && look.acc === 'beard');
        o.visible = want;
      }
      if (!o.isMesh) return;
      const mname = o.material.name || '';
      if (/Superhero/i.test(mname)) {
        o.material = kitMaterial(o.material, kit, { skinTint: tint, boots: look.boots, gloves: gk || look.acc === 'gloves', glove: gk ? '#f2f2f2' : look.accColor, numTex: numberTexture(number, opts.preview ? '' : info.name) });
      } else if (/Hair/i.test(mname)) {
        o.material = o.material.clone();
        o.material.color = new THREE.Color(look.hairColor).multiplyScalar(1.25);
      } else return;
      this.mats.push(o.material);
    });

    // الحركات
    this.mixer = new THREE.AnimationMixer(model);
    this.act = {};
    for (const [n, clip] of Object.entries(PA.clips)) {
      const a = this.mixer.clipAction(clip);
      a.enabled = true; a.setEffectiveWeight(0);
      this.act[n] = a;
    }
    for (const [n] of LOCO) { const a = this.act[n]; a.setEffectiveTimeScale(0); a.play(); }
    this.layer = { state: null, stateW: 0, stateTarget: 0, stateFade: 0.2, os: null, osW: 0 };
    this.locoPhase = Math.random();
    this.idleT = Math.random() * 3;

    // مؤشرات
    if (!opts.preview) {
      const ring = new THREE.Mesh(geo('selring', () => new THREE.RingGeometry(0.58, 0.86, 40)),
        new THREE.MeshBasicMaterial({ color: '#ffe600', transparent: true, opacity: 1, depthWrite: false, toneMapped: false }));
      const edge = new THREE.Mesh(geo('seledge', () => new THREE.RingGeometry(0.86, 0.95, 40)), new THREE.MeshBasicMaterial({ color: '#1a1a00', transparent: true, opacity: 0.55, depthWrite: false }));
      ring.add(edge);
      ring.rotation.x = -PI / 2; ring.position.y = 0.03;
      this.root.add(ring); this.ring = ring;
      const arrow = new THREE.Mesh(geo('arrow', () => new THREE.ConeGeometry(0.22, 0.42, 4)), new THREE.MeshBasicMaterial({ color: '#ffe600', toneMapped: false }));
      arrow.rotation.x = PI; arrow.position.y = 2.35 * h;
      this.root.add(arrow); this.arrow = arrow;
      const tring = new THREE.Mesh(geo('teamring', () => new THREE.RingGeometry(0.5, 0.58, 32)),
        new THREE.MeshBasicMaterial({ color: team.color, transparent: true, opacity: 0.55, depthWrite: false }));
      tring.rotation.x = -PI / 2; tring.position.y = 0.025;
      this.root.add(tring); this.teamRing = tring;
      this.setSelected(!!opts.local);
    }
    if (!opts.preview && (info.human || opts.local)) {
      const label = makeLabel(opts.local ? `⭐ ${info.name}` : info.name, opts.local ? '#fff27a' : info.human ? '#ffffff' : '#dfe6ee', info.human ? 'rgba(0,0,0,0.55)' : 'rgba(0,0,0,0.3)');
      label.position.y = 2.1 * h;
      if (!info.human && !opts.local) label.scale.multiplyScalar(0.8);
      this.root.add(label); this.label = label;
    }
    this.stars = new THREE.Group();
    for (let i = 0; i < 3; i++) {
      const st = new THREE.Mesh(geo('star', () => new THREE.OctahedronGeometry(0.07)), new THREE.MeshBasicMaterial({ color: '#ffe14d' }));
      st.position.set(Math.cos((i * 2 * PI) / 3) * 0.3, 0, Math.sin((i * 2 * PI) / 3) * 0.3);
      this.stars.add(st);
    }
    this.stars.visible = false;
    this.root.add(this.stars);

    this.prevState = -1;
    this.stateTime = 0;
    this.kickT = 99;
    this.lastFace = 0;
    this.turnRate = 0;
    this.headYaw = 0;
    this.leanRoll = 0;
  }

  // ---------- طبقات الحركة ----------
  // حركة حالة مستمرة (انزلاق، سقوط، احتفال…)
  setState(name, { fade = 0.18, loop = true, from = 0, speed = 1 } = {}) {
    const L = this.layer;
    if (L.state && L.state.name === name) return;
    if (L.state) L.prevState = L.state; // يتلاشى تدريجياً
    if (!name || !this.act[name]) { L.state = null; L.stateTarget = 0; L.stateFade = fade; return; }
    const a = this.act[name];
    a.reset();
    a.setLoop(loop ? THREE.LoopRepeat : THREE.LoopOnce, Infinity);
    a.clampWhenFinished = true;
    a.time = from;
    a.setEffectiveTimeScale(speed);
    a.play();
    L.state = { name, a };
    L.stateTarget = 1; L.stateFade = fade;
    if (L.prevState && L.prevState.name !== name) L.prevW = L.stateW; else L.prevW = 0;
    L.stateW = 0;
  }
  // حركة لمرة واحدة فوق كل شيء (ركلة، رمية، اصطدام)
  oneShot(name, { from = 0, to = null, speed = 1, fadeIn = 0.06, fadeOut = 0.22, hold = null } = {}) {
    const a = this.act[name];
    if (!a) return;
    const L = this.layer;
    const same = L.os && L.os.a === a;
    if (L.os && !same) { L.osOld = { a: L.os.a, w: L.osW }; }
    a.reset();
    a.setLoop(THREE.LoopOnce, 1);
    a.clampWhenFinished = true;
    a.time = from;
    a.setEffectiveTimeScale(speed);
    a.play();
    L.os = { name, a, to: to ?? a.getClip().duration, fadeIn, fadeOut, hold, ending: false };
    if (!same && !L.osOld) L.osW = Math.min(L.osW, 0.5);
  }

  updateLayers(dt, speed) {
    const L = this.layer;
    // الطبقة العليا
    if (L.os) {
      const os = L.os;
      if (os.hold != null && os.a.time >= os.hold) { os.a.time = os.hold; os.a.setEffectiveTimeScale(0); }
      if (!os.ending && os.a.time >= os.to - 1e-3) os.ending = true;
      L.osW = os.ending ? Math.max(0, L.osW - dt / os.fadeOut) : Math.min(1, L.osW + dt / os.fadeIn);
      if (os.ending && L.osW <= 0) { os.a.stop(); L.os = null; }
      else os.a.setEffectiveWeight(L.osW);
    }
    if (L.osOld) {
      L.osOld.w -= dt / 0.08;
      if (L.osOld.w <= 0) { if (!L.os || L.os.a !== L.osOld.a) L.osOld.a.stop(); L.osOld = null; } else L.osOld.a.setEffectiveWeight(L.osOld.w * (1 - L.osW));
    }
    const top = L.os ? L.osW : 0;
    // طبقة الحالة
    L.stateW += (L.stateTarget > L.stateW ? 1 : -1) * dt / L.stateFade;
    L.stateW = clamp(L.stateW, 0, 1);
    if (L.state) L.state.a.setEffectiveWeight(L.stateW * (1 - top));
    if (L.prevState) {
      L.prevW -= dt / L.stateFade;
      if (L.prevW <= 0 || (L.state && L.prevState.a === L.state.a)) { if (!L.state || L.prevState.a !== L.state.a) L.prevState.a.stop(); L.prevState = null; }
      else L.prevState.a.setEffectiveWeight(L.prevW * (1 - top));
    }
    if (!L.state && L.stateW <= 0 && L.stateTarget === 0) { /* لا شيء */ }
    // الجري: مزج حسب السرعة مع مزامنة الخطوات
    const base = (1 - Math.max(L.state ? L.stateW : 0, L.prevState ? L.prevW : 0)) * (1 - top);
    let w = [0, 0, 0, 0];
    if (speed <= LOCO[1][1]) { const k = sm(0.15, LOCO[1][1], speed); w = [1 - k, k, 0, 0]; }
    else if (speed <= LOCO[2][1]) { const k = (speed - LOCO[1][1]) / (LOCO[2][1] - LOCO[1][1]); w = [0, 1 - k, k, 0]; }
    else { const k = clamp((speed - LOCO[2][1]) / (LOCO[3][1] - LOCO[2][1]), 0, 1); w = [0, 0, 1 - k, k]; }
    // تردد الخطوة المطلوب
    let freq = 0;
    for (let i = 1; i < 4; i++) { const [n, sp] = LOCO[i]; const d = this.act[n].getClip().duration; freq += w[i] * (1 / d) * clamp(speed / sp, 0.6, 1.45); }
    const mw = w[1] + w[2] + w[3];
    if (mw > 0) this.locoPhase = (this.locoPhase + dt * freq / mw) % 1;
    this.idleT += dt;
    LOCO.forEach(([n], i) => {
      const a = this.act[n];
      const d = a.getClip().duration;
      a.time = i === 0 ? this.idleT % d : this.locoPhase * d;
      a.setEffectiveWeight(w[i] * base);
    });
    this.mixer.update(dt);
  }

  // توجيه عظمة نحو اتجاه في العالم (بعد الحركة) — للحارس والاحتفالات
  aimBone(name, child, dirWorld, weight = 1) {
    const b = this.bones[name], c = this.bones[child];
    if (!b || !c || weight <= 0) return;
    b.updateWorldMatrix(true, false);
    const from = c.getWorldPosition(_v).sub(b.getWorldPosition(_v2)).normalize();
    _q.setFromUnitVectors(from, dirWorld.clone().normalize());
    const parentQ = b.parent.getWorldQuaternion(_q2);
    const worldQ = b.getWorldQuaternion(new THREE.Quaternion());
    const target = _q.multiply(worldQ);
    const local = parentQ.invert().multiply(target);
    b.quaternion.slerp(local, weight);
    b.updateMatrixWorld(true);
  }
  // دوران إضافي لعظمة حول محور في العالم
  twistBone(name, axisWorld, angle) {
    const b = this.bones[name];
    if (!b || !angle) return;
    const parentQ = b.parent.getWorldQuaternion(_q2);
    _q.setFromAxisAngle(axisWorld, angle);
    const pInv = parentQ.clone().invert();
    b.quaternion.premultiply(pInv.multiply(_q).multiply(parentQ));
  }

  // ---------- التحديث كل إطار ----------
  update(dt, s, time) {
    dt = Math.min(dt, 0.1);
    const speed = Math.hypot(s.vx, s.vy);
    const stateChanged = s.state !== this.prevState || (s.state === STATE.CELEBRATE && s.emote !== this.prevEmote);
    if (stateChanged) {
      const was = this.prevState;
      this.prevState = s.state; this.prevEmote = s.emote; this.stateTime = 0;
      this.enterState(s, was);
    }
    this.stateTime += dt;
    this.kickT += dt;
    const st = this.stateTime;

    const dFace = Math.atan2(Math.sin(s.face - this.lastFace), Math.cos(s.face - this.lastFace));
    this.turnRate += ((dFace / Math.max(dt, 1e-3)) - this.turnRate) * Math.min(1, dt * 8);
    this.lastFace = s.face;
    this.root.position.set(s.x, 0, -s.y);
    this.root.rotation.set(0, s.face, 0);
    this.body.position.set(0, 0, 0);
    this.body.rotation.set(0, PI / 2, 0, 'YXZ');
    this.stars.visible = false;

    // وضعيات مؤقتة أثناء الحالة العادية
    if (s.state === STATE.NORMAL || s.state === STATE.STUMBLE) {
      const L = this.layer;
      // شحن التسديدة: نوقف الحركة عند لحظة الرجوع للخلف
      if (s.hold > 0 && this.kickT > 0.4 && !s.throwIn) {
        if (!L.os || L.os.name !== 'charge') { this.oneShot('kick', { from: 0.18, hold: 0.42, speed: 1.4, fadeIn: 0.12 }); L.os.name = 'charge'; }
      } else if (L.os && L.os.name === 'charge' && this.kickT > 0.4) {
        L.os.ending = true;
      }
    }
    if (s.state === STATE.CELEBRATE) this.celebrateMotion(s, st, time);

    this.updateLayers(dt, s.state === STATE.NORMAL || s.state === STATE.STUMBLE ? speed : 0);
    this.model.updateMatrixWorld(true);

    // ميلان في المنعطفات + تقدم الجذع عند الركض
    const wantRoll = s.state === STATE.NORMAL ? clamp(-this.turnRate * speed * 0.011, -0.32, 0.32) : 0;
    this.leanRoll += (wantRoll - this.leanRoll) * Math.min(1, dt * 7);
    this.lean.rotation.set(this.leanRoll, 0, 0);

    // النظر نحو الكرة
    if (s.ballX != null && (s.state === STATE.NORMAL) && !this.layer.os) {
      let rel = Math.atan2(s.ballY - s.y, s.ballX - s.x) - s.face;
      rel = Math.atan2(Math.sin(rel), Math.cos(rel));
      const target = clamp(rel, -1.2, 1.2) * 0.7;
      this.headYaw += (target - this.headYaw) * Math.min(1, dt * 6);
    } else this.headYaw *= Math.max(0, 1 - dt * 6);
    if (Math.abs(this.headYaw) > 0.01) {
      _v.set(0, 1, 0);
      this.twistBone('neck_01', _v, this.headYaw * 0.4);
      this.twistBone('Head', _v, this.headYaw * 0.6);
    }

    // وضعيات إجرائية مكملة
    const up = _v.set(0, 1, 0);
    if (s.gkHold && s.state === STATE.NORMAL) {
      // الحارس يحتضن الكرة
      const fwd = new THREE.Vector3(Math.cos(s.face), -0.25, -Math.sin(s.face));
      for (const sd of ['l', 'r']) { this.aimBone(`upperarm_${sd}`, `lowerarm_${sd}`, fwd.clone().add(new THREE.Vector3(0, -0.9, 0)), 0.8); this.aimBone(`lowerarm_${sd}`, `hand_${sd}`, new THREE.Vector3(Math.cos(s.face + (sd === 'l' ? -1.3 : 1.3)), 0.15, -Math.sin(s.face + (sd === 'l' ? -1.3 : 1.3))), 0.85); }
    }
    // رمية التماس: الكرة بكلتا اليدين خلف الرأس ثم دفعها للأمام
    this.throwT = (this.throwT ?? 9) + dt;
    if ((s.throwIn || this.throwT < 0.4) && s.state === STATE.NORMAL) {
      const rel = s.throwIn && this.throwT >= 0.4 ? 0 : Math.min(1, this.throwT / 0.25);
      const f = new THREE.Vector3(Math.cos(s.face), 0, -Math.sin(s.face));
      const upArm = new THREE.Vector3(0, 1, 0).addScaledVector(f, -0.25 + rel * 1.2);
      const fore = new THREE.Vector3(0, 0.35 - rel * 0.2, 0).addScaledVector(f, -1 + rel * 2.2);
      const w = s.throwIn ? 1 : 1 - Math.max(0, (this.throwT - 0.25) / 0.15);
      for (const sd of ['l', 'r']) {
        const lat = new THREE.Vector3(-f.z, 0, f.x).multiplyScalar(sd === 'l' ? 0.18 : -0.18);
        this.aimBone(`upperarm_${sd}`, `lowerarm_${sd}`, upArm.clone().add(lat), w);
        this.aimBone(`lowerarm_${sd}`, `hand_${sd}`, fore.clone().sub(lat), w);
      }
    }
    if (s.state === STATE.DIVE) {
      // الارتماء: الجسم يميل جانبياً والذراعان ممدودتان فوق الرأس
      const lat = -Math.sin(s.face) * s.vx + Math.cos(s.face) * s.vy;
      const side = lat >= 0 ? 1 : -1;
      const k = sm(0, 0.16, st);
      const lift = Math.sin(clamp(st / 0.7, 0, 1) * PI);
      this.lean.rotation.x = -side * 1.3 * k;
      this.lean.position.y = lift * 0.45;
      this.lean.position.z = -side * 0.35 * k;
      this.model.updateMatrixWorld(true);
      const headDir = this.bones.Head.getWorldPosition(new THREE.Vector3()).sub(this.bones.spine_02.getWorldPosition(new THREE.Vector3())).normalize();
      for (const sd of ['l', 'r']) { this.aimBone(`upperarm_${sd}`, `lowerarm_${sd}`, headDir, k); this.aimBone(`lowerarm_${sd}`, `hand_${sd}`, headDir, k); }
    } else { this.lean.position.set(0, 0, 0); }
    if (s.state === STATE.DOWN) {
      this.stars.visible = st > 0.5;
      this.stars.position.set(-0.6, 0.35, 0);
      this.stars.rotation.y = time * 5;
    }
    if (s.state === STATE.CELEBRATE) this.celebratePost(s, st, time, up);

    if (this.arrow && this.selected) { this.arrow.position.y = 2.35 * this.h + Math.sin(time * 5) * 0.08; this.arrow.rotation.y = time * 2; }
    if (this.ring && this.selected) this.ring.material.opacity = 0.85 + Math.sin(time * 6) * 0.15;
  }

  enterState(s, was) {
    switch (s.state) {
      case STATE.SLIDE: this.oneShot('slide_start', { from: 0.15, to: 0.8, speed: 1.6, fadeIn: 0.05, fadeOut: 0.1 }); this.setState('slide_loop', { fade: 0.25 }); break;
      case STATE.DOWN: this.setState('knockback', { loop: false, fade: 0.08, from: 0.05, speed: 1.2 }); break;
      case STATE.DIVE: this.setState('jump_loop', { fade: 0.1 }); break;
      case STATE.STUMBLE: this.oneShot('hit', { from: 0.05, speed: 1.4, fadeOut: 0.25 }); this.setState(null); break;
      case STATE.SAD: this.setState(this.info.slot % 2 ? 'no' : 'crouch', { fade: 0.4 }); break;
      case STATE.CELEBRATE: {
        const e = s.emote;
        const map = { 1: 'slide_loop', 2: 'jump_start', 3: 'dance', 4: 'jog', 5: 'jump_start', 6: 'jump_loop' };
        const name = map[e] === undefined ? 'yes' : map[e];
        this.setState(name, { fade: 0.25, loop: e !== 2 && e !== 5, from: e === 5 || e === 2 ? 0.25 : 0 });
        this.siuDone = false;
        break;
      }
      default:
        if (was === STATE.SLIDE) this.oneShot('slide_exit', { from: 0.1, speed: 1.6, fadeIn: 0.05 });
        if (was === STATE.DOWN) this.oneShot('getup', { from: 0.6, speed: 2.2, fadeIn: 0.1 });
        this.setState(null, { fade: 0.22 });
    }
  }

  // احتفالات: الجزء الذي يغيّر الحركة قبل المزج
  celebrateMotion(s, st, time) {
    const e = s.emote;
    if ((e === 2 || e === 5) && st > 0.9 && this.layer.state && this.layer.state.name === 'jump_start') this.setState(e === 5 ? 'jump_land' : 'yes', { fade: 0.15, loop: e !== 5, from: 0 });
  }
  // احتفالات: لمسات بعد المزج (حركة الجسم كاملاً + الذراعان)
  celebratePost(s, st, time, up) {
    const e = s.emote;
    const B = this.body;
    const armsTo = (dirFn, w = 1) => { for (const sd of ['l', 'r']) { const d = dirFn(sd); this.aimBone(`upperarm_${sd}`, `lowerarm_${sd}`, d, w); this.aimBone(`lowerarm_${sd}`, `hand_${sd}`, d, w * 0.8); } };
    const side = (sd) => { const a = this.root.rotation.y + (sd === 'l' ? PI / 2 : -PI / 2); return new THREE.Vector3(Math.cos(a), 0.15, -Math.sin(a)); };
    switch (e) {
      case 1: { // انزلاق على الركبتين والذراعان مفتوحتان
        const k = Math.min(1, st / 1.0);
        B.position.x = 2.6 * (1 - (1 - k) * (1 - k));
        this.model.updateMatrixWorld(true);
        armsTo((sd) => side(sd).add(new THREE.Vector3(0, 0.5, 0)), 0.9);
        break;
      }
      case 2: { // شقلبة
        if (st < 0.9) { const k = st / 0.9; B.rotation.x = -k * PI * 2; B.position.y = Math.sin(k * PI) * 1.0; }
        else armsTo(() => up.clone(), 0.85);
        break;
      }
      case 4: { // الطائرة: جري دائري والذراعان كالأجنحة
        const a = st * 2.4;
        B.position.x = Math.sin(a) * 1.6;
        B.position.z = -(1 - Math.cos(a)) * 1.6;
        B.rotation.y = PI / 2 + a;
        this.model.updateMatrixWorld(true);
        armsTo(side, 1);
        break;
      }
      case 5: { // سيييو: قفزة مع دوران ثم الهبوط بذراعين للأسفل
        if (st < 0.9) { B.rotation.y = PI / 2 + Math.min(1, st / 0.8) * PI; }
        else {
          B.rotation.y = PI / 2 + PI;
          this.model.updateMatrixWorld(true);
          armsTo((sd) => side(sd).multiplyScalar(0.7).add(new THREE.Vector3(0, -1, 0)), 0.8);
          if (!this.siuDone && st > 1.0) { this.siuDone = true; this.onSiu && this.onSiu(); }
        }
        break;
      }
      case 6: { // قفز جماعي
        B.position.y = Math.abs(Math.sin(time * 5 + this.info.slot)) * 0.25;
        this.model.updateMatrixWorld(true);
        armsTo(() => up.clone(), 0.9);
        break;
      }
    }
  }

  // ركلة/تمريرة/رأسية/رمية تماس — تُستدعى عند حدث الركل
  triggerKick(header, isThrow, kind, pw = 0.6) {
    this.kickT = 0;
    if (isThrow) { this.throwT = 0; return; }
    if (header) { this.oneShot('jump_start', { from: 0.3, to: 0.95, speed: 1.6, fadeIn: 0.05, fadeOut: 0.25 }); return; }
    // نبدأ قبيل لحظة لمس الكرة (0.567ث في المقطع) لتظهر الضربة فوراً
    const clip = kind === 'pass' ? 'kick_c' : KICKS[Math.floor(Math.random() * KICKS.length)];
    const fast = kind === 'pass' ? 1.5 : 1.2 + (1 - pw) * 0.3;
    this.oneShot(clip, { from: 0.4, to: 0.95, speed: fast, fadeIn: 0.05, fadeOut: 0.22 });
  }

  setSelected(on) {
    this.selected = on;
    if (this.ring) this.ring.visible = on;
    if (this.arrow) this.arrow.visible = on;
    if (this.teamRing) this.teamRing.visible = !on;
  }

  dispose() {
    this.mixer.stopAllAction();
    this.mixer.uncacheRoot(this.model);
    for (const m of this.mats) m.dispose();
    this.root.traverse((o) => { if (o.isSprite) { o.material.map.dispose(); o.material.dispose(); } });
  }
}
