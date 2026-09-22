// نموذج اللاعب ثلاثي الأبعاد (مبني من أشكال بسيطة) مع رسوم متحركة إجرائية
import * as THREE from 'three';
import { STATE, TEAMS } from '/shared/constants.js';
import { charOf } from '/shared/characters.js';

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

function shade(m) { m.castShadow = true; m.receiveShadow = false; return m; }

function numberTexture(num, name, color, textColor) {
  const c = document.createElement('canvas');
  c.width = 128; c.height = 128;
  const g = c.getContext('2d');
  g.fillStyle = color; g.fillRect(0, 0, 128, 128);
  g.fillStyle = textColor;
  g.textAlign = 'center';
  g.font = 'bold 22px sans-serif';
  g.fillText(name.replace(/[^\p{L}\p{N} ]/gu, '').trim().slice(0, 10), 64, 30);
  g.font = 'bold 78px Arial Black, sans-serif';
  g.fillText(String(num), 64, 108);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = THREE.SRGBColorSpace;
  return t;
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
    const jersey = gk ? team.gk : team.color;
    const h = look.height, b = look.build;
    this.h = h;
    this.root = new THREE.Group();
    this.body = new THREE.Group();
    this.root.add(this.body);
    this.hipY = 0.92 * h;
    this.body.position.y = this.hipY;

    const skinM = mat(look.skin, 0.6);
    const jerseyM = mat(jersey, 0.8);
    const shortsM = mat(gk ? '#222222' : team.shorts, 0.8);
    const socksM = mat(gk ? jersey : team.socks, 0.85);
    const bootsM = mat(look.boots, 0.35);
    const hairM = mat(look.hairColor, 0.9);

    // الجذع
    this.torso = new THREE.Group();
    this.body.add(this.torso);
    const pelvis = shade(new THREE.Mesh(box(0.24 * b, 0.2, 0.36 * b), shortsM));
    pelvis.position.y = 0.02;
    this.torso.add(pelvis);
    const chest = shade(new THREE.Mesh(capsule(0.17, 0.3), jerseyM));
    chest.scale.set(0.78 * b, 1, 1.18 * b);
    chest.position.y = 0.3;
    this.torso.add(chest);
    // رقم على الظهر
    const numTex = numberTexture(info.slot === 0 ? 1 : [0, 4, 5, 10, 9][info.slot] || 7, info.name || '', jersey, team.color2);
    const numM = new THREE.MeshStandardMaterial({ map: numTex, roughness: 0.8 });
    const back = new THREE.Mesh(geo('numplane', () => new THREE.PlaneGeometry(0.3, 0.3)), numM);
    back.position.set(-0.135 * b - 0.01, 0.32, 0);
    back.rotation.y = -PI / 2;
    this.torso.add(back);
    // شعار على الصدر
    const crest = new THREE.Mesh(geo('crest', () => new THREE.CircleGeometry(0.04, 12)), mat(team.color2, 0.5));
    crest.position.set(0.135 * b + 0.005, 0.42, -0.08);
    crest.rotation.y = PI / 2;
    this.torso.add(crest);

    // الرأس
    this.neck = new THREE.Group();
    this.neck.position.y = 0.58;
    this.torso.add(this.neck);
    const neckM = shade(new THREE.Mesh(capsule(0.055, 0.06), skinM));
    neckM.position.y = 0.02;
    this.neck.add(neckM);
    this.head = new THREE.Group();
    this.head.position.y = 0.17;
    this.neck.add(this.head);
    const headM = shade(new THREE.Mesh(sphere(0.125), skinM));
    headM.scale.set(1, 1.1, 0.92);
    this.head.add(headM);
    const eyeM = mat('#111111', 0.3);
    for (const s of [-1, 1]) {
      const eye = new THREE.Mesh(sphere(0.018, 8, 6), eyeM);
      eye.position.set(0.112, 0.02, s * 0.045);
      this.head.add(eye);
      const ear = new THREE.Mesh(sphere(0.03, 8, 6), skinM);
      ear.position.set(0, 0, s * 0.118);
      ear.scale.set(0.6, 1, 0.5);
      this.head.add(ear);
    }
    const nose = new THREE.Mesh(box(0.03, 0.04, 0.03), skinM);
    nose.position.set(0.125, -0.005, 0);
    this.head.add(nose);
    this.addHair(look, hairM, skinM);

    // الذراعان
    this.arms = [];
    for (const s of [-1, 1]) {
      const sh = new THREE.Group();
      sh.position.set(0, 0.47, s * 0.21 * b);
      this.torso.add(sh);
      const sleeve = shade(new THREE.Mesh(capsule(0.06 * b, 0.1), jerseyM));
      sleeve.position.y = -0.08;
      sh.add(sleeve);
      const upper = shade(new THREE.Mesh(capsule(0.048 * b, 0.16), skinM));
      upper.position.y = -0.14;
      sh.add(upper);
      const el = new THREE.Group();
      el.position.y = -0.27;
      sh.add(el);
      const fore = shade(new THREE.Mesh(capsule(0.042 * b, 0.18), gk ? mat(jersey, 0.8) : skinM));
      fore.position.y = -0.12;
      el.add(fore);
      const glove = look.acc === 'gloves' || gk;
      const hand = shade(new THREE.Mesh(sphere(glove ? 0.065 : 0.048, 10, 8), glove ? mat(gk ? '#f5f5f5' : look.accColor, 0.5) : skinM));
      hand.position.y = -0.26;
      el.add(hand);
      if (look.acc === 'wristbands') {
        const wb = new THREE.Mesh(geo('wb', () => new THREE.CylinderGeometry(0.05, 0.05, 0.05, 10)), mat(look.accColor, 0.6));
        wb.position.y = -0.2;
        el.add(wb);
      }
      this.arms.push({ sh, el, s });
    }

    // الساقان
    this.legs = [];
    for (const s of [-1, 1]) {
      const hip = new THREE.Group();
      hip.position.set(0, 0, s * 0.1 * b);
      this.body.add(hip);
      const shortLeg = shade(new THREE.Mesh(capsule(0.085 * b, 0.12), shortsM));
      shortLeg.position.y = -0.1;
      hip.add(shortLeg);
      const thigh = shade(new THREE.Mesh(capsule(0.07 * b, 0.22), skinM));
      thigh.position.y = -0.24;
      hip.add(thigh);
      const knee = new THREE.Group();
      knee.position.y = -0.44 * h;
      hip.add(knee);
      const shin = shade(new THREE.Mesh(capsule(0.058 * b, 0.28), socksM));
      shin.position.y = -0.2;
      knee.add(shin);
      const boot = shade(new THREE.Mesh(box(0.24, 0.075, 0.1), bootsM));
      boot.position.set(0.05, -0.43 * h, 0);
      knee.add(boot);
      this.legs.push({ hip, knee, s });
    }

    this.scale = 1;
    this.root.traverse((o) => { if (o.isMesh) o.castShadow = true; });

    // مؤشرات
    if (opts.local) {
      const ring = new THREE.Mesh(
        geo('selring', () => new THREE.RingGeometry(0.62, 0.8, 40)),
        new THREE.MeshBasicMaterial({ color: '#fff27a', transparent: true, opacity: 0.9, depthWrite: false }),
      );
      ring.rotation.x = -PI / 2;
      ring.position.y = 0.03;
      this.root.add(ring);
      this.ring = ring;
      const arrow = new THREE.Mesh(geo('arrow', () => new THREE.ConeGeometry(0.16, 0.3, 4)), new THREE.MeshBasicMaterial({ color: '#fff27a' }));
      arrow.rotation.x = PI;
      arrow.position.y = 2.35 * h;
      this.root.add(arrow);
      this.arrow = arrow;
    } else if (!opts.preview) {
      const ring = new THREE.Mesh(
        geo('teamring', () => new THREE.RingGeometry(0.5, 0.58, 32)),
        new THREE.MeshBasicMaterial({ color: team.color, transparent: true, opacity: 0.55, depthWrite: false }),
      );
      ring.rotation.x = -PI / 2;
      ring.position.y = 0.025;
      this.root.add(ring);
    }
    if (!opts.preview) {
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
          T.rotation.y = -sn * 0.12 * amp;
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
    // المؤشرات
    if (this.arrow) { this.arrow.position.y = 2.35 * this.h + Math.sin(time * 5) * 0.08; this.arrow.rotation.y = time * 2; }
    if (this.ring) this.ring.material.opacity = 0.65 + Math.sin(time * 6) * 0.25;
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

  triggerKick(header, isThrow) { this.kickT = 0; this.kickHeader = header; this.kickThrow = isThrow; }

  // موضع اليد/القدم في العالم (للكرة في الاحتفال)
  dispose() {
    this.root.traverse((o) => {
      if (o.isSprite) { o.material.map.dispose(); o.material.dispose(); }
    });
  }
}
