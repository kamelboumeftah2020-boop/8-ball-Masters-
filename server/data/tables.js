// 10 tables, each with a fully separate matchmaking pool (100% isolated queues).
export const TABLES = [
  { id: 1, key: 'garage', name: 'Garage', entry: 200, unlockLvl: 1, style: 'old-wood', xp: 50,
    colors: { felt: '#3a2e22', rail: '#5c4530', accent: '#c9a24b' } },
  { id: 2, key: 'cairo', name: 'Cairo', entry: 1000, unlockLvl: 2, style: 'street-coffee', xp: 150,
    colors: { felt: '#5c3a1e', rail: '#8a5a2a', accent: '#e0a83d' } },
  { id: 3, key: 'paris', name: 'Paris', entry: 2000, unlockLvl: 4, style: 'french-neon', xp: 300,
    colors: { felt: '#1a1033', rail: '#2d1b57', accent: '#ff2fd0' } },
  { id: 4, key: 'dubai', name: 'Dubai', entry: 10000, unlockLvl: 6, style: 'pure-gold', xp: 800,
    colors: { felt: '#1b1204', rail: '#3a2705', accent: '#ffd447' } },
  { id: 5, key: 'berlin', name: 'Berlin', entry: 25000, unlockLvl: 9, style: 'dark-techno', xp: 1500,
    colors: { felt: '#0a0a0f', rail: '#17171f', accent: '#00f0ff' } },
  { id: 6, key: 'tokyo', name: 'Tokyo', entry: 75000, unlockLvl: 12, style: 'japanese-neon', xp: 3500,
    colors: { felt: '#170a1f', rail: '#2a0f3a', accent: '#ff3d6e' } },
  { id: 7, key: 'vegas', name: 'Vegas', entry: 200000, unlockLvl: 16, style: 'casino', xp: 8000,
    colors: { felt: '#20060a', rail: '#3d0d14', accent: '#ff2b2b' } },
  { id: 8, key: 'london', name: 'London', entry: 500000, unlockLvl: 20, style: 'royal', xp: 15000,
    colors: { felt: '#0c1330', rail: '#1a2456', accent: '#d4af37' } },
  { id: 9, key: 'newyork', name: 'New York', entry: 1500000, unlockLvl: 25, style: 'skyscrapers', xp: 30000,
    colors: { felt: '#0a0a0a', rail: '#1c1c1c', accent: '#55c8ff' } },
  { id: 10, key: 'masters-throne', name: 'Masters Throne', entry: 5000000, unlockLvl: 30, style: 'flying-throne', xp: 60000,
    colors: { felt: '#120a00', rail: '#2b1800', accent: '#ffdd66' } },
];

export function getTable(id) {
  return TABLES.find(t => t.id === Number(id));
}
