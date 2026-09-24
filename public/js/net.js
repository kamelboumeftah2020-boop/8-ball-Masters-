// طبقة النقل: محلي (ضد البوتات في المتصفح) أو شبكة (WebSocket)
import { Game } from '/shared/sim.js';
import { DT, PHASE } from '/shared/constants.js';

export class LocalTransport {
  constructor(opts) {
    this.opts = opts;
    this.game = new Game(opts);
    this.you = opts.you;
    this.acc = 0;
    this.paused = false;
    this.onSnapshot = null;
    this.onEnd = null;
    this.endSent = false;
  }
  roster() { return this.game.roster(); }
  update(dt) {
    if (this.paused) return;
    // تصوير بطيء لحظة الهدف
    const slow = this.game.phase === PHASE.GOAL && this.game.phaseT < 1.1 ? 0.3 : 1;
    this.acc += Math.min(dt, 0.1) * slow;
    let steps = 0;
    while (this.acc >= DT && steps < 8) {
      this.acc -= DT; steps++;
      this.game.step(DT);
      this.onSnapshot && this.onSnapshot(this.game.snapshot(), this.game.drainEvents());
    }
    const g = this.game;
    if (g.phase === PHASE.END && g.phaseT > 0.05 && !this.endSent) {
      this.endSent = true;
      setTimeout(() => this.onEnd && this.onEnd({ stats: g.statsTable(), score: g.score, poss: g.possession() }), 3500);
    }
  }
  // اللاعب المتحكَّم فيه حالياً (يتغير مع التبديل التلقائي)
  ctrlId() {
    const g = this.game;
    if (g.ctrl && this.you >= 0) { const c = g.ctrl[g.players[this.you].team]; if (c != null) return c; }
    return this.you;
  }
  sendInput(inp) { const id = this.ctrlId(); if (id >= 0) this.game.setInput(id, inp); }
  sendEmote(n) { const id = this.ctrlId(); if (id >= 0) this.game.requestEmote(id, n); }
  close() {}
}

export class NetClient {
  constructor() {
    this.ws = null;
    this.handlers = {};
    this.id = null;
    this.rtt = 0.08;
    this.connected = false;
  }
  on(t, fn) { this.handlers[t] = fn; }
  connect() {
    return new Promise((resolve, reject) => {
      if (this.connected) return resolve();
      const proto = location.protocol === 'https:' ? 'wss' : 'ws';
      let ws;
      try { ws = new WebSocket(`${proto}://${location.host}/ws`); } catch (e) { return reject(e); }
      this.ws = ws;
      const timer = setTimeout(() => { reject(new Error('timeout')); try { ws.close(); } catch {} }, 6000);
      ws.onopen = () => {
        clearTimeout(timer);
        this.connected = true;
        this.pingTimer = setInterval(() => this.send({ t: 'ping', c: performance.now() }), 2000);
        resolve();
      };
      ws.onerror = () => { clearTimeout(timer); reject(new Error('ws error')); };
      ws.onclose = () => {
        this.connected = false;
        clearInterval(this.pingTimer);
        this.handlers.close && this.handlers.close();
      };
      ws.onmessage = (ev) => {
        let m;
        try { m = JSON.parse(ev.data); } catch { return; }
        if (m.t === 'welcome') this.id = m.id;
        if (m.t === 'pong') this.rtt = this.rtt * 0.7 + ((performance.now() - m.c) / 1000) * 0.3;
        const h = this.handlers[m.t];
        if (h) h(m);
      };
    });
  }
  send(m) { if (this.ws && this.ws.readyState === 1) this.ws.send(JSON.stringify(m)); }
}

// واجهة نقل المباراة عبر الشبكة
export class NetTransport {
  constructor(net) { this.net = net; }
  sendInput(inp) { this.net.send({ t: 'in', ...inp }); }
  sendEmote(n) { this.net.send({ t: 'emote', n }); }
  close() {}
}
