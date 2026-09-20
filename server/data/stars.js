// 10-star global league system. Weekly coin totals decide promotion; resets every Friday 00:00 UTC.
// Star 10 (Galaxy Godfather) is the top league: no further promotion, players just fight for rank.
export const STARS = [
  { id: 1, key: 'silver', color: '#c7cdd6', promoteAt: 5000, prizeMult: 0.10 },
  { id: 2, key: 'bronze', color: '#c9793c', promoteAt: 10000, prizeMult: 0.18 },
  { id: 3, key: 'gold', color: '#ffd447', promoteAt: 20000, prizeMult: 0.28 },
  { id: 4, key: 'platinum', color: '#bfe9ff', promoteAt: 40000, prizeMult: 0.40 },
  { id: 5, key: 'ruby', color: '#ff3d5a', promoteAt: 80000, prizeMult: 0.52 },
  { id: 6, key: 'emerald', color: '#33e08a', promoteAt: 150000, prizeMult: 0.64 },
  { id: 7, key: 'diamond', color: '#4fd8ff', promoteAt: 300000, prizeMult: 0.76 },
  { id: 8, key: 'legendary', color: '#b060ff', promoteAt: 600000, prizeMult: 0.88 },
  { id: 9, key: 'eternal', color: '#ff9f45', promoteAt: 1200000, prizeMult: 0.95 },
  { id: 10, key: 'galaxyGodfather', color: '#1a1a1a', accent: '#ffd447', promoteAt: null, prizeMult: 1.0 },
];

// Base weekly prizes at star 10 (as spec'd); other stars scale down by prizeMult.
const BASE_PRIZES = { top1: 1000000, top2: 500000, top3: 250000 };

export function weeklyPrizes(starId) {
  const star = STARS.find(s => s.id === starId) || STARS[0];
  return {
    top1: Math.round(BASE_PRIZES.top1 * star.prizeMult),
    top2: Math.round(BASE_PRIZES.top2 * star.prizeMult),
    top3: Math.round(BASE_PRIZES.top3 * star.prizeMult),
  };
}

export function getStar(id) {
  return STARS.find(s => s.id === id) || STARS[0];
}
