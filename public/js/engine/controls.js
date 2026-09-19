// One-thumb control scheme: aim with the dial (or by dragging straight on the table),
// then pull the cue down on the power slider and release to shoot - there is no
// separate "hit" button.
export function createControls({
  dialEl, powerEl, canvasEl, needleEl, fillEl, cueEl, labelEl, readoutEl,
  ccwEl, cwEl, sensitivity = 50,
  onAimChange, onPowerChange, onShoot,
}) {
  let aimAngle = -Math.PI / 2;
  let power = 0;
  let charging = false;
  let onCanvasAim = null;

  const fineStep = (0.6 + (sensitivity / 100) * 2.4) * Math.PI / 180;

  function setAim(angle) {
    aimAngle = angle;
    const deg = (angle * 180) / Math.PI;
    if (needleEl) needleEl.style.transform = `rotate(${deg + 90}deg)`;
    if (readoutEl) readoutEl.textContent = `${Math.round(((deg + 90) % 360 + 360) % 360)}°`;
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

  /* --- aim dial: behaves like a compass, the needle follows your thumb --- */
  function dialPointer(e) {
    const rect = dialEl.getBoundingClientRect();
    const cx = rect.left + rect.width / 2, cy = rect.top + rect.height / 2;
    setAim(Math.atan2(e.clientY - cy, e.clientX - cx));
  }
  dialEl?.addEventListener('pointerdown', (e) => { dialEl.setPointerCapture(e.pointerId); dialPointer(e); });
  dialEl?.addEventListener('pointermove', (e) => { if (e.buttons > 0 || e.pressure > 0) dialPointer(e); });

  /* --- fine adjust arrows, with hold-to-repeat --- */
  function bindHold(el, delta) {
    if (!el) return;
    let to = null, iv = null;
    const step = () => setAim(aimAngle + delta);
    const stop = () => { clearTimeout(to); clearInterval(iv); to = iv = null; };
    el.addEventListener('pointerdown', (e) => {
      e.preventDefault();
      step();
      to = setTimeout(() => { iv = setInterval(step, 55); }, 320);
    });
    ['pointerup', 'pointerleave', 'pointercancel'].forEach(ev => el.addEventListener(ev, stop));
  }
  bindHold(ccwEl, -fineStep);
  bindHold(cwEl, fineStep);

  /* --- aiming directly on the table --- */
  function canvasPointer(e) {
    const rect = canvasEl.getBoundingClientRect();
    onCanvasAim?.((e.clientX - rect.left) / rect.width, (e.clientY - rect.top) / rect.height);
  }
  canvasEl?.addEventListener('pointerdown', (e) => { canvasEl.setPointerCapture(e.pointerId); canvasPointer(e); });
  canvasEl?.addEventListener('pointermove', (e) => { if (e.buttons > 0) canvasPointer(e); });

  /* --- power slider: drag the cue down for more power, release to shoot --- */
  function powerPointer(e) {
    const rect = powerEl.getBoundingClientRect();
    setPower((e.clientY - rect.top) / rect.height);
  }
  powerEl?.addEventListener('pointerdown', (e) => {
    charging = true;
    powerEl.setPointerCapture(e.pointerId);
    powerEl.classList.add('charging');
    powerPointer(e);
  });
  powerEl?.addEventListener('pointermove', (e) => { if (charging) powerPointer(e); });
  powerEl?.addEventListener('pointerup', () => {
    if (!charging) return;
    charging = false;
    powerEl.classList.remove('charging');
    if (power > 0.06) onShoot?.(aimAngle, power);
    setPower(0);
  });
  powerEl?.addEventListener('pointercancel', () => {
    charging = false;
    powerEl.classList.remove('charging');
    setPower(0);
  });

  setAim(aimAngle);
  setPower(0);

  return {
    setAim, setPower,
    setCanvasAimHandler: (fn) => { onCanvasAim = fn; },
    getAimAngle: () => aimAngle,
    getPower: () => power,
  };
}
