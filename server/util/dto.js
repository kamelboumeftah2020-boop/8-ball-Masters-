import { levelFromXp, formatCoins } from './econ.js';
import { titleForWins } from '../data/titles.js';
import { getStar } from '../data/stars.js';

export function publicUserDto(user) {
  if (!user) return null;
  const lvl = levelFromXp(user.xp);
  const star = getStar(user.starId);
  return {
    id: user.id,
    nickname: user.nickname,
    avatarId: user.avatarId,
    level: lvl.level,
    xpIntoLevel: lvl.xpIntoLevel,
    xpForNextLevel: lvl.xpForNextLevel,
    starId: star.id,
    starKey: star.key,
    starColor: star.color,
    title: titleForWins(user.totalWins),
    totalWins: user.totalWins,
    totalLosses: user.totalLosses,
    totalPlayed: user.totalPlayed,
    winRate: user.totalPlayed ? Math.round((user.totalWins / user.totalPlayed) * 100) : 0,
    careerCoinsWon: user.careerCoinsWon,
    careerCoinsWonFmt: formatCoins(user.careerCoinsWon),
    winStreak: user.winStreak,
    country: user.country,
  };
}

export function privateUserDto(user) {
  if (!user) return null;
  const base = publicUserDto(user);
  return {
    ...base,
    coins: user.coins,
    equippedCueId: user.equippedCueId,
    ownedCues: user.ownedCues,
    ownedAvatars: user.ownedAvatars,
    settings: user.settings,
    dailyBoxAvailableAt: user.dailyBoxAvailableAt,
    missions: user.missions,
    lossBoxes: user.lossBoxes,
    friends: user.friends,
    nicknameChangedAt: user.nicknameChangedAt,
    firstPurchaseDone: user.firstPurchaseDone,
    banned: user.banned,
    banReason: user.banReason,
    notifications: user.notifications,
    weeklyCoins: user.weeklyCoins,
    isAdmin: !!user.isAdmin,
  };
}

export function tableDto(table, user) {
  const lvl = user ? levelFromXp(user.xp).level : 1;
  return {
    ...table,
    locked: lvl < table.unlockLvl,
  };
}
