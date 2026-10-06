// يبني ملف اللاعبين public/models/players.glb من حزم Quaternius المجانية (CC0):
//   Universal Base Characters + Universal Animation Library 1 و 2  (حركات Motion Capture على نفس الهيكل)
// + ركلات كرة قدم حقيقية من قاعدة CMU (تُحوَّل بـ scripts/retarget-cmu.mjs)
// الاستعمال: ASSETS=/path/to/unzipped node scripts/build-players.mjs
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { NodeIO, Document } from '@gltf-transform/core';
import { ALL_EXTENSIONS } from '@gltf-transform/extensions';
import { prune, dedup, resample, textureCompress, reorder, quantize, weld } from '@gltf-transform/functions';
import { EXTMeshoptCompression } from '@gltf-transform/extensions';
import { MeshoptEncoder } from 'meshoptimizer';
import sharp from 'sharp';
import * as THREE from 'three';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const A = process.env.ASSETS;
if (!A) { console.error('ASSETS=<folder with unzipped Quaternius packs> required'); process.exit(1); }
const find = (re) => {
  const out = [];
  const walk = (d) => { for (const f of fs.readdirSync(d, { withFileTypes: true })) { const p = path.join(d, f.name); if (f.isDirectory()) walk(p); else if (re.test(p)) out.push(p); } };
  walk(A);
  return out.sort();
};
const BODY = find(/Godot - UE[\/\\]Superhero_Male_FullBody\.gltf$/)[0];
const HAIR_DIR = path.dirname(find(/Rigged to Head Bone[\/\\]glTF[^\/\\]*[\/\\]Hair_Buzzed\.gltf$/)[0]);
const UAL1 = find(/Unreal-Godot[\/\\]UAL1_Standard\.glb$/)[0];
const UAL2 = find(/Unreal-Godot[\/\\]UAL2_Standard\.glb$/)[0];
const LIGHT_SKIN = find(/Textures[\/\\]T_Superhero_Male_Ligh\.png$/)[0];
console.log({ BODY, HAIR_DIR, UAL1, UAL2, LIGHT_SKIN });

// اسم الحركة في الحزمة ← اسمها في اللعبة
const CLIPS1 = {
  Idle_Loop: 'idle', Walk_Loop: 'walk', Jog_Fwd_Loop: 'jog', Sprint_Loop: 'sprint',
  Jump_Start: 'jump_start', Jump_Loop: 'jump_loop', Jump_Land: 'jump_land',
  Roll: 'roll', Hit_Chest: 'hit', Dance_Loop: 'dance', Death01: 'fall', Crouch_Idle_Loop: 'crouch',
};
const CLIPS2 = {
  Slide_Start: 'slide_start', Slide_Loop: 'slide_loop', Slide_Exit: 'slide_exit',
  Hit_Knockback: 'knockback', LayToIdle: 'getup', OverhandThrow: 'throw',
  Idle_FoldArms_Loop: 'folded', Yes: 'yes', Idle_No_Loop: 'no',
};
const HAIRS = ['Hair_Buzzed', 'Hair_SimpleParted', 'Hair_Long', 'Hair_Beard', 'Hair_Buns'];

const io = new NodeIO().registerExtensions(ALL_EXTENSIONS).registerDependencies({ 'meshopt.encoder': MeshoptEncoder });
await MeshoptEncoder.ready;

// الملف الأصلي يشير لملفات صور غير موجودة بأسماء *_png.png
const bodyDir = path.dirname(BODY);
for (const [miss, src] of [['T_Hair_1_Normal_png.png', 'T_Hair_1_Normal.png'], ['T_Eye_Normal_png.png', 'T_Eye_Normal.png']]) {
  if (!fs.existsSync(path.join(bodyDir, miss)) && fs.existsSync(path.join(bodyDir, src))) fs.copyFileSync(path.join(bodyDir, src), path.join(bodyDir, miss));
}
const doc = await io.read(BODY);
const R = doc.getRoot();
const buffer = R.listBuffers()[0];
const nodeByName = new Map(R.listNodes().map((n) => [n.getName(), n]));

// إزالة السمات غير المستعملة (أصغر حجماً)
for (const m of R.listMeshes()) for (const p of m.listPrimitives()) {
  for (const s of ['TEXCOORD_1', 'TEXCOORD_2', 'TEXCOORD_3', 'COLOR_0', 'COLOR_1']) if (p.getAttribute(s)) p.setAttribute(s, null);
}

// جلد فاتح كقاعدة (يُلوَّن حسب الشخصية في اللعبة)
const bodyMat = R.listMaterials().find((m) => m.getName() === 'MI_Superhero_Male');
bodyMat.getBaseColorTexture().setImage(fs.readFileSync(LIGHT_SKIN)).setMimeType('image/png').setURI('skin.png');
bodyMat.setMetallicRoughnessTexture(null).setRoughnessFactor(0.62).setMetallicFactor(0);

// الشعر: مثبت على عظمة الرأس → نحوله لشبكة عادية ابنة لعقدة Head
const head = nodeByName.get('Head');
const headInv = new THREE.Matrix4().fromArray(head.getWorldMatrix()).invert();
const hairMat = R.listMaterials().find((m) => m.getName() === 'MI_Hair_1');
for (const name of HAIRS) {
  const hdoc = await io.read(path.join(HAIR_DIR, `${name}.gltf`));
  const prim = hdoc.getRoot().listMeshes()[0].listPrimitives()[0];
  const pos = prim.getAttribute('POSITION'), nor = prim.getAttribute('NORMAL'), uv = prim.getAttribute('TEXCOORD_0');
  const P = new Float32Array(pos.getCount() * 3), N = new Float32Array(pos.getCount() * 3);
  const v = new THREE.Vector3(), nm = new THREE.Matrix3().getNormalMatrix(headInv);
  for (let i = 0; i < pos.getCount(); i++) {
    v.fromArray(pos.getElement(i, [])).applyMatrix4(headInv); v.toArray(P, i * 3);
    v.fromArray(nor.getElement(i, [])).applyMatrix3(nm).normalize(); v.toArray(N, i * 3);
  }
  const p2 = doc.createPrimitive()
    .setAttribute('POSITION', doc.createAccessor().setType('VEC3').setArray(P).setBuffer(buffer))
    .setAttribute('NORMAL', doc.createAccessor().setType('VEC3').setArray(N).setBuffer(buffer))
    .setAttribute('TEXCOORD_0', doc.createAccessor().setType('VEC2').setArray(new Float32Array(uv.getArray())).setBuffer(buffer))
    .setIndices(doc.createAccessor().setType('SCALAR').setArray(new Uint32Array(prim.getIndices().getArray())).setBuffer(buffer))
    .setMaterial(hairMat);
  const node = doc.createNode(`hair_${name.replace('Hair_', '').toLowerCase()}`).setMesh(doc.createMesh(name).addPrimitive(p2));
  head.addChild(node);
}

// الحركات: نسخ قنوات الدوران لكل العظام + انتقال الحوض فقط (لا نغير أبعاد الجسم)
async function addClips(file, map) {
  const adoc = await io.read(file);
  for (const a of adoc.getRoot().listAnimations()) {
    const name = map[a.getName()];
    if (!name) continue;
    const anim = doc.createAnimation(name);
    for (const ch of a.listChannels()) {
      const tp = ch.getTargetPath();
      const tn = ch.getTargetNode().getName();
      if (tp === 'scale' || (tp === 'translation' && tn !== 'pelvis')) continue;
      const target = nodeByName.get(tn);
      if (!target) continue;
      const s = ch.getSampler();
      const smp = doc.createAnimationSampler()
        .setInput(doc.createAccessor().setType('SCALAR').setArray(new Float32Array(s.getInput().getArray())).setBuffer(buffer))
        .setOutput(doc.createAccessor().setType(tp === 'rotation' ? 'VEC4' : 'VEC3').setArray(new Float32Array(s.getOutput().getArray())).setBuffer(buffer))
        .setInterpolation(s.getInterpolation());
      anim.addSampler(smp).addChannel(doc.createAnimationChannel().setTargetNode(target).setTargetPath(tp).setSampler(smp));
    }
    console.log('clip', name);
  }
}
await addClips(UAL1, CLIPS1);
await addClips(UAL2, CLIPS2);

// ركلات CMU المحوّلة (إن وُجدت)
const extra = path.join(ROOT, 'scripts/data/cmu-clips.json');
if (fs.existsSync(extra)) {
  const clips = JSON.parse(fs.readFileSync(extra, 'utf8'));
  for (const c of clips) {
    const anim = doc.createAnimation(c.name);
    const input = doc.createAccessor().setType('SCALAR').setArray(new Float32Array(c.times)).setBuffer(buffer);
    for (const tr of c.tracks) {
      const target = nodeByName.get(tr.bone);
      if (!target) continue;
      const smp = doc.createAnimationSampler().setInput(input)
        .setOutput(doc.createAccessor().setType(tr.path === 'rotation' ? 'VEC4' : 'VEC3').setArray(new Float32Array(tr.values)).setBuffer(buffer))
        .setInterpolation('LINEAR');
      anim.addSampler(smp).addChannel(doc.createAnimationChannel().setTargetNode(target).setTargetPath(tr.path).setSampler(smp));
    }
    console.log('cmu clip', c.name);
  }
}

await doc.transform(
  resample({ tolerance: 1e-4 }),
  prune(),
  dedup(),
  weld(),
  textureCompress({ encoder: sharp, targetFormat: 'webp', resize: [1024, 1024], quality: 82, pattern: /Superhero|skin/i }),
  textureCompress({ encoder: sharp, targetFormat: 'webp', resize: [512, 512], quality: 80, pattern: /Hair/i }),
  textureCompress({ encoder: sharp, targetFormat: 'webp', resize: [128, 128], quality: 80, pattern: /Eye/i }),
  reorder({ encoder: MeshoptEncoder, target: 'size' }),
  // لا نضغط المواقع: شيدر الطقم يقرأ إحداثيات الجسم الحقيقية (بالمتر) في وضعية الربط
  quantize({ pattern: /^(NORMAL|TEXCOORD|JOINTS|WEIGHTS)(_\d+)?$/, patternTargets: /^(NORMAL|TEXCOORD)(_\d+)?$/ }),
);
doc.createExtension(EXTMeshoptCompression).setRequired(true).setEncoderOptions({ method: EXTMeshoptCompression.EncoderMethod.QUANTIZE });
const out = path.join(ROOT, 'public/models/players.glb');
fs.mkdirSync(path.dirname(out), { recursive: true });
await io.write(out, doc);
console.log(out, (fs.statSync(out).size / 1024).toFixed(0) + ' KB');
