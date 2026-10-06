// يحوّل ركلات كرة القدم الحقيقية من قاعدة CMU (ASF/AMC — تصوير حركة Vicon بـ 120 إطار/ث)
// إلى هيكل لاعبي اللعبة (أسماء عظام Unreal) → scripts/data/cmu-clips.json
// الاستعمال: CMU=/folder/with/asf+amc ASSETS=/folder/with/quaternius node scripts/retarget-cmu.mjs
// طريقة التحويل: لكل عظمة نطبق دوران العظمة المقابلة في العالم بعد محاذاة اتجاهات وضعية الراحة
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { NodeIO } from '@gltf-transform/core';
import { ALL_EXTENSIONS } from '@gltf-transform/extensions';
import * as THREE from 'three';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const CMU = process.env.CMU, ASSETS = process.env.ASSETS;
const FPS_OUT = 30;
const D2R = Math.PI / 180;

// ---------- ASF / AMC ----------
function parseASF(txt) {
  const bones = { root: { name: 'root', dir: new THREE.Vector3(), len: 0, axis: [0, 0, 0], dofs: ['rx', 'ry', 'rz'], children: [] } };
  const lines = txt.split(/\r?\n/);
  let sec = '', cur = null;
  for (const raw of lines) {
    const l = raw.trim();
    if (!l || l.startsWith('#')) continue;
    if (l.startsWith(':')) { sec = l.split(/\s+/)[0]; continue; }
    const t = l.split(/\s+/);
    if (sec === ':bonedata') {
      if (t[0] === 'begin') cur = { dofs: [], children: [] };
      else if (t[0] === 'end') { bones[cur.name] = cur; cur = null; }
      else if (t[0] === 'name') cur.name = t[1];
      else if (t[0] === 'direction') cur.dir = new THREE.Vector3(+t[1], +t[2], +t[3]).normalize();
      else if (t[0] === 'length') cur.len = +t[1];
      else if (t[0] === 'axis') cur.axis = [+t[1], +t[2], +t[3]];
      else if (t[0] === 'dof') cur.dofs = t.slice(1);
    } else if (sec === ':hierarchy') {
      if (t[0] === 'begin' || t[0] === 'end') continue;
      for (const c of t.slice(1)) { bones[c].parent = t[0]; bones[t[0]].children.push(c); }
    }
  }
  for (const b of Object.values(bones)) {
    // C = Rz·Ry·Rx (محاور ثابتة XYZ)
    b.C = new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(b.axis[0] * D2R, b.axis[1] * D2R, b.axis[2] * D2R, 'ZYX'));
    b.Cinv = b.C.clone().invert();
  }
  return bones;
}
function parseAMC(txt) {
  const frames = [];
  let f = null;
  for (const raw of txt.split(/\r?\n/)) {
    const l = raw.trim();
    if (!l || l.startsWith('#') || l.startsWith(':')) continue;
    if (/^\d+$/.test(l)) { f = {}; frames.push(f); continue; }
    const t = l.split(/\s+/);
    f[t[0]] = t.slice(1).map(Number);
  }
  return frames;
}
// الحركة الأمامية: مصفوفة العالم + موضع نهاية كل عظمة
function fk(bones, frame) {
  const out = {};
  const visit = (name, parentM, parentP) => {
    const b = bones[name];
    let M, P;
    if (name === 'root') {
      const v = frame.root || [0, 0, 0, 0, 0, 0];
      M = new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(v[3] * D2R, v[4] * D2R, v[5] * D2R, 'ZYX'));
      P = new THREE.Vector3(v[0], v[1], v[2]);
    } else {
      const r = [0, 0, 0];
      const v = frame[name] || [];
      b.dofs.forEach((d, i) => { r[{ rx: 0, ry: 1, rz: 2 }[d]] = (v[i] || 0) * D2R; });
      const R = new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(r[0], r[1], r[2], 'ZYX'));
      M = parentM.clone().multiply(b.C).multiply(R).multiply(b.Cinv);
      P = parentP.clone().add(b.dir.clone().transformDirection(M).multiplyScalar(b.len));
    }
    out[name] = { M, P, start: parentP ? parentP.clone() : P.clone() };
    for (const c of b.children) visit(c, M, P);
  };
  visit('root', null, null);
  return out;
}

// ---------- الهيكل الهدف ----------
async function loadTarget() {
  const find = (re) => { const out = []; const walk = (d) => { for (const f of fs.readdirSync(d, { withFileTypes: true })) { const p = path.join(d, f.name); if (f.isDirectory()) walk(p); else if (re.test(p)) out.push(p); } }; walk(ASSETS); return out[0]; };
  const doc = await new NodeIO().registerExtensions(ALL_EXTENSIONS).read(find(/Godot - UE[\/\\]Superhero_Male_FullBody\.gltf$/));
  const skin = doc.getRoot().listSkins().find((s) => s.listJoints().length > 30);
  const nodes = new Map();
  const make = (n) => {
    const o = new THREE.Object3D();
    o.name = n.getName();
    o.position.fromArray(n.getTranslation()); o.quaternion.fromArray(n.getRotation()); o.scale.fromArray(n.getScale());
    nodes.set(o.name, o);
    for (const c of n.listChildren()) o.add(make(c));
    return o;
  };
  const rootNode = skin.listJoints().find((j) => j.getName() === 'root');
  const root = make(rootNode);
  root.updateMatrixWorld(true);
  return { root, nodes, order: [...nodes.keys()] };
}

// العظمة الهدف ← عظمة CMU + العظمة الابنة لتحديد الاتجاه
// الذراعان: نأخذ الاتجاه فقط (الالتواء طبيعي من الجسم) — أنظف من نسخ دوران CMU الكامل
const SWING = new Set(['upperarm_l', 'lowerarm_l', 'hand_l', 'upperarm_r', 'lowerarm_r', 'hand_r']);
const MAP = {
  pelvis: ['root'], spine_01: ['lowerback', 'spine_02'], spine_02: ['upperback', 'spine_03'], spine_03: ['thorax', 'neck_01'],
  neck_01: ['lowerneck', 'Head'], Head: ['upperneck'],
  upperarm_l: ['lhumerus', 'lowerarm_l'], lowerarm_l: ['lradius', 'hand_l'], hand_l: ['lhand', 'middle_01_l'],
  upperarm_r: ['rhumerus', 'lowerarm_r'], lowerarm_r: ['rradius', 'hand_r'], hand_r: ['rhand', 'middle_01_r'],
  thigh_l: ['lfemur', 'calf_l'], calf_l: ['ltibia', 'foot_l'], foot_l: ['lfoot', 'ball_l'], ball_l: ['ltoes', 'ball_leaf_l'],
  thigh_r: ['rfemur', 'calf_r'], calf_r: ['rtibia', 'foot_r'], foot_r: ['rfoot', 'ball_r'], ball_r: ['rtoes', 'ball_leaf_r'],
};

function retarget(T, bones, frames, opts) {
  const { nodes, order } = T;
  const rest = {};
  for (const n of order) { const o = nodes.get(n); rest[n] = { local: o.quaternion.clone(), world: o.getWorldQuaternion(new THREE.Quaternion()), pos: o.getWorldPosition(new THREE.Vector3()), localPos: o.position.clone() }; }
  // محاذاة اتجاهات الراحة: A = الدوران من اتجاه العظمة الهدف إلى اتجاه عظمة CMU
  const align = {};
  for (const [tn, [sn, child]] of Object.entries(MAP)) {
    if (!child) { align[tn] = new THREE.Quaternion(); continue; }
    const dt = rest[child].pos.clone().sub(rest[tn].pos).normalize();
    let ds = bones[sn].dir.clone();
    if (opts.mirror) ds.x *= -1;
    align[tn] = new THREE.Quaternion().setFromUnitVectors(dt, ds);
  }
  // وضعية CMU: إن كانت معكوسة نبدّل اليسار باليمين ونعكس المحور X
  const S = new THREE.Matrix4().makeScale(-1, 1, 1);
  const srcName = (sn) => (opts.mirror ? sn.replace(/^l(?=[a-z])/, '#').replace(/^r(?=[a-z])/, 'l').replace(/^#/, 'r') : sn);
  // المقياس: ارتفاع الورك
  const restFK = fk(bones, {});
  const minY = Math.min(...Object.values(restFK).map((v) => v.P.y));
  const scale = rest.pelvis.pos.y / (0 - minY);
  const out = [];
  for (const fr of frames) {
    const F = fk(bones, fr);
    const world = new Map();
    const frameOut = { rot: {}, pelvisPos: null };
    for (const n of order) {
      const o = nodes.get(n);
      const parentQ = o.parent && world.has(o.parent.name) ? world.get(o.parent.name) : (o.parent ? o.parent.getWorldQuaternion(new THREE.Quaternion()) : new THREE.Quaternion());
      let W;
      if (MAP[n] && SWING.has(n)) {
        const sname = opts.mirror ? srcName(MAP[n][0]) : MAP[n][0];
        const dS = F[sname].P.clone().sub(F[sname].start);
        if (opts.mirror) dS.x *= -1;
        dS.applyMatrix4(opts.yawM).normalize();
        const cand = parentQ.clone().multiply(opts.restLocal?.[n] || rest[n].local);
        const dT = rest[MAP[n][1]].localPos.clone().applyQuaternion(cand).normalize();
        W = new THREE.Quaternion().setFromUnitVectors(dT, dS).multiply(cand);
      } else if (MAP[n]) {
        const src = F[opts.mirror ? srcName(MAP[n][0]) : MAP[n][0]];
        let M = src.M.clone();
        if (opts.mirror) M = S.clone().multiply(M).multiply(S);
        M.premultiply(opts.yawM);
        const Qs = new THREE.Quaternion().setFromRotationMatrix(M);
        W = Qs.multiply(align[n]).multiply(rest[n].world);
      } else {
        W = parentQ.clone().multiply(opts.restLocal?.[n] || rest[n].local);
      }
      world.set(n, W);
      if (process.env.CHECK && MAP[n] && MAP[n][1]) {
        const childLocal = rest[MAP[n][1]].localPos.clone();
        const dT = childLocal.applyQuaternion(W).normalize();
        const sname = opts.mirror ? srcName(MAP[n][0]) : MAP[n][0];
        const dS = F[sname].P.clone().sub(F[sname].start);
        if (opts.mirror) dS.x *= -1;
        dS.applyMatrix4(opts.yawM).normalize();
        (globalThis.ERR ||= {})[n] = Math.max((globalThis.ERR[n] || 0), dT.angleTo(dS) * 180 / Math.PI);
      }
      const local = parentQ.clone().invert().multiply(W);
      frameOut.rot[n] = local;
    }
    const rp = F.root.P.clone();
    if (opts.mirror) rp.x *= -1;
    rp.applyMatrix4(opts.yawM).multiplyScalar(scale);
    frameOut.hip = rp;
    // أدنى نقطة للقدمين (للأرضية)
    const toes = ['ltoes', 'rtoes', 'lfoot', 'rfoot'].map((k) => F[k].P.clone().applyMatrix4(opts.yawM).multiplyScalar(scale).y);
    frameOut.floor = Math.min(...toes);
    out.push(frameOut);
  }
  return { frames: out, scale };
}

// ---------- معالجة كل ملف ----------
const T = await loadTarget();
// وضعية يد مرتخية (أصابع منثنية) من حركة الوقوف في Quaternius
const RELAX = {};
{
  const find = (re) => { const out = []; const walk = (d) => { for (const f of fs.readdirSync(d, { withFileTypes: true })) { const p = path.join(d, f.name); if (f.isDirectory()) walk(p); else if (re.test(p)) out.push(p); } }; walk(ASSETS); return out[0]; };
  const adoc = await new NodeIO().registerExtensions(ALL_EXTENSIONS).read(find(/Unreal-Godot[\/\\]UAL1_Standard\.glb$/));
  const idle = adoc.getRoot().listAnimations().find((a) => a.getName() === 'Idle_Loop');
  for (const ch of idle.listChannels()) {
    const n = ch.getTargetNode().getName();
    if (ch.getTargetPath() === 'rotation' && /(index|middle|pinky|ring|thumb)_/.test(n)) RELAX[n] = new THREE.Quaternion().fromArray(ch.getSampler().getOutput().getElement(0, []));
  }
}
const clips = [];
const files = [
  ['10', '10_01'], ['10', '10_02'], ['10', '10_03'], ['10', '10_05'], ['10', '10_06'], ['11', '11_01'],
];
const results = [];
for (const [subj, id] of files) {
  const bones = parseASF(fs.readFileSync(path.join(CMU, `${subj}.asf`), 'utf8'));
  const frames = parseAMC(fs.readFileSync(path.join(CMU, `${id}.amc`), 'utf8'));
  // لحظة لمس الكرة = أعلى سرعة لمقدمة القدم
  let best = { v: 0, i: 0, foot: 'r' };
  let prev = null;
  frames.forEach((fr, i) => {
    const F = fk(bones, fr);
    if (prev) for (const ft of ['l', 'r']) { const v = F[`${ft}toes`].P.distanceTo(prev[`${ft}toes`].P) * 120; if (v > best.v) best = { v, i, foot: ft }; }
    prev = F;
  });
  // اتجاه الجسم عند اللمس
  const Fc = fk(bones, frames[best.i]);
  const fwd = new THREE.Vector3(0, 0, 1).transformDirection(Fc.root.M);
  const yaw = Math.atan2(fwd.x, fwd.z);
  results.push({ subj, id, bones, frames, contact: best.i, foot: best.foot, yaw, speed: best.v });
  console.log(id, 'contact frame', best.i, '/', frames.length, 'foot', best.foot, 'toe speed', best.v.toFixed(1), 'yaw', (yaw / D2R).toFixed(0));
}

function makeClip(r, name, pre, post) {
  const mirror = r.foot === 'l'; // نحوّل كل الركلات للقدم اليمنى
  let yaw = r.yaw;
  if (mirror) yaw = -yaw;
  const yawM = new THREE.Matrix4().makeRotationY(-yaw);
  const step = 120 / FPS_OUT;
  const i0 = Math.max(0, Math.round(r.contact - pre * 120)), i1 = Math.min(r.frames.length - 1, Math.round(r.contact + post * 120));
  const sel = [];
  for (let i = i0; i <= i1; i += step) sel.push(r.frames[Math.round(i)]);
  const { frames } = retarget(T, r.bones, sel, { mirror, yawM, restLocal: RELAX });
  // في المكان: نطرح المسار الأفقي المنعّم، ونثبت القدمين على الأرض
  const n = frames.length, W = Math.round(0.35 * FPS_OUT);
  const smooth = frames.map((_, i) => { const a = new THREE.Vector3(); let c = 0; for (let k = Math.max(0, i - W); k <= Math.min(n - 1, i + W); k++) { a.add(frames[k].hip); c++; } return a.multiplyScalar(1 / c); });
  const ci = Math.round(pre * FPS_OUT);
  const floor = Math.min(...frames.map((f) => f.floor));
  const rootQinv = T.nodes.get('root').getWorldQuaternion(new THREE.Quaternion()).invert();
  const times = frames.map((_, i) => +(i / FPS_OUT).toFixed(4));
  const tracks = {};
  for (const bn of T.order) tracks[bn] = { path: 'rotation', values: [] };
  const pel = { path: 'translation', values: [] };
  frames.forEach((f, i) => {
    for (const bn of T.order) tracks[bn].values.push(...f.rot[bn].toArray().map((v) => +v.toFixed(5)));
    const hp = f.hip.clone();
    hp.x -= smooth[i].x; hp.z -= smooth[i].z;
    hp.y -= floor;
    // إلى إحداثيات عقدة root المحلية
    hp.applyQuaternion(rootQinv);
    pel.values.push(...hp.toArray().map((v) => +v.toFixed(4)));
  });
  // نحذف مسارات الأصابع الثابتة لتصغير الحجم (تبقى وضعية الراحة)
  // الأصابع ثابتة: نكتفي بمفتاحين (أول وآخر إطار) لتصغير الحجم
  for (const bn of Object.keys(tracks)) {
    if (/leaf/.test(bn)) { delete tracks[bn]; continue; }
    if (/(index|middle|pinky|ring|thumb)_/.test(bn)) tracks[bn].constant = true;
  }
  const list = Object.entries(tracks).map(([bone, v]) => ({ bone, ...v }));
  list.push({ bone: 'pelvis', ...pel });
  const res = { name, times, contact: +(ci / FPS_OUT).toFixed(3), source: `CMU ${r.id}${mirror ? ' (mirrored)' : ''}`, tracks: list };
  return res;
}
const pick = (process.env.PICK || '10_02:kick,10_05:kick_b,11_01:kick_c').split(',');
for (const p of pick) {
  const [id, name] = p.split(':');
  const r = results.find((x) => x.id === id);
  clips.push(makeClip(r, name, 0.55, 0.7));
}
if (process.env.CHECK) console.log('max angle error (deg):', Object.entries(globalThis.ERR).map(([k, v]) => `${k}:${v.toFixed(1)}`).join(' '));
fs.mkdirSync(path.join(ROOT, 'scripts/data'), { recursive: true });
fs.writeFileSync(path.join(ROOT, 'scripts/data/cmu-clips.json'), JSON.stringify(clips));
console.log('wrote', clips.map((c) => `${c.name} (${c.source}, ${c.times.length} frames, contact ${c.contact}s)`).join(' | '));
