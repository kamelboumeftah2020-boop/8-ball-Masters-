// Daily mission templates. A fresh set of 3 is assigned to each player every 24h server-side.
export const MISSION_POOL = [
  { key: 'pot5', descKey: 'missionPot5', target: 5, metric: 'ballsPotted', reward: 500 },
  { key: 'win3paris', descKey: 'missionWin3Paris', target: 3, metric: 'winsOnTable:paris', reward: 1200 },
  { key: 'win1', descKey: 'missionWin1', target: 1, metric: 'wins', reward: 300 },
  { key: 'play3', descKey: 'missionPlay3', target: 3, metric: 'matchesPlayed', reward: 400 },
  { key: 'pot20', descKey: 'missionPot20', target: 20, metric: 'ballsPotted', reward: 1500 },
  { key: 'win5', descKey: 'missionWin5', target: 5, metric: 'wins', reward: 2000 },
  { key: 'noScratch3', descKey: 'missionNoScratch3', target: 3, metric: 'cleanWins', reward: 1000 },
];

export function pickDailyMissions(seed) {
  const pool = [...MISSION_POOL];
  const out = [];
  let s = seed;
  const rand = () => {
    s = (s * 9301 + 49297) % 233280;
    return s / 233280;
  };
  for (let i = 0; i < 3 && pool.length; i++) {
    const idx = Math.floor(rand() * pool.length);
    out.push(pool.splice(idx, 1)[0]);
  }
  return out.map(m => ({ ...m, progress: 0, claimed: false }));
}
