// ينسخ ملفات الواجهة إلى www/ ليضمّها Capacitor في تطبيق أندرويد
// النسخة المخففة (LITE=1): دون خطوط صفحات المصحف (fonts/qcf، نحو ٩٥ م.ب)، فتُحمَّل من داخل التطبيق
import { cpSync, rmSync, mkdirSync } from 'node:fs';

const OUT = 'www';
const LITE = process.env.LITE === '1';
const FILES = ['index.html', 'manifest.webmanifest', 'sw.js', 'css', 'js', 'data', 'fonts', 'icons'];
rmSync(OUT, { recursive: true, force: true });
mkdirSync(OUT);
for (const f of FILES) {
  cpSync(f, `${OUT}/${f}`, { recursive: true, filter: src => !(LITE && /fonts[\\/]qcf/.test(src)) });
}
console.log(LITE ? 'www جاهز (النسخة المخففة، دون خطوط المصحف)' : 'www جاهز');
