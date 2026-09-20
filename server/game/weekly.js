import { db, allUsers, persist } from '../store.js';
import { STARS, weeklyPrizes } from '../data/stars.js';

// The leaderboard season closes every Friday at 00:00 UTC, as specified. The screen
// has always shown this countdown and the prize amounts; this is the job that
// actually pays them out and starts the next week.

export function nextResetAt(from = Date.now()) {
  const d = new Date(from);
  const next = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate(), 0, 0, 0, 0));
  // walk forward to the next Friday that is strictly in the future
  while (next.getUTCDay() !== 5 || next.getTime() <= from) {
    next.setUTCDate(next.getUTCDate() + 1);
  }
  return next.getTime();
}

// The most recent Friday 00:00 UTC at or before `from`.
function lastResetBoundary(from = Date.now()) {
  const d = new Date(from);
  const boundary = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate(), 0, 0, 0, 0));
  while (boundary.getUTCDay() !== 5) boundary.setUTCDate(boundary.getUTCDate() - 1);
  return boundary.getTime();
}

// Top three of every star tier are paid, scaled by the tier. Everyone else gets
// nothing, per spec - then every weekly total goes back to zero.
export function payoutAndReset(now = Date.now()) {
  const results = [];

  for (const star of STARS) {
    const prizes = weeklyPrizes(star.id);
    const podium = allUsers()
      .filter(u => !u.banned && u.starId === star.id && u.weeklyCoins > 0)
      .sort((a, b) => b.weeklyCoins - a.weeklyCoins)
      .slice(0, 3);

    podium.forEach((user, i) => {
      const amount = [prizes.top1, prizes.top2, prizes.top3][i];
      user.coins += amount;
      user.careerCoinsWon += amount;
      user.notifications = user.notifications || [];
      user.notifications.push({
        id: `weekly-${now}-${user.id}`,
        text: `You finished #${i + 1} in ${star.key} this week — ${amount.toLocaleString()} coins awarded!`,
        at: now,
      });
      results.push({ starId: star.id, rank: i + 1, userId: user.id, amount });
    });
  }

  for (const user of allUsers()) user.weeklyCoins = 0;

  db.lastWeeklyReset = now;
  persist();
  return results;
}

// Called on a timer. Pays out once per season, even if the server was down when
// the boundary passed.
export function runWeeklyResetIfDue(now = Date.now()) {
  const boundary = lastResetBoundary(now);
  if ((db.lastWeeklyReset || 0) >= boundary) return null;
  // a fresh database has never had a season, so just mark the current one as started
  if (!db.lastWeeklyReset) {
    db.lastWeeklyReset = boundary;
    persist();
    return null;
  }
  return payoutAndReset(now);
}
