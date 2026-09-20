// Every sound in the game is synthesised at runtime with the Web Audio API - there
// are no audio files to download, which keeps the whole client tiny and means the
// clicks can be pitched and shaped from the actual impact speed instead of replaying
// the same canned sample every time.

let ctx = null;
let master = null;
let enabled = true;
let noiseBuffer = null;
let lastBallHit = 0;

function ensure() {
  if (ctx) return ctx;
  const AC = window.AudioContext || window.webkitAudioContext;
  if (!AC) return null;
  try {
    ctx = new AC();
    master = ctx.createGain();
    master.gain.value = 0.85;
    master.connect(ctx.destination);
  } catch { return null; }
  return ctx;
}

// Browsers keep audio suspended until the user interacts with the page.
export function unlock() {
  const c = ensure();
  if (c && c.state === 'suspended') c.resume().catch(() => {});
}

export function setEnabled(on) { enabled = !!on; }
export function isEnabled() { return enabled; }

function live() {
  if (!enabled) return null;
  const c = ensure();
  if (!c || c.state === 'suspended') return null;
  return c;
}

function getNoise(c) {
  if (!noiseBuffer || noiseBuffer.sampleRate !== c.sampleRate) {
    noiseBuffer = c.createBuffer(1, Math.floor(c.sampleRate * 0.4), c.sampleRate);
    const data = noiseBuffer.getChannelData(0);
    for (let i = 0; i < data.length; i++) data[i] = Math.random() * 2 - 1;
  }
  const src = c.createBufferSource();
  src.buffer = noiseBuffer;
  src.loop = true;
  return src;
}

// One shaped noise burst: the backbone of every impact sound.
function burst(c, { freq, q = 1, type = 'bandpass', gain = 0.3, attack = 0.001, decay = 0.08, delay = 0 }) {
  const t = c.currentTime + delay;
  const src = getNoise(c);
  const filter = c.createBiquadFilter();
  filter.type = type;
  filter.frequency.setValueAtTime(freq, t);
  filter.Q.value = q;
  const env = c.createGain();
  env.gain.setValueAtTime(0.0001, t);
  env.gain.exponentialRampToValueAtTime(Math.max(0.0002, gain), t + attack);
  env.gain.exponentialRampToValueAtTime(0.0001, t + attack + decay);
  src.connect(filter).connect(env).connect(master);
  src.start(t);
  src.stop(t + attack + decay + 0.02);
}

function tone(c, { freq, endFreq, type = 'sine', gain = 0.2, decay = 0.12, delay = 0 }) {
  const t = c.currentTime + delay;
  const osc = c.createOscillator();
  osc.type = type;
  osc.frequency.setValueAtTime(freq, t);
  if (endFreq) osc.frequency.exponentialRampToValueAtTime(Math.max(20, endFreq), t + decay);
  const env = c.createGain();
  env.gain.setValueAtTime(0.0001, t);
  env.gain.exponentialRampToValueAtTime(Math.max(0.0002, gain), t + 0.006);
  env.gain.exponentialRampToValueAtTime(0.0001, t + decay);
  osc.connect(env).connect(master);
  osc.start(t);
  osc.stop(t + decay + 0.02);
}

/* ------------------------------------------------------------- gameplay */

// Two phenolic balls meeting: a bright, very short click that rises in pitch
// and volume with the impact speed.
export function ballHit(intensity) {
  const c = live();
  if (!c) return;
  const now = performance.now();
  if (now - lastBallHit < 28) return;      // avoid machine-gunning on a pile-up
  lastBallHit = now;
  const i = Math.max(0.06, Math.min(1, intensity));
  burst(c, { freq: 1700 + i * 2400, q: 1.6, gain: 0.05 + i * 0.3, decay: 0.03 + i * 0.05 });
  tone(c, { freq: 900 + i * 900, endFreq: 500 + i * 400, type: 'triangle', gain: 0.03 + i * 0.12, decay: 0.05 + i * 0.05 });
}

// Cushion rubber: duller and lower than a ball-on-ball click.
export function cushionHit(intensity) {
  const c = live();
  if (!c) return;
  const i = Math.max(0.05, Math.min(1, intensity));
  burst(c, { freq: 320 + i * 380, type: 'lowpass', q: 0.8, gain: 0.05 + i * 0.22, decay: 0.07 + i * 0.06 });
}

// A ball dropping in: the fall, then the rattle in the pocket.
export function pocket() {
  const c = live();
  if (!c) return;
  tone(c, { freq: 260, endFreq: 70, type: 'sine', gain: 0.32, decay: 0.22 });
  burst(c, { freq: 420, type: 'lowpass', q: 0.7, gain: 0.2, decay: 0.16, delay: 0.05 });
  burst(c, { freq: 900, type: 'bandpass', q: 1.2, gain: 0.1, decay: 0.07, delay: 0.12 });
}

export function cueStrike(power) {
  const c = live();
  if (!c) return;
  const p = Math.max(0.1, Math.min(1, power));
  burst(c, { freq: 2200 + p * 1600, q: 2, gain: 0.08 + p * 0.28, decay: 0.02 + p * 0.03 });
  tone(c, { freq: 620 + p * 400, endFreq: 300, type: 'triangle', gain: 0.05 + p * 0.1, decay: 0.05 });
}

export function warnBeep() {
  const c = live();
  if (!c) return;
  tone(c, { freq: 940, type: 'square', gain: 0.1, decay: 0.09 });
}

/* ------------------------------------------------------------------- ui */

export function uiTap() {
  const c = live();
  if (!c) return;
  burst(c, { freq: 2400, q: 2.5, gain: 0.05, decay: 0.02 });
}

export function coin() {
  const c = live();
  if (!c) return;
  tone(c, { freq: 1568, type: 'triangle', gain: 0.16, decay: 0.09 });
  tone(c, { freq: 2093, type: 'triangle', gain: 0.13, decay: 0.12, delay: 0.07 });
}

export function win() {
  const c = live();
  if (!c) return;
  [523, 659, 784, 1046].forEach((f, i) => {
    tone(c, { freq: f, type: 'triangle', gain: 0.2, decay: 0.26, delay: i * 0.1 });
  });
}

export function lose() {
  const c = live();
  if (!c) return;
  [392, 330, 262].forEach((f, i) => {
    tone(c, { freq: f, type: 'sine', gain: 0.18, decay: 0.34, delay: i * 0.15 });
  });
}

/* ---------------------------------------------------------------- music */
// A slow, quiet chord bed for the menus - also synthesised, so it costs nothing
// to ship. It never plays during a match, where the impact sounds carry the feel.
const CHORDS = [
  [196.00, 246.94, 293.66],   // Gm-ish
  [174.61, 220.00, 261.63],
  [155.56, 196.00, 233.08],
  [174.61, 233.08, 277.18],
];
let musicTimer = null;
let musicGain = null;
let chordIndex = 0;

export function startMusic() {
  const c = live();
  if (!c || musicTimer) return;
  musicGain = c.createGain();
  musicGain.gain.value = 0.055;          // deliberately just under the UI sounds
  musicGain.connect(master);

  const playChord = () => {
    const ctxNow = live();
    if (!ctxNow || !musicGain) return;
    const chord = CHORDS[chordIndex++ % CHORDS.length];
    const t = ctxNow.currentTime;
    chord.forEach((freq, i) => {
      const osc = ctxNow.createOscillator();
      osc.type = 'triangle';
      osc.frequency.value = freq;
      const env = ctxNow.createGain();
      env.gain.setValueAtTime(0.0001, t);
      env.gain.exponentialRampToValueAtTime(0.5 - i * 0.12, t + 1.2);
      env.gain.exponentialRampToValueAtTime(0.0001, t + 3.8);
      osc.connect(env).connect(musicGain);
      osc.start(t);
      osc.stop(t + 4);
    });
  };
  playChord();
  musicTimer = setInterval(playChord, 3800);
}

export function stopMusic() {
  clearInterval(musicTimer);
  musicTimer = null;
  if (musicGain) {
    try { musicGain.disconnect(); } catch {}
    musicGain = null;
  }
}

export function levelUp() {
  const c = live();
  if (!c) return;
  [659, 880, 1319].forEach((f, i) => {
    tone(c, { freq: f, type: 'triangle', gain: 0.18, decay: 0.3, delay: i * 0.09 });
  });
}
