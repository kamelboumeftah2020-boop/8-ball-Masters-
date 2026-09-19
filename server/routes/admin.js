import { Router } from 'express';
import { db, getUser, allUsers, persist } from '../store.js';
import { levelFromXp } from '../util/econ.js';

const ADMIN_TOKEN = process.env.ADMIN_TOKEN || 'admin123';

export const admin = Router();

function checkAuth(req, res, next) {
  const token = req.headers['x-admin-token'] || req.query.token;
  if (token !== ADMIN_TOKEN) return res.status(401).json({ error: 'unauthorized' });
  next();
}

admin.use(checkAuth);

admin.get('/players', (req, res) => {
  const now = Date.now();
  const q = (req.query.q || '').toLowerCase();
  let list = allUsers().map(u => ({
    id: u.id,
    nickname: u.nickname,
    level: levelFromXp(u.xp).level,
    coins: u.coins,
    totalWins: u.totalWins,
    totalLosses: u.totalLosses,
    careerCoinsWon: u.careerCoinsWon,
    active: now - u.lastSeenAt < 5 * 60 * 1000,
    banned: u.banned,
    banReason: u.banReason,
    incomingReports: u.incomingReports,
    tempBanCount: u.tempBanCount,
    createdAt: u.createdAt,
    lastSeenAt: u.lastSeenAt,
    country: u.country,
  }));
  if (q) list = list.filter(u => u.id.includes(q) || u.nickname.toLowerCase().includes(q));
  list.sort((a, b) => b.lastSeenAt - a.lastSeenAt);
  res.json({ players: list });
});

admin.get('/reports', (req, res) => {
  const rows = db.reports.slice(-200).reverse().map(r => ({
    ...r,
    targetNickname: getUser(r.targetId)?.nickname,
    reporterNickname: getUser(r.reporterId)?.nickname,
  }));
  res.json({ reports: rows });
});

admin.post('/ban', (req, res) => {
  const user = getUser(req.body?.userId);
  if (!user) return res.status(404).json({ error: 'user_not_found' });
  user.banned = true;
  user.banReason = req.body?.reason || 'Account banned for violating the rules.';
  persist();
  res.json({ ok: true });
});

admin.post('/unban', (req, res) => {
  const user = getUser(req.body?.userId);
  if (!user) return res.status(404).json({ error: 'user_not_found' });
  user.banned = false;
  user.banReason = null;
  user.tempBanUntil = null;
  persist();
  res.json({ ok: true });
});

admin.get('/analytics', (req, res) => {
  const users = allUsers();
  const now = Date.now();
  const active24h = users.filter(u => now - u.lastSeenAt < 24 * 60 * 60 * 1000).length;
  const banned = users.filter(u => u.banned).length;
  const totalMatches = db.matches.length;
  const totalCoinsInEconomy = users.reduce((s, u) => s + u.coins, 0);
  const winRateAvg = users.length
    ? Math.round(users.reduce((s, u) => s + (u.totalPlayed ? u.totalWins / u.totalPlayed : 0), 0) / users.length * 100)
    : 0;
  res.json({
    totalPlayers: users.length,
    active24h,
    banned,
    totalMatches,
    totalCoinsInEconomy,
    winRateAvg,
    reportsCount: db.reports.length,
  });
});
