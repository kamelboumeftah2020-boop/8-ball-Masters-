// الشخصيات: لكل شخصية إحصائيات + مظهر + قدرة خاصة
// stats: speed, shot, pass, dribble, tackle, stamina, keeper (مضاعفات حول 1)
export const CHARACTERS = [
  {
    id: 'blaze', name: 'بليز', title: 'الهدّاف الناري', icon: '🔥',
    ability: { id: 'fire', name: 'التسديدة النارية', desc: 'تسديدتك التالية تصبح كرة لهب لا يستطيع الحارس الإمساك بها', cd: 15 },
    stats: { speed: 1.0, shot: 1.15, pass: 0.95, dribble: 1.0, tackle: 0.92, stamina: 1.0, keeper: 0.9 },
    look: { skin: '#c68642', hair: 'spiky', hairColor: '#ff5a1f', build: 1.0, height: 1.0, boots: '#ff5a1f', acc: 'headband', accColor: '#ff5a1f', aura: '#ff6a00' },
    celebration: 5,
  },
  {
    id: 'flash', name: 'فلاش', title: 'البرق السريع', icon: '⚡',
    ability: { id: 'dash', name: 'الانطلاقة الخاطفة', desc: 'سرعة خارقة لمدة 3 ثوانٍ دون استهلاك اللياقة', cd: 13 },
    stats: { speed: 1.1, shot: 0.95, pass: 1.0, dribble: 1.05, tackle: 0.9, stamina: 1.15, keeper: 0.85 },
    look: { skin: '#8d5524', hair: 'mohawk', hairColor: '#ffe14d', build: 0.92, height: 0.98, boots: '#ffe14d', acc: 'wristbands', accColor: '#ffe14d', aura: '#ffe14d' },
    celebration: 4,
  },
  {
    id: 'titan', name: 'تايتن', title: 'الجدار الحديدي', icon: '🗿',
    ability: { id: 'quake', name: 'الزلزال', desc: 'ضربة أرضية تطيح بالخصوم القريبين وتبعد الكرة', cd: 16 },
    stats: { speed: 0.93, shot: 1.12, pass: 0.95, dribble: 0.9, tackle: 1.25, stamina: 1.05, keeper: 1.1 },
    look: { skin: '#5a3825', hair: 'bald', hairColor: '#222222', build: 1.22, height: 1.07, boots: '#444444', acc: 'beard', accColor: '#1a1a1a', aura: '#b07a4a' },
    celebration: 3,
  },
  {
    id: 'frost', name: 'فروست', title: 'ملك الجليد', icon: '❄️',
    ability: { id: 'ice', name: 'العاصفة الجليدية', desc: 'تجمّد وتبطئ كل الخصوم حولك لمدة 3.5 ثانية', cd: 15 },
    stats: { speed: 1.0, shot: 1.0, pass: 1.12, dribble: 1.0, tackle: 1.05, stamina: 1.0, keeper: 1.0 },
    look: { skin: '#f1c27d', hair: 'long', hairColor: '#dff6ff', build: 0.98, height: 1.03, boots: '#7fdbff', acc: 'none', accColor: '#7fdbff', aura: '#7fdbff' },
    celebration: 2,
  },
  {
    id: 'shadow', name: 'شادو', title: 'الشبح', icon: '👤',
    ability: { id: 'blink', name: 'الانتقال الآني', desc: 'تختفي وتظهر 7 أمتار للأمام ومعك الكرة', cd: 12 },
    stats: { speed: 1.05, shot: 0.97, pass: 1.0, dribble: 1.15, tackle: 0.95, stamina: 1.0, keeper: 0.9 },
    look: { skin: '#e0ac69', hair: 'hood', hairColor: '#2a1f3d', build: 0.95, height: 1.0, boots: '#6b3fa0', acc: 'mask', accColor: '#111111', aura: '#9b5cff' },
    celebration: 1,
  },
  {
    id: 'magnet', name: 'ماغنيت', title: 'المغناطيس', icon: '🧲',
    ability: { id: 'pull', name: 'الجذب المغناطيسي', desc: 'يجذب الكرة نحوك من مسافة بعيدة حتى من الخصم', cd: 16 },
    stats: { speed: 1.0, shot: 1.0, pass: 1.05, dribble: 1.12, tackle: 1.0, stamina: 1.0, keeper: 0.95 },
    look: { skin: '#ffdbac', hair: 'afro', hairColor: '#3b2314', build: 1.0, height: 0.97, boots: '#e63946', acc: 'wristbands', accColor: '#e63946', aura: '#e63946' },
    celebration: 3,
  },
  {
    id: 'viper', name: 'فايبر', title: 'ساحر المنعطفات', icon: '🐍',
    ability: { id: 'curve', name: 'الكرة الملتوية', desc: 'تسديدتك التالية تنحني بشكل جنوني حول المدافعين', cd: 13 },
    stats: { speed: 1.02, shot: 1.05, pass: 1.15, dribble: 1.05, tackle: 0.9, stamina: 1.0, keeper: 0.9 },
    look: { skin: '#c68642', hair: 'bun', hairColor: '#111111', build: 0.96, height: 1.01, boots: '#2ecc71', acc: 'headband', accColor: '#2ecc71', aura: '#2ecc71' },
    celebration: 4,
  },
  {
    id: 'thunder', name: 'ثاندر', title: 'سيد الصواعق', icon: '🌩️',
    ability: { id: 'bolt', name: 'الصاعقة', desc: 'صاعقة تشل أقرب خصم لمدة 1.6 ثانية وتسقط منه الكرة', cd: 15 },
    stats: { speed: 1.02, shot: 1.05, pass: 1.0, dribble: 1.0, tackle: 1.05, stamina: 1.0, keeper: 0.95 },
    look: { skin: '#a1665e', hair: 'short', hairColor: '#f5f5f5', build: 1.05, height: 1.02, boots: '#4dd0ff', acc: 'goggles', accColor: '#4dd0ff', aura: '#4dd0ff' },
    celebration: 5,
  },
  {
    id: 'guardian', name: 'غارديان', title: 'الحارس الأسطوري', icon: '🛡️',
    ability: { id: 'wall', name: 'الدرع الطاقي', desc: 'جدار طاقة أمامك يصد الكرة لمدة 4 ثوانٍ', cd: 14 },
    stats: { speed: 0.96, shot: 1.0, pass: 1.05, dribble: 0.95, tackle: 1.15, stamina: 1.0, keeper: 1.3 },
    look: { skin: '#8d5524', hair: 'short', hairColor: '#111111', build: 1.1, height: 1.06, boots: '#ffd700', acc: 'gloves', accColor: '#ffd700', aura: '#ffd700' },
    celebration: 3,
  },
];

export const CHAR_BY_ID = Object.fromEntries(CHARACTERS.map((c) => [c.id, c]));

export function charOf(id) {
  return CHAR_BY_ID[id] || CHARACTERS[0];
}
