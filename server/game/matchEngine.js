import { getUser, persist, logMatch, refreshMissionProgress, cueById } from '../store.js';
import { getTable } from '../data/tables.js';
import { getStar, STARS } from '../data/stars.js';
import { levelFromXp } from '../util/econ.js';
// The server runs the very same simulation the client draws. Because the physics
// is frame-rate independent, replaying a player's shot here yields exactly the
// state they see - so pots are decided by the server, not claimed by the client.
import { Table } from '../../public/js/engine/physics.js';

const TICK_MS = 100;
const START_TIME = 20.0;
const BALL_BONUS = 10.0;
const TOTAL_BALLS = 7;
const RESCUE_BONUS = 10.0;

const matches = new Map(); // matchId -> match

let ioRef = null;
export function initMatchEngine(io) { ioRef = io; }

function emitToUser(userId, event, payload) {
  ioRef?.to(`user:${userId}`).emit(event, payload);
}

export function createMatch({ id, tableId, mode = 'ranked', entry = 0, xpReward = 0, players, isBot = [false, false] }) {
  const table = getTable(tableId);
  const startTimes = players.map(p => {
    const cue = cueById(p.cueId) || cueById(1);
    return +(START_TIME + (cue?.bonuses?.startBonus || 0)).toFixed(2);
  });

  const match = {
    id,
    tableId,
    table,
    mode,
    entry,
    xpReward,
    status: 'live',
    startedAt: Date.now(),
    rescueUsed: [false, false],
    players: players.map((p, i) => ({
      userId: p.userId,
      socketId: p.socketId,
      cueId: p.cueId,
      isBot: isBot[i],
      time: startTimes[i],
      potted: 0,
      scratches: 0,
      finished: false,
      won: false,
    })),
    interval: null,
    finishedCount: 0,
  };
  matches.set(id, match);

  for (const p of match.players) {
    if (p.isBot) continue;
    const cue = cueById(p.cueId) || cueById(1);
    p.table = new Table(
      () => applyPot(match, p),
      () => applyScratch(match, p),
      cue?.bonuses || {},
    );
  }

  for (const p of match.players) {
    if (!p.isBot) {
      emitToUser(p.userId, 'match_start', serializeMatchFor(match, p.userId));
    }
  }

  match.interval = setInterval(() => tick(match), TICK_MS);

  // Simple bot AI: pots a ball on a random interval so single-player testing / offline
  // demo play always has a "live" opponent.
  for (const p of match.players) {
    if (p.isBot) scheduleBotShot(match, p);
  }

  return match;
}

// Bot skill climbs with the table tier, so Garage is a warm-up and the top tables
// actually punish a slow player: it lines up faster and misses less often.
function botSkill(tableId) {
  return Math.min(0.94, 0.34 + (Number(tableId) || 1) * 0.062);
}

function scheduleBotShot(match, botPlayer) {
  if (match.status !== 'live' || botPlayer.finished) return;
  const skill = botSkill(match.tableId);
  const base = 2700 - skill * 1500;              // ~2.5s down to ~1.3s between shots
  const delay = base + Math.random() * base * 0.55;
  setTimeout(() => {
    if (match.status !== 'live' || botPlayer.finished) return;
    if (Math.random() > skill) {
      // missed the pot - the clock keeps running, then it lines up again
      scheduleBotShot(match, botPlayer);
      return;
    }
    applyPot(match, botPlayer);
    scheduleBotShot(match, botPlayer);
  }, delay);
}

// How many real people are playing this table right now. Feeds the lobby's
// online count, which reports what is actually happening rather than a number
// invented to look busy.
export function playersOnTable(tableId) {
  let n = 0;
  for (const match of matches.values()) {
    if (match.status !== 'live' || match.tableId !== tableId) continue;
    for (const p of match.players) if (!p.isBot) n++;
  }
  return n;
}

export function getMatch(id) {
  return matches.get(id);
}

// Used when a player comes back after dropping out: their match kept running
// server-side, so they can be put straight back into it with the real state.
export function resumeMatchFor(userId) {
  for (const match of matches.values()) {
    if (match.status !== 'live') continue;
    const player = findPlayer(match, userId);
    if (player && !player.isBot && !player.finished) {
      return serializeMatchFor(match, userId);
    }
  }
  return null;
}

function otherPlayer(match, userId) {
  return match.players.find(p => p.userId !== userId);
}
function findPlayer(match, userId) {
  return match.players.find(p => p.userId === userId);
}

function serializeMatchFor(match, userId) {
  const me = findPlayer(match, userId);
  const opp = otherPlayer(match, userId);
  return {
    matchId: match.id,
    tableId: match.tableId,
    table: match.table,
    mode: match.mode,
    entry: match.entry,
    me: { time: me.time, potted: me.potted, totalBalls: TOTAL_BALLS },
    layout: me.table ? me.table.snapshot() : null,
    opponent: opp ? {
      userId: opp.userId,
      nickname: getUser(opp.userId)?.nickname || 'Bot',
      avatarId: getUser(opp.userId)?.avatarId ?? 0,
      time: opp.time,
      potted: opp.potted,
      totalBalls: TOTAL_BALLS,
    } : null,
  };
}

function tick(match) {
  if (match.status !== 'live') return;

  // Advance every player's table on the server's clock. Pot and scratch callbacks
  // fire from here, which is what actually scores the match.
  for (const p of match.players) {
    if (!p.table || p.finished) continue;
    const wasSettled = p.table.isAllStopped();
    p.table.step(TICK_MS / 1000);
    if (!wasSettled && p.table.isAllStopped()) {
      emitToUser(p.userId, 'table_sync', { matchId: match.id, snapshot: p.table.snapshot() });
    }
  }

  let anyAlive = false;
  for (const p of match.players) {
    if (p.finished) continue;
    p.time = Math.max(0, +(p.time - TICK_MS / 1000).toFixed(2));
    anyAlive = true;
    if (p.time <= 0) {
      p.finished = true;
      p.timedOut = true;
      p.won = false;
    }
  }
  broadcastTick(match);
  // The match is decided the moment either player finishes: one of them cleared the
  // table, or one of them ran out of clock. Nobody keeps playing a settled match.
  if (match.players.some(p => p.finished) || !anyAlive) {
    resolveMatch(match);
  }
}

function broadcastTick(match) {
  for (const p of match.players) {
    if (p.isBot) continue;
    emitToUser(p.userId, 'match_tick', serializeMatchFor(match, p.userId));
  }
}

export function applyPot(match, player) {
  if (match.status !== 'live' || player.finished) return;
  player.potted += 1;
  player.time = +(player.time + BALL_BONUS).toFixed(2);
  if (player.potted >= TOTAL_BALLS) {
    player.finished = true;
    player.won = true;
  }
  broadcastTick(match);
  if (player.finished) checkResolution(match);
}

// A client sends what it did (angle, power, spin) - never what it scored.
export function applyShot(match, userId, { angle, power, spin }) {
  if (match.status !== 'live') return false;
  const player = findPlayer(match, userId);
  if (!player || player.finished || !player.table) return false;
  if (!player.table.isAllStopped()) return false;      // still rolling: ignore
  const a = Number(angle), p = Number(power);
  if (!Number.isFinite(a) || !Number.isFinite(p)) return false;
  return player.table.shootCue(a, Math.max(0, Math.min(1, p)), {
    x: Math.max(-1, Math.min(1, Number(spin?.x) || 0)),
    y: Math.max(-1, Math.min(1, Number(spin?.y) || 0)),
  });
}

export function applyScratch(match, player) {
  if (match.status !== 'live' || player.finished) return;
  player.scratches += 1;
  broadcastTick(match);
}

export function applyRescueAd(match, userId) {
  const player = findPlayer(match, userId);
  const idx = match.players.indexOf(player);
  if (!player || player.finished || match.rescueUsed[idx]) return false;
  match.rescueUsed[idx] = true;
  player.time = +(player.time + RESCUE_BONUS).toFixed(2);
  broadcastTick(match);
  return true;
}

function checkResolution(match) {
  if (match.players.some(p => p.finished)) resolveMatch(match);
}

function marginAtLoss(loser, winner) {
  // "near miss" if loser lost with the winner having only just edged them out
  // (winner finished with < 2s of buffer time remaining relative to when loser ran out,
  // approximated here by the loser's own potted-count gap: 6/7 balls potted = 1 away).
  return TOTAL_BALLS - loser.potted;
}

function resolveMatch(match) {
  if (match.status !== 'live') return;
  match.status = 'ended';
  clearInterval(match.interval);

  const [p1, p2] = match.players;
  let winner, loser;
  const cleared = match.players.find(p => p.potted >= TOTAL_BALLS);
  const ranOut = match.players.find(p => p.timedOut);
  if (cleared) {
    // cleared the table first
    winner = cleared;
  } else if (ranOut && !match.players.every(p => p.timedOut)) {
    // the other player's clock hit 00.00
    winner = match.players.find(p => p !== ranOut);
  } else {
    // both clocks expired together (rare) -> more balls potted takes it
    winner = p1.potted >= p2.potted ? p1 : p2;
  }
  loser = match.players.find(p => p !== winner);
  winner.won = true;
  loser.won = false;

  const results = {};
  for (const side of [winner, loser]) {
    const isWinner = side === winner;
    const user = side.isBot ? null : getUser(side.userId);
    let coinsDelta = 0;
    let xpGained = 0;
    let leveledUp = false;
    let newLevel = null;

    if (user) {
      user.totalPlayed += 1;
      refreshMissionProgress(user, 'matchesPlayed', 1);
      refreshMissionProgress(user, 'ballsPotted', side.potted);

      if (isWinner) {
        user.totalWins += 1;
        user.winStreak += 1;
        coinsDelta = match.entry; // net profit = opponent's entry, 0% tax, winner takes the full pot
        if (user.winStreak > 0 && user.winStreak % 4 === 0) coinsDelta *= 2; // 4th win in a row streak bonus
        xpGained = match.xpReward;
        if (side.scratches === 0) refreshMissionProgress(user, 'cleanWins', 1);
        refreshMissionProgress(user, 'wins', 1);
        if (match.tableId && match.table) {
          refreshMissionProgress(user, `winsOnTable:${match.table.key}`, 1);
        }
        user.coins += coinsDelta;
        user.careerCoinsWon += coinsDelta;
        user.weeklyCoins += coinsDelta;
        maybePromoteStar(user);
      } else {
        user.totalLosses += 1;
        user.winStreak = 0;
        coinsDelta = -match.entry;
        user.coins = Math.max(0, user.coins + coinsDelta);
        xpGained = Math.round(match.xpReward * 0.15);
        // loss consolation box, opens in 2 real hours
        user.lossBoxes.push({ id: `${match.id}-box`, availableAt: Date.now() + 2 * 60 * 60 * 1000, claimed: false, opened: false, coins: Math.round(match.entry * 0.3) + 100 });
      }
      user.xp += xpGained;
      const before = levelFromXp(user.xp - xpGained).level;
      const afterLvl = levelFromXp(user.xp);
      if (afterLvl.level > before) { leveledUp = true; newLevel = afterLvl.level; }
    }

    results[side.userId] = {
      won: isWinner,
      coinsDelta,
      xpGained,
      leveledUp,
      newLevel,
      potted: side.potted,
      nearMiss: !isWinner && marginAtLoss(side, isWinner ? side : winner) === 1,
      streak: user ? user.winStreak : 0,
      canRevenge: !isWinner,
    };
  }

  match.results = results;
  match.doubleAdUsed = {};
  persist();
  logMatch({ id: match.id, tableId: match.tableId, mode: match.mode, entry: match.entry, at: Date.now(), players: match.players.map(p => ({ userId: p.userId, potted: p.potted, won: p.won })) });

  for (const p of match.players) {
    if (p.isBot) continue;
    const opp = otherPlayer(match, p.userId);
    emitToUser(p.userId, 'match_end', {
      matchId: match.id,
      you: results[p.userId],
      opponentNickname: opp?.isBot ? 'Bot' : getUser(opp?.userId)?.nickname,
      opponentUserId: opp?.userId,
      opponentIsBot: !!opp?.isBot,
      table: match.table,
      entry: match.entry,
    });
  }

  setTimeout(() => matches.delete(match.id), 60_000);
}

function maybePromoteStar(user) {
  const star = getStar(user.starId);
  if (star.id >= STARS.length) return; // top tier, no auto-promote
  if (star.promoteAt != null && user.weeklyCoins >= star.promoteAt) {
    user.starId = Math.min(STARS.length, star.id + 1);
    user.weeklyCoins = 0;
  }
}

// Optional "watch ad to double your prize" - only valid once, right after a win.
export function applyDoubleAd(match, userId) {
  if (!match || match.status !== 'ended' || !match.results) return null;
  const result = match.results[userId];
  if (!result || !result.won || result.coinsDelta <= 0) return null;
  if (match.doubleAdUsed[userId]) return null;
  const user = getUser(userId);
  if (!user) return null;
  const bonus = result.coinsDelta;
  user.coins += bonus;
  user.careerCoinsWon += bonus;
  user.weeklyCoins += bonus;
  match.doubleAdUsed[userId] = true;
  persist();
  return bonus;
}

export { TOTAL_BALLS, START_TIME };
