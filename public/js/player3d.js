// نموذج اللاعب ثلاثي الأبعاد (مبني من أشكال بسيطة) مع رسوم متحركة إجرائية
import * as THREE from 'three';
import { mergeGeometries } from 'three/addons/utils/BufferGeometryUtils.js';
import { STATE, TEAMS } from '/shared/constants.js';
import { charOf } from '/shared/characters.js';
import { kitFor, jerseyTexture, bandTexture } from './skins.js';

const PI = Math.PI;
const matCache = new Map();
function mat(color, rough = 0.75, extra = {}) {
  const key = color + rough + JSON.stringify(extra);
  if (!matCache.has(key)) matCache.set(key, new THREE.MeshStandardMaterial({ color, roughness: rough, metalness: 0, ...extra }));
  return matCache.get(key);
}
const geoCache = new Map();
function geo(key, make) {
  if (!geoCache.has(key)) geoCache.set(key, make());
  return geoCache.get(key);
}
const capsule = (r, l) => geo(`cap${r}_${l}`, () => new THREE.CapsuleGeometry(r, l, 6, 14));
const sphere = (r, w = 16, h = 12) => geo(`sph${r}_${w}`, () => new THREE.SphereGeometry(r, w, h));
const box = (x, y, z) => geo(`box${x}_${y}_${z}`, () => new THREE.BoxGeometry(x, y, z));

function sm(a, b, v) { const t = Math.min(1, Math.max(0, (v - a) / (b - a))); return t * t * (3 - 2 * t); }
function shade(m) { m.castShadow = true; m.receiveShadow = false; return m; }
const lathe = (key, pts, seg = 18) => geo(`lathe_${key}_${seg}_${pts.join()}`, () => new THREE.LatheGeometry(pts.map(([r, y]) => new THREE.Vector2(r, y)), seg));
function clothMat(map) {
  return new THREE.MeshPhysicalMaterial({ map, roughness: 0.72, sheen: 0.8, sheenRoughness: 0.55, sheenColor: new THREE.Color('#ffffff').multiplyScalar(0.35) });
}
function skinMat(color) {
  const k = 'skin' + color;
  if (!matCache.has(k)) matCache.set(k, new THREE.MeshPhysicalMaterial({ color, roughness: 0.52, sheen: 0.35, sheenRoughness: 0.5, sheenColor: new THREE.Color('#ff8a70') }));
  return matCache.get(k);
}
// حذاء كرة قدم: مقطع جانبي مبثوق بحواف ناعمة
function bootGeo() {
  return geo('boot', () => {
    const sh = new THREE.Shape();
    sh.moveTo(-0.05, 0); sh.lineTo(0.17, 0);
    sh.quadraticCurveTo(0.215, 0.002, 0.212, 0.03);
    sh.quadraticCurveTo(0.2, 0.058, 0.13, 0.066);
    sh.lineTo(0.04, 0.095); sh.lineTo(-0.045, 0.1);
    sh.quadraticCurveTo(-0.066, 0.05, -0.05, 0);
    const g = new THREE.ExtrudeGeometry(sh, { depth: 0.066, bevelEnabled: true, bevelThickness: 0.012, bevelSize: 0.01, bevelSegments: 3, curveSegments: 10 });
    g.translate(0, 0, -0.033);
    return g;
  });
}

export function makeLabel(text, color = '#ffffff', bg = 'rgba(0,0,0,0.45)', big = false) {
  const c = document.createElement('canvas');
  c.width = 256; c.height = 64;
  const g = c.getContext('2d');
  g.font = `bold ${big ? 30 : 26}px Tahoma, sans-serif`;
  const w = Math.min(250, g.measureText(text).width + 24);
  g.fillStyle = bg;
  const x = (256 - w) / 2;
  g.beginPath(); g.roundRect(x, 12, w, 40, 20); g.fill();
  g.fillStyle = color; g.textAlign = 'center'; g.textBaseline = 'middle';
  g.direction = 'rtl';
  g.fillText(text, 128, 33);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  const s = new THREE.Sprite(new THREE.SpriteMaterial({ map: t, depthTest: false, transparent: true }));
  s.scale.set(2.4, 0.6, 1);
  s.renderOrder = 10;
  return s;
}

export class Player3D {
  constructor(info, opts = {}) {
    this.info = info;
    this.c = charOf(info.char);
    const look = this.c.look;
    const team = TEAMS[info.team] || TEAMS[0];
    const gk = info.slot === 0 && !opts.preview;
    const h = look.height, b = look.build;
    this.h = h;
    this.root = new THREE.Group();
    this.body = new THREE.Bone();
    this._bake = [];
    this.root.add(this.body);
    this.hipY = 0.92 * h;
    this.body.position.y = this.hipY;

    const kit = kitFor(info.team, gk ? 0 : 1, opts.skin);
    const number = info.slot === 0 ? 1 : [0, 4, 5, 10, 9][info.slot] || 7;
    const skinM = skinMat(look.skin);
    const jerseyM = clothMat(jerseyTexture(kit, number, info.name || ''));
    const sleeveM = clothMat(bandTexture(kit.base, kit.trim, 'bottom', kit.pattern === 'pinstripes'));
    const shortsM = clothMat(bandTexture(kit.shorts, kit.shortsTrim, 'none', true));
    const socksM = clothMat(bandTexture(kit.socks, kit.sockBand, 'top'));
    const bootsM = mat(look.boots, 0.3, { metalness: 0.1 });
    const soleM = mat('#1a1a1a', 0.6);
    const hairM = mat(look.hairColor, 0.85);
    const B = b;

    // الحوض (الشورت) — مثبت على الورك
    const shortsTop = shade(new THREE.Mesh(lathe('shorts', [[0.168, -0.2], [0.176, -0.12], [0.172, -0.02], [0.162, 0.07]], 22), shortsM));
    shortsTop.scale.set(0.68 * B, 1, B);
    this.body.add(shortsTop);
    this._bake.push([shortsTop, (x, y) => [[this.torso, sm(-0.02, 0.07, y) * 0.45]]]);

    // الجذع (القميص بخامة كاملة)
    this.torso = new THREE.Bone();
    this.body.add(this.torso);
    const chest = shade(new THREE.Mesh(lathe('torso', [[0.158, -0.03], [0.152, 0.08], [0.148, 0.17], [0.165, 0.27], [0.188, 0.36], [0.2, 0.43], [0.205, 0.49], [0.185, 0.545], [0.12, 0.585], [0.055, 0.605]], 28), jerseyM));
    chest.scale.set(0.66 * B, 1, B);
    this.torso.add(chest);
    this._bake.push([chest, (x, y, z) => [[this.body, sm(0.1, -0.03, y) * 0.5], [z > 0 ? () => this.arms[1].sh : () => this.arms[0].sh, sm(0.11, 0.2, Math.abs(z)) * sm(0.38, 0.5, y) * 0.5]]]);

    // الرقبة والرأس
    this.neck = new THREE.Bone();
    this.neck.position.y = 0.57;
    this.torso.add(this.neck);
    const neckM = shade(new THREE.Mesh(lathe('neck', [[0.055, -0.04], [0.046, 0.06], [0.05, 0.13]], 14), skinM));
    this.neck.add(neckM);
    this._bake.push([neckM, (x, y) => [[this.torso, sm(0.03, -0.04, y) * 0.6], [this.head, sm(0.07, 0.13, y) * 0.5]]]);
    this.head = new THREE.Bone();
    this.head.position.y = 0.2;
    this.neck.add(this.head);
    const skull = shade(new THREE.Mesh(sphere(0.105, 28, 20), skinM));
    skull.scale.set(1.1, 1.2, 0.97); skull.position.set(-0.005, 0.015, 0);
    this.head.add(skull);
    const jaw = shade(new THREE.Mesh(sphere(0.082, 20, 14), skinM));
    jaw.scale.set(1.05, 0.85, 0.96); jaw.position.set(0.022, -0.05, 0);
    this.head.add(jaw);
    const chin = new THREE.Mesh(sphere(0.03, 12, 10), skinM);
    chin.position.set(0.085, -0.095, 0); chin.scale.set(0.9, 0.8, 1.2);
    this.head.add(chin);
    const nose = new THREE.Mesh(sphere(0.02, 12, 10), skinM);
    nose.scale.set(0.9, 1.6, 0.8); nose.position.set(0.114, -0.005, 0);
    this.head.add(nose);
    const eyeW = mat('#f4f1ea', 0.25), iris = mat('#2b1a10', 0.2), brow = mat(look.hairColor === '#f5f5f5' || look.hairColor === '#dff6ff' ? '#8a8a8a' : look.hairColor, 0.9), lip = mat('#8a4a3c', 0.5);
    for (const sd of [-1, 1]) {
      const ew = new THREE.Mesh(sphere(0.017, 12, 10), eyeW);
      ew.scale.set(0.55, 0.75, 1); ew.position.set(0.1, 0.018, sd * 0.04);
      this.head.add(ew);
      const ir = new THREE.Mesh(sphere(0.0085, 10, 8), iris);
      ir.position.set(0.108, 0.018, sd * 0.04);
      this.head.add(ir);
      const bw = new THREE.Mesh(box(0.012, 0.009, 0.038), brow);
      bw.position.set(0.106, 0.045, sd * 0.041); bw.rotation.x = sd * -0.12;
      this.head.add(bw);
      const ear = shade(new THREE.Mesh(sphere(0.028, 10, 8), skinM));
      ear.scale.set(0.55, 1, 0.45); ear.position.set(-0.005, 0.0, sd * 0.104);
      this.head.add(ear);
      const cheek = new THREE.Mesh(sphere(0.03, 10, 8), skinM);
      cheek.position.set(0.07, -0.02, sd * 0.05);
      this.head.add(cheek);
    }
    const mouth = new THREE.Mesh(box(0.008, 0.009, 0.042), lip);
    mouth.position.set(0.103, -0.058, 0);
    this.head.add(mouth);
    this.addHair(look, hairM, skinM);

    // الذراعان
    this.arms = [];
    const gloveM = gk || look.acc === 'gloves' ? mat(gk ? '#f2f2f2' : look.accColor, 0.55) : null;
    for (const sd of [-1, 1]) {
      const sh = new THREE.Bone();
      sh.position.set(0, 0.49, sd * 0.215 * B);
      this.torso.add(sh);
      const delt = shade(new THREE.Mesh(sphere(0.07 * B, 16, 12), sleeveM));
      delt.scale.set(1, 0.9, 1); delt.position.y = -0.01;
      sh.add(delt);
      this._bake.push([delt, (x, y) => [[this.torso, sm(0.0, 0.06, y) * 0.35]]]);
      const sleeve = shade(new THREE.Mesh(lathe('sleeve', [[0.058, -0.17], [0.063, -0.08], [0.066, 0.0]], 16), sleeveM));
      sleeve.scale.set(B, 1, B);
      sh.add(sleeve);
      this._bake.push([sleeve, () => []]);
      const upper = shade(new THREE.Mesh(lathe('uarm', [[0.038, -0.28], [0.046, -0.21], [0.052, -0.13], [0.05, -0.04]], 14), kit.longSleeves ? sleeveM : skinM));
      upper.scale.set(B, 1, B);
      sh.add(upper);
      this._bake.push([upper, (x, y) => [[el, sm(-0.2, -0.28, y) * 0.5]]]);
      const el = new THREE.Bone();
      el.position.y = -0.27;
      sh.add(el);
      const fore = shade(new THREE.Mesh(lathe('farm', [[0.029, -0.235], [0.036, -0.16], [0.043, -0.06], [0.04, 0.02]], 14), kit.longSleeves ? sleeveM : skinM));
      fore.scale.set(B, 1, B);
      el.add(fore);
      this._bake.push([fore, (x, y) => [[sh, sm(-0.04, 0.02, y) * 0.5]]]);
      const hand = shade(new THREE.Mesh(sphere(0.045, 14, 10), gloveM || skinM));
      hand.scale.set(gloveM ? 0.75 : 0.55, 1.15, gloveM ? 1.25 : 1);
      hand.position.y = -0.285;
      el.add(hand);
      const thumb = new THREE.Mesh(capsule(0.012, 0.03), gloveM || skinM);
      thumb.position.set(0.02, -0.265, sd * -0.02); thumb.rotation.z = 0.5;
      el.add(thumb);
      if (look.acc === 'wristbands') {
        const wb = new THREE.Mesh(geo('wb', () => new THREE.CylinderGeometry(0.036, 0.036, 0.045, 14)), mat(look.accColor, 0.7));
        wb.position.y = -0.21;
        el.add(wb);
      }
      this.arms.push({ sh, el, s: sd });
    }

    // الساقان
    this.legs = [];
    for (const sd of [-1, 1]) {
      const hip = new THREE.Bone();
      hip.position.set(0, 0, sd * 0.095 * B);
      this.body.add(hip);
      const shortLeg = shade(new THREE.Mesh(lathe('sleg', [[0.088, -0.22], [0.094, -0.12], [0.092, 0.02]], 18), shortsM));
      shortLeg.scale.set(B, 1, B);
      hip.add(shortLeg);
      this._bake.push([shortLeg, (x, y) => [[this.body, sm(-0.06, 0.02, y) * 0.5]]]);
      const thigh = shade(new THREE.Mesh(lathe('thigh', [[0.052, -0.44 * h], [0.064, -0.36], [0.078, -0.24], [0.084, -0.12]], 16), skinM));
      thigh.scale.set(B, 1, B);
      hip.add(thigh);
      this._bake.push([thigh, (x, y) => [[knee, sm(-0.34 * h, -0.44 * h, y) * 0.5]]]);
      const knee = new THREE.Bone();
      knee.position.y = -0.44 * h;
      hip.add(knee);
      const kc = new THREE.Mesh(sphere(0.05, 12, 10), skinM);
      kc.scale.set(1.05, 1, 1);
      knee.add(kc);
      this._bake.push([kc, (x, y) => [[hip, sm(-0.02, 0.05, y) * 0.5]]]);
      const shin = shade(new THREE.Mesh(lathe('shin', [[0.046, -0.1], [0.05, -0.04], [0.049, 0.01]], 14), skinM));
      shin.scale.set(B, 1, B);
      knee.add(shin);
      this._bake.push([shin, (x, y) => [[hip, sm(-0.03, 0.01, y) * 0.5]]]);
      const sock = shade(new THREE.Mesh(lathe('sock', [[0.034, -0.43 * h], [0.037, -0.36], [0.05, -0.26], [0.058, -0.17], [0.054, -0.09], [0.052, -0.06]], 16), socksM));
      sock.scale.set(B, 1, B);
      knee.add(sock);
      this._bake.push([sock, () => []]);
      const boot = shade(new THREE.Mesh(bootGeo(), bootsM));
      boot.position.set(0, -0.475 * h, 0);
      knee.add(boot);
      const sole = new THREE.Mesh(box(0.25, 0.018, 0.085), soleM);
      sole.position.set(0.075, -0.475 * h + 0.004, 0);
      knee.add(sole);
      this.legs.push({ hip, knee, s: sd });
    }

    this.bakeSkin();
    this.scale = 1;
    this.root.traverse((o) => { if (o.isMesh) o.castShadow = true; });

    // مؤشرات
    if (!opts.preview) {
      const ring = new THREE.Mesh(
        geo('selring', () => new THREE.RingGeometry(0.58, 0.86, 40)),
        new THREE.MeshBasicMaterial({ color: '#ffe600', transparent: true, opacity: 1, depthWrite: false, toneMapped: false }),
      );
      // حافة داكنة لتباين أوضح فوق العشب
      const edge = new THREE.Mesh(geo('seledge', () => new THREE.RingGeometry(0.86, 0.95, 40)), new THREE.MeshBasicMaterial({ color: '#1a1a00', transparent: true, opacity: 0.55, depthWrite: false }));
      ring.add(edge);
      ring.rotation.x = -PI / 2;
      ring.position.y = 0.03;
      this.root.add(ring);
      this.ring = ring;
      const arrow = new THREE.Mesh(geo('arrow', () => new THREE.ConeGeometry(0.22, 0.42, 4)), new THREE.MeshBasicMaterial({ color: '#ffe600', toneMapped: false }));
      arrow.rotation.x = PI;
      arrow.position.y = 2.35 * h;
      this.root.add(arrow);
      this.arrow = arrow;
      const tring = new THREE.Mesh(
        geo('teamring', () => new THREE.RingGeometry(0.5, 0.58, 32)),
        new THREE.MeshBasicMaterial({ color: team.color, transparent: true, opacity: 0.55, depthWrite: false }),
      );
      tring.rotation.x = -PI / 2;
      tring.position.y = 0.025;
      this.root.add(tring);
      this.teamRing = tring;
      this.setSelected(!!opts.local);
    }
    if (!opts.preview && (info.human || opts.local)) {
      const label = makeLabel(opts.local ? `⭐ ${info.name}` : info.name, opts.local ? '#fff27a' : info.human ? '#ffffff' : '#dfe6ee', info.human ? 'rgba(0,0,0,0.55)' : 'rgba(0,0,0,0.3)');
      label.position.y = 2.1 * h;
      if (!info.human && !opts.local) label.scale.multiplyScalar(0.8);
      this.root.add(label);
      this.label = label;
    }
    // نجوم الدوخة
    this.stars = new THREE.Group();
    for (let i = 0; i < 3; i++) {
      const st = new THREE.Mesh(geo('star', () => new THREE.OctahedronGeometry(0.07)), new THREE.MeshBasicMaterial({ color: '#ffe14d' }));
      st.position.set(Math.cos((i * 2 * PI) / 3) * 0.3, 0, Math.sin((i * 2 * PI) / 3) * 0.3);
      this.stars.add(st);
    }
    this.stars.visible = false;
    this.root.add(this.stars);

    // حالة الحركة
    this.phase = Math.random() * 10;
    this.prevState = -1;
    this.stateTime = 0;
    this.kickT = 99;
    this.kickHeader = false;
    this.celebOffset = new THREE.Vector3();
    this.lastFace = 0;
    this.turnRate = 0;
  }

  // دمج أجزاء الجسم في جسد واحد مكسو بعظام (Skinned Mesh) لتنحني المفاصل بنعومة
  bakeSkin() {
    const root = this.root;
    root.updateMatrixWorld(true);
    const bones = [this.body, this.torso, this.neck, this.head, ...this.arms.flatMap((a) => [a.sh, a.el]), ...this.legs.flatMap((l) => [l.hip, l.knee])];
    const idx = new Map(bones.map((b, i) => [b, i]));
    const byMat = new Map();
    const inv = new THREE.Matrix4().copy(root.matrixWorld).invert();
    for (const [mesh, rule] of this._bake) {
      const parent = mesh.parent;
      const g = mesh.geometry.clone();
      const pos = g.attributes.position;
      const n = pos.count;
      const si = new Uint16Array(n * 4), sw = new Float32Array(n * 4);
      for (let i = 0; i < n; i++) {
        const x = pos.getX(i), y = pos.getY(i), z = pos.getZ(i);
        const sec = rule(x, y, z).map(([b, w]) => [typeof b === 'function' ? b() : b, w]).filter(([b, w]) => w > 0.001 && idx.has(b));
        let total = 0;
        sec.slice(0, 3).forEach(([b, w], k) => { si[i * 4 + k + 1] = idx.get(b); sw[i * 4 + k + 1] = w; total += w; });
        si[i * 4] = idx.get(parent); sw[i * 4] = Math.max(0, 1 - total);
      }
      g.setAttribute('skinIndex', new THREE.Uint16BufferAttribute(si, 4));
      g.setAttribute('skinWeight', new THREE.Float32BufferAttribute(sw, 4));
      g.applyMatrix4(new THREE.Matrix4().multiplyMatrices(inv, mesh.matrixWorld));
      for (const k of Object.keys(g.attributes)) if (!['position', 'normal', 'uv', 'skinIndex', 'skinWeight'].includes(k)) g.deleteAttribute(k);
      if (!g.attributes.uv) g.setAttribute('uv', new THREE.Float32BufferAttribute(new Float32Array(n * 2), 2));
      if (g.index) { /* keep */ }
      const m = mesh.material;
      if (!byMat.has(m)) byMat.set(m, []);
      byMat.get(m).push(g);
      parent.remove(mesh);
    }
    const mats = [...byMat.keys()];
    const geos = mats.map((m) => mergeGeometries(byMat.get(m).map((x) => (x.index ? x.toNonIndexed() : x))));
    const merged = mergeGeometries(geos, true);
    const skinned = new THREE.SkinnedMesh(merged, mats);
    skinned.castShadow = true;
    skinned.frustumCulled = false;
    root.add(skinned);
    const skeleton = new THREE.Skeleton(bones);
    skinned.bind(skeleton, new THREE.Matrix4());
    this.skinned = skinned;
    this._bake = null;
  }

  addHair(look, hairM, skinM) {
    const hd = this.head;
    const cap = (s = 1.08, theta = PI / 2.1) => {
      const m = shade(new THREE.Mesh(geo(`hcap${theta}`, () => new THREE.SphereGeometry(0.13, 16, 10, 0, PI * 2, 0, theta)), hairM));
      m.scale.set(s, s * 1.08, s * 0.98);
      m.position.y = 0.01;
      m.rotation.z = 0.25;
      hd.add(m);
      return m;
    };
    switch (look.hair) {
      case 'short': cap(1.04, PI / 2.3); break;
      case 'spiky': {
        cap(1.02, PI / 2.4);
        for (let i = 0; i < 7; i++) {
          const sp = shade(new THREE.Mesh(geo('spike', () => new THREE.ConeGeometry(0.035, 0.14, 5)), hairM));
          const a = (i / 7) * PI * 2;
          sp.position.set(Math.cos(a) * 0.06 - 0.02, 0.12, Math.sin(a) * 0.06);
          sp.rotation.set(Math.sin(a) * 0.5, 0, -Math.cos(a) * 0.5 - 0.2);
          hd.add(sp);
        }
        break;
      }
      case 'mohawk': {
        for (let i = 0; i < 6; i++) {
          const m = shade(new THREE.Mesh(box(0.05, 0.1 + (i === 2 || i === 3 ? 0.04 : 0), 0.035), hairM));
          m.position.set(0.09 - i * 0.04, 0.12 - Math.abs(i - 2.5) * 0.012, 0);
          hd.add(m);
        }
        break;
      }
      case 'long': {
        cap(1.07, PI / 2);
        const bk = shade(new THREE.Mesh(box(0.08, 0.3, 0.24), hairM));
        bk.position.set(-0.1, -0.1, 0);
        hd.add(bk);
        break;
      }
      case 'afro': {
        const m = shade(new THREE.Mesh(sphere(0.17, 14, 10), hairM));
        m.position.set(-0.02, 0.07, 0);
        m.scale.set(1, 0.9, 1);
        hd.add(m);
        break;
      }
      case 'bun': {
        cap(1.04, PI / 2.2);
        const bn = shade(new THREE.Mesh(sphere(0.06, 10, 8), hairM));
        bn.position.set(-0.08, 0.13, 0);
        hd.add(bn);
        break;
      }
      case 'hood': {
        const m = shade(new THREE.Mesh(geo('hood', () => new THREE.SphereGeometry(0.15, 16, 10, PI * 0.25, PI * 1.5, 0, PI / 1.6)), mat(look.hairColor, 0.9, { side: THREE.DoubleSide })));
        m.rotation.y = PI;
        m.position.y = 0.0;
        hd.add(m);
        break;
      }
      case 'bald': break;
    }
    switch (look.acc) {
      case 'headband': {
        const t = new THREE.Mesh(geo('band', () => new THREE.TorusGeometry(0.128, 0.018, 6, 20)), mat(look.accColor, 0.6));
        t.rotation.x = PI / 2;
        t.rotation.y = 0.25;
        t.position.y = 0.05;
        hd.add(t);
        break;
      }
      case 'beard': {
        const bd = new THREE.Mesh(box(0.06, 0.07, 0.17), mat(look.accColor, 0.9));
        bd.position.set(0.09, -0.09, 0);
        hd.add(bd);
        break;
      }
      case 'mask': {
        const mk = new THREE.Mesh(box(0.03, 0.05, 0.25), mat(look.accColor, 0.4));
        mk.position.set(0.108, 0.025, 0);
        hd.add(mk);
        break;
      }
      case 'goggles': {
        for (const s of [-1, 1]) {
          const g = new THREE.Mesh(geo('gog', () => new THREE.TorusGeometry(0.032, 0.012, 6, 14)), mat(look.accColor, 0.2, { emissive: look.accColor, emissiveIntensity: 0.4 }));
          g.position.set(0.1, 0.08, s * 0.05);
          g.rotation.y = PI / 2;
          hd.add(g);
        }
        break;
      }
    }
  }

  // تحديث الرسوم المتحركة
  // s: { x, y, vx, vy, face, state, emote, hold, owner(bool), gkHold(bool), throwIn(bool), ballZ }
  update(dt, s, time) {
    const root = this.root;
    const speed = Math.hypot(s.vx, s.vy);
    if (s.state !== this.prevState || (s.state === STATE.CELEBRATE && s.emote !== this.prevEmote)) {
      this.prevState = s.state; this.prevEmote = s.emote; this.stateTime = 0;
      if (s.state === STATE.CELEBRATE && s.emote === 1) this.celebOffset.set(0, 0, 0);
    }
    this.stateTime += dt;
    this.kickT += dt;
    const st = this.stateTime;

    // الاتجاه وسرعة الدوران (للميلان)
    const dFace = Math.atan2(Math.sin(s.face - this.lastFace), Math.cos(s.face - this.lastFace));
    this.turnRate += ((dFace / Math.max(dt, 1e-3)) - this.turnRate) * Math.min(1, dt * 8);
    this.lastFace = s.face;

    root.position.set(s.x, 0, -s.y);
    root.rotation.set(0, s.face, 0);
    const B = this.body, T = this.torso;
    const [la, ra] = this.arms;
    const [ll, rl] = this.legs;
    // إعادة الضبط
    B.position.set(0, this.hipY, 0); B.rotation.set(0, 0, 0);
    T.rotation.set(0, 0, 0);
    this.neck.rotation.set(0, 0, 0); this.head.rotation.set(0, 0, 0);
    for (const a of this.arms) { a.sh.rotation.set(0, 0, 0); a.el.rotation.set(0, 0, 0); }
    for (const l of this.legs) { l.hip.rotation.set(0, 0, 0); l.knee.rotation.set(0, 0, 0); }
    this.stars.visible = false;

    const amp = Math.min(1, speed / 7.5);
    const sprint = speed > 6.2;
    this.phase += dt * (2 * PI) * (speed > 0.3 ? 0.85 + speed * 0.16 : 0);
    const ph = this.phase;

    const armsIdle = () => {
      la.sh.rotation.x = 0.12; ra.sh.rotation.x = -0.12;
      la.el.rotation.z = 0.25; ra.el.rotation.z = 0.25;
    };

    switch (s.state) {
      case STATE.NORMAL:
      case STATE.STUMBLE: {
        if (speed > 0.25) {
          const a = 0.35 + amp * 0.75;
          const sn = Math.sin(ph);
          ll.hip.rotation.z = sn * a;
          rl.hip.rotation.z = -sn * a;
          ll.knee.rotation.z = -Math.max(0, -Math.cos(ph)) * (0.4 + amp * 1.3) - 0.1;
          rl.knee.rotation.z = -Math.max(0, Math.cos(ph)) * (0.4 + amp * 1.3) - 0.1;
          la.sh.rotation.z = -sn * a * 0.9; ra.sh.rotation.z = sn * a * 0.9;
          la.sh.rotation.x = 0.1; ra.sh.rotation.x = -0.1;
          la.el.rotation.z = 0.5 + amp * 0.9; ra.el.rotation.z = 0.5 + amp * 0.9;
          B.position.y = this.hipY - 0.03 * amp + Math.abs(Math.cos(ph)) * 0.07 * amp;
          T.rotation.z = -(0.08 + amp * (sprint ? 0.28 : 0.16));
          // ميلان في المنعطفات
          B.rotation.x = Math.max(-0.35, Math.min(0.35, -this.turnRate * speed * 0.012));
          T.rotation.y = -sn * 0.18 * amp;
          B.rotation.y = sn * 0.1 * amp;
        } else {
          // وقفة تنفس
          const br = Math.sin(time * 2 + this.phase) * 0.02;
          T.rotation.z = -0.03 + br;
          armsIdle();
          ll.hip.rotation.x = -0.05; rl.hip.rotation.x = 0.05;
          ll.knee.rotation.z = -0.08; rl.knee.rotation.z = -0.08;
          B.position.y = this.hipY - 0.02 + br * 0.3;
        }
        if (s.state === STATE.STUMBLE) { T.rotation.z = -0.5; B.position.y -= 0.12; ll.knee.rotation.z = -0.7; rl.knee.rotation.z = -0.7; }
        // شحن التسديدة
        if (s.hold > 0 && this.kickT > 0.4) {
          const k = Math.min(1, s.hold);
          rl.hip.rotation.z = -0.3 - k * 0.6;
          rl.knee.rotation.z = -0.4 - k * 0.9;
          T.rotation.z = 0.05 + k * 0.1;
          la.sh.rotation.x = 0.6 * k; ra.sh.rotation.x = -0.6 * k;
        }
        if (s.gkHold) {
          la.sh.rotation.z = 1.2; ra.sh.rotation.z = 1.2; la.el.rotation.z = 0.5; ra.el.rotation.z = 0.5;
          la.sh.rotation.x = -0.25; ra.sh.rotation.x = 0.25;
        }
        if (s.throwIn) {
          la.sh.rotation.z = PI - 0.2; ra.sh.rotation.z = PI - 0.2; la.el.rotation.z = -0.9; ra.el.rotation.z = -0.9;
          la.sh.rotation.x = -0.15; ra.sh.rotation.x = 0.15;
        }
        break;
      }
      case STATE.SLIDE: {
        B.position.y = 0.32;
        B.rotation.z = 1.05;
        rl.hip.rotation.z = 0.35; rl.knee.rotation.z = 0;
        ll.hip.rotation.z = -0.2; ll.knee.rotation.z = -1.4;
        la.sh.rotation.z = -0.6; ra.sh.rotation.z = -0.9; la.sh.rotation.x = 0.5; ra.sh.rotation.x = -0.5;
        this.neck.rotation.z = -0.6;
        break;
      }
      case STATE.DOWN: {
        const fall = Math.min(1, st / 0.25);
        B.position.y = this.hipY - (this.hipY - 0.18) * fall;
        B.rotation.z = 1.5 * fall;
        la.sh.rotation.x = 0.9; ra.sh.rotation.x = -0.9; la.sh.rotation.z = 1.2; ra.sh.rotation.z = 0.8;
        ll.hip.rotation.z = 0.3; rl.hip.rotation.z = 0.1; ll.knee.rotation.z = -0.7;
        this.stars.visible = st > 0.3;
        this.stars.position.set(-0.8, 0.45, 0);
        this.stars.rotation.y = time * 5;
        break;
      }
      case STATE.DIVE: {
        // الارتماء جانبياً
        const lat = -Math.sin(s.face) * s.vx + Math.cos(s.face) * s.vy; // موجب = يسار
        const side = lat >= 0 ? -1 : 1;
        const k = Math.min(1, st / 0.18);
        const up = Math.sin(Math.min(PI, (st / 0.7) * PI));
        B.position.y = this.hipY * (1 - k * 0.55) + up * 0.45;
        B.rotation.x = side * 1.35 * k;
        la.sh.rotation.z = PI * 0.95; ra.sh.rotation.z = PI * 0.95;
        ll.hip.rotation.x = 0.2 * side; rl.hip.rotation.x = 0.2 * side; ll.knee.rotation.z = -0.4;
        break;
      }
      case STATE.CELEBRATE: this.celebrate(s, st, dt, time); break;
      case STATE.SAD: {
        T.rotation.z = -0.2 - Math.sin(time * 0.8) * 0.05;
        this.neck.rotation.z = -0.4;
        la.sh.rotation.z = 2.6; ra.sh.rotation.z = 2.6; la.sh.rotation.x = -0.9; ra.sh.rotation.x = 0.9;
        la.el.rotation.z = 2.2; ra.el.rotation.z = 2.2;
        if (this.info.slot % 2 === 0 && st > 1.2) {
          // يجثو على ركبتيه
          const k = Math.min(1, (st - 1.2) / 0.4);
          B.position.y = this.hipY - 0.42 * k;
          ll.hip.rotation.z = 0.1; rl.hip.rotation.z = 0.1; ll.knee.rotation.z = -1.5 * k; rl.knee.rotation.z = -1.5 * k;
        }
        break;
      }
    }

    // النظر نحو الكرة
    if (s.ballX != null && (s.state === STATE.NORMAL || s.state === STATE.STUMBLE)) {
      let rel = Math.atan2(s.ballY - s.y, s.ballX - s.x) - s.face;
      rel = Math.atan2(Math.sin(rel), Math.cos(rel));
      const target = Math.max(-1.1, Math.min(1.1, rel)) * 0.75;
      this.headYaw = (this.headYaw || 0) + (target - (this.headYaw || 0)) * Math.min(1, dt * 6);
      this.head.rotation.y = this.headYaw;
    }

    // الركلة
    if (this.kickT < 0.45 && (s.state === STATE.NORMAL)) {
      const k = this.kickT;
      if (this.kickHeader) {
        const n = Math.sin(Math.min(1, k / 0.3) * PI);
        this.neck.rotation.z = -0.9 * n; T.rotation.z = -0.3 * n;
        B.position.y += n * 0.25;
      } else if (s.throwIn || this.kickThrow) {
        const n = Math.min(1, k / 0.25);
        la.sh.rotation.z = PI - 0.2 - n * 2.2; ra.sh.rotation.z = PI - 0.2 - n * 2.2;
      } else {
        let hz, kz;
        if (k < 0.1) { const n = k / 0.1; hz = -0.7 * n; kz = -1.4 * n; }
        else if (k < 0.2) { const n = (k - 0.1) / 0.1; hz = -0.7 + 2.2 * n; kz = -1.4 + 1.4 * n; }
        else { const n = Math.min(1, (k - 0.2) / 0.25); hz = 1.5 * (1 - n); kz = -0.3 * (1 - n); }
        rl.hip.rotation.z = hz; rl.knee.rotation.z = kz;
        ll.knee.rotation.z = -0.25;
        la.sh.rotation.x = 0.9; ra.sh.rotation.x = -0.5; la.sh.rotation.z = 0.5;
        T.rotation.z = 0.12 + (k > 0.1 ? -0.25 : 0);
      }
    }
    // مزج ناعم بين الوضعيات (لا قفزات مفاجئة في الحركة)
    this.smoothPose(dt, s.state);

    // المؤشرات
    if (this.arrow && this.selected) { this.arrow.position.y = 2.35 * this.h + Math.sin(time * 5) * 0.08; this.arrow.rotation.y = time * 2; }
    if (this.ring && this.selected) this.ring.material.opacity = 0.85 + Math.sin(time * 6) * 0.15;
  }

  celebrate(s, st, dt, time) {
    const B = this.body, T = this.torso;
    const [la, ra] = this.arms;
    const [ll, rl] = this.legs;
    const armsUp = (k = 1) => { la.sh.rotation.z = PI * 0.9 * k; ra.sh.rotation.z = PI * 0.9 * k; la.sh.rotation.x = -0.35 * k; ra.sh.rotation.x = 0.35 * k; };
    const armsWide = (up = 0.4) => { la.sh.rotation.x = 1.45; ra.sh.rotation.x = -1.45; la.sh.rotation.z = up; ra.sh.rotation.z = up; };
    switch (s.emote) {
      case 1: { // زحلقة على الركبتين
        const slide = Math.max(0, 1 - st / 1.0);
        B.position.x = 2.6 * (1 - slide * slide);
        B.position.y = this.hipY - 0.4;
        ll.hip.rotation.z = 0.0; rl.hip.rotation.z = 0.0; ll.knee.rotation.z = -1.55; rl.knee.rotation.z = -1.55;
        T.rotation.z = 0.45 + Math.sin(time * 3) * 0.05;
        armsWide(0.6 + Math.sin(time * 4) * 0.1);
        this.neck.rotation.z = 0.4;
        break;
      }
      case 2: { // شقلبة خلفية
        const k = Math.min(1, st / 0.9);
        if (st < 0.9) {
          B.rotation.z = k * PI * 2;
          B.position.y = this.hipY + Math.sin(k * PI) * 1.2;
          ll.hip.rotation.z = 1.2 * Math.sin(k * PI); rl.hip.rotation.z = 1.2 * Math.sin(k * PI);
          ll.knee.rotation.z = -1.8 * Math.sin(k * PI); rl.knee.rotation.z = -1.8 * Math.sin(k * PI);
          armsUp(1 - Math.sin(k * PI));
        } else {
          const pump = Math.abs(Math.sin(time * 5));
          armsUp(0.8 + pump * 0.15);
          la.el.rotation.z = 0.3 * pump; ra.el.rotation.z = 0.3 * pump;
          B.position.y = this.hipY + pump * 0.08;
        }
        break;
      }
      case 3: { // رقصة
        const t = time * 7;
        B.position.z = Math.sin(t * 0.5) * 0.15;
        B.rotation.x = Math.sin(t * 0.5) * 0.15;
        T.rotation.y = Math.sin(t * 0.5) * 0.4;
        la.sh.rotation.z = 1.5 + Math.sin(t) * 1.2; ra.sh.rotation.z = 1.5 - Math.sin(t) * 1.2;
        la.el.rotation.z = 1.0; ra.el.rotation.z = 1.0;
        ll.hip.rotation.z = Math.max(0, Math.sin(t * 0.5)) * 0.8; ll.knee.rotation.z = -Math.max(0, Math.sin(t * 0.5)) * 1.4;
        rl.hip.rotation.z = Math.max(0, -Math.sin(t * 0.5)) * 0.8; rl.knee.rotation.z = -Math.max(0, -Math.sin(t * 0.5)) * 1.4;
        B.position.y = this.hipY + Math.abs(Math.sin(t)) * 0.06;
        break;
      }
      case 4: { // الطائرة
        const a = st * 2.4;
        B.position.x = Math.sin(a) * 1.6;
        B.position.z = (1 - Math.cos(a)) * 1.6;
        B.rotation.y = -a;
        B.rotation.x = -0.35;
        armsWide(0.1);
        const ph = time * 12;
        ll.hip.rotation.z = Math.sin(ph) * 0.8; rl.hip.rotation.z = -Math.sin(ph) * 0.8;
        ll.knee.rotation.z = -Math.max(0, -Math.cos(ph)) * 1.3; rl.knee.rotation.z = -Math.max(0, Math.cos(ph)) * 1.3;
        T.rotation.z = -0.2;
        break;
      }
      case 5: { // سيييو
        if (st < 0.35) {
          const k = st / 0.35;
          B.position.y = this.hipY - 0.2 * k; ll.knee.rotation.z = -0.8 * k; rl.knee.rotation.z = -0.8 * k; ll.hip.rotation.z = 0.4 * k; rl.hip.rotation.z = 0.4 * k;
          armsUp(k * 0.5);
        } else if (st < 1.0) {
          const k = (st - 0.35) / 0.65;
          B.position.y = this.hipY + Math.sin(k * PI) * 1.0;
          B.rotation.y = k * PI;
          armsUp(0.8);
          ll.hip.rotation.z = 0.5 * Math.sin(k * PI); rl.hip.rotation.z = 0.5 * Math.sin(k * PI); ll.knee.rotation.z = -0.8 * Math.sin(k * PI); rl.knee.rotation.z = -0.8 * Math.sin(k * PI);
        } else {
          B.rotation.y = PI;
          B.position.y = this.hipY - 0.12;
          ll.hip.rotation.x = -0.35; rl.hip.rotation.x = 0.35;
          ll.knee.rotation.z = -0.25; rl.knee.rotation.z = -0.25;
          la.sh.rotation.x = 0.7; ra.sh.rotation.x = -0.7; la.sh.rotation.z = 0.3; ra.sh.rotation.z = 0.3;
          T.rotation.z = 0.15;
          this.neck.rotation.z = 0.2;
          if (!this.siuDone) { this.siuDone = true; this.onSiu && this.onSiu(); }
        }
        if (st < 0.3) this.siuDone = false;
        break;
      }
      case 6: { // قفز مع الزملاء
        const j = Math.abs(Math.sin(time * 5 + this.info.slot));
        B.position.y = this.hipY + j * 0.4;
        armsUp(0.9);
        la.el.rotation.z = 0.2; ra.el.rotation.z = 0.2;
        ll.knee.rotation.z = -(1 - j) * 0.6; rl.knee.rotation.z = -(1 - j) * 0.6;
        break;
      }
      default: { // تصفيق
        const c = Math.abs(Math.sin(time * 8));
        la.sh.rotation.z = 1.1; ra.sh.rotation.z = 1.1;
        la.sh.rotation.x = -0.3 * c - 0.05; ra.sh.rotation.x = 0.3 * c + 0.05;
        la.el.rotation.z = 1.2; ra.el.rotation.z = 1.2;
      }
    }
  }

  smoothPose(dt, state) {
    const list = this._poseList || (this._poseList = [this.body, this.torso, this.neck, ...this.arms.flatMap((a) => [a.sh, a.el]), ...this.legs.flatMap((l) => [l.hip, l.knee])]);
    const prev = this._prev || (this._prev = list.map((o) => ({ r: o.rotation.clone(), p: o.position.clone() })));
    const k = 1 - Math.exp(-dt * (state === STATE.DOWN || state === STATE.DIVE ? 12 : 16));
    const celebrate = state === STATE.CELEBRATE;
    list.forEach((o, i) => {
      const pr = prev[i];
      if (celebrate && o === this.body) { pr.r.copy(o.rotation); pr.p.copy(o.position); return; }
      for (const ax of ['x', 'y', 'z']) {
        let d = o.rotation[ax] - pr.r[ax];
        if (Math.abs(d) > Math.PI) { pr.r[ax] = o.rotation[ax]; continue; }
        pr.r[ax] += d * k;
        o.rotation[ax] = pr.r[ax];
      }
      pr.p.lerp(o.position, k);
      o.position.copy(pr.p);
    });
  }

  triggerKick(header, isThrow) { this.kickT = 0; this.kickHeader = header; this.kickThrow = isThrow; }

  // موضع اليد/القدم في العالم (للكرة في الاحتفال)
  // مؤشر اللاعب المتحكَّم فيه (يتنقل مع التبديل)
  setSelected(on) {
    this.selected = on;
    if (this.ring) this.ring.visible = on;
    if (this.arrow) this.arrow.visible = on;
    if (this.teamRing) this.teamRing.visible = !on;
  }

  dispose() {
    this.root.traverse((o) => {
      if (o.isSprite) { o.material.map.dispose(); o.material.dispose(); }
    });
  }
}
