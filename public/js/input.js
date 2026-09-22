// الإدخال: لوحة المفاتيح + لمس (جوال) + يد تحكم
import { BTN } from '/shared/constants.js';

const KEYMAP = {
  KeyW: 'up', ArrowUp: 'up', KeyS: 'down', ArrowDown: 'down', KeyA: 'left', ArrowLeft: 'left', KeyD: 'right', ArrowRight: 'right',
  ShiftLeft: 'sprint', ShiftRight: 'sprint',
  Space: 'shoot', KeyJ: 'shoot',
  KeyE: 'pass', KeyK: 'pass',
  KeyQ: 'lob', KeyL: 'lob',
  KeyF: 'tackle', KeyI: 'tackle', ControlLeft: 'tackle',
  KeyR: 'ability', KeyU: 'ability',
};

export class Input {
  constructor() {
    this.keys = new Set();
    this.touch = { mx: 0, my: 0, b: 0 };
    this.handlers = {};
    this.enabled = false;
    this.isTouch = matchMedia('(pointer: coarse)').matches || 'ontouchstart' in window;
    window.addEventListener('keydown', (e) => this.onKey(e, true));
    window.addEventListener('keyup', (e) => this.onKey(e, false));
    window.addEventListener('blur', () => this.keys.clear());
    this.setupTouch();
  }

  on(name, fn) { this.handlers[name] = fn; }
  emit(name, ...a) { this.handlers[name] && this.handlers[name](...a); }

  onKey(e, down) {
    if (e.target && (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA')) return;
    const k = KEYMAP[e.code];
    if (!this.enabled) return;
    if (k) {
      e.preventDefault();
      if (down) this.keys.add(k); else this.keys.delete(k);
    }
    if (down && !e.repeat) {
      if (/^Digit[1-5]$/.test(e.code)) this.emit('emote', +e.code.slice(5));
      if (e.code === 'KeyC') this.emit('camera');
      if (e.code === 'Escape' || e.code === 'KeyP') this.emit('pause');
      if (e.code === 'KeyM') this.emit('mute');
      if (e.code === 'Enter' || e.code === 'KeyT') { this.emit('chat'); e.preventDefault(); }
      if (e.code === 'Tab') { this.emit('stats', true); e.preventDefault(); }
    }
    if (!down && e.code === 'Tab') this.emit('stats', false);
  }

  setupTouch() {
    const root = document.getElementById('touch');
    if (!root) return;
    const stick = root.querySelector('.stick');
    const knob = root.querySelector('.knob');
    const zone = root.querySelector('.stick-zone');
    let sid = null, cx = 0, cy = 0;
    const R = 55;
    zone.addEventListener('pointerdown', (e) => {
      sid = e.pointerId; cx = e.clientX; cy = e.clientY;
      stick.style.left = cx + 'px'; stick.style.top = cy + 'px';
      stick.classList.add('on');
      zone.setPointerCapture(e.pointerId);
      e.preventDefault();
    });
    zone.addEventListener('pointermove', (e) => {
      if (e.pointerId !== sid) return;
      let dx = e.clientX - cx, dy = e.clientY - cy;
      const l = Math.hypot(dx, dy);
      if (l > R) { dx *= R / l; dy *= R / l; }
      knob.style.transform = `translate(${dx}px, ${dy}px)`;
      this.touch.mx = dx / R; this.touch.my = -dy / R;
      // الركض التلقائي عند دفع العصا للنهاية
      if (l > R * 1.2) this.touch.autoSprint = true; else if (l < R * 0.9) this.touch.autoSprint = false;
    });
    const end = (e) => {
      if (e.pointerId !== sid) return;
      sid = null; this.touch.mx = 0; this.touch.my = 0; this.touch.autoSprint = false;
      knob.style.transform = ''; stick.classList.remove('on');
    };
    zone.addEventListener('pointerup', end);
    zone.addEventListener('pointercancel', end);
    this.touch.t = { main: false, second: false };
    for (const btn of root.querySelectorAll('[data-t]')) {
      const key = btn.dataset.t;
      const bit = btn.dataset.btn ? BTN[btn.dataset.btn] : 0;
      const down = (e) => {
        e.preventDefault();
        if (bit) this.touch.b |= bit; else this.touch.t[key] = true;
        btn.classList.add('on');
        btn.setPointerCapture && btn.setPointerCapture(e.pointerId);
        if (navigator.vibrate) try { navigator.vibrate(8); } catch { /* ignore */ }
      };
      const up = (e) => { e.preventDefault(); if (bit) this.touch.b &= ~bit; else this.touch.t[key] = false; btn.classList.remove('on'); };
      btn.addEventListener('pointerdown', down);
      btn.addEventListener('pointerup', up);
      btn.addEventListener('pointercancel', up);
      btn.addEventListener('lostpointercapture', up);
    }
    for (const btn of root.querySelectorAll('[data-act]')) {
      btn.addEventListener('pointerdown', (e) => { e.preventDefault(); this.emit(btn.dataset.act); });
    }
  }

  showTouch(on) {
    const root = document.getElementById('touch');
    if (root) root.classList.toggle('hidden', !(on && this.isTouch));
  }

  read() {
    let mx = 0, my = 0, b = 0;
    const k = this.keys;
    if (k.has('left')) mx -= 1;
    if (k.has('right')) mx += 1;
    if (k.has('up')) my += 1;
    if (k.has('down')) my -= 1;
    if (k.has('sprint')) b |= BTN.SPRINT;
    if (k.has('shoot')) b |= BTN.SHOOT | BTN.SKIP;
    if (k.has('pass')) b |= BTN.PASS;
    if (k.has('lob')) b |= BTN.LOB;
    if (k.has('tackle')) b |= BTN.TACKLE;
    if (k.has('ability')) b |= BTN.ABILITY;
    // اللمس
    if (this.touch.mx || this.touch.my) { mx = this.touch.mx; my = this.touch.my; }
    b |= this.touch.b;
    if (this.touch.autoSprint) b |= BTN.SPRINT;
    const t = this.touch.t || {};
    // يد التحكم
    const pads = navigator.getGamepads ? navigator.getGamepads() : [];
    for (const gp of pads) {
      if (!gp) continue;
      const ax = gp.axes[0] || 0, ay = gp.axes[1] || 0;
      if (Math.hypot(ax, ay) > 0.18) { mx = ax; my = -ay; }
      const pr = (i) => gp.buttons[i] && gp.buttons[i].pressed;
      if (pr(12)) my = 1; if (pr(13)) my = -1; if (pr(14)) mx = -1; if (pr(15)) mx = 1;
      if (pr(0)) b |= BTN.PASS;
      if (pr(1)) b |= BTN.SHOOT | BTN.SKIP;
      if (pr(2)) b |= BTN.LOB;
      if (pr(3)) b |= BTN.ABILITY;
      if (pr(4) || pr(5)) b |= BTN.TACKLE;
      if (pr(7) || pr(6)) b |= BTN.SPRINT;
      if (pr(9) && !this._padStart) this.emit('pause');
      this._padStart = pr(9);
      break;
    }
    const l = Math.hypot(mx, my);
    if (l > 1) { mx /= l; my /= l; }
    return { mx: Math.round(mx * 100) / 100, my: Math.round(my * 100) / 100, b, tMain: !!t.main, tSec: !!t.second };
  }
}
