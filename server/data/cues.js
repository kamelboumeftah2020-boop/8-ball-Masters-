// 30+ cues across categories: wood, national flags, dragon, galaxy, laser, gold.
// Each cue grants: startBonus (extra seconds at match start), aimBonus (% aim accuracy /
// narrower aim-assist cone), powerBonus (% max power). Bonuses scale with rarity.
const RARITY = {
  common: { mult: 1, color: '#9aa0a6' },
  rare: { mult: 2, color: '#4fa3ff' },
  epic: { mult: 3.5, color: '#b060ff' },
  legendary: { mult: 6, color: '#ffd447' },
};

function cue(id, key, name, category, rarity, price, currency, unlockLvl = 0) {
  const r = RARITY[rarity];
  return {
    id, key, name, category, rarity,
    price, currency, // currency: 'coins' | 'free'
    unlockLvl,
    bonuses: {
      startBonus: +(0.5 * r.mult).toFixed(1) - 0.5 + 0.5, // 0.5, 1, 1.75, 3
      aimBonus: +(2 * r.mult).toFixed(1),
      powerBonus: +(1 * r.mult).toFixed(1),
    },
  };
}

const FLAGS = [
  ['sa', 'Saudi Arabia'], ['eg', 'Egypt'], ['fr', 'France'], ['us', 'United States'],
  ['gb', 'United Kingdom'], ['de', 'Germany'], ['es', 'Spain'], ['tr', 'Turkey'],
  ['jp', 'Japan'], ['br', 'Brazil'], ['ma', 'Morocco'], ['dz', 'Algeria'],
];

export const CUES = [
  cue(1, 'wood-basic', 'Wooden Cue', 'wood', 'common', 0, 'free', 0), // starter cue
  cue(2, 'wood-oak', 'Oak Cue', 'wood', 'common', 3000, 'coins', 1),
  cue(3, 'wood-ebony', 'Ebony Cue', 'wood', 'rare', 12000, 'coins', 3),
  cue(4, 'wood-rosewood', 'Rosewood Cue', 'wood', 'rare', 20000, 'coins', 5),
  cue(5, 'wood-master', 'Master Craft Cue', 'wood', 'epic', 60000, 'coins', 8),
  ...FLAGS.map(([code, name], i) =>
    cue(10 + i, `flag-${code}`, `${name} Flag Cue`, 'flag', i < 4 ? 'rare' : 'epic',
      i < 4 ? 15000 : 45000, 'coins', i < 4 ? 3 : 7)),
  cue(30, 'dragon-fire', 'Fire Dragon Cue', 'dragon', 'epic', 90000, 'coins', 10),
  cue(31, 'dragon-imperial', 'Imperial Dragon Cue', 'dragon', 'legendary', 350000, 'coins', 14),
  cue(32, 'galaxy-nebula', 'Nebula Cue', 'galaxy', 'epic', 120000, 'coins', 11),
  cue(33, 'galaxy-supernova', 'Supernova Cue', 'galaxy', 'legendary', 500000, 'coins', 17),
  cue(34, 'laser-blue', 'Blue Laser Cue', 'laser', 'epic', 100000, 'coins', 9),
  cue(35, 'laser-red', 'Red Laser Cue', 'laser', 'epic', 100000, 'coins', 9),
  cue(36, 'laser-prism', 'Prism Laser Cue', 'laser', 'legendary', 400000, 'coins', 15),
  cue(37, 'gold-pure', 'Pure Gold Cue', 'gold', 'legendary', 800000, 'coins', 20),
  cue(38, 'gold-diamond', 'Diamond Gold Cue', 'gold', 'legendary', 1500000, 'coins', 24),
  cue(39, 'viking', 'Viking Cue', 'special', 'legendary', 250000, 'coins', 12),
  cue(40, 'phoenix', 'Phoenix Cue', 'special', 'legendary', 2000000, 'coins', 28),
  cue(41, 'wood-bamboo', 'Bamboo Cue', 'wood', 'common', 1500, 'coins', 1),
  cue(42, 'carbon-fiber', 'Carbon Fiber Cue', 'special', 'rare', 18000, 'coins', 4),
  cue(43, 'neon-storm', 'Neon Storm Cue', 'laser', 'epic', 110000, 'coins', 10),
  cue(44, 'crystal-shard', 'Crystal Shard Cue', 'galaxy', 'epic', 130000, 'coins', 11),
];

export function getCue(id) {
  return CUES.find(c => c.id === Number(id));
}
