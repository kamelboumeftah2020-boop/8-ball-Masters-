import { nanoid } from 'nanoid';
import { getUser, persist, userByToken } from './store.js';
import { getTable } from './data/tables.js';
import { levelFromXp } from './util/econ.js';
import {
  enqueue, dequeueByUser, popOpponent, queueLength,
} from './game/matchQueue.js';
import {
  initMatchEngine, createMatch, getMatch, applyShot, applyRescueAd, applyDoubleAd,
} from './game/matchEngine.js';

const BOT_FILL_MS = 5000;

const presence = new Map(); // userId -> Set<socketId>
const socketUser = new Map(); // socketId -> userId

export function initSockets(io) {
  initMatchEngine(io);

  io.on('connection', (socket) => {
    socket.on('identify', ({ token }) => {
      const user = userByToken(token);
      if (!user || user.banned) { socket.emit('auth_error', { error: 'unauthenticated' }); return; }
      const userId = user.id;
      socketUser.set(socket.id, userId);
      socket.join(`user:${userId}`);
      if (!presence.has(userId)) presence.set(userId, new Set());
      presence.get(userId).add(socket.id);
      user.lastSeenAt = Date.now();
    });

    socket.on('join_queue', ({ tableId }) => {
      const userId = socketUser.get(socket.id);
      const user = getUser(userId);
      const table = getTable(tableId);
      if (!user || !table) return;
      if (levelFromXp(user.xp).level < table.unlockLvl) {
        socket.emit('queue_error', { error: 'level_locked' });
        return;
      }
      if (user.coins < table.entry) {
        socket.emit('queue_error', { error: 'insufficient_coins' });
        return;
      }
      dequeueByUser(table.id, user.id); // avoid double-entry

      const opponent = popOpponent(table.id, user.id);
      if (opponent) {
        startRankedMatch(io, table, [
          { userId: user.id, socketId: socket.id, cueId: user.equippedCueId },
          { userId: opponent.userId, socketId: opponent.socketId, cueId: getUser(opponent.userId)?.equippedCueId || 1 },
        ], [false, false]);
        return;
      }

      const entry = { userId: user.id, socketId: socket.id, joinedAt: Date.now() };
      entry.botTimer = setTimeout(() => {
        const stillQueued = dequeueByUser(table.id, user.id);
        if (!stillQueued) return;
        startRankedMatch(io, table, [
          { userId: user.id, socketId: socket.id, cueId: user.equippedCueId },
          { userId: `bot-${nanoid(6)}`, socketId: null, cueId: 1 },
        ], [false, true]);
      }, BOT_FILL_MS);
      enqueue(table.id, entry);
      socket.emit('queue_joined', { tableId: table.id, position: queueLength(table.id) });
    });

    socket.on('leave_queue', ({ tableId }) => {
      const userId = socketUser.get(socket.id);
      if (userId) dequeueByUser(tableId, userId);
    });

    // A client can only say how it struck the ball. The server runs the shot on
    // its own copy of the table and decides what went in.
    socket.on('shoot', ({ matchId, angle, power, spin }) => {
      const userId = socketUser.get(socket.id);
      const match = getMatch(matchId);
      if (!match || !userId) return;
      applyShot(match, userId, { angle, power, spin });
    });

    socket.on('watch_ad_rescue', ({ matchId }) => {
      const userId = socketUser.get(socket.id);
      const match = getMatch(matchId);
      if (!match || !userId) return;
      const applied = applyRescueAd(match, userId);
      socket.emit('rescue_result', { applied });
    });

    socket.on('watch_ad_double', ({ matchId }) => {
      const userId = socketUser.get(socket.id);
      const match = getMatch(matchId);
      if (!match || !userId) return;
      const bonus = applyDoubleAd(match, userId);
      socket.emit('double_result', { bonus });
    });

    socket.on('emoji', ({ matchId, emoji }) => {
      const userId = socketUser.get(socket.id);
      const match = getMatch(matchId);
      if (!match || !userId) return;
      const opp = match.players.find(p => p.userId !== userId);
      if (opp && !opp.isBot) io.to(`user:${opp.userId}`).emit('opponent_emoji', { emoji });
    });

    socket.on('challenge_friend', ({ friendId, tableId }) => {
      const userId = socketUser.get(socket.id);
      const user = getUser(userId);
      const friend = getUser(friendId);
      const table = getTable(tableId) || getTable(1);
      if (!user || !friend) return;
      const friendSockets = presence.get(friend.id);
      if (!friendSockets || friendSockets.size === 0) {
        socket.emit('challenge_error', { error: 'friend_offline' });
        return;
      }
      const friendSocketId = [...friendSockets][0];
      startFriendlyMatch(io, table, [
        { userId: user.id, socketId: socket.id, cueId: user.equippedCueId },
        { userId: friend.id, socketId: friendSocketId, cueId: friend.equippedCueId },
      ]);
    });

    socket.on('revenge', ({ matchId }) => {
      const userId = socketUser.get(socket.id);
      const old = getMatch(matchId);
      if (!old || old.status !== 'ended') return;
      const me = old.players.find(p => p.userId === userId);
      const opp = old.players.find(p => p.userId !== userId);
      if (!me || !opp) return;
      const table = old.table || getTable(old.tableId);
      const doubledEntry = old.entry * 2;
      const meUser = getUser(userId);
      if (!meUser || meUser.coins < doubledEntry) {
        socket.emit('queue_error', { error: 'insufficient_coins' });
        return;
      }
      if (opp.isBot) {
        startRankedMatch(io, table, [
          { userId, socketId: socket.id, cueId: meUser.equippedCueId },
          { userId: `bot-${nanoid(6)}`, socketId: null, cueId: 1 },
        ], [false, true], doubledEntry);
        return;
      }
      const oppSockets = presence.get(opp.userId);
      if (!oppSockets || oppSockets.size === 0) {
        socket.emit('queue_error', { error: 'opponent_offline' });
        return;
      }
      const oppUser = getUser(opp.userId);
      if (!oppUser || oppUser.coins < doubledEntry) {
        socket.emit('queue_error', { error: 'opponent_insufficient_coins' });
        return;
      }
      startRankedMatch(io, table, [
        { userId, socketId: socket.id, cueId: meUser.equippedCueId },
        { userId: opp.userId, socketId: [...oppSockets][0], cueId: oppUser.equippedCueId },
      ], [false, false], doubledEntry, 'revenge');
    });

    socket.on('disconnect', () => {
      const userId = socketUser.get(socket.id);
      socketUser.delete(socket.id);
      if (userId) {
        const set = presence.get(userId);
        if (set) {
          set.delete(socket.id);
          if (set.size === 0) presence.delete(userId);
        }
        const user = getUser(userId);
        if (user) { user.lastSeenAt = Date.now(); persist(); }
        for (const table of [...Array(10)].map((_, i) => i + 1)) dequeueByUser(table, userId);
      }
    });
  });
}

function startRankedMatch(io, table, players, isBot, entryOverride, modeOverride) {
  const id = nanoid(10);
  createMatch({
    id,
    tableId: table.id,
    mode: modeOverride || 'ranked',
    entry: entryOverride ?? table.entry,
    xpReward: table.xp,
    players,
    isBot,
  });
}

function startFriendlyMatch(io, table, players) {
  const id = nanoid(10);
  createMatch({
    id,
    tableId: table.id,
    mode: 'friendly',
    entry: 0,
    xpReward: 100,
    players,
    isBot: [false, false],
  });
}

export function isOnline(userId) {
  const set = presence.get(userId);
  return !!set && set.size > 0;
}
