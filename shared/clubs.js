// الأندية: الاسم، الألوان، الطقم، وشكل الشعار (يُرسم SVG إجرائياً)
export const CLUBS = [
  { id: 'falcons', name: 'الصقور', short: 'SQR', city: 'الجزائر', rating: 82, primary: '#c8102e', secondary: '#ffffff', crest: { shape: 'shield', stripes: 'v', icon: 'wing' },
    kit: { pattern: 'pinstripes', base: '#c8102e', second: '#e8e8e8', trim: '#ffffff', text: '#ffffff', sponsor: 'LEGENDS', shorts: '#f4f4f4', shortsTrim: '#c8102e', socks: '#c8102e', sockBand: '#ffffff' } },
  { id: 'tigers', name: 'النمور', short: 'NMR', city: 'وهران', rating: 81, primary: '#1e5bd6', secondary: '#ffd23f', crest: { shape: 'round', stripes: 'h', icon: 'claw' },
    kit: { pattern: 'hoops', base: '#1e5bd6', second: '#15409e', trim: '#ffd23f', text: '#ffd23f', sponsor: '5v5 ONLINE', shorts: '#0d2c66', shortsTrim: '#ffd23f', socks: '#1e5bd6', sockBand: '#ffd23f' } },
  { id: 'lions', name: 'الأسود', short: 'ASD', city: 'قسنطينة', rating: 84, primary: '#0f7a3a', secondary: '#f4f4f4', crest: { shape: 'shield', stripes: 'none', icon: 'crown' },
    kit: { pattern: 'stripes', base: '#0f7a3a', second: '#f2f2f2', trim: '#0b4f27', text: '#ffffff', sponsor: 'ATLAS', shorts: '#0f7a3a', shortsTrim: '#ffffff', socks: '#f2f2f2', sockBand: '#0f7a3a' } },
  { id: 'eagles', name: 'النسور', short: 'NSR', city: 'عنابة', rating: 80, primary: '#111111', secondary: '#e9e9e9', crest: { shape: 'diamond', stripes: 'v', icon: 'star' },
    kit: { pattern: 'halves', base: '#161616', second: '#ededed', trim: '#d4a017', text: '#d4a017', sponsor: 'EAGLE', shorts: '#161616', shortsTrim: '#d4a017', socks: '#161616', sockBand: '#ededed' } },
  { id: 'sharks', name: 'القروش', short: 'QRS', city: 'بجاية', rating: 79, primary: '#0a9396', secondary: '#0b1f3a', crest: { shape: 'round', stripes: 'wave', icon: 'fin' },
    kit: { pattern: 'gradient', base: '#0fb5b8', second: '#0a4f6b', trim: '#0b1f3a', text: '#ffffff', sponsor: 'OCEAN', shorts: '#0b1f3a', shortsTrim: '#0fb5b8', socks: '#0b1f3a', sockBand: '#0fb5b8' } },
  { id: 'wolves', name: 'الذئاب', short: 'DHB', city: 'سطيف', rating: 81, primary: '#f07c00', secondary: '#2b2b2b', crest: { shape: 'shield', stripes: 'chevron', icon: 'moon' },
    kit: { pattern: 'chevron', base: '#f07c00', second: '#c45f00', trim: '#2b2b2b', text: '#1a1a1a', sponsor: 'PACK', shorts: '#2b2b2b', shortsTrim: '#f07c00', socks: '#f07c00', sockBand: '#2b2b2b' } },
  { id: 'stars', name: 'النجوم', short: 'NJM', city: 'تلمسان', rating: 83, primary: '#f5f5f5', secondary: '#d4a017', crest: { shape: 'round', stripes: 'none', icon: 'star' },
    kit: { pattern: 'sash', base: '#f5f5f5', second: '#d4a017', trim: '#d4a017', text: '#1a1a1a', sponsor: 'GALAXY', shorts: '#f5f5f5', shortsTrim: '#d4a017', socks: '#f5f5f5', sockBand: '#d4a017' } },
  { id: 'kings', name: 'الملوك', short: 'MLK', city: 'البليدة', rating: 85, primary: '#5b21b6', secondary: '#fbbf24', crest: { shape: 'diamond', stripes: 'none', icon: 'crown' },
    kit: { pattern: 'plain', base: '#5b21b6', second: '#4c1d95', trim: '#fbbf24', text: '#fbbf24', sponsor: 'ROYAL', shorts: '#fbbf24', shortsTrim: '#5b21b6', socks: '#5b21b6', sockBand: '#fbbf24' } },
];
export const CLUB_BY_ID = Object.fromEntries(CLUBS.map((c) => [c.id, c]));
export function clubOf(id) { return CLUB_BY_ID[id] || CLUBS[0]; }

const GK_KITS = [
  { pattern: 'gradient', base: '#1f9d55', second: '#0b4f29', trim: '#111111', text: '#ffffff', shorts: '#151515', shortsTrim: '#1f9d55', socks: '#1f9d55', sockBand: '#111111', longSleeves: true },
  { pattern: 'chevron', base: '#f08a00', second: '#b85f00', trim: '#111111', text: '#111111', shorts: '#222222', shortsTrim: '#f08a00', socks: '#f08a00', sockBand: '#111111', longSleeves: true },
  { pattern: 'plain', base: '#e6ff00', second: '#b8cc00', trim: '#111111', text: '#111111', shorts: '#111111', shortsTrim: '#e6ff00', socks: '#e6ff00', sockBand: '#111111', longSleeves: true },
  { pattern: 'plain', base: '#ff3d8b', second: '#c21f66', trim: '#111111', text: '#ffffff', shorts: '#111111', shortsTrim: '#ff3d8b', socks: '#ff3d8b', sockBand: '#111111', longSleeves: true },
];

function hexRgb(h) { const n = parseInt(h.slice(1), 16); return [(n >> 16) & 255, (n >> 8) & 255, n & 255]; }
function dist(a, b) { const x = hexRgb(a), y = hexRgb(b); return Math.hypot(x[0] - y[0], x[1] - y[1], x[2] - y[2]); }

// طقم بديل عند تشابه الألوان
function awayKit(c) {
  const k = c.kit;
  return { ...k, pattern: 'plain', base: k.shorts === k.base ? '#f4f4f4' : k.shorts, second: k.base, trim: k.base, text: k.base, shorts: k.base, shortsTrim: k.shorts, socks: k.shorts, sockBand: k.base };
}

// يحوّل ناديين إلى بيانات فريقين (مع حل تعارض الألوان)
export function teamsFromClubs(homeId, awayId) {
  const h = clubOf(homeId);
  let a = clubOf(awayId);
  if (a.id === h.id) a = CLUBS.find((c) => c.id !== h.id);
  const hk = h.kit;
  let ak = a.kit;
  if (dist(hk.base, ak.base) < 120) ak = awayKit(a);
  const gks = GK_KITS.filter((g) => dist(g.base, hk.base) > 150 && dist(g.base, ak.base) > 150);
  const mk = (c, kit, gk) => ({ id: c.id, name: c.name, short: c.short, color: kit.base, color2: kit.trim === kit.base ? c.secondary : kit.trim, shorts: kit.shorts, socks: kit.socks, gk: gk.base, kit, gkKit: { ...gk, sponsor: kit.sponsor }, crest: c.crest, primary: c.primary, secondary: c.secondary, rating: c.rating });
  return [mk(h, hk, gks[0] || GK_KITS[0]), mk(a, ak, gks[1] || GK_KITS[1])];
}

// شعار النادي SVG
export function crestSVG(c, size = 64) {
  const club = typeof c === 'string' ? clubOf(c) : c;
  const p = club.primary, s = club.secondary;
  const cr = club.crest || {};
  const shapes = {
    shield: 'M50 4 L92 16 L90 58 Q86 84 50 97 Q14 84 10 58 L8 16 Z',
    round: 'M50 4 A46 46 0 1 1 49.9 4 Z',
    diamond: 'M50 3 L95 50 L50 97 L5 50 Z',
  };
  const d = shapes[cr.shape] || shapes.shield;
  let stripes = '';
  if (cr.stripes === 'v') stripes = [30, 50, 70].map((x) => `<rect x="${x - 5}" y="0" width="10" height="100" fill="${s}" opacity="0.9"/>`).join('');
  else if (cr.stripes === 'h') stripes = [35, 60].map((y) => `<rect x="0" y="${y - 6}" width="100" height="12" fill="${s}" opacity="0.9"/>`).join('');
  else if (cr.stripes === 'chevron') stripes = `<path d="M0 40 L50 62 L100 40 L100 56 L50 78 L0 56 Z" fill="${s}"/>`;
  else if (cr.stripes === 'wave') stripes = `<path d="M0 60 Q25 48 50 60 T100 60 L100 72 Q75 60 50 72 T0 72 Z" fill="${s}"/>`;
  const icons = {
    wing: `<path d="M28 58 Q40 30 72 30 Q60 38 64 44 Q52 42 50 50 Q44 48 40 56 Z" fill="#fff" stroke="#111" stroke-width="2"/>`,
    claw: `<path d="M34 34 L42 62 M48 30 L52 62 M62 34 L58 62" stroke="#fff" stroke-width="7" stroke-linecap="round"/>`,
    crown: `<path d="M28 60 L30 36 L41 48 L50 30 L59 48 L70 36 L72 60 Z" fill="${s === '#f4f4f4' || s === '#ffffff' ? '#fbbf24' : s}" stroke="#111" stroke-width="2.5"/>`,
    star: `<path d="M50 26 L56 42 L73 42 L59 52 L64 69 L50 59 L36 69 L41 52 L27 42 L44 42 Z" fill="${s}" stroke="#111" stroke-width="2"/>`,
    fin: `<path d="M30 64 Q46 60 52 30 Q62 50 72 64 Z" fill="#fff" stroke="#111" stroke-width="2"/>`,
    moon: `<path d="M58 28 A22 22 0 1 0 64 66 A17 17 0 1 1 58 28 Z" fill="${s}" stroke="#111" stroke-width="2"/>`,
  };
  const id = 'c' + club.id + Math.floor(Math.random() * 1e6);
  return `<svg class="crest" width="${size}" height="${size}" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
    <defs><clipPath id="${id}"><path d="${d}"/></clipPath><linearGradient id="${id}g" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fff" stop-opacity="0.35"/><stop offset="0.5" stop-color="#fff" stop-opacity="0"/></linearGradient></defs>
    <path d="${d}" fill="${p}"/>
    <g clip-path="url(#${id})">${stripes}${icons[cr.icon] || ''}<rect width="100" height="100" fill="url(#${id}g)"/></g>
    <path d="${d}" fill="none" stroke="${s === '#ffffff' || s === '#f4f4f4' ? '#111' : s}" stroke-width="5"/>
    <text x="50" y="91" text-anchor="middle" font-family="Oswald, Arial Black, Arial" font-weight="700" font-size="13" fill="${cr.shape === 'round' || cr.shape === 'diamond' ? s : '#fff'}" opacity="0">${club.short}</text>
  </svg>`;
}
