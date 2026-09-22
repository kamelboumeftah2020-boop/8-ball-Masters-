// يبني نسخة "ضد البوتات" في ملف HTML واحد مستقل (بدون خادم) — dist/offline.html
import { build } from 'esbuild';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const alias = {
  name: 'alias',
  setup(b) {
    b.onResolve({ filter: /^\/shared\// }, (a) => ({ path: path.join(ROOT, a.path) }));
    b.onResolve({ filter: /^three\/addons\// }, (a) => ({ path: path.join(ROOT, 'node_modules/three/examples/jsm', a.path.slice('three/addons/'.length)) }));
  },
};
const res = await build({
  entryPoints: [path.join(ROOT, 'public/js/main.js')],
  bundle: true, minify: true, format: 'iife', write: false, plugins: [alias], target: 'es2020', legalComments: 'none',
});
const js = res.outputFiles[0].text.replace(/<\/script/g, '<\\/script');
const css = fs.readFileSync(path.join(ROOT, 'public/css/style.css'), 'utf8');
const html = fs.readFileSync(path.join(ROOT, 'public/index.html'), 'utf8');
const body = html.slice(html.indexOf('<body>') + 6, html.indexOf('<script type="module"'));
const out = `<title>أساطير الكرة 5×5</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Cairo:wght@400;700;900&display=swap" />
<style>${css}\n:root{color-scheme:dark}body{background:var(--bg)}</style>
<div dir="rtl" lang="ar">${body}</div>
<script>window.OFFLINE_ONLY=true;</script>
<script>${js}</script>
`;
fs.mkdirSync(path.join(ROOT, 'dist'), { recursive: true });
fs.writeFileSync(path.join(ROOT, 'dist/offline.html'), out);
console.log('dist/offline.html', (out.length / 1024).toFixed(0) + ' KB');
