// End-to-end proof through the real UI: capture the layout the server sends, work
// out a shot that pots a ball, aim and strike it with actual pointer input, and
// confirm the server credits the pot back into the UI.
import { launchBrowser } from './browser.mjs';
import { Table, TABLE_W, TABLE_H } from './public/js/engine/physics.js';

const OUT = '/tmp/claude-0/-home-user-8-ball-Masters-/e015f5c8-e4c3-5c14-9783-0070dfa2f24f/scratchpad';
const FRAME = 17, BLEED = 32, OFF = FRAME + BLEED;
const CANVAS_W = TABLE_W + OFF * 2, CANVAS_H = TABLE_H + OFF * 2;

function findPottingShot(layout, bonuses) {
  for (const power of [0.45, 0.6, 0.8, 0.3]) {
    for (let i = 0; i < 180; i++) {
      const angle = (i / 180) * Math.PI * 2;
      const t = new Table(null, null, bonuses);
      t.applySnapshot(JSON.parse(JSON.stringify(layout)));
      let potted = 0;
      t.onPot = () => { potted++; };
      if (!t.shootCue(angle, power)) continue;
      let guard = 0;
      while (!t.isAllStopped() && guard++ < 4000) t.step(1 / 60);
      if (potted > 0) return { angle, power };
    }
  }
  return null;
}

const browser = await launchBrowser();
const page = await browser.newPage({ viewport: { width: 420, height: 860 }, deviceScaleFactor: 2 });
const errs = [];
page.on('pageerror', e => errs.push('EXC: ' + e.message));
page.on('console', m => { if (m.type() === 'error') errs.push('ERR: ' + m.text()); });

// listen in on what the server actually sends this client
let layout = null;
page.on('websocket', (ws) => {
  ws.on('framereceived', ({ payload }) => {
    if (typeof payload !== 'string' || !payload.startsWith('42')) return;
    try {
      const [event, data] = JSON.parse(payload.slice(2));
      if (event === 'match_start' && data.layout) layout = data.layout;
      if (event === 'table_sync' && data.snapshot) layout = data.snapshot;
    } catch {}
  });
});

const bonuses = (await (await fetch('http://localhost:3000/api/cues')).json()).cues.find(c => c.id === 1).bonuses;

await page.goto('http://localhost:3000/', { waitUntil: 'networkidle' });
await page.waitForTimeout(400);
await page.fill('#username', 'Pot' + Date.now() % 10000); await page.click('#go'); await page.waitForTimeout(400);
await page.click('.avatar-pick'); await page.click('#create'); await page.waitForTimeout(900);
await page.click('#go'); await page.waitForTimeout(600);
await page.click('[data-play="1"]');
await page.waitForSelector('.game-screen', { timeout: 12000 });
await page.waitForTimeout(1000);

console.log('layout received from server:', layout ? layout.balls.length + ' balls' : 'NONE');
const box = await (await page.$('#table-canvas')).boundingBox();
const toScreen = (tx, ty) => ({
  x: box.x + ((tx + OFF) / CANVAS_W) * box.width,
  y: box.y + ((ty + OFF) / CANVAS_H) * box.height,
});
const readAim = async () => {
  const deg = parseFloat((await page.textContent('#angle-readout')).replace('°', ''));
  return (deg - 90) * Math.PI / 180;
};

let potted = 0;
for (let shot = 0; shot < 4 && potted < 2; shot++) {
  const plan = findPottingShot(layout, bonuses);
  if (!plan) { console.log('no potting shot available from this layout'); break; }

  // rotate the aim by dragging around the cue ball - the same gesture a player makes
  const cue = layout.balls.find(b => b.cue);
  const pivot = toScreen(cue.x, cue.y);
  const cur = await readAim();
  const r = 110;
  await page.mouse.move(pivot.x + Math.cos(cur) * r, pivot.y + Math.sin(cur) * r);
  await page.mouse.down();
  await page.mouse.move(pivot.x + Math.cos(plan.angle) * r, pivot.y + Math.sin(plan.angle) * r, { steps: 25 });
  await page.mouse.up();
  await page.waitForTimeout(250);

  const aimed = await readAim();
  const err = Math.abs(Math.atan2(Math.sin(aimed - plan.angle), Math.cos(aimed - plan.angle))) * 180 / Math.PI;
  console.log(`shot ${shot + 1}: aimed to ${(plan.angle * 180 / Math.PI).toFixed(1)}deg, off by ${err.toFixed(2)}deg`);

  // pull the cue back to the planned power and release
  const track = await (await page.$('#power-track')).boundingBox();
  await page.mouse.move(track.x + track.width / 2, track.y + 6);
  await page.mouse.down();
  await page.mouse.move(track.x + track.width / 2, track.y + track.height * plan.power, { steps: 12 });
  await page.waitForTimeout(250);
  await page.mouse.up();
  await page.waitForTimeout(3200);

  potted = await page.evaluate(() => document.querySelectorAll('#my-balls .dot.done').length);
  console.log(`  -> pots credited by the server: ${potted}`);
}

await page.screenshot({ path: `${OUT}/authoritative-pot.png` });
console.log(potted > 0 ? 'PASS  a real UI shot was simulated and scored by the server' : 'FAIL  no pot was credited');
console.log(errs.length ? 'ERRORS:\n' + errs.join('\n') : 'no console errors');
await browser.close();
process.exit(potted > 0 ? 0 : 1);
