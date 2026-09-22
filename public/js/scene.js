// بناء الملعب ثلاثي الأبعاد بالكامل إجرائياً: عشب، خطوط، مدرجات، جمهور متحرك، أضواء، شباك، طقس
import * as THREE from 'three';
import { mergeGeometries } from 'three/addons/utils/BufferGeometryUtils.js';
import { FIELD, BALL_R, TEAMS } from '/shared/constants.js';

const { L, W, GW, GH, GD, BOX_R, CIRCLE_R, POST_R } = FIELD;
const HL = L / 2, HW = W / 2;
const PI = Math.PI;
const rnd = (a, b) => a + Math.random() * (b - a);

// تحويل إحداثيات المحاكاة إلى Three.js
export const toV = (x, y, z = 0, v = new THREE.Vector3()) => v.set(x, z, -y);

function canvasTex(w, h, draw, opts = {}) {
  const c = document.createElement('canvas');
  c.width = w; c.height = h;
  const g = c.getContext('2d');
  draw(g, w, h);
  const t = new THREE.CanvasTexture(c);
  t.colorSpace = opts.linear ? THREE.NoColorSpace : THREE.SRGBColorSpace;
  if (opts.repeat) { t.wrapS = t.wrapT = THREE.RepeatWrapping; }
  t.anisotropy = opts.aniso || 4;
  t.userData.canvas = c;
  return t;
}

function shadeHex(hex, amt) {
  const c = new THREE.Color(hex);
  const hsl = {};
  c.getHSL(hsl);
  c.setHSL(hsl.h, hsl.s, Math.max(0, Math.min(1, hsl.l + amt)));
  return '#' + c.getHexString();
}

export class World {
  constructor(renderer, stadium, quality = 'medium') {
    this.renderer = renderer;
    this.st = stadium;
    this.q = quality;
    this.scene = new THREE.Scene();
    this.time = 0;
    this.excite = 0;
    this.teamExcite = [0, 0];
    this.wave = -10;
    this.flash = 0;
    this.nets = [];
    this.disposables = [];
    this.updaters = [];
    const s = this.scene;
    s.background = new THREE.Color(stadium.skyBottom);
    s.fog = new THREE.FogExp2(stadium.fog, stadium.fogDensity * (quality === 'low' ? 0.8 : 1));
    this.buildSky();
    this.buildLights();
    this.buildPitch();
    this.buildGoals();
    this.buildBoards();
    this.buildStands();
    this.buildFloodlights();
    this.buildJumbotron();
    this.buildFlags();
    this.buildWeather();
  }

  // ---------------- السماء ----------------
  buildSky() {
    const st = this.st;
    const geo = new THREE.SphereGeometry(420, 32, 16);
    const mat = new THREE.ShaderMaterial({
      side: THREE.BackSide, depthWrite: false, fog: false,
      uniforms: {
        top: { value: new THREE.Color(st.skyTop) }, bottom: { value: new THREE.Color(st.skyBottom) },
        sunDir: { value: new THREE.Vector3(...st.sun.pos).normalize() }, sunCol: { value: new THREE.Color(st.sun.color) },
        sunAmt: { value: st.time === 'sunset' ? 1.0 : st.time === 'day' ? 0.6 : 0.0 },
      },
      vertexShader: 'varying vec3 vP; void main(){ vP = normalize(position); gl_Position = projectionMatrix * modelViewMatrix * vec4(position,1.0); }',
      fragmentShader: `uniform vec3 top; uniform vec3 bottom; uniform vec3 sunDir; uniform vec3 sunCol; uniform float sunAmt; varying vec3 vP;
        void main(){ float h = clamp(vP.y*1.6+0.15,0.0,1.0); vec3 c = mix(bottom, top, pow(h,0.8));
        float sd = max(dot(normalize(vP), sunDir),0.0); c += sunCol * (pow(sd, 400.0)*3.0 + pow(sd,12.0)*0.35) * sunAmt;
        gl_FragColor = vec4(c,1.0); }`,
    });
    const sky = new THREE.Mesh(geo, mat);
    sky.renderOrder = -10;
    this.scene.add(sky);
    this.sky = sky;
    if (st.time === 'night' || st.time === 'storm') {
      const n = 900, pos = new Float32Array(n * 3);
      for (let i = 0; i < n; i++) {
        const a = Math.random() * PI * 2, e = Math.random() * 0.9 + 0.1;
        pos.set([Math.cos(a) * Math.cos(e) * 400, Math.sin(e) * 400, Math.sin(a) * Math.cos(e) * 400], i * 3);
      }
      const g = new THREE.BufferGeometry();
      g.setAttribute('position', new THREE.BufferAttribute(pos, 3));
      const stars = new THREE.Points(g, new THREE.PointsMaterial({ color: 0xffffff, size: 1.6, sizeAttenuation: false, fog: false, transparent: true, opacity: st.time === 'storm' ? 0.0 : 0.85 }));
      this.scene.add(stars);
    }
    if (st.time === 'day' || st.time === 'sunset' || st.time === 'snow' || st.time === 'storm') {
      const cloudTex = canvasTex(256, 128, (g, w, h) => {
        for (let i = 0; i < 26; i++) {
          const x = rnd(40, w - 40), y = rnd(45, h - 30), r = rnd(18, 42);
          const gr = g.createRadialGradient(x, y, 0, x, y, r);
          gr.addColorStop(0, 'rgba(255,255,255,0.85)'); gr.addColorStop(1, 'rgba(255,255,255,0)');
          g.fillStyle = gr; g.beginPath(); g.arc(x, y, r, 0, PI * 2); g.fill();
        }
      });
      const tint = st.time === 'sunset' ? '#ffc39a' : st.time === 'storm' ? '#5a6570' : '#ffffff';
      this.clouds = [];
      const count = st.time === 'storm' ? 16 : 9;
      for (let i = 0; i < count; i++) {
        const m = new THREE.SpriteMaterial({ map: cloudTex, color: tint, transparent: true, opacity: st.time === 'storm' ? 0.95 : 0.8, fog: false, depthWrite: false });
        const sp = new THREE.Sprite(m);
        const a = Math.random() * PI * 2, d = rnd(200, 330);
        sp.position.set(Math.cos(a) * d, rnd(70, 140), Math.sin(a) * d);
        sp.scale.set(rnd(120, 200), rnd(45, 70), 1);
        this.scene.add(sp);
        this.clouds.push({ sp, a, d, s: rnd(0.002, 0.006) });
      }
    }
  }

  // ---------------- الإضاءة ----------------
  buildLights() {
    const st = this.st;
    const hemi = new THREE.HemisphereLight(st.hemi[0], st.hemi[1], st.hemi[2]);
    this.scene.add(hemi);
    this.hemi = hemi;
    this.hemiBase = st.hemi[2];
    const sun = new THREE.DirectionalLight(st.sun.color, st.sun.intensity);
    sun.position.set(...st.sun.pos);
    if (this.q !== 'low') {
      sun.castShadow = true;
      const size = this.q === 'high' ? 4096 : 2048;
      sun.shadow.mapSize.set(size, size);
      const c = sun.shadow.camera;
      c.left = -48; c.right = 48; c.top = 36; c.bottom = -36; c.near = 1; c.far = 220;
      sun.shadow.bias = -0.0004;
      sun.shadow.normalBias = 0.03;
    }
    this.scene.add(sun, sun.target);
    this.sun = sun;
    if (st.floodlights) {
      // إضاءة الأبراج الأربعة (ضوء تعبئة)
      const fill = new THREE.DirectionalLight(st.time === 'night' ? '#dfe8ff' : '#fff0dd', st.time === 'night' ? 1.4 : 0.6);
      fill.position.set(-30, 45, -40);
      this.scene.add(fill);
      const fill2 = new THREE.DirectionalLight(st.time === 'night' ? '#dfe8ff' : '#fff0dd', st.time === 'night' ? 0.9 : 0.4);
      fill2.position.set(35, 40, 38);
      this.scene.add(fill2);
    }
    // ضوء نقطي للمؤثرات (يبقى دائماً لتجنب إعادة ترجمة الشيدرات)
    this.fxLight = new THREE.PointLight('#ffffff', 0, 18, 2);
    this.fxLight.position.set(0, 3, 0);
    this.scene.add(this.fxLight);
  }

  // ---------------- أرضية الملعب ----------------
  buildPitch() {
    const st = this.st;
    const M = 5; // الهامش حول الملعب
    const PW = L + M * 2, PH = W + M * 2;
    const ppm = this.q === 'high' ? 36 : this.q === 'medium' ? 26 : 16;
    const cw = Math.round(PW * ppm), ch = Math.round(PH * ppm);
    const tex = canvasTex(cw, ch, (g, w, h) => {
      const X = (x) => (x + PW / 2) * ppm, Y = (y) => (PH / 2 - y) * ppm;
      // خطوط القص
      const stripe = 4;
      for (let i = 0; i * stripe < PW; i++) {
        g.fillStyle = st.grass[i % 2];
        g.fillRect(i * stripe * ppm, 0, stripe * ppm + 1, h);
      }
      g.globalAlpha = 0.05;
      for (let j = 0; j * stripe < PH; j++) {
        if (j % 2) { g.fillStyle = '#000'; g.fillRect(0, j * stripe * ppm, w, stripe * ppm); }
      }
      g.globalAlpha = 1;
      // حبيبات العشب
      const n = Math.floor(w * h * 0.06);
      for (let i = 0; i < n; i++) {
        const l = Math.random();
        g.fillStyle = l < 0.5 ? `rgba(0,0,0,${0.05 + Math.random() * 0.08})` : `rgba(255,255,160,${0.03 + Math.random() * 0.06})`;
        g.fillRect(Math.random() * w, Math.random() * h, 1 + Math.random() * 1.5, 1 + Math.random() * 2);
      }
      // تآكل أمام المرميين
      for (const s of [-1, 1]) {
        for (let i = 0; i < 70; i++) {
          const x = X(s * (HL - rnd(0, 4))), y = Y(rnd(-3, 3));
          const r = rnd(0.3, 1.2) * ppm;
          const gr = g.createRadialGradient(x, y, 0, x, y, r);
          gr.addColorStop(0, st.snowy ? 'rgba(240,248,255,0.35)' : 'rgba(120,95,55,0.22)'); gr.addColorStop(1, 'rgba(0,0,0,0)');
          g.fillStyle = gr; g.fillRect(x - r, y - r, r * 2, r * 2);
        }
      }
      if (st.snowy) {
        for (let i = 0; i < 1400; i++) {
          const edge = Math.random() < 0.7;
          let x = rnd(-PW / 2, PW / 2), y = rnd(-PH / 2, PH / 2);
          if (edge) { if (Math.random() < 0.5) y = (Math.random() < 0.5 ? -1 : 1) * rnd(HW - 1, PH / 2); else x = (Math.random() < 0.5 ? -1 : 1) * rnd(HL - 1, PW / 2); }
          const r = rnd(0.2, edge ? 1.6 : 0.6) * ppm;
          const gr = g.createRadialGradient(X(x), Y(y), 0, X(x), Y(y), r);
          gr.addColorStop(0, `rgba(250,252,255,${edge ? 0.7 : 0.25})`); gr.addColorStop(1, 'rgba(250,252,255,0)');
          g.fillStyle = gr; g.fillRect(X(x) - r, Y(y) - r, r * 2, r * 2);
        }
      }
      // الخطوط البيضاء
      g.strokeStyle = 'rgba(255,255,255,0.92)';
      g.fillStyle = 'rgba(255,255,255,0.92)';
      g.lineWidth = 0.12 * ppm;
      g.strokeRect(X(-HL), Y(HW), L * ppm, W * ppm);
      g.beginPath(); g.moveTo(X(0), Y(HW)); g.lineTo(X(0), Y(-HW)); g.stroke();
      g.beginPath(); g.arc(X(0), Y(0), CIRCLE_R * ppm, 0, PI * 2); g.stroke();
      g.beginPath(); g.arc(X(0), Y(0), 0.2 * ppm, 0, PI * 2); g.fill();
      for (const s of [-1, 1]) {
        const cx = X(s * HL);
        // منطقة الجزاء (قوس)
        g.beginPath();
        if (s > 0) g.arc(cx, Y(0), BOX_R * ppm, PI / 2, PI * 1.5); else g.arc(cx, Y(0), BOX_R * ppm, -PI / 2, PI / 2);
        g.stroke();
        // منطقة المرمى
        g.beginPath();
        if (s > 0) g.arc(cx, Y(0), 3.2 * ppm, PI / 2, PI * 1.5); else g.arc(cx, Y(0), 3.2 * ppm, -PI / 2, PI / 2);
        g.stroke();
        g.beginPath(); g.arc(X(s * (HL - 6)), Y(0), 0.16 * ppm, 0, PI * 2); g.fill();
        // أقواس الركنيات
        for (const t of [-1, 1]) {
          g.beginPath();
          const a0 = s > 0 ? (t > 0 ? PI / 2 : PI) : t > 0 ? 0 : -PI / 2;
          g.arc(X(s * HL), Y(t * HW), 0.8 * ppm, a0, a0 + PI / 2);
          g.stroke();
        }
      }
    }, { aniso: this.renderer.capabilities.getMaxAnisotropy() });
    const mat = new THREE.MeshStandardMaterial({ map: tex, roughness: st.wet ? 0.42 : 0.95, metalness: st.wet ? 0.08 : 0 });
    const pitch = new THREE.Mesh(new THREE.PlaneGeometry(PW, PH), mat);
    pitch.rotation.x = -PI / 2;
    pitch.receiveShadow = this.q !== 'low';
    this.scene.add(pitch);
    // الأرض المحيطة
    const around = new THREE.Mesh(
      new THREE.PlaneGeometry(260, 220),
      new THREE.MeshStandardMaterial({ color: st.snowy ? '#e9f0f5' : st.time === 'sunset' ? '#b88a5a' : '#3d5c35', roughness: 1 }),
    );
    around.rotation.x = -PI / 2;
    around.position.y = -0.02;
    around.receiveShadow = this.q !== 'low';
    this.scene.add(around);
  }

  // ---------------- المرميان والشباك ----------------
  buildGoals() {
    const postM = new THREE.MeshStandardMaterial({ color: '#ffffff', roughness: 0.3, metalness: 0.2 });
    const netTex = canvasTex(128, 128, (g, w, h) => {
      g.clearRect(0, 0, w, h);
      g.strokeStyle = 'rgba(255,255,255,0.95)';
      g.lineWidth = 5;
      g.beginPath();
      for (let i = 0; i <= 4; i++) { g.moveTo(i * 32, 0); g.lineTo(i * 32, h); g.moveTo(0, i * 32); g.lineTo(w, i * 32); }
      g.stroke();
    }, { repeat: true });
    const makeNetMat = (rx, ry) => {
      const t = netTex.clone();
      t.needsUpdate = true;
      t.wrapS = t.wrapT = THREE.RepeatWrapping;
      t.repeat.set(rx, ry);
      return new THREE.MeshStandardMaterial({ map: t, transparent: true, alphaTest: 0.3, side: THREE.DoubleSide, roughness: 0.9, depthWrite: true });
    };
    const cell = 0.12;
    for (const s of [-1, 1]) {
      const g = new THREE.Group();
      const post = (len, r = POST_R) => new THREE.CylinderGeometry(r, r, len, 12);
      for (const py of [-GW / 2, GW / 2]) {
        const m = new THREE.Mesh(post(GH + POST_R), postM);
        toV(s * HL, py, (GH + POST_R) / 2, m.position);
        m.castShadow = true;
        g.add(m);
        // عمود خلفي مائل
        const back = new THREE.Mesh(post(Math.hypot(GD, GH), 0.03), postM);
        const bx = s * (HL + GD / 2);
        toV(bx, py, GH / 2, back.position);
        back.rotation.z = s * Math.atan2(GD, GH);
        g.add(back);
        const base = new THREE.Mesh(post(GD, 0.03), postM);
        toV(bx, py, 0.03, base.position);
        base.rotation.z = PI / 2;
        g.add(base);
      }
      const bar = new THREE.Mesh(post(GW + POST_R * 2), postM);
      toV(s * HL, 0, GH, bar.position);
      bar.rotation.x = PI / 2;
      bar.castShadow = true;
      g.add(bar);
      const backBar = new THREE.Mesh(post(GW, 0.03), postM);
      toV(s * (HL + GD), 0, 0.03, backBar.position);
      backBar.rotation.x = PI / 2;
      g.add(backBar);
      // الشباك الخلفية (قابلة للاهتزاز)
      const segX = 16, segY = 8;
      const backGeo = new THREE.PlaneGeometry(GW, Math.hypot(GD, GH), segX, segY);
      const backNet = new THREE.Mesh(backGeo, makeNetMat(GW / cell / 4, Math.hypot(GD, GH) / cell / 4));
      toV(s * (HL + GD / 2), 0, GH / 2, backNet.position);
      backNet.rotation.y = s > 0 ? -PI / 2 : PI / 2;
      backNet.rotateX(Math.atan2(GD, GH));
      g.add(backNet);
      // الجانبان (مثلثان)
      for (const py of [-GW / 2, GW / 2]) {
        const shape = new THREE.Shape();
        shape.moveTo(0, 0); shape.lineTo(GD, 0); shape.lineTo(0, GH); shape.lineTo(0, 0);
        const sg = new THREE.ShapeGeometry(shape);
        const uv = sg.attributes.uv;
        for (let i = 0; i < uv.count; i++) uv.setXY(i, uv.getX(i) * GD / cell / 4 / GD * 1, uv.getY(i) * GH / cell / 4 / GH * 1);
        const side = new THREE.Mesh(sg, makeNetMat(1, 1));
        side.material.map.repeat.set(1, 1);
        toV(s * HL, py, 0, side.position);
        side.rotation.y = s > 0 ? 0 : PI;
        g.add(side);
      }
      this.scene.add(g);
      const pos = backGeo.attributes.position;
      this.nets.push({ s, mesh: backNet, base: Float32Array.from(pos.array), amp: 0, hitY: 0, hitZ: 0, t: 0 });
    }
  }

  netHit(x, y, z, speed) {
    const n = this.nets.find((nn) => Math.sign(x) === nn.s);
    if (!n) return;
    n.amp = Math.min(0.9, 0.2 + speed * 0.03);
    n.hitY = y; n.hitZ = z; n.t = 0;
  }

  updateNets(dt) {
    for (const n of this.nets) {
      if (n.amp <= 0.001) continue;
      n.t += dt;
      n.amp *= Math.exp(-dt * 2.2);
      const pos = n.mesh.geometry.attributes.position;
      const len = Math.hypot(GD, GH);
      for (let i = 0; i < pos.count; i++) {
        const bx = n.base[i * 3], by = n.base[i * 3 + 1];
        // bx: عرض المرمى، by: على طول الشبكة المائلة
        const wy = (n.s > 0 ? -bx : bx) , wz = (by + len / 2) * (GH / len);
        const d = Math.hypot(wy - n.hitY, wz - n.hitZ);
        const edge = Math.min(1, (GW / 2 - Math.abs(bx)) / 0.6) * Math.min(1, (len / 2 - Math.abs(by)) / 0.4);
        const off = n.amp * Math.exp(-d * d * 0.8) * Math.cos(n.t * 14 - d * 3) * edge;
        pos.setZ(i, -off);
      }
      pos.needsUpdate = true;
    }
  }

  // ---------------- اللوحات الإعلانية ----------------
  buildBoards() {
    const brands = ['⚽ أساطير الكرة', 'ELITE FOOTBALL', '٥ ضد ٥', 'LEGENDS CUP', 'قوة • سرعة • مهارة', 'ONLINE 5v5', 'الدوري الذهبي', 'PLAY FAIR'];
    const cols = [['#0b1d51', '#ffd23f'], ['#d7263d', '#ffffff'], ['#111111', '#00e5ff'], ['#1f9d55', '#ffffff'], ['#ff5a1f', '#111111'], ['#5b2a86', '#ffffff']];
    const adTex = canvasTex(2048, 64, (g, w, h) => {
      let x = 0, i = 0;
      while (x < w) {
        const [bg, fg] = cols[i % cols.length];
        const bw = 256;
        g.fillStyle = bg; g.fillRect(x, 0, bw, h);
        g.fillStyle = fg; g.font = 'bold 30px Tahoma, Arial'; g.textAlign = 'center'; g.textBaseline = 'middle';
        g.fillText(brands[i % brands.length], x + bw / 2, h / 2 + 2);
        x += bw; i++;
      }
    }, { repeat: true });
    const goalTex = canvasTex(1024, 64, (g, w, h) => {
      for (let i = 0; i < 8; i++) {
        g.fillStyle = i % 2 ? '#ffd23f' : '#d7263d'; g.fillRect(i * 128, 0, 128, h);
        g.fillStyle = i % 2 ? '#111' : '#fff'; g.font = 'bold 40px Arial Black, Tahoma'; g.textAlign = 'center'; g.textBaseline = 'middle';
        g.fillText(i % 2 ? 'هدف!' : 'GOAL!', i * 128 + 64, h / 2 + 2);
      }
    }, { repeat: true });
    this.adTex = adTex; this.goalAdTex = goalTex;
    const mat = new THREE.MeshStandardMaterial({ map: adTex, emissive: '#ffffff', emissiveMap: adTex, emissiveIntensity: this.st.time === 'night' ? 0.9 : 0.45, roughness: 0.5 });
    this.boardMat = mat;
    const addBoard = (len, x, y, rotY) => {
      const geo = new THREE.BoxGeometry(len, 0.9, 0.12);
      const uv = geo.attributes.uv;
      for (let i = 0; i < uv.count; i++) uv.setX(i, uv.getX(i) * len / 16);
      const m = new THREE.Mesh(geo, mat);
      toV(x, y, 0.45, m.position);
      m.rotation.y = rotY;
      m.castShadow = this.q === 'high';
      this.scene.add(m);
    };
    addBoard(L + 6, 0, HW + 2.4, PI);
    addBoard(L + 6, 0, -HW - 2.4, 0);
    addBoard(W + 5, HL + 3.4, 0, PI / 2);
    addBoard(W + 5, -HL - 3.4, 0, -PI / 2);
  }

  // ---------------- المدرجات والجمهور ----------------
  buildStands() {
    const st = this.st;
    const standMat = new THREE.MeshLambertMaterial({ vertexColors: true });
    const roofMat = new THREE.MeshStandardMaterial({ color: st.roof, roughness: 0.6, metalness: 0.3, side: THREE.DoubleSide });
    const wallMat = new THREE.MeshStandardMaterial({ color: shadeHex(st.standColor, -0.1), roughness: 0.9 });
    this.crowdU = { uTime: { value: 0 }, uExcite: { value: 0 }, uTeam0: { value: 0 }, uTeam1: { value: 0 }, uWave: { value: -10 } };
    const crowdGeo = this.makeCrowdGeo();
    const crowdMat = new THREE.MeshLambertMaterial({ color: '#ffffff' });
    const U = this.crowdU;
    crowdMat.onBeforeCompile = (sh) => {
      Object.assign(sh.uniforms, U);
      sh.vertexShader = sh.vertexShader
        .replace('#include <common>', `#include <common>
          uniform float uTime; uniform float uExcite; uniform float uTeam0; uniform float uTeam1; uniform float uWave;
          attribute float aPhase; attribute float aFan; attribute float aAng; attribute float aArm; attribute float aHead;`)
        .replace('#include <begin_vertex>', `#include <begin_vertex>
          float fanEx = aFan < 0.5 ? uTeam0 : (aFan < 1.5 ? uTeam1 : max(uTeam0, uTeam1) * 0.6);
          float ex = max(uExcite * 0.5, fanEx);
          float wd = mod(aAng - uWave + 3.14159, 6.28318) - 3.14159;
          float wave = exp(-wd * wd * 18.0);
          float bounce = abs(sin(uTime * (5.0 + fract(aPhase * 13.0) * 3.0) + aPhase * 6.28)) * (0.05 + ex * 0.32) + wave * 0.55;
          float sway = sin(uTime * 1.3 + aPhase * 6.28) * 0.03;
          float raise = clamp(ex * 1.6 * step(0.35, fract(aPhase * 7.0)) + wave, 0.0, 1.0);
          if (aArm > 0.5) { transformed.y = mix(transformed.y, 1.25 - transformed.y + sin(uTime * 9.0 + aPhase * 20.0) * 0.08 * ex, raise); }
          transformed.y += bounce; transformed.x += sway;`)
        .replace('#include <color_vertex>', `#include <color_vertex>
          #ifdef USE_INSTANCING_COLOR
          vColor.rgb = mix(vColor.rgb, vec3(0.86, 0.66, 0.5) * (0.55 + 0.45 * fract(aPhase * 31.0)), aHead);
          #endif`);
    };
    this.crowdMat = crowdMat;
    const fill = (this.q === 'low' ? 0.45 : this.q === 'medium' ? 0.8 : 0.92) * st.crowd;

    const teamCols = [
      [TEAMS[0].color, TEAMS[0].color2, TEAMS[0].color, '#8a1020'],
      [TEAMS[1].color, TEAMS[1].color2, TEAMS[1].color, '#0d2c66'],
    ];
    const neutral = ['#222222', '#dddddd', '#555555', '#7a5230', '#2e4a2e', '#aa7722', '#334455'];

    const stands = [
      { name: 'far', len: L + 8, pos: [0, -(HW + 4.2)], rot: PI, tiers: [[14, 0.85, 0.48, 0.8], [12, 0.9, 0.62, 9.2]], roofH: 22, roofD: 26, bias: 2 },
      { name: 'near', len: L + 8, pos: [0, HW + 4.2], rot: 0, tiers: [[12, 0.85, 0.46, 0.8]], roofH: 0, roofD: 0, bias: 2 },
      { name: 'east', len: W + 6, pos: [HL + 5.2, 0], rot: PI / 2, tiers: [[14, 0.85, 0.52, 0.8], [8, 0.9, 0.62, 9.8]], roofH: 20, roofD: 21, bias: 1 },
      { name: 'west', len: W + 6, pos: [-(HL + 5.2), 0], rot: -PI / 2, tiers: [[14, 0.85, 0.52, 0.8], [8, 0.9, 0.62, 9.8]], roofH: 20, roofD: 21, bias: 0 },
    ];
    const tmpM = new THREE.Matrix4(), tmpQ = new THREE.Quaternion(), tmpS = new THREE.Vector3(), tmpP = new THREE.Vector3(), wp = new THREE.Vector3();
    const col = new THREE.Color();
    for (const sd of stands) {
      const grp = new THREE.Group();
      grp.position.set(sd.pos[0], 0, sd.pos[1]);
      grp.rotation.y = sd.rot;
      this.scene.add(grp);
      grp.updateMatrixWorld(true);
      const geos = [];
      const seats = [];
      let z = 0;
      sd.tiers.forEach((tier, ti) => {
        const [rows, rowD, rise, y0] = tier;
        if (ti > 0) z += 1.6;
        for (let r = 0; r < rows; r++) {
          const top = y0 + r * rise;
          const thick = ti === 0 ? top : 0.9;
          const g = new THREE.BoxGeometry(sd.len, thick, rowD);
          g.translate(0, top - thick / 2, z + rowD / 2);
          const c = new THREE.Color(st.seatColors[(r + ti) % st.seatColors.length]).multiplyScalar(r % 2 ? 0.8 : 1);
          const cols = new Float32Array(g.attributes.position.count * 3);
          for (let i = 0; i < cols.length; i += 3) { cols[i] = c.r; cols[i + 1] = c.g; cols[i + 2] = c.b; }
          g.setAttribute('color', new THREE.BufferAttribute(cols, 3));
          geos.push(g);
          for (let x = -sd.len / 2 + 0.4; x < sd.len / 2 - 0.3; x += 0.62) {
            if (Math.random() < fill) seats.push([x + rnd(-0.08, 0.08), top, z + rowD * 0.55]);
          }
          z += rowD;
        }
      });
      // الجدار الخلفي
      const topY = sd.tiers[sd.tiers.length - 1][3] + sd.tiers[sd.tiers.length - 1][0] * sd.tiers[sd.tiers.length - 1][2];
      const back = new THREE.BoxGeometry(sd.len + 0.6, topY + 2.5, 0.6);
      back.translate(0, (topY + 2.5) / 2, z + 0.3);
      const bc = new THREE.Color(st.standColor);
      const bcol = new Float32Array(back.attributes.position.count * 3);
      for (let i = 0; i < bcol.length; i += 3) { bcol[i] = bc.r; bcol[i + 1] = bc.g; bcol[i + 2] = bc.b; }
      back.setAttribute('color', new THREE.BufferAttribute(bcol, 3));
      geos.push(back);
      // الجداران الجانبيان
      for (const sx of [-1, 1]) {
        const side = new THREE.BoxGeometry(0.6, topY + 2.5, z);
        side.translate(sx * (sd.len / 2 + 0.3), (topY + 2.5) / 2, z / 2);
        const scol = new Float32Array(side.attributes.position.count * 3);
        const sc = bc.clone().multiplyScalar(0.8);
        for (let i = 0; i < scol.length; i += 3) { scol[i] = sc.r; scol[i + 1] = sc.g; scol[i + 2] = sc.b; }
        side.setAttribute('color', new THREE.BufferAttribute(scol, 3));
        geos.push(side);
      }
      const standMesh = new THREE.Mesh(mergeGeometries(geos), standMat);
      standMesh.castShadow = this.q !== 'low' && sd.name !== 'near';
      standMesh.receiveShadow = this.q === 'high';
      grp.add(standMesh);
      // الحافة الأمامية
      const lip = new THREE.Mesh(new THREE.BoxGeometry(sd.len, 1.1, 0.3), wallMat);
      lip.position.set(0, 0.55, -0.15);
      grp.add(lip);
      // السقف
      if (sd.roofH) {
        const roof = new THREE.Mesh(new THREE.BoxGeometry(sd.len + 4, 0.5, sd.roofD), roofMat);
        roof.position.set(0, sd.roofH, z - sd.roofD / 2 + 1);
        roof.rotation.x = -0.08;
        roof.castShadow = this.q !== 'low';
        grp.add(roof);
        // حافة مضيئة
        const edgeCol = st.neon ? (sd.bias === 0 ? '#ff2e88' : sd.bias === 1 ? '#00e5ff' : '#b04dff') : '#fff6d8';
        const edge = new THREE.Mesh(new THREE.BoxGeometry(sd.len + 4, 0.25, 0.3), new THREE.MeshBasicMaterial({ color: edgeCol, fog: false }));
        edge.position.set(0, sd.roofH - 0.35 - Math.sin(0.08) * sd.roofD / 2, z - sd.roofD + 1);
        grp.add(edge);
        if (st.neon) this.neonEdges = (this.neonEdges || []).concat(edge);
        for (let i = -2; i <= 2; i++) {
          const col2 = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.45, sd.roofH, 8), roofMat);
          col2.position.set((i * sd.len) / 5, sd.roofH / 2, z + 0.2);
          grp.add(col2);
        }
      }
      if (st.neon) {
        const strip = new THREE.Mesh(new THREE.BoxGeometry(sd.len, 0.12, 0.05), new THREE.MeshBasicMaterial({ color: sd.bias === 0 ? '#ff2e88' : '#00e5ff' }));
        strip.position.set(0, 1.05, -0.32);
        grp.add(strip);
        this.neonEdges = (this.neonEdges || []).concat(strip);
      }
      // الجمهور
      const n = seats.length;
      const inst = new THREE.InstancedMesh(crowdGeo, crowdMat, n);
      const aPhase = new Float32Array(n), aFan = new Float32Array(n), aAng = new Float32Array(n);
      for (let i = 0; i < n; i++) {
        const [x, y, zz] = seats[i];
        tmpP.set(x, y, zz);
        tmpQ.setFromAxisAngle(new THREE.Vector3(0, 1, 0), rnd(-0.25, 0.25));
        const sc = rnd(0.9, 1.1);
        tmpS.set(sc, sc * rnd(0.92, 1.08), sc);
        tmpM.compose(tmpP, tmpQ, tmpS);
        inst.setMatrixAt(i, tmpM);
        let fan;
        if (sd.bias === 2) fan = x < 0 ? (Math.random() < 0.8 ? 0 : 1) : Math.random() < 0.8 ? 1 : 0;
        else fan = Math.random() < 0.85 ? sd.bias : 1 - sd.bias;
        if (Math.random() < 0.1) fan = 2;
        aFan[i] = fan;
        aPhase[i] = Math.random();
        const palette = fan === 2 ? neutral : teamCols[fan];
        col.set(palette[Math.floor(Math.random() * palette.length)]);
        col.multiplyScalar(rnd(0.75, 1.05));
        inst.setColorAt(i, col);
        wp.set(x, y, zz).applyMatrix4(grp.matrixWorld);
        aAng[i] = Math.atan2(wp.z, wp.x);
      }
      inst.geometry = crowdGeo.clone();
      inst.geometry.setAttribute('aPhase', new THREE.InstancedBufferAttribute(aPhase, 1));
      inst.geometry.setAttribute('aFan', new THREE.InstancedBufferAttribute(aFan, 1));
      inst.geometry.setAttribute('aAng', new THREE.InstancedBufferAttribute(aAng, 1));
      inst.instanceMatrix.needsUpdate = true;
      inst.frustumCulled = false;
      grp.add(inst);
      sd.seats = seats; sd.grp = grp;
    }
    this.stands = stands;
    // وميض كاميرات الجمهور
    const fc = this.q === 'low' ? 60 : 180;
    const fpos = new Float32Array(fc * 3);
    const fall = stands.flatMap((sd) => sd.seats.slice(0, 4000).map((s) => new THREE.Vector3(s[0], s[1] + 1.2, s[2]).applyMatrix4(sd.grp.matrixWorld)));
    for (let i = 0; i < fc; i++) {
      const v = fall[Math.floor(Math.random() * fall.length)] || new THREE.Vector3();
      fpos.set([v.x, v.y, v.z], i * 3);
    }
    const fgeo = new THREE.BufferGeometry();
    fgeo.setAttribute('position', new THREE.BufferAttribute(fpos, 3));
    const fsize = new Float32Array(fc);
    fgeo.setAttribute('size', new THREE.BufferAttribute(fsize, 1));
    const fmat = new THREE.ShaderMaterial({
      transparent: true, depthWrite: false, blending: THREE.AdditiveBlending,
      vertexShader: 'attribute float size; void main(){ vec4 mv = modelViewMatrix * vec4(position,1.0); gl_PointSize = size * 300.0 / -mv.z; gl_Position = projectionMatrix * mv; }',
      fragmentShader: 'void main(){ float d = length(gl_PointCoord-0.5); float a = smoothstep(0.5,0.0,d); gl_FragColor = vec4(vec3(1.0), a); }',
    });
    this.flashes = new THREE.Points(fgeo, fmat);
    this.flashes.frustumCulled = false;
    this.scene.add(this.flashes);
    this.flashSizes = fsize;
  }

  makeCrowdGeo() {
    const parts = [];
    const add = (g, arm, head) => {
      const n = g.attributes.position.count;
      g.setAttribute('aArm', new THREE.BufferAttribute(new Float32Array(n).fill(arm), 1));
      g.setAttribute('aHead', new THREE.BufferAttribute(new Float32Array(n).fill(head), 1));
      g.deleteAttribute('uv');
      parts.push(g);
    };
    const torso = new THREE.BoxGeometry(0.42, 0.5, 0.28); torso.translate(0, 0.32, 0); add(torso, 0, 0);
    const head = new THREE.BoxGeometry(0.22, 0.24, 0.22); head.translate(0, 0.72, -0.02); add(head, 0, 1);
    const legs = new THREE.BoxGeometry(0.38, 0.14, 0.4); legs.translate(0, 0.07, -0.18); add(legs, 0, 0);
    for (const s of [-1, 1]) {
      const arm = new THREE.BoxGeometry(0.1, 0.42, 0.1); arm.translate(s * 0.26, 0.38, 0); add(arm, 1, 0);
    }
    return mergeGeometries(parts);
  }

  // ---------------- الأبراج الضوئية ----------------
  buildFloodlights() {
    const st = this.st;
    const poleM = new THREE.MeshStandardMaterial({ color: '#9aa3ad', roughness: 0.5, metalness: 0.6 });
    const lampTex = canvasTex(128, 64, (g, w, h) => {
      g.fillStyle = '#222'; g.fillRect(0, 0, w, h);
      for (let x = 0; x < 8; x++) for (let y = 0; y < 4; y++) {
        const gr = g.createRadialGradient(8 + x * 16, 8 + y * 16, 0, 8 + x * 16, 8 + y * 16, 8);
        gr.addColorStop(0, '#ffffff'); gr.addColorStop(0.6, '#fff6d0'); gr.addColorStop(1, '#555');
        g.fillStyle = gr; g.fillRect(x * 16, y * 16, 16, 16);
      }
    });
    const on = st.floodlights;
    const lampM = new THREE.MeshStandardMaterial({ map: lampTex, emissive: '#ffffff', emissiveMap: lampTex, emissiveIntensity: on ? 2.2 : 0.2 });
    const glowTex = canvasTex(128, 128, (g, w, h) => {
      const gr = g.createRadialGradient(64, 64, 0, 64, 64, 64);
      gr.addColorStop(0, 'rgba(255,255,240,1)'); gr.addColorStop(0.2, 'rgba(255,250,220,0.5)'); gr.addColorStop(1, 'rgba(255,250,220,0)');
      g.fillStyle = gr; g.fillRect(0, 0, w, h);
    });
    for (const sx of [-1, 1]) for (const sy of [-1, 1]) {
      const x = sx * (HL + 17), y = sy * (HW + 15);
      const h = 36;
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.5, 0.9, h, 8), poleM);
      toV(x, y, h / 2, pole.position);
      pole.castShadow = this.q === 'high';
      this.scene.add(pole);
      const head = new THREE.Mesh(new THREE.BoxGeometry(7, 3.4, 0.5), lampM);
      toV(x, y, h + 1.2, head.position);
      head.lookAt(0, 0, 0);
      head.rotateY(PI);
      this.scene.add(head);
      if (on) {
        const gl = new THREE.Sprite(new THREE.SpriteMaterial({ map: glowTex, color: '#fff7dd', transparent: true, blending: THREE.AdditiveBlending, depthWrite: false, fog: false, opacity: st.time === 'night' ? 0.95 : 0.5 }));
        gl.position.copy(head.position).multiplyScalar(0.985);
        gl.scale.set(26, 26, 1);
        this.scene.add(gl);
      }
    }
  }

  // ---------------- الشاشة العملاقة ----------------
  buildJumbotron() {
    const c = document.createElement('canvas');
    c.width = 512; c.height = 256;
    const tex = new THREE.CanvasTexture(c);
    tex.colorSpace = THREE.SRGBColorSpace;
    this.jumbo = { c, g: c.getContext('2d'), tex, key: '' };
    const frame = new THREE.Mesh(new THREE.BoxGeometry(15, 8, 0.6), new THREE.MeshStandardMaterial({ color: '#15171c', roughness: 0.6 }));
    const screen = new THREE.Mesh(new THREE.PlaneGeometry(14, 7), new THREE.MeshBasicMaterial({ map: tex, fog: false }));
    screen.position.z = 0.31;
    const g = new THREE.Group();
    g.add(frame, screen);
    toV(HL + 32, 0, 27, g.position);
    g.rotation.y = -PI / 2;
    const leg = new THREE.Mesh(new THREE.BoxGeometry(0.8, 24, 0.8), new THREE.MeshStandardMaterial({ color: '#555' }));
    leg.position.set(0, -15, -0.5);
    g.add(leg);
    this.scene.add(g);
    this.drawJumbo('0 - 0', '');
  }

  drawJumbo(score, sub, flash = false) {
    const j = this.jumbo;
    const key = score + sub + flash;
    if (key === j.key) return;
    j.key = key;
    const g = j.g;
    g.fillStyle = flash ? '#d7263d' : '#05070c'; g.fillRect(0, 0, 512, 256);
    g.fillStyle = TEAMS[0].color; g.fillRect(20, 20, 130, 26);
    g.fillStyle = TEAMS[1].color; g.fillRect(362, 20, 130, 26);
    g.fillStyle = '#fff'; g.font = 'bold 20px Tahoma'; g.textAlign = 'center';
    g.fillText(TEAMS[0].name, 85, 40); g.fillText(TEAMS[1].name, 427, 40);
    g.font = 'bold 110px Arial Black, Tahoma'; g.fillStyle = flash ? '#ffd23f' : '#ffffff';
    g.fillText(score, 256, 160);
    g.font = 'bold 36px Tahoma'; g.fillStyle = '#ffd23f';
    g.fillText(sub, 256, 225);
    j.tex.needsUpdate = true;
  }

  // ---------------- أعلام الركنيات ----------------
  buildFlags() {
    const poleM = new THREE.MeshStandardMaterial({ color: '#f2f2f2' });
    this.cornerFlags = [];
    for (const sx of [-1, 1]) for (const sy of [-1, 1]) {
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.02, 0.02, 1.5, 6), poleM);
      toV(sx * HL, sy * HW, 0.75, pole.position);
      pole.castShadow = true;
      this.scene.add(pole);
      const flag = new THREE.Mesh(new THREE.PlaneGeometry(0.4, 0.3, 4, 1), new THREE.MeshStandardMaterial({ color: sx < 0 ? TEAMS[0].color : TEAMS[1].color, side: THREE.DoubleSide }));
      flag.geometry.translate(0.2, 0, 0);
      toV(sx * HL, sy * HW, 1.35, flag.position);
      this.scene.add(flag);
      this.cornerFlags.push(flag);
    }
  }

  // ---------------- الطقس ----------------
  buildWeather() {
    const w = this.st.weather;
    if (!w) return;
    const n = w === 'rain' ? (this.q === 'low' ? 1500 : 4000) : w === 'snow' ? (this.q === 'low' ? 1200 : 3500) : 400;
    const box = { x: 90, y: 40, z: 70 };
    if (w === 'rain') {
      const pos = new Float32Array(n * 6);
      for (let i = 0; i < n; i++) {
        const x = rnd(-box.x / 2, box.x / 2), y = rnd(0, box.y), z = rnd(-box.z / 2, box.z / 2);
        pos.set([x, y, z, x + 0.05, y - 0.7, z], i * 6);
      }
      const g = new THREE.BufferGeometry();
      g.setAttribute('position', new THREE.BufferAttribute(pos, 3));
      const m = new THREE.LineBasicMaterial({ color: '#b8c8d8', transparent: true, opacity: 0.45 });
      const lines = new THREE.LineSegments(g, m);
      lines.frustumCulled = false;
      this.scene.add(lines);
      this.weather = { kind: w, obj: lines, n, box, speed: 30 };
      this.lightningT = rnd(4, 9);
    } else {
      const pos = new Float32Array(n * 3);
      for (let i = 0; i < n; i++) pos.set([rnd(-box.x / 2, box.x / 2), rnd(0, box.y), rnd(-box.z / 2, box.z / 2)], i * 3);
      const g = new THREE.BufferGeometry();
      g.setAttribute('position', new THREE.BufferAttribute(pos, 3));
      const tex = canvasTex(32, 32, (gg) => {
        const gr = gg.createRadialGradient(16, 16, 0, 16, 16, 16);
        gr.addColorStop(0, 'rgba(255,255,255,1)'); gr.addColorStop(1, 'rgba(255,255,255,0)');
        gg.fillStyle = gr; gg.fillRect(0, 0, 32, 32);
      });
      const m = new THREE.PointsMaterial({ map: tex, size: w === 'snow' ? 0.22 : 0.12, color: w === 'snow' ? '#ffffff' : '#ffcf99', transparent: true, depthWrite: false, opacity: w === 'snow' ? 0.95 : 0.5 });
      const pts = new THREE.Points(g, m);
      pts.frustumCulled = false;
      this.scene.add(pts);
      this.weather = { kind: w, obj: pts, n, box, speed: w === 'snow' ? 1.6 : 0.4 };
    }
  }

  updateWeather(dt, focus) {
    const wth = this.weather;
    if (!wth) return;
    const pos = wth.obj.geometry.attributes.position;
    const a = pos.array;
    const { box, speed } = wth;
    const t = this.time;
    if (wth.kind === 'rain') {
      for (let i = 0; i < wth.n; i++) {
        const k = i * 6;
        a[k + 1] -= speed * dt; a[k + 4] -= speed * dt;
        a[k] += 1.5 * dt; a[k + 3] += 1.5 * dt;
        if (a[k + 4] < 0) {
          const x = focus.x + rnd(-box.x / 2, box.x / 2), z = focus.z + rnd(-box.z / 2, box.z / 2), y = box.y + rnd(0, 5);
          a[k] = x; a[k + 1] = y; a[k + 2] = z; a[k + 3] = x + 0.05; a[k + 4] = y - 0.7; a[k + 5] = z;
        }
      }
      this.lightningT -= dt;
      if (this.lightningT <= 0) {
        this.lightningT = rnd(6, 14);
        this.flash = 1;
        this.onThunder && this.onThunder();
      }
    } else {
      for (let i = 0; i < wth.n; i++) {
        const k = i * 3;
        a[k + 1] -= speed * dt * (0.6 + (i % 7) * 0.08);
        a[k] += Math.sin(t * 0.7 + i) * dt * 0.5 + (wth.kind === 'dust' ? 1.2 * dt : 0);
        a[k + 2] += Math.cos(t * 0.5 + i * 1.3) * dt * 0.4;
        if (a[k + 1] < 0 || Math.abs(a[k] - focus.x) > box.x / 2 + 2) {
          a[k] = focus.x + rnd(-box.x / 2, box.x / 2); a[k + 1] = wth.kind === 'dust' ? rnd(0, 6) : box.y; a[k + 2] = focus.z + rnd(-box.z / 2, box.z / 2);
        }
      }
    }
    pos.needsUpdate = true;
  }

  // ---------------- التحديث ----------------
  update(dt, focus, info = {}) {
    this.time += dt;
    const t = this.time;
    // حماس الجمهور
    this.excite += ((info.excite || 0) - this.excite) * Math.min(1, dt * 2);
    for (const k of [0, 1]) this.teamExcite[k] = Math.max(0, this.teamExcite[k] - dt * 0.12);
    const U = this.crowdU;
    U.uTime.value = t;
    U.uExcite.value = this.excite;
    U.uTeam0.value = Math.min(1, this.teamExcite[0]);
    U.uTeam1.value = Math.min(1, this.teamExcite[1]);
    if (this.wave > -9) { this.wave += dt * 1.1; U.uWave.value = this.wave; if (this.wave > PI * 3) { this.wave = -10; U.uWave.value = -10; } }
    // إعلانات متحركة
    this.adTex.offset.x = (t * 0.03) % 1;
    this.goalAdTex.offset.x = (t * 0.25) % 1;
    // أعلام
    for (const f of this.cornerFlags) f.rotation.y = Math.sin(t * 3 + f.position.x) * 0.5 + 0.4;
    // الغيوم
    if (this.clouds) for (const c of this.clouds) { c.a += c.s * dt; c.sp.position.x = Math.cos(c.a) * c.d; c.sp.position.z = Math.sin(c.a) * c.d; }
    // وميض الكاميرات
    const fs = this.flashSizes;
    const rate = 0.004 + this.excite * 0.06;
    for (let i = 0; i < fs.length; i++) {
      if (fs[i] > 0) fs[i] = Math.max(0, fs[i] - dt * 6);
      else if (Math.random() < rate) fs[i] = rnd(0.4, 1.0);
    }
    this.flashes.geometry.attributes.size.needsUpdate = true;
    // نيون
    if (this.neonEdges) this.neonEdges.forEach((e, i) => e.material.color.setHSL((t * 0.08 + i * 0.13) % 1, 1, 0.55 + this.excite * 0.2));
    // البرق
    if (this.flash > 0) {
      this.flash = Math.max(0, this.flash - dt * 3);
      const f = this.flash > 0.6 || (this.flash > 0.25 && this.flash < 0.4) ? 1 : 0;
      this.hemi.intensity = this.hemiBase + f * 3;
    } else this.hemi.intensity = this.hemiBase;
    this.updateNets(dt);
    this.updateWeather(dt, focus || new THREE.Vector3());
    // الظل يتبع مركز اللعب
    if (this.sun.castShadow && focus) {
      const off = new THREE.Vector3(...this.st.sun.pos).normalize().multiplyScalar(90);
      const fx = Math.max(-12, Math.min(12, focus.x));
      this.sun.position.set(fx + off.x, off.y, off.z);
      this.sun.target.position.set(fx, 0, 0);
    }
    // الشاشة
    if (info.jumbo) this.drawJumbo(info.jumbo.score, info.jumbo.sub, info.jumbo.flash);
    this.boardMat.map = info.goalBoards ? this.goalAdTex : this.adTex;
    this.boardMat.emissiveMap = this.boardMat.map;
  }

  goalCelebration(team) {
    this.teamExcite[team] = 1.6;
    if (Math.random() < 0.6) this.wave = 0;
  }
  startWave() { if (this.wave < -9) this.wave = 0; }

  dispose() {
    this.scene.traverse((o) => {
      if (o.geometry) o.geometry.dispose();
      if (o.material) {
        const ms = Array.isArray(o.material) ? o.material : [o.material];
        for (const m of ms) { for (const k in m) if (m[k] && m[k].isTexture) m[k].dispose(); m.dispose(); }
      }
    });
  }
}
