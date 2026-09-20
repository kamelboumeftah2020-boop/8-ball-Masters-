// 50+ profile pictures. First 6 are free starter avatars offered at onboarding.
// Rest are purchasable in the shop, priced by rarity. Rendered client-side as
// gradient-badge emoji avatars (no external image assets required).
const STARTER_EMOJIS = ['🎱', '🧢', '🕶️', '😎', '🤴', '🦁'];

const SHOP_EMOJIS = [
  '🐯', '🐺', '🦅', '🐉', '🦈', '🐍', '🦂', '🐢', '🦉', '🐸',
  '🔥', '⚡', '❄️', '🌊', '🌪️', '🌙', '☄️', '🌟', '💎', '👑',
  '🥷', '🤖', '👽', '💀', '🎭', '🃏', '⚔️', '🛡️', '🏆', '🎯',
  '🚀', '🛸', '🏹', '🥊', '🎮', '🎸', '🍀', '🌵', '🌹', '🦄',
  '🐲', '🦇', '🐆', '🦍', '🐊', '🦖',
];

const GRADIENTS = [
  ['#ff512f', '#dd2476'], ['#1f4037', '#99f2c8'], ['#8e2de2', '#4a00e0'],
  ['#f7971e', '#ffd200'], ['#00c6ff', '#0072ff'], ['#ee0979', '#ff6a00'],
  ['#16222a', '#3a6073'], ['#fc5c7d', '#6a82fb'], ['#0f2027', '#2c5364'],
  ['#c31432', '#240b36'],
];

function makeAvatar(id, emoji, price, currency, unlockLvl) {
  const g = GRADIENTS[id % GRADIENTS.length];
  return { id, emoji, gradient: g, price, currency, unlockLvl };
}

export const AVATARS = [
  ...STARTER_EMOJIS.map((e, i) => makeAvatar(i, e, 0, 'free', 0)),
  ...SHOP_EMOJIS.map((e, i) => {
    const idx = i + STARTER_EMOJIS.length;
    const tier = Math.floor(i / 12); // price rises every 12 avatars
    const price = [2000, 8000, 25000, 80000][Math.min(tier, 3)];
    const unlockLvl = [1, 5, 10, 15][Math.min(tier, 3)];
    return makeAvatar(idx, e, price, 'coins', unlockLvl);
  }),
];

export function getAvatar(id) {
  return AVATARS.find(a => a.id === Number(id));
}
