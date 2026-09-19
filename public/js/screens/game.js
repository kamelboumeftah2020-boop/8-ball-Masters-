import { state } from '../state.js';
import { t } from '../i18n.js';
import { navigate, onLeave } from '../router.js';
import { on, emit } from '../net/socket.js';
import { avatarHtml, formatTime } from '../ui.js';
import { Table } from '../engine/physics.js';
import { sizeCanvas, drawTable, canvasNormToTable, cueStyleFor } from '../engine/render.js';
import { createControls } from '../engine/controls.js';

const EMOJIS = ['😂', '🥲', '😡', '😱'];
const PULL_MAX = 34;       // how far the stick draws back at full power
const THRUST_MS = 110;     // stick follow-through before the ball is actually struck

export function render(root, { matchData }) {
  const matchId = matchData.matchId;
  const table = matchData.table;
  const totalBalls = matchData.me.totalBalls;

  let myTime = matchData.me.time;
  let myPotted = matchData.me.potted;
  let oppTime = matchData.opponent?.time ?? 0;
  let oppPotted = matchData.opponent?.potted ?? 0;
  let lastTickAt = performance.now();
  let rescueUsed = false;
  let rafId = null;
  let shotAnim = null;      // { start, power }
  let pendingShot = null;   // fired once the stick finishes its thrust

  const cue = state.cues.find(c => c.id === state.user.equippedCueId) || state.cues[0];
  const cueStyle = cueStyleFor(cue?.category);

  root.innerHTML = `
    <div class="game-screen">
      <div class="game-topbar">
        <div class="side">
          ${avatarHtml(state.user.avatarId, 'sm')}
          <div class="info">
            <span class="nm">${state.user.nickname}</span>
            <span class="cn">🪙 ${matchData.entry}</span>
            <div class="balls-left" id="my-balls"></div>
          </div>
        </div>
        <div class="timer" id="my-timer">20.00</div>
        <div class="timer opp" id="opp-timer">20.00</div>
        <div class="side" style="flex-direction:row-reverse; text-align:end;">
          ${avatarHtml(matchData.opponent?.avatarId ?? 0, 'sm')}
          <div class="info" style="align-items:flex-end;">
            <span class="nm">${matchData.opponent?.nickname || 'Bot'}</span>
            <div class="balls-left" id="opp-balls"></div>
          </div>
        </div>
      </div>

      <div class="table-area" id="table-area">
        <canvas id="table-canvas"></canvas>
        <button class="btn sm gold rescue-btn" id="rescue-btn">📺 ${t('watchAdRescue')}</button>
      </div>

      <div class="controls-area">
        <div class="ctrl-left">
          <div class="aim-row">
            <button class="fine-btn" id="aim-ccw">◀</button>
            <div class="aim-strip" id="aim-strip">
              <div class="strip-ticks" id="strip-ticks"></div>
              <span id="angle-readout">0°</span>
            </div>
            <button class="fine-btn" id="aim-cw">▶</button>
          </div>
          <div class="emoji-bar" id="emoji-bar">
            ${EMOJIS.map(e => `<button data-e="${e}">${e}</button>`).join('')}
          </div>
        </div>

        <div class="power-control">
          <div class="power-track" id="power-track">
            <div class="power-fill" id="power-fill"></div>
            <div class="power-cue" id="power-cue"></div>
          </div>
          <div class="power-label" id="power-label">0%</div>
        </div>
      </div>
    </div>`;

  const canvas = document.getElementById('table-canvas');
  const tableArea = document.getElementById('table-area');
  const rescueBtn = document.getElementById('rescue-btn');
  const gameTable = new Table(onPot, onScratch, cue?.bonuses || {});

  function onPot() {
    myPotted++;
    myTime += 10;
    lastTickAt = performance.now();
    vibrate(60);
    emit('ball_potted', { matchId });
    updateBallsUi();
  }
  function onScratch() {
    emit('cue_scratch', { matchId });
  }

  function resize() { sizeCanvas(canvas, tableArea); }
  resize();
  window.addEventListener('resize', resize);

  const controls = createControls({
    canvasEl: canvas,
    powerEl: document.getElementById('power-track'),
    fillEl: document.getElementById('power-fill'),
    cueEl: document.getElementById('power-cue'),
    labelEl: document.getElementById('power-label'),
    stripEl: document.getElementById('aim-strip'),
    tickEl: document.getElementById('strip-ticks'),
    readoutEl: document.getElementById('angle-readout'),
    ccwEl: document.getElementById('aim-ccw'),
    cwEl: document.getElementById('aim-cw'),
    sensitivity: state.user?.settings?.aimSensitivity ?? 50,
    getCueBall: () => gameTable.cue,
    tableFromNorm: canvasNormToTable,
    canPlay: () => gameTable.isAllStopped() && !shotAnim,
    onShoot: (angle, power) => {
      if (!gameTable.isAllStopped() || shotAnim) return;
      shotAnim = { start: performance.now(), power };
      pendingShot = { angle, power };
    },
  });

  document.getElementById('emoji-bar').addEventListener('click', (e) => {
    const btn = e.target.closest('[data-e]');
    if (btn) emit('emoji', { matchId, emoji: btn.dataset.e });
  });
  rescueBtn.addEventListener('click', () => {
    rescueUsed = true;
    rescueBtn.classList.remove('show');
    playFakeAd(() => emit('watch_ad_rescue', { matchId }));
  });

  function updateBallsUi() {
    document.getElementById('my-balls').innerHTML = dots(myPotted, totalBalls);
    document.getElementById('opp-balls').innerHTML = dots(oppPotted, totalBalls);
  }
  function dots(potted, total) {
    return Array.from({ length: total }, (_, i) => `<span class="dot ${i < potted ? 'done' : ''}"></span>`).join('');
  }
  updateBallsUi();

  let aboveFive = myTime >= 5;

  function loop(now) {
    const dt = Math.min(0.033, (now - (loop.last || now)) / 1000);
    loop.last = now;
    gameTable.step(dt);

    // cue stick: idle draw-back scales with the power slider, then thrusts forward
    const settled = gameTable.isAllStopped();
    let stick = null;
    if (shotAnim) {
      const k = Math.min(1, (now - shotAnim.start) / THRUST_MS);
      const eased = k * k;
      stick = { visible: true, angle: pendingShot.angle, pullback: shotAnim.power * PULL_MAX * (1 - eased) - eased * 6 };
      if (k >= 1) {
        gameTable.shootCue(pendingShot.angle, pendingShot.power);
        pendingShot = null;
        shotAnim = null;
      }
    } else if (settled) {
      stick = { visible: true, angle: controls.getAimAngle(), pullback: 7 + controls.getPower() * PULL_MAX };
    }

    drawTable(canvas.getContext('2d'), gameTable, {
      theme: table.colors,
      aimAngle: controls.getAimAngle(),
      power: controls.getPower(),
      canShoot: !shotAnim,
      stick,
      cueStyle,
    });

    const elapsed = (performance.now() - lastTickAt) / 1000;
    const dispMy = Math.max(0, myTime - elapsed);
    const dispOpp = Math.max(0, oppTime - elapsed);
    setTimerText('my-timer', dispMy);
    setTimerText('opp-timer', dispOpp, true);

    if (dispMy < 5 && aboveFive) { aboveFive = false; vibrate(250); }
    rescueBtn.classList.toggle('show', dispMy <= 10 && dispMy > 0 && !rescueUsed);

    rafId = requestAnimationFrame(loop);
  }
  rafId = requestAnimationFrame(loop);

  function setTimerText(id, val, dim) {
    const elm = document.getElementById(id);
    elm.textContent = formatTime(val);
    if (!dim) elm.classList.toggle('urgent', val < 10);
  }

  const offTick = on('match_tick', (payload) => {
    myTime = payload.me.time; myPotted = payload.me.potted;
    if (payload.opponent) { oppTime = payload.opponent.time; oppPotted = payload.opponent.potted; }
    lastTickAt = performance.now();
    updateBallsUi();
  });

  const offEmoji = on('opponent_emoji', (payload) => {
    const f = document.createElement('div');
    f.className = 'emoji-float';
    f.textContent = payload.emoji;
    f.style.right = '20px';
    f.style.top = '50px';
    tableArea.appendChild(f);
    setTimeout(() => f.remove(), 1400);
  });

  const offEnd = on('match_end', (payload) => {
    cleanup();
    navigate('result', { payload });
  });

  function cleanup() {
    cancelAnimationFrame(rafId);
    window.removeEventListener('resize', resize);
    offTick(); offEmoji(); offEnd();
  }
  onLeave(cleanup);
}

function vibrate(ms) {
  if (state.user?.settings?.vibration && navigator.vibrate) navigator.vibrate(ms);
}

function playFakeAd(onDone) {
  const root = document.getElementById('modal-root');
  root.innerHTML = `<div class="modal-backdrop"><div class="modal-box"><h3>📺 ${t('adPlaying')}</h3>
    <div class="progress-bar"><div class="fill" id="ad-fill" style="width:0%"></div></div></div></div>`;
  const fill = document.getElementById('ad-fill');
  let p = 0;
  const iv = setInterval(() => {
    p += 20;
    fill.style.width = `${Math.min(100, p)}%`;
    if (p >= 100) {
      clearInterval(iv);
      root.innerHTML = '';
      onDone();
    }
  }, 300);
}
