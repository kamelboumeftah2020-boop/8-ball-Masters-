// Career titles based on TOTAL wins across all matches (not just current table).
export const TITLES = [
  { minWins: 0, key: 'beginner' },
  { minWins: 25, key: 'amateur' },
  { minWins: 65, key: 'pro' },
  { minWins: 150, key: 'elite' },
  { minWins: 300, key: 'champion' },
  { minWins: 500, key: 'legend' },
  { minWins: 750, key: 'tableKing' },
  { minWins: 1000, key: 'godfather' },
];

export function titleForWins(totalWins) {
  let cur = TITLES[0];
  for (const t of TITLES) {
    if (totalWins >= t.minWins) cur = t;
  }
  return cur.key;
}
