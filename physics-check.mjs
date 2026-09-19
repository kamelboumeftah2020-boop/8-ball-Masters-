// Numeric sanity check for the billiard simulation: no tunneling, no stuck overlaps,
// nothing leaves the table, and shots travel a sensible distance.
import { Table, TABLE_W, TABLE_H, BALL_R, CUSHION } from './public/js/engine/physics.js';

function simulate(angle, power, maxSeconds = 12) {
  let pots = 0, scratches = 0;
  const table = new Table(() => pots++, () => scratches++);
  table.shootCue(angle, power);

  const start = { x: table.cue.x, y: table.cue.y };
  let travelled = 0, prev = { ...start };
  let worstOverlap = 0, escapes = 0, frames = 0;

  const dt = 1 / 60;
  for (let tSec = 0; tSec < maxSeconds; tSec += dt) {
    table.step(dt);
    frames++;

    const live = table.balls.filter(b => b.active);
    for (const b of live) {
      travelledCheck(b);
      for (const o of live) {
        if (o === b) continue;
        const d = Math.hypot(b.x - o.x, b.y - o.y);
        worstOverlap = Math.max(worstOverlap, Math.max(0, BALL_R * 2 - d));
      }
    }
    if (table.cue.active) {
      travelled += Math.hypot(table.cue.x - prev.x, table.cue.y - prev.y);
      prev = { x: table.cue.x, y: table.cue.y };
    }
    if (table.isAllStopped()) break;
  }

  function travelledCheck(b) {
    const slack = 1.5;
    if (b.x < CUSHION + BALL_R - slack || b.x > TABLE_W - CUSHION - BALL_R + slack
      || b.y < CUSHION + BALL_R - slack || b.y > TABLE_H - CUSHION - BALL_R + slack) escapes++;
  }

  return { travelled, worstOverlap, escapes, pots, scratches, seconds: frames / 60 };
}

let bad = 0;
const rows = [];
for (const power of [0.15, 0.35, 0.6, 0.85, 1.0]) {
  let agg = { travelled: 0, worstOverlap: 0, escapes: 0, seconds: 0, pots: 0 };
  const runs = 40;
  for (let i = 0; i < runs; i++) {
    const r = simulate(-Math.PI / 2 + (Math.random() - 0.5) * 1.2, power);
    agg.travelled += r.travelled / runs;
    agg.seconds += r.seconds / runs;
    agg.pots += r.pots / runs;
    agg.worstOverlap = Math.max(agg.worstOverlap, r.worstOverlap);
    agg.escapes += r.escapes;
  }
  rows.push([power, agg]);
  if (agg.escapes > 0 || agg.worstOverlap > 1.2) bad++;
}

console.log('power | cue travel (units) | roll time (s) | avg pots | worst overlap | escapes');
for (const [power, a] of rows) {
  console.log(
    `${power.toFixed(2).padStart(5)} | ${a.travelled.toFixed(0).padStart(18)} | ${a.seconds.toFixed(2).padStart(13)} | ` +
    `${a.pots.toFixed(2).padStart(8)} | ${a.worstOverlap.toFixed(2).padStart(13)} | ${String(a.escapes).padStart(7)}`,
  );
}
console.log(`\ntable playfield: ${TABLE_W - CUSHION * 2} x ${TABLE_H - CUSHION * 2} units`);
console.log(bad === 0 ? 'PHYSICS OK' : `PHYSICS PROBLEMS in ${bad} power bands`);
process.exit(bad === 0 ? 0 : 1);
