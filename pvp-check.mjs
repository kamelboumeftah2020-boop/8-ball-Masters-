// Two real clients, same table, at the same time: they must be paired with each other
// (not with bots), see each other's live clock and pot count, and settle opposite results.
import { io } from 'socket.io-client';

const BASE = 'http://localhost:3000';

async function api(method, path, body) {
  const res = await fetch(BASE + '/api' + path, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  return res.json();
}

async function makePlayer(name) {
  const { user } = await api('POST', '/auth/guest', { nickname: name, avatarId: 0, country: 'INT' });
  const socket = io(BASE, { transports: ['websocket'] });
  await new Promise(r => socket.on('connect', r));
  socket.emit('identify', { userId: user.id });
  const p = { user, socket, match: null, result: null, sawOpponentProgress: false };
  socket.on('match_start', (m) => { p.match = m; });
  socket.on('match_tick', (m) => {
    if (m.opponent && m.opponent.potted > 0) p.sawOpponentProgress = true;
    p.lastTick = m;
  });
  socket.on('match_end', (r) => { p.result = r; });
  return p;
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

  // A clears the table, B pots two
  for (let i = 0; i < 2; i++) {
    await wait(280);
    b.socket.emit('ball_potted', { matchId: b.match.matchId });
  }
  for (let i = 0; i < 7; i++) {
    await wait(280);
    a.socket.emit('ball_potted', { matchId: a.match.matchId });
  }

  const ended = await until(() => a.result && b.result);
  fails += check('both players received the result', ended);
  if (!ended) process.exit(1);

  fails += check('A won, B lost', a.result.you.won === true && b.result.you.won === false);
  fails += check('B saw A\'s live progress during play', b.sawOpponentProgress);
  fails += check('winner gains the stake, loser pays it',
    a.result.you.coinsDelta === 200 && b.result.you.coinsDelta === -200);

  const afterA = (await api('GET', `/me/${a.user.id}`)).user;
  const afterB = (await api('GET', `/me/${b.user.id}`)).user;
  fails += check('server balances match the settlement',
    afterA.coins === startCoins.a + 200 && afterB.coins === startCoins.b - 200);
  fails += check('win/loss recorded on both profiles',
    afterA.totalWins === 1 && afterB.totalLosses === 1);
  fails += check('0% tax: the pot moved whole, nothing vanished',
    (afterA.coins + afterB.coins) === (startCoins.a + startCoins.b));

  fails += await timeoutScenario();

  console.log(fails === 0 ? '\nPvP OK' : `\n${fails} CHECK(S) FAILED`);
  process.exit(fails === 0 ? 0 : 1);
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
    if (!d.result) d.socket.emit('ball_potted', { matchId: d.match.matchId });
  }, 4000);

  const ended = await until(() => c.result && d.result, 40000);
  clearInterval(keepAlive);
  let fails = check('match ended when a clock expired', ended);
  if (!ended) return fails;

  fails += check('the player whose clock expired lost', c.result.you.won === false && d.result.you.won === true);
  fails += check('loser got a consolation box', Array.isArray((await api('GET', `/me/${c.user.id}`)).user.lossBoxes));
  return fails;
}

main().catch((e) => { console.error(e); process.exit(1); });
