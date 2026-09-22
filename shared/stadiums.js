// الملاعب: كل ملعب له إضاءة وطقس وخصائص عشب مختلفة
export const STADIUMS = [
  {
    id: 'royal',
    pitch: { pattern: 'stripes', colors: ['#3d8c2c', '#4ba335'], lines: '#ffffff', around: '#2f6b27', ring: '#9c3b2b' }, light: [1, 1, 1], name: 'الملعب الملكي', desc: 'نهار مشمس وجمهور غفير',
    time: 'day', skyTop: '#2f7fd8', skyBottom: '#bfe3ff', fog: '#cfe6ff', fogDensity: 0.0035,
    sun: { color: '#fff4e0', intensity: 3.0, pos: [-40, 60, 30] }, hemi: ['#dff1ff', '#3a5f2a', 1.1],
    grass: ['#3f8f2f', '#4aa336'], standColor: '#b3202c', seatColors: ['#b3202c', '#ffffff', '#1d3557'],
    roof: '#e8e8e8', weather: null, friction: 1.0, bounce: 1.0, crowd: 1.0, floodlights: false,
  },
  {
    id: 'neon',
    pitch: { pattern: 'checker', colors: ['#1d6636', '#277d45'], lines: '#eafcff', glow: ['#00e5ff', '#ff2e88'], around: '#0d1124', ring: '#161c38' }, light: [0.72, 0.76, 0.95], name: 'ساحة النيون', desc: 'مباراة ليلية تحت الأضواء الكاشفة',
    time: 'night', skyTop: '#050716', skyBottom: '#1b1440', fog: '#120d2a', fogDensity: 0.006,
    sun: { color: '#b8c6ff', intensity: 1.1, pos: [20, 60, -20] }, hemi: ['#4b4f9a', '#0c1a10', 0.55],
    grass: ['#2d7a2a', '#35902f'], standColor: '#1b1b2f', seatColors: ['#ff2e88', '#00e5ff', '#3b3b5c'],
    roof: '#22223a', weather: null, friction: 1.0, bounce: 1.0, crowd: 1.1, floodlights: true, neon: true,
  },
  {
    id: 'desert',
    pitch: { pattern: 'diagonal', colors: ['#7f9636', '#90a741'], lines: '#fff4d6', dry: true, around: '#d9b27a', ring: '#c9a063' }, light: [1.05, 0.88, 0.72], name: 'واحة الغروب', desc: 'ملعب صحراوي عند غروب الشمس',
    time: 'sunset', skyTop: '#3d2a6b', skyBottom: '#ff9a4a', fog: '#f0a36a', fogDensity: 0.005,
    sun: { color: '#ffb36b', intensity: 2.6, pos: [-70, 18, -10] }, hemi: ['#ffd0a0', '#6b4a2a', 0.9],
    grass: ['#5b8f2e', '#679c35'], standColor: '#c9a36b', seatColors: ['#f4d35e', '#ee964b', '#0d3b66'],
    roof: '#e6cfa3', weather: 'dust', friction: 1.05, bounce: 1.08, crowd: 0.9, floodlights: true,
  },
  {
    id: 'frozen',
    pitch: { pattern: 'snow', colors: ['#6f956c', '#7aa276'], lines: '#1f5fd1', around: '#eef4f8', ring: '#e2ebf1' }, light: [0.95, 0.98, 1.05], name: 'قمة الجليد', desc: 'ثلج يتساقط والكرة تنزلق أكثر',
    time: 'snow', skyTop: '#8aa3b8', skyBottom: '#e6eef5', fog: '#dde7ef', fogDensity: 0.011,
    sun: { color: '#eef6ff', intensity: 1.6, pos: [30, 50, 40] }, hemi: ['#ffffff', '#8aa0b0', 1.3],
    grass: ['#6f9a6a', '#7da577'], standColor: '#3c5a73', seatColors: ['#e0f2ff', '#1d3557', '#a8dadc'],
    roof: '#f5f8fb', weather: 'snow', friction: 0.72, bounce: 0.85, crowd: 0.85, floodlights: true, snowy: true,
  },
  {
    id: 'rain',
    pitch: { pattern: 'circles', colors: ['#225f28', '#2b6e30'], lines: '#ffffff', puddles: true, around: '#1e3a22', ring: '#2a2e33' }, light: [0.7, 0.74, 0.8], name: 'ملعب العاصفة', desc: 'أمطار غزيرة وبرق في السماء',
    time: 'storm', skyTop: '#1a2129', skyBottom: '#4a5663', fog: '#3a444f', fogDensity: 0.012,
    sun: { color: '#c9d6e3', intensity: 1.0, pos: [10, 60, 30] }, hemi: ['#8898aa', '#1f3322', 0.8],
    grass: ['#2f6e2a', '#37792f'], standColor: '#2a2f36', seatColors: ['#f1faee', '#e63946', '#457b9d'],
    roof: '#3a3f46', weather: 'rain', friction: 0.85, bounce: 0.8, crowd: 1.0, floodlights: true, wet: true,
  },
];

export const STADIUM_BY_ID = Object.fromEntries(STADIUMS.map((s) => [s.id, s]));
export function stadiumOf(id) {
  return STADIUM_BY_ID[id] || STADIUMS[0];
}
