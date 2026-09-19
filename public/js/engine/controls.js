// Control scheme, modelled on how the big mobile pool games feel:
//
//   * Drag anywhere on the cloth to aim. The aim does NOT jump to your finger - it
//     rotates by how far your finger swings around the cue ball, so you can place a
//     shot precisely instead of stabbing at a point.
//   * Drag straight back from behind the cue ball to draw the stick, release to strike.
//   * A fine-aim strip and arrow buttons under the table for last-degree adjustments,
//     plus the side power slider for anyone who prefers a slider.

const PULL_REACH = 185;   // how far back along the stick a touch still grabs it
const PULL_WIDTH = 34;    // how far off the stick's axis that touch may be
const PULL_FULL = 120;    // drag distance (table units) that equals 100% power

export function createControls({
  canvasEl, powerEl, cueEl, fillEl, labelEl,
  stripEl, tickEl, readoutEl, ccwEl, cwEl,
  getCueBall, tableFromNorm, canPlay,
  sensitivity = 50,
  onAimChange, onPowerChange, onShoot,
}) {
  let aimAngle = -Math.PI / 2;
  let power = 0;
  let gesture = null;        // { kind: 'aim' | 'pull' | 'strip', ... }
  let tickOffset = 0;

  const fineStep = (0.5 + (sensitivity / 100) * 2.0) * Math.PI / 180;
  const stripRate = (0.06 + (sensitivity / 100) * 0.16) * Math.PI / 180; // radians per px

  function setAim(angle) {
    aimAngle = norm(angle);
    const deg = Math.round(((aimAngle * 180 / Math.PI) + 90 + 360) % 360);
    if (readoutEl) readoutEl.textContent = `${deg}°`;
    onAimChange?.(aimAngle);
  }

  function setPower(p) {
    power = Math.max(0, Math.min(1, p));
    if (fillEl) fillEl.style.height = `${power * 100}%`;
    if (labelEl) labelEl.textContent = `${Math.round(power * 100)}%`;
    if (cueEl && powerEl) {
      const travel = Math.max(0, powerEl.clientHeight - cueEl.offsetHeight - 10);
      cueEl.style.transform = `translateX(-50%) translateY(${power * travel}px)`;
    }
    onPowerChange?.(power);
  }

  function release() {
    if (power > 0.05) onShoot?.(aimAngle, power);
    setPower(0);
  }

  /* ---------------------------------------------------------- table gestures */
  function pointFromEvent(e) {
    const rect = canvasEl.getBoundingClientRect();
    return tableFromNorm((e.clientX - rect.left) / rect.width, (e.clientY - rect.top) / rect.height);
  }

  canvasEl?.addEventListener('pointerdown', (e) => {
    if (!canPlay()) return;
    canvasEl.setPointerCapture(e.pointerId);
    const p = pointFromEvent(e);
    const ball = getCueBall();
    const dx = p.x - ball.x, dy = p.y - ball.y;
    const ax = Math.cos(aimAngle), ay = Math.sin(aimAngle);
    // Touching the drawn stick (behind the ball, near its axis) grabs it to shoot.
    // Anywhere else on the cloth aims.
    const behind = -(dx * ax + dy * ay);
    const offAxis = Math.abs(dx * -ay + dy * ax);
    if (behind > -ball.r && behind < PULL_REACH && offAxis < PULL_WIDTH) {
      gesture = { kind: 'pull', startX: p.x, startY: p.y, base: power };
    } else {
      gesture = { kind: 'aim', startAngle: Math.atan2(dy, dx), baseAim: aimAngle };
    }
  });

  canvasEl?.addEventListener('pointermove', (e) => {
    if (!gesture || e.buttons === 0) return;
    const p = pointFromEvent(e);
    const ball = getCueBall();

    if (gesture.kind === 'aim') {
      const a = Math.atan2(p.y - ball.y, p.x - ball.x);
      setAim(gesture.baseAim + norm(a - gesture.startAngle));
    } else if (gesture.kind === 'pull') {
      // only the component dragged *away* from the aim direction counts
      const dx = p.x - gesture.startX, dy = p.y - gesture.startY;
      const back = -(dx * Math.cos(aimAngle) + dy * Math.sin(aimAngle));
      setPower(gesture.base + back / PULL_FULL);
    }
  });

  const endTableGesture = () => {
    if (!gesture) return;
    const kind = gesture.kind;
    gesture = null;
    if (kind === 'pull') release();
  };
  canvasEl?.addEventListener('pointerup', endTableGesture);
  canvasEl?.addEventListener('pointercancel', endTableGesture);

  /* ------------------------------------------------------- fine aim strip */
  stripEl?.addEventListener('pointerdown', (e) => {
    stripEl.setPointerCapture(e.pointerId);
    gesture = { kind: 'strip', lastX: e.clientX };
    stripEl.classList.add('active');
  });
  stripEl?.addEventListener('pointermove', (e) => {
    if (gesture?.kind !== 'strip') return;
    const dx = e.clientX - gesture.lastX;
    gesture.lastX = e.clientX;
    setAim(aimAngle + dx * stripRate);
    tickOffset = (tickOffset + dx) % 12;
    if (tickEl) tickEl.style.backgroundPositionX = `${tickOffset}px`;
  });
  const endStrip = () => {
    if (gesture?.kind === 'strip') gesture = null;
    stripEl?.classList.remove('active');
  };
  stripEl?.addEventListener('pointerup', endStrip);
  stripEl?.addEventListener('pointercancel', endStrip);

  /* --------------------------------------------------- fine arrows (hold) */
  function bindHold(el, delta) {
    if (!el) return;
    let to = null, iv = null;
    const step = () => setAim(aimAngle + delta);
    const stop = () => { clearTimeout(to); clearInterval(iv); to = iv = null; };
    el.addEventListener('pointerdown', (e) => {
      e.preventDefault();
      step();
      to = setTimeout(() => { iv = setInterval(step, 55); }, 300);
    });
    ['pointerup', 'pointerleave', 'pointercancel'].forEach(ev => el.addEventListener(ev, stop));
  }
  bindHold(ccwEl, -fineStep);
  bindHold(cwEl, fineStep);

  /* ------------------------------------------------------- power slider */
  function sliderPower(e) {
    const rect = powerEl.getBoundingClientRect();
    setPower((e.clientY - rect.top) / rect.height);
  }
  powerEl?.addEventListener('pointerdown', (e) => {
    if (!canPlay()) return;
    gesture = { kind: 'slider' };
    powerEl.setPointerCapture(e.pointerId);
    powerEl.classList.add('charging');
    sliderPower(e);
  });
  powerEl?.addEventListener('pointermove', (e) => { if (gesture?.kind === 'slider') sliderPower(e); });
  const endSlider = (fire) => {
    if (gesture?.kind !== 'slider') return;
    gesture = null;
    powerEl.classList.remove('charging');
    if (fire) release(); else setPower(0);
  };
  powerEl?.addEventListener('pointerup', () => endSlider(true));
  powerEl?.addEventListener('pointercancel', () => endSlider(false));

  setAim(aimAngle);
  setPower(0);

  return {
    setAim, setPower,
    getAimAngle: () => aimAngle,
    getPower: () => power,
    isPulling: () => gesture?.kind === 'pull' || gesture?.kind === 'slider',
  };
}

// wrap to (-PI, PI] so relative rotations never jump a full turn
function norm(a) {
  while (a > Math.PI) a -= Math.PI * 2;
  while (a <= -Math.PI) a += Math.PI * 2;
  return a;
}
