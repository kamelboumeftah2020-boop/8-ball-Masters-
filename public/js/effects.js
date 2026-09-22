// المؤثرات البصرية: جزيئات، نار، جليد، برق، موجات صدمية، ألعاب نارية، قصاصات ملونة
import * as THREE from 'three';
import { toV } from './scene.js';
import { BALL_R } from '/shared/constants.js';

const rnd = (a, b) => a + Math.random() * (b - a);
const PI = Math.PI;

class ParticleSystem {
  constructor(scene, max, additive) {
    this.max = max;
    this.n = 0;
    this.p = new Float32Array(max * 3);
    this.v = new Float32Array(max * 3);
    this.c = new Float32Array(max * 3);
    this.life = new Float32Array(max);
    this.maxLife = new Float32Array(max);
    this.size = new Float32Array(max);
    this.grav = new Float32Array(max);
    this.drag = new Float32Array(max);
    this.alpha = new Float32Array(max);
    const g = new THREE.BufferGeometry();
    this.posAttr = new THREE.BufferAttribute(this.p, 3).setUsage(THREE.DynamicDrawUsage);
    this.colAttr = new THREE.BufferAttribute(this.c, 3).setUsage(THREE.DynamicDrawUsage);
    this.sizeAttr = new THREE.BufferAttribute(new Float32Array(max), 1).setUsage(THREE.DynamicDrawUsage);
    this.alphaAttr = new THREE.BufferAttribute(this.alpha, 1).setUsage(THREE.DynamicDrawUsage);
    g.setAttribute('position', this.posAttr);
    g.setAttribute('color', this.colAttr);
    g.setAttribute('size', this.sizeAttr);
    g.setAttribute('alpha', this.alphaAttr);
    const mat = new THREE.ShaderMaterial({
      transparent: true, depthWrite: false,
      blending: additive ? THREE.AdditiveBlending : THREE.NormalBlending,
      vertexShader: `attribute float size; attribute float alpha; attribute vec3 color; varying vec3 vC; varying float vA;
        uniform float uScale;
        void main(){ vC = color; vA = alpha; vec4 mv = modelViewMatrix * vec4(position,1.0); gl_PointSize = size * uScale / -mv.z; gl_Position = projectionMatrix * mv; }`,
      fragmentShader: `varying vec3 vC; varying float vA; uniform float uSoft;
        void main(){ vec2 d = gl_PointCoord - 0.5; float r = length(d);
          float a = uSoft > 0.5 ? smoothstep(0.5, 0.0, r) : step(r, 0.5) * (0.6 + 0.4*step(abs(d.x),0.25));
          if (a < 0.01) discard; gl_FragColor = vec4(vC, a * vA); }`,
      uniforms: { uScale: { value: 600 }, uSoft: { value: additive ? 1 : 0 } },
    });
    this.points = new THREE.Points(g, mat);
    this.points.frustumCulled = false;
    this.points.renderOrder = 5;
    scene.add(this.points);
  }

  emit(x, y, z, vx, vy, vz, color, size, life, grav = 0, drag = 0) {
    let i = this.n;
    if (i >= this.max) { i = Math.floor(Math.random() * this.max); } else this.n++;
    this.p[i * 3] = x; this.p[i * 3 + 1] = y; this.p[i * 3 + 2] = z;
    this.v[i * 3] = vx; this.v[i * 3 + 1] = vy; this.v[i * 3 + 2] = vz;
    const c = typeof color === 'string' || typeof color === 'number' ? _col.set(color) : color;
    this.c[i * 3] = c.r; this.c[i * 3 + 1] = c.g; this.c[i * 3 + 2] = c.b;
    this.life[i] = life; this.maxLife[i] = life; this.size[i] = size; this.grav[i] = grav; this.drag[i] = drag;
  }

  update(dt) {
    const sz = this.sizeAttr.array;
    for (let i = 0; i < this.n; i++) {
      this.life[i] -= dt;
      if (this.life[i] <= 0) {
        // حذف بالتبديل مع الأخير
        const j = --this.n;
        if (i !== j) {
          for (const arr of [this.p, this.v, this.c]) { arr[i * 3] = arr[j * 3]; arr[i * 3 + 1] = arr[j * 3 + 1]; arr[i * 3 + 2] = arr[j * 3 + 2]; }
          this.life[i] = this.life[j]; this.maxLife[i] = this.maxLife[j]; this.size[i] = this.size[j]; this.grav[i] = this.grav[j]; this.drag[i] = this.drag[j];
          i--;
        }
        continue;
      }
      const k = i * 3;
      const d = Math.max(0, 1 - this.drag[i] * dt);
      this.v[k] *= d; this.v[k + 1] = this.v[k + 1] * d - this.grav[i] * dt; this.v[k + 2] *= d;
      this.p[k] += this.v[k] * dt; this.p[k + 1] += this.v[k + 1] * dt; this.p[k + 2] += this.v[k + 2] * dt;
      if (this.p[k + 1] < 0.02 && this.grav[i] > 0) { this.p[k + 1] = 0.02; this.v[k + 1] *= -0.3; this.v[k] *= 0.6; this.v[k + 2] *= 0.6; }
      const t = this.life[i] / this.maxLife[i];
      this.alpha[i] = Math.min(1, t * 2.5);
      sz[i] = this.size[i] * (0.4 + 0.6 * t);
    }
    this.points.geometry.setDrawRange(0, this.n);
    this.posAttr.needsUpdate = true; this.colAttr.needsUpdate = true; this.sizeAttr.needsUpdate = true; this.alphaAttr.needsUpdate = true;
  }
}
const _col = new THREE.Color();

export class Effects {
  constructor(world, quality) {
    this.world = world;
    this.scene = world.scene;
    this.q = quality;
    const mul = quality === 'low' ? 0.4 : quality === 'medium' ? 0.75 : 1;
    this.mul = mul;
    this.add = new ParticleSystem(this.scene, Math.floor(3000 * mul), true);
    this.norm = new ParticleSystem(this.scene, Math.floor(3000 * mul), false);
    this.temp = []; // كائنات مؤقتة {obj, t, T, update}
    this.zoneObjs = new Map();
    this.lightT = 0;
    // مسار الكرة
    const trailN = 24;
    this.trail = { n: trailN, pts: [], geo: new THREE.BufferGeometry() };
    this.trail.geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(trailN * 3), 3));
    this.trail.line = new THREE.Line(this.trail.geo, new THREE.LineBasicMaterial({ color: '#ffffff', transparent: true, opacity: 0 }));
    this.trail.line.visible = false;
    this.trail.line.frustumCulled = false;
    this.scene.add(this.trail.line);
    this.ringGeo = new THREE.RingGeometry(0.8, 1, 48);
    this.discTex = this.makeDiscTex();
  }

  makeDiscTex() {
    const c = document.createElement('canvas');
    c.width = c.height = 256;
    const g = c.getContext('2d');
    const gr = g.createRadialGradient(128, 128, 20, 128, 128, 128);
    gr.addColorStop(0, 'rgba(200,240,255,0.15)'); gr.addColorStop(0.8, 'rgba(160,230,255,0.45)'); gr.addColorStop(1, 'rgba(160,230,255,0)');
    g.fillStyle = gr; g.fillRect(0, 0, 256, 256);
    g.strokeStyle = 'rgba(255,255,255,0.6)'; g.lineWidth = 2;
    for (let i = 0; i < 12; i++) {
      const a = (i / 12) * PI * 2;
      g.beginPath(); g.moveTo(128, 128);
      let x = 128, y = 128;
      for (let k = 0; k < 6; k++) { x += Math.cos(a + rnd(-0.4, 0.4)) * 18; y += Math.sin(a + rnd(-0.4, 0.4)) * 18; g.lineTo(x, y); }
      g.stroke();
    }
    const t = new THREE.CanvasTexture(c);
    t.colorSpace = THREE.SRGBColorSpace;
    return t;
  }

  // ---------- مؤثرات عامة ----------
  kickDust(x, y, power, header) {
    const v = toV(x, y, header ? 1.8 : 0.1);
    const n = Math.floor((6 + power * 10) * this.mul);
    for (let i = 0; i < n; i++) {
      const a = Math.random() * PI * 2, s = rnd(0.5, 2 + power * 2);
      if (header) this.add.emit(v.x, v.y, v.z, Math.cos(a) * s, rnd(0, 1), Math.sin(a) * s, '#ffffff', 0.12, 0.25);
      else this.norm.emit(v.x, v.y, v.z, Math.cos(a) * s, rnd(1, 3), Math.sin(a) * s, i % 2 ? '#3a7a28' : '#6b5a3a', 0.08, rnd(0.4, 0.8), 9, 1);
    }
  }

  slideSpray(x, y, vx, vy) {
    const v = toV(x, y, 0.1);
    for (let i = 0; i < 3 * this.mul + 1; i++) {
      this.norm.emit(v.x, v.y, v.z, -vx * 0.2 + rnd(-1, 1), rnd(1, 2.5), vy * 0.2 + rnd(-1, 1), i % 2 ? '#3f8a2d' : '#7a6440', 0.09, 0.6, 9, 1);
    }
  }

  sprintDust(x, y) {
    if (Math.random() > 0.35 * this.mul) return;
    const v = toV(x + rnd(-0.2, 0.2), y + rnd(-0.2, 0.2), 0.05);
    this.norm.emit(v.x, v.y, v.z, rnd(-0.3, 0.3), rnd(0.3, 0.8), rnd(-0.3, 0.3), this.world.st.snowy ? '#ffffff' : '#8a7a5a', 0.1, 0.4, 2, 2);
  }

  splash(x, y) {
    const v = toV(x, y, 0.05);
    for (let i = 0; i < 6 * this.mul; i++) this.add.emit(v.x, v.y, v.z, rnd(-1.5, 1.5), rnd(1, 2.5), rnd(-1.5, 1.5), '#a8c8e8', 0.08, 0.4, 9);
  }

  ballFx(b, fx, dt) {
    const v = toV(b[0], b[1], b[2]);
    const sp = Math.hypot(b[3], b[4], b[5]);
    if (fx === 1) {
      for (let i = 0; i < 4; i++) this.add.emit(v.x + rnd(-0.15, 0.15), v.y + rnd(-0.15, 0.15), v.z + rnd(-0.15, 0.15), rnd(-0.5, 0.5), rnd(0.5, 2), rnd(-0.5, 0.5), i % 3 ? '#ff6a00' : '#ffd23f', rnd(0.4, 0.8), rnd(0.25, 0.5));
      this.norm.emit(v.x, v.y, v.z, 0, 0.8, 0, '#333333', 0.5, 0.8, -0.5);
    } else if (fx === 2) {
      for (let i = 0; i < 2; i++) this.add.emit(v.x, v.y, v.z, rnd(-0.3, 0.3), rnd(-0.3, 0.3), rnd(-0.3, 0.3), i ? '#2ecc71' : '#b6ffcf', 0.35, 0.5);
    }
    // مسار أبيض للكرات السريعة
    const tr = this.trail;
    if (tr.pts.length && tr.pts[0].distanceTo(v) > 3) tr.pts.length = 0;
    tr.pts.unshift(v.clone());
    if (tr.pts.length > tr.n) tr.pts.pop();
    const arr = tr.geo.attributes.position.array;
    for (let i = 0; i < tr.n; i++) {
      const p = tr.pts[Math.min(i, tr.pts.length - 1)];
      arr[i * 3] = p.x; arr[i * 3 + 1] = p.y; arr[i * 3 + 2] = p.z;
    }
    tr.geo.attributes.position.needsUpdate = true;
    const target = sp > 16 || fx ? Math.min(0.75, (sp - 12) / 14) : 0;
    tr.line.material.opacity += (target - tr.line.material.opacity) * Math.min(1, dt * 10);
    tr.line.material.color.set(fx === 1 ? '#ffa040' : fx === 2 ? '#7dffb0' : '#ffffff');
    tr.line.visible = tr.line.material.opacity > 0.02;
  }

  playerBuffs(p3, flags, x, y) {
    const v = toV(x, y, 0);
    if (flags & 1) { // نار
      for (let i = 0; i < 2; i++) this.add.emit(v.x + rnd(-0.3, 0.3), 0.1 + rnd(0, 0.4), v.z + rnd(-0.3, 0.3), 0, rnd(1, 2.2), 0, i ? '#ff6a00' : '#ffb000', rnd(0.3, 0.6), 0.4);
    }
    if (flags & 2) { // سرعة
      this.add.emit(v.x + rnd(-0.2, 0.2), rnd(0.3, 1.6), v.z + rnd(-0.2, 0.2), 0, 0, 0, '#ffe14d', 0.3, 0.35);
      if (Math.random() < 0.3) this.add.emit(v.x, rnd(0.2, 1.8), v.z, rnd(-3, 3), rnd(-1, 1), rnd(-3, 3), '#ffffff', 0.12, 0.2);
    }
    if (flags & 4) { // التواء
      const a = this.world.time * 8;
      this.add.emit(v.x + Math.cos(a) * 0.6, 0.9 + Math.sin(a * 0.5) * 0.4, v.z + Math.sin(a) * 0.6, 0, 0.3, 0, '#2ecc71', 0.25, 0.5);
    }
    if (flags & 8) { // مغناطيس
      const a = this.world.time * 10;
      this.add.emit(v.x + Math.cos(a) * 0.8, 1, v.z + Math.sin(a) * 0.8, 0, 0, 0, '#ff3355', 0.3, 0.3);
    }
    if (flags & 16) { // مجمد
      if (Math.random() < 0.5) this.add.emit(v.x + rnd(-0.4, 0.4), rnd(0.1, 1.8), v.z + rnd(-0.4, 0.4), 0, -0.3, 0, '#bff0ff', 0.2, 0.6);
    }
  }

  magnetPull(px, py, bx, by, bz) {
    const a = toV(bx, by, bz), b = toV(px, py, 1);
    for (let i = 0; i < 3; i++) {
      const t = Math.random();
      this.add.emit(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t, (b.x - a.x) * 0.5, 0, (b.z - a.z) * 0.5, '#ff4466', 0.25, 0.3);
    }
  }

  // ---------- القدرات ----------
  ability(kind, x, y, ev, players) {
    const v = toV(x, y, 0);
    switch (kind) {
      case 'fire':
        this.burst(v, '#ff6a00', 40, 5, 0.6, true, 1);
        this.light(v, '#ff7a20', 12, 0.6);
        break;
      case 'curve':
        this.burst(v, '#2ecc71', 30, 4, 0.6, true, 1);
        break;
      case 'dash':
        this.burst(v, '#ffe14d', 30, 6, 0.4, true, 0.8);
        this.shock(v, '#ffe14d', 4, 0.4);
        break;
      case 'quake':
        this.shock(v, '#d9a066', 11, 0.7, 0.4);
        this.shock(v, '#ffffff', 7, 0.5, 0.2);
        for (let i = 0; i < 60 * this.mul; i++) {
          const a = Math.random() * PI * 2, s = rnd(3, 9);
          this.norm.emit(v.x, 0.2, v.z, Math.cos(a) * s, rnd(2, 6), Math.sin(a) * s, i % 2 ? '#7a5a3a' : '#4a7a30', rnd(0.12, 0.25), rnd(0.6, 1.2), 12, 1);
        }
        this.world.shake = Math.max(this.world.shake || 0, 0.6);
        break;
      case 'ice':
        for (let i = 0; i < 80 * this.mul; i++) {
          const a = Math.random() * PI * 2, s = rnd(2, 8);
          this.add.emit(v.x, 0.5, v.z, Math.cos(a) * s, rnd(0.5, 3), Math.sin(a) * s, i % 2 ? '#bff0ff' : '#ffffff', rnd(0.15, 0.35), rnd(0.6, 1.2), 1, 1.5);
        }
        this.shock(v, '#7fdbff', 9, 0.6);
        this.light(v, '#7fdbff', 10, 0.5);
        break;
      case 'blink': {
        const f = toV(ev.fx, ev.fy, 0);
        this.puff(f, '#9b5cff');
        this.puff(v, '#d6b8ff');
        // خط بين النقطتين
        for (let i = 0; i < 25 * this.mul; i++) {
          const t = Math.random();
          this.add.emit(f.x + (v.x - f.x) * t, rnd(0.3, 1.6), f.z + (v.z - f.z) * t, 0, rnd(0, 0.5), 0, '#b388ff', 0.25, rnd(0.2, 0.5));
        }
        break;
      }
      case 'pull':
        this.shock(v, '#ff3355', 6, 0.5);
        break;
      case 'bolt': {
        const tgt = players && ev.v != null ? players[ev.v] : null;
        const tv = tgt ? toV(tgt.x, tgt.y, 0) : v;
        this.lightning(tv);
        this.light(tv, '#9fe8ff', 20, 0.35);
        this.world.flash = Math.max(this.world.flash, 0.5);
        break;
      }
      case 'wall':
        this.burst(v, '#ffd700', 25, 3, 0.5, true, 1.2);
        break;
    }
  }

  burst(v, color, n, speed, life, additive = true, y = 1) {
    const sys = additive ? this.add : this.norm;
    for (let i = 0; i < n * this.mul; i++) {
      const a = Math.random() * PI * 2, e = rnd(-0.3, 1), s = rnd(speed * 0.3, speed);
      sys.emit(v.x, y, v.z, Math.cos(a) * Math.cos(e) * s, Math.sin(e) * s, Math.sin(a) * Math.cos(e) * s, color, rnd(0.2, 0.45), rnd(life * 0.5, life), 0, 2);
    }
  }

  puff(v, color) {
    for (let i = 0; i < 30 * this.mul; i++) {
      this.add.emit(v.x + rnd(-0.3, 0.3), rnd(0.1, 1.9), v.z + rnd(-0.3, 0.3), rnd(-1, 1), rnd(0, 1), rnd(-1, 1), color, rnd(0.3, 0.6), rnd(0.3, 0.7), 0, 2);
      this.norm.emit(v.x + rnd(-0.3, 0.3), rnd(0.1, 1.9), v.z + rnd(-0.3, 0.3), rnd(-0.5, 0.5), rnd(0, 0.6), rnd(-0.5, 0.5), '#2a1f3d', rnd(0.3, 0.6), rnd(0.4, 0.8), 0, 2);
    }
  }

  shock(v, color, radius, dur, y = 0.08) {
    const m = new THREE.Mesh(this.ringGeo, new THREE.MeshBasicMaterial({ color, transparent: true, opacity: 0.9, side: THREE.DoubleSide, depthWrite: false, blending: THREE.AdditiveBlending }));
    m.rotation.x = -PI / 2;
    m.position.set(v.x, y, v.z);
    this.scene.add(m);
    this.temp.push({ obj: m, t: 0, T: dur, update: (o, k) => { const s = 0.3 + k * radius; o.scale.set(s, s, s); o.material.opacity = 0.9 * (1 - k); } });
  }

  light(v, color, intensity, dur) {
    const L = this.world.fxLight;
    L.color.set(color);
    L.position.set(v.x, 2.5, v.z);
    this.lightMax = intensity; this.lightT = dur; this.lightDur = dur;
  }

  lightning(tv) {
    const pts = [];
    let x = tv.x + rnd(-3, 3), y = 30, z = tv.z + rnd(-3, 3);
    while (y > 0.3) {
      pts.push(new THREE.Vector3(x, y, z));
      y -= rnd(1.5, 3.5);
      x += (tv.x - x) * 0.25 + rnd(-1.2, 1.2);
      z += (tv.z - z) * 0.25 + rnd(-1.2, 1.2);
    }
    pts.push(new THREE.Vector3(tv.x, 0.9, tv.z));
    for (let k = 0; k < 2; k++) {
      const g = new THREE.BufferGeometry().setFromPoints(pts.map((p, i) => (k ? p.clone().add(new THREE.Vector3(rnd(-0.3, 0.3), 0, rnd(-0.3, 0.3))) : p)));
      const l = new THREE.Line(g, new THREE.LineBasicMaterial({ color: k ? '#9fe8ff' : '#ffffff', transparent: true, opacity: 1, blending: THREE.AdditiveBlending }));
      l.frustumCulled = false;
      this.scene.add(l);
      this.temp.push({ obj: l, t: 0, T: 0.45, update: (o, kk) => { o.material.opacity = (Math.random() < 0.7 ? 1 : 0.2) * (1 - kk); } });
    }
    for (let i = 0; i < 40 * this.mul; i++) {
      const a = Math.random() * PI * 2, s = rnd(2, 7);
      this.add.emit(tv.x, 0.8, tv.z, Math.cos(a) * s, rnd(1, 5), Math.sin(a) * s, i % 2 ? '#9fe8ff' : '#ffffff', 0.15, rnd(0.3, 0.6), 10);
    }
  }

  // ---------- المناطق (جليد/جدار) ----------
  syncZones(zones) {
    const seen = new Set();
    for (const z of zones || []) {
      const key = `${z[0]}_${Math.round(z[1] * 2)}_${Math.round(z[2] * 2)}`;
      seen.add(key);
      let o = this.zoneObjs.get(key);
      if (!o) {
        if (z[0] === 'ice') {
          const m = new THREE.Mesh(new THREE.CircleGeometry(z[3], 48), new THREE.MeshBasicMaterial({ map: this.discTex, transparent: true, depthWrite: false, opacity: 0.9 }));
          m.rotation.x = -PI / 2;
          toV(z[1], z[2], 0.04, m.position);
          o = m;
        } else {
          const g = new THREE.BoxGeometry(z[3], 2.4, 0.25);
          const m = new THREE.Mesh(g, new THREE.MeshStandardMaterial({ color: '#ffd700', emissive: '#ffb000', emissiveIntensity: 1.2, transparent: true, opacity: 0.45, depthWrite: false }));
          toV(z[1], z[2], 1.2, m.position);
          m.rotation.y = z[4];
          const edges = new THREE.LineSegments(new THREE.EdgesGeometry(g), new THREE.LineBasicMaterial({ color: '#fff6b0' }));
          m.add(edges);
          o = m;
        }
        this.scene.add(o);
        this.zoneObjs.set(key, o);
      }
      const k = z[5];
      if (z[0] === 'ice') {
        o.material.opacity = Math.min(1, k * 3) * 0.9;
        if (Math.random() < 0.5 * this.mul) {
          const a = Math.random() * PI * 2, r = Math.random() * z[3];
          this.add.emit(o.position.x + Math.cos(a) * r, 0.1, o.position.z + Math.sin(a) * r, 0, rnd(0.5, 1.5), 0, '#dff8ff', 0.2, 0.8);
        }
      } else {
        o.material.opacity = (0.3 + Math.sin(this.world.time * 12) * 0.08) * Math.min(1, k * 4);
      }
    }
    for (const [key, o] of this.zoneObjs) {
      if (!seen.has(key)) {
        this.scene.remove(o);
        o.geometry.dispose();
        o.material.dispose();
        this.zoneObjs.delete(key);
      }
    }
  }

  wallHit(x, y) {
    const v = toV(x, y, 0);
    this.burst(v, '#ffd700', 20, 4, 0.4, true, 1);
  }

  // ---------- الاحتفال ----------
  confetti(team, colors) {
    const n = Math.floor(400 * this.mul);
    for (let i = 0; i < n; i++) {
      const side = Math.random() < 0.5 ? -1 : 1;
      const x = rnd(-30, 30), z = side * rnd(20, 28);
      this.norm.emit(x, rnd(8, 16), z, rnd(-2, 2), rnd(2, 6), -side * rnd(2, 8), colors[i % colors.length], rnd(0.12, 0.22), rnd(3, 5), 2.5, 0.9);
    }
  }

  fireworks(colors, count = 6) {
    for (let k = 0; k < count; k++) {
      setTimeout(() => {
        const x = rnd(-35, 35), y = rnd(28, 42), z = rnd(-45, -25);
        const col = colors[k % colors.length];
        const n = Math.floor(90 * this.mul);
        for (let i = 0; i < n; i++) {
          const a = Math.random() * PI * 2, e = Math.acos(rnd(-1, 1)) - PI / 2, s = rnd(8, 12);
          this.add.emit(x, y, z, Math.cos(a) * Math.cos(e) * s, Math.sin(e) * s, Math.sin(a) * Math.cos(e) * s, i % 5 ? col : '#ffffff', 0.9, rnd(1.2, 1.8), 4, 1.2);
        }
        this.onBoom && this.onBoom();
      }, k * 380 + Math.random() * 200);
    }
  }

  update(dt) {
    this.add.update(dt);
    this.norm.update(dt);
    for (let i = this.temp.length - 1; i >= 0; i--) {
      const e = this.temp[i];
      e.t += dt;
      const k = Math.min(1, e.t / e.T);
      e.update(e.obj, k);
      if (e.t >= e.T) {
        this.scene.remove(e.obj);
        if (e.obj.geometry !== this.ringGeo) e.obj.geometry.dispose();
        e.obj.material.dispose();
        this.temp.splice(i, 1);
      }
    }
    if (this.lightT > 0) {
      this.lightT -= dt;
      this.world.fxLight.intensity = Math.max(0, (this.lightT / this.lightDur)) * this.lightMax;
    } else this.world.fxLight.intensity = 0;
  }

  setScale(h) {
    for (const s of [this.add, this.norm]) s.points.material.uniforms.uScale.value = h * 0.9;
  }
}
