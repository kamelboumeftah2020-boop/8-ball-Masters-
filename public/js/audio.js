// محرك صوت اصطناعي بالكامل (Web Audio) — لا ملفات صوتية = لعبة خفيفة
// الجمهور، الأهازيج، الصافرة، الركلات، القائم، الشباك، القدرات، والمعلق

const clamp = (v, a, b) => (v < a ? a : v > b ? b : v);

export class AudioEngine {
  constructor() {
    this.ctx = null;
    this.enabled = true;
    this.volume = 0.8;
    this.commentary = true;
    this.excite = 0;
    this.chantTimer = 6;
    this.drums = false;
    this.beatIdx = 0;
    this.nextBeat = 0;
    this.chantBusy = 0;
    this.stadiumMood = 1;
  }

  init() {
    if (this.ctx) { if (this.ctx.state === 'suspended') this.ctx.resume(); return; }
    const AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return;
    const ctx = (this.ctx = new AC());
    this.master = ctx.createGain();
    this.master.gain.value = this.enabled ? this.volume : 0;
    const comp = ctx.createDynamicsCompressor();
    comp.threshold.value = -14; comp.ratio.value = 4;
    this.master.connect(comp).connect(ctx.destination);
    this.sfx = ctx.createGain(); this.sfx.gain.value = 1.15; this.sfx.connect(this.master);
    this.crowdBus = ctx.createGain(); this.crowdBus.gain.value = 1.0; this.crowdBus.connect(this.master);
    // مخزن ضوضاء
    const len = ctx.sampleRate * 3;
    const buf = ctx.createBuffer(2, len, ctx.sampleRate);
    for (let c = 0; c < 2; c++) {
      const d = buf.getChannelData(c);
      let b0 = 0, b1 = 0, b2 = 0;
      for (let i = 0; i < len; i++) {
        const w = Math.random() * 2 - 1;
        b0 = 0.99765 * b0 + w * 0.099; b1 = 0.963 * b1 + w * 0.2965; b2 = 0.57 * b2 + w * 1.0526;
        d[i] = (b0 + b1 + b2 + w * 0.1848) * 0.18; // ضوضاء وردية
      }
    }
    this.noiseBuf = buf;
    const wb = ctx.createBuffer(1, ctx.sampleRate, ctx.sampleRate);
    const wd = wb.getChannelData(0);
    for (let i = 0; i < wd.length; i++) wd[i] = Math.random() * 2 - 1;
    this.whiteBuf = wb;
    this.startAmbience();
  }

  setEnabled(on) {
    this.enabled = on;
    if (this.master) this.master.gain.setTargetAtTime(on ? this.volume : 0, this.ctx.currentTime, 0.05);
  }
  setVolume(v) {
    this.volume = v;
    if (this.master && this.enabled) this.master.gain.setTargetAtTime(v, this.ctx.currentTime, 0.05);
  }

  noise(white = false) {
    const s = this.ctx.createBufferSource();
    s.buffer = white ? this.whiteBuf : this.noiseBuf;
    s.loop = true;
    s.loopStart = Math.random();
    return s;
  }

  // ضجيج الجمهور المستمر
  startAmbience() {
    const ctx = this.ctx;
    const src = this.noise();
    const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.frequency.value = 700; bp.Q.value = 0.6;
    const lp = ctx.createBiquadFilter(); lp.type = 'lowpass'; lp.frequency.value = 2200;
    const g = ctx.createGain(); g.gain.value = 0.35;
    src.connect(bp).connect(lp).connect(g).connect(this.crowdBus);
    src.start();
    // همهمة (أصوات متداخلة) بتذبذب بطيء
    const lfo = ctx.createOscillator(); lfo.frequency.value = 0.13;
    const lfoG = ctx.createGain(); lfoG.gain.value = 0.08;
    lfo.connect(lfoG).connect(g.gain); lfo.start();
    this.amb = { g, bp, lp };
  }

  update(dt, excite) {
    if (!this.ctx) return;
    this.excite += (excite - this.excite) * Math.min(1, dt * 1.5);
    const e = clamp(this.excite, 0, 1);
    const t = this.ctx.currentTime;
    this.amb.g.gain.setTargetAtTime((0.42 + e * 0.65) * this.stadiumMood, t, 0.2);
    this.amb.bp.frequency.setTargetAtTime(600 + e * 700, t, 0.3);
    this.amb.lp.frequency.setTargetAtTime(1800 + e * 2500, t, 0.3);
    this.chantBusy -= dt;
    this.chantTimer -= dt;
    if (this.drums && this.chantTimer <= 0 && this.chantBusy <= 0) {
      this.chantTimer = 12 + Math.random() * 10;
      this.chant();
    }
    // طبول المدرجات المستمرة (تتسارع مع الحماس)
    if (this.drums && this.enabled) {
      if (this.nextBeat < t) this.nextBeat = t + 0.05;
      const pat = [1, 0, 0, 1, 1, 0, 1, 0];
      while (this.nextBeat < t + 0.25) {
        const i = this.beatIdx++ % 8;
        if (this.chantBusy <= 0) {
          if (pat[i]) this.drum(this.nextBeat, 0.12 + e * 0.3, i === 0 ? 60 : 75);
          if (e > 0.45 && i % 2 === 1) this.clap(this.nextBeat, 0.08 + e * 0.18);
        }
        this.nextBeat += 0.31 - e * 0.07;
      }
    }
  }

  env(g, t, a, peak, d, end = 0.0001) {
    g.gain.cancelScheduledValues(t);
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(peak, t + a);
    g.gain.exponentialRampToValueAtTime(end, t + a + d);
  }

  // ---------- مؤثرات ----------
  kick(power = 0.6, header = false) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const o = ctx.createOscillator(); o.type = 'sine';
    o.frequency.setValueAtTime(header ? 220 : 150, t);
    o.frequency.exponentialRampToValueAtTime(header ? 90 : 45, t + 0.12);
    const g = ctx.createGain();
    this.env(g, t, 0.003, 0.5 + power * 0.6, 0.16);
    o.connect(g).connect(this.sfx); o.start(t); o.stop(t + 0.2);
    const n = this.noise(true);
    const hp = ctx.createBiquadFilter(); hp.type = 'bandpass'; hp.frequency.value = header ? 1200 : 1800; hp.Q.value = 1.2;
    const ng = ctx.createGain();
    this.env(ng, t, 0.001, 0.25 + power * 0.35, 0.05);
    n.connect(hp).connect(ng).connect(this.sfx); n.start(t); n.stop(t + 0.08);
  }

  touch() {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const o = ctx.createOscillator(); o.frequency.setValueAtTime(120, t); o.frequency.exponentialRampToValueAtTime(60, t + 0.06);
    const g = ctx.createGain(); this.env(g, t, 0.002, 0.18, 0.07);
    o.connect(g).connect(this.sfx); o.start(t); o.stop(t + 0.1);
  }

  bounce(v) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const o = ctx.createOscillator(); o.frequency.setValueAtTime(95, t); o.frequency.exponentialRampToValueAtTime(50, t + 0.08);
    const g = ctx.createGain(); this.env(g, t, 0.002, clamp(v / 14, 0.05, 0.4), 0.1);
    o.connect(g).connect(this.sfx); o.start(t); o.stop(t + 0.12);
  }

  whistle(kind = 'short') {
    if (!this.ctx) return;
    const ctx = this.ctx;
    const pattern = kind === 'end' ? [[0, 0.35], [0.5, 0.35], [1.0, 1.1]] : kind === 'long' ? [[0, 0.8]] : [[0, 0.35]];
    for (const [off, dur] of pattern) {
      const t = ctx.currentTime + off;
      const g = ctx.createGain();
      g.gain.setValueAtTime(0.0001, t);
      g.gain.exponentialRampToValueAtTime(0.22, t + 0.02);
      g.gain.setValueAtTime(0.22, t + dur - 0.05);
      g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
      const trem = ctx.createOscillator(); trem.frequency.value = 32;
      const tg = ctx.createGain(); tg.gain.value = 0.09;
      trem.connect(tg).connect(g.gain);
      for (const f of [2750, 2990]) {
        const o = ctx.createOscillator(); o.type = 'sine'; o.frequency.value = f;
        o.connect(g); o.start(t); o.stop(t + dur);
      }
      trem.start(t); trem.stop(t + dur);
      g.connect(this.sfx);
    }
  }

  post() {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    for (const [f, a, d] of [[523, 0.35, 1.2], [1318, 0.2, 0.8], [2149, 0.12, 0.5], [3310, 0.08, 0.3]]) {
      const o = ctx.createOscillator(); o.frequency.value = f;
      const g = ctx.createGain(); this.env(g, t, 0.002, a, d);
      o.connect(g).connect(this.sfx); o.start(t); o.stop(t + d + 0.05);
    }
    setTimeout(() => this.ooh(), 150);
  }

  net() {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const n = this.noise(true);
    const f = ctx.createBiquadFilter(); f.type = 'highpass'; f.frequency.value = 2500;
    const g = ctx.createGain(); this.env(g, t, 0.01, 0.3, 0.35);
    n.connect(f).connect(g).connect(this.sfx); n.start(t); n.stop(t + 0.4);
  }

  // هتاف الجمهور القوي (هدف)
  roar(dur = 5, amount = 1) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const n = this.noise();
    const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.Q.value = 0.5;
    bp.frequency.setValueAtTime(500, t); bp.frequency.linearRampToValueAtTime(1300, t + 0.5); bp.frequency.linearRampToValueAtTime(800, t + dur);
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(1.3 * amount, t + 0.3);
    g.gain.exponentialRampToValueAtTime(0.5 * amount, t + dur * 0.6);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    n.connect(bp).connect(g).connect(this.crowdBus); n.start(t); n.stop(t + dur + 0.1);
    this.voices('aa', t, dur * 0.8, 0.12 * amount, [220, 262, 294, 330], true);
  }

  // "أووووه" عند الفرصة الضائعة
  ooh() {
    if (!this.ctx) return;
    const t = this.ctx.currentTime;
    this.voices('oo', t, 1.4, 0.18, [260, 300, 340, 220], false, -0.25);
    const n = this.noise();
    const bp = this.ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.frequency.value = 450; bp.Q.value = 2;
    const g = this.ctx.createGain(); this.env(g, t, 0.15, 0.5, 1.3);
    n.connect(bp).connect(g).connect(this.crowdBus); n.start(t); n.stop(t + 1.6);
  }

  // أصوات جوقة بصيغ الحروف المتحركة (formants)
  voices(vowel, t, dur, amp, freqs, rise = false, glide = 0) {
    const ctx = this.ctx;
    const F = { aa: [800, 1200, 2500], oo: [400, 800, 2600], ee: [300, 2300, 3000], eh: [550, 1800, 2600], ii: [280, 2250, 2900], uu: [330, 900, 2300] }[vowel] || [700, 1200, 2600];
    const out = ctx.createGain(); out.gain.value = 0;
    out.gain.setValueAtTime(0.0001, t);
    out.gain.exponentialRampToValueAtTime(amp, t + Math.min(0.12, dur * 0.3));
    out.gain.setValueAtTime(amp, t + dur * 0.7);
    out.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    const mix = ctx.createGain(); mix.gain.value = 1;
    F.forEach((f, i) => {
      const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.frequency.value = f; bp.Q.value = 6 + i * 2;
      const g = ctx.createGain(); g.gain.value = [1, 0.5, 0.25][i];
      mix.connect(bp).connect(g).connect(out);
    });
    out.connect(this.crowdBus);
    for (const base of freqs) {
      for (let k = 0; k < 3; k++) {
        const o = ctx.createOscillator(); o.type = 'sawtooth';
        const f0 = base * (1 + (Math.random() - 0.5) * 0.04);
        o.frequency.setValueAtTime(f0 * (rise ? 0.9 : 1), t);
        if (rise) o.frequency.linearRampToValueAtTime(f0, t + 0.3);
        if (glide) o.frequency.linearRampToValueAtTime(f0 * (1 + glide), t + dur);
        const vib = ctx.createOscillator(); vib.frequency.value = 4 + Math.random() * 2;
        const vg = ctx.createGain(); vg.gain.value = f0 * 0.012;
        vib.connect(vg).connect(o.frequency);
        const og = ctx.createGain(); og.gain.value = 0.15;
        o.connect(og).connect(mix);
        o.start(t); o.stop(t + dur + 0.05); vib.start(t); vib.stop(t + dur + 0.05);
      }
    }
  }

  drum(t, amp = 0.6, f = 90) {
    const ctx = this.ctx;
    const o = ctx.createOscillator(); o.frequency.setValueAtTime(f * 1.6, t); o.frequency.exponentialRampToValueAtTime(f * 0.5, t + 0.25);
    const g = ctx.createGain(); this.env(g, t, 0.003, amp, 0.3);
    o.connect(g).connect(this.crowdBus); o.start(t); o.stop(t + 0.35);
  }

  clap(t, amp = 0.4) {
    const ctx = this.ctx;
    const n = this.noise(true);
    const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.frequency.value = 1400; bp.Q.value = 0.8;
    const g = ctx.createGain(); this.env(g, t, 0.002, amp, 0.12);
    n.connect(bp).connect(g).connect(this.crowdBus); n.start(t); n.stop(t + 0.16);
  }

  horn(t = null, dur = 1.2) {
    if (!this.ctx) return;
    const ctx = this.ctx;
    t = t ?? ctx.currentTime;
    const o = ctx.createOscillator(); o.type = 'sawtooth'; o.frequency.value = 233;
    const o2 = ctx.createOscillator(); o2.type = 'sawtooth'; o2.frequency.value = 235.5;
    const lp = ctx.createBiquadFilter(); lp.type = 'lowpass'; lp.frequency.value = 1400;
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, t); g.gain.exponentialRampToValueAtTime(0.13, t + 0.05);
    g.gain.setValueAtTime(0.13, t + dur - 0.1); g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    o.connect(lp); o2.connect(lp); lp.connect(g).connect(this.crowdBus);
    o.start(t); o2.start(t); o.stop(t + dur); o2.stop(t + dur);
  }

  // تصفيق جماعي
  applause(dur = 2.5, amt = 1) {
    if (!this.ctx) return;
    const t0 = this.ctx.currentTime;
    const n = Math.floor(dur * 26 * amt);
    for (let i = 0; i < n; i++) {
      const tt = t0 + Math.random() * dur;
      const fade = 1 - (tt - t0) / dur;
      this.clap(tt, (0.05 + Math.random() * 0.12) * fade * amt);
    }
  }

  // صوت الكرة وهي تطير
  whoosh(power = 0.7) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const n = this.noise(true);
    const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.Q.value = 2;
    bp.frequency.setValueAtTime(2400 + power * 1500, t); bp.frequency.exponentialRampToValueAtTime(500, t + 0.45);
    const g = ctx.createGain(); this.env(g, t, 0.02, 0.18 + power * 0.25, 0.45);
    n.connect(bp).connect(g).connect(this.sfx); n.start(t); n.stop(t + 0.5);
  }

  // اندفاع الجمهور عند الهجمة الخطيرة
  cheer(amt = 0.6) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const n = this.noise();
    const bp = ctx.createBiquadFilter(); bp.type = 'bandpass'; bp.Q.value = 0.7;
    bp.frequency.setValueAtTime(500, t); bp.frequency.linearRampToValueAtTime(1100, t + 0.8);
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, t); g.gain.exponentialRampToValueAtTime(0.7 * amt, t + 0.5); g.gain.exponentialRampToValueAtTime(0.0001, t + 1.8);
    n.connect(bp).connect(g).connect(this.crowdBus); n.start(t); n.stop(t + 1.9);
  }

  click() {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const o = ctx.createOscillator(); o.type = 'triangle'; o.frequency.setValueAtTime(900, t); o.frequency.exponentialRampToValueAtTime(500, t + 0.06);
    const g = ctx.createGain(); this.env(g, t, 0.002, 0.15, 0.07);
    o.connect(g).connect(this.master); o.start(t); o.stop(t + 0.1);
  }

  // احتفال الهدف: أبواق وطبول سريعة
  goalParty() {
    if (!this.ctx) return;
    const t0 = this.ctx.currentTime;
    for (let i = 0; i < 3; i++) this.horn(t0 + 0.2 + i * 0.5 + Math.random() * 0.2, 1.0 + Math.random() * 0.6);
    for (let b = 0; b < 24; b++) this.drum(t0 + 0.5 + b * 0.18, 0.5, b % 4 === 0 ? 55 : 80);
    this.applause(5, 1.3);
  }

  // ---------- الأهازيج ----------
  chant(kind) {
    if (!this.ctx) return;
    const t0 = this.ctx.currentTime + 0.05;
    kind = kind ?? Math.floor(Math.random() * 4);
    if (kind === 0) {
      // "أوليه، أوليه أوليه أوليه"
      const beat = 0.36;
      const mel = [
        [0, 1, 330, 'oo'], [1, 1.6, 392, 'eh'],
        [3, 1, 330, 'oo'], [4, 1, 392, 'eh'], [5, 1, 330, 'oo'], [6, 1, 392, 'eh'], [7, 1, 330, 'oo'], [8, 2.2, 294, 'eh'],
        [11, 1, 294, 'oo'], [12, 1.6, 349, 'eh'],
        [14, 1, 294, 'oo'], [15, 1, 349, 'eh'], [16, 1, 294, 'oo'], [17, 1, 349, 'eh'], [18, 1, 294, 'oo'], [19, 2.4, 330, 'eh'],
      ];
      for (const [b, l, f, v] of mel) this.voices(v, t0 + b * beat, l * beat, 0.09, [f, f * 0.5, f * 0.75], false);
      for (let b = 0; b < 22; b += 2) this.drum(t0 + b * beat, 0.35);
      this.chantBusy = 22 * beat;
    } else if (kind === 1) {
      // دق دق تصفيق
      const beat = 0.42;
      for (let bar = 0; bar < 6; bar++) {
        const tb = t0 + bar * beat * 4;
        this.drum(tb, 0.7, 70); this.drum(tb + beat, 0.7, 70); this.clap(tb + beat * 2, 0.6);
        this.voices('eh', tb + beat * 2, 0.25, 0.06, [240, 300]);
      }
      this.chantBusy = 24 * beat;
    } else if (kind === 2) {
      // تصفيق الفايكنغ "هُوه!" بإيقاع متسارع
      let t = t0, gap = 2.0;
      for (let i = 0; i < 12; i++) {
        this.drum(t, 0.8, 60); this.clap(t, 0.5);
        this.voices('uu', t, 0.35, 0.12, [180, 200, 150]);
        t += gap; gap = Math.max(0.35, gap * 0.8);
      }
      this.chantBusy = t - t0;
    } else {
      // طبول وأبواق و"هيه!"
      const beat = 0.3;
      for (let b = 0; b < 32; b++) {
        if (b % 4 === 0 || b % 4 === 3) this.drum(t0 + b * beat, 0.5, 85);
        if (b % 2 === 1) this.clap(t0 + b * beat, 0.3);
        if (b % 8 === 7) this.voices('eh', t0 + b * beat, 0.3, 0.1, [260, 330, 200], true);
      }
      this.horn(t0 + 8 * beat * 1.0, 0.8); this.horn(t0 + 24 * beat, 0.8);
      this.chantBusy = 32 * beat;
    }
  }

  siu() {
    if (!this.ctx) return;
    const t = this.ctx.currentTime;
    this.voices('ii', t, 0.25, 0.14, [300, 360, 250]);
    this.voices('uu', t + 0.2, 1.3, 0.16, [300, 360, 250], false, -0.08);
  }

  // ---------- أصوات القدرات ----------
  ability(kind) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const sweep = (type, f0, f1, dur, amp, filt) => {
      const o = ctx.createOscillator(); o.type = type;
      o.frequency.setValueAtTime(f0, t); o.frequency.exponentialRampToValueAtTime(f1, t + dur);
      const g = ctx.createGain(); this.env(g, t, 0.01, amp, dur);
      let node = o;
      if (filt) { const f = ctx.createBiquadFilter(); f.type = 'lowpass'; f.frequency.value = filt; o.connect(f); node = f; }
      node.connect(g).connect(this.sfx); o.start(t); o.stop(t + dur + 0.05);
    };
    const nz = (type, f0, f1, dur, amp) => {
      const n = this.noise(true);
      const f = ctx.createBiquadFilter(); f.type = type; f.frequency.setValueAtTime(f0, t); f.frequency.exponentialRampToValueAtTime(f1, t + dur); f.Q.value = 1.5;
      const g = ctx.createGain(); this.env(g, t, 0.02, amp, dur);
      n.connect(f).connect(g).connect(this.sfx); n.start(t); n.stop(t + dur + 0.05);
    };
    switch (kind) {
      case 'fire': nz('lowpass', 300, 3000, 0.6, 0.5); sweep('sawtooth', 80, 200, 0.5, 0.15, 800); break;
      case 'dash': sweep('square', 300, 1400, 0.3, 0.12, 3000); nz('bandpass', 800, 5000, 0.35, 0.3); break;
      case 'quake': sweep('sine', 90, 25, 0.9, 0.9); nz('lowpass', 400, 60, 0.9, 0.7); break;
      case 'ice': for (let i = 0; i < 6; i++) { const o = ctx.createOscillator(); o.frequency.value = 1800 + Math.random() * 2500; const g = ctx.createGain(); const tt = t + i * 0.06; g.gain.setValueAtTime(0.0001, tt); g.gain.exponentialRampToValueAtTime(0.08, tt + 0.01); g.gain.exponentialRampToValueAtTime(0.0001, tt + 0.5); o.connect(g).connect(this.sfx); o.start(tt); o.stop(tt + 0.55); } nz('highpass', 3000, 8000, 0.8, 0.2); break;
      case 'blink': sweep('sine', 200, 2400, 0.25, 0.25); sweep('sine', 2400, 300, 0.25, 0.1); break;
      case 'pull': { const o = ctx.createOscillator(); o.type = 'triangle'; o.frequency.value = 110; const l = ctx.createOscillator(); l.frequency.value = 9; const lg = ctx.createGain(); lg.gain.value = 60; l.connect(lg).connect(o.frequency); const g = ctx.createGain(); this.env(g, t, 0.05, 0.3, 1.6); o.connect(g).connect(this.sfx); o.start(t); l.start(t); o.stop(t + 1.7); l.stop(t + 1.7); break; }
      case 'curve': sweep('triangle', 500, 1500, 0.4, 0.15); break;
      case 'bolt': nz('highpass', 6000, 300, 0.5, 0.8); sweep('square', 2000, 60, 0.4, 0.15, 5000); setTimeout(() => this.thunder(0.5), 120); break;
      case 'wall': sweep('sine', 150, 600, 0.5, 0.3); sweep('triangle', 300, 1200, 0.5, 0.1); break;
      case 'wallhit': sweep('sine', 900, 300, 0.25, 0.25); break;
      case 'tackle': nz('lowpass', 900, 200, 0.2, 0.4); break;
      case 'slide': nz('bandpass', 1500, 400, 0.45, 0.25); break;
    }
  }

  thunder(amp = 1) {
    if (!this.ctx) return;
    const ctx = this.ctx, t = ctx.currentTime;
    const n = this.noise();
    const lp = ctx.createBiquadFilter(); lp.type = 'lowpass'; lp.frequency.setValueAtTime(1200, t); lp.frequency.exponentialRampToValueAtTime(90, t + 2.5);
    const g = ctx.createGain(); this.env(g, t, 0.02, 1.2 * amp, 2.6);
    n.connect(lp).connect(g).connect(this.master); n.start(t); n.stop(t + 2.8);
  }

  // ---------- المعلق ----------
  say(text, excited = false) {
    if (this.onSay) this.onSay(text, excited);
    if (!this.commentary || !this.enabled || !window.speechSynthesis) return;
    try {
      const u = new SpeechSynthesisUtterance(text);
      u.lang = 'ar-SA';
      const v = speechSynthesis.getVoices().find((x) => x.lang && x.lang.startsWith('ar'));
      if (v) u.voice = v;
      u.rate = excited ? 1.15 : 1.05;
      u.pitch = excited ? 1.3 : 1;
      u.volume = clamp(this.volume * 1.1, 0, 1);
      if (excited) speechSynthesis.cancel();
      else if (speechSynthesis.speaking) return;
      speechSynthesis.speak(u);
    } catch { /* ignore */ }
  }
}

export const audio = new AudioEngine();
