// ثوابت مشتركة بين الخادم والمتصفح
export const TICK_RATE = 60;
export const DT = 1 / TICK_RATE;
export const SNAPSHOT_EVERY = 2; // 30 لقطة/ثانية للشبكة

// أبعاد الملعب بالمتر (x = الطول, y = العرض, z = الارتفاع)
export const FIELD = {
  L: 52,
  W: 34,
  GW: 6.0,   // عرض المرمى
  GH: 2.3,   // ارتفاع العارضة
  GD: 1.9,   // عمق الشباك
  BOX_R: 7.5,  // نصف قطر منطقة الجزاء (شكل قوس)
  CIRCLE_R: 5,
  POST_R: 0.07,
  WALL: 0.7, // جدران الملعب الداخلي (بعد خط التماس)
};

export const BALL_R = 0.13;
export const PLAYER_R = 0.45;
export const GRAVITY = 9.81;

export const TEAM_SIZE = 5;

// أوضاع اللعب: واقعي (افتراضي) أو أساطير (قدرات خاصة وإيقاع أركيد)
export const MODES = {
  real: {
    id: 'real', label: 'واقعي', base: 5.9, sprint: 1.47, acc: 11, accS: 8.5, stop: 15, k: 8, turn: 0.42, stamina: 0.11,
    walls: false, abilities: false, assist: false, fouls: true, knock: true,
    stickR: 1.4, stickK: 11, reach: 0.38, rel: 17, catchK: 16, pass: [7, 0.7, 8.5, 23], shot: [13, 17],
    kickoff: 2.0, out: 1.0, spBot: 1.2, spHuman: 8, goalT: 4.8, replayT: 6.2,
  },
  legends: {
    id: 'legends', label: 'أساطير', base: 7.0, sprint: 1.33, acc: 26, accS: 22, stop: 26, k: 14, turn: 0.75, stamina: 0.075,
    walls: true, abilities: true, assist: true, fouls: false, knock: false,
    stickR: 2.6, stickK: 26, reach: 0.6, rel: 26, catchK: 17, pass: [10, 0.85, 12, 30], shot: [17, 19],
    kickoff: 1.2, out: 0.6, spBot: 0.6, spHuman: 4, goalT: 3.6, replayT: 4.6,
  },
};

// أزرار الإدخال (bitmask)
export const BTN = {
  SHOOT: 1,
  PASS: 2,
  LOB: 4,
  SPRINT: 8,
  TACKLE: 16,
  ABILITY: 32,
  SKIP: 64,
};

export const PHASE = {
  KICKOFF: 'kickoff',
  PLAY: 'play',
  OUT: 'out',
  SETPIECE: 'setpiece',
  GOAL: 'goal',
  REPLAY: 'replay',
  END: 'end',
};

export const STATE = {
  NORMAL: 0,
  SLIDE: 1,
  DOWN: 2,
  DIVE: 3,
  CELEBRATE: 4,
  STUMBLE: 5,
  SAD: 6,
};

export const TEAMS = [
  { name: 'الصقور', short: 'SQR', color: '#d7263d', color2: '#ffffff', shorts: '#ffffff', socks: '#d7263d', gk: '#1f9d55' },
  { name: 'النمور', short: 'NMR', color: '#1e63d6', color2: '#ffd23f', shorts: '#0d2c66', socks: '#1e63d6', gk: '#f08a00' },
];

export const DIFFICULTY = {
  easy:   { think: 0.22, speed: 0.9,  aim: 0.55, tackle: 0.35, reach: 0.8, label: 'سهل' },
  normal: { think: 0.13, speed: 0.97, aim: 0.75, tackle: 0.55, reach: 0.9,  label: 'متوسط' },
  hard:   { think: 0.08, speed: 1.03, aim: 0.88, tackle: 0.75, reach: 1.05, label: 'صعب' },
};

export const EMOTES = [
  { id: 1, name: 'زحلقة الركبة', icon: '🦵' },
  { id: 2, name: 'شقلبة خلفية', icon: '🤸' },
  { id: 3, name: 'رقصة', icon: '💃' },
  { id: 4, name: 'الطائرة', icon: '✈️' },
  { id: 5, name: 'سيييو', icon: '🐐' },
];

export const DURATIONS = [120, 180, 300, 480];
