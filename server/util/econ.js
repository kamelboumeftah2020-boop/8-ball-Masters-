// Leveling curve: level 1..10000. xpForLevel(n) = XP required to go from level n -> n+1.
// Growth is quadratic-ish so late levels take meaningfully longer (matches a 10,000-level range).
export function xpForLevel(level) {
  return Math.round(100 * level * (1 + level * 0.02));
}

// Closed-form-ish cumulative XP needed to REACH `level` (i.e. sum of xpForLevel(1..level-1)).
export function cumulativeXpForLevel(level) {
  const n = level - 1;
  if (n <= 0) return 0;
  // sum(100*l + 2*l^2) for l=1..n
  const sumL = (n * (n + 1)) / 2;
  const sumL2 = (n * (n + 1) * (2 * n + 1)) / 6;
  return Math.round(100 * sumL + 2 * sumL2);
}

const MAX_LEVEL = 10000;

export function levelFromXp(totalXp) {
  let lo = 1, hi = MAX_LEVEL;
  while (lo < hi) {
    const mid = Math.ceil((lo + hi) / 2);
    if (cumulativeXpForLevel(mid) <= totalXp) lo = mid;
    else hi = mid - 1;
  }
  const level = Math.min(lo, MAX_LEVEL);
  const into = totalXp - cumulativeXpForLevel(level);
  const need = xpForLevel(level);
  return { level, xpIntoLevel: into, xpForNextLevel: need };
}

export function formatCoins(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(n % 1_000_000 === 0 ? 0 : 1) + 'M';
  if (n >= 1_000) return (n / 1_000).toFixed(n % 1_000 === 0 ? 0 : 1) + 'K';
  return String(n);
}

export function genNumericId(len = 8) {
  let s = '';
  for (let i = 0; i < len; i++) s += Math.floor(Math.random() * 10);
  return s;
}
