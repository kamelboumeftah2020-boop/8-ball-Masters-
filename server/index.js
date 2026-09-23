// خادم اللعبة: ملفات ثابتة + WebSocket + غرف + محاكاة موثوقة 60Hz
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocketServer } from 'ws';
import { Game } from '../shared/sim.js';
import { TICK_RATE, DT, SNAPSHOT_EVERY, TEAM_SIZE, PHASE, DURATIONS, DIFFICULTY, MODES } from '../shared/constants.js';
import { CHAR_BY_ID } from '../shared/characters.js';
import { STADIUM_BY_ID } from '../shared/stadiums.js';
import { CLUB_BY_ID } from '../shared/clubs.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const PORT = process.env.PORT || 3000;

const MOUNTS = [
  ['/shared/', path.join(ROOT, 'shared')],
  ['/vendor/three/', path.join(ROOT, 'node_modules/three')],
  ['/', path.join(ROOT, 'public')],
];
const MIME = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.json': 'application/json', '.png': 'image/png', '.svg': 'image/svg+xml', '.ico': 'image/x-icon',
  '.webmanifest': 'application/manifest+json',
};

const server = http.createServer((req, res) => {
  let url = decodeURIComponent((req.url || '/').split('?')[0]);
  if (url === '/health') { res.writeHead(200); return res.end('ok'); }
  if (url === '/') url = '/index.html';
  for (const [prefix, dir] of MOUNTS) {
    if (!url.startsWith(prefix)) continue;
    const file = path.normalize(path.join(dir, url.slice(prefix.length)));
    if (!file.startsWith(dir)) break;
    if (prefix === '/vendor/three/' && !/\/(build|examples\/jsm)\//.test(file)) break;
    return fs.stat(file, (err, st) => {
      if (err || !st.isFile()) { res.writeHead(404); return res.end('not found'); }
      res.writeHead(200, {
        'Content-Type': MIME[path.extname(file)] || 'application/octet-stream',
        'Cache-Control': prefix === '/vendor/three/' ? 'public, max-age=86400' : 'no-cache',
      });
      fs.createReadStream(file).pipe(res);
    });
  }
  res.writeHead(404); res.end('not found');
});

const wss = new WebSocketServer({ server, path: '/ws', maxPayload: 16 * 1024 });

const clients = new Map(); // id -> client
const rooms = new Map(); // code -> room
let nextId = 1;

const clean = (s, n = 16) => String(s || '').replace(/[<>&"'`]/g, '').trim().slice(0, n);
function send(c, msg) { if (c.ws.readyState === 1) c.ws.send(JSON.stringify(msg)); }
function makeCode() {
  const A = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code;
  do { code = Array.from({ length: 5 }, () => A[Math.floor(Math.random() * A.length)]).join(''); } while (rooms.has(code));
  return code;
}

// ---------------- الغرف ----------------
class Room {
  constructor(code, host, settings) {
    this.code = code;
    this.host = host.id;
    this.members = new Map();
    this.settings = { stadium: 'royal', duration: 180, difficulty: 'normal', mode: 'real', home: 'falcons', away: 'tigers', public: true, name: `غرفة ${host.name}` };
    this.applySettings(settings || {});
    this.game = null;
    this.timer = null;
    this.chat = [];
  }
  applySettings(s) {
    if (s.stadium && STADIUM_BY_ID[s.stadium]) this.settings.stadium = s.stadium;
    if (DURATIONS.includes(+s.duration)) this.settings.duration = +s.duration;
    if (s.difficulty && DIFFICULTY[s.difficulty]) this.settings.difficulty = s.difficulty;
    if (typeof s.public === 'boolean') this.settings.public = s.public;
    if (s.mode && MODES[s.mode]) this.settings.mode = s.mode;
    if (s.home && CLUB_BY_ID[s.home]) this.settings.home = s.home;
    if (s.away && CLUB_BY_ID[s.away] && s.away !== this.settings.home) this.settings.away = s.away;
    if (this.settings.away === this.settings.home) this.settings.away = Object.keys(CLUB_BY_ID).find((k) => k !== this.settings.home);
    if (s.name) this.settings.name = clean(s.name, 24);
  }
  slotTaken(team, slot) {
    for (const m of this.members.values()) if (m.team === team && m.slot === slot) return true;
    return false;
  }
  autoSlot(c) {
    const counts = [0, 0];
    for (const m of this.members.values()) if (m.team >= 0 && m !== c) counts[m.team]++;
    const order = counts[0] <= counts[1] ? [0, 1] : [1, 0];
    for (const team of order) for (const slot of [3, 4, 1, 2, 0]) if (!this.slotTaken(team, slot)) { c.team = team; c.slot = slot; return; }
    c.team = -1; c.slot = -1;
  }
  add(c) {
    this.members.set(c.id, c);
    c.room = this;
    this.autoSlot(c);
    if (this.game) {
      if (c.team >= 0) { this.game.setHuman(c.team * TEAM_SIZE + c.slot, { cid: c.id, name: c.name, char: c.char }); }
      this.sendMatch();
    }
    this.broadcastRoom();
  }
  remove(c) {
    this.members.delete(c.id);
    if (this.game && c.team >= 0) { this.game.setBot(c.team * TEAM_SIZE + c.slot); this.sendMatch(); }
    c.room = null; c.team = -1; c.slot = -1;
    if (this.members.size === 0) return this.destroy();
    if (this.host === c.id) this.host = this.members.keys().next().value;
    this.broadcastRoom();
  }
  destroy() {
    clearInterval(this.timer);
    rooms.delete(this.code);
  }
  info() {
    return {
      code: this.code, host: this.host, settings: this.settings, playing: !!this.game,
      members: [...this.members.values()].map((m) => ({ id: m.id, name: m.name, char: m.char, team: m.team, slot: m.slot })),
    };
  }
  broadcast(msg) {
    const s = JSON.stringify(msg);
    for (const m of this.members.values()) if (m.ws.readyState === 1) m.ws.send(s);
  }
  broadcastRoom() { this.broadcast({ t: 'room', room: this.info() }); }
  sendMatch() {
    if (!this.game) return;
    const roster = this.game.roster();
    for (const m of this.members.values()) {
      send(m, { t: 'match', stadium: this.settings.stadium, duration: this.settings.duration, difficulty: this.settings.difficulty, mode: this.settings.mode, home: this.settings.home, away: this.settings.away, roster, you: m.team >= 0 ? m.team * TEAM_SIZE + m.slot : -1 });
    }
  }
  start() {
    if (this.game) return;
    const roster = [[], []];
    for (const m of this.members.values()) if (m.team >= 0) roster[m.team][m.slot] = { name: m.name, char: m.char, human: true, cid: m.id };
    this.game = new Game({ ...this.settings, roster, seed: Math.floor(Math.random() * 1e9) });
    this.tick = 0;
    this.sendMatch();
    this.broadcastRoom();
    let last = performance.now(), acc = 0;
    this.timer = setInterval(() => {
      const now = performance.now();
      acc += Math.min(0.25, (now - last) / 1000);
      last = now;
      while (acc >= DT) {
        acc -= DT;
        this.game.step(DT);
        this.tick++;
        if (this.tick % SNAPSHOT_EVERY === 0) {
          const snap = this.game.snapshot();
          const ev = this.game.drainEvents();
          this.broadcast({ t: 's', s: snap, ev });
        }
        if (this.game.phase === PHASE.END && this.game.phaseT > 0.05 && !this.endSent) {
          this.endSent = true;
          this.broadcast({ t: 'end', stats: this.game.statsTable(), score: this.game.score, poss: this.game.possession() });
        }
        if (this.game.phase === PHASE.END && this.game.phaseT > 14) return this.stop();
      }
    }, 1000 / TICK_RATE);
  }
  stop() {
    clearInterval(this.timer);
    this.timer = null;
    this.game = null;
    this.endSent = false;
    this.broadcast({ t: 'lobby' });
    this.broadcastRoom();
  }
}

function roomList() {
  return [...rooms.values()].filter((r) => r.settings.public).slice(0, 30).map((r) => ({
    code: r.code, name: r.settings.name, stadium: r.settings.stadium, players: r.members.size, playing: !!r.game,
  }));
}

// ---------------- الاتصالات ----------------
wss.on('connection', (ws) => {
  const c = { id: nextId++, ws, name: 'لاعب', char: 'blaze', room: null, team: -1, slot: -1, lastMsg: 0, rate: 0 };
  clients.set(c.id, c);
  send(c, { t: 'welcome', id: c.id });
  ws.isAlive = true;
  ws.on('pong', () => (ws.isAlive = true));

  ws.on('message', (data) => {
    // حد بسيط لمعدل الرسائل
    const now = Date.now();
    if (now - c.lastMsg > 1000) { c.lastMsg = now; c.rate = 0; }
    if (++c.rate > 200) return;
    let m;
    try { m = JSON.parse(data); } catch { return; }
    if (!m || typeof m.t !== 'string') return;
    const room = c.room;
    const g = room && room.game;
    switch (m.t) {
      case 'in': {
        if (g && c.team >= 0) g.setInput(c.team * TEAM_SIZE + c.slot, m);
        break;
      }
      case 'ping': send(c, { t: 'pong', c: m.c, s: g ? g.time : 0 }); break;
      case 'hello':
        c.name = clean(m.name) || c.name;
        if (CHAR_BY_ID[m.char]) c.char = m.char;
        break;
      case 'list': send(c, { t: 'rooms', list: roomList() }); break;
      case 'create': {
        if (room) room.remove(c);
        const r = new Room(makeCode(), c, m.settings);
        rooms.set(r.code, r);
        r.add(c);
        break;
      }
      case 'join': {
        const r = rooms.get(clean(m.code, 8).toUpperCase());
        if (!r) return send(c, { t: 'err', msg: 'الغرفة غير موجودة' });
        if (r.members.size >= 16) return send(c, { t: 'err', msg: 'الغرفة ممتلئة' });
        if (room) room.remove(c);
        r.add(c);
        break;
      }
      case 'leave': if (room) room.remove(c); send(c, { t: 'left' }); break;
      case 'slot': {
        if (!room) return;
        const team = m.team | 0, slot = m.slot | 0;
        if (team === -1) {
          if (g && c.team >= 0) g.setBot(c.team * TEAM_SIZE + c.slot);
          c.team = -1; c.slot = -1;
        } else {
          if (team < 0 || team > 1 || slot < 0 || slot >= TEAM_SIZE || room.slotTaken(team, slot)) return;
          if (g && c.team >= 0) g.setBot(c.team * TEAM_SIZE + c.slot);
          c.team = team; c.slot = slot;
          if (g) g.setHuman(team * TEAM_SIZE + slot, { cid: c.id, name: c.name, char: c.char });
        }
        room.broadcastRoom();
        if (g) room.sendMatch();
        break;
      }
      case 'char':
        if (!CHAR_BY_ID[m.char]) return;
        c.char = m.char;
        if (room) room.broadcastRoom();
        break;
      case 'settings':
        if (room && room.host === c.id && !g) { room.applySettings(m.settings || {}); room.broadcastRoom(); }
        break;
      case 'start':
        if (room && room.host === c.id && !g) room.start();
        break;
      case 'stop':
        if (room && room.host === c.id && g) room.stop();
        break;
      case 'emote':
        if (g && c.team >= 0) g.requestEmote(c.team * TEAM_SIZE + c.slot, m.n);
        break;
      case 'chat': {
        const text = clean(m.text, 80);
        if (room && text) room.broadcast({ t: 'chat', from: c.name, team: c.team, text });
        break;
      }
    }
  });

  ws.on('close', () => {
    if (c.room) c.room.remove(c);
    clients.delete(c.id);
  });
});

setInterval(() => {
  for (const ws of wss.clients) {
    if (!ws.isAlive) { ws.terminate(); continue; }
    ws.isAlive = false;
    ws.ping();
  }
}, 15000);

server.listen(PORT, () => {
  console.log(`⚽ Football Legends 5v5 running on http://localhost:${PORT}`);
});
