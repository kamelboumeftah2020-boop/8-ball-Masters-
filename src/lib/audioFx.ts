/**
 * Voice boost + silence trimming on a media element via Web Audio.
 *
 * Web Audio can only read samples from same-origin or CORS-enabled media, and once an
 * element is wired into an AudioContext it stays wired. The player therefore keeps a
 * dedicated element for this and only uses it for sources it can read (downloaded
 * episodes, and hosts that send CORS headers such as the Quran CDN).
 */

export interface FxSettings {
  boost: boolean;
  trimSilence: boolean;
}

const SILENCE_RMS = 0.012; // ~ -38 dBFS
const SILENCE_HOLD_MS = 350; // ignore short pauses between words
const SPEEDUP = 3; // silent stretches play this many times faster

export class AudioFx {
  private ctx?: AudioContext;
  private source?: MediaElementAudioSourceNode;
  private comp?: DynamicsCompressorNode;
  private gain?: GainNode;
  private analyser?: AnalyserNode;
  private timer?: number;
  private silentSince = 0;
  private skipping = false;
  /** Seconds of listening time saved by trimming. */
  saved = 0;
  onSaved?: (sec: number) => void;
  baseRate = 1;

  constructor(private el: HTMLMediaElement) {}

  private ensureGraph() {
    if (this.ctx) return;
    const Ctx = window.AudioContext ?? (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    this.ctx = new Ctx();
    this.source = this.ctx.createMediaElementSource(this.el);
    this.comp = this.ctx.createDynamicsCompressor();
    this.comp.threshold.value = -32;
    this.comp.knee.value = 18;
    this.comp.ratio.value = 6;
    this.comp.attack.value = 0.003;
    this.comp.release.value = 0.25;
    this.gain = this.ctx.createGain();
    this.analyser = this.ctx.createAnalyser();
    this.analyser.fftSize = 1024;
  }

  apply(settings: FxSettings) {
    this.ensureGraph();
    const { source, comp, gain, analyser, ctx } = this;
    if (!ctx || !source || !comp || !gain || !analyser) return;
    source.disconnect();
    comp.disconnect();
    gain.disconnect();
    // source -> analyser (silence detection, pre-boost) and source -> [compressor -> gain] -> speakers
    source.connect(analyser);
    if (settings.boost) {
      gain.gain.value = 1.7;
      source.connect(comp);
      comp.connect(gain);
      gain.connect(ctx.destination);
    } else {
      source.connect(ctx.destination);
    }
    this.watchSilence(settings.trimSilence);
  }

  resume() {
    if (this.ctx?.state === "suspended") this.ctx.resume().catch(() => {});
  }

  private watchSilence(on: boolean) {
    clearInterval(this.timer);
    this.endSkip();
    if (!on || !this.analyser) return;
    const buf = new Float32Array(this.analyser.fftSize);
    let last = performance.now();
    this.timer = window.setInterval(() => {
      const now = performance.now();
      const dt = now - last;
      last = now;
      if (this.el.paused) return;
      this.analyser!.getFloatTimeDomainData(buf);
      let sum = 0;
      for (let i = 0; i < buf.length; i++) sum += buf[i] * buf[i];
      const rms = Math.sqrt(sum / buf.length);
      if (rms < SILENCE_RMS) {
        if (!this.silentSince) this.silentSince = now;
        if (!this.skipping && now - this.silentSince > SILENCE_HOLD_MS) {
          this.skipping = true;
          this.el.playbackRate = Math.min(this.baseRate * SPEEDUP, 8);
        }
        if (this.skipping) {
          // Wall time spent at the boosted rate covered (rate - base) extra seconds.
          this.saved += (dt / 1000) * (this.el.playbackRate - this.baseRate) / this.baseRate;
          this.onSaved?.(this.saved);
        }
      } else {
        this.silentSince = 0;
        this.endSkip();
      }
    }, 50);
  }

  private endSkip() {
    if (this.skipping) {
      this.skipping = false;
      this.el.playbackRate = this.baseRate;
    }
  }

  setBaseRate(r: number) {
    this.baseRate = r;
    if (!this.skipping) this.el.playbackRate = r;
  }
}
