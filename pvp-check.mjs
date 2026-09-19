// Two real clients, same table, at the same time: they must be paired with each other
// (not with bots), see each other's live clock and pot count, and settle opposite results.
import { io } from 'socket.io-client';
import { Table } from './public/js/engine/physics.js';

const BASE = 'http://localhost:3000';

async function api(method, path, body, token) {
  const headers = {};
  if (body) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(BASE + '/api' + path, {
    method,
    headers: Object.keys(headers).length ? headers : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  return { status: res.status, body: await res.json().catch(() => null) };
}

async function makePlayer(name) {
  const { body } = await api('POST', '/auth/guest', { nickname: name, avatarId: 0, country: 'INT' });
  const { user, token } = body;
  const socket = io(BASE, { transports: ['websocket'] });
  await new Promise(r => socket.on('connect', r));
  socket.emit('identify', { token });
  const p = { user, token, socket, match: null, result: null, sawOpponentProgress: false };
  socket.on('match_start', (m) => { p.match = m; p.layout = m.layout; });
  socket.on('match_resume', (m) => { p.resumed = m; p.match = m; p.layout = m.layout; });
  socket.on('table_sync', (m) => { p.layout = m.snapshot; });
  socket.on('match_tick', (m) => {
    if (m.opponent && m.opponent.potted > 0) p.sawOpponentProgress = true;
    p.lastTick = m;
  });
  socket.on('match_end', (r) => { p.result = r; });
  return p;
}

let cueBonuses = {};

// Brute-force a shot that pots a ball, using the very same physics the server runs.
function findPottingShot(layout) {
  for (const power of [0.45, 0.6, 0.8, 0.3]) {
    for (let i = 0; i < 180; i++) {
      const angle = (i / 180) * Math.PI * 2;
      const t = new Table(null, null, cueBonuses);
      t.applySnapshot(JSON.parse(JSON.stringify(layout)));
      let potted = 0;
      t.onPot = () => { potted++; };
      if (!t.shootCue(angle, power)) continue;
      let guard = 0;
      while (!t.isAllStopped() && guard++ < 4000) t.step(1 / 60);
      if (potted > 0) return { angle, power, predicted: potted };
    }
  }
  return null;
}

// Play real shots until the server has credited `target` pots to this player.
async function potBalls(player, target) {
  let guard = 0;
  while ((player.lastTick?.me.potted ?? 0) < target && !player.result && guard++ < 40) {
    const shot = findPottingShot(player.layout);
    if (!shot) return false;
    const before = player.lastTick?.me.potted ?? 0;
    player.socket.emit('shoot', { matchId: player.match.matchId, ...shot, spin: { x: 0, y: 0 } });
    await until(() => (player.lastTick?.me.potted ?? 0) > before || player.result, 8000);
  }
  return (player.lastTick?.me.potted ?? 0) >= target || !!player.result;
}

const wait = (ms) => new Promise(r => setTimeout(r, ms));
async function until(fn, timeout = 12000) {
  const t0 = Date.now();
  while (Date.now() - t0 < timeout) {
    if (fn()) return true;
    await wait(80);
  }
  return false;
}

function check(label, ok) {
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${label}`);
  return ok ? 0 : 1;
}

async function main() {
  cueBonuses = (await api('GET', '/cues')).body.cues.find(c => c.id === 1).bonuses;
  const stamp = Date.now() % 100000;
  const a = await makePlayer('PvpA' + stamp);
  const b = await makePlayer('PvpB' + stamp);
  const startCoins = { a: a.user.coins, b: b.user.coins };

  // both search the same table at the same moment - they should find each other
  a.socket.emit('join_queue', { tableId: 1 });
  await wait(250);
  b.socket.emit('join_queue', { tableId: 1 });

  let fails = 0;
  const matched = await until(() => a.match && b.match);
  fails += check('both players got a match', matched);
  if (!matched) process.exit(1);

  fails += check('paired with each other, not bots',
    a.match.opponent.userId === b.user.id && b.match.opponent.userId === a.user.id);
  fails += check('same match id on both sides', a.match.matchId === b.match.matchId);
  fails += check('entry fee is the table stake (200)', a.match.entry === 200);

  // Both play real shots. The server simulates each one and decides what dropped.
  fails += check('the server sent an authoritative ball layout', !!a.layout?.balls?.length);
  const bPotted = await potBalls(b, 2);
  fails += check('shots reported to the server score on the server', bPotted);
  await potBalls(a, 7);

  const ended = await until(() => a.result && b.result);
  fails += check('both players received the result', ended);
  if (!ended) process.exit(1);

  fails += check('A won, B lost', a.result.you.won === true && b.result.you.won === false);
  fails += check('B saw A\'s live progress during play', b.sawOpponentProgress);
  fails += check('winner gains the stake, loser pays it',
    a.result.you.coinsDelta === 200 && b.result.you.coinsDelta === -200);

  const afterA = (await api('GET', `/me/${a.user.id}`, null, a.token)).body.user;
  const afterB = (await api('GET', `/me/${b.user.id}`, null, b.token)).body.user;
  fails += check('server balances match the settlement',
    afterA.coins === startCoins.a + 200 && afterB.coins === startCoins.b - 200);
  fails += check('win/loss recorded on both profiles',
    afterA.totalWins === 1 && afterB.totalLosses === 1);
  fails += check('0% tax: the pot moved whole, nothing vanished',
    (afterA.coins + afterB.coins) === (startCoins.a + startCoins.b));

  fails += await reconnectScenario();
  fails += await cheatScenario();
  fails += await authScenario(a, b);
  fails += await timeoutScenario();

  console.log(fails === 0 ? '\nPvP OK' : `\n${fails} CHECK(S) FAILED`);
  process.exit(fails === 0 ? 0 : 1);
}

// Dropping out mid-match must not be an automatic loss.
async function reconnectScenario() {
  console.log('\n-- reconnect --');
  const stamp = Date.now() % 100000;
  const f = await makePlayer('Recon' + stamp);
  f.socket.emit('join_queue', { tableId: 1 });
  if (!await until(() => f.match)) return check('reconnect match started', false);
  await until(() => f.lastTick, 4000);

  // pot one so there is real progress to come back to
  await potBalls(f, 1);
  const pottedBefore = f.lastTick.me.potted;
  const matchId = f.match.matchId;

  let fails = 0;
  f.socket.disconnect();
  await wait(1200);

  // come back on a brand new connection, exactly like reopening the app
  const back = io(BASE, { transports: ['websocket'] });
  await new Promise(r => back.on('connect', r));
  let resumed = null;
  back.on('match_resume', (m) => { resumed = m; });
  back.emit('identify', { token: f.token });
  const gotBack = await until(() => resumed, 6000);

  fails += check('a returning player is put back into their match', gotBack);
  if (gotBack) {
    fails += check('back in the same match', resumed.matchId === matchId);
    fails += check('progress survived the disconnect', resumed.me.potted === pottedBefore);
    fails += check('the table came back with them', !!resumed.layout?.balls?.length);
  }
  back.disconnect();
  return fails;
}

// A tampered client must not be able to award itself anything: the only thing it
// can send is how it struck the ball.
async function cheatScenario() {
  console.log('\n-- cheat attempts --');
  const stamp = Date.now() % 100000;
  const e = await makePlayer('Cheat' + stamp);
  e.socket.emit('join_queue', { tableId: 1 });
  if (!await until(() => e.match)) return check('cheat-test match started', false);
  await until(() => e.lastTick, 4000);

  let fails = 0;
  const before = e.lastTick.me.potted;

  // the old "I potted a ball" message no longer exists
  for (let i = 0; i < 12; i++) e.socket.emit('ball_potted', { matchId: e.match.matchId });
  // nor can a client claim a pot under any other name
  for (let i = 0; i < 12; i++) e.socket.emit('match_tick', { me: { potted: 7 } });
  await wait(900);
  fails += check('claiming pots directly does nothing', e.lastTick.me.potted === before);

  // a shot with absurd values is clamped, not trusted
  e.socket.emit('shoot', { matchId: e.match.matchId, angle: 0, power: 9999, spin: { x: 50, y: 50 } });
  await wait(900);
  fails += check('an out-of-range shot cannot pot the whole table', e.lastTick.me.potted < 7);

  e.socket.emit('shoot', { matchId: 'some-other-match', angle: 0, power: 0.5 });
  await wait(400);
  fails += check('shooting at a match you are not in is ignored', e.lastTick.me.potted < 7);

  e.socket.disconnect();
  return fails;
}

// A player's 8-digit ID is printed publicly on their profile, so knowing it must
// never be enough to act as them.
async function authScenario(a, b) {
  console.log('\n-- account security --');
  let fails = 0;

  const noToken = await api('GET', `/me/${a.user.id}`);
  fails += check('reading an account without a token is refused', noToken.status === 401);

  const wrongToken = await api('GET', `/me/${a.user.id}`, null, b.token);
  fails += check("another player's token cannot read your account", wrongToken.status === 403);

  const spendSomeoneElsesCoins = await api('POST', '/shop/buy-cue', { userId: a.user.id, cueId: 2 }, b.token);
  const afterA = (await api('GET', `/me/${a.user.id}`, null, a.token)).body.user;
  fails += check('a userId in the body cannot redirect a purchase to another account',
    !afterA.ownedCues.includes(2));

  const renameAttempt = await api('PUT', '/nickname', { userId: a.user.id, nickname: 'Hijacked' }, null);
  fails += check('renaming an account without a token is refused', renameAttempt.status === 401);

  const garbage = await api('GET', '/session', null, 'not-a-real-token');
  fails += check('a made-up token is rejected', garbage.status === 401);

  const mine = await api('GET', '/session', null, a.token);
  fails += check('your own token still works', mine.status === 200 && mine.body.user.id === a.user.id);
  return fails;
}

// The spec's second win condition: you also win when your opponent's clock hits 00.00.
// Here C pots nothing and runs out; D must take it even though D potted nothing either.
async function timeoutScenario() {
  console.log('\n-- clock-expiry win (takes ~25s) --');
  const stamp = Date.now() % 100000;
  const c = await makePlayer('PvpC' + stamp);
  const d = await makePlayer('PvpD' + stamp);

  c.socket.emit('join_queue', { tableId: 1 });
  await wait(250);
  d.socket.emit('join_queue', { tableId: 1 });
  if (!await until(() => c.match && d.match)) return check('timeout match started', false);

  // D keeps its clock alive by potting; C never shoots and runs out of time.
  const keepAlive = setInterval(() => {
    if (d.result || !d.layout) return;
    const shot = findPottingShot(d.layout);
    if (shot) d.socket.emit('shoot', { matchId: d.match.matchId, ...shot, spin: { x: 0, y: 0 } });
  }, 3000);

  const ended = await until(() => c.result && d.result, 40000);
  clearInterval(keepAlive);
  let fails = check('match ended when a clock expired', ended);
  if (!ended) return fails;

  fails += check('the player whose clock expired lost', c.result.you.won === false && d.result.you.won === true);
  fails += check('loser got a consolation box',
    Array.isArray((await api('GET', `/me/${c.user.id}`, null, c.token)).body.user.lossBoxes));
  return fails;
}

main().catch((e) => { console.error(e); process.exit(1); });
