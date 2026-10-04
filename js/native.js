// الجسر مع تطبيق أندرويد (Capacitor). في المتصفح تعود الدوال دون أثر.
const Cap = window.Capacitor;
export const isNative = !!(Cap && Cap.isNativePlatform && Cap.isNativePlatform());
const plugin = name => (isNative && Cap.Plugins ? Cap.Plugins[name] : null);

export const Notifications = () => plugin('LocalNotifications');

// أصوات الأذان المضمّنة في التطبيق (res/raw) لتعمل الإشعارات والتطبيق مغلق
export const BUNDLED_ADHANS = ['008', '007', '001'];
export const adhanChannel = sound => `adhan_${BUNDLED_ADHANS.includes(sound) ? sound : '008'}`;

export async function share(text) {
  const p = plugin('Share');
  if (!p) return false;
  try { await p.share({ text, dialogTitle: 'مشاركة' }); } catch { /* أُلغيت */ }
  return true;
}

export async function copy(text) {
  const p = plugin('Clipboard');
  if (!p) return false;
  await p.write({ string: text });
  return true;
}

// فتح رابط خارجي (للتحميل مثلًا) في المتصفح
export function openExternal(url) {
  if (isNative) window.location.href = url;
  else window.open(url, '_blank', 'noopener');
}

// خدمة تشغيل في الخلفية تُبقي الصوت يعمل والشاشة مطفأة
let mediaOn = false;
export function mediaPlaying(title, text) {
  const p = plugin('NurMedia');
  if (!p) return;
  mediaOn = true;
  p.start({ title, text }).catch(() => {});
}
export function mediaStopped() {
  const p = plugin('NurMedia');
  if (!p || !mediaOn) return;
  mediaOn = false;
  p.stop().catch(() => {});
}

// لون أيقونات شريط الحالة بحسب مظهر التطبيق (DARK = أيقونات فاتحة على خلفية داكنة)
export function setBarsStyle(dark) {
  plugin('SystemBars')?.setStyle({ style: dark ? 'DARK' : 'LIGHT' }).catch(() => {});
}
