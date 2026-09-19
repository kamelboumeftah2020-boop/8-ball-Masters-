// One-thumb control scheme: aim via the dial OR by dragging directly on the table,
// then pull the vertical power slider down and release to shoot (no separate "hit" button).
export function createControls({ dialEl, powerEl, canvasEl, needleEl, fillEl, onAimChange, onPowerChange, onShoot }) {
  let aimAngle = -Math.PI / 2;
  let power = 0;
  let charging = false;

  function setAim(angle) {
    aimAngle = angle;
    if (needleEl) needleEl.style.transform = `rotate(${(angle * 180) / Math.PI + 90}deg)`;
    onAimChange?.(aimAngle);
  }

  function setPower(p) {
    power = Math.max(0, Math.min(1, p));
    if (fillEl) fillEl.style.height = `${power * 100}%`;
    onPowerChange?.(power);
  }

  // --- Aim dial ---
  function dialPointer(e) {
    const rect = dialEl.getBoundingClientRect();
    const cx = rect.left + rect.width / 2, cy = rect.top + rect.height / 2;
    const dx = e.clientX - cx, dy = e.clientY - cy;
    setAim(Math.atan2(dy, dx));
  }
  dialEl?.addEventListener('pointerdown', (e) => { dialEl.setPointerCapture(e.pointerId); dialPointer(e); });
  dialEl?.addEventListener('pointermove', (e) => { if (e.pressure > 0 || e.buttons > 0) dialPointer(e); });

  // --- Direct aim on table ---
  function canvasPointer(e) {
    const rect = canvasEl.getBoundingClientRect();
    const px = ((e.clientX - rect.left) / rect.width);
    const py = ((e.clientY - rect.top) / rect.height);
    onCanvasAim?.(px, py);
  }
  let onCanvasAim = null;
  function setCanvasAimHandler(fn) { onCanvasAim = fn; }
  canvasEl?.addEventListener('pointerdown', (e) => { canvasEl.setPointerCapture(e.pointerId); canvasPointer(e); });
  canvasEl?.addEventListener('pointermove', (e) => { if (e.buttons > 0) canvasPointer(e); });

  // --- Power slider (drag down = more power, release = shoot) ---
  function powerPointer(e) {
    const rect = powerEl.getBoundingClientRect();
    const rel = (e.clientY - rect.top) / rect.height;
    setPower(rel);
  }
  powerEl?.addEventListener('pointerdown', (e) => {
    charging = true;
    powerEl.setPointerCapture(e.pointerId);
    powerPointer(e);
  });
  powerEl?.addEventListener('pointermove', (e) => { if (charging) powerPointer(e); });
  powerEl?.addEventListener('pointerup', () => {
    if (!charging) return;
    charging = false;
    if (power > 0.06) onShoot?.(aimAngle, power);
    setPower(0);
  });
  powerEl?.addEventListener('pointercancel', () => { charging = false; setPower(0); });

  return {
    setAim, setPower, setCanvasAimHandler,
    getAimAngle: () => aimAngle,
    getPower: () => power,
  };
}
