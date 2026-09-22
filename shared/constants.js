// ثوابت مشتركة بين الخادم والمتصفح
export const TICK_RATE = 60;
export const DT = 1 / TICK_RATE;
export const SNAPSHOT_EVERY = 2; // 30 لقطة/ثانية للشبكة

// أبعاد الملعب بالمتر (x = الطول, y = العرض, z = الارتفاع)
export const FIELD = {
  L: 56,
  W: 34,
  GW: 6.4,   // عرض المرمى
  GH: 2.4,   // ارتفاع العارضة
  GD: 1.9,   // عمق الشباك
  BOX_R: 8,  // نصف قطر منطقة الجزاء (شكل قوس)
  CIRCLE_R: 5,
  POST_R: 0.07,
};

export const BALL_R = 0.2;
export const PLAYER_R = 0.45;
export const GRAVITY = 9.81;

export const TEAM_SIZE = 5;

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
  easy:   { think: 0.32, speed: 0.9,  aim: 0.55, tackle: 0.35, reach: 0.85, label: 'سهل' },
  normal: { think: 0.2,  speed: 0.97, aim: 0.75, tackle: 0.55, reach: 0.95,  label: 'متوسط' },
  hard:   { think: 0.1,  speed: 1.03, aim: 0.9,  tackle: 0.75, reach: 1.12, label: 'صعب' },
};

export const EMOTES = [
  { id: 1, name: 'زحلقة الركبة', icon: '🦵' },
  { id: 2, name: 'شقلبة خلفية', icon: '🤸' },
  { id: 3, name: 'رقصة', icon: '💃' },
  { id: 4, name: 'الطائرة', icon: '✈️' },
  { id: 5, name: 'سيييو', icon: '🐐' },
];

export const DURATIONS = [120, 180, 300, 480];
