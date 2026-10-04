// ينسخ ملفات الواجهة إلى www/ ليضمّها Capacitor في تطبيق أندرويد
import { cpSync, rmSync, mkdirSync } from 'node:fs';

const OUT = 'www';
const FILES = ['index.html', 'manifest.webmanifest', 'sw.js', 'css', 'js', 'data', 'fonts', 'icons'];
rmSync(OUT, { recursive: true, force: true });
mkdirSync(OUT);
for (const f of FILES) cpSync(f, `${OUT}/${f}`, { recursive: true });
console.log('www جاهز');
