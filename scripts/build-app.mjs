// يجهّز ملفات تطبيق الأندرويد (Capacitor): نسخة البوتات المستقلة داخل مستند HTML كامل — dist/app
import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
execFileSync(process.execPath, [path.join(ROOT, 'scripts/build-offline.mjs')], { stdio: 'inherit' });
const inner = fs.readFileSync(path.join(ROOT, 'dist/offline.html'), 'utf8');
const html = `<!doctype html>
<html lang="ar" dir="rtl">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover" />
<meta name="theme-color" content="#060814" />
</head>
<body>
<script>window.IS_APP = true;</script>
${inner}
</body>
</html>
`;
const out = path.join(ROOT, 'dist/app');
fs.mkdirSync(out, { recursive: true });
fs.writeFileSync(path.join(out, 'index.html'), html);
console.log('dist/app/index.html', (html.length / 1024).toFixed(0) + ' KB');
