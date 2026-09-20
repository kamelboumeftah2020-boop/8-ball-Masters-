import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { genNumericId } from './util/econ.js';
import { CUES } from './data/cues.js';
import { AVATARS } from './data/avatars.js';
import { pickDailyMissions } from './data/missions.js';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DB_PATH = path.join(__dirname, 'data', 'db.json');

const EMPTY = { users: {}, nicknames: {}, invites: {}, reports: [], matches: [], lastWeeklyReset: 0 };

function load() {
  try {
    const raw = fs.readFileSync(DB_PATH, 'utf-8');
    return JSON.parse(raw);
  } catch {
    return structuredClone(EMPTY);
  }
}

export const db = load();

let saveTimer = null;
export function persist() {
  if (saveTimer) return;
  saveTimer = setTimeout(writeNow, 250);
}

// Write to a sibling file and rename over the original: a crash mid-write leaves
// the previous database intact instead of a truncated one that loses every account.
function writeNow() {
  saveTimer = null;
  const tmp = `${DB_PATH}.${process.pid}.tmp`;
  try {
    fs.writeFileSync(tmp, JSON.stringify(db, null, 2));
    fs.renameSync(tmp, DB_PATH);
  } catch (err) {
    console.error('failed to persist database:', err.message);
    try { fs.unlinkSync(tmp); } catch {}
  }
}

// Don't lose the last few hundred milliseconds of play on a clean shutdown.
for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => {
    if (saveTimer) { clearTimeout(saveTimer); writeNow(); }
    process.exit(0);
  });
}

export function getUser(id) {
  return db.users[id];
}

/* ----------------------------------------------------------------- sessions
   A player's 8-digit ID is public - it is printed on their profile with a copy
   button - so it can never be what proves who you are. Every account action is
   authorised by a secret session token instead. Only the token's hash is stored,
   so a leaked database still cannot be used to act as anyone.
*/
const tokenIndex = new Map(); // sha256(token) -> userId

function hashToken(token) {
  return crypto.createHash('sha256').update(String(token)).digest('hex');
}

// rebuild the lookup index for accounts loaded from disk
for (const user of Object.values(db.users)) {
  if (user.tokenHash) tokenIndex.set(user.tokenHash, user.id);
}

export function issueToken(user) {
  if (user.tokenHash) tokenIndex.delete(user.tokenHash);
  const token = crypto.randomBytes(32).toString('base64url');
  user.tokenHash = hashToken(token);
  tokenIndex.set(user.tokenHash, user.id);
  persist();
  return token;
}

// A PIN is optional account protection. Like the session token it is only ever
// stored as a salted hash. Setting one also mints a recovery code, since a PIN
// nobody can reset is a way to lose an account for good.
export function setPin(user, pin) {
  if (!pin) {
    delete user.pinHash; delete user.pinSalt;
    delete user.recoveryHash; delete user.recoverySalt;   // nothing left to recover
    clearFailures(user, 'pin'); clearFailures(user, 'recovery');
    persist();
    return null;
  }
  user.pinSalt = crypto.randomBytes(12).toString('hex');
  user.pinHash = hashSecret(pin, user.pinSalt);
  clearFailures(user, 'pin');
  return issueRecoveryCode(user);
}

export function hasPin(user) { return !!user?.pinHash; }

export function checkPin(user, pin) {
  if (!user?.pinHash) return true;           // no PIN set: the name is enough
  if (!pin) return false;
  return sameSecret(hashSecret(String(pin), user.pinSalt), user.pinHash);
}

/* --------------------------------------------------------- recovery codes
   Twelve characters from a 32-symbol alphabet with no look-alikes: 2^60
   combinations, so it cannot be guessed, and it can be read off paper without
   confusing O for 0. Only its hash is kept, and using it burns it.
*/
const CODE_ALPHABET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

function normalizeCode(raw) {
  return String(raw || '').toUpperCase().replace(/[^A-Z0-9]/g, '');
}

export function issueRecoveryCode(user) {
  const bytes = crypto.randomBytes(12);
  let plain = '';
  for (let i = 0; i < 12; i++) plain += CODE_ALPHABET[bytes[i] % 32];   // 256 % 32 === 0, so no bias
  user.recoverySalt = crypto.randomBytes(12).toString('hex');
  user.recoveryHash = hashSecret(plain, user.recoverySalt);
  persist();
  return `${plain.slice(0, 4)}-${plain.slice(4, 8)}-${plain.slice(8, 12)}`;
}

export function checkRecoveryCode(user, code) {
  if (!user?.recoveryHash) return false;
  const norm = normalizeCode(code);
  if (norm.length !== 12) return false;
  return sameSecret(hashSecret(norm, user.recoverySalt), user.recoveryHash);
}

function hashSecret(secret, salt) {
  return crypto.createHash('sha256').update(`${salt}:${secret}`).digest('hex');
}

function sameSecret(given, known) {
  const a = Buffer.from(given);
  const b = Buffer.from(String(known));
  return a.length === b.length && crypto.timingSafeEqual(a, b);
}

/* ------------------------------------------------------- guessing lockout
   A four-digit PIN is only 10,000 guesses, so unlimited tries would make it
   decoration. After a few misses each further miss locks the account for
   longer, and the counters live on the account so a restart does not wipe them.
*/
const LOCK_STEPS = [30e3, 120e3, 600e3, 3600e3];

export function lockRemaining(user, kind) {
  const until = user?.gates?.[kind]?.until || 0;
  return Math.max(0, until - Date.now());
}

export function noteFailure(user, kind, freeTries = 5) {
  user.gates ||= {};
  const gate = (user.gates[kind] ||= { fails: 0, until: 0 });
  gate.fails += 1;
  const over = gate.fails - freeTries;
  if (over >= 0) gate.until = Date.now() + LOCK_STEPS[Math.min(over, LOCK_STEPS.length - 1)];
  persist();
  return Math.max(0, freeTries - gate.fails);
}

export function clearFailures(user, kind) {
  if (!user?.gates?.[kind]) return;
  delete user.gates[kind];
  persist();
}

export function userByToken(token) {
  if (!token) return null;
  const id = tokenIndex.get(hashToken(token));
  return id ? db.users[id] : null;
}

export function findByNickname(nick) {
  const id = db.nicknames[nick.toLowerCase()];
  return id ? db.users[id] : null;
}

export function createAccount({ nickname, avatarId, country = 'INT' }) {
  let id;
  do { id = genNumericId(8); } while (db.users[id]);

  const user = {
    id,
    nickname,
    nicknameChangedAt: Date.now(),
    avatarId: avatarId ?? 0,
    equippedCueId: 1,
    ownedCues: [1],
    ownedAvatars: [0, 1, 2, 3, 4, 5],
    coins: 5000,
    xp: 0,
    totalWins: 0,
    totalLosses: 0,
    totalPlayed: 0,
    careerCoinsWon: 0,
    winStreak: 0,
    starId: 1,
    weeklyCoins: 0,
    country,
    friends: [],
    incomingReports: 0,
    settings: { lang: 'en', vibration: true, sound: true, aimSensitivity: 50 },
    dailyBoxAvailableAt: Date.now(),
    missions: { day: todayKey(), list: pickDailyMissions(hashSeed(id)) },
    lossBoxes: [],
    firstPurchaseDone: false,
    notifications: [],
    createdAt: Date.now(),
    lastSeenAt: Date.now(),
    banned: false,
    banReason: null,
    tempBanUntil: null,
    tempBanCount: 0,
    isAdmin: false,
  };
  db.users[id] = user;
  db.nicknames[nickname.toLowerCase()] = id;
  persist();
  return user;
}

function todayKey() {
  return new Date().toISOString().slice(0, 10);
}

function hashSeed(str) {
  let h = 0;
  for (let i = 0; i < str.length; i++) h = (h * 31 + str.charCodeAt(i)) >>> 0;
  return h || 1;
}

export function ensureFreshMissions(user) {
  const key = todayKey();
  if (user.missions.day !== key) {
    user.missions = { day: key, list: pickDailyMissions(hashSeed(user.id + key)) };
    persist();
  }
}

export function refreshMissionProgress(user, metric, amount = 1) {
  ensureFreshMissions(user);
  let changed = false;
  for (const m of user.missions.list) {
    if (m.claimed) continue;
    if (m.metric === metric) {
      m.progress = Math.min(m.target, m.progress + amount);
      changed = true;
    }
  }
  if (changed) persist();
}

export function createInvite(fromUserId) {
  let code;
  do { code = Math.random().toString(36).slice(2, 8).toUpperCase(); } while (db.invites[code]);
  db.invites[code] = { fromUserId, createdAt: Date.now() };
  persist();
  return code;
}

/* ------------------------------------------------------------------ reports
   Five reports temp-ban an account and three temp-bans ban it for good, so the
   report button is a weapon unless it takes five *different* people to pull it.
   Only the first report a player files against a target counts, and each player
   gets a limited number of reports a day, so one person - or one person with a
   handful of throwaway accounts - cannot ban whoever they like.
*/
const REPORTS_PER_DAY = 5;

export function reportStatus(reporter, targetId) {
  if (reporter.id === targetId) return 'self';
  const today = todayKey();
  if (reporter.reportDay !== today) return 'ok';
  if ((reporter.reportedToday || []).includes(targetId)) return 'duplicate';
  if ((reporter.reportedToday || []).length >= REPORTS_PER_DAY) return 'rate_limited';
  return 'ok';
}

export function addReport(targetId, reporterId, reason) {
  const reporter = getUser(reporterId);
  const target = getUser(targetId);

  if (reporter) {
    const today = todayKey();
    if (reporter.reportDay !== today) { reporter.reportDay = today; reporter.reportedToday = []; }
    reporter.reportedToday.push(targetId);
  }

  db.reports.push({ targetId, reporterId, reason, at: Date.now() });
  if (target) {
    // Count the people who reported, not the number of times the button was hit.
    target.reporters = target.reporters || [];
    if (!target.reporters.includes(reporterId)) target.reporters.push(reporterId);
    target.incomingReports = target.reporters.length;
    if (target.incomingReports >= 5 && !target.banned) {
      target.tempBanCount = (target.tempBanCount || 0) + 1;
      if (target.tempBanCount >= 3) {
        target.banned = true;
        target.banReason = 'Repeated rule violations (3 temporary bans)';
      } else {
        target.tempBanUntil = Date.now() + 30 * 24 * 60 * 60 * 1000;
        target.banned = true;
        target.banReason = 'Account banned for violating the rules (temporary, 30 days)';
      }
      target.incomingReports = 0;
      target.reporters = [];
    }
  }
  persist();
}

export function logMatch(entry) {
  db.matches.push(entry);
  if (db.matches.length > 5000) db.matches.shift();
  persist();
}

export function allUsers() {
  return Object.values(db.users);
}

export function cueById(id) { return CUES.find(c => c.id === id); }
export function avatarById(id) { return AVATARS.find(a => a.id === id); }
