import { Router } from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  db, getUser, createAccount, findByNickname, persist,
  createInvite, addReport, reportStatus, ensureFreshMissions, cueById, avatarById, allUsers,
  issueToken, userByToken, setPin, hasPin, checkPin,
  checkRecoveryCode, issueRecoveryCode, lockRemaining, noteFailure, clearFailures,
} from '../store.js';
import { TABLES, getTable } from '../data/tables.js';
import { CUES } from '../data/cues.js';
import { AVATARS } from '../data/avatars.js';
import { COIN_PACKAGES, getPackage } from '../data/coinPackages.js';
import { STARS, weeklyPrizes } from '../data/stars.js';
import { levelFromXp } from '../util/econ.js';
import { privateUserDto, publicUserDto, tableDto } from '../util/dto.js';
import { queueLength } from '../game/matchQueue.js';
import { playersOnTable } from '../game/matchEngine.js';
import { nextResetAt } from '../game/weekly.js';
import { createRateLimiter, callerKey, isLoopback } from '../util/rateLimit.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const I18N_DIR = path.join(__dirname, '..', 'data', 'i18n');
const LANGS = ['en', 'ar', 'fr', 'de', 'es', 'tr'];

export const api = Router();

// Resolves the caller from their bearer token. Anything that reads or changes an
// account goes through here - a userId in the request body is never trusted.
api.use((req, res, next) => {
  const header = req.get('authorization') || '';
  const token = header.startsWith('Bearer ') ? header.slice(7) : null;
  req.authUser = userByToken(token);
  if (req.authUser) req.authUser.lastSeenAt = Date.now();
  next();
});

function requireUser(req, res) {
  const user = req.authUser;
  if (!user) { res.status(401).json({ error: 'unauthenticated' }); return null; }
  if (user.banned) { res.status(403).json({ error: 'banned', reason: user.banReason }); return null; }
  return user;
}

// For endpoints that name a user in the path: you may only ask about yourself.
function requireSelf(req, res) {
  const user = requireUser(req, res);
  if (!user) return null;
  const asked = req.params.userId;
  if (asked && asked !== user.id) { res.status(403).json({ error: 'forbidden' }); return null; }
  return user;
}

// ---------- Auth / session ----------
// Sign-in is the username and nothing else. Names are unique, so one name is one
// player: a new name opens an account, a known name signs back into it.
const NAME_MIN = 3, NAME_MAX = 18;

function cleanName(raw) {
  return String(raw || '').trim().replace(/\s+/g, ' ').slice(0, NAME_MAX);
}

// Tells the sign-in screen whether this name opens an account or resumes one,
// and whether that account is PIN-protected. Deliberately says nothing else.
api.post('/auth/check', (req, res) => {
  const name = cleanName(req.body?.username);
  if (name.length < NAME_MIN) return res.status(400).json({ error: 'invalid_username' });
  const existing = findByNickname(name);
  res.json({ exists: !!existing, needsPin: hasPin(existing) });
});

// Names are unique and permanent, so bulk registration is how you would ruin the
// namespace. One address gets a generous but finite number of accounts per hour.
const SIGNUP_LIMIT = Number(process.env.SIGNUP_LIMIT_PER_HOUR) || 20;
const signupLimiter = createRateLimiter({ limit: SIGNUP_LIMIT, windowMs: 60 * 60 * 1000 });

api.post('/auth/signup', (req, res) => {
  const name = cleanName(req.body?.username);
  const { avatarId, country, pin } = req.body || {};
  if (name.length < NAME_MIN) return res.status(400).json({ error: 'invalid_username' });
  if (findByNickname(name)) return res.status(409).json({ error: 'username_taken' });

  const key = callerKey(req);
  if (!isLoopback(key)) {
    const { allowed, retryAfterMs } = signupLimiter.take(key);
    if (!allowed) return res.status(429).json({ error: 'too_many_accounts', retryAfterMs });
  }

  const user = createAccount({ nickname: name, avatarId, country: country || 'INT' });
  const recoveryCode = pin ? setPin(user, String(pin)) : null;
  res.json({ user: privateUserDto(user), token: issueToken(user), recoveryCode });
});

api.post('/auth/login', (req, res) => {
  const name = cleanName(req.body?.username);
  const user = findByNickname(name);
  if (!user) return res.status(404).json({ error: 'no_such_username' });
  if (user.banned) return res.status(403).json({ error: 'banned', reason: user.banReason });

  const locked = lockRemaining(user, 'pin');
  if (locked > 0) return res.status(429).json({ error: 'too_many_tries', retryAfterMs: locked });

  if (!checkPin(user, req.body?.pin)) {
    const triesLeft = noteFailure(user, 'pin');
    return res.status(401).json({ error: 'wrong_pin', triesLeft });
  }
  clearFailures(user, 'pin');
  ensureFreshMissions(user);
  res.json({ user: privateUserDto(user), token: issueToken(user) });
});

// Setting a PIN is how a player stops anyone else signing in as them, since a
// username on its own is public. The reply carries the recovery code once and
// never again - the server only keeps its hash.
api.put('/auth/pin', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const { pin, currentPin } = req.body || {};

  const locked = lockRemaining(user, 'pin');
  if (locked > 0) return res.status(429).json({ error: 'too_many_tries', retryAfterMs: locked });
  if (!checkPin(user, currentPin)) {
    const triesLeft = noteFailure(user, 'pin');
    return res.status(401).json({ error: 'wrong_pin', triesLeft });
  }

  const next = pin === null || pin === '' ? null : String(pin);
  if (next && !/^\d{4,8}$/.test(next)) return res.status(400).json({ error: 'invalid_pin' });
  const recoveryCode = setPin(user, next);
  res.json({ ok: true, hasPin: hasPin(user), recoveryCode });
});

// A new recovery code for a player who has lost theirs but still knows the PIN.
api.post('/auth/recovery-code', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  if (!hasPin(user)) return res.status(400).json({ error: 'no_pin' });

  const locked = lockRemaining(user, 'pin');
  if (locked > 0) return res.status(429).json({ error: 'too_many_tries', retryAfterMs: locked });
  if (!checkPin(user, req.body?.currentPin)) {
    const triesLeft = noteFailure(user, 'pin');
    return res.status(401).json({ error: 'wrong_pin', triesLeft });
  }
  clearFailures(user, 'pin');
  res.json({ recoveryCode: issueRecoveryCode(user) });
});

// The way back in when the PIN is forgotten: the code clears the PIN and signs
// you in, and is spent doing so. It has its own lockout, so a PIN locked by
// guessing does not also block the person who actually holds the code.
api.post('/auth/recover', (req, res) => {
  const name = cleanName(req.body?.username);
  const user = findByNickname(name);
  if (!user) return res.status(404).json({ error: 'no_such_username' });
  if (user.banned) return res.status(403).json({ error: 'banned', reason: user.banReason });

  const locked = lockRemaining(user, 'recovery');
  if (locked > 0) return res.status(429).json({ error: 'too_many_tries', retryAfterMs: locked });

  if (!checkRecoveryCode(user, req.body?.code)) {
    noteFailure(user, 'recovery');
    return res.status(401).json({ error: 'wrong_code' });
  }
  setPin(user, null);                       // burns the code and clears both lockouts
  ensureFreshMissions(user);
  res.json({ user: privateUserDto(user), token: issueToken(user) });
});

// Resume a stored session. The token is the credential; nothing is looked up by ID.
api.get('/session', (req, res) => {
  const user = req.authUser;
  if (!user) return res.status(401).json({ error: 'unauthenticated' });
  if (user.banned) return res.status(403).json({ error: 'banned', reason: user.banReason });
  ensureFreshMissions(user);
  res.json({ user: privateUserDto(user) });
});

api.put('/nickname', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const clean = cleanName(req.body?.nickname);
  if (clean.length < NAME_MIN) return res.status(400).json({ error: 'invalid_username' });
  const cooldownMs = 90 * 24 * 60 * 60 * 1000;
  if (Date.now() - user.nicknameChangedAt < cooldownMs) {
    return res.status(429).json({ error: 'cooldown', retryAt: user.nicknameChangedAt + cooldownMs });
  }
  if (findByNickname(clean)) return res.status(409).json({ error: 'nickname_taken' });
  delete db.nicknames[user.nickname.toLowerCase()];
  user.nickname = clean;
  user.nicknameChangedAt = Date.now();
  db.nicknames[clean.toLowerCase()] = user.id;
  persist();
  res.json({ user: privateUserDto(user) });
});

api.put('/settings', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const { lang, vibration, sound, aimSensitivity, country } = req.body || {};
  if (lang && LANGS.includes(lang)) user.settings.lang = lang;
  if (typeof vibration === 'boolean') user.settings.vibration = vibration;
  if (typeof sound === 'boolean') user.settings.sound = sound;
  if (typeof aimSensitivity === 'number') user.settings.aimSensitivity = Math.max(0, Math.min(100, aimSensitivity));
  if (country) user.country = country;
  persist();
  res.json({ user: privateUserDto(user) });
});

// ---------- i18n ----------
api.get('/i18n/:lang', (req, res) => {
  const lang = LANGS.includes(req.params.lang) ? req.params.lang : 'en';
  const raw = fs.readFileSync(path.join(I18N_DIR, `${lang}.json`), 'utf-8');
  res.type('application/json').send(raw);
});
api.get('/i18n', (req, res) => res.json({ languages: LANGS }));

// ---------- Catalogs ----------
api.get('/tables', (req, res) => {
  const user = req.authUser;
  res.json({
    tables: TABLES.map(t => ({ ...tableDto(t, user), onlineCount: onlineOnTable(t.id) })),
  });
});

// The real number: everyone waiting in this table's queue plus every human
// currently playing a match on it. It reads 0 on a quiet table, which is the
// truth - nothing here invents a crowd.
function onlineOnTable(tableId) {
  return queueLength(tableId) + playersOnTable(tableId);
}

api.get('/cues', (req, res) => res.json({ cues: CUES }));
api.get('/avatars', (req, res) => res.json({ avatars: AVATARS }));
api.get('/coin-packages', (req, res) => res.json({ packages: COIN_PACKAGES }));
api.get('/stars', (req, res) => res.json({ stars: STARS }));

// ---------- Profile ----------
api.get('/me/:userId', (req, res) => {
  const user = requireSelf(req, res); if (!user) return;
  res.json({ user: privateUserDto(user) });
});

api.get('/profile/:userId', (req, res) => {
  const user = getUser(req.params.userId);
  if (!user) return res.status(404).json({ error: 'user_not_found' });
  res.json({ user: publicUserDto(user) });
});

api.post('/report', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const { targetId, reason } = req.body || {};
  const target = getUser(targetId);
  if (!target) return res.status(404).json({ error: 'target_not_found' });

  // Reports get people banned, so one person cannot stack them.
  const status = reportStatus(user, targetId);
  if (status === 'self') return res.status(400).json({ error: 'cannot_report_self' });
  if (status === 'duplicate') return res.json({ ok: true, alreadyReported: true });
  if (status === 'rate_limited') return res.status(429).json({ error: 'too_many_reports' });

  addReport(targetId, user.id, reason || 'unspecified');
  res.json({ ok: true });
});

// ---------- Shop ----------
api.post('/shop/buy-cue', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const cue = cueById(Number(req.body?.cueId));
  if (!cue) return res.status(404).json({ error: 'cue_not_found' });
  if (user.ownedCues.includes(cue.id)) return res.status(409).json({ error: 'already_owned' });
  if (levelFromXp(user.xp).level < cue.unlockLvl) return res.status(403).json({ error: 'level_locked' });
  if (cue.currency === 'coins' && user.coins < cue.price) return res.status(402).json({ error: 'insufficient_coins' });
  if (cue.currency === 'coins') user.coins -= cue.price;
  user.ownedCues.push(cue.id);
  persist();
  res.json({ user: privateUserDto(user) });
});

api.post('/shop/equip-cue', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const cueId = Number(req.body?.cueId);
  if (!user.ownedCues.includes(cueId)) return res.status(403).json({ error: 'not_owned' });
  user.equippedCueId = cueId;
  persist();
  res.json({ user: privateUserDto(user) });
});

api.post('/shop/buy-avatar', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const avatar = avatarById(Number(req.body?.avatarId));
  if (!avatar) return res.status(404).json({ error: 'avatar_not_found' });
  if (user.ownedAvatars.includes(avatar.id)) return res.status(409).json({ error: 'already_owned' });
  if (levelFromXp(user.xp).level < avatar.unlockLvl) return res.status(403).json({ error: 'level_locked' });
  if (avatar.currency === 'coins' && user.coins < avatar.price) return res.status(402).json({ error: 'insufficient_coins' });
  if (avatar.currency === 'coins') user.coins -= avatar.price;
  user.ownedAvatars.push(avatar.id);
  persist();
  res.json({ user: privateUserDto(user) });
});

api.post('/shop/equip-avatar', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const avatarId = Number(req.body?.avatarId);
  if (!user.ownedAvatars.includes(avatarId)) return res.status(403).json({ error: 'not_owned' });
  user.avatarId = avatarId;
  persist();
  res.json({ user: privateUserDto(user) });
});

// Coin purchase: DEMO checkout only - no real payment processor is wired up. Instantly
// grants the package's coins server-side (server is the single source of truth for coins).
api.post('/shop/buy-coins', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const pkg = getPackage(req.body?.packageId);
  if (!pkg) return res.status(404).json({ error: 'package_not_found' });
  let coins = pkg.coins;
  let bonus = false;
  if (!user.firstPurchaseDone) {
    coins *= 2;
    user.firstPurchaseDone = true;
    bonus = true;
  }
  user.coins += coins;
  persist();
  res.json({ user: privateUserDto(user), coinsGranted: coins, firstPurchaseBonus: bonus });
});

// ---------- Missions / Daily Box / Loss boxes ----------
api.get('/missions/:userId', (req, res) => {
  const user = requireSelf(req, res); if (!user) return;
  ensureFreshMissions(user);
  res.json({ missions: user.missions });
});

api.post('/missions/claim', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  ensureFreshMissions(user);
  const m = user.missions.list.find(x => x.key === req.body?.missionKey);
  if (!m) return res.status(404).json({ error: 'mission_not_found' });
  if (m.claimed) return res.status(409).json({ error: 'already_claimed' });
  if (m.progress < m.target) return res.status(400).json({ error: 'not_complete' });
  m.claimed = true;
  user.coins += m.reward;
  persist();
  res.json({ user: privateUserDto(user), reward: m.reward });
});

api.post('/dailybox/claim', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  if (Date.now() < user.dailyBoxAvailableAt) {
    return res.status(429).json({ error: 'not_ready', availableAt: user.dailyBoxAvailableAt });
  }
  const reward = 300 + Math.floor(Math.random() * 1200);
  user.coins += reward;
  user.dailyBoxAvailableAt = Date.now() + 24 * 60 * 60 * 1000;
  persist();
  res.json({ user: privateUserDto(user), reward });
});

api.post('/lossboxes/claim', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const box = user.lossBoxes.find(b => b.id === req.body?.boxId);
  if (!box) return res.status(404).json({ error: 'box_not_found' });
  if (box.opened) return res.status(409).json({ error: 'already_opened' });
  if (Date.now() < box.availableAt) return res.status(429).json({ error: 'not_ready', availableAt: box.availableAt });
  box.opened = true;
  user.coins += box.coins;
  persist();
  res.json({ user: privateUserDto(user), reward: box.coins });
});

// ---------- Leaderboard ----------
api.get('/leaderboard/global', (req, res) => {
  const user = req.authUser;
  const starId = Number(req.query.starId) || user?.starId || 1;
  const rows = allUsers()
    .filter(u => u.starId === starId && !u.banned)
    .sort((a, b) => b.weeklyCoins - a.weeklyCoins)
    .slice(0, 50)
    .map((u, i) => ({ rank: i + 1, userId: u.id, nickname: u.nickname, avatarId: u.avatarId, weeklyCoins: u.weeklyCoins }));
  res.json({ starId, stars: STARS, prizes: weeklyPrizes(starId), rows, resetsAt: nextResetAt() });
});

api.get('/leaderboard/local', (req, res) => {
  const user = req.authUser;
  const country = req.query.country || user?.country || 'INT';
  const rows = allUsers()
    .filter(u => u.country === country && !u.banned)
    .sort((a, b) => b.careerCoinsWon - a.careerCoinsWon)
    .slice(0, 50)
    .map((u, i) => ({ rank: i + 1, userId: u.id, nickname: u.nickname, avatarId: u.avatarId, coins: u.careerCoinsWon }));
  res.json({ country, rows });
});

api.get('/leaderboard/friends', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const rows = [user, ...user.friends.map(getUser).filter(Boolean)]
    .sort((a, b) => b.careerCoinsWon - a.careerCoinsWon)
    .map((u, i) => ({ rank: i + 1, userId: u.id, nickname: u.nickname, avatarId: u.avatarId, coins: u.careerCoinsWon, isMe: u.id === user.id }));
  res.json({ rows });
});

// ---------- Friends ----------
api.get('/friends/:userId', (req, res) => {
  const user = requireSelf(req, res); if (!user) return;
  const list = user.friends.map(getUser).filter(Boolean).map(u => ({
    id: u.id, nickname: u.nickname, avatarId: u.avatarId,
    online: Date.now() - u.lastSeenAt < 60_000,
    level: levelFromXp(u.xp).level,
  }));
  res.json({ friends: list });
});

api.post('/friends/add', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const { friendId } = req.body || {};
  const friend = getUser(friendId);
  if (!friend) return res.status(404).json({ error: 'user_not_found' });
  if (friend.id === user.id) return res.status(400).json({ error: 'cannot_add_self' });
  if (!user.friends.includes(friend.id)) user.friends.push(friend.id);
  if (!friend.friends.includes(user.id)) friend.friends.push(user.id);
  persist();
  res.json({ ok: true });
});

api.get('/users/search', (req, res) => {
  const id = req.query.id;
  const user = getUser(id);
  if (!user) return res.status(404).json({ error: 'user_not_found' });
  res.json({ user: publicUserDto(user) });
});

// ---------- Invite ----------
api.post('/invite/create', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  const code = createInvite(user.id);
  res.json({ code, url: `https://8ballmasters.com/invite/${code}` });
});

api.get('/invite/:code', (req, res) => {
  const invite = db.invites[req.params.code];
  if (!invite) return res.status(404).json({ error: 'invite_not_found' });
  const from = getUser(invite.fromUserId);
  res.json({ fromUserId: invite.fromUserId, fromNickname: from?.nickname || 'Player' });
});

// ---------- Notifications ----------
api.get('/notifications/:userId', (req, res) => {
  const user = requireSelf(req, res); if (!user) return;
  res.json({ notifications: user.notifications || [] });
});

api.post('/notifications/read', (req, res) => {
  const user = requireUser(req, res); if (!user) return;
  user.notifications = [];
  persist();
  res.json({ ok: true });
});
