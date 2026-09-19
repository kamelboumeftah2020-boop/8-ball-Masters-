// 10 tables, each with a fully separate matchmaking pool (100% isolated queues).
export const TABLES = [
  // `felt` is real billiard-cloth colour, `rail` the wood/frame tone, `accent` the trim.
  { id: 1, key: 'garage', name: 'Garage', entry: 200, unlockLvl: 1, style: 'old-wood', xp: 50,
    colors: { felt: '#2f4a35', rail: '#5c4530', accent: '#c9a24b' } },
  { id: 2, key: 'cairo', name: 'Cairo', entry: 1000, unlockLvl: 2, style: 'street-coffee', xp: 150,
    colors: { felt: '#3d5228', rail: '#7a5326', accent: '#e0a83d' } },
  { id: 3, key: 'paris', name: 'Paris', entry: 2000, unlockLvl: 4, style: 'french-neon', xp: 300,
    colors: { felt: '#2b2a63', rail: '#2d1b57', accent: '#ff2fd0' } },
  { id: 4, key: 'dubai', name: 'Dubai', entry: 10000, unlockLvl: 6, style: 'pure-gold', xp: 800,
    colors: { felt: '#1d5c40', rail: '#3a2705', accent: '#ffd447' } },
  { id: 5, key: 'berlin', name: 'Berlin', entry: 25000, unlockLvl: 9, style: 'dark-techno', xp: 1500,
    colors: { felt: '#1d2b3a', rail: '#17171f', accent: '#00f0ff' } },
  { id: 6, key: 'tokyo', name: 'Tokyo', entry: 75000, unlockLvl: 12, style: 'japanese-neon', xp: 3500,
    colors: { felt: '#42184a', rail: '#2a0f3a', accent: '#ff3d6e' } },
  { id: 7, key: 'vegas', name: 'Vegas', entry: 200000, unlockLvl: 16, style: 'casino', xp: 8000,
    colors: { felt: '#5e1620', rail: '#3d0d14', accent: '#ff2b2b' } },
  { id: 8, key: 'london', name: 'London', entry: 500000, unlockLvl: 20, style: 'royal', xp: 15000,
    colors: { felt: '#1b2f66', rail: '#1a2456', accent: '#d4af37' } },
  { id: 9, key: 'newyork', name: 'New York', entry: 1500000, unlockLvl: 25, style: 'skyscrapers', xp: 30000,
    colors: { felt: '#14403d', rail: '#1c1c1c', accent: '#55c8ff' } },
  { id: 10, key: 'masters-throne', name: 'Masters Throne', entry: 5000000, unlockLvl: 30, style: 'flying-throne', xp: 60000,
    colors: { felt: '#17301c', rail: '#2b1800', accent: '#ffdd66' } },
];

export function getTable(id) {
  return TABLES.find(t => t.id === Number(id));
}
